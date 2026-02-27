package com.horrorgame.game;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * MP4 playback uses JavaFX if it is present on the classpath.
 * This class is implemented via reflection so the project compiles even without JavaFX.
 */
public class VideoBackgroundPanel extends JPanel {

    private String currentMediaPath;
    private final boolean isJavaFxAvailable;
    private final Component fxHostComponent;

    private Object mediaView;
    private Object mediaPlayer;

    public VideoBackgroundPanel() {
        setLayout(new BorderLayout());
        isJavaFxAvailable = isJavaFxPresent();
        if (!isJavaFxAvailable) {
            fxHostComponent = createFallbackPanel();
            add(fxHostComponent, BorderLayout.CENTER);
            return;
        }

        fxHostComponent = createJfxPanelInstance();
        add(fxHostComponent, BorderLayout.CENTER);
        ensureJavaFxStarted();
        runOnFxThread(this::initializeScene);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension size = getSize();
                runOnFxThread(() -> updateMediaViewSize(size.width, size.height));
            }
        });
    }

    public void playLoopedResource(String resourcePath) {
        if (resourcePath == null) {
            stopPlayback();
            return;
        }
        if (resourcePath.equals(currentMediaPath)) {
            return;
        }
        currentMediaPath = resourcePath;
        if (!isJavaFxAvailable) {
            return;
        }
        runOnFxThread(() -> playResourceInternal(resourcePath, -1, null));
    }

    public void playOnceResource(String resourcePath, Runnable onFinished) {
        if (resourcePath == null) {
            if (onFinished != null) {
                SwingUtilities.invokeLater(onFinished);
            }
            return;
        }
        currentMediaPath = resourcePath;
        if (!isJavaFxAvailable) {
            if (onFinished != null) {
                SwingUtilities.invokeLater(onFinished);
            }
            return;
        }
        runOnFxThread(() -> playResourceInternal(resourcePath, 1, onFinished));
    }

    public void stopPlayback() {
        currentMediaPath = null;
        if (!isJavaFxAvailable) {
            return;
        }
        runOnFxThread(() -> {
            if (mediaPlayer == null) {
                return;
            }
            invokeNoArgs(mediaPlayer, "stop");
            invokeNoArgs(mediaPlayer, "dispose");
            mediaPlayer = null;
            if (mediaView != null) {
                invoke(mediaView, "setMediaPlayer", new Class[]{getJavaFxClass("javafx.scene.media.MediaPlayer")}, new Object[]{null});
            }
        });
    }

    private void initializeScene() {
        Object root = newInstance(getJavaFxClass("javafx.scene.Group"), new Class[]{}, new Object[]{});
        Object scene = newInstance(getJavaFxClass("javafx.scene.Scene"), new Class[]{getJavaFxClass("javafx.scene.Parent")}, new Object[]{root});

        mediaView = newInstance(getJavaFxClass("javafx.scene.media.MediaView"), new Class[]{}, new Object[]{});
        invoke(mediaView, "setPreserveRatio", new Class[]{boolean.class}, new Object[]{false});
        updateMediaViewSize(getWidth(), getHeight());

        if (mediaPlayer != null) {
            invoke(mediaView, "setMediaPlayer", new Class[]{getJavaFxClass("javafx.scene.media.MediaPlayer")}, new Object[]{mediaPlayer});
        }

        Object children = invoke(root, "getChildren", new Class[]{}, new Object[]{});
        invoke(children, "add", new Class[]{Object.class}, new Object[]{mediaView});

        invoke(fxHostComponent, "setScene", new Class[]{getJavaFxClass("javafx.scene.Scene")}, new Object[]{scene});
    }

    private void playResourceInternal(String resourcePath, int cycleCount, Runnable onFinished) {
        java.net.URL resourceUrl = getClass().getResource(resourcePath);
        if (resourceUrl == null) {
            return;
        }
        String uri = resourceUrl.toExternalForm();
        Object media = newInstance(getJavaFxClass("javafx.scene.media.Media"), new Class[]{String.class}, new Object[]{uri});
        Object newPlayer = newInstance(getJavaFxClass("javafx.scene.media.MediaPlayer"), new Class[]{getJavaFxClass("javafx.scene.media.Media")}, new Object[]{media});

        invoke(newPlayer, "setCycleCount", new Class[]{int.class}, new Object[]{cycleCount});
        if (cycleCount != -1 && onFinished != null) {
            invoke(newPlayer, "setOnEndOfMedia", new Class[]{Runnable.class}, new Object[]{(Runnable) () -> SwingUtilities.invokeLater(onFinished)});
        }

        if (mediaPlayer != null) {
            invokeNoArgs(mediaPlayer, "stop");
            invokeNoArgs(mediaPlayer, "dispose");
        }
        mediaPlayer = newPlayer;
        if (mediaView != null) {
            invoke(mediaView, "setMediaPlayer", new Class[]{getJavaFxClass("javafx.scene.media.MediaPlayer")}, new Object[]{mediaPlayer});
        }
        invokeNoArgs(mediaPlayer, "play");
    }

    private void updateMediaViewSize(int width, int height) {
        if (mediaView == null) {
            return;
        }
        invoke(mediaView, "setFitWidth", new Class[]{double.class}, new Object[]{(double) width});
        invoke(mediaView, "setFitHeight", new Class[]{double.class}, new Object[]{(double) height});
    }

    private static synchronized void ensureJavaFxStarted() {
        try {
            Class<?> platform = Class.forName("javafx.application.Platform");
            Method startup = platform.getMethod("startup", Runnable.class);
            startup.invoke(null, (Runnable) () -> {});
        } catch (ClassNotFoundException ignored) {
        } catch (NoSuchMethodException | IllegalAccessException ignored) {
        } catch (InvocationTargetException ignored) {
        } catch (IllegalStateException ignored) {
        }
    }

    private static void runOnFxThread(Runnable runnable) {
        try {
            Class<?> platform = Class.forName("javafx.application.Platform");
            Method runLater = platform.getMethod("runLater", Runnable.class);
            runLater.invoke(null, runnable);
        } catch (ClassNotFoundException ignored) {
        } catch (NoSuchMethodException | IllegalAccessException ignored) {
        } catch (InvocationTargetException ignored) {
        }
    }

    private static boolean isJavaFxPresent() {
        try {
            Class.forName("javafx.application.Platform");
            Class.forName("javafx.embed.swing.JFXPanel");
            Class.forName("javafx.scene.media.MediaPlayer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static Component createJfxPanelInstance() {
        try {
            Class<?> clazz = Class.forName("javafx.embed.swing.JFXPanel");
            Object instance = clazz.getConstructor().newInstance();
            return (Component) instance;
        } catch (ReflectiveOperationException e) {
            return createFallbackPanel();
        }
    }

    private static Component createFallbackPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);
        JLabel label = new JLabel("JavaFX not configured – MP4 playback disabled", SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private static Class<?> getJavaFxClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Object newInstance(Class<?> clazz, Class<?>[] paramTypes, Object[] args) {
        try {
            return clazz.getConstructor(paramTypes).newInstance(args);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Object invoke(Object target, String methodName, Class<?>[] paramTypes, Object[] args) {
        if (target == null) {
            return null;
        }
        try {
            Method method = target.getClass().getMethod(methodName, paramTypes);
            return method.invoke(target, args);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            return null;
        } catch (InvocationTargetException e) {
            return null;
        }
    }

    private static void invokeNoArgs(Object target, String methodName) {
        invoke(target, methodName, new Class[]{}, new Object[]{});
    }
}

