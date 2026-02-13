package com.horrorgame.audio;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.*;

public class SoundManager {

    private static Clip backgroundClip;

    // 🎵 Looping background music from classpath
    public static void playLoop(String resourcePath) {
        stopLoop();  // clean up any previous music

        try {
            // Load from classpath (works in IDE and JAR)
            // resourcePath should start with "/" → e.g. "/com/horrorgame/assets/audio/menu_music.wav"
            InputStream is = SoundManager.class.getResourceAsStream(resourcePath);
            if (is == null) {
                System.err.println("Audio resource not found: " + resourcePath);
                return;
            }

            // BufferedInputStream helps with mark/reset support (some formats need it)
            InputStream bufferedIs = new BufferedInputStream(is);
            AudioInputStream audio = AudioSystem.getAudioInputStream(bufferedIs);

            backgroundClip = AudioSystem.getClip();
            backgroundClip.open(audio);
            backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundClip.start();

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Failed to play loop: " + resourcePath);
            e.printStackTrace();
        }
    }

    public static void stopLoop() {
        if (backgroundClip != null) {
            if (backgroundClip.isRunning()) {
                backgroundClip.stop();
            }
            backgroundClip.close();
            backgroundClip = null;
        }
    }

    // 🔊 One-shot sound effect (non-looping)
    public static void playSound(String resourcePath) {
        try {
            InputStream is = SoundManager.class.getResourceAsStream(resourcePath);
            if (is == null) {
                System.err.println("Sound resource not found: " + resourcePath);
                return;
            }

            InputStream bufferedIs = new BufferedInputStream(is);
            AudioInputStream audio = AudioSystem.getAudioInputStream(bufferedIs);

            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            clip.start();

            // Optional: close when done (but don't block thread)
            new Thread(() -> {
                while (clip.isRunning()) {
                    try { Thread.sleep(50); } catch (InterruptedException ignored) {}
                }
                clip.close();
                try { bufferedIs.close(); } catch (IOException ignored) {}
            }).start();

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Failed to play sound: " + resourcePath);
            e.printStackTrace();
        }
    }
}