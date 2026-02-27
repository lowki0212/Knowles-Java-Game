package com.horrorgame.game;

import com.horrorgame.core.Difficulty;
import com.horrorgame.core.GameFrame;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class GamePanel extends JPanel {

    private enum ReportResultType {
        NO_ANOMALY,
        WRONG_TYPE
    }

    private final GameFrame frame;
    private final Difficulty difficulty;
    private final Random random = new Random();

    // Cameras
    private int currentCamera = 0;
    private final List<CameraData> cameras = new ArrayList<>();

    // Game state
    private boolean paused = false;
    private boolean reporting = false;
    private int gameMinutesTotal = 0;
    private Timer gameTimer;
    private Timer anomalySpawnTimer;
    private Timer criticalThreatTimer;
    private int criticalThreatSeconds = 0;
    private boolean isGameOverSequence = false;

    // Threat / reporting
    private int threatLevel = 0; // 0–100 scale
    private boolean reportOnCooldown = false;
    private Timer reportCooldownTimer;
    private Timer reportingLabelTimer;
    private ReportResultType pendingReportResult;

    // UI
    // NOTE: Temporarily disabled JavaFX video background to avoid white-screen / overlay issues.
    // private VideoBackgroundPanel videoBackgroundPanel;
    private final RoomMediaLibrary mediaLibrary = RoomMediaLibrary.load(GamePanel.class);
    private JLabel timeLabel;
    private JLabel threatLabel;
    private JLabel reportingLabel;
    private JLabel jumpscareLabel;
    private JButton leftBtn;
    private JButton rightBtn;
    private JButton reportBtn;
    private JButton pauseBtn;
    private JPanel reportOverlay;
    private JPanel pauseOverlay;
    private JPanel gameOverOverlay;
    private FeedbackLabel feedbackLabel;
    private int secondsOnCurrentCamera = 0;

    private static final int REPORT_SUCCESS_DELAY_MS = 1500;
    private static final int CRITICAL_THREAT_SECONDS_TO_FAIL = 10;

    private static class CameraData {
        boolean hasAnomaly;
        int anomalyType; // 0 = none
        final String roomKey;
        final String roomPrefix;
        final String normalMediaPath;
        String anomalyMediaPath;

        CameraData(String roomKey, String roomPrefix, String normalMediaPath, String anomalyMediaPath) {
            this.hasAnomaly = false;
            this.anomalyType = 0;
            this.roomKey = roomKey;
            this.roomPrefix = roomPrefix;
            this.normalMediaPath = normalMediaPath;
            this.anomalyMediaPath = anomalyMediaPath;
        }
    }

    private static final String[] ANOMALY_TYPES = {
            "Missing Object",
            "Object Displacement",
            "Shadowy Figure",
            "Intruder",
            "Strange Imagery",
            "Demonic",
            "Extra Object",
            "Audio Disturbance"
    };

    private static final String JUMPSCARE_GIF_PATH = "/com/horrorgame/assets/gif/jumpscare.gif";

    public GamePanel(GameFrame frame, Difficulty difficulty) {
        this.frame = frame;
        this.difficulty = difficulty;
        setLayout(null);
        setBackground(new Color(5, 5, 18));

        initCameras();
        createUIComponents();
        createReportOverlay();
        createPauseOverlay();
        startGameTimer();
        startAnomalySpawner();

        feedbackLabel = new FeedbackLabel();
        add(feedbackLabel);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (getWidth() > 100 && getHeight() > 100) {
                    layoutUI();
                    if (reportOverlay != null) {
                        reportOverlay.setBounds(0, 0, getWidth(), getHeight());
                    }
                    if (pauseOverlay != null) {
                        pauseOverlay.setBounds(0, 0, getWidth(), getHeight());
                    }
                    repaint();
                }
            }
        });

        SwingUtilities.invokeLater(this::layoutUI);
        randomizeStartingCamera();
        updateCameraView();
    }

    private void initCameras() {
        String[] roomKeys = {"garage", "kitchen", "living_room", "stairs", "basement", "bedroom"};
        String[] roomPrefixes = {"Garage", "Kitchen", "LivingRoom", "Stairs", "Basement", "Bedroom"};
        for (int i = 0; i < roomKeys.length; i++) {
            String roomKey = roomKeys[i];
            String roomPrefix = roomPrefixes[i];
            String normalMediaPath = mediaLibrary.getRandomNormal(roomKey);
            cameras.add(new CameraData(roomKey, roomPrefix, normalMediaPath, null));
        }
    }

    private void randomizeStartingCamera() {
        if (cameras.isEmpty()) {
            currentCamera = 0;
            return;
        }
        currentCamera = random.nextInt(cameras.size());
    }

    private RoomMediaLibrary.AnomalyType getAnomalyTypeFromIndex(int index) {
        switch (index) {
            case 1:
                return RoomMediaLibrary.AnomalyType.MISSING_OBJECT;
            case 2:
                return RoomMediaLibrary.AnomalyType.OBJECT_DISPLACEMENT;
            case 3:
                return RoomMediaLibrary.AnomalyType.SHADOWY_FIGURE;
            case 4:
                return RoomMediaLibrary.AnomalyType.INTRUDER;
            case 5:
                return RoomMediaLibrary.AnomalyType.STRANGE_IMAGERY;
            case 6:
                return RoomMediaLibrary.AnomalyType.DEMONIC;
            case 7:
                return RoomMediaLibrary.AnomalyType.EXTRA_OBJECT;
            case 8:
                return RoomMediaLibrary.AnomalyType.AUDIO_DISTURBANCE;
            default:
                return null;
        }
    }

    private void spawnRandomAnomaly() {
        List<Integer> availableCameras = new ArrayList<>();
        for (int i = 0; i < cameras.size(); i++) {
            CameraData cam = cameras.get(i);
            if (!cam.hasAnomaly && i != currentCamera) {
                availableCameras.add(i);
            }
        }
        if (availableCameras.isEmpty()) {
            return;
        }
        int cameraIndex = availableCameras.get(random.nextInt(availableCameras.size()));
        CameraData data = cameras.get(cameraIndex);

        int[] shuffledTypeIndices = buildShuffledTypeIndices();
        for (int typeIndex : shuffledTypeIndices) {
            int type = typeIndex + 1;
            String anomalyPath = loadAnomalyMediaPathForCamera(cameraIndex, type);
            if (anomalyPath == null) {
                continue;
            }
            data.hasAnomaly = true;
            data.anomalyType = type;
            data.anomalyMediaPath = anomalyPath;
            break;
        }

        if (data.hasAnomaly && cameraIndex == currentCamera) {
            updateCameraView();
        }
    }

    private int[] buildShuffledTypeIndices() {
        int[] indices = new int[ANOMALY_TYPES.length];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }
        for (int i = indices.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = indices[i];
            indices[i] = indices[j];
            indices[j] = tmp;
        }
        return indices;
    }

    private String loadAnomalyMediaPathForCamera(int cameraIndex, int anomalyType) {
        if (cameraIndex < 0 || cameraIndex >= cameras.size()) {
            return null;
        }
        CameraData data = cameras.get(cameraIndex);
        RoomMediaLibrary.AnomalyType type = getAnomalyTypeFromIndex(anomalyType);
        if (type == null) {
            return null;
        }
        return mediaLibrary.getRandomAnomaly(data.roomKey, type);
    }

    private void createUIComponents() {
        timeLabel = new JLabel("12:00 AM", SwingConstants.LEFT);
        timeLabel.setForeground(new Color(220, 40, 40));
        timeLabel.setFont(new Font("Arial", Font.BOLD, 36));
        add(timeLabel);

        threatLabel = new JLabel("", SwingConstants.LEFT);
        threatLabel.setForeground(new Color(160, 160, 160));
        threatLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(threatLabel);
        updateThreatLabel();

        reportingLabel = new JLabel("REPORTING...", SwingConstants.CENTER);
        reportingLabel.setForeground(new Color(255, 220, 120));
        reportingLabel.setFont(new Font("Arial", Font.BOLD, 28));
        reportingLabel.setVisible(false);
        add(reportingLabel);

        leftBtn = createNavButton("<", e -> prevCamera());
        rightBtn = createNavButton(">", e -> nextCamera());
        reportBtn = createMainButton("REPORT ANOMALY", e -> showReportOverlay());
        pauseBtn = createMainButton("PAUSE", e -> togglePause());

        add(leftBtn);
        add(rightBtn);
        add(reportBtn);
        add(pauseBtn);
    }

    private JButton createNavButton(String text, java.awt.event.ActionListener listener) {
        JButton b = new JButton(text);
        b.setFont(new Font("Arial", Font.BOLD, 60));
        b.setForeground(new Color(220, 60, 60));
        b.setBackground(new Color(20, 0, 0));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.addActionListener(listener);
        ButtonHoverEffect.applyHoverEffect(
                b,
                new Color(20, 0, 0),
                new Color(60, 0, 0),
                new Color(140, 0, 0),
                new Color(200, 50, 50)
        );
        return b;
    }

    private JButton createMainButton(String text, java.awt.event.ActionListener listener) {
        JButton b = new JButton(text);
        b.setFont(new Font("Arial", Font.BOLD, 22));
        b.setForeground(new Color(220, 40, 40));
        b.setBackground(new Color(30, 0, 0));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.addActionListener(listener);
        ButtonHoverEffect.applyHoverEffect(
                b,
                new Color(30, 0, 0),
                new Color(80, 0, 0),
                new Color(180, 0, 0),
                new Color(255, 80, 80)
        );
        return b;
    }

    private void layoutUI() {
        int w = getWidth();
        int h = getHeight();
        if (w < 300 || h < 300) {
            return;
        }

        ensureComponentLayering();

        timeLabel.setBounds(40, 25, 380, 50);
        threatLabel.setBounds(40, 70, 380, 30);

        leftBtn.setBounds(40, h / 2 - 70, 110, 140);
        rightBtn.setBounds(w - 150, h / 2 - 70, 110, 140);

        reportBtn.setBounds(w / 2 - 220, h - 110, 440, 80);
        pauseBtn.setBounds(w - 200, 25, 170, 60);

        feedbackLabel.setBounds(w / 2 - 250, h / 2 - 80, 500, 160);
        reportingLabel.setBounds(w / 2 - 220, h - 160, 440, 40);

        if (jumpscareLabel != null) {
            jumpscareLabel.setBounds(0, 0, w, h);
        }

        if (gameOverOverlay != null) {
            gameOverOverlay.setBounds(0, 0, w, h);
        }
    }

    private void ensureComponentLayering() {
        if (reportOverlay != null) {
            setComponentZOrder(reportOverlay, getComponentCount() - 1);
        }
        if (pauseOverlay != null) {
            setComponentZOrder(pauseOverlay, getComponentCount() - 1);
        }
        if (gameOverOverlay != null) {
            setComponentZOrder(gameOverOverlay, getComponentCount() - 1);
        }
        if (jumpscareLabel != null) {
            setComponentZOrder(jumpscareLabel, getComponentCount() - 1);
        }
    }

    private void createReportOverlay() {
        reportOverlay = new JPanel(new GridBagLayout());
        reportOverlay.setBackground(new Color(0, 0, 0, 185));
        reportOverlay.setVisible(false);
        add(reportOverlay);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 40, 20, 40);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("CLASSIFY ANOMALY", SwingConstants.CENTER);
        title.setForeground(new Color(255, 80, 80));
        title.setFont(new Font("Arial", Font.BOLD, 32));
        reportOverlay.add(title, gbc);
        gbc.gridy = 1;

        for (int i = 0; i < ANOMALY_TYPES.length; i++) {
            final int type = i + 1;
            JButton btn = new JButton(ANOMALY_TYPES[i]);
            btn.setPreferredSize(new Dimension(520, 60));
            btn.setFont(new Font("Arial", Font.PLAIN, 20));
            btn.setForeground(Color.WHITE);
            btn.setBackground(new Color(110, 0, 0));
            btn.setFocusPainted(false);
            btn.setOpaque(true);
            btn.setBorder(BorderFactory.createLineBorder(new Color(150, 0, 0), 1));

            ButtonHoverEffect.applyHoverEffect(
                    btn,
                    new Color(110, 0, 0),
                    new Color(160, 20, 20),
                    new Color(150, 0, 0),
                    new Color(220, 100, 100)
            );
            btn.addActionListener(e -> {
                processReport(type);
                reportOverlay.setVisible(false);
                reporting = false;
            });
            reportOverlay.add(btn, gbc);
            gbc.gridy++;
        }

        JButton cancel = new JButton("CANCEL");
        cancel.setPreferredSize(new Dimension(520, 60));
        cancel.setFont(new Font("Arial", Font.BOLD, 20));
        cancel.setForeground(Color.WHITE);
        cancel.setBackground(new Color(60, 60, 60));
        cancel.setFocusPainted(false);
        cancel.setOpaque(true);
        cancel.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100), 1));

        ButtonHoverEffect.applyHoverEffect(
                cancel,
                new Color(60, 60, 60),
                new Color(100, 100, 100),
                new Color(100, 100, 100),
                new Color(150, 150, 150)
        );
        cancel.addActionListener(e -> {
            reportOverlay.setVisible(false);
            reporting = false;
        });
        reportOverlay.add(cancel, gbc);
    }

    private void showReportOverlay() {
        if (reportOnCooldown) {
            showFeedback("SYSTEM BUSY – PLEASE WAIT", new Color(255, 200, 120));
            return;
        }
        reporting = true;
        reportOverlay.setBounds(0, 0, getWidth(), getHeight());
        reportOverlay.setVisible(true);
    }

    private void processReport(int reportedType) {
        CameraData cam = cameras.get(currentCamera);

        if (!cam.hasAnomaly) {
            startReportCooldown(ReportResultType.NO_ANOMALY);
            return;
        }

        if (reportedType == cam.anomalyType) {
            reportBtn.setEnabled(false);
            reportingLabel.setVisible(true);
            Timer t = new Timer(REPORT_SUCCESS_DELAY_MS, e -> {
                cam.hasAnomaly = false;
                cam.anomalyType = 0;
                cam.anomalyMediaPath = null;
                decreaseThreat(getThreatDecreaseOnCorrect());
                reportingLabel.setVisible(false);
                reportBtn.setEnabled(true);
                updateCameraView();
                showFeedback("ANOMALY REMOVED", new Color(60, 255, 60));
            });
            t.setRepeats(false);
            t.start();
            if (random.nextDouble() < 0.65) {
                spawnRandomAnomaly();
            }
        } else {
            startReportCooldown(ReportResultType.WRONG_TYPE);
        }
    }

    private void showFeedback(String msg, Color color) {
        feedbackLabel.showFeedback(msg, color, 2500);
    }

    private void increaseThreat(int amount) {
        threatLevel = Math.min(100, threatLevel + amount);
        updateThreatLabel();
        if (threatLevel >= 100) {
            endGameWithThreatOverrun();
        }
    }

    private void decreaseThreat(int amount) {
        threatLevel = Math.max(0, threatLevel - amount);
        updateThreatLabel();
        if (threatLevel < 100) {
            stopCriticalThreatCountdown();
        }
    }

    private void updateThreatLabel() {
        String text;
        Color color;

        if (threatLevel < 25) {
            text = "THREAT: LOW";
            color = new Color(120, 220, 120);
        } else if (threatLevel < 50) {
            text = "THREAT: UNSTABLE";
            color = new Color(220, 200, 120);
        } else if (threatLevel < 75) {
            text = "THREAT: HIGH";
            color = new Color(255, 140, 0);
        } else {
            text = "THREAT: CRITICAL";
            color = new Color(255, 60, 60);
        }

        threatLabel.setText(text + " (" + threatLevel + "%)");
        threatLabel.setForeground(color);
    }

    /**
     * 20 second cooldown: player cannot report.
     * "REPORTING..." is shown for the first 15 seconds and only
     * after that we show the outcome message (e.g., "No anomalies found").
     */
    private void startReportCooldown(ReportResultType resultType) {
        reportOnCooldown = true;
        reportBtn.setEnabled(false);
        pendingReportResult = resultType;

        if (reportCooldownTimer != null) {
            reportCooldownTimer.stop();
        }
        if (reportingLabelTimer != null) {
            reportingLabelTimer.stop();
        }

        reportingLabel.setVisible(true);

        final int[] remainingSeconds = {20};
        reportCooldownTimer = new Timer(1000, e -> {
            remainingSeconds[0]--;
            if (remainingSeconds[0] <= 0) {
                reportCooldownTimer.stop();
                reportOnCooldown = false;
                reportBtn.setEnabled(true);
                reportingLabel.setVisible(false);
            }
        });
        reportCooldownTimer.start();

        reportingLabelTimer = new Timer(15000, e -> {
            reportingLabel.setVisible(false);
            handleDelayedReportResult();
            reportingLabelTimer.stop();
        });
        reportingLabelTimer.setRepeats(false);
        reportingLabelTimer.start();
    }

    private void handleDelayedReportResult() {
        if (pendingReportResult == null) {
            return;
        }
        if (pendingReportResult == ReportResultType.NO_ANOMALY) {
            increaseThreat(getThreatIncreaseOnFalseReport());
            showFeedback("NO ANOMALIES FOUND", new Color(255, 200, 120));
        } else if (pendingReportResult == ReportResultType.WRONG_TYPE) {
            increaseThreat(getThreatIncreaseOnWrongType());
            showFeedback("NO ANOMALIES FOUND", new Color(255, 180, 120));
        }
        pendingReportResult = null;
    }

    // ────────────────────────────────────────────────
    //   Difficulty-based tuning
    // ────────────────────────────────────────────────

    private int getThreatIncreaseOnFalseReport() {
        switch (difficulty) {
            case EASY:
                return 7;
            case HARD:
                return 15;
            case MEDIUM:
            default:
                return 10;
        }
    }

    private int getThreatIncreaseOnWrongType() {
        switch (difficulty) {
            case EASY:
                return 10;
            case HARD:
                return 20;
            case MEDIUM:
            default:
                return 15;
        }
    }

    private int getThreatDecreaseOnCorrect() {
        switch (difficulty) {
            case EASY:
                return 8;
            case HARD:
                return 3;
            case MEDIUM:
            default:
                return 5;
        }
    }

    private int getAnomalySpawnIntervalMs() {
        switch (difficulty) {
            case EASY:
                return 10000;
            case HARD:
                return 4000;
            case MEDIUM:
            default:
                return 7000;
        }
    }

    private double getAnomalySpawnChancePerTick() {
        switch (difficulty) {
            case EASY:
                return 0.4;
            case HARD:
                return 0.8;
            case MEDIUM:
            default:
                return 0.6;
        }
    }

    private void startAnomalySpawner() {
        int interval = getAnomalySpawnIntervalMs();
        double chance = getAnomalySpawnChancePerTick();

        anomalySpawnTimer = new Timer(interval, e -> {
            if (paused) {
                return;
            }
            if (gameMinutesTotal < 30) {
                return;
            }
            if (random.nextDouble() <= chance) {
                spawnRandomAnomaly();
            }
        });
        anomalySpawnTimer.start();
    }

    // ────────────────────────────────────────────────
    //   Pause menu, timer, camera view
    // ────────────────────────────────────────────────

    private void createPauseOverlay() {
        pauseOverlay = new JPanel(new GridBagLayout());
        pauseOverlay.setBackground(new Color(0, 0, 0, 210));
        pauseOverlay.setVisible(false);
        add(pauseOverlay);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(30, 0, 30, 0);

        JButton resume = createMainButton("RESUME", e -> togglePause());
        pauseOverlay.add(resume, gbc);

        gbc.gridy = 1;
        JButton mainMenu = createMainButton("MAIN MENU", e -> {
            stopAllTimers();
            frame.showScreen("MainMenu");
        });
        pauseOverlay.add(mainMenu, gbc);
    }

    private void togglePause() {
        paused = !paused;
        pauseOverlay.setBounds(0, 0, getWidth(), getHeight());
        pauseOverlay.setVisible(paused);
    }

    private void startGameTimer() {
        gameTimer = new Timer(1000, e -> {
            if (paused) {
                return;
            }
            gameMinutesTotal++;
            if (gameMinutesTotal >= 360) {
                stopAllTimers();
                JOptionPane.showMessageDialog(
                        this,
                        "06:00 AM – You survived.",
                        "Survived",
                        JOptionPane.INFORMATION_MESSAGE
                );
                frame.showScreen("MainMenu");
                return;
            }
            int h = gameMinutesTotal / 60;
            int m = gameMinutesTotal % 60;
            int dispH = (12 + h) % 12;
            if (dispH == 0) {
                dispH = 12;
            }
            timeLabel.setText(String.format("%d:%02d AM", dispH, m));
            secondsOnCurrentCamera++;
            if (secondsOnCurrentCamera >= 20) {
                secondsOnCurrentCamera = 0;
                nextCamera();
            }
        });
        gameTimer.start();
    }

    private void stopAllTimers() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        if (anomalySpawnTimer != null) {
            anomalySpawnTimer.stop();
        }
        if (reportCooldownTimer != null) {
            reportCooldownTimer.stop();
        }
        if (reportingLabelTimer != null) {
            reportingLabelTimer.stop();
        }
        if (criticalThreatTimer != null) {
            criticalThreatTimer.stop();
        }
    }

    private void updateCameraView() {
        if (isGameOverSequence) {
            return;
        }
        repaint();
    }

    private void prevCamera() {
        currentCamera = (currentCamera - 1 + cameras.size()) % cameras.size();
        secondsOnCurrentCamera = 0;
        updateCameraView();
    }

    private void nextCamera() {
        currentCamera = (currentCamera + 1) % cameras.size();
        secondsOnCurrentCamera = 0;
        updateCameraView();
    }

    private void endGameWithThreatOverrun() {
        startCriticalThreatCountdown();
    }

    private void showJumpscare() {
        if (jumpscareLabel == null) {
            jumpscareLabel = new JLabel("", SwingConstants.CENTER);
            jumpscareLabel.setOpaque(true);
            jumpscareLabel.setBackground(Color.BLACK);
            add(jumpscareLabel);
        }
        java.net.URL resource = getClass().getResource(JUMPSCARE_GIF_PATH);
        if (resource != null) {
            jumpscareLabel.setIcon(new ImageIcon(resource));
        } else {
            jumpscareLabel.setIcon(null);
        }
        jumpscareLabel.setBounds(0, 0, getWidth(), getHeight());
        jumpscareLabel.setVisible(true);
        repaint();
    }

    private void startCriticalThreatCountdown() {
        if (criticalThreatTimer != null && criticalThreatTimer.isRunning()) {
            return;
        }
        criticalThreatSeconds = 0;
        criticalThreatTimer = new Timer(1000, e -> {
            if (paused) {
                return;
            }
            if (threatLevel < 100) {
                stopCriticalThreatCountdown();
                return;
            }
            criticalThreatSeconds++;
            if (criticalThreatSeconds >= CRITICAL_THREAT_SECONDS_TO_FAIL) {
                criticalThreatTimer.stop();
                triggerGameOverWithRoomJumpscare();
            }
        });
        criticalThreatTimer.start();
    }

    private void stopCriticalThreatCountdown() {
        criticalThreatSeconds = 0;
        if (criticalThreatTimer != null) {
            criticalThreatTimer.stop();
        }
    }

    private void triggerGameOverWithRoomJumpscare() {
        if (isGameOverSequence) {
            return;
        }
        isGameOverSequence = true;
        stopAllTimers();

        if (reportOverlay != null) {
            reportOverlay.setVisible(false);
        }
        if (pauseOverlay != null) {
            pauseOverlay.setVisible(false);
        }
        reportingLabel.setVisible(false);

        reportBtn.setEnabled(false);
        pauseBtn.setEnabled(false);
        leftBtn.setEnabled(false);
        rightBtn.setEnabled(false);

        currentCamera = (currentCamera + 1) % cameras.size();
        showFallbackJumpscareGifThenGameOver();
    }



    private void showFallbackJumpscareGifThenGameOver() {
        showJumpscare();
        Timer t = new Timer(2000, e -> showGameOverOverlay());
        t.setRepeats(false);
        t.start();
    }

    private void showGameOverOverlay() {
        if (gameOverOverlay == null) {
            gameOverOverlay = new JPanel(new GridBagLayout());
            gameOverOverlay.setBackground(new Color(0, 0, 0, 210));
            gameOverOverlay.setVisible(false);
            add(gameOverOverlay);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(20, 40, 20, 40);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;

            JLabel title = new JLabel("YOU FAILED TO SURVIVE UNTIL 6:00 AM", SwingConstants.CENTER);
            title.setForeground(new Color(255, 80, 80));
            title.setFont(new Font("Arial", Font.BOLD, 28));
            gameOverOverlay.add(title, gbc);

            gbc.gridy++;
            JButton mainMenu = createMainButton("MAIN MENU", e -> {
                frame.showScreen("MainMenu");
            });
            gameOverOverlay.add(mainMenu, gbc);

            gbc.gridy++;
            JButton playAgain = createMainButton("PLAY AGAIN", e -> {
                frame.showScreen("Difficulty");
            });
            gameOverOverlay.add(playAgain, gbc);
        }
        gameOverOverlay.setBounds(0, 0, getWidth(), getHeight());
        gameOverOverlay.setVisible(true);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }
}

