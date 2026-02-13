package com.horrorgame.menu;

import com.horrorgame.audio.SoundManager;
import com.horrorgame.core.GameFrame;
import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;

public class DifficultyPanel extends JPanel {

    public DifficultyPanel(GameFrame frame) {
        setBackground(Color.BLACK);
        setLayout(new BorderLayout());

        // ===== TITLE =====
        JLabel title = new JLabel("Select Difficulty", SwingConstants.CENTER);
        title.setForeground(Color.RED);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int titleSize = screenSize.width / 18;

        title.setFont(new Font("Chiller", Font.BOLD, titleSize));
        title.setBorder(BorderFactory.createEmptyBorder(100, 0, 50, 0));

        add(title, BorderLayout.NORTH);

        // ===== BUTTON PANEL =====
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.setLayout(new GridLayout(3, 1, 0, 30));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(50, 400, 200, 400));

        JButton easy   = new JButton("Easy");
        JButton medium = new JButton("Medium");
        JButton hard   = new JButton("Hard");

        styleButton(easy, screenSize);
        styleButton(medium, screenSize);
        styleButton(hard, screenSize);

        buttonPanel.add(easy);
        buttonPanel.add(medium);
        buttonPanel.add(hard);

        add(buttonPanel, BorderLayout.CENTER);

        // ===== SHARED BUTTON ACTION (no sound here – only hover has sound) =====
        ActionListener startGame = e -> {
            SoundManager.stopLoop();           // stop menu music
            frame.showScreen("Game");
        };

        easy.addActionListener(startGame);
        medium.addActionListener(startGame);
        hard.addActionListener(startGame);
    }

    private void styleButton(JButton button, Dimension screenSize) {
        int fontSize = screenSize.width / 30;

        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setBackground(new Color(60, 0, 0));
        button.setForeground(Color.RED);
        button.setFont(new Font("Chiller", Font.BOLD, fontSize));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(120, 0, 0));
                // Play the same hover sound as in main menu
                SoundManager.playSound("/com/horrorgame/assets/audio/hover_click.wav");
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(60, 0, 0));
            }
        });
    }
}