package com.school.utilityservice.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.school.utilityservice.dto.ReportRequest;
import org.apache.commons.csv.*;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class ReportService {
    public byte[] csv(ReportRequest request) throws IOException {
        StringWriter writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder().setHeader(request.getHeaders().toArray(String[]::new)).build())) {
            if (request.getRows() != null) for (List<String> row : request.getRows()) printer.printRecord(row);
        }
        return writer.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] pdf(ReportRequest request) throws DocumentException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, output);
        document.open();
        document.add(new Paragraph(request.getTitle()));
        document.add(Chunk.NEWLINE);
        PdfPTable table = new PdfPTable(request.getHeaders().size());
        for (String header : request.getHeaders()) table.addCell(header);
        if (request.getRows() != null) for (List<String> row : request.getRows()) for (String value : row) table.addCell(value == null ? "" : value);
        document.add(table);
        document.close();
        return output.toByteArray();
    }
}
