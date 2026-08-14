package com.warisango.controller;

import com.warisango.dto.HeritageBusinessDTO;
import com.warisango.model.service.BusinessService;
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

    public BusinessDirectoryController(BusinessService businessService) {
        this.businessService = businessService;
    }

    // Directory page (list)
    @GetMapping({"/", "/directory", "/business-directory"})
    public String showBusinessDirectory(Model model) {
        List<HeritageBusinessDTO> businesses = businessService.getApprovedBusinesses();
        model.addAttribute("businesses", businesses);
        return "BusinessDirectoryPage"; // must match src/main/resources/view/BusinessDirectoryPage.html
    }

    // Details page
    @GetMapping("/business/{id}")
    public String showBusinessDetails(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes) {
        Optional<HeritageBusinessDTO> opt = businessService.getBusinessById(id);
        if (opt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Business not found");
            return "redirect:/directory";
        }
        model.addAttribute("business", opt.get());
        return "BusinessDetailsPage"; // must match file name
    }

    // SSE endpoint for realtime updates (optional front-end subscription)
    @GetMapping("/sse/businesses")
    public SseEmitter streamBusinesses() {
        return businessService.streamApprovedBusinesses();
    }
}