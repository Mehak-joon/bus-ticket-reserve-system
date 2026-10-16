package com.brs.client.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class IconUtils {

    public static Icon getBusIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            // Bus body
            g2.fillRoundRect(2, 3, w - 4, h - 9, 5, 5);
            // Windshield / Windows
            g2.setColor(Color.WHITE);
            g2.fillRect(4, 5, w - 8, (h - 10) / 2);
            // Divider
            g2.setColor(color);
            g2.drawLine(w / 2, 5, w / 2, 5 + (h - 10) / 2);
            // Wheels
            g2.setColor(color.darker());
            int wheelSize = Math.max(3, size / 5);
            g2.fillOval(4, h - 6, wheelSize, wheelSize);
            g2.fillOval(w - 4 - wheelSize, h - 6, wheelSize, wheelSize);
            // Headlights
            g2.setColor(new Color(254, 240, 138));
            g2.fillRect(3, h - 8, 2, 2);
            g2.fillRect(w - 5, h - 8, 2, 2);
        });
    }

    public static Icon getSearchIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int r = (int) (w * 0.35);
            int cx = (int) (w * 0.42);
            int cy = (int) (h * 0.42);
            g2.drawOval(cx - r, cy - r, r * 2, r * 2);
            int x1 = (int) (cx + r * 0.7);
            int y1 = (int) (cy + r * 0.7);
            g2.drawLine(x1, y1, w - 2, h - 2);
        });
    }

    public static Icon getTicketIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawRoundRect(2, 4, w - 4, h - 8, 4, 4);
            // Perforation dashed line
            Stroke dashed = new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{2.0f}, 0.0f);
            g2.setStroke(dashed);
            int cutX = (int) (w * 0.65);
            g2.drawLine(cutX, 4, cutX, h - 4);
            // Notches
            g2.setColor(UiUtils.COLOR_BG);
            g2.fillOval(cutX - 3, 2, 6, 4);
            g2.fillOval(cutX - 3, h - 6, 6, 4);
        });
    }

    public static Icon getUserIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            int headR = (int) (size * 0.22);
            g2.fillOval(w / 2 - headR, (int) (h * 0.15), headR * 2, headR * 2);
            // Body arc
            g2.fillArc(2, (int) (h * 0.55), w - 4, (int) (h * 0.8), 0, 180);
        });
    }

    public static Icon getClockIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawOval(2, 2, w - 4, h - 4);
            int cx = w / 2;
            int cy = h / 2;
            g2.drawLine(cx, cy, cx, cy - (int) (h * 0.3));
            g2.drawLine(cx, cy, cx + (int) (w * 0.25), cy);
        });
    }

    public static Icon getCalendarIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawRoundRect(2, 4, w - 4, h - 6, 4, 4);
            g2.drawLine(2, (int) (h * 0.4), w - 2, (int) (h * 0.4));
            // Pins
            g2.drawLine((int) (w * 0.3), 1, (int) (w * 0.3), 4);
            g2.drawLine((int) (w * 0.7), 1, (int) (w * 0.7), 4);
            // Dots
            int dotY = (int) (h * 0.65);
            g2.fillRect((int) (w * 0.3) - 1, dotY, 2, 2);
            g2.fillRect((int) (w * 0.5) - 1, dotY, 2, 2);
            g2.fillRect((int) (w * 0.7) - 1, dotY, 2, 2);
        });
    }

    public static Icon getCheckIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D.Float path = new Path2D.Float();
            path.moveTo(w * 0.2, h * 0.55);
            path.lineTo(w * 0.45, h * 0.8);
            path.lineTo(w * 0.85, h * 0.25);
            g2.draw(path);
        });
    }

    public static Icon getRefreshIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(3, 3, w - 6, h - 6, 45, 270);
            // Arrowhead
            int ax = (int) (w * 0.75);
            int ay = 3;
            Polygon arrow = new Polygon();
            arrow.addPoint(ax, ay);
            arrow.addPoint(ax + 5, ay + 6);
            arrow.addPoint(ax - 2, ay + 6);
            g2.fill(arrow);
        });
    }

    public static Icon getDownloadIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // Down Arrow
            g2.drawLine(w / 2, 2, w / 2, (int) (h * 0.68));
            g2.drawLine(w / 2, (int) (h * 0.68), (int) (w * 0.25), (int) (h * 0.45));
            g2.drawLine(w / 2, (int) (h * 0.68), (int) (w * 0.75), (int) (h * 0.45));
            // Base line
            g2.drawLine(2, h - 2, w - 2, h - 2);
        });
    }

    public static Icon getDocumentIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawRoundRect(3, 2, w - 6, h - 4, 3, 3);
            g2.drawLine(6, (int) (h * 0.35), w - 6, (int) (h * 0.35));
            g2.drawLine(6, (int) (h * 0.55), w - 6, (int) (h * 0.55));
            g2.drawLine(6, (int) (h * 0.75), (int) (w * 0.6), (int) (h * 0.75));
        });
    }

    public static Icon getSettingsIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.0f));
            int r = size / 3;
            g2.drawOval(w / 2 - r, h / 2 - r, r * 2, r * 2);
            // Outer gear spokes
            for (int i = 0; i < 8; i++) {
                double angle = i * Math.PI / 4.0;
                int x1 = (int) (w / 2 + (r + 1) * Math.cos(angle));
                int y1 = (int) (h / 2 + (r + 1) * Math.sin(angle));
                int x2 = (int) (w / 2 + (r + 4) * Math.cos(angle));
                int y2 = (int) (h / 2 + (r + 4) * Math.sin(angle));
                g2.drawLine(x1, y1, x2, y2);
            }
        });
    }

    public static Icon getTrashIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // Lid
            g2.drawLine(3, 4, w - 3, 4);
            g2.drawLine(w / 2 - 2, 2, w / 2 + 2, 2);
            // Bin
            g2.drawRoundRect(4, 5, w - 8, h - 7, 2, 2);
            g2.drawLine((int) (w * 0.4), 8, (int) (w * 0.4), h - 4);
            g2.drawLine((int) (w * 0.6), 8, (int) (w * 0.6), h - 4);
        });
    }

    public static Icon getPlusIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(w / 2, 3, w / 2, h - 3);
            g2.drawLine(3, h / 2, w - 3, h / 2);
        });
    }

    public static Icon getCloseIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(4, 4, w - 4, h - 4);
            g2.drawLine(w - 4, 4, 4, h - 4);
        });
    }

    public static Icon getRevenueIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawOval(2, 2, w - 4, h - 4);
            g2.setFont(new Font("Segoe UI", Font.BOLD, (int) (size * 0.6)));
            FontMetrics fm = g2.getFontMetrics();
            String s = "₹";
            int x = (w - fm.stringWidth(s)) / 2;
            int y = (h + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(s, x, y);
        });
    }

    public static Icon getLocationPinIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            int headR = Math.max(3, (int) (w * 0.35));
            int cx = w / 2;
            int cy = (int) (h * 0.4);
            g2.fillOval(cx - headR, cy - headR, headR * 2, headR * 2);
            Polygon poly = new Polygon();
            poly.addPoint(cx - headR, cy + 2);
            poly.addPoint(cx + headR, cy + 2);
            poly.addPoint(cx, h - 2);
            g2.fill(poly);
            g2.setColor(Color.WHITE);
            int innerR = Math.max(1, headR / 2);
            g2.fillOval(cx - innerR, cy - innerR, innerR * 2, innerR * 2);
        });
    }

    public static Icon getDestinationPinIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // Pole
            g2.drawLine(3, 2, 3, h - 2);
            // Flag
            Polygon flag = new Polygon();
            flag.addPoint(4, 3);
            flag.addPoint(w - 2, (int) (h * 0.35));
            flag.addPoint(4, (int) (h * 0.65));
            g2.fill(flag);
        });
    }

    public static Icon getSeatIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            // Seat backrest
            g2.fillRoundRect(3, 2, w - 6, (int) (h * 0.55), 4, 4);
            // Seat cushion
            g2.fillRoundRect(2, (int) (h * 0.55), w - 4, (int) (h * 0.35), 3, 3);
            // Leg / stand
            g2.fillRect(w / 2 - 1, (int) (h * 0.85), 2, h - (int) (h * 0.85));
        });
    }

    public static Icon getLoginIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // Door / frame
            g2.drawRoundRect(2, 2, (int) (w * 0.6), h - 4, 3, 3);
            // Arrow entering
            int cy = h / 2;
            g2.drawLine((int) (w * 0.4), cy, w - 2, cy);
            g2.drawLine(w - 5, cy - 3, w - 2, cy);
            g2.drawLine(w - 5, cy + 3, w - 2, cy);
        });
    }

    public static Icon getLogoutIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // Door
            g2.drawRoundRect(2, 2, (int) (w * 0.6), h - 4, 3, 3);
            // Arrow leaving
            int cy = h / 2;
            g2.drawLine((int) (w * 0.35), cy, w - 2, cy);
            g2.drawLine(w - 5, cy - 3, w - 2, cy);
            g2.drawLine(w - 5, cy + 3, w - 2, cy);
        });
    }

    public static Icon getShieldLockIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f));
            // Padlock body
            g2.fillRoundRect(3, (int) (h * 0.42), w - 6, (int) (h * 0.52), 3, 3);
            // Shackle
            g2.drawArc((int) (w * 0.28), 2, (int) (w * 0.44), (int) (h * 0.5), 0, 180);
            // Keyhole
            g2.setColor(Color.WHITE);
            g2.fillOval(w / 2 - 1, (int) (h * 0.6), 3, 3);
            g2.fillRect(w / 2, (int) (h * 0.62), 1, 4);
        });
    }

    public static Icon getSteeringWheelIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.0f));
            // Outer ring
            g2.drawOval(2, 2, w - 4, h - 4);
            int cx = w / 2;
            int cy = h / 2;
            // Center hub
            g2.fillOval(cx - 2, cy - 2, 5, 5);
            // 3 Spokes
            g2.drawLine(cx, cy, 3, cy);
            g2.drawLine(cx, cy, w - 3, cy);
            g2.drawLine(cx, cy, cx, h - 3);
        });
    }

    public static Image getAppImage(int size) {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Icon bus = getBusIcon(size, new Color(245, 158, 11)); // Amber
        bus.paintIcon(null, g2, 0, 0);
        g2.dispose();
        return img;
    }

    @FunctionalInterface
    public interface IconPainter {
        void paint(Graphics2D g2, int width, int height);
    }

    private static class VectorIcon implements Icon {
        private final int width;
        private final int height;
        private final IconPainter painter;

        public VectorIcon(int width, int height, IconPainter painter) {
            this.width = width;
            this.height = height;
            this.painter = painter;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.translate(x, y);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            painter.paint(g2, width, height);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return width;
        }

        @Override
        public int getIconHeight() {
            return height;
        }
    }
}
