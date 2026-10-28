import java.util.LinkedHashMap;
import java.util.Map;

/** One mystery case: its scenes, clues and settings. Loaded from a file in the "stories" folder. */
public class Story {
    private final String id;
    private final String fileName;
    private final Map<String, Scene> scenes = new LinkedHashMap<>();
    private final Map<String, String> clueNames = new LinkedHashMap<>();
    private String title;
    private String start;
    private String cover = "manor";
    private String gameOverText = "The thief got away.";

    public Story(String id, String fileName) {
        this.id = id;
        this.fileName = fileName;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getStart() {
        return start;
    }

    public String getCover() {
        return cover;
    }

    public String getGameOverText() {
        return gameOverText;
    }

    public Scene get(String sceneId) {
        return scenes.get(sceneId);
    }

    public String clueName(String clue) {
        return clueNames.get(clue);
    }

    public int clueCount() {
        return clueNames.size();
    }

    void setTitle(String title) {
        this.title = title;
    }

    void setStart(String start) {
        this.start = start;
    }

    void setCover(String cover) {
        this.cover = cover;
    }

    void setGameOverText(String text) {
        this.gameOverText = text;
    }

    void addClue(String clue, String name, int line) {
        if (clueNames.containsKey(clue)) {
            throw error("line " + line + ": there are two clues called '" + clue + "'");
        }
        clueNames.put(clue, name);
    }

    void addScene(Scene scene) {
        if (scenes.containsKey(scene.getId())) {
            throw error("line " + scene.getLine() + ": there are two scenes called '" + scene.getId() + "'");
        }
        scenes.put(scene.getId(), scene);
    }

    /** Catches story mistakes at startup, so a broken link fails loudly instead of mid-game. */
    void validate() {
        if (title == null) {
            throw error("it needs a 'TITLE:' line");
        }
        if (title.length() > GamePanel.TITLE_MAX) {
            throw error("the title is too long (max " + GamePanel.TITLE_MAX + " letters)");
        }
        if (start == null) {
            throw error("it needs a 'START:' line saying which scene comes first");
        }
        if (!scenes.containsKey(start)) {
            throw error("START says '" + start + "', but there's no scene with that name");
        }
        checkArt("COVER", cover);
        if (PixelFont.wrap(gameOverText, GamePanel.TEXT_COLUMNS).size() > 2) {
            throw error("the GAMEOVER text is too long (max 2 lines)");
        }
        for (Map.Entry<String, String> clue : clueNames.entrySet()) {
            if (("CLUE FOUND: " + clue.getValue()).length() > GamePanel.TEXT_COLUMNS) {
                throw error("the name of clue '" + clue.getKey() + "' is too long to fit on screen");
            }
        }

        boolean hasEnding = false;
        for (Scene scene : scenes.values()) {
            String where = "scene '" + scene.getId() + "' (line " + scene.getLine() + ") ";
            hasEnding |= scene.isWinning();
            if (scene.getArt() == null) {
                throw error(where + "needs an 'ART:' line");
            }
            checkArt(where + "ART", scene.getArt());
            if (scene.getText().isEmpty()) {
                throw error(where + "has no story text");
            }
            int lines = PixelFont.wrap(scene.getText(), GamePanel.TEXT_COLUMNS).size();
            if (lines > GamePanel.TEXT_ROWS) {
                throw error(where + "has too much text: " + lines + " lines, but only " + GamePanel.TEXT_ROWS + " fit");
            }
            if (scene.getChoices().isEmpty() && !scene.isWinning()) {
                throw error(where + "has no choices, so the player would be stuck");
            }
            if (scene.getChoices().size() > GamePanel.MAX_CHOICES) {
                throw error(where + "has more than " + GamePanel.MAX_CHOICES + " choices");
            }
            for (Choice choice : scene.getChoices()) {
                if (choice.getLabel().length() > GamePanel.LABEL_MAX) {
                    throw error(where + "choice '" + choice.getLabel() + "' is too long (max " + GamePanel.LABEL_MAX + " letters)");
                }
                checkLink(where, choice.getNext());
                if (choice.getOtherwise() != null) {
                    checkLink(where, choice.getOtherwise());
                }
                if (choice.getGives() != null) {
                    checkClue(where, choice.getGives());
                }
                for (String needed : choice.getNeeds()) {
                    checkClue(where, needed);
                }
            }
        }
        if (!hasEnding) {
            throw error("no scene has 'ENDING: win', so the case can't be solved");
        }
    }

    private void checkArt(String what, String art) {
        if (!PixelArt.exists(art)) {
            throw error(what + " '" + art + "' doesn't exist. Pick one of: " + String.join(", ", PixelArt.names()));
        }
    }

    private void checkLink(String where, String target) {
        if (!scenes.containsKey(target)) {
            throw error(where + "has a choice leading to '" + target + "', but there's no scene with that name");
        }
    }

    private void checkClue(String where, String clue) {
        if (!clueNames.containsKey(clue)) {
            throw error(where + "uses clue '" + clue + "', which isn't listed in a 'CLUE:' line");
        }
    }

    IllegalStateException error(String message) {
        return new IllegalStateException(fileName + ": " + message);
    }
}
