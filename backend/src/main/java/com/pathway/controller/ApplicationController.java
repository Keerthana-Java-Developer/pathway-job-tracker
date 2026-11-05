package com.pathway.controller;

import com.pathway.dto.ApplicationRequest;
import com.pathway.dto.ApplicationResponse;
import com.pathway.model.AppUser;
import com.pathway.model.ApplicationStatus;
import com.pathway.model.JobApplication;
import com.pathway.repo.JobApplicationRepository;
import com.pathway.repo.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {
    private final JobApplicationRepository applications;
    private final UserRepository users;

    public ApplicationController(JobApplicationRepository applications, UserRepository users) {
        this.applications = applications;
        this.users = users;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<ApplicationResponse> list(Authentication auth) {
        return applications.findByUserEmailOrderByCreatedAtDesc(auth.getName())
                .stream()
                .map(ApplicationResponse::from)
                .toList();
    }

    @PostMapping
    @Transactional
    public ApplicationResponse create(@Valid @RequestBody ApplicationRequest r, Authentication auth) {
        AppUser user = users.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        JobApplication saved = applications.save(new JobApplication(
                user,
                r.company().trim(),
                r.jobTitle().trim(),
                r.jobUrl(),
                r.location(),
                r.workMode(),
                r.status(),
                r.appliedDate(),
                r.followUpDate(),
                r.notes()
        ));
        return ApplicationResponse.from(saved);
    }

    @PutMapping("/{id}")
    @Transactional
    public ApplicationResponse update(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationRequest r,
            Authentication auth
    ) {
        JobApplication application = applications.findByIdAndUserEmail(id, auth.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Application not found."
                ));

        application.update(
                r.company().trim(),
                r.jobTitle().trim(),
                r.jobUrl(),
                r.location(),
                r.workMode(),
                r.status() == null ? ApplicationStatus.APPLIED : r.status(),
                r.appliedDate() == null ? LocalDate.now() : r.appliedDate(),
                r.followUpDate(),
                r.notes()
        );

        JobApplication saved = applications.save(application);
        return ApplicationResponse.from(saved);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void delete(@PathVariable Long id, Authentication auth) {
        JobApplication application = applications.findByIdAndUserEmail(id, auth.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Application not found."
                ));
        applications.delete(application);
    }
}
