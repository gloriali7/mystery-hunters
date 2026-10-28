import java.util.ArrayList;
import java.util.List;

/** One moment in the story: a picture, some text, and the choices the player can make. */
public class Scene {
    private final String id;
    private final int line;
    private final List<Choice> choices = new ArrayList<>();
    private final StringBuilder text = new StringBuilder();
    private String art;
    private boolean winning;

    /** line is where the scene starts in its story file, for error messages. */
    public Scene(String id, int line) {
        this.id = id;
        this.line = line;
    }

    public void setArt(String art) {
        this.art = art;
    }

    /** Adds a line of story text. A paragraph break starts the text on a new line. */
    public void addText(String more, boolean newParagraph) {
        if (text.length() > 0) {
            text.append(newParagraph ? "\n" : " ");
        }
        text.append(more);
    }

    public void addChoice(Choice choice) {
        choices.add(choice);
    }

    /** Marks this scene as the ending where the player solves the case. */
    public void setWinning() {
        winning = true;
    }

    public String getId() {
        return id;
    }

    public int getLine() {
        return line;
    }

    public String getArt() {
        return art;
    }

    public String getText() {
        return text.toString();
    }

    public List<Choice> getChoices() {
        return choices;
    }

    public boolean isWinning() {
        return winning;
    }
}
