package com.warisango.config;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.GeoPoint;
import com.warisango.model.Business;
import com.warisango.model.repository.BusinessRepository;
import com.warisango.model.repository.HeritageBusinessImageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true", matchIfMissing = true)
public class HeritageBusinessDataInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(HeritageBusinessDataInitializer.class);
    private final BusinessRepository businessRepository;
    private final HeritageBusinessImageRepository imageRepository;
    private final boolean enabled;

    public HeritageBusinessDataInitializer(
            BusinessRepository businessRepository,
            HeritageBusinessImageRepository imageRepository,
            @Value("${warisango.firebase.initialize-heritage-businesses:true}") boolean enabled) {
        this.businessRepository = businessRepository;
        this.imageRepository = imageRepository;
        this.enabled = enabled;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        if (!enabled) {
            logger.info("Heritage business migration and test-data initialization are disabled.");
            return;
        }

        try {
            int migratedImages = imageRepository.migrateEmbeddedImagesAndRemoveDeprecatedFields();
            int migrated = businessRepository.migrateLegacyCollection();
            int seeded = 0;
            for (Business business : testBusinesses()) {
                if (businessRepository.saveIfAbsent(business)) {
                    seeded++;
                }
            }
            seedTestImages();
            logger.info("Heritage business initialization completed: {} businesses migrated, "
                            + "{} images migrated, {} test records added.",
                    migrated, migratedImages, seeded);
        } catch (Exception exception) {
            logger.error("Heritage business initialization failed; application startup will continue.", exception);
        }
    }

    private List<Business> testBusinesses() {
        return List.of(
                business("test_hb_001", "[TEST] Yut Kee Restaurant", "7, Jalan Kamunting, Chow Kit",
                        "Kuala Lumpur", "Kuala Lumpur", 50,
                        3.1568, 101.7001, "sample1"),
                business("test_hb_002", "[TEST] George Town Nyonya Kitchen", "25, Lebuh Armenian",
                        "George Town", "Penang", 60,
                        5.4141, 100.3383, "sample2"),
                business("test_hb_003", "[TEST] Ipoh Heritage Kopitiam", "18, Jalan Bandar Timah",
                        "Ipoh", "Perak", 50,
                        4.5975, 101.0901, "sample3"),
                business("test_hb_004", "[TEST] Melaka Satay Celup House", "42, Jalan Hang Jebat",
                        "Melaka", "Melaka", 55,
                        2.1960, 102.2405, "sample4"),
                business("test_hb_005", "[TEST] Kota Bharu Nasi Kerabu", "10, Jalan Padang Garong",
                        "Kota Bharu", "Kelantan", 65,
                        6.1254, 102.2381, "sample5")
        );
    }

    private Business business(String id, String name, String address, String city, String state,
                              int points, double latitude,
                              double longitude, String videoId) {
        Business business = new Business(id, name, state, city, null, address,
                "Sample heritage-food business record for application testing.");
        business.setOperatingHour("08:00 - 23:00");
        business.setLocation(new GeoPoint(latitude, longitude));
        business.setCheckInPoints(points);
        business.setSourceVideoLink("https://www.youtube.com/watch?v=" + videoId);
        business.setStatus("Approved");
        business.setCreatedAt(Timestamp.now());
        business.setApproveAt(Timestamp.now());
        business.setRejectedAt(null);
        return business;
    }

    private void seedTestImages() {
        String[] imageUrls = {
            "https://images.unsplash.com/photo-1559925393-8be0ec4767c8?auto=format&fit=crop&w=1200&q=85",
            "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=1200&q=85"
        };

        for (int businessIndex = 1; businessIndex <= 5; businessIndex++) {
            String businessId = "test_hb_00" + businessIndex;
            for (int imageIndex = 0; imageIndex < imageUrls.length; imageIndex++) {
                imageRepository.saveIfAbsent(
                        businessId + "_image_" + (imageIndex + 1),
                        businessId,
                        imageUrls[imageIndex],
                        imageIndex
                );
            }
        }
    }
}
