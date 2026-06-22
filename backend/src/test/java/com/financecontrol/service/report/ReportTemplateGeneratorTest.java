package com.financecontrol.service.report;

import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Gera src/main/resources/templates/report-template.xlsx: um template PT completo
 * (abas Resumo / Por mês / Por categoria / Transações) com logo, colunas, labels e
 * formatações fixas. O {@link ExcelReportWriter} apenas injeta os dados (e traduz os
 * labels quando o idioma != pt). A paleta de estilos das células de dados fica na aba
 * oculta "_estilos".
 *
 * Não roda na suíte (bootstrap). Para regerar, remova o @Disabled e rode:
 *   mvn -o test -Dtest=ReportTemplateGeneratorTest -Dmaven-surefire-plugin.version=3.5.5 -Djunit.jupiter.conditions.deactivate=*
 */
@Disabled("Bootstrap manual do template de relatório Excel")
class ReportTemplateGeneratorTest {

    @Test
    void generate() throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            short fmt = wb.createDataFormat().getFormat("#,##0.00");

            XSSFCellStyle title    = mk(wb, 16, true,  BrandAssets.PRIMARY,        null,                (short) 0);
            XSSFCellStyle subtitle = mk(wb, 10, false, BrandAssets.PRIMARY_500,    null,                (short) 0);
            XSSFCellStyle meta     = mk(wb, 11, false, BrandAssets.TEXT_SECONDARY, null,                (short) 0);
            XSSFCellStyle section  = mk(wb, 12, true,  BrandAssets.PRIMARY,        null,                (short) 0);
            XSSFCellStyle header   = mk(wb, 11, true,  Color.WHITE,                BrandAssets.PRIMARY, (short) 0);

            buildResumo(wb, title, subtitle, meta);
            buildMensal(wb, header);
            buildCategorias(wb, section, header);
            buildTransacoes(wb, header);

            XSSFSheet est = wb.createSheet("_estilos");
            int r = 0;
            put(est, r++, "label",        mk(wb, 11, false, BrandAssets.TEXT,    null, (short) 0));
            put(est, r++, "green",        mk(wb, 11, false, BrandAssets.SUCCESS, null, (short) 0));
            put(est, r++, "red",          mk(wb, 11, false, BrandAssets.DANGER,  null, (short) 0));
            put(est, r++, "moneyGreen",   mk(wb, 11, false, BrandAssets.SUCCESS, null, fmt));
            put(est, r++, "moneyRed",     mk(wb, 11, false, BrandAssets.DANGER,  null, fmt));
            put(est, r++, "moneyPrimary", mk(wb, 11, false, BrandAssets.PRIMARY, null, fmt));
            wb.setSheetHidden(wb.getSheetIndex(est), true);

