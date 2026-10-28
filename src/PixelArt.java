import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Draws the picture at the top of each scene, using simple rectangles and shapes.
 * Everything is drawn on the low-res canvas, so it comes out chunky and pixelated.
 *
 * To add a new location: write a method below and register its name in the ART table.
 * Story files can then use it with  ART: yourname
 */
public final class PixelArt {
    public static final int W = 320, H = 104;

    private static final String[] HEART = {
            ".##.##.",
            "#######",
            "#######",
            ".#####.",
            "..###..",
            "...#...",
    };

    // Character sprites. Letters pick colours: h=hair/hat, s=skin, e=eyes,
    // b=body, w=accent (shirt/collar), l=legs, k=shoes, .=see-through.
    private static final String[] BUTLER = {
            "..hhhh..",
            ".hhhhhh.",
            ".hssssh.",
            ".sesses.",
            ".ssssss.",
            "..ssss..",
            ".bbwwbb.",
            "bbbwwbbb",
            "bbbwwbbb",
            "sbbwwbbs",
            ".bbbbbb.",
            ".bbbbbb.",
            ".ll..ll.",
            ".ll..ll.",
            ".ll..ll.",
            ".kk..kk.",
    };

    private static final String[] GARDENER = {
            "..hhhh..",
            "hhhhhhhh",
            ".ssssss.",
            ".sesses.",
            ".ssssss.",
            "..ssss..",
            ".wbwwbw.",
            "wwbbbbww",
            "wwbbbbww",
            "swbbbbws",
            ".bbbbbb.",
            ".bbbbbb.",
            ".bb..bb.",
            ".bb..bb.",
            ".kk..kk.",
            "kkk..kkk",
    };

    private static final String[] NIECE = {
            "..hhhh..",
            ".hhhhhh.",
            "hhsssshh",
            "hsessesh",
            "hssssssh",
            "hhsssshh",
            "h.bwwb.h",
            ".bbbbbb.",
            "sbbbbbbs",
            ".bbbbbb.",
            ".bbbbbb.",
            "bbbbbbbb",
            "bbbbbbbb",
            "..s..s..",
            "..s..s..",
            ".kk..kk.",
    };

    private static final String[] GUARD = {
            "..hhhh..",
            ".hhhhhh.",
            "hhhhhhhh",
            ".ssssss.",
            ".sesses.",
            "..ssss..",
            ".bbwbbb.",
            "bbbbbbbb",
            "bbbbbbbb",
            "sbbbbbbs",
            ".kkkkkk.",
            ".llllll.",
            ".ll..ll.",
            ".ll..ll.",
            ".ll..ll.",
            ".kk..kk.",
    };

    private static final String[] CURATOR = {
            "...hh...",
            "..hhhh..",
            ".hhhhhh.",
            ".hssssh.",
            ".wewwew.",
            ".ssssss.",
            "..ssss..",
            ".bbwwbb.",
            "bbbbbbbb",
            "bbbbbbbb",
            "sbbbbbbs",
            ".bbbbbb.",
            ".bbbbbb.",
            "..s..s..",
            "..s..s..",
            ".kk..kk.",
    };

    private static final String[] INTERN = {
            "..hhhh..",
            ".hhhhhh.",
            "hhhhhhhh",
            ".hssssh.",
            ".sesses.",
            ".ssssss.",
            "..ssss..",
            ".bbbbbb.",
            "bbbwwbbb",
            "bbbwwbbb",
            "sbbbbbbs",
            ".bbbbbb.",
            ".llllll.",
            ".ll..ll.",
            ".ll..ll.",
            ".kk..kk.",
    };

    private static final String[] STAR = {
            "...#...",
            "..###..",
            "#######",
            ".#####.",
            "..###..",
            ".##.##.",
            ".#...#.",
    };

    /** Something that can paint a scene picture. t is the frame counter, for animation. */
    private interface Painter {
        void paint(Graphics2D g, int t);
    }

    /** Every picture a story file can use, by name. */
    private static final Map<String, Painter> ART = new LinkedHashMap<>();

    static {
        ART.put("manor", PixelArt::manor);
        ART.put("foyer", PixelArt::foyer);
        ART.put("study", PixelArt::study);
        ART.put("kitchen", PixelArt::kitchen);
        ART.put("garden", PixelArt::garden);
        ART.put("guestroom", PixelArt::guestRoom);
        ART.put("lineup", PixelArt::lineup);
        ART.put("emerald", PixelArt::emerald);
        ART.put("museum", PixelArt::museum);
        ART.put("gallery", PixelArt::gallery);
        ART.put("security", PixelArt::security);
        ART.put("office", PixelArt::office);
        ART.put("storage", PixelArt::storage);
        ART.put("museumlineup", PixelArt::museumLineup);
        ART.put("mask", PixelArt::mask);
    }

    private PixelArt() {
    }

    public static boolean exists(String name) {
        return ART.containsKey(name);
    }

    public static Set<String> names() {
        return ART.keySet();
    }

    /** Draws the named scene art with its top-left corner at (x, y). t is the frame counter, for animation. */
    public static void draw(Graphics2D g0, String name, int t, int x, int y) {
        Graphics2D g = (Graphics2D) g0.create();
        g.translate(x, y);
        g.clipRect(0, 0, W, H);
        Painter painter = ART.get(name);
        if (painter != null) {
            painter.paint(g, t);
        } else {
            g.setColor(Palette.BLACK);
            g.fillRect(0, 0, W, H);
            PixelFont.draw(g, "NO ART CALLED: " + name, 8, 8, Palette.RED);
        }
        g.dispose();
    }

    public static void heart(Graphics2D g, int x, int y, boolean full) {
        sprite(g, HEART, x, y, 1, "#", full ? Palette.RED : Palette.DARK_GRAY);
    }

    public static void star(Graphics2D g, int x, int y) {
        sprite(g, STAR, x, y, 1, "#", Palette.YELLOW);
    }

    /** Draws a sprite from text rows. Each letter in codes maps to the colour at the same position. */
    public static void sprite(Graphics2D g, String[] rows, int x, int y, int size, String codes, Color... colors) {
        for (int r = 0; r < rows.length; r++) {
            for (int c = 0; c < rows[r].length(); c++) {
                int index = codes.indexOf(rows[r].charAt(c));
                if (index >= 0) {
                    g.setColor(colors[index]);
                    g.fillRect(x + c * size, y + r * size, size, size);
                }
            }
        }
    }

