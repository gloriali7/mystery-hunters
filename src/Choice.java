/**
 * One option the player can pick in a scene. In a story file it looks like:
 *   > Search the coat -> coat (gives TICKET)
 *   > Arrest the butler! -> wrong (bad)
 *   > Accuse Lily -> win (needs LACE TICKET, else noproof)
 */
public class Choice {
    private final String label;
    private final String next;
    private boolean bad;
    private String gives;
    private String[] needs = new String[0];
    private String otherwise;

    public Choice(String label, String next) {
        this.label = label;
        this.next = next;
    }

    /** Picking this costs the player a heart. */
    public Choice bad() {
        bad = true;
        return this;
    }

    /** Picking this hands the player a clue. The choice hides itself once the clue is found. */
    public Choice gives(String clue) {
        gives = clue;
        return this;
    }

    /**
     * Only works if the player has every listed clue. Without them, the player
     * goes to the "otherwise" scene instead and loses a heart.
     */
    public Choice needs(String otherwise, String... clues) {
        this.otherwise = otherwise;
        this.needs = clues;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public String getNext() {
        return next;
    }

    public boolean isBad() {
        return bad;
    }

    public String getGives() {
        return gives;
    }

    public String[] getNeeds() {
        return needs;
    }

    public String getOtherwise() {
        return otherwise;
    }
}
