package com.warisango.controller;

import com.warisango.dto.HeritageBusinessUpdateRequest;
import com.warisango.model.service.PendingHeritageBusinessService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/pending-list")
public class PendingHeritageBusinessController {

    private final PendingHeritageBusinessService pendingHeritageBusinessService;

    public PendingHeritageBusinessController(
            PendingHeritageBusinessService pendingHeritageBusinessService) {

        this.pendingHeritageBusinessService = pendingHeritageBusinessService;
    }

    @GetMapping
    public String pendingList(Model model) {
        model.addAttribute("businesses", pendingHeritageBusinessService.findAll());
        return "admin/pending-list";
    }

    @GetMapping("/{businessId}")
    public String pendingDetails(
            @PathVariable String businessId,
            Model model) {

        var business = pendingHeritageBusinessService.findById(businessId);
        model.addAttribute("business", business);
        if (!model.containsAttribute("businessUpdate")) {
            model.addAttribute("businessUpdate", toUpdateRequest(business));
        }
        return "admin/pending-details";
    }

    @PostMapping("/{businessId}/update")
    public String update(
            @PathVariable String businessId,
            @Valid @ModelAttribute("businessUpdate") HeritageBusinessUpdateRequest businessUpdate,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("business", pendingHeritageBusinessService.findById(businessId));
            return "admin/pending-details";
        }

        pendingHeritageBusinessService.update(businessId, businessUpdate);
        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Business information updated successfully."
        );
        return "redirect:/admin/pending-list/" + businessId;
    }

    @PostMapping("/{businessId}/approve")
    public String approve(
            @PathVariable String businessId,
            RedirectAttributes redirectAttributes) {

        pendingHeritageBusinessService.approve(businessId);
        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Heritage business approved successfully."
        );
        return "redirect:/admin/pending-list";
    }

    @PostMapping("/{businessId}/reject")
    public String reject(
            @PathVariable String businessId,
            RedirectAttributes redirectAttributes) {

        pendingHeritageBusinessService.reject(businessId);
        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Heritage business rejected successfully."
        );
        return "redirect:/admin/pending-list";
    }

    private HeritageBusinessUpdateRequest toUpdateRequest(
            com.warisango.dto.HeritageBusinessView business) {
        HeritageBusinessUpdateRequest request = new HeritageBusinessUpdateRequest();
        request.setName(business.name());
        request.setAddress(business.address());
        request.setState(business.state());
        request.setCity(business.city());
        request.setDescription(business.description());
        request.setLatitude(business.latitude());
        request.setLongitude(business.longitude());
        request.setOperatingHour(business.operatingHour());
        request.setAverageRating(business.averageRating());
        request.setCheckInPoints(business.checkInPoints());
        request.setSourceVideoLink(business.sourceVideoLink());
        return request;
    }
}
