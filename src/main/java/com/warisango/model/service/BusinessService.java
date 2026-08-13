package com.warisango.service;

import com.warisango.model.Business;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class BusinessService {
    private final List<Business> businesses = new ArrayList<>();

    public BusinessService() {
        businesses.add(new Business("b1", "Kedai Nasi Lemak Warisan", "Street Food", "Selangor", "Kuala Lumpur", 4.6,
                "No 12, Jalan Heritage, KL", "Traditional nasi lemak served with family sambal recipe.", Arrays.asList("/images/business-default.jpg","/images/hero-banner.jpg")));
        businesses.add(new Business("b2", "Melaka Satay House", "Grill", "Melaka", "Melaka City", 4.4,
                "Lot 5, Jonker Street", "Famous local satay using charcoal grill.", Arrays.asList("/images/business-default.jpg")));
    }

    public List<Business> findAll() {
        return new ArrayList<>(businesses);
    }

    public Optional<Business> findById(String id) {
        return businesses.stream().filter(b -> b.getId().equals(id)).findFirst();
    }
}