    // ---------------------------------------------------------------- characters

    private static void person(Graphics2D g, String[] rows, int x, int feetY, int t, int phase, boolean asleep,
            Color hair, Color body, Color accent, Color legs, Color shoes) {
        int size = 3;
        boolean eyesShut = asleep || (t + phase) % 180 < 6;
        g.setColor(Palette.BLACK);
        g.fillOval(x - 2, feetY - 3, 28, 5);
        sprite(g, rows, x, feetY - rows.length * size, size, "hsebwlk",
                hair, Palette.PEACH, eyesShut ? Palette.PEACH : Palette.BLACK, body, accent, legs, shoes);
    }

    private static void butler(Graphics2D g, int x, int feetY, int t) {
        person(g, BUTLER, x, feetY, t, 0, false, Palette.LIGHT_GRAY, Palette.BLACK, Palette.WHITE, Palette.BLACK, Palette.DARK_GRAY);
    }

    private static void gardener(Graphics2D g, int x, int feetY, int t) {
        person(g, GARDENER, x, feetY, t, 70, false, Palette.BROWN, Palette.BLUE, Palette.ORANGE, Palette.BLUE, Palette.BROWN);
    }

    private static void niece(Graphics2D g, int x, int feetY, int t) {
        person(g, NIECE, x, feetY, t, 130, false, Palette.YELLOW, Palette.PINK, Palette.WHITE, Palette.PINK, Palette.RED);
    }

    private static void guard(Graphics2D g, int x, int feetY, int t, boolean asleep) {
        person(g, GUARD, x, feetY, t, 40, asleep, Palette.DARK_BLUE, Palette.BLUE, Palette.YELLOW, Palette.DARK_BLUE, Palette.BLACK);
    }

    private static void curator(Graphics2D g, int x, int feetY, int t) {
        person(g, CURATOR, x, feetY, t, 100, false, Palette.LIGHT_GRAY, Palette.PURPLE, Palette.WHITE, Palette.PEACH, Palette.BLACK);
    }

    private static void intern(Graphics2D g, int x, int feetY, int t) {
        person(g, INTERN, x, feetY, t, 150, false, Palette.BROWN, Palette.ORANGE, Palette.WHITE, Palette.BLUE, Palette.RED);
    }

    // ---------------------------------------------------------------- shared bits

    private static void rain(Graphics2D g, int t) {
        g.setColor(Palette.BLUE);
        for (int i = 0; i < 60; i++) {
            int x = (i * 53 + t) % W;
            int y = (i * 37 + t * 4) % (H + 8) - 4;
            g.fillRect(x, y, 1, 3);
        }
    }

    private static void lightningBolt(Graphics2D g, int x) {
        g.setColor(Palette.WHITE);
        int[] xs = {x, x + 6, x + 2, x + 10, x + 4};
        int[] ys = {0, 14, 14, 32, 46};
        g.drawPolyline(xs, ys, xs.length);
        g.translate(1, 0);
        g.drawPolyline(xs, ys, xs.length);
        g.translate(-1, 0);
    }

    private static void window(Graphics2D g, int x, int y, boolean lit) {
        g.setColor(Palette.BLACK);
        g.fillRect(x - 1, y - 1, 10, 12);
        g.setColor(lit ? Palette.YELLOW : Palette.DARK_BLUE);
        g.fillRect(x, y, 8, 10);
        g.setColor(Palette.BLACK);
        g.fillRect(x + 4, y, 1, 10);
        g.fillRect(x, y + 5, 8, 1);
    }

    private static void door(Graphics2D g, int x, int y) {
        g.setColor(Palette.BLACK);
        g.fillRect(x - 2, y - 2, 28, 50);
        g.setColor(Palette.BROWN);
        g.fillRect(x, y, 24, 48);
        g.setColor(Palette.BLACK);
        g.drawRect(x + 3, y + 4, 7, 16);
        g.drawRect(x + 14, y + 4, 7, 16);
        g.drawRect(x + 3, y + 26, 7, 16);
        g.drawRect(x + 14, y + 26, 7, 16);
        g.setColor(Palette.YELLOW);
        g.fillRect(x + 19, y + 23, 2, 2);
    }

    private static void sparkle(Graphics2D g, int x, int y, int r) {
        g.setColor(Palette.YELLOW);
        g.fillRect(x - r, y, r * 2 + 1, 1);
        g.fillRect(x, y - r, 1, r * 2 + 1);
        g.setColor(Palette.WHITE);
        g.fillRect(x, y, 1, 1);
    }

    private static void stars(Graphics2D g, int t) {
        g.setColor(Palette.LIGHT_GRAY);
        for (int i = 0; i < 30; i++) {
            if ((i + t / 25) % 9 != 0) {
                g.fillRect((i * 73) % W, (i * 29) % 60, 1, 1);
            }
        }
    }

    /** Slowly spinning light rays behind a treasure. */
    private static void rays(Graphics2D g, int t, int cx, int cy) {
        g.setColor(Palette.PURPLE);
        for (int i = 0; i < 12; i++) {
            double a = i * Math.PI / 6 + t * 0.01;
            double b = a + 0.14;
            g.fillPolygon(
                    new int[] {cx, cx + (int) (Math.cos(a) * 220), cx + (int) (Math.cos(b) * 220)},
                    new int[] {cy, cy + (int) (Math.sin(a) * 220), cy + (int) (Math.sin(b) * 220)}, 3);
        }
    }

    private static void twinkles(Graphics2D g, int t) {
        int[][] spots = {{120, 24}, {200, 30}, {128, 80}, {196, 76}, {160, 12}};
        for (int i = 0; i < spots.length; i++) {
            int phase = (t / 10 + i) % 4;
            if (phase != 0) {
                sparkle(g, spots[i][0], spots[i][1], phase == 2 ? 3 : 1);
            }
        }
    }

    private static void frame(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(Palette.YELLOW);
        g.fillRect(x, y, w, h);
        g.setColor(Palette.ORANGE);
        g.drawRect(x + 1, y + 1, w - 3, h - 3);
    }

