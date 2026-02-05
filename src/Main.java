import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class Main {
    private static final String[] WEEKDAYS = {
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday",
            "codexday", "claudexday"
    };

    private static final int DAYS_PER_WEEK = 9;
    private static final int DAYS_PER_MONTH = 37;

public static void main(String[] args) {
        try {
            if (args.length > 0 && isHelpArg(args[0])) {
                printUsageAndExit(0);
            }

            String outputPath = (args.length >= 1) ? args[0] : "calendar.png";
            int startDayIndex = (args.length >= 2) ? parseStartDay(args[1]) : 0; // default Monday
            String title = (args.length >= 3) ? joinFrom(args, 2) : "Custom Calendar Month (37 days)";

            if (startDayIndex < 0 || startDayIndex >= DAYS_PER_WEEK) {
                // Defensive check; parseStartDay should already reject invalid inputs.
                System.err.println("startDay must resolve to an index between 0 and 8.");
                printUsageAndExit(2);
            }

            BufferedImage image = renderMonthCalendar(title, startDayIndex);

            File out = new File(outputPath);
            File parent = out.getAbsoluteFile().getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                // Edge case: cannot create output directory.
                System.err.println("Failed to create output directory: " + parent.getAbsolutePath());
                System.exit(3);
            }

             // Always write PNG for simplicity and portability.
            boolean ok = ImageIO.write(image, "png", out);
            if (!ok) {
                // Edge case: no PNG writer available (rare in standard JREs).
                System.err.println("No PNG writer available via ImageIO.");
                System.exit(4);
            }

            System.out.println("Wrote: " + out.getAbsolutePath());
        } catch (IllegalArgumentException ex) {
            // Input errors (invalid day name/index, etc.)
            System.err.println(ex.getMessage());
            printUsageAndExit(2);
        } catch (IOException ex) {
            // File write errors, etc.
            System.err.println("I/O error: " + ex.getMessage());
            System.exit(5);
        }
    }

    private static boolean isHelpArg(String s) {
        String v = s.trim().toLowerCase(Locale.ROOT);
        return v.equals("-h") || v.equals("--help") || v.equals("/?");
    }

    private static void printUsageAndExit(int code) {
        System.out.println("Usage:");
        System.out.println("  java Main [outputPath] [startDay] [title...]");
        System.out.println();
        System.out.println("  outputPath (optional): default 'calendar.png'");
        System.out.println("  startDay   (optional): 0..8 or weekday name (default Monday)");
        System.out.println("     0=Monday, 1=Tuesday, 2=Wednesday, 3=Thursday, 4=Friday, 5=Saturday, 6=Sunday, 7=codexday, 8=claudexday");
        System.out.println("  title      (optional): default 'Custom Calendar Month (37 days)'");
        System.exit(code);
    }

