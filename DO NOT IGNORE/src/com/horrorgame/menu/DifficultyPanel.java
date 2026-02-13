package com.horrorgame.menu;

import com.horrorgame.audio.SoundManager;
import com.horrorgame.core.GameFrame;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;

public class DifficultyPanel extends JPanel {

    private Image difficultyBackgroundGif;

    public DifficultyPanel(GameFrame frame) {

        setLayout(new BorderLayout());
        setOpaque(false);

        // 🎥 Load Difficulty Background GIF
        difficultyBackgroundGif = new ImageIcon(
                getClass().getResource("/com/horrorgame/assets/images/difficultyscreen.gif")
        ).getImage();

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
        buttonPanel.setOpaque(false);
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

        // ===== BUTTON ACTION =====
        ActionListener startGame = e -> {
            SoundManager.stopLoop();
            frame.showScreen("Game");
        };

        easy.addActionListener(startGame);
        medium.addActionListener(startGame);
        hard.addActionListener(startGame);

        // 🎬 Force repaint for smooth GIF animation
        new Timer(40, e -> repaint()).start();
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
                SoundManager.playSound("/com/horrorgame/assets/audio/hover_click.wav");
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

        Graphics2D g2 = (Graphics2D) g;

        // 🎥 Draw background GIF full screen
        if (difficultyBackgroundGif != null) {
            g2.drawImage(difficultyBackgroundGif, 0, 0, getWidth(), getHeight(), this);
        }

        // 🌑 Dark overlay for horror effect
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRect(0, 0, getWidth(), getHeight());
    }
}
