package com.catamaran.catamaranbackend.controller;

import com.catamaran.catamaranbackend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    // Excel Reports
    @GetMapping("/boats/excel")
    public ResponseEntity<byte[]> downloadBoatsExcel() {
        try {
            byte[] excelData = reportService.generateBoatsExcelReport();
            String filename = "embarcaciones_" + LocalDateTime.now().format(FILE_DATE_FORMATTER) + ".xlsx";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/payments/excel")
    public ResponseEntity<byte[]> downloadPaymentsExcel() {
        try {
            byte[] excelData = reportService.generatePaymentsExcelReport();
            String filename = "pagos_" + LocalDateTime.now().format(FILE_DATE_FORMATTER) + ".xlsx";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/maintenance/excel")
    public ResponseEntity<byte[]> downloadMaintenanceExcel() {
        try {
            byte[] excelData = reportService.generateMaintenanceExcelReport();
            String filename = "mantenimientos_" + LocalDateTime.now().format(FILE_DATE_FORMATTER) + ".xlsx";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // PDF Reports
    @GetMapping("/boats/pdf")
    public ResponseEntity<byte[]> downloadBoatsPdf() {
        try {
            byte[] pdfData = reportService.generateBoatsPdfReport();
            String filename = "embarcaciones_" + LocalDateTime.now().format(FILE_DATE_FORMATTER) + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

            return new ResponseEntity<>(pdfData, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/payments/pdf")
    public ResponseEntity<byte[]> downloadPaymentsPdf() {
        try {
            byte[] pdfData = reportService.generatePaymentsPdfReport();
            String filename = "pagos_" + LocalDateTime.now().format(FILE_DATE_FORMATTER) + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

            return new ResponseEntity<>(pdfData, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/maintenance/pdf")
    public ResponseEntity<byte[]> downloadMaintenancePdf() {
        try {
            byte[] pdfData = reportService.generateMaintenancePdfReport();
            String filename = "mantenimientos_" + LocalDateTime.now().format(FILE_DATE_FORMATTER) + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

            return new ResponseEntity<>(pdfData, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}