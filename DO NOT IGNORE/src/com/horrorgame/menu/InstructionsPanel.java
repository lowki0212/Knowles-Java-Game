package com.horrorgame.menu;

import com.horrorgame.core.Difficulty;
import com.horrorgame.core.GameFrame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Graphics;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class InstructionsPanel extends JPanel {

    private final GameFrame frame;
    private final Difficulty difficulty;
    private java.awt.Image backgroundGif;

    public InstructionsPanel(GameFrame frame, Difficulty difficulty) {
        this.frame = frame;
        this.difficulty = difficulty;
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        java.net.URL bgUrl = getClass().getResource("/com/horrorgame/assets/images/instructions.gif");
        if (bgUrl != null) {
            backgroundGif = new javax.swing.ImageIcon(bgUrl).getImage();
        }

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        JLabel title = new JLabel("Instructions", SwingConstants.CENTER);
        title.setForeground(Color.RED);
        int titleSize = screenSize.width / 20;
        title.setFont(new Font("Chiller", Font.BOLD, titleSize));
        title.setBorder(BorderFactory.createEmptyBorder(60, 0, 20, 0));
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JButton backButton = new JButton("Back");
        int headerFontSize = screenSize.width / 60;
        backButton.setFont(new Font("Chiller", Font.BOLD, headerFontSize));
        backButton.setBackground(new Color(30, 0, 0));
        backButton.setForeground(Color.RED);
        backButton.setFocusPainted(false);
        backButton.setBorderPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> frame.showScreen("Difficulty"));

        topPanel.add(backButton, BorderLayout.WEST);
        topPanel.add(title, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        add(contentPanel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 40, 5, 40);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel introLabel = createTextLabel(
                "Before your shift begins, learn how to classify anomalies:");
        contentPanel.add(introLabel, gbc);
        gbc.gridy++;

        contentPanel.add(createTextLabel("• Missing Object – something that was there is gone."), gbc);
        gbc.gridy++;
        contentPanel.add(createTextLabel("• Object Displacement – an object has moved from its original position."), gbc);
        gbc.gridy++;
        contentPanel.add(createTextLabel("• Shadowy Figure – a shadowy entity appears in the background."), gbc);
        gbc.gridy++;
        contentPanel.add(createTextLabel("• Intruder – a humanoid entity that can harm if not reported."), gbc);
        gbc.gridy++;
        contentPanel.add(createTextLabel("• Strange Imagery – any image that should not exist in the room."), gbc);
        gbc.gridy++;
        contentPanel.add(createTextLabel("• Demonic – demonic presence, often marked by something red."), gbc);
        gbc.gridy++;
        contentPanel.add(createTextLabel("• Extra Object – a new object that was not there before."), gbc);
        gbc.gridy++;
        contentPanel.add(createTextLabel("• Audio Disturbance – any audio that is not normal."), gbc);
        gbc.gridy++;

        gbc.insets = new Insets(20, 40, 5, 40);
        JLabel noteTitle = createTextLabel("NOTE:");
        noteTitle.setForeground(new Color(255, 120, 120));
        contentPanel.add(noteTitle, gbc);
        gbc.gridy++;

        gbc.insets = new Insets(5, 40, 5, 40);
        contentPanel.add(createTextLabel("MEMORIZE EVERY ROOM. Anomalies start appearing at 12:30 AM."), gbc);
        gbc.gridy++;

        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 40, 0));
        add(bottomPanel, BorderLayout.SOUTH);

        JButton startButton = new JButton("Start");
        int fontSize = screenSize.width / 40;
        startButton.setFont(new Font("Chiller", Font.BOLD, fontSize));
        startButton.setBackground(new Color(60, 0, 0));
        startButton.setForeground(Color.RED);
        startButton.setFocusPainted(false);
        startButton.setBorderPainted(false);
        startButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        startButton.addActionListener(e -> frame.startGame(this.difficulty));

        bottomPanel.add(startButton);
    }

    private JLabel createTextLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.PLAIN, 18));
        return label;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundGif != null) {
            g.drawImage(backgroundGif, 0, 0, getWidth(), getHeight(), this);
        }
    }
}

