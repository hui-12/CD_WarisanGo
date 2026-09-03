package com.warisango.controller;

import com.warisango.service.AuditLogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/admin/audit-log")
    public String auditLog(Model model) {
        model.addAttribute("entries", auditLogService.findAll());
        return "admin/audit-log";
    }
}
