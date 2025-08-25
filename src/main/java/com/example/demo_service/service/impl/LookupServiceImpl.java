package com.example.demo_service.service.impl;

import com.example.demo_service.constants.ApplicationConstants;
import com.example.demo_service.dto.InvoiceDTO;
import com.example.demo_service.dto.TimeSheetRequest;
import com.example.demo_service.service.LookupService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import com.itextpdf.html2pdf.HtmlConverter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Service
public class LookupServiceImpl implements LookupService {

    @Autowired
    private TemplateEngine templateEngine;

    private static final String FILE_NAME = "employee_timesheet.xlsx";

    @Override
    public Object downloadInvoicePdf(MultipartFile multipartFile) throws IOException {
        log.info("LookupServiceImpl :: downloadInvoicePdf() :: Init - file={}", multipartFile.getOriginalFilename());
        List<InvoiceDTO> invoiceDTOS = extractFromExcel(multipartFile.getInputStream());
        String fileName = "invoice.pdf";
        File file = null;

        try {
            Map<String, Object> data = new HashMap<>();
            data.put("companyName", ApplicationConstants.COMPANY_NAME);
            data.put("companyAddress", ApplicationConstants.COMPANY_ADDRESS);
            data.put("companyPhoneNo", ApplicationConstants.COMPANY_PHONE_NUMBER);
            data.put("companyUrl", ApplicationConstants.COMPANY_URL);
            data.put("bankName", ApplicationConstants.BANK_NAME);
            data.put("bankAccountType", ApplicationConstants.BANK_ACCOUNT_TYPE);
            data.put("bankAccount", ApplicationConstants.BANK_ACCOUNT);
            data.put("remitEmail", ApplicationConstants.REMIT_EMAIL);
            data.put("today", new java.util.Date());
            data.put("invoice", groupByEmployee(invoiceDTOS));
            byte[] logoBytes = Files.readAllBytes(Paths.get("src/main/resources/static/appLogics.png"));
            String logoBase64 = Base64.getEncoder().encodeToString(logoBytes);
            data.put("logoBase64", logoBase64);

            double totalAmount = invoiceDTOS.stream()
                    .mapToDouble(i -> i.getAmount() == null ? 0.0 : i.getAmount())
                    .sum();
            data.put("totalAmount", totalAmount);

            file = generateInvoicePDF("invoice", data);
            byte[] fileContent = Files.readAllBytes(file.toPath());

            log.info("LookupServiceImpl :: downloadInvoicePdf() :: Success - totalAmount={}", totalAmount);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(fileContent);

        } catch (IOException e) {
            log.error("LookupServiceImpl :: downloadInvoicePdf() :: Failed", e);
            throw new RuntimeException(e);
        } finally {
            if (file != null) {
                deleteFile(file.getAbsolutePath());
            }
            log.info("LookupServiceImpl :: downloadInvoicePdf() :: Ends");
        }
    }

    @Override
    public void saveTimesheetEntry(TimeSheetRequest entry) throws IOException {
        log.info("LookupServiceImpl :: saveTimesheetEntry() :: Init - employeeId={}", entry.getEmployeeId());

        File file = new File(FILE_NAME);
        Workbook workbook;
        Sheet sheet;

        if (file.exists()) {
            workbook = new XSSFWorkbook(new FileInputStream(file));
            sheet = workbook.getSheetAt(0);
        } else {
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet("Timesheet");
            createHeaderRow(sheet);
        }

        int rowNum = sheet.getLastRowNum() + 1;
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(entry.getEmployeeId());
        row.createCell(1).setCellValue(entry.getName());
        row.createCell(2).setCellValue(entry.getDate());
        row.createCell(3).setCellValue(entry.getClockIn());
        row.createCell(4).setCellValue(entry.getClockOut());
        row.createCell(5).setCellValue(entry.getHoursWorked());
        row.createCell(6).setCellValue(entry.getProject());
        row.createCell(7).setCellValue(entry.getTaskDescription());

        try (FileOutputStream fos = new FileOutputStream(FILE_NAME)) {
            workbook.write(fos);
        }
        workbook.close();

        log.info("LookupServiceImpl :: saveTimesheetEntry() :: Success - row={}", rowNum);
    }

    @Override
    public File getTimesheetFile() {
        log.info("LookupServiceImpl :: getTimesheetFile()");
        return new File(FILE_NAME);
    }

