package com.warisango.model.service;

import com.warisango.dto.HeritageBusinessDTO;
import com.warisango.model.repository.HeritageBusinessRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HeritageBusinessService {

    private static final Logger logger = LoggerFactory.getLogger(HeritageBusinessService.class);
    private final HeritageBusinessRepository heritageBusinessRepository;

    public HeritageBusinessService(HeritageBusinessRepository heritageBusinessRepository) {
        this.heritageBusinessRepository = heritageBusinessRepository;
    }

    public List<HeritageBusinessDTO> getApprovedBusinesses() {
        try {
            return heritageBusinessRepository.findApprovedBusinesses();
        } catch (Exception e) {
            logger.error("Error fetching approved heritage businesses", e);
            throw new RuntimeException("Failed to load map data.");
        }
    }
}