package com.horrorgame.core;

import javax.swing.*;
import java.awt.*;

import com.horrorgame.menu.MainMenuPanel;
import com.horrorgame.menu.DifficultyPanel;
import com.horrorgame.game.GamePanel;

public class GameFrame extends JFrame {

    private CardLayout layout;
    private JPanel container;

    public GameFrame() {

        setUndecorated(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        // Get screen device
        GraphicsDevice device = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();

        layout = new CardLayout();
        container = new JPanel(layout);

        // Create panels
        MainMenuPanel mainMenu = new MainMenuPanel(this);
        DifficultyPanel difficultyPanel = new DifficultyPanel(this);
        GamePanel gamePanel = new GamePanel(this);

        container.add(mainMenu, "MainMenu");
        container.add(difficultyPanel, "Difficulty");
        container.add(gamePanel, "Game");

        add(container);

        // Set true fullscreen
        device.setFullScreenWindow(this);

        setVisible(true);
    }

    public void showScreen(String name) {
        layout.show(container, name);
    }
}