    private static void crate(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(Palette.BROWN);
        g.fillRect(x, y, w, h);
        g.setColor(Palette.ORANGE);
        g.drawRect(x + 1, y + 1, w - 3, h - 3);
        g.drawLine(x + 2, y + 2, x + w - 3, y + h - 3);
        g.drawLine(x + w - 3, y + 2, x + 2, y + h - 3);
        g.setColor(Palette.BLACK);
        g.drawRect(x, y, w - 1, h - 1);
    }

    // ---------------------------------------------------------------- Case 1: Ravenwood Manor

    private static void manor(Graphics2D g, int t) {
        int cycle = t % 360;
        boolean flash = cycle < 10 && cycle % 4 < 2;
        Color sky = flash ? Palette.INDIGO : Palette.DARK_BLUE;
        g.setColor(sky);
        g.fillRect(0, 0, W, H);
        stars(g, t);
        // Crescent moon: a full circle with a sky-coloured circle over it.
        g.setColor(Palette.PEACH);
        g.fillOval(262, 10, 16, 16);
        g.setColor(sky);
        g.fillOval(267, 7, 16, 16);
        if (flash) {
            lightningBolt(g, 50);
        }

        // Tower
        g.setColor(Palette.DARK_GRAY);
        g.fillRect(88, 26, 24, 64);
        g.setColor(Palette.PURPLE);
        g.fillPolygon(new int[] {84, 100, 116}, new int[] {26, 4, 26}, 3);
        window(g, 96, 36, (t / 50) % 4 != 0);

        // Main house
        g.setColor(Palette.DARK_GRAY);
        g.fillRect(196, 20, 8, 16);
        g.fillRect(112, 44, 108, 46);
        g.setColor(Palette.PURPLE);
        g.fillPolygon(new int[] {106, 166, 226}, new int[] {44, 18, 44}, 3);
        int[] windowX = {122, 140, 184, 202};
        for (int row = 0; row < 2; row++) {
            for (int i = 0; i < windowX.length; i++) {
                boolean lit = (i + row) % 2 == 0 || (row == 1 && i == 3 && (t / 40) % 3 == 0);
                window(g, windowX[i], 52 + row * 18, lit);
            }
        }
        g.setColor(Palette.BROWN);
        g.fillRect(158, 70, 16, 20);
        g.fillOval(158, 64, 16, 12);
        g.setColor(Palette.YELLOW);
        g.fillRect(170, 80, 2, 2);

        // Lawn and path
        g.setColor(Palette.DARK_GREEN);
        g.fillRect(0, 90, W, H - 90);
        g.setColor(Palette.DARK_GRAY);
        g.fillPolygon(new int[] {158, 174, 190, 142}, new int[] {90, 90, H, H}, 4);

        rain(g, t);
    }

