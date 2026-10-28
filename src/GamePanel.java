import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Draws the game onto a tiny 320x256 canvas, then blows it up with hard pixel edges.
 * That low resolution is what gives the 8-bit look. Also handles keyboard and mouse input.
 */
public class GamePanel extends JPanel {
    public static final int W = 320, H = 256;

    // Limits on how much fits on screen (Story checks story files against these).
    public static final int TEXT_COLUMNS = 49, TEXT_ROWS = 6, MAX_CHOICES = 5, LABEL_MAX = 44, TITLE_MAX = 36;

    private static final int HUD_H = 14, ART_Y = 14;
    private static final int TEXT_Y = 122, TEXT_H = 62;
    private static final int CHOICE_Y = 188, CHOICE_H = 64, ROW_H = 11;
    private static final int LIST_Y = 146, LIST_H = 62, LIST_ROWS = 5;

    private enum Screen { TITLE, CASE_OPTIONS, PLAYING, GAME_OVER }

    private final BufferedImage canvas = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
    private final List<Story> stories;
    private final SaveData save;
    private final Sound sound;
    private final GameState state = new GameState();

    private Screen screen = Screen.TITLE;
    private int tick, screenAge, selected;
    private Story story; // the case being played
    private Scene scene;
    private List<String> lines;
    private String fullText = "";
    private List<Choice> choices;
    private int textLength, typed;
    private int flashTicks, toastTicks;
    private String toast = "";

    // Where the scaled-up canvas sits inside the window, for mapping mouse clicks.
    private int offsetX, offsetY, scale = 1;

