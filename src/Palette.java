import java.awt.Color;

/**
 * The 16 colours the whole game is drawn with (the PICO-8 palette).
 * Sticking to a small fixed palette is a big part of what makes it look retro.
 */
public final class Palette {
    public static final Color BLACK = new Color(0x000000);
    public static final Color DARK_BLUE = new Color(0x1D2B53);
    public static final Color PURPLE = new Color(0x7E2553);
    public static final Color DARK_GREEN = new Color(0x008751);
    public static final Color BROWN = new Color(0xAB5236);
    public static final Color DARK_GRAY = new Color(0x5F574F);
    public static final Color LIGHT_GRAY = new Color(0xC2C3C7);
    public static final Color WHITE = new Color(0xFFF1E8);
    public static final Color RED = new Color(0xFF004D);
    public static final Color ORANGE = new Color(0xFFA300);
    public static final Color YELLOW = new Color(0xFFEC27);
    public static final Color GREEN = new Color(0x00E436);
    public static final Color BLUE = new Color(0x29ADFF);
    public static final Color INDIGO = new Color(0x83769C);
    public static final Color PINK = new Color(0xFF77A8);
    public static final Color PEACH = new Color(0xFFCCAA);

    private Palette() {
    }
}
