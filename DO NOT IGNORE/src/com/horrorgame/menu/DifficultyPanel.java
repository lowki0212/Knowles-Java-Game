package com.horrorgame.menu;

import com.horrorgame.audio.SoundManager;
import com.horrorgame.core.Difficulty;
import com.horrorgame.core.GameFrame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Graphics;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class DifficultyPanel extends JPanel {

    private Image backgroundGif;

    public DifficultyPanel(GameFrame frame) {
        setBackground(Color.BLACK);
        setLayout(new BorderLayout());

        java.net.URL bgUrl = getClass().getResource("/com/horrorgame/assets/images/difficultyscreen.gif");
        if (bgUrl != null) {
            backgroundGif = new javax.swing.ImageIcon(bgUrl).getImage();
        } else {
            backgroundGif = null;
        }

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int titleSize = screenSize.width / 18;

        JLabel title = new JLabel("Select Difficulty", SwingConstants.CENTER);
        title.setForeground(Color.RED);
        title.setFont(new Font("Chiller", Font.BOLD, titleSize));
        title.setBorder(BorderFactory.createEmptyBorder(100, 0, 50, 0));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JButton backButton = new JButton("Back");
        int backFontSize = screenSize.width / 60;
        backButton.setFont(new Font("Chiller", Font.BOLD, backFontSize));
        backButton.setBackground(new Color(30, 0, 0));
        backButton.setForeground(Color.RED);
        backButton.setFocusPainted(false);
        backButton.setBorderPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> frame.showScreen("MainMenu"));

        topPanel.add(backButton, BorderLayout.WEST);
        topPanel.add(title, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.setLayout(new GridLayout(3, 1, 0, 30));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(50, 400, 200, 400));

        JButton easy = new JButton("Easy");
        JButton medium = new JButton("Medium");
        JButton hard = new JButton("Hard");

        styleButton(easy, screenSize);
        styleButton(medium, screenSize);
        styleButton(hard, screenSize);

        buttonPanel.add(easy);
        buttonPanel.add(medium);
        buttonPanel.add(hard);

        add(buttonPanel, BorderLayout.CENTER);

        easy.addActionListener(e -> {
            SoundManager.stopLoop();
            frame.showInstructions(Difficulty.EASY);
        });

        medium.addActionListener(e -> {
            SoundManager.stopLoop();
            frame.showInstructions(Difficulty.MEDIUM);
        });

        hard.addActionListener(e -> {
            SoundManager.stopLoop();
            frame.showInstructions(Difficulty.HARD);
        });
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
                SoundManager.playSound("/com/horrorgame/assets/audio/hover_click.WAV");
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(60, 0, 0));
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundGif != null) {
            g.drawImage(backgroundGif, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
