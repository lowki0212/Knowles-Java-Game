package com.horrorgame.fx.logic;

/**
 * Encapsulates threat-level rules and maps threat into UI styling.
 */
public class ThreatManager {

    public static final int MIN = 0;
    public static final int MAX = 100;

    public int clamp(int value) {
        return Math.max(MIN, Math.min(MAX, value));
    }

    public ThreatViewModel toViewModel(int threatLevel) {
        String text;
        String css;
        if (threatLevel < 25) {
            text = "THREAT: LOW";
            css = "-fx-text-fill: #88ff88; -fx-font-size: 18px; -fx-font-family: Arial; -fx-font-weight: bold;";
        } else if (threatLevel < 50) {
            text = "THREAT: UNSTABLE";
            css = "-fx-text-fill: #ffff88; -fx-font-size: 18px; -fx-font-family: Arial; -fx-font-weight: bold;";
        } else if (threatLevel < 75) {
            text = "THREAT: HIGH";
            css = "-fx-text-fill: #ffaa44; -fx-font-size: 18px; -fx-font-family: Arial; -fx-font-weight: bold;";
        } else {
            text = "THREAT: CRITICAL";
            css = "-fx-text-fill: #ff5555; -fx-font-size: 18px; -fx-font-family: Arial; -fx-font-weight: bold;";
        }
        return new ThreatViewModel(text, css);
    }

    public record ThreatViewModel(String labelText, String css) {}
}

