package com.pathway.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.pathway.model.JobListing;
import com.pathway.repo.JobListingRepository;
import com.pathway.service.JobService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobListingRepository jobs;
    private final JobService jobService;

    public JobController(
            JobListingRepository jobs,
            JobService jobService
    ) {
        this.jobs = jobs;
        this.jobService = jobService;
    }

    @GetMapping
    public List<JobListing> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) String location
    ) {
        return jobs.search(blankToNull(q), blankToNull(mode), blankToNull(location));
    }

    @GetMapping("/remote")
    public JsonNode searchRemote(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String country,
            @RequestParam(defaultValue = "false") boolean worldwide,
            @RequestParam(defaultValue = "1") int page
    ) {
        return jobService.searchRemoteJobs(q, country, worldwide, page);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
