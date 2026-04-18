package com.example.swen_project_v1.web;

import com.example.swen_project_v1.service.WaitlistProcessingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/waitlist")
public class WaitlistController {

    private final WaitlistProcessingService waitlistProcessingService;

    public WaitlistController(WaitlistProcessingService waitlistProcessingService) {
        this.waitlistProcessingService = waitlistProcessingService;
    }

    /**
     * Admin manually triggers waitlist processing — e.g. after increasing section capacity.
     */
    @PostMapping("/process/{sectionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> processWaitlist(@PathVariable Long sectionId) {
        waitlistProcessingService.processWaitlistForSection(sectionId);
        return ResponseEntity.ok("Waitlist processed for section " + sectionId);
    }
}