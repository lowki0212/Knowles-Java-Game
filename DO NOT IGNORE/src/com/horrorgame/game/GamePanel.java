package com.horrorgame.game;

import com.horrorgame.core.GameFrame;
import java.awt.*;
import javax.swing.*;

public class GamePanel extends JPanel {

    private JLabel label;

    public GamePanel(GameFrame frame) {

        setBackground(Color.BLACK);
        setLayout(new GridBagLayout()); // Better centering

        label = new JLabel("GAME STARTED");
        label.setForeground(Color.RED);

        // Make text scale depending on screen resolution
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int fontSize = screenSize.width / 20; // Adjust scaling factor if needed

        label.setFont(new Font("Chiller", Font.BOLD, fontSize));

        add(label);
    }

    // Optional: if you want to later draw game graphics manually
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Example: subtle horror overlay effect
        Graphics2D g2d = (Graphics2D) g;

        // Dark red transparent overlay (optional horror vibe)
        g2d.setColor(new Color(50, 0, 0, 40));
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}
