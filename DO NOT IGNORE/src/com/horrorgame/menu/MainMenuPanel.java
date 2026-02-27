package com.horrorgame.menu;

import com.horrorgame.audio.SoundManager;
import com.horrorgame.core.GameFrame;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

public class MainMenuPanel extends JPanel {

    private Random random = new Random();
    private JLabel title;
    private int glitchOffsetX = 0;
    private int glitchOffsetY = 0;
    private Image backgroundGif;


    public MainMenuPanel(GameFrame frame) {

        //setBackground(Color.BLACK);
        setLayout(new BorderLayout());

        java.net.URL bgUrl = getClass().getResource("/com/horrorgame/assets/images/homescreen.gif");
        if (bgUrl != null) {
            backgroundGif = new ImageIcon(bgUrl).getImage();
        } else {
            backgroundGif = null;
            setBackground(Color.BLACK);
        }

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // 🎵 Start background music
        SoundManager.playLoop("/com/horrorgame/assets/audio/menu_music.WAV");
        // ===== TITLE =====
        title = new JLabel("DO NOT IGNORE", SwingConstants.CENTER);
        title.setForeground(Color.RED);

        int titleSize = screenSize.width / 15;
        title.setFont(new Font("Chiller", Font.BOLD, titleSize));
        title.setBorder(BorderFactory.createEmptyBorder(150, 0, 50, 0));

        add(title, BorderLayout.NORTH);

        // ===== BUTTON PANEL =====
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new GridLayout(2, 1, 0, 40));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(100, 500, 200, 500));

        JButton start = new JButton("Start");
        JButton exit = new JButton("Exit");

        styleButton(start, screenSize);
        styleButton(exit, screenSize);

        buttonPanel.add(start);
        buttonPanel.add(exit);

        add(buttonPanel, BorderLayout.CENTER);

        // ===== ACTIONS =====
        start.addActionListener(e -> frame.showScreen("Difficulty"));
        exit.addActionListener(e -> System.exit(0));

        // ⚡ Glitch + Flicker Timer
        Timer glitchTimer = new Timer(100, e -> {

            float alpha = 0.7f + random.nextFloat() * 0.3f;
            title.setForeground(new Color(1f, 0f, 0f, alpha));

            if (random.nextDouble() > 0.8) {
                glitchOffsetX = random.nextInt(6) - 3;
                glitchOffsetY = random.nextInt(6) - 3;
            } else {
                glitchOffsetX = 0;
                glitchOffsetY = 0;
            }

            repaint();
        });
        glitchTimer.start();

        // 🎛 Noise Refresh
        Timer noiseTimer = new Timer(60, e -> repaint());
        noiseTimer.start();
    }

    private void styleButton(JButton button, Dimension screenSize) {

        int fontSize = screenSize.width / 30;

        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setBackground(new Color(60, 0, 0));
        button.setForeground(Color.RED);
        button.setFont(new Font("Chiller", Font.BOLD, fontSize));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {

            public void mouseEntered(MouseEvent evt) {
                button.setBackground(new Color(120, 0, 0));

                // 🔊 Play hover clock click
                SoundManager.playSound("/com/horrorgame/assets/audio/hover_click.WAV");            }

            public void mouseExited(MouseEvent evt) {
                button.setBackground(new Color(60, 0, 0));
            }
        });
    }

    // 🎨 Paint Noise + Glitch Effect
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        if (backgroundGif != null) {
        g2.drawImage(backgroundGif, 0, 0, getWidth(), getHeight(), this);
        }

        // 🎛 Static Noise
        for (int i = 0; i < 3000; i++) {
            int x = random.nextInt(getWidth());
            int y = random.nextInt(getHeight());
            int alpha = random.nextInt(40);
            g2.setColor(new Color(255, 255, 255, alpha));
            g2.fillRect(x, y, 1, 1);
        }

        // ⚡ Glitch Draw
        if (glitchOffsetX != 0 || glitchOffsetY != 0) {
            g2.setFont(title.getFont());
            g2.setColor(new Color(255, 0, 0, 120));
            FontMetrics fm = g2.getFontMetrics();

            String text = title.getText();
            int textWidth = fm.stringWidth(text);
            int x = (getWidth() - textWidth) / 2 + glitchOffsetX;
            int y = 200 + glitchOffsetY;

            g2.drawString(text, x, y);
        }
    }
}