/**
     * Converts a start-day argument into an index 0..8.
     * Accepts:
     * - integer "0".."8"
     * - weekday names (case-insensitive), including "codexday" and "claudexday"
     */
    private static int parseStartDay(String startDayArg) {
        if (startDayArg == null) throw new IllegalArgumentException("startDay is missing.");

        String trimmed = startDayArg.trim();
        if (trimmed.isEmpty()) throw new IllegalArgumentException("startDay cannot be empty.");

        // First try integer form.
        try {
            int idx = Integer.parseInt(trimmed);
            if (idx < 0 || idx >= DAYS_PER_WEEK) {
                throw new IllegalArgumentException("startDay index must be between 0 and 8 (inclusive).");
            }
            return idx;
        } catch (NumberFormatException ignored) {
            // Fall through to name parsing.
        }
String key = trimmed.toLowerCase(Locale.ROOT);
        for (int i = 0; i < WEEKDAYS.length; i++) {
            if (WEEKDAYS[i].toLowerCase(Locale.ROOT).equals(key)) return i;
        }

        // A tiny convenience: accept common 3-letter abbreviations for the standard 7 days.
        // (We do not invent abbreviations for codexday/claudexday to avoid ambiguity.)
        switch (key) {
            case "mon": return 0;
            case "tue":
            case "tues": return 1;
            case "wed": return 2;
            case "thu":
            case "thur":
            case "thurs": return 3;
            case "fri": return 4;
            case "sat": return 5;
            case "sun": return 6;
            default:
                throw new IllegalArgumentException("Invalid startDay: '" + startDayArg + "'. Use 0..8 or a weekday name like 'Monday'/'codexday'.");
        }
    }

    /**
     * Renders a month-view grid:
     * - 9 columns (weekdays)
     * - enough rows to place (start offset + 37 days)
     * This is always 5 rows for 37 days in a 9-day week, but we compute it for correctness.
     */
    private static BufferedImage renderMonthCalendar(String title, int startDayIndex) {
        int totalSlotsNeeded = startDayIndex + DAYS_PER_MONTH;
        int rows = (int) Math.ceil(totalSlotsNeeded / (double) DAYS_PER_WEEK);

        // Layout constants (kept simple and readable).
        int margin = 24;
        int titleHeight = 44;
        int headerHeight = 34;
        int cellWidth = 140;
        int cellHeight = 84;

        int gridWidth = DAYS_PER_WEEK * cellWidth;
        int gridHeight = rows * cellHeight;

        int width = margin * 2 + gridWidth;
        int height = margin * 2 + titleHeight + headerHeight + gridHeight + 18; // +footer space

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            // Make lines and text look smooth.
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Background.
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);

            int gridLeft = margin;
            int titleTop = margin;
            int headerTop = titleTop + titleHeight;
            int gridTop = headerTop + headerHeight;

            // Title.
            g.setColor(new Color(20, 20, 20));
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
            drawCenteredString(g, title, new Rectangle(gridLeft, titleTop, gridWidth, titleHeight));

            // Header background.
            g.setColor(new Color(245, 247, 250));
            g.fillRect(gridLeft, headerTop, gridWidth, headerHeight);

         // Header text.
            g.setColor(new Color(50, 50, 50));
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            for (int col = 0; col < DAYS_PER_WEEK; col++) {
                int x = gridLeft + col * cellWidth;
                drawCenteredString(g, WEEKDAYS[col], new Rectangle(x, headerTop, cellWidth, headerHeight));
            }

            // Draw grid lines (including header boundaries).
            g.setColor(new Color(210, 214, 220));
            // Vertical lines from header through grid.
            for (int c = 0; c <= DAYS_PER_WEEK; c++) {
                int x = gridLeft + c * cellWidth;
                g.drawLine(x, headerTop, x, gridTop + gridHeight);
            }
            // Horizontal lines: header top/bottom and each row in the grid.
            g.drawLine(gridLeft, headerTop, gridLeft + gridWidth, headerTop);
            g.drawLine(gridLeft, gridTop, gridLeft + gridWidth, gridTop);
            for (int r = 1; r <= rows; r++) {
                int y = gridTop + r * cellHeight;
                g.drawLine(gridLeft, y, gridLeft + gridWidth, y);
            }

            // Fill cells and draw day numbers.
            // Each slot i corresponds to a cell in row-major order:
            // dayNumber = i - startDayIndex + 1
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
            FontMetrics fm = g.getFontMetrics();

            int cellCount = rows * DAYS_PER_WEEK;
            for (int i = 0; i < cellCount; i++) {
                int col = i % DAYS_PER_WEEK;
                int row = i / DAYS_PER_WEEK;

                int x = gridLeft + col * cellWidth;
                int y = gridTop + row * cellHeight;

                int dayNumber = i - startDayIndex + 1;
                boolean inMonth = (dayNumber >= 1 && dayNumber <= DAYS_PER_MONTH);

                if (!inMonth) {
                    // Out-of-month cells are lightly shaded and left blank.
                    g.setColor(new Color(252, 252, 252));
                    g.fillRect(x + 1, y + 1, cellWidth - 1, cellHeight - 1);
                    continue;
                }

                // Normal in-month cell.
                g.setColor(Color.WHITE);
                g.fillRect(x + 1, y + 1, cellWidth - 1, cellHeight - 1);

                // Day number in top-left.
                g.setColor(new Color(25, 25, 25));
                String label = String.valueOf(dayNumber);
                int tx = x + 10;
                int ty = y + 10 + fm.getAscent();
                g.drawString(label, tx, ty);
            }

            // Footer note (helps verify rules at a glance).
            g.setColor(new Color(120, 120, 120));
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            String footer = "9 days/week • 37 days/month • start=" + WEEKDAYS[startDayIndex];
            g.drawString(footer, margin, height - margin);
        } finally {
            g.dispose();
        }
        return img;
    }

     // Draws text centered within a rectangle (used for title and weekday headers).
    private static void drawCenteredString(Graphics2D g, String text, Rectangle rect) {
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        g.drawString(text, x, y);
    }

    private static String joinFrom(String[] args, int startIndex) {
        StringBuilder sb = new StringBuilder();
        for (int i = startIndex; i < args.length; i++) {
            if (i > startIndex) sb.append(' ');
            sb.append(args[i]);
        }
        return sb.toString();
    }
}