package com.horrorgame.core;

import com.horrorgame.game.GamePanel;
import com.horrorgame.menu.DifficultyPanel;
import com.horrorgame.menu.MainMenuPanel;
import java.awt.CardLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class GameFrame extends JFrame {

    private final CardLayout layout;
    private final JPanel container;
    private GamePanel gamePanel;
    private Difficulty currentDifficulty = Difficulty.MEDIUM;

    public GameFrame() {
        setUndecorated(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        GraphicsDevice device = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();

        layout = new CardLayout();
        container = new JPanel(layout);

        MainMenuPanel mainMenu = new MainMenuPanel(this);
        DifficultyPanel difficultyPanel = new DifficultyPanel(this);

        container.add(mainMenu, "MainMenu");
        container.add(difficultyPanel, "Difficulty");

        add(container);

        device.setFullScreenWindow(this);
        setVisible(true);
    }

    public void showScreen(String name) {
        layout.show(container, name);
    }

    public void startGame(Difficulty difficulty) {
        currentDifficulty = difficulty;

        if (gamePanel != null) {
            container.remove(gamePanel);
        }

        gamePanel = new GamePanel(this, currentDifficulty);
        container.add(gamePanel, "Game");
        layout.show(container, "Game");
    }

    public Difficulty getCurrentDifficulty() {
        return currentDifficulty;
    }
}

