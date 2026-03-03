package com.horrorgame.fx.ui;

import com.horrorgame.fx.logic.ThreatManager;

/**
 * View interface used by {@code GameController}.
 * Implemented by {@code GameSceneController}.
 */
public interface GameView {
    void setTimeText(String text);
    void setRoomText(String text);
    void setThreat(ThreatManager.ThreatViewModel vm, double progress);

    void playLoopingMedia(String resourcePath);
    void playOnceMedia(String resourcePath, Runnable onFinished);
    void stopMedia();
    void pauseMedia();
    void resumeMedia();

    void setReportButtonDisabled(boolean disabled);
    void setReportCooldownText(String text);

    void showReportOverlay();
    void hideReportOverlay();
    void setReportStatus(String text, String css);
    void setReportTypeButtonsEnabled(boolean enabled);

    void showPauseOverlay();
    void hidePauseOverlay();

    void showGameOverOverlay(boolean win);
    void hideGameOverOverlay();
}