    private void createHeaderRow(Sheet sheet) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Employee ID");
        header.createCell(1).setCellValue("Name");
        header.createCell(2).setCellValue("Date");
        header.createCell(3).setCellValue("Clock In");
        header.createCell(4).setCellValue("Clock Out");
        header.createCell(5).setCellValue("Hours Worked");
        header.createCell(6).setCellValue("Project");
        header.createCell(7).setCellValue("Task Description");
    }

    private void deleteFile(String fileName) {
        try {
            Files.deleteIfExists(new File(fileName).toPath());
            log.info("LookupServiceImpl :: deleteFile() :: Deleted - path={}", fileName);
        } catch (IOException e) {
            log.warn("LookupServiceImpl :: deleteFile() :: Failed - path={}", fileName);
        }
    }

    private Double parseDouble(String value) {
        try {
            if (value == null || value.isEmpty() || value.equals("-")) return null;
            return Double.parseDouble(value.replace("$", ""));
        } catch (NumberFormatException e) {
            log.warn("LookupServiceImpl :: parseDouble() :: Could not parse - value={}", value);
            return null;
        }
    }

    private Map<String, List<InvoiceDTO>> groupByEmployee(List<InvoiceDTO> invoiceList) {
        log.info("LookupServiceImpl :: groupByEmployee() :: Init - records={}", invoiceList.size());

        Map<String, List<InvoiceDTO>> groupedData = new LinkedHashMap<>();
        for (InvoiceDTO invoice : invoiceList) {
            String employeeName = invoice.getName();
            if (employeeName != null && !employeeName.trim().isEmpty()) {
                groupedData.computeIfAbsent(employeeName.trim(), k -> new ArrayList<>()).add(invoice);
            }
        }

        log.info("LookupServiceImpl :: groupByEmployee() :: Success - employees={}", groupedData.keySet().size());
        return groupedData;
    }

    private List<InvoiceDTO> extractFromExcel(InputStream is) throws IOException {
        log.info("LookupServiceImpl :: extractFromExcel() :: Init");

        List<InvoiceDTO> invoiceList = new ArrayList<>();
        try (XSSFWorkbook workbook = new XSSFWorkbook(is)) {
            XSSFSheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            log.info("LookupServiceImpl :: extractFromExcel() :: Rows={}", sheet.getLastRowNum());

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                InvoiceDTO dto = new InvoiceDTO();
                String name = getCellValueAsString(row.getCell(1), formatter);
                String weekEnd = getCellValueAsString(row.getCell(2), formatter);
                Double stRate = parseDouble(getCellValueAsString(row.getCell(3), formatter));
                Double stUnits = parseDouble(getCellValueAsString(row.getCell(4), formatter));
                Double otRate = parseDouble(getCellValueAsString(row.getCell(5), formatter));
                Double otUnits = parseDouble(getCellValueAsString(row.getCell(6), formatter));
                Double amount = parseDouble(getCellValueAsString(row.getCell(7), formatter));

                if (amount == null) {
                    amount = (stRate == null ? 0 : stRate) * (stUnits == null ? 0 : stUnits)
                            + (otRate == null ? 0 : otRate) * (otUnits == null ? 0 : otUnits);
                }

                dto.setName(name);
                dto.setWeekEnd(weekEnd);
                dto.setStRate(stRate);
                dto.setStUnits(stUnits);
                dto.setOtRate(otRate);
                dto.setOtUnits(otUnits);
                dto.setAmount(amount);

                invoiceList.add(dto);
            }
        }

        log.info("LookupServiceImpl :: extractFromExcel() :: Success - records={}", invoiceList.size());
        return invoiceList;
    }

    private String getCellValueAsString(Cell cell, DataFormatter formatter) {
        return (cell == null) ? "" : formatter.formatCellValue(cell).trim();
    }

    private File generateInvoicePDF(String templateName, Map<String, Object> data) {
        log.info("LookupServiceImpl :: generateInvoicePDF() :: Init - template={}", templateName);
        Context context = new Context();
        context.setVariables(data);
        File file = null;
        try {
            String htmlContent = templateEngine.process(templateName, context);
            String fileName = "invoice.pdf";
            try (OutputStream out = new FileOutputStream(fileName)) {
                HtmlConverter.convertToPdf(htmlContent, out);
            }
            file = new File(fileName);
            log.info("LookupServiceImpl :: generateInvoicePDF() :: Success - path={}", file.getAbsolutePath());
        } catch (Exception e) {
            log.error("LookupServiceImpl :: generateInvoicePDF() :: Failed", e);
        }
        return file;
    }
}