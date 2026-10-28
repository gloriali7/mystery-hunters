import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Remembers things between runs: where you left off in each case, which cases
 * you've solved, and whether sound is on. Stored in a small text file at
 * ~/.mystery-hunters/save.properties
 */
public class SaveData {
    private final Path file = Paths.get(System.getProperty("user.home"), ".mystery-hunters", "save.properties");
    private final Properties props = new Properties();

    public SaveData() {
        if (Files.exists(file)) {
            try (InputStream in = Files.newInputStream(file)) {
                props.load(in);
            } catch (IOException e) {
                System.err.println("Couldn't read save file, starting fresh: " + e.getMessage());
            }
        }
    }

    public boolean isSoundOn() {
        return !"false".equals(props.getProperty("sound"));
    }

    public void setSoundOn(boolean on) {
        props.setProperty("sound", String.valueOf(on));
        write();
    }

    public boolean isSolved(Story story) {
        return "true".equals(props.getProperty(key(story, "solved")));
    }

    public void markSolved(Story story) {
        props.setProperty(key(story, "solved"), "true");
        clearProgress(story);
    }

    public void saveProgress(Story story, GameState state) {
        props.setProperty(key(story, "scene"), state.getSceneId());
        props.setProperty(key(story, "hearts"), String.valueOf(state.getHearts()));
        props.setProperty(key(story, "clues"), String.join(",", state.getClues()));
        write();
    }

    /**
     * Loads saved progress for a case into state. Returns false if there is none,
     * or if the story file has changed so the save no longer makes sense.
     */
    public boolean loadProgress(Story story, GameState state) {
        String sceneId = props.getProperty(key(story, "scene"));
        if (sceneId == null || story.get(sceneId) == null || story.get(sceneId).isWinning()) {
            return false;
        }
        int hearts;
        try {
            hearts = Integer.parseInt(props.getProperty(key(story, "hearts"), ""));
        } catch (NumberFormatException e) {
            return false;
        }
        if (hearts < 1 || hearts > GameState.MAX_HEARTS) {
            return false;
        }
        List<String> clues = new ArrayList<>();
        for (String clue : props.getProperty(key(story, "clues"), "").split(",")) {
            if (clue.isEmpty()) {
                continue;
            }
            if (story.clueName(clue) == null) {
                return false;
            }
            clues.add(clue);
        }
        state.restore(sceneId, hearts, clues);
        return true;
    }

    public void clearProgress(Story story) {
        props.remove(key(story, "scene"));
        props.remove(key(story, "hearts"));
        props.remove(key(story, "clues"));
        write();
    }

    private static String key(Story story, String name) {
        return story.getId() + "." + name;
    }

    private void write() {
        try {
            Files.createDirectories(file.getParent());
            try (OutputStream out = Files.newOutputStream(file)) {
                props.store(out, "Mystery Hunters save data");
            }
        } catch (IOException e) {
            System.err.println("Couldn't save progress: " + e.getMessage());
        }
    }
}