    private static void foyer(Graphics2D g, int t) {
        // Striped wallpaper and wood panelling
        g.setColor(Palette.PURPLE);
        g.fillRect(0, 0, W, 70);
        g.setColor(Palette.DARK_BLUE);
        for (int x = 6; x < W; x += 14) {
            g.fillRect(x, 0, 2, 58);
        }
        g.setColor(Palette.BROWN);
        g.fillRect(0, 58, W, 12);
        g.setColor(Palette.ORANGE);
        g.fillRect(0, 58, W, 1);

        // Checkered floor
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 16; col++) {
                g.setColor((row + col) % 2 == 0 ? Palette.WHITE : Palette.DARK_GRAY);
                g.fillRect(col * 20, 70 + row * 7, 20, 7);
            }
        }

        // Staircase with a red carpet
        for (int i = 0; i < 7; i++) {
            int width = 90 - i * 8;
            int sx = 160 - width / 2;
            int sy = 64 - i * 6;
            g.setColor(Palette.BROWN);
            g.fillRect(sx, sy, width, 6);
            g.setColor(Palette.ORANGE);
            g.fillRect(sx, sy, width, 1);
            g.setColor(Palette.RED);
            g.fillRect(152, sy + 1, 16, 5);
        }
        g.setColor(Palette.ORANGE);
        g.drawLine(115, 54, 139, 20);
        g.drawLine(205, 54, 181, 20);
        g.fillRect(113, 52, 3, 12);
        g.fillRect(204, 52, 3, 12);

        door(g, 22, 22);
        door(g, 274, 22);

        // Portrait and a painting of the emerald
        g.setColor(Palette.ORANGE);
        g.fillRect(62, 14, 28, 22);
        g.fillRect(230, 14, 28, 22);
        g.setColor(Palette.DARK_GREEN);
        g.fillRect(64, 16, 24, 18);
        g.setColor(Palette.PEACH);
        g.fillOval(71, 19, 10, 12);
        g.setColor(Palette.BROWN);
        g.fillRect(71, 18, 10, 3);
        g.setColor(Palette.DARK_BLUE);
        g.fillRect(232, 16, 24, 18);
        g.setColor(Palette.GREEN);
        g.fillPolygon(new int[] {244, 250, 244, 238}, new int[] {18, 25, 32, 25}, 4);

        // Chandelier with flickering candles
        g.setColor(Palette.DARK_GRAY);
        g.fillRect(159, 0, 2, 8);
        g.setColor(Palette.YELLOW);
        g.fillRect(148, 8, 24, 3);
        for (int i = 0; i < 4; i++) {
            int cx = 149 + i * 7;
            g.setColor(Palette.WHITE);
            g.fillRect(cx, 5, 1, 3);
            g.setColor((t / 6 + i) % 3 == 0 ? Palette.ORANGE : Palette.YELLOW);
            g.fillRect(cx, 3, 1, 2);
        }
    }

    private static void study(Graphics2D g, int t) {
        g.setColor(Palette.DARK_GREEN);
        g.fillRect(0, 0, W, 80);
        g.setColor(Palette.BROWN);
        g.fillRect(0, 80, W, H - 80);
        g.setColor(Palette.BLACK);
        g.fillRect(0, 80, W, 1);
        for (int x = 0; x < W; x += 24) {
            g.fillRect(x, 80, 1, H - 80);
        }
        g.setColor(Palette.RED);
        g.fillRect(110, 88, 100, 12);
        g.setColor(Palette.ORANGE);
        g.drawRect(112, 90, 95, 7);

        // Smashed window with the storm outside
        g.setColor(Palette.BROWN);
        g.fillRect(126, 8, 68, 56);
        Graphics2D outside = (Graphics2D) g.create(130, 12, 60, 48);
        outside.setColor(Palette.DARK_BLUE);
        outside.fillRect(0, 0, 60, 48);
        rain(outside, t);
        outside.dispose();
        g.setColor(Palette.BROWN);
        g.fillRect(159, 12, 2, 48);
        g.fillRect(130, 35, 60, 2);
        g.setColor(Palette.BLACK);
        g.fillPolygon(new int[] {168, 190, 190, 180, 174}, new int[] {12, 12, 30, 24, 28}, 5);
        g.setColor(Palette.WHITE);
        g.drawLine(174, 28, 170, 33);
        g.drawLine(180, 24, 186, 33);
        g.drawLine(168, 12, 164, 20);

        // Bookshelf
        g.setColor(Palette.BROWN);
        g.fillRect(10, 8, 72, 72);
        g.setColor(Palette.BLACK);
        g.fillRect(14, 12, 64, 64);
        Color[] books = {Palette.RED, Palette.BLUE, Palette.ORANGE, Palette.GREEN, Palette.PINK, Palette.INDIGO, Palette.YELLOW};
        for (int shelf = 0; shelf < 4; shelf++) {
            int bottom = 26 + shelf * 16;
            int bx = 15;
            for (int i = 0; ; i++) {
                int bw = 3 + (i * 7 + shelf) % 3;
                int bh = 9 + (i * 5 + shelf * 3) % 5;
                if (bx + bw > 77) {
                    break;
                }
                g.setColor(books[(i + shelf * 2) % books.length]);
                g.fillRect(bx, bottom - bh, bw, bh);
                bx += bw + 1;
            }
            g.setColor(Palette.BROWN);
            g.fillRect(14, bottom, 64, 2);
        }

        // The open, empty safe
        g.setColor(Palette.DARK_GRAY);
        g.fillRect(232, 40, 44, 40);
        g.setColor(Palette.BLACK);
        g.fillRect(237, 45, 34, 30);
        g.setColor(Palette.PURPLE);
        g.fillRect(246, 66, 16, 5);
        g.setColor(Palette.LIGHT_GRAY);
        g.fillPolygon(new int[] {276, 290, 290, 276}, new int[] {40, 34, 86, 80}, 4);
        g.setColor(Palette.YELLOW);
        g.fillOval(280, 54, 6, 6);
        // A glint of lace on the hinge
        if ((t / 20) % 3 != 0) {
            g.setColor(Palette.WHITE);
            g.fillRect(275, 48, 2, 1);
            g.fillRect(276, 49, 2, 1);
            g.fillRect(275, 50, 1, 1);
        }
    }

    private static void kitchen(Graphics2D g, int t) {
        // Tiled wall
        g.setColor(Palette.PEACH);
        g.fillRect(0, 0, W, 90);
        g.setColor(Palette.LIGHT_GRAY);
        for (int y = 0; y < 90; y += 8) {
            g.fillRect(0, y, W, 1);
            for (int x = (y / 8 % 2) * 4; x < W; x += 8) {
                g.fillRect(x, y, 1, 8);
            }
        }
        g.setColor(Palette.DARK_GRAY);
        g.fillRect(0, 90, W, H - 90);

        // Hanging pots
        g.setColor(Palette.DARK_GRAY);
        g.fillRect(20, 8, 90, 2);
        for (int i = 0; i < 3; i++) {
            int px = 28 + i * 28;
            g.setColor(Palette.DARK_GRAY);
            g.fillRect(px + 5, 10, 1, 5);
            g.setColor(Palette.ORANGE);
            g.fillOval(px, 15, 12, 10);
            g.setColor(Palette.BROWN);
            g.fillRect(px - 3, 18, 3, 2);
        }

        // Counter with the silver being polished
        g.setColor(Palette.BROWN);
        g.fillRect(0, 62, 120, 28);
        g.setColor(Palette.LIGHT_GRAY);
        g.fillRect(0, 60, 122, 3);
        g.setColor(Palette.BLACK);
        for (int x = 8; x < 110; x += 36) {
            g.drawRect(x, 66, 30, 20);
        }
        g.setColor(Palette.YELLOW);
        for (int x = 34; x < 120; x += 36) {
            g.fillRect(x, 75, 2, 2);
        }
        g.setColor(Palette.WHITE);
        g.fillOval(40, 56, 26, 5);
        g.fillOval(80, 50, 12, 10);
        g.fillRect(91, 53, 4, 2);
        if ((t / 15) % 4 == 0) {
            sparkle(g, 84, 52, 2);
        }

        // Stove with a steaming pot
        g.setColor(Palette.BLACK);
        g.fillRect(222, 54, 76, 36);
        g.setColor(Palette.DARK_GRAY);
        g.fillRect(226, 66, 68, 20);
        g.setColor(Palette.ORANGE);
        g.fillRect(240, 70, 40, 10);
        g.setColor(Palette.RED);
        g.fillRect(232, 52, 16, 2);
        g.fillRect(270, 52, 16, 2);
        g.setColor(Palette.LIGHT_GRAY);
        g.fillRect(268, 40, 20, 12);
        g.setColor(Palette.WHITE);
        for (int i = 0; i < 3; i++) {
            int sy = 36 - ((t / 4 + i * 6) % 18);
            int sx = 272 + i * 5 + ((t / 8 + i) % 2);
            g.fillRect(sx, sy, 2, 2);
        }

        butler(g, 150, 94, t);
    }

    private static void garden(Graphics2D g, int t) {
        boolean flash = t % 420 < 8 && t % 4 < 2;
        g.setColor(flash ? Palette.INDIGO : Palette.DARK_BLUE);
        g.fillRect(0, 0, W, H);
        if (flash) {
            lightningBolt(g, 40);
        }

        // Greenhouse
        g.setColor(Palette.INDIGO);
        g.fillRect(200, 30, 110, 54);
        g.fillPolygon(new int[] {200, 255, 310}, new int[] {30, 10, 30}, 3);
        g.setColor(Palette.DARK_GREEN);
        for (int i = 0; i < 6; i++) {
            g.fillOval(204 + i * 17, 64, 12, 18);
        }
        g.setColor(Palette.RED);
        for (int i = 0; i < 6; i++) {
            g.fillRect(208 + i * 17, 70 + (i % 2) * 4, 2, 2);
        }
        g.setColor(Palette.LIGHT_GRAY);
        g.drawRect(200, 30, 110, 54);
        g.drawLine(200, 30, 255, 10);
        g.drawLine(255, 10, 310, 30);
        g.drawLine(200, 56, 310, 56);
        for (int x = 222; x < 310; x += 22) {
            g.drawLine(x, 30, x, 84);
        }

        // Hedges with flowers
        g.setColor(Palette.DARK_GREEN);
        for (int x = -10; x < 200; x += 30) {
            g.fillOval(x, 50, 40, 36);
        }
        Color[] flowers = {Palette.RED, Palette.PINK, Palette.YELLOW};
        for (int i = 0; i < 14; i++) {
            g.setColor(flowers[i % 3]);
            g.fillRect((i * 37) % 190 + 4, 58 + (i * 13) % 20, 2, 2);
        }

        // Mud, puddles and a shovel
        g.setColor(Palette.BROWN);
        g.fillRect(0, 80, W, H - 80);
        g.setColor(Palette.BLUE);
        g.fillOval(30, 94, 30, 4);
        g.fillOval(230, 90, 40, 5);
        if ((t / 10) % 2 == 0) {
            g.setColor(Palette.LIGHT_GRAY);
            g.drawOval(40, 94, 8, 2);
            g.drawOval(246, 90, 8, 2);
        }
        g.setColor(Palette.ORANGE);
        g.fillRect(98, 56, 2, 26);
        g.setColor(Palette.LIGHT_GRAY);
        g.fillRect(95, 80, 8, 6);

        gardener(g, 130, 96, t);
        rain(g, t);
    }

    private static void guestRoom(Graphics2D g, int t) {
        g.setColor(Palette.INDIGO);
        g.fillRect(0, 0, W, 80);
        g.setColor(Palette.PURPLE);
        for (int y = 6; y < 76; y += 12) {
            for (int x = (y / 12 % 2) * 8 + 4; x < W; x += 16) {
                g.fillRect(x, y, 2, 2);
            }
        }
        g.setColor(Palette.BROWN);
        g.fillRect(0, 80, W, H - 80);
        g.setColor(Palette.BLACK);
        for (int y = 80; y < H; y += 6) {
            g.fillRect(0, y, W, 1);
        }

        // Bed
        g.setColor(Palette.BROWN);
        g.fillRect(16, 40, 8, 50);
        g.setColor(Palette.WHITE);
        g.fillRect(24, 58, 90, 12);
        g.setColor(Palette.LIGHT_GRAY);
        g.fillRect(26, 52, 20, 8);
        g.setColor(Palette.PINK);
        g.fillRect(50, 56, 64, 16);
        g.setColor(Palette.BROWN);
        g.fillRect(110, 60, 6, 30);
        g.fillRect(24, 70, 4, 20);

        // Nightstand with a candle
        g.setColor(Palette.BROWN);
        g.fillRect(124, 66, 18, 24);
        g.setColor(Palette.BLACK);
        g.drawRect(127, 72, 12, 6);
        g.setColor(Palette.WHITE);
        g.fillRect(131, 58, 4, 8);
        g.setColor((t / 5) % 2 == 0 ? Palette.YELLOW : Palette.ORANGE);
        g.fillRect(132, 55, 2, 3);

        // Packed suitcase
        g.setColor(Palette.ORANGE);
        g.fillRect(196, 72, 40, 20);
        g.setColor(Palette.BROWN);
        g.fillRect(206, 72, 3, 20);
        g.fillRect(224, 72, 3, 20);
        g.setColor(Palette.BLACK);
        g.drawRect(211, 68, 10, 4);

        // Red coat on a hook, with a glove peeking out of the pocket
        g.setColor(Palette.DARK_GRAY);
        g.fillRect(266, 14, 4, 3);
        g.setColor(Palette.RED);
        g.fillPolygon(new int[] {262, 274, 284, 252}, new int[] {18, 18, 74, 74}, 4);
        g.setColor(Palette.PURPLE);
        g.fillRect(267, 18, 2, 30);
        g.fillRect(270, 52, 8, 2);
        g.setColor(Palette.WHITE);
        g.fillRect(272, 49, 3, 3);
        g.fillRect(273, 48, 1, 1);

        niece(g, 160, 94, t);
    }

    private static void lineup(Graphics2D g, int t) {
        foyer(g, t);
        butler(g, 84, 100, t);
        gardener(g, 148, 100, t);
        niece(g, 212, 100, t);
    }

    private static void emerald(Graphics2D g, int t) {
        g.setColor(Palette.DARK_BLUE);
        g.fillRect(0, 0, W, H);
        int cx = 160, cy = 52;
        rays(g, t, cx, cy);

        g.setColor(Palette.DARK_GREEN);
        g.fillPolygon(
                new int[] {cx - 8, cx + 8, cx + 14, cx + 14, cx + 8, cx - 8, cx - 14, cx - 14},
                new int[] {cy - 18, cy - 18, cy - 10, cy + 10, cy + 18, cy + 18, cy + 10, cy - 10}, 8);
        g.setColor(Palette.GREEN);
        g.fillPolygon(
                new int[] {cx - 5, cx + 5, cx + 8, cx + 8, cx + 5, cx - 5, cx - 8, cx - 8},
                new int[] {cy - 12, cy - 12, cy - 6, cy + 6, cy + 12, cy + 12, cy + 6, cy - 6}, 8);
        g.setColor(Palette.WHITE);
        g.fillRect(cx - 4, cy - 10, 3, 2);
        g.fillRect(cx - 6, cy - 7, 1, 4);

        twinkles(g, t);
    }

    // ---------------------------------------------------------------- Case 2: City Museum

    private static void museum(Graphics2D g, int t) {
        g.setColor(Palette.DARK_BLUE);
        g.fillRect(0, 0, W, H);
        stars(g, t);
        g.setColor(Palette.PEACH);
        g.fillOval(30, 10, 14, 14);

        // Roof and name
        g.setColor(Palette.LIGHT_GRAY);
        g.fillPolygon(new int[] {86, 160, 234}, new int[] {36, 12, 36}, 3);
        g.setColor(Palette.INDIGO);
        g.fillPolygon(new int[] {104, 160, 216}, new int[] {33, 17, 33}, 3);
        g.setColor(Palette.LIGHT_GRAY);
        g.fillRect(90, 36, 140, 11);
        PixelFont.draw(g, "MUSEUM", 160 - PixelFont.width("MUSEUM", 1) / 2, 38, Palette.DARK_GRAY);

        // Wall, lit doorway and columns
        g.setColor(Palette.DARK_GRAY);
        g.fillRect(94, 47, 132, 37);
        g.setColor(Palette.YELLOW);
        g.fillRect(152, 60, 16, 24);
        int[] columns = {98, 120, 142, 170, 192, 214};
        for (int x : columns) {
            g.setColor(Palette.WHITE);
            g.fillRect(x, 47, 8, 37);
            g.setColor(Palette.LIGHT_GRAY);
            g.fillRect(x + 5, 47, 2, 37);
            g.fillRect(x - 1, 47, 10, 2);
            g.fillRect(x - 1, 82, 10, 2);
        }

        // Steps and street
        for (int i = 0; i < 3; i++) {
            g.setColor(i % 2 == 0 ? Palette.LIGHT_GRAY : Palette.WHITE);
            g.fillRect(86 - i * 6, 84 + i * 4, 148 + i * 12, 4);
        }
        g.setColor(Palette.BLACK);
        g.fillRect(0, 96, W, H - 96);

        policeCar(g, 246, 96, t);
    }

    private static void policeCar(Graphics2D g, int x, int groundY, int t) {
        boolean redOn = (t / 8) % 2 == 0;
        g.setColor(Palette.WHITE);
        g.fillRect(x, groundY - 12, 52, 8);
        g.fillRect(x + 10, groundY - 18, 30, 6);
        g.setColor(Palette.BLUE);
        g.fillRect(x + 13, groundY - 17, 11, 4);
        g.fillRect(x + 27, groundY - 17, 11, 4);
        g.setColor(Palette.DARK_BLUE);
        g.fillRect(x, groundY - 8, 52, 2);

        // Flashing lights with a little glow
        g.setColor(redOn ? Palette.RED : Palette.DARK_GRAY);
        g.fillRect(x + 18, groundY - 21, 6, 3);
        g.setColor(redOn ? Palette.DARK_GRAY : Palette.BLUE);
        g.fillRect(x + 26, groundY - 21, 6, 3);
        g.setColor(redOn ? Palette.RED : Palette.BLUE);
        int glowX = redOn ? x + 21 : x + 29;
        g.drawLine(glowX - 6, groundY - 26, glowX - 3, groundY - 23);
        g.drawLine(glowX, groundY - 28, glowX, groundY - 24);
        g.drawLine(glowX + 6, groundY - 26, glowX + 3, groundY - 23);

        g.setColor(Palette.BLACK);
        g.fillOval(x + 6, groundY - 6, 9, 9);
        g.fillOval(x + 37, groundY - 6, 9, 9);
        g.setColor(Palette.LIGHT_GRAY);
        g.fillRect(x + 9, groundY - 3, 3, 3);
        g.fillRect(x + 40, groundY - 3, 3, 3);
    }

    private static void gallery(Graphics2D g, int t) {
        g.setColor(Palette.DARK_BLUE);
        g.fillRect(0, 0, W, 80);
        g.setColor(Palette.INDIGO);
        g.fillRect(0, 0, W, 4);
        g.fillRect(0, 74, W, 6);
        // Spotlight on the empty case
        g.fillPolygon(new int[] {152, 168, 196, 124}, new int[] {0, 0, 80, 80}, 4);

        // Polished floor
        g.setColor(Palette.DARK_GRAY);
        g.fillRect(0, 80, W, H - 80);
        g.setColor(Palette.INDIGO);
        for (int y = 84; y < H; y += 6) {
            g.fillRect(0, y, W, 1);
        }
        g.setColor(Palette.LIGHT_GRAY);
        g.fillOval(120, 86, 80, 12);

        // Paintings
        frame(g, 16, 16, 40, 30);
        g.setColor(Palette.BLUE);
        g.fillRect(19, 19, 34, 12);
        g.setColor(Palette.DARK_GREEN);
        g.fillRect(19, 31, 34, 12);
        g.setColor(Palette.YELLOW);
        g.fillOval(44, 21, 6, 6);
        frame(g, 262, 16, 40, 30);
        g.setColor(Palette.PURPLE);
        g.fillRect(265, 19, 34, 24);
        g.setColor(Palette.PEACH);
        g.fillOval(276, 23, 12, 14);
        g.setColor(Palette.BLACK);
        g.fillRect(276, 22, 12, 4);

        // Sarcophagus standing at the back
        int sx = 70;
        g.setColor(Palette.ORANGE);
        g.fillRoundRect(sx, 22, 22, 56, 10, 10);
        g.setColor(Palette.YELLOW);
        g.fillOval(sx + 4, 26, 14, 16);
        g.setColor(Palette.BLACK);
        g.fillRect(sx + 7, 32, 2, 1);
        g.fillRect(sx + 13, 32, 2, 1);
        g.setColor(Palette.BLUE);
        for (int y = 46; y < 76; y += 5) {
            g.fillRect(sx + 2, y, 18, 2);
        }

        // Black cat statue on a plinth
        g.setColor(Palette.LIGHT_GRAY);
        g.fillRect(230, 76, 22, 4);
        g.setColor(Palette.BLACK);
        g.fillRect(236, 50, 10, 26);
        g.fillOval(234, 40, 14, 14);
        g.fillPolygon(new int[] {234, 236, 240}, new int[] {44, 35, 42}, 3);
        g.fillPolygon(new int[] {248, 246, 242}, new int[] {44, 35, 42}, 3);
        g.setColor(Palette.YELLOW);
        g.fillRect(238, 46, 1, 1);
        g.fillRect(244, 46, 1, 1);

        // Pedestal and the empty case, lid left open
        g.setColor(Palette.WHITE);
        g.fillRect(144, 56, 32, 36);
        g.setColor(Palette.LIGHT_GRAY);
        g.fillRect(170, 56, 6, 36);
        g.fillRect(142, 54, 36, 3);
        g.setColor(Palette.PURPLE);
        g.fillRect(154, 48, 12, 6);
        g.setColor(Palette.WHITE);
        g.drawRect(146, 30, 28, 24);
        g.drawPolygon(new int[] {146, 150, 178, 174}, new int[] {30, 18, 18, 30}, 4);

        // Velvet rope
        g.setColor(Palette.YELLOW);
        g.fillRect(112, 66, 3, 24);
        g.fillRect(205, 66, 3, 24);
        g.setColor(Palette.RED);
        g.drawPolyline(new int[] {114, 130, 150, 170, 190, 206}, new int[] {68, 74, 77, 77, 74, 68}, 6);

        // Gold flakes glinting on the floor
        if ((t / 12) % 3 != 0) {
            g.setColor(Palette.YELLOW);
            g.fillRect(150, 94, 1, 1);
            g.fillRect(158, 96, 2, 1);
            g.fillRect(167, 92, 1, 1);
        }
    }

    private static void security(Graphics2D g, int t) {
        // Cinder-block wall
        g.setColor(Palette.LIGHT_GRAY);
        g.fillRect(0, 0, W, 84);
        g.setColor(Palette.INDIGO);
        for (int y = 0; y < 84; y += 10) {
            g.fillRect(0, y, W, 1);
            for (int x = (y / 10 % 2) * 12; x < W; x += 24) {
                g.fillRect(x, y, 1, 10);
            }
        }
        g.setColor(Palette.DARK_GRAY);
        g.fillRect(0, 84, W, H - 84);

        // Clock
        g.setColor(Palette.WHITE);
        g.fillOval(30, 10, 16, 16);
        g.setColor(Palette.BLACK);
        g.drawOval(30, 10, 15, 15);
        g.drawLine(38, 18, 38, 12);
        g.drawLine(38, 18, 42, 18);

        // Wall of camera monitors, most showing static
        g.setColor(Palette.BLACK);
        g.fillRect(176, 8, 128, 52);
        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < 3; c++) {
                int sx = 180 + c * 41;
                int sy = 12 + r * 23;
                if (r == 0 && c == 1) {
                    g.setColor(Palette.DARK_BLUE);
                    g.fillRect(sx, sy, 37, 20);
                    g.setColor(Palette.WHITE);
                    g.fillRect(sx + 15, sy + 9, 7, 9);
                    continue;
                }
                g.setColor(Palette.DARK_GRAY);
                g.fillRect(sx, sy, 37, 20);
                g.setColor(Palette.LIGHT_GRAY);
                int seed = (t / 3) * 31 + (r * 3 + c) * 977;
                for (int k = 0; k < 25; k++) {
                    seed = seed * 1103515245 + 12345;
                    int bits = seed >>> 1;
                    g.fillRect(sx + bits % 37, sy + (bits / 37) % 20, 1, 1);
                }
            }
        }
        if ((t / 30) % 2 == 0) {
            g.setColor(Palette.RED);
            g.fillRect(182, 14, 2, 2);
        }

        // Desk with coffee and donuts
        g.setColor(Palette.BROWN);
        g.fillRect(160, 64, 150, 6);
        g.fillRect(166, 70, 6, 20);
        g.fillRect(298, 70, 6, 20);
        g.setColor(Palette.WHITE);
        g.fillRect(200, 57, 6, 7);
        g.fillRect(206, 59, 2, 3);
        g.setColor(Palette.PINK);
        g.fillRect(250, 58, 26, 6);
        g.setColor(Palette.ORANGE);
        g.fillOval(254, 55, 7, 4);
        g.fillOval(264, 55, 7, 4);

        // Sleepy guard with floating Zs
        guard(g, 90, 96, t, true);
        for (int i = 0; i < 3; i++) {
            int age = (t / 2 + i * 20) % 60;
            PixelFont.draw(g, "Z", 114 + age / 5, 44 - age / 2, Palette.DARK_BLUE);
        }
    }

    private static void office(Graphics2D g, int t) {
        g.setColor(Palette.DARK_GRAY);
        g.fillRect(0, 0, W, 80);
        g.setColor(Palette.ORANGE);
        g.fillRect(0, 60, W, 2);
        g.setColor(Palette.RED);
        g.fillRect(0, 80, W, H - 80);
        g.setColor(Palette.PURPLE);
        for (int y = 84; y < H; y += 8) {
            for (int x = (y / 8 % 2) * 8; x < W; x += 16) {
                g.fillRect(x, y, 2, 2);
            }
        }

        // Window onto the city at night
        g.setColor(Palette.BROWN);
        g.fillRect(118, 8, 64, 44);
        g.setColor(Palette.DARK_BLUE);
        g.fillRect(121, 11, 58, 38);
        int[][] buildings = {{121, 30, 12, 19}, {135, 20, 14, 29}, {151, 34, 10, 15}, {163, 25, 16, 24}};
        for (int[] b : buildings) {
            g.setColor(Palette.BLACK);
            g.fillRect(b[0], b[1], b[2], b[3]);
            g.setColor(Palette.YELLOW);
            for (int wy = b[1] + 3; wy < b[1] + b[3] - 2; wy += 4) {
                for (int wx = b[0] + 2; wx < b[0] + b[2] - 1; wx += 3) {
                    if ((wx * 3 + wy * 5 + t / 90) % 4 == 0) {
                        g.fillRect(wx, wy, 1, 2);
                    }
                }
            }
        }

        // Certificates
        for (int i = 0; i < 2; i++) {
            int cx = 28 + i * 30;
            int cy = 16 + i * 6;
            g.setColor(Palette.YELLOW);
            g.drawRect(cx - 1, cy - 1, 21, 15);
            g.setColor(Palette.WHITE);
            g.fillRect(cx, cy, 20, 14);
            g.setColor(Palette.DARK_GRAY);
            g.fillRect(cx + 3, cy + 3, 14, 1);
            g.fillRect(cx + 3, cy + 6, 10, 1);
            g.setColor(Palette.RED);
            g.fillRect(cx + 14, cy + 9, 3, 3);
        }

        // Filing cabinet and a plant
        g.setColor(Palette.LIGHT_GRAY);
        g.fillRect(250, 34, 34, 56);
        g.setColor(Palette.DARK_GRAY);
        for (int y = 38; y < 88; y += 17) {
            g.drawRect(253, y, 27, 14);
            g.fillRect(263, y + 6, 8, 2);
        }
        g.setColor(Palette.BROWN);
        g.fillRect(292, 76, 14, 14);
        g.setColor(Palette.DARK_GREEN);
        g.fillOval(287, 56, 12, 22);
        g.fillOval(298, 58, 12, 20);
        g.setColor(Palette.GREEN);
        g.fillOval(293, 50, 10, 18);

        // Desk with computer and the telling papers
        g.setColor(Palette.BROWN);
        g.fillRect(120, 62, 110, 30);
        g.setColor(Palette.ORANGE);
        g.fillRect(118, 60, 114, 3);
        g.setColor(Palette.BLACK);
        g.drawRect(130, 68, 30, 10);
        g.drawRect(190, 68, 30, 10);
        g.fillRect(190, 38, 30, 20);
        g.setColor(Palette.DARK_GREEN);
        g.fillRect(192, 40, 26, 16);
        g.setColor(Palette.GREEN);
        g.fillRect(194, 43, 16, 1);
        g.fillRect(194, 46, 20, 1);
        g.fillRect(194, 49, 12, 1);
        if ((t / 20) % 2 == 0) {
            g.fillRect(207, 49, 2, 2);
        }
        g.setColor(Palette.DARK_GRAY);
        g.fillRect(203, 58, 4, 2);
        g.setColor(Palette.WHITE);
        g.fillRect(134, 55, 18, 5);
        g.fillRect(158, 57, 16, 3);
        g.setColor(Palette.RED);
        g.fillRect(168, 57, 3, 2);

        curator(g, 76, 94, t);
    }

    private static void storage(Graphics2D g, int t) {
        // Brick walls
        g.setColor(Palette.DARK_BLUE);
        g.fillRect(0, 0, W, 80);
        g.setColor(Palette.BLACK);
        for (int y = 0; y < 80; y += 6) {
            g.fillRect(0, y, W, 1);
            for (int x = (y / 6 % 2) * 8; x < W; x += 16) {
                g.fillRect(x, y, 1, 6);
            }
        }
        g.setColor(Palette.DARK_GRAY);
        g.fillRect(0, 80, W, H - 80);

        // Swinging light bulb and its pool of light
        int bx = 160 + (int) Math.round(Math.sin(t * 0.04) * 8);
        g.setColor(Palette.INDIGO);
        g.fillOval(bx - 44, 86, 88, 14);
        g.setColor(Palette.BLACK);
        g.drawLine(160, 0, bx, 18);
        g.setColor(Palette.YELLOW);
        g.fillOval(bx - 3, 18, 7, 8);
        g.setColor(Palette.WHITE);
        g.fillRect(bx - 1, 20, 2, 2);

        // Crates
        crate(g, 12, 50, 40, 34);
        crate(g, 20, 24, 30, 26);
        crate(g, 56, 62, 30, 22);
        crate(g, 232, 44, 44, 40);
        crate(g, 278, 58, 34, 26);

        // Statue under a dust sheet
        g.setColor(Palette.WHITE);
        g.fillPolygon(new int[] {200, 206, 216, 222, 228, 194}, new int[] {86, 44, 38, 44, 86, 86}, 6);
        g.fillOval(203, 30, 16, 14);
        g.setColor(Palette.LIGHT_GRAY);
        g.drawLine(210, 48, 205, 84);
        g.drawLine(216, 50, 221, 84);

        // Intern with a glowing phone
        intern(g, 130, 96, t);
        g.setColor((t / 10) % 2 == 0 ? Palette.BLUE : Palette.WHITE);
        g.fillRect(155, 74, 3, 5);
    }

    private static void museumLineup(Graphics2D g, int t) {
        gallery(g, t);
        guard(g, 84, 100, t, false);
        intern(g, 148, 100, t);
        curator(g, 212, 100, t);
    }

    private static void mask(Graphics2D g, int t) {
        g.setColor(Palette.DARK_BLUE);
        g.fillRect(0, 0, W, H);
        int cx = 160, cy = 50;
        rays(g, t, cx, cy);

        // Striped headdress
        Polygon hood = new Polygon(new int[] {cx - 16, cx + 16, cx + 26, cx - 26}, new int[] {cy - 26, cy - 26, cy + 26, cy + 26}, 4);
        g.setColor(Palette.BLUE);
        g.fillPolygon(hood);
        Graphics2D stripes = (Graphics2D) g.create();
        stripes.clip(hood);
        stripes.setColor(Palette.YELLOW);
        for (int y = cy - 26; y < cy + 26; y += 5) {
            stripes.fillRect(cx - 30, y, 60, 2);
        }
        stripes.dispose();

        // Golden face
        g.setColor(Palette.YELLOW);
        g.fillOval(cx - 12, cy - 20, 24, 32);
        g.setColor(Palette.ORANGE);
        g.fillRect(cx - 1, cy - 4, 2, 7);
        g.fillRect(cx - 4, cy + 6, 8, 2);
        g.fillRect(cx - 1, cy - 25, 3, 6);
        g.setColor(Palette.WHITE);
        g.fillRect(cx - 8, cy - 8, 5, 3);
        g.fillRect(cx + 3, cy - 8, 5, 3);
        g.setColor(Palette.BLACK);
        g.fillRect(cx - 6, cy - 8, 2, 3);
        g.fillRect(cx + 5, cy - 8, 2, 3);
        g.fillRect(cx - 9, cy - 9, 7, 1);
        g.fillRect(cx + 2, cy - 9, 7, 1);
        g.fillRect(cx - 11, cy - 7, 2, 1);
        g.fillRect(cx + 9, cy - 7, 2, 1);

        // Beard
        g.setColor(Palette.BLUE);
        g.fillRect(cx - 3, cy + 12, 6, 12);
        g.setColor(Palette.YELLOW);
        for (int k = 0; k < 3; k++) {
            g.fillRect(cx - 3, cy + 14 + k * 4, 6, 1);
        }

        twinkles(g, t);
    }
}
