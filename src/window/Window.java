package window;

import javax.swing.*;
import java.awt.*;

public class Window {
    public Window(Dimension size, String title, Game game) {
        JFrame frame = new JFrame(title);
        frame.setPreferredSize(size);
        frame.setMaximumSize(size);
        frame.setMinimumSize(size);

        frame.add(game);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}