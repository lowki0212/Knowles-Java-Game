package com.horrorgame.core;

import com.horrorgame.game.GamePanel;
import com.horrorgame.menu.DifficultyPanel;
import com.horrorgame.menu.MainMenuPanel;
import com.horrorgame.menu.InstructionsPanel;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class GameFrame extends JFrame {

    private final CardLayout layout;
    private final JPanel container;
    private GamePanel gamePanel;
    private InstructionsPanel instructionsPanel;
    private Difficulty currentDifficulty = Difficulty.MEDIUM;

    public GameFrame() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setUndecorated(true);

        layout = new CardLayout();
        container = new JPanel(layout);

        MainMenuPanel mainMenu = new MainMenuPanel(this);
        DifficultyPanel difficultyPanel = new DifficultyPanel(this);

        container.add(mainMenu, "MainMenu");
        container.add(difficultyPanel, "Difficulty");

        add(container);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize);
        setLocationRelativeTo(null);
        setExtendedState(MAXIMIZED_BOTH);
        setVisible(true);
    }

    public void showScreen(String name) {
        layout.show(container, name);
    }

    public void showInstructions(Difficulty difficulty) {
        currentDifficulty = difficulty;
        if (instructionsPanel != null) {
            container.remove(instructionsPanel);
        }
        instructionsPanel = new InstructionsPanel(this, currentDifficulty);
        container.add(instructionsPanel, "Instructions");
        layout.show(container, "Instructions");
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

