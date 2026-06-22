package com.financecontrol.service.report;

import com.financecontrol.dto.response.DashboardResponse;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class PdfReportWriter {

    private static final PDFont FONT      = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDFont FONT_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    private static final DateTimeFormatter DATE     = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DecimalFormat MONEY =
        new DecimalFormat("#,##0.00", new DecimalFormatSymbols(java.util.Locale.forLanguageTag("pt-BR")));

    private static final float MARGIN = 40f;
    private static final float TOP    = 805f;
    private static final float BOTTOM = 45f;
    private static final float RIGHT  = 555f;
    private static final float CONTENT_W = RIGHT - MARGIN;

    private static final float COL_DATE = MARGIN, COL_CAT = 100, COL_LOC = 215, COL_DESC = 300, COL_TYPE = 430;

    private PdfReportWriter() {}

    public static byte[] write(ReportData data, String lang) {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Cursor c = new Cursor(doc);

            c.header(lbl(lang, "title"), new String[]{
                    lbl(lang, "period") + ": " + data.startDate().format(DATE) + " - " + data.endDate().format(DATE),
                    lbl(lang, "account") + ": " + data.accountName(),
                    lbl(lang, "generatedAt") + ": " + LocalDateTime.now().format(DATETIME)
            });

            section(c, lbl(lang, "summary"));
            summaryLine(c, lbl(lang, "totalIncome"),  data.totalIncome(),  BrandAssets.SUCCESS);
            summaryLine(c, lbl(lang, "totalExpenses"), data.totalExpenses(), BrandAssets.DANGER);
            summaryLine(c, lbl(lang, "netResult"),    data.netResult(),     data.netResult() >= 0 ? BrandAssets.SUCCESS : BrandAssets.DANGER);
            summaryLine(c, lbl(lang, "balance"),      data.balance(),       BrandAssets.PRIMARY);
            c.gap(14);

            section(c, lbl(lang, "monthlySection"));
            float[] mcols = {MARGIN, 180, 330};
            String[] mhead = {lbl(lang, "month"), lbl(lang, "income"), lbl(lang, "expenses")};
            tableHeader(c, mhead, mcols);
            boolean alt = false;
            for (DashboardResponse.MonthlyDataPoint m : data.monthly()) {
                if (c.ensure(16)) tableHeader(c, mhead, mcols);
                c.rowStart(alt = !alt);
                c.at(FONT, 9, mcols[0], m.month(), BrandAssets.TEXT);
                c.at(FONT, 9, mcols[1], MONEY.format(m.income()),   BrandAssets.SUCCESS);
                c.at(FONT, 9, mcols[2], MONEY.format(m.expenses()), BrandAssets.DANGER);
                c.advance(14);
            }
            c.gap(14);

            categorySection(c, lbl(lang, "expensesByCategory"), lbl(lang, "category"), lbl(lang, "total"), data.expensesByCategory(), BrandAssets.DANGER);
            categorySection(c, lbl(lang, "incomeByCategory"),   lbl(lang, "category"), lbl(lang, "total"), data.incomeByCategory(),   BrandAssets.SUCCESS);

            section(c, lbl(lang, "transactions"));
            txHeader(c, lang);
            alt = false;
            for (ReportData.TxRow tx : data.transactions()) {
                if (c.ensure(15)) txHeader(c, lang);
                txRow(c, tx, lang, alt = !alt);
            }

            c.close();
            doc.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to build PDF report", e);
        }
    }

    private static void summaryLine(Cursor c, String label, double value, Color valueColor) {
        c.ensure(16);
        c.at(FONT, 10, MARGIN + 12, label + ":", BrandAssets.TEXT_SECONDARY);
        c.atRight(FONT_BOLD, 10, RIGHT, "R$ " + MONEY.format(value), valueColor);
        c.advance(16);
    }

    private static void section(Cursor c, String title) {
        c.ensure(26);
        c.at(FONT_BOLD, 12, MARGIN, title, BrandAssets.PRIMARY);
        c.advance(6);
        c.hline(BrandAssets.PRIMARY_100, 0.8f);
        c.advance(10);
    }

    private static void categorySection(Cursor c, String title, String catLabel, String totalLabel,
                                        List<DashboardResponse.CategoryDataPoint> cats, Color totalColor) {
        if (cats.isEmpty()) return;
        section(c, title);
        float[] cols = {MARGIN, 330};
        String[] head = {catLabel, totalLabel};
        tableHeader(c, head, cols);
        boolean alt = false;
        for (DashboardResponse.CategoryDataPoint cat : cats) {
            if (c.ensure(16)) tableHeader(c, head, cols);
            c.rowStart(alt = !alt);
            c.at(FONT, 9, cols[0], fit(cat.categoryName(), 280, 9), BrandAssets.TEXT);
            c.at(FONT, 9, cols[1], MONEY.format(cat.total()), totalColor);
            c.advance(14);
        }
        c.gap(14);
    }

    private static void tableHeader(Cursor c, String[] cells, float[] x) {
        c.ensure(17);
        c.band(BrandAssets.PRIMARY_50, 14);
        for (int i = 0; i < cells.length; i++) c.at(FONT_BOLD, 9, x[i], cells[i], BrandAssets.PRIMARY);
        c.advance(15);
    }

    private static void txHeader(Cursor c, String lang) {
        c.ensure(17);
        c.band(BrandAssets.PRIMARY_50, 14);
        c.at(FONT_BOLD, 9, COL_DATE, lbl(lang, "date"), BrandAssets.PRIMARY);
        c.at(FONT_BOLD, 9, COL_CAT,  lbl(lang, "category"), BrandAssets.PRIMARY);
        c.at(FONT_BOLD, 9, COL_LOC,  lbl(lang, "location"), BrandAssets.PRIMARY);
        c.at(FONT_BOLD, 9, COL_DESC, lbl(lang, "description"), BrandAssets.PRIMARY);
        c.at(FONT_BOLD, 9, COL_TYPE, lbl(lang, "type"), BrandAssets.PRIMARY);
        c.atRight(FONT_BOLD, 9, RIGHT, lbl(lang, "value"), BrandAssets.PRIMARY);
        c.advance(15);
    }

    private static void txRow(Cursor c, ReportData.TxRow tx, String lang, boolean alt) {
        boolean credit = "CREDIT".equals(tx.type().name());
        double signed = credit ? tx.value() : -tx.value();
        Color valueColor = credit ? BrandAssets.SUCCESS : BrandAssets.DANGER;

        c.rowStart(alt);
        c.at(FONT, 8, COL_DATE, tx.date().format(DATE), BrandAssets.TEXT_SECONDARY);
        c.at(FONT, 8, COL_CAT,  fit(tx.category(),    COL_LOC  - COL_CAT  - 4, 8), BrandAssets.TEXT);
        c.at(FONT, 8, COL_LOC,  fit(tx.location(),    COL_DESC - COL_LOC  - 4, 8), BrandAssets.TEXT);
        c.at(FONT, 8, COL_DESC, fit(tx.description(), COL_TYPE - COL_DESC - 4, 8), BrandAssets.TEXT);
        c.at(FONT, 8, COL_TYPE, lbl(lang, tx.type().name().toLowerCase()), valueColor);
        c.atRight(FONT, 8, RIGHT, MONEY.format(signed), valueColor);
        c.advance(13);
    }

    private static String lbl(String lang, String key) {
        return ReportLabels.get(lang, key);
    }

    private static String fit(String text, float maxWidth, int size) {
        String s = sanitize(text);
        if (textWidth(s, size) <= maxWidth) return s;
        while (s.length() > 1 && textWidth(s + "...", size) > maxWidth) s = s.substring(0, s.length() - 1);
        return s + "...";
    }

    private static float textWidth(String s, int size) {
        try {
            return FONT.getStringWidth(s) / 1000f * size;
        } catch (IOException e) {
            return s.length() * size * 0.5f;
        }
    }

    private static String sanitize(String text) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder(text.length());
        for (char ch : text.toCharArray()) {
            if (ch == '\t' || ch == '\n' || ch == '\r') sb.append(' ');
            else if (ch < 32 || ch > 0xFF) sb.append('?');
            else sb.append(ch);
        }
        return sb.toString();
    }

    private static final class Cursor {
        private final PDDocument doc;
        private PDImageXObject logo;
        private PDPageContentStream cs;
        private float y;

        Cursor(PDDocument doc) throws IOException {
            this.doc = doc;
            byte[] png = BrandAssets.logoPng();
            if (png.length > 0) logo = PDImageXObject.createFromByteArray(doc, png, "logo");
            newPage();
        }

        private void newPage() throws IOException {
            if (cs != null) cs.close();
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            cs = new PDPageContentStream(doc, page);
            y = TOP;
        }

        void header(String title, String[] metaLines) {
            float logoSize = 38f;
            if (logo != null) {
                try {
                    cs.drawImage(logo, MARGIN, y - logoSize, logoSize, logoSize);
                } catch (IOException e) { throw new UncheckedIOException(e); }
            }
            float textX = logo != null ? MARGIN + logoSize + 12 : MARGIN;
            write(FONT_BOLD, 18, textX, y - 16, sanitize(title), BrandAssets.PRIMARY);
            write(FONT, 9, textX, y - 31, "Finance Control", BrandAssets.PRIMARY_500);
            y -= logoSize + 8;

            for (String meta : metaLines) { at(FONT, 9, MARGIN, meta, BrandAssets.TEXT_SECONDARY); advance(13); }
            advance(2);
            hline(BrandAssets.PRIMARY_500, 1.2f);
            advance(14);
        }

        boolean ensure(float needed) {
            if (y - needed < BOTTOM) {
                try { newPage(); } catch (IOException e) { throw new UncheckedIOException(e); }
                return true;
            }
            return false;
        }

        void rowStart(boolean alt) {
            if (alt) band(BrandAssets.ROW_ALT, 13);
        }

        void band(Color color, float height) {
            try {
                cs.setNonStrokingColor(color);
                cs.addRect(MARGIN, y - 3, CONTENT_W, height);
                cs.fill();
                cs.setNonStrokingColor(Color.BLACK);
            } catch (IOException e) { throw new UncheckedIOException(e); }
        }

        void hline(Color color, float thickness) {
            try {
                cs.setStrokingColor(color);
                cs.setLineWidth(thickness);
                cs.moveTo(MARGIN, y);
                cs.lineTo(RIGHT, y);
                cs.stroke();
                cs.setStrokingColor(Color.BLACK);
            } catch (IOException e) { throw new UncheckedIOException(e); }
        }

        void at(PDFont font, int size, float x, String s, Color color) {
            write(font, size, x, y, sanitize(s), color);
        }

        void atRight(PDFont font, int size, float right, String s, Color color) {
            String clean = sanitize(s);
            float w;
            try { w = font.getStringWidth(clean) / 1000f * size; } catch (IOException e) { w = 0; }
            write(font, size, right - w, y, clean, color);
        }

        private void write(PDFont font, int size, float x, float yy, String s, Color color) {
            try {
                cs.beginText();
                cs.setNonStrokingColor(color);
                cs.setFont(font, size);
                cs.newLineAtOffset(x, yy);
                cs.showText(s);
                cs.endText();
                cs.setNonStrokingColor(Color.BLACK);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        void advance(float dy) { y -= dy; }

        void gap(float dy) { y -= dy; }

        void close() throws IOException { if (cs != null) cs.close(); }
    }
}
