import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Reads the mystery cases from the .txt files in the "stories" folder.
 * Files are listed in name order, so "1-..." comes before "2-...".
 * In the web version there is no folder to look in, so the stories are packed
 * inside the game's .jar with a "stories/index.txt" listing them.
 * See README.md for the story file format.
 */
public final class StoryLoader {
    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
            "TITLE", "COVER", "START", "GAMEOVER", "CLUE", "SCENE", "ART", "ENDING"));

    private StoryLoader() {
    }

    public static List<Story> loadAll() throws IOException {
        // The web build packs the stories inside the .jar. Use those rather than asking
        // the web server for a "stories" folder, which some hosts answer with a web page.
        if (App.class.getResource("/stories/index.txt") != null) {
            return loadPacked();
        }
        Path folder = findFolder();
        List<Path> files = new ArrayList<>();
        try (DirectoryStream<Path> dir = Files.newDirectoryStream(folder, "*.txt")) {
            for (Path file : dir) {
                files.add(file);
            }
        }
        Collections.sort(files);
        if (files.isEmpty()) {
            throw new IllegalStateException("There are no .txt story files in " + folder.toAbsolutePath());
        }
        List<Story> stories = new ArrayList<>();
        for (Path file : files) {
            stories.add(load(file));
        }
        return stories;
    }

    /** Looks for "stories" in the folder the game was started from, then next to the compiled code. */
    private static Path findFolder() {
        List<Path> candidates = new ArrayList<>();
        candidates.add(Paths.get("stories"));
        try {
            Path code = Paths.get(App.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            candidates.add(code.resolveSibling("stories"));
        } catch (Exception ignored) {
            // Fall back to the working directory only.
        }
        for (Path candidate : candidates) {
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Couldn't find the 'stories' folder. It should be in the project folder, next to 'src'.");
    }

    /** Loads the stories packed inside the .jar, in the order index.txt lists them. */
    private static List<Story> loadPacked() throws IOException {
        List<Story> stories = new ArrayList<>();
        for (String fileName : readResource("index.txt")) {
            if (!fileName.trim().isEmpty()) {
                stories.add(load(fileName.trim(), readResource(fileName.trim())));
            }
        }
        return stories;
    }

    private static List<String> readResource(String name) throws IOException {
        InputStream in = App.class.getResourceAsStream("/stories/" + name);
        if (in == null) {
            throw new IOException("The story file " + name + " is missing from the game");
        }
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                lines.add(line);
            }
        }
        return lines;
    }

    public static Story load(Path file) throws IOException {
        return load(file.getFileName().toString(), Files.readAllLines(file, StandardCharsets.UTF_8));
    }

    private static Story load(String fileName, List<String> lines) {
        Story story = new Story(fileName.substring(0, fileName.length() - ".txt".length()), fileName);

        Scene scene = null;
        boolean paragraphBreak = false;
        for (int i = 0; i < lines.size(); i++) {
            int lineNo = i + 1;
            String line = tidy(lines.get(i)).trim();
            if (line.startsWith("//")) {
                continue;
            }
            if (line.isEmpty()) {
                paragraphBreak = scene != null && !scene.getText().isEmpty();
                continue;
            }

            int colon = line.indexOf(':');
            String keyword = colon > 0 ? line.substring(0, colon).trim().toUpperCase(Locale.ROOT) : "";
            if (KEYWORDS.contains(keyword)) {
                String value = line.substring(colon + 1).trim();
                if (value.isEmpty()) {
                    throw story.error("line " + lineNo + ": '" + keyword + ":' needs something after it");
                }
                switch (keyword) {
                    case "TITLE":
                        story.setTitle(value);
                        break;
                    case "COVER":
                        story.setCover(value);
                        break;
                    case "START":
                        story.setStart(value);
                        break;
                    case "GAMEOVER":
                        story.setGameOverText(value);
                        break;
                    case "CLUE":
                        int equals = value.indexOf('=');
                        if (equals < 0) {
                            throw story.error("line " + lineNo + ": write clues like  CLUE: LACE = White lace from the safe");
                        }
                        story.addClue(value.substring(0, equals).trim(), value.substring(equals + 1).trim(), lineNo);
                        break;
                    case "SCENE":
                        checkName(story, lineNo, value);
                        scene = new Scene(value, lineNo);
                        story.addScene(scene);
                        paragraphBreak = false;
                        break;
                    case "ART":
                        requireScene(story, scene, lineNo, keyword).setArt(value);
                        break;
                    case "ENDING":
                        if (!value.equalsIgnoreCase("win")) {
                            throw story.error("line " + lineNo + ": the only ending type is  ENDING: win");
                        }
                        requireScene(story, scene, lineNo, keyword).setWinning();
                        break;
                }
            } else if (line.startsWith(">")) {
                requireScene(story, scene, lineNo, "a choice").addChoice(parseChoice(story, lineNo, line.substring(1).trim()));
            } else {
                requireScene(story, scene, lineNo, "story text").addText(line, paragraphBreak);
                paragraphBreak = false;
            }
        }

        story.validate();
        return story;
    }

    /** Reads a choice like:  Search the coat -> coat (gives TICKET) */
    private static Choice parseChoice(Story story, int lineNo, String body) {
        String where = "line " + lineNo + ": ";
        int arrow = body.lastIndexOf("->");
        if (arrow < 0) {
            throw story.error(where + "a choice needs '->' and then the scene it leads to");
        }
        String label = body.substring(0, arrow).trim();
        String rest = body.substring(arrow + 2).trim();
        if (label.isEmpty()) {
            throw story.error(where + "this choice has no text before the '->'");
        }

        String options = "";
        int paren = rest.indexOf('(');
        if (paren >= 0) {
            if (!rest.endsWith(")")) {
                throw story.error(where + "missing ')' at the end of the choice");
            }
            options = rest.substring(paren + 1, rest.length() - 1);
            rest = rest.substring(0, paren).trim();
        }
        checkName(story, lineNo, rest);

        Choice choice = new Choice(label, rest);
        List<String> needs = new ArrayList<>();
        String otherwise = null;
        for (String option : options.split(",")) {
            String[] words = option.trim().split("\\s+");
            switch (words[0].toLowerCase(Locale.ROOT)) {
                case "":
                    break;
                case "bad":
                    choice.bad();
                    break;
                case "gives":
                    if (words.length != 2) {
                        throw story.error(where + "write  (gives CLUENAME)  with one clue");
                    }
                    choice.gives(words[1]);
                    break;
                case "needs":
                    if (words.length < 2) {
                        throw story.error(where + "'needs' must list at least one clue");
                    }
                    needs.addAll(Arrays.asList(words).subList(1, words.length));
                    break;
                case "else":
                    if (words.length != 2) {
                        throw story.error(where + "write  else scenename  with one scene");
                    }
                    otherwise = words[1];
                    break;
                default:
                    throw story.error(where + "unknown choice option '" + words[0] + "'. Use bad, gives, needs or else");
            }
        }
        if (!needs.isEmpty() && otherwise == null) {
            throw story.error(where + "'needs' also requires 'else scenename' for when the player lacks the clues");
        }
        if (needs.isEmpty() && otherwise != null) {
            throw story.error(where + "'else' only makes sense together with 'needs'");
        }
        if (!needs.isEmpty()) {
            choice.needs(otherwise, needs.toArray(new String[0]));
        }
        return choice;
    }

    private static Scene requireScene(Story story, Scene scene, int lineNo, String what) {
        if (scene == null) {
            throw story.error("line " + lineNo + ": " + what + " must come after a 'SCENE:' line");
        }
        return scene;
    }

    private static void checkName(Story story, int lineNo, String name) {
        if (name.isEmpty() || name.contains(" ")) {
            throw story.error("line " + lineNo + ": scene names must be one word with no spaces, like 'guestroom'");
        }
    }

    /** Swaps "smart" punctuation from word processors for plain characters the pixel font has. */
    private static String tidy(String line) {
        return line.replace('﻿', ' ')
                .replace('‘', '\'').replace('’', '\'')
                .replace('“', '"').replace('”', '"')
                .replace('–', '-').replace('—', '-')
                .replace("…", "...")
                .replace('\t', ' ');
    }
}
