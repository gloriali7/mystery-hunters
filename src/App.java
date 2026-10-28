import java.awt.Toolkit;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/** Starts the game: loads the stories, opens a window and puts the game panel inside it. */
public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            List<Story> stories;
            try {
                stories = StoryLoader.loadAll();
            } catch (Exception e) {
                // Usually a typo in a story file. Show exactly where, instead of crashing mysteriously.
                String message = e.getMessage() != null ? e.getMessage() : e.toString();
                System.err.println(message);
                JOptionPane.showMessageDialog(null, message, "Mystery Hunters: problem in a story file", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
                return;
            }
            SaveData save = new SaveData();
            Sound sound = new Sound(save.isSoundOn());

            // Pick the biggest whole-number zoom that fits the screen, so pixels stay square.
            int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
            int scale = Math.max(1, Math.min(4, (screenHeight - 120) / GamePanel.H));

            JFrame frame = new JFrame("Mystery Hunters");
            GamePanel panel = new GamePanel(scale, stories, save, sound);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            panel.requestFocusInWindow();
        });
    }
}
