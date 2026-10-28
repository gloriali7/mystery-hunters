import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A tiny 5x7 bitmap font, drawn pixel by pixel like an old game console.
 * Only capital letters exist, so all text is shown in uppercase.
 */
public final class PixelFont {
    public static final int CHAR_W = 6; // 5px glyph + 1px gap
    public static final int LINE_H = 9; // 7px glyph + 2px gap

    private static final Map<Character, String[]> GLYPHS = new HashMap<>();

    static {
        glyph('A', ".###.", "#...#", "#...#", "#####", "#...#", "#...#", "#...#");
        glyph('B', "####.", "#...#", "#...#", "####.", "#...#", "#...#", "####.");
        glyph('C', ".###.", "#...#", "#....", "#....", "#....", "#...#", ".###.");
        glyph('D', "####.", "#...#", "#...#", "#...#", "#...#", "#...#", "####.");
        glyph('E', "#####", "#....", "#....", "####.", "#....", "#....", "#####");
        glyph('F', "#####", "#....", "#....", "####.", "#....", "#....", "#....");
        glyph('G', ".###.", "#...#", "#....", "#.###", "#...#", "#...#", ".####");
        glyph('H', "#...#", "#...#", "#...#", "#####", "#...#", "#...#", "#...#");
        glyph('I', ".###.", "..#..", "..#..", "..#..", "..#..", "..#..", ".###.");
        glyph('J', "..###", "...#.", "...#.", "...#.", "...#.", "#..#.", ".##..");
        glyph('K', "#...#", "#..#.", "#.#..", "##...", "#.#..", "#..#.", "#...#");
        glyph('L', "#....", "#....", "#....", "#....", "#....", "#....", "#####");
        glyph('M', "#...#", "##.##", "#.#.#", "#.#.#", "#...#", "#...#", "#...#");
        glyph('N', "#...#", "#...#", "##..#", "#.#.#", "#..##", "#...#", "#...#");
        glyph('O', ".###.", "#...#", "#...#", "#...#", "#...#", "#...#", ".###.");
        glyph('P', "####.", "#...#", "#...#", "####.", "#....", "#....", "#....");
        glyph('Q', ".###.", "#...#", "#...#", "#...#", "#.#.#", "#..#.", ".##.#");
        glyph('R', "####.", "#...#", "#...#", "####.", "#.#..", "#..#.", "#...#");
        glyph('S', ".####", "#....", "#....", ".###.", "....#", "....#", "####.");
        glyph('T', "#####", "..#..", "..#..", "..#..", "..#..", "..#..", "..#..");
        glyph('U', "#...#", "#...#", "#...#", "#...#", "#...#", "#...#", ".###.");
        glyph('V', "#...#", "#...#", "#...#", "#...#", "#...#", ".#.#.", "..#..");
        glyph('W', "#...#", "#...#", "#...#", "#.#.#", "#.#.#", "#.#.#", ".#.#.");
        glyph('X', "#...#", "#...#", ".#.#.", "..#..", ".#.#.", "#...#", "#...#");
        glyph('Y', "#...#", "#...#", ".#.#.", "..#..", "..#..", "..#..", "..#..");
        glyph('Z', "#####", "....#", "...#.", "..#..", ".#...", "#....", "#####");
        glyph('0', ".###.", "#...#", "#..##", "#.#.#", "##..#", "#...#", ".###.");
        glyph('1', "..#..", ".##..", "..#..", "..#..", "..#..", "..#..", ".###.");
        glyph('2', ".###.", "#...#", "....#", "...#.", "..#..", ".#...", "#####");
        glyph('3', "####.", "....#", "....#", ".###.", "....#", "....#", "####.");
        glyph('4', "...#.", "..##.", ".#.#.", "#..#.", "#####", "...#.", "...#.");
        glyph('5', "#####", "#....", "####.", "....#", "....#", "#...#", ".###.");
        glyph('6', ".###.", "#....", "#....", "####.", "#...#", "#...#", ".###.");
        glyph('7', "#####", "....#", "...#.", "..#..", ".#...", ".#...", ".#...");
        glyph('8', ".###.", "#...#", "#...#", ".###.", "#...#", "#...#", ".###.");
        glyph('9', ".###.", "#...#", "#...#", ".####", "....#", "....#", ".###.");
        glyph('.', ".....", ".....", ".....", ".....", ".....", ".##..", ".##..");
        glyph(',', ".....", ".....", ".....", ".....", ".##..", "..#..", ".#...");
        glyph('!', "..#..", "..#..", "..#..", "..#..", "..#..", ".....", "..#..");
        glyph('?', ".###.", "#...#", "....#", "...#.", "..#..", ".....", "..#..");
        glyph('\'', "..#..", "..#..", ".#...", ".....", ".....", ".....", ".....");
        glyph('"', ".#.#.", ".#.#.", ".....", ".....", ".....", ".....", ".....");
        glyph('-', ".....", ".....", ".....", ".###.", ".....", ".....", ".....");
        glyph(':', ".....", ".##..", ".##..", ".....", ".##..", ".##..", ".....");
        glyph(';', ".....", ".##..", ".##..", ".....", ".##..", "..#..", ".#...");
        glyph('(', "...#.", "..#..", ".#...", ".#...", ".#...", "..#..", "...#.");
        glyph(')', ".#...", "..#..", "...#.", "...#.", "...#.", "..#..", ".#...");
        glyph('/', "....#", "....#", "...#.", "..#..", ".#...", "#....", "#....");
        glyph('>', ".#...", "..#..", "...#.", "....#", "...#.", "..#..", ".#...");
        glyph('+', ".....", "..#..", "..#..", "#####", "..#..", "..#..", ".....");
        glyph('#', ".#.#.", "#####", ".#.#.", ".#.#.", "#####", ".#.#.", ".....");
    }

    private PixelFont() {
    }

    private static void glyph(char c, String... rows) {
        GLYPHS.put(c, rows);
    }

    public static void draw(Graphics2D g, String text, int x, int y, Color color) {
        draw(g, text, x, y, color, 1);
    }

    /** Draws text with its top-left corner at (x, y). Scale 2 makes each font pixel 2x2, etc. */
    public static void draw(Graphics2D g, String text, int x, int y, Color color, int scale) {
        g.setColor(color);
        int cx = x;
        for (char ch : text.toUpperCase(Locale.ROOT).toCharArray()) {
            String[] rows = GLYPHS.get(ch);
            if (rows == null && ch != ' ') {
                rows = GLYPHS.get('?');
            }
            if (rows != null) {
                for (int r = 0; r < rows.length; r++) {
                    for (int c = 0; c < rows[r].length(); c++) {
                        if (rows[r].charAt(c) == '#') {
                            g.fillRect(cx + c * scale, y + r * scale, scale, scale);
                        }
                    }
                }
            }
            cx += CHAR_W * scale;
        }
    }

    public static int width(String text, int scale) {
        return Math.max(0, text.length() * CHAR_W * scale - scale);
    }

    /** Splits text into lines of at most maxChars, breaking between words. "\n" forces a new line. */
    public static List<String> wrap(String text, int maxChars) {
        List<String> out = new ArrayList<>();
        for (String paragraph : text.split("\n")) {
            StringBuilder line = new StringBuilder();
            for (String word : paragraph.split(" ")) {
                if (word.isEmpty()) {
                    continue;
                }
                if (line.length() > 0 && line.length() + 1 + word.length() > maxChars) {
                    out.add(line.toString());
                    line.setLength(0);
                }
                if (line.length() > 0) {
                    line.append(' ');
                }
                line.append(word);
            }
            out.add(line.toString());
        }
        return out;
    }
}
