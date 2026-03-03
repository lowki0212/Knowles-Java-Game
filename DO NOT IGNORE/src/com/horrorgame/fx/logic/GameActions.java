package com.horrorgame.fx.logic;

/**
 * Actions invoked by the JavaFX game UI.
 * Implemented by {@link GameController} to keep UI wiring decoupled from logic.
 */
public interface GameActions {
    void onPrevRoom();
    void onNextRoom();
    void onPauseRequested();
    void onReportRequested();

    void onReportTypeSelected(String label);
    void onReportCancelled();

    void onResumeFromPause();
    void onReturnToMainMenuFromPause();

    void onReturnToMainMenuFromGameOver();
    void onPlayAgainFromGameOver();
}

