package com.horrorgame.fx.logic;

import com.horrorgame.fx.RoomState;
import com.horrorgame.game.RoomMediaLibrary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Owns per-room anomaly state and implements anomaly spawning/aging rules.
 * UI-free: it only mutates {@link RoomState} and reports outcomes to callers.
 */
public class AnomalyManager {

    private final RoomMediaLibrary mediaLibrary;
    private final List<String> roomKeys;
    private final Map<String, RoomState> roomStates = new HashMap<>();
    private final Random random = new Random();

    public AnomalyManager(RoomMediaLibrary mediaLibrary, List<String> roomKeys) {
        this.mediaLibrary = mediaLibrary;
        this.roomKeys = roomKeys;
        for (String key : roomKeys) {
            roomStates.put(key, new RoomState());
        }
    }

    public Map<String, RoomState> getRoomStates() {
        return roomStates;
    }

    public RoomState getState(String roomKey) {
        return roomStates.get(roomKey);
    }

    public void reset() {
        for (String key : roomKeys) {
            RoomState s = roomStates.get(key);
            if (s != null) {
                s.hasAnomaly = false;
                s.anomalyType = null;
                s.anomalySecondsAlive = 0;
                s.penaltyApplied = false;
            }
        }
    }

    public int tickExistingAnomalies(int delaySeconds, int currentRoomIndex) {
        int misses = 0;
        String currentKey = roomKeys.isEmpty() ? null : roomKeys.get(currentRoomIndex);
        for (String key : roomKeys) {
            RoomState state = roomStates.get(key);
            if (state != null && state.hasAnomaly && !state.penaltyApplied) {
                state.anomalySecondsAlive++;
                if (state.anomalySecondsAlive >= delaySeconds) {
                    misses++;
                    state.penaltyApplied = true;
                    state.hasAnomaly = false;
                    state.anomalyType = null;
                    state.anomalySecondsAlive = 0;
                    // Caller decides whether to refresh current room display.
                }
            }
        }
        return misses;
    }

    /**
     * Spawns a new anomaly in a non-current room that doesn't already have an anomaly.
     * Returns the index of the room that received an anomaly, or -1 if none.
     */
    public int spawnAnomalyExcludingCurrent(int currentRoomIndex) {
        if (roomKeys.isEmpty()) {
            return -1;
        }
        int chosenIndex = -1;
        for (int attempts = 0; attempts < roomKeys.size(); attempts++) {
            int candidate = random.nextInt(roomKeys.size());
            if (candidate == currentRoomIndex) {
                continue;
            }
            String candidateKey = roomKeys.get(candidate);
            RoomState candidateState = roomStates.get(candidateKey);
            if (candidateState == null || !candidateState.hasAnomaly) {
                chosenIndex = candidate;
                break;
            }
        }
        if (chosenIndex < 0) {
            return -1;
        }

        String roomKey = roomKeys.get(chosenIndex);
        RoomState state = roomStates.computeIfAbsent(roomKey, k -> new RoomState());
        if (state.hasAnomaly) {
            return -1;
        }

        List<RoomMediaLibrary.AnomalyType> available = mediaLibrary.getAvailableAnomalyTypes(roomKey);
        if (available.isEmpty()) {
            return -1;
        }
        RoomMediaLibrary.AnomalyType type = available.get(random.nextInt(available.size()));
        state.hasAnomaly = true;
        state.anomalyType = type;
        state.anomalySecondsAlive = 0;
        state.penaltyApplied = false;
        return chosenIndex;
    }
}

