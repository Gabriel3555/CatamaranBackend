package com.catamaran.catamaranbackend.service;

import com.catamaran.catamaranbackend.auth.infrastructure.entity.UserEntity;
import com.catamaran.catamaranbackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.catamaran.catamaranbackend.domain.*;
import com.catamaran.catamaranbackend.repository.BoatRepository;
import com.catamaran.catamaranbackend.repository.MaintananceRepository;
import com.catamaran.catamaranbackend.repository.PaymentRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final BoatRepository boatRepository;
    private final PaymentRepository paymentRepository;
    private final MaintananceRepository maintananceRepository;
    private final UserRepositoryJpa userRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Excel Report Generation
    public byte[] generateBoatsExcelReport() throws IOException {
        List<BoatEntity> boats = boatRepository.findAll();
        
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Embarcaciones");
            
            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Nombre", "Tipo", "Modelo", "Ubicación", "Precio", "Propietario", "Balance"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Fill data
            int rowNum = 1;
            for (BoatEntity boat : boats) {
                Row row = sheet.createRow(rowNum++);
                
                createCell(row, 0, boat.getId(), dataStyle);
                createCell(row, 1, boat.getName(), dataStyle);
                createCell(row, 2, boat.getType() != null ? boat.getType().name() : "", dataStyle);
                createCell(row, 3, boat.getModel(), dataStyle);
                createCell(row, 4, boat.getLocation(), dataStyle);
                createCell(row, 5, boat.getPrice(), dataStyle);
                createCell(row, 6, boat.getOwner() != null ? boat.getOwner().getFullName() : "", dataStyle);
                createCell(row, 7, boat.getBalance(), dataStyle);
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] generatePaymentsExcelReport() throws IOException {
        List<PaymentEntity> payments = paymentRepository.findAll();
        
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Pagos");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Monto", "Fecha", "Razón", "Estado", "Embarcación", "Propietario"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            int rowNum = 1;
            for (PaymentEntity payment : payments) {
                Row row = sheet.createRow(rowNum++);
                
                createCell(row, 0, payment.getId(), dataStyle);
                createCell(row, 1, payment.getMount(), dataStyle);
                createCell(row, 2, payment.getDate() != null ? payment.getDate().format(DATE_FORMATTER) : "", dataStyle);
                createCell(row, 3, payment.getReason() != null ? payment.getReason().name() : "", dataStyle);
                createCell(row, 4, payment.getStatus() != null ? payment.getStatus().name() : "", dataStyle);
                createCell(row, 5, payment.getBoat() != null ? payment.getBoat().getName() : "", dataStyle);
                createCell(row, 6, payment.getBoat() != null && payment.getBoat().getOwner() != null ? 
                    payment.getBoat().getOwner().getFullName() : "", dataStyle);
            }
            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] generateMaintenanceExcelReport() throws IOException {
        List<MaintananceEntity> maintenances = maintananceRepository.findAll();
        
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Mantenimientos");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Embarcación", "Tipo", "Estado", "Prioridad", "Fecha Programada", "Fecha Realizada", "Costo", "Descripción"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            int rowNum = 1;
            for (MaintananceEntity maintenance : maintenances) {
                Row row = sheet.createRow(rowNum++);
                
                createCell(row, 0, maintenance.getId(), dataStyle);
                createCell(row, 1, maintenance.getBoat() != null ? maintenance.getBoat().getName() : "", dataStyle);
                createCell(row, 2, maintenance.getType() != null ? maintenance.getType().name() : "", dataStyle);
                createCell(row, 3, maintenance.getStatus() != null ? maintenance.getStatus().name() : "", dataStyle);
                createCell(row, 4, maintenance.getPriority() != null ? maintenance.getPriority().name() : "", dataStyle);
                createCell(row, 5, maintenance.getDateScheduled() != null ? maintenance.getDateScheduled().format(DATE_FORMATTER) : "", dataStyle);
                createCell(row, 6, maintenance.getDatePerformed() != null ? maintenance.getDatePerformed().format(DATE_FORMATTER) : "", dataStyle);
                createCell(row, 7, maintenance.getCost(), dataStyle);
                createCell(row, 8, maintenance.getDescription(), dataStyle);
            }
            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
        }
    }

    // PDF Report Generation
    public byte[] generateBoatsPdfReport() throws IOException {
        List<BoatEntity> boats = boatRepository.findAll();
        
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // Title
            Paragraph title = new Paragraph("Reporte de Embarcaciones")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
            document.add(title);
            
            Paragraph date = new Paragraph("Fecha: " + LocalDateTime.now().format(DATE_FORMATTER))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER);
            document.add(date);
            
            document.add(new Paragraph("\n"));
            
            // Table
            float[] columnWidths = {1, 3, 2, 2, 2, 2, 3, 2};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));
            
            // Headers
            addTableHeader(table, new String[]{"ID", "Nombre", "Tipo", "Modelo", "Ubicación", "Precio", "Propietario", "Balance"});
            
            // Data
            for (BoatEntity boat : boats) {
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(String.valueOf(boat.getId()))));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(boat.getName() != null ? boat.getName() : "")));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(boat.getType() != null ? boat.getType().name() : "")));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(boat.getModel() != null ? boat.getModel() : "")));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(boat.getLocation() != null ? boat.getLocation() : "")));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(boat.getPrice() != null ? "$" + boat.getPrice() : "")));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(boat.getOwner() != null ? boat.getOwner().getFullName() : "")));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(boat.getBalance() != null ? "$" + boat.getBalance() : "")));
            }
            
            document.add(table);
            document.close();
            
            return out.toByteArray();
        }
    }

    public byte[] generatePaymentsPdfReport() throws IOException {
        List<PaymentEntity> payments = paymentRepository.findAll();
        
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            Paragraph title = new Paragraph("Reporte de Pagos")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
            document.add(title);
            
            Paragraph date = new Paragraph("Fecha: " + LocalDateTime.now().format(DATE_FORMATTER))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER);
            document.add(date);
            
            document.add(new Paragraph("\n"));
            
            float[] columnWidths = {1, 2, 3, 2, 2, 3, 3};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));
            
            addTableHeader(table, new String[]{"ID", "Monto", "Fecha", "Razón", "Estado", "Embarcación", "Propietario"});
            
            for (PaymentEntity payment : payments) {
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(String.valueOf(payment.getId()))));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(payment.getMount() != null ? "$" + payment.getMount() : "")));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(payment.getDate() != null ? payment.getDate().format(DATE_FORMATTER) : "")));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(payment.getReason() != null ? payment.getReason().name() : "")));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(payment.getStatus() != null ? payment.getStatus().name() : "")));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(payment.getBoat() != null ? payment.getBoat().getName() : "")));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(payment.getBoat() != null && payment.getBoat().getOwner() != null ?
                    payment.getBoat().getOwner().getFullName() : "")));
            }
            
            document.add(table);
            document.close();
            
            return out.toByteArray();
        }
    }

    public byte[] generateMaintenancePdfReport() throws IOException {
        List<MaintananceEntity> maintenances = maintananceRepository.findAll();
        
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            Paragraph title = new Paragraph("Reporte de Mantenimientos")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
            document.add(title);
            
            Paragraph date = new Paragraph("Fecha: " + LocalDateTime.now().format(DATE_FORMATTER))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER);
            document.add(date);
            
            document.add(new Paragraph("\n"));
            
            float[] columnWidths = {1, 2, 2, 2, 2, 2, 2, 2};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));
            
            addTableHeader(table, new String[]{"ID", "Embarcación", "Tipo", "Estado", "Prioridad", "F. Programada", "F. Realizada", "Costo"});
            
            for (MaintananceEntity maintenance : maintenances) {
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(String.valueOf(maintenance.getId()))));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(maintenance.getBoat() != null ? maintenance.getBoat().getName() : "")));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(maintenance.getType() != null ? maintenance.getType().name() : "")));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(maintenance.getStatus() != null ? maintenance.getStatus().name() : "")));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(maintenance.getPriority() != null ? maintenance.getPriority().name() : "")));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(maintenance.getDateScheduled() != null ?
                    maintenance.getDateScheduled().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "")));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(maintenance.getDatePerformed() != null ?
                    maintenance.getDatePerformed().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "")));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(maintenance.getCost() != null ? "$" + maintenance.getCost() : "")));
            }
            
            document.add(table);
            document.close();
            
            return out.toByteArray();
        }
    }

    // Helper methods for Excel
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private void createCell(Row row, int column, Object value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value != null) {
            cell.setCellValue(value.toString());
        }
        cell.setCellStyle(style);
    }

    // Helper methods for PDF
    private void addTableHeader(Table table, String[] headers) {
        for (String header : headers) {
            com.itextpdf.layout.element.Cell cell = new com.itextpdf.layout.element.Cell().add(new Paragraph(header).setBold());
            cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
            cell.setTextAlignment(TextAlignment.CENTER);
            table.addHeaderCell(cell);
        }
    }
}