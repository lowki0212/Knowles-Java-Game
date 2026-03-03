package com.horrorgame.fx;

import com.horrorgame.game.RoomMediaLibrary;

/**
 * Per-room anomaly state for the JavaFX game.
 */
public class RoomState {
    public boolean hasAnomaly;
    public RoomMediaLibrary.AnomalyType anomalyType;
    public int anomalySecondsAlive;
    public boolean penaltyApplied;
}

