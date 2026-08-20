package com.warisango.model.service;

import com.warisango.dto.ReviewDTO;
import com.warisango.dto.ReviewPhotoDTO;
import com.warisango.model.repository.ReviewPhotoRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Coordinates local image files and the root-level ReviewPhotos collection.
 */
@Service
public class ReviewPhotoService {

    private static final int MAX_PHOTOS_PER_REVIEW = 10;

    private final ReviewPhotoRepository reviewPhotoRepository;
    private final ReviewPhotoStorageService storageService;

    public ReviewPhotoService(
            ReviewPhotoRepository reviewPhotoRepository,
            ReviewPhotoStorageService storageService) {
        this.reviewPhotoRepository = reviewPhotoRepository;
        this.storageService = storageService;
    }

    public void populatePhotos(ReviewDTO review) {
        if (review == null || review.getReviewId() == null || review.getReviewId().isBlank()) {
            return;
        }

        List<ReviewPhotoDTO> photos = reviewPhotoRepository.findByReviewId(review.getReviewId());
        review.setPhotos(photos);
        review.setPhotoUrls(photos.stream()
                .map(ReviewPhotoDTO::getPhotoUrl)
                .filter(url -> url != null && !url.isBlank())
                .collect(Collectors.toList()));
    }

    public void populatePhotos(Collection<ReviewDTO> reviews) {
        if (reviews == null) {
            return;
        }

        reviews.forEach(this::populatePhotos);
    }

    public void validateNewPhotos(MultipartFile[] newPhotos) {
        List<MultipartFile> files = nonEmptyFiles(newPhotos);
        storageService.validateFiles(files);

        if (files.size() > MAX_PHOTOS_PER_REVIEW) {
            throw new IllegalArgumentException("You can upload a maximum of 10 photos per review.");
        }
    }

    public void validatePhotoChange(
            String reviewId,
            Collection<String> removePhotoIds,
            MultipartFile[] newPhotos) {

        List<ReviewPhotoDTO> existingPhotos = reviewPhotoRepository.findByReviewId(reviewId);
        Set<String> removableIds = normalizeRemovableIds(removePhotoIds, existingPhotos);
        int retainedPhotoCount = existingPhotos.size() - removableIds.size();
        List<MultipartFile> files = nonEmptyFiles(newPhotos);

        storageService.validateFiles(files);

        if (retainedPhotoCount + files.size() > MAX_PHOTOS_PER_REVIEW) {
            throw new IllegalArgumentException("A review can contain a maximum of 10 photos.");
        }
    }

    public List<ReviewPhotoDTO> savePhotos(String reviewId, MultipartFile[] newPhotos) {
        List<MultipartFile> files = nonEmptyFiles(newPhotos);
        storageService.validateFiles(files);

        List<ReviewPhotoDTO> savedPhotos = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                ReviewPhotoStorageService.StoredPhoto storedPhoto = null;
                ReviewPhotoDTO photo = null;

                try {
                    storedPhoto = storageService.store(reviewId, file);
                    photo = new ReviewPhotoDTO(
                            reviewPhotoRepository.generateNextPhotoId(),
                            reviewId,
                            storedPhoto.publicUrl()
                    );
                    reviewPhotoRepository.save(photo);
                    savedPhotos.add(photo);
                } catch (RuntimeException e) {
                    if (storedPhoto != null) {
                        storageService.delete(storedPhoto.publicUrl());
                    }
                    throw e;
                }
            }

            return savedPhotos;
        } catch (RuntimeException e) {
            deleteSavedPhotos(savedPhotos);
            throw e;
        }
    }

    public void replacePhotos(
            String reviewId,
            Collection<String> removePhotoIds,
            MultipartFile[] newPhotos) {

        validatePhotoChange(reviewId, removePhotoIds, newPhotos);

        List<ReviewPhotoDTO> existingPhotos = reviewPhotoRepository.findByReviewId(reviewId);
        Set<String> removableIds = normalizeRemovableIds(removePhotoIds, existingPhotos);

        savePhotos(reviewId, newPhotos);

        for (ReviewPhotoDTO photo : existingPhotos) {
            if (removableIds.contains(photo.getPhotoId())) {
                storageService.delete(photo.getPhotoUrl());
                reviewPhotoRepository.delete(photo.getPhotoId());
            }
        }
    }

    public void deletePhotos(String reviewId) {
        List<ReviewPhotoDTO> photos = reviewPhotoRepository.findByReviewId(reviewId);

        for (ReviewPhotoDTO photo : photos) {
            storageService.delete(photo.getPhotoUrl());
            reviewPhotoRepository.delete(photo.getPhotoId());
        }
    }

    private List<MultipartFile> nonEmptyFiles(MultipartFile[] files) {
        if (files == null) {
            return List.of();
        }

        List<MultipartFile> nonEmptyFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                nonEmptyFiles.add(file);
            }
        }
        return nonEmptyFiles;
    }

    private Set<String> normalizeRemovableIds(
            Collection<String> removePhotoIds,
            List<ReviewPhotoDTO> existingPhotos) {

        if (removePhotoIds == null || removePhotoIds.isEmpty()) {
            return Set.of();
        }

        Set<String> existingIds = existingPhotos.stream()
                .map(ReviewPhotoDTO::getPhotoId)
                .collect(Collectors.toSet());

        return removePhotoIds.stream()
                .filter(existingIds::contains)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private void deleteSavedPhotos(List<ReviewPhotoDTO> savedPhotos) {
        for (ReviewPhotoDTO photo : savedPhotos) {
            storageService.delete(photo.getPhotoUrl());
            reviewPhotoRepository.delete(photo.getPhotoId());
        }
    }
}
