package com.example.demo_service.controller;

import com.example.demo_service.constants.EndpointConstants;
import com.example.demo_service.dto.TimeSheetRequest;
import com.example.demo_service.service.LookupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(value = EndpointConstants.BASE_URL, produces = MediaType.APPLICATION_JSON_VALUE)
public class LookupController {

    private final LookupService lookupService;

    public LookupController(LookupService lookupService) {
        this.lookupService = lookupService;
    }

    @PostMapping(value = EndpointConstants.DOWNLOAD_INVOICE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> downloadInvoice(@RequestParam("file") MultipartFile file) throws IOException {
        log.info("BookingController :: downloadInvoice() :: Init");
        return (ResponseEntity<Object>) lookupService.downloadInvoicePdf(file);
    }

    @PostMapping(value = EndpointConstants.CREATE_TIMESHEET)
    public ResponseEntity<String> submitTimesheet(@RequestBody TimeSheetRequest entry) {
        log.info("BookingController :: submitTimesheet() :: Init");
        try {
            lookupService.saveTimesheetEntry(entry);
            return ResponseEntity.ok("Timesheet saved.");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping(value = EndpointConstants.DOWNLOAD_TIMESHEET)
    public ResponseEntity<Resource> downloadTimesheet() {
        log.info("BookingController :: downloadTimesheet() :: Init");
        File file = lookupService.getTimesheetFile();
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }
}