    public GamePanel(int startScale, List<Story> stories, SaveData save, Sound sound) {
        this.stories = stories;
        this.save = save;
        this.sound = sound;
        this.story = stories.get(0);

        setPreferredSize(new Dimension(W * startScale, H * startScale));
        setBackground(Color.BLACK);
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                pressKey(e.getKeyCode());
            }
        });

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = menuRowAt(e.getX(), e.getY());
                if (row >= 0 && row != selected) {
                    selected = row;
                    sound.play(Sound.Effect.MOVE);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                int row = menuRowAt(e.getX(), e.getY());
                if (row >= 0) {
                    selected = row;
                    activate(row);
                } else if (screen == Screen.GAME_OVER || (screen == Screen.PLAYING && !choosing())) {
                    pressKey(KeyEvent.VK_ENTER);
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);

        // About 60 frames per second.
        new Timer(16, e -> {
            update();
            repaint();
        }).start();
    }

    // ---------------------------------------------------------------- input

    void pressKey(int key) {
        boolean ok = key == KeyEvent.VK_ENTER || key == KeyEvent.VK_SPACE;
        boolean back = key == KeyEvent.VK_ESCAPE;

        if (key == KeyEvent.VK_M) {
            sound.setEnabled(!sound.isEnabled());
            save.setSoundOn(sound.isEnabled());
            showToast(sound.isEnabled() ? "SOUND ON" : "SOUND OFF");
            return;
        }

        switch (screen) {
            case TITLE:
                if (screenAge < 20) {
                    return; // so a held-down Enter doesn't skip straight past
                }
                break;
            case CASE_OPTIONS:
                if (back) {
                    backToTitle();
                    return;
                }
                break;
            case GAME_OVER:
                if (screenAge > 45 && ok) {
                    startNew();
                } else if (screenAge > 45 && back) {
                    backToTitle();
                }
                return;
            case PLAYING:
                if (back) {
                    backToTitle(); // progress is already saved
                    return;
                }
                if (!typingDone()) {
                    if (ok) {
                        typed = textLength;
                    }
                    return;
                }
                if (scene.isWinning()) {
                    if (ok) {
                        backToTitle();
                    }
                    return;
                }
                break;
        }

        // Every other screen is a menu: move with arrows, pick with Enter or a number.
        int n = menuSize();
        if (n == 0) {
            return;
        }
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) {
            selected = (selected + n - 1) % n;
            sound.play(Sound.Effect.MOVE);
        } else if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) {
            selected = (selected + 1) % n;
            sound.play(Sound.Effect.MOVE);
        } else if (ok) {
            activate(selected);
        } else if (key >= KeyEvent.VK_1 && key < KeyEvent.VK_1 + Math.min(n, 9)) {
            selected = key - KeyEvent.VK_1;
            activate(selected);
        }
    }

    private int menuSize() {
        switch (screen) {
            case TITLE:
                return stories.size();
            case CASE_OPTIONS:
                return 3;
            case PLAYING:
                return choosing() ? choices.size() : 0;
            default:
                return 0;
        }
    }

    private void activate(int index) {
        switch (screen) {
            case TITLE:
                openCase(stories.get(index));
                break;
            case CASE_OPTIONS:
                if (index == 0) {
                    continueCase();
                } else if (index == 1) {
                    startNew();
                } else {
                    backToTitle();
                }
                break;
            case PLAYING:
                confirm(choices.get(index));
                break;
            default:
                break;
        }
    }

    private boolean choosing() {
        return screen == Screen.PLAYING && typingDone() && !scene.isWinning();
    }

    /** The case list scrolls if there are more cases than fit in the box. */
    private int firstVisibleCase() {
        return Math.max(0, Math.min(selected - (LIST_ROWS - 1), stories.size() - LIST_ROWS));
    }

    private int menuRowAt(int mouseX, int mouseY) {
        int n = menuSize();
        if (n == 0) {
            return -1;
        }
        int x = (mouseX - offsetX) / scale;
        int y = (mouseY - offsetY) / scale;
        int top = (screen == Screen.PLAYING ? CHOICE_Y : LIST_Y) + 4;
        if (x < 4 || x > W - 4 || y < top) {
            return -1;
        }
        int row = (y - top) / ROW_H;
        if (screen == Screen.TITLE) {
            return row < Math.min(n, LIST_ROWS) ? row + firstVisibleCase() : -1;
        }
        return row < n ? row : -1;
    }

    // ---------------------------------------------------------------- game flow

    private void openCase(Story picked) {
        story = picked;
        if (save.loadProgress(story, new GameState())) {
            sound.play(Sound.Effect.SELECT);
            setScreen(Screen.CASE_OPTIONS);
        } else {
            startNew();
        }
    }

    private void startNew() {
        state.reset(story.getStart());
        save.clearProgress(story);
        beginPlaying();
    }

    private void continueCase() {
        if (!save.loadProgress(story, state)) {
            state.reset(story.getStart());
        }
        beginPlaying();
    }

    private void beginPlaying() {
        toastTicks = 0;
        flashTicks = 0;
        setScreen(Screen.PLAYING);
        enterScene(state.getSceneId());
        sound.play(Sound.Effect.START);
    }

    private void backToTitle() {
        setScreen(Screen.TITLE);
        selected = stories.indexOf(story);
    }

    private void confirm(Choice choice) {
        String clue = choice.getGives();
        boolean newClue = clue != null && !state.hasClue(clue);

        boolean mistake = state.apply(choice);
        if (mistake) {
            flashTicks = 24;
            sound.play(Sound.Effect.BAD);
            if (state.getHearts() <= 0) {
                save.clearProgress(story);
                sound.then(Sound.Effect.GAME_OVER);
                setScreen(Screen.GAME_OVER);
                return;
            }
        }
        if (newClue && state.hasClue(clue)) {
            showToast("CLUE FOUND: " + story.clueName(clue));
            if (!mistake) {
                sound.play(Sound.Effect.CLUE);
            }
        } else if (!mistake) {
            sound.play(Sound.Effect.SELECT);
        }
        enterScene(state.getSceneId());
    }

    private void enterScene(String id) {
        scene = story.get(id);
        lines = PixelFont.wrap(scene.getText(), TEXT_COLUMNS);
        fullText = String.join("", lines);
        textLength = fullText.length();
        typed = 0;
        choices = state.visibleChoices(scene);
        selected = 0;

        if (scene.isWinning()) {
            save.markSolved(story);
            sound.play(Sound.Effect.WIN);
        } else {
            save.saveProgress(story, state);
        }
    }

    private void setScreen(Screen next) {
        screen = next;
        screenAge = 0;
        selected = 0;
    }

    private void showToast(String message) {
        toast = message;
        toastTicks = 180;
    }

    private boolean typingDone() {
        return typed >= textLength;
    }

    private void update() {
        tick++;
        screenAge++;
        if (screen == Screen.PLAYING && typed < textLength) {
            // Typewriter effect: one letter per frame, with a blip every few letters.
            char letter = fullText.charAt(typed);
            typed++;
            if (letter != ' ' && typed % 3 == 0) {
                sound.play(Sound.Effect.TYPE);
            }
        }
        if (flashTicks > 0) {
            flashTicks--;
        }
        if (toastTicks > 0) {
            toastTicks--;
        }
    }

    // ---------------------------------------------------------------- drawing

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D c = canvas.createGraphics();
        render(c);
        c.dispose();

        scale = Math.max(1, Math.min(getWidth() / W, getHeight() / H));
        offsetX = (getWidth() - W * scale) / 2;
        offsetY = (getHeight() - H * scale) / 2;
        int shake = flashTicks > 12 ? ((tick / 2) % 2 == 0 ? -2 : 2) * scale : 0;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(canvas, offsetX + shake, offsetY, W * scale, H * scale, null);
    }

    private void render(Graphics2D g) {
        g.setColor(Palette.BLACK);
        g.fillRect(0, 0, W, H);
        switch (screen) {
            case TITLE:
            case CASE_OPTIONS:
                renderTitle(g);
                break;
            case PLAYING:
                renderPlaying(g);
                break;
            case GAME_OVER:
                renderGameOver(g);
                break;
        }
        // Red flash after a bad choice.
        if (flashTicks > 0 && flashTicks % 6 < 3) {
            g.setColor(new Color(255, 0, 77, 110));
            g.fillRect(0, 0, W, H);
        }
    }

    private void renderTitle(Graphics2D g) {
        shadowText(g, "MYSTERY HUNTERS", center("MYSTERY HUNTERS", 3), 10, Palette.YELLOW, 3);
        Story shown = screen == Screen.TITLE ? stories.get(selected) : story;
        PixelArt.draw(g, shown.getCover(), tick, 0, 38);
        box(g, 4, LIST_Y, W - 8, LIST_H);

        String status;
        Color statusColor;
        GameState saved = new GameState();
        boolean inProgress = save.loadProgress(shown, saved);

        if (screen == Screen.TITLE) {
            int first = firstVisibleCase();
            for (int i = first; i < Math.min(stories.size(), first + LIST_ROWS); i++) {
                int y = LIST_Y + 6 + (i - first) * ROW_H;
                menuRow(g, y, i == selected, caseName(i));
                if (save.isSolved(stories.get(i))) {
                    PixelArt.star(g, W - 18, y);
                }
            }
            if (inProgress) {
                status = "IN PROGRESS: " + saved.clueCount() + "/" + shown.clueCount() + " CLUES FOUND";
                statusColor = Palette.ORANGE;
            } else if (save.isSolved(shown)) {
                status = "SOLVED! PLAY AGAIN ANY TIME";
                statusColor = Palette.GREEN;
            } else {
                status = "UNSOLVED";
                statusColor = Palette.LIGHT_GRAY;
            }
        } else {
            String[] options = {
                    "CONTINUE (" + saved.clueCount() + "/" + story.clueCount() + " CLUES, "
                            + plural(saved.getHearts(), "HEART") + ")",
                    "START OVER",
                    "BACK TO CASE LIST",
            };
            for (int i = 0; i < options.length; i++) {
                menuRow(g, LIST_Y + 6 + i * ROW_H, i == selected, options[i]);
            }
            status = caseName(stories.indexOf(story));
            statusColor = Palette.PEACH;
        }
        PixelFont.draw(g, status, center(status, 1), 214, statusColor);

        String controls = "ENTER: PLAY   M: SOUND " + (sound.isEnabled() ? "ON" : "OFF");
        PixelFont.draw(g, controls, center(controls, 1), 230, Palette.LIGHT_GRAY);
        String warning = "THREE BAD CALLS AND THE THIEF ESCAPES!";
        PixelFont.draw(g, warning, center(warning, 1), 242, Palette.RED);
    }

    private void renderPlaying(Graphics2D g) {
        // Status bar: hearts on the left, clue count on the right.
        g.setColor(Palette.DARK_BLUE);
        g.fillRect(0, 0, W, HUD_H);
        for (int i = 0; i < GameState.MAX_HEARTS; i++) {
            PixelArt.heart(g, 4 + i * 9, 4, i < state.getHearts());
        }
        PixelFont.draw(g, "ESC: MENU", center("ESC: MENU", 1), 4, Palette.INDIGO);
        String clues = "CLUES " + state.clueCount() + "/" + story.clueCount();
        PixelFont.draw(g, clues, W - 4 - PixelFont.width(clues, 1), 4, Palette.YELLOW);

        PixelArt.draw(g, scene.getArt(), tick, 0, ART_Y);

        if (toastTicks > 0) {
            box(g, 6, ART_Y + 4, W - 12, 15);
            PixelFont.draw(g, toast, center(toast, 1), ART_Y + 8, Palette.GREEN);
        }

        // Story text, revealed a letter at a time.
        box(g, 4, TEXT_Y, W - 8, TEXT_H);
        int remaining = typed;
        for (int i = 0; i < lines.size() && remaining > 0; i++) {
            String line = lines.get(i);
            String shown = line.substring(0, Math.min(line.length(), remaining));
            PixelFont.draw(g, shown, 10, TEXT_Y + 6 + i * PixelFont.LINE_H, Palette.WHITE);
            remaining -= line.length();
        }

        box(g, 4, CHOICE_Y, W - 8, CHOICE_H);
        if (!typingDone()) {
            if ((tick / 20) % 2 == 0) {
                String skip = "ENTER: SKIP";
                PixelFont.draw(g, skip, W - 12 - PixelFont.width(skip, 1), CHOICE_Y + CHOICE_H - 13, Palette.DARK_GRAY);
            }
        } else if (scene.isWinning()) {
            shadowText(g, "CASE CLOSED!", center("CASE CLOSED!", 2), CHOICE_Y + 14, Palette.GREEN, 2);
            if ((tick / 30) % 2 == 0) {
                String again = "PRESS ENTER TO CHOOSE A CASE";
                PixelFont.draw(g, again, center(again, 1), CHOICE_Y + 42, Palette.WHITE);
            }
        } else {
            for (int i = 0; i < choices.size(); i++) {
                menuRow(g, CHOICE_Y + 6 + i * ROW_H, i == selected, (i + 1) + ". " + choices.get(i).getLabel());
            }
        }
    }

    private void renderGameOver(Graphics2D g) {
        shadowText(g, "GAME OVER", center("GAME OVER", 4), 24, Palette.RED, 4);
        PixelArt.draw(g, story.getCover(), tick, 0, 66);

        int y = 176;
        String first = "TOO MANY BAD CALLS, DETECTIVE.";
        PixelFont.draw(g, first, center(first, 1), y, Palette.WHITE);
        for (String line : PixelFont.wrap(story.getGameOverText(), TEXT_COLUMNS)) {
            y += PixelFont.LINE_H;
            PixelFont.draw(g, line, center(line, 1), y, Palette.WHITE);
        }
        String clues = "CLUES FOUND: " + state.clueCount() + "/" + story.clueCount();
        PixelFont.draw(g, clues, center(clues, 1), 208, Palette.YELLOW);
        if ((tick / 30) % 2 == 0) {
            String again = "PRESS ENTER TO TRY AGAIN";
            PixelFont.draw(g, again, center(again, 1), 228, Palette.WHITE);
        }
        PixelFont.draw(g, "ESC: CASE LIST", center("ESC: CASE LIST", 1), 242, Palette.DARK_GRAY);
    }

    private String caseName(int index) {
        return "CASE " + (index + 1) + ": " + stories.get(index).getTitle();
    }

    private static String plural(int count, String word) {
        return count + " " + word + (count == 1 ? "" : "S");
    }

    /** One line of a menu, highlighted when selected. */
    private static void menuRow(Graphics2D g, int y, boolean isSelected, String label) {
        if (isSelected) {
            g.setColor(Palette.DARK_BLUE);
            g.fillRect(7, y - 2, W - 14, ROW_H);
        }
        PixelFont.draw(g, (isSelected ? "> " : "  ") + label, 10, y, isSelected ? Palette.YELLOW : Palette.WHITE);
    }

    /** A black text box with a white border and clipped corners, like classic RPGs. */
    private static void box(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(Palette.BLACK);
        g.fillRect(x, y, w, h);
        g.setColor(Palette.WHITE);
        g.drawRect(x + 1, y + 1, w - 3, h - 3);
        g.setColor(Palette.BLACK);
        g.fillRect(x + 1, y + 1, 1, 1);
        g.fillRect(x + w - 2, y + 1, 1, 1);
        g.fillRect(x + 1, y + h - 2, 1, 1);
        g.fillRect(x + w - 2, y + h - 2, 1, 1);
    }

    private static void shadowText(Graphics2D g, String text, int x, int y, Color color, int scale) {
        PixelFont.draw(g, text, x + scale, y + scale, Palette.BLACK, scale);
        PixelFont.draw(g, text, x, y, color, scale);
    }

    private static int center(String text, int scale) {
        return (W - PixelFont.width(text, scale)) / 2;
    }
}
