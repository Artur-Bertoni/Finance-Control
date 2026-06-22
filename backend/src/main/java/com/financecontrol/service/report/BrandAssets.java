package com.financecontrol.service.report;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public final class BrandAssets {

    public static final Color PRIMARY        = new Color(0x2E7D32);
    public static final Color PRIMARY_500    = new Color(0x4CAF50);
    public static final Color PRIMARY_50     = new Color(0xE8F5E9);
    public static final Color PRIMARY_100    = new Color(0xC8E6C9);
    public static final Color TEXT           = new Color(0x111827);
    public static final Color TEXT_SECONDARY = new Color(0x4B5563);
    public static final Color SUCCESS        = new Color(0x16A34A);
    public static final Color DANGER         = new Color(0xDC2626);
    public static final Color ROW_ALT        = new Color(0xF3F6F3);
    public static final Color WHITE          = Color.WHITE;

    private static byte[] logoCache;
    private static int logoW;
    private static int logoH;

    private BrandAssets() {}

    public static synchronized byte[] logoPng() {
        if (logoCache != null) return logoCache;
        logoCache = new byte[0];
        try (InputStream in = BrandAssets.class.getResourceAsStream("/templates/logo.png")) {
            if (in == null) return logoCache;
            BufferedImage src = ImageIO.read(in);
            if (src == null) return logoCache;

            int target = 160;
            double scale = (double) target / Math.max(src.getWidth(), src.getHeight());
            logoW = Math.max(1, (int) Math.round(src.getWidth()  * scale));
            logoH = Math.max(1, (int) Math.round(src.getHeight() * scale));

            BufferedImage dst = new BufferedImage(logoW, logoH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = dst.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(src, 0, 0, logoW, logoH, null);
            g.dispose();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(dst, "png", out);
            logoCache = out.toByteArray();
        } catch (Exception e) {
            logoCache = new byte[0];
        }
        return logoCache;
    }
}
