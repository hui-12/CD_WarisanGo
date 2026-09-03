package com.warisango.controller;

import com.warisango.dto.HeritageBusinessDTO;
import com.warisango.service.BusinessService;
import com.warisango.service.ReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class BusinessDirectoryController {

    private final BusinessService businessService;
    private final ReviewService reviewService;
    private final BusinessStreamManager businessStreamManager;

    public BusinessDirectoryController(
            BusinessService businessService,
            ReviewService reviewService,
            BusinessStreamManager businessStreamManager) {
        this.businessService = businessService;
        this.reviewService = reviewService;
        this.businessStreamManager = businessStreamManager;
    }

    // Directory page (list)
    @GetMapping({"/directory", "/business-directory"})
    public String showBusinessDirectory(Model model) {
        List<HeritageBusinessDTO> businesses = businessService.getApprovedBusinesses();
        model.addAttribute("businesses", businesses);
        return "business-directory";
    }

    // Details page
    @GetMapping("/business/{id}")
    public String showBusinessDetails(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes) {
        Optional<HeritageBusinessDTO> opt = businessService.getBusinessById(id);
        if (opt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Business not found");
            return "redirect:/directory";
        }
        HeritageBusinessDTO business = opt.get();
        String businessId = business.getBusinessId();

        model.addAttribute("business", business);
        model.addAttribute("featuredReview", reviewService.getFeaturedReview(businessId));
        model.addAttribute("totalReviews", reviewService.getTotalReviews(businessId));
        return "business-details";
    }

    // SSE endpoint for realtime updates (optional front-end subscription)
    @GetMapping("/sse/businesses")
    public SseEmitter streamBusinesses() {
        return businessStreamManager.openStream();
    }
}
