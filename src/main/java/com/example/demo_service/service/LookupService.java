package com.example.demo_service.service;

import com.example.demo_service.dto.TimeSheetRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

public interface LookupService {

    Object downloadInvoicePdf(MultipartFile file) throws IOException;

    void saveTimesheetEntry(TimeSheetRequest request) throws IOException;

    File getTimesheetFile();
}
