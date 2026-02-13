package com.horrorgame.game;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/**
 * Displays short feedback messages in the center of the game screen.
 */
public class FeedbackLabel extends JLabel {

    private Timer hideTimer;

    public FeedbackLabel() {
        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);
        setVisible(false);
        setOpaque(true);
    }

    public void showFeedback(String text, Color color, int durationMs) {
        setText(text);
        setForeground(color);
        setBackground(new Color(0, 0, 0, 220));
        setBorder(BorderFactory.createLineBorder(color, 2));
        setFont(new Font("Arial", Font.BOLD, 28));
        setVisible(true);

        if (hideTimer != null) {
            hideTimer.stop();
        }

        hideTimer = new Timer(durationMs, e -> setVisible(false));
        hideTimer.setRepeats(false);
        hideTimer.start();
    }
}

