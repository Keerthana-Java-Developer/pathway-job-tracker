package com.pathway.dto;

import com.pathway.model.ApplicationStatus;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Safe API response for an application.
 * Deliberately excludes JPA relationships (user and jobListing) so Jackson
 * never tries to serialize lazy Hibernate proxies after the session closes.
 */
public record ApplicationResponse(
        Long id,
        String company,
        String jobTitle,
        String jobUrl,
        String location,
        String workMode,
        ApplicationStatus status,
        LocalDate appliedDate,
        LocalDate followUpDate,
        String notes,
        Instant createdAt
) {
    public static ApplicationResponse from(com.pathway.model.JobApplication application) {
        return new ApplicationResponse(
                application.getId(),
                application.getCompany(),
                application.getJobTitle(),
                application.getJobUrl(),
                application.getLocation(),
                application.getWorkMode(),
                application.getStatus(),
                application.getAppliedDate(),
                application.getFollowUpDate(),
                application.getNotes(),
                application.getCreatedAt()
        );
    }
}
