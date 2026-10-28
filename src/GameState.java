import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Everything about the current playthrough: where you are, hearts left, and clues found. */
public class GameState {
    public static final int MAX_HEARTS = 3;

    private final Set<String> clues = new HashSet<>();
    private int hearts;
    private String sceneId;

    public void reset(String startScene) {
        hearts = MAX_HEARTS;
        clues.clear();
        sceneId = startScene;
    }

    /** Puts back a saved game. */
    public void restore(String savedScene, int savedHearts, Collection<String> savedClues) {
        sceneId = savedScene;
        hearts = savedHearts;
        clues.clear();
        clues.addAll(savedClues);
    }

    /** Applies a choice the player made. Returns true if it was a mistake (and cost a heart). */
    public boolean apply(Choice choice) {
        boolean hasEvidence = true;
        for (String clue : choice.getNeeds()) {
            if (!clues.contains(clue)) {
                hasEvidence = false;
            }
        }

        boolean mistake = choice.isBad() || !hasEvidence;
        if (mistake) {
            hearts--;
        }
        if (hasEvidence && choice.getGives() != null) {
            clues.add(choice.getGives());
        }
        sceneId = hasEvidence ? choice.getNext() : choice.getOtherwise();
        return mistake;
    }

    /** The choices to show for a scene, hiding ones that would give a clue the player already has. */
    public List<Choice> visibleChoices(Scene scene) {
        List<Choice> visible = new ArrayList<>();
        for (Choice choice : scene.getChoices()) {
            if (choice.getGives() == null || !clues.contains(choice.getGives())) {
                visible.add(choice);
            }
        }
        return visible;
    }

    public boolean hasClue(String clue) {
        return clues.contains(clue);
    }

    public Set<String> getClues() {
        return Collections.unmodifiableSet(clues);
    }

    public int clueCount() {
        return clues.size();
    }

    public int getHearts() {
        return hearts;
    }

    public String getSceneId() {
        return sceneId;
    }
}