            Path out = Path.of("src/main/resources/templates/report-template.xlsx");
            try (OutputStream os = Files.newOutputStream(out)) {
                wb.write(os);
            }
            System.out.println("TEMPLATE_WRITTEN=" + out.toAbsolutePath() + " bytes=" + Files.size(out));
        }
    }

    private static void buildResumo(XSSFWorkbook wb, XSSFCellStyle title, XSSFCellStyle subtitle, XSSFCellStyle meta) throws Exception {
        XSSFSheet s = wb.createSheet("Resumo");
        s.setColumnWidth(0, 22 * 256);
        s.setColumnWidth(1, 18 * 256);
        drawLogo(wb, s);

        set(s, 0, 1, pt("title"), title);
        set(s, 1, 1, "Finance Control", subtitle);
        set(s, 3, 0, pt("period"), meta);
        set(s, 4, 0, pt("account"), meta);
        set(s, 6, 0, pt("totalIncome"), meta);
        set(s, 7, 0, pt("totalExpenses"), meta);
        set(s, 8, 0, pt("netResult"), meta);
        set(s, 9, 0, pt("balance"), meta);
    }

    private static void buildMensal(XSSFWorkbook wb, XSSFCellStyle header) {
        XSSFSheet s = wb.createSheet("Por mês");
        s.setColumnWidth(0, 14 * 256);
        s.setColumnWidth(1, 16 * 256);
        s.setColumnWidth(2, 16 * 256);
        set(s, 0, 0, pt("month"), header);
        set(s, 0, 1, pt("income"), header);
        set(s, 0, 2, pt("expenses"), header);
    }

    private static void buildCategorias(XSSFWorkbook wb, XSSFCellStyle section, XSSFCellStyle header) {
        XSSFSheet s = wb.createSheet("Por categoria");
        s.setColumnWidth(0, 22 * 256);
        s.setColumnWidth(1, 16 * 256);
        s.setColumnWidth(2, 4 * 256);
        s.setColumnWidth(3, 22 * 256);
        s.setColumnWidth(4, 16 * 256);
        set(s, 0, 0, pt("expensesByCategory"), section);
        set(s, 1, 0, pt("category"), header);
        set(s, 1, 1, pt("total"), header);
        set(s, 0, 3, pt("incomeByCategory"), section);
        set(s, 1, 3, pt("category"), header);
        set(s, 1, 4, pt("total"), header);
    }

    private static void buildTransacoes(XSSFWorkbook wb, XSSFCellStyle header) {
        XSSFSheet s = wb.createSheet("Transações");
        int[] widths = {12, 18, 16, 28, 10, 14};
        for (int i = 0; i < widths.length; i++) s.setColumnWidth(i, widths[i] * 256);
        String[] heads = {pt("date"), pt("category"), pt("location"), pt("description"), pt("type"), pt("value")};
        for (int i = 0; i < heads.length; i++) set(s, 0, i, heads[i], header);
    }

    private static void drawLogo(XSSFWorkbook wb, XSSFSheet sheet) throws Exception {
        byte[] png = BrandAssets.logoPng();
        if (png.length == 0) return;
        int target = 90;
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(png));
        int lw = img != null ? img.getWidth() : 0, lh = img != null ? img.getHeight() : 0;
        double scale = (lw > 0 && lh > 0) ? (double) target / Math.max(lw, lh) : 1;
        int w = (int) Math.round(lw * scale);
        int h = (int) Math.round(lh * scale);

        int idx = wb.addPicture(png, Workbook.PICTURE_TYPE_PNG);
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = new XSSFClientAnchor(
                0, 0, Units.pixelToEMU(w), Units.pixelToEMU(h), 0, 0, 0, 0);
        anchor.setAnchorType(ClientAnchor.AnchorType.DONT_MOVE_AND_RESIZE);
        drawing.createPicture(anchor, idx);
    }

    private static String pt(String key) {
        return ReportLabels.get("pt", key);
    }

    private static void set(XSSFSheet s, int row, int col, String text, XSSFCellStyle style) {
        XSSFRow r = s.getRow(row);
        if (r == null) r = s.createRow(row);
        XSSFCell c = r.createCell(col);
        c.setCellValue(text);
        c.setCellStyle(style);
    }

    private static void put(XSSFSheet est, int row, String name, XSSFCellStyle style) {
        XSSFCell c = est.createRow(row).createCell(0);
        c.setCellValue(name);
        c.setCellStyle(style);
    }

    private static XSSFCellStyle mk(XSSFWorkbook wb, int size, boolean bold, Color font, Color fill, short fmt) {
        XSSFFont f = wb.createFont();
        f.setFontName("Inter");
        f.setFontHeightInPoints((short) size);
        f.setBold(bold);
        f.setColor(new XSSFColor(font, null));
        XSSFCellStyle st = wb.createCellStyle();
        st.setFont(f);
        if (fill != null) {
            st.setFillForegroundColor(new XSSFColor(fill, null));
            st.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        if (fmt != 0) st.setDataFormat(fmt);
        return st;
    }
}
