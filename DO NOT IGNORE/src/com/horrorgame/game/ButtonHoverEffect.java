package com.horrorgame.game;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

/**
 * Utility to apply consistent hover effects to buttons.
 */
public final class ButtonHoverEffect {

    private ButtonHoverEffect() {
    }

    public static void applyHoverEffect(
            JButton button,
            Color normalBackground,
            Color hoverBackground,
            Color normalBorderColor,
            Color hoverBorderColor
    ) {
        Border originalBorder = button.getBorder();
        int borderThickness = 2;

        if (originalBorder instanceof LineBorder) {
            borderThickness = ((LineBorder) originalBorder).getThickness();
        }

        Border normalBorder = BorderFactory.createLineBorder(normalBorderColor, borderThickness);
        Border hoverBorder = BorderFactory.createLineBorder(hoverBorderColor, borderThickness);

        button.setBackground(normalBackground);
        button.setBorder(normalBorder);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverBackground);
                button.setBorder(hoverBorder);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(normalBackground);
                button.setBorder(normalBorder);
            }
        });
    }
}

