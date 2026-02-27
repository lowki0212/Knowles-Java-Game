package com.horrorgame.game;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class RoomMediaLibrary {

    public enum AnomalyType {
        MISSING_OBJECT,
        OBJECT_DISPLACEMENT,
        SHADOWY_FIGURE,
        INTRUDER,
        STRANGE_IMAGERY,
        DEMONIC,
        EXTRA_OBJECT,
        AUDIO_DISTURBANCE
    }

    private static final String ROOMS_ROOT = "/com/horrorgame/assets/images/rooms/";

    private final Map<String, RoomMedia> roomsByKey;
    private final Random random = new Random();

    private RoomMediaLibrary(Map<String, RoomMedia> roomsByKey) {
        this.roomsByKey = roomsByKey;
    }

    public static RoomMediaLibrary load(Class<?> anchorClass) {
        List<String> mp4Resources = listResources(anchorClass, ROOMS_ROOT, ".mp4");
        Map<String, RoomMedia> rooms = new HashMap<>();
        for (String resourcePath : mp4Resources) {
            ParsedResource parsed = ParsedResource.parse(resourcePath);
            if (parsed == null) {
                continue;
            }
            RoomMedia room = rooms.computeIfAbsent(parsed.roomKey, RoomMedia::new);
            if (parsed.isJumpscare) {
                room.jumpscares.add(resourcePath);
                continue;
            }
            if (parsed.isNormal) {
                room.normals.add(resourcePath);
                continue;
            }
            if (parsed.anomalyType != null) {
                room.anomalies.computeIfAbsent(parsed.anomalyType, t -> new ArrayList<>()).add(resourcePath);
            }
        }
        return new RoomMediaLibrary(rooms);
    }

    public List<String> getRoomKeys() {
        List<String> keys = new ArrayList<>(roomsByKey.keySet());
        Collections.sort(keys);
        return keys;
    }

    public String getRandomNormal(String roomKey) {
        RoomMedia room = roomsByKey.get(roomKey);
        if (room == null || room.normals.isEmpty()) {
            return null;
        }
        return room.normals.get(random.nextInt(room.normals.size()));
    }

    public String getRandomJumpscare(String roomKey) {
        RoomMedia room = roomsByKey.get(roomKey);
        if (room == null || room.jumpscares.isEmpty()) {
            return null;
        }
        return room.jumpscares.get(random.nextInt(room.jumpscares.size()));
    }

    public String getRandomAnomaly(String roomKey, AnomalyType anomalyType) {
        RoomMedia room = roomsByKey.get(roomKey);
        if (room == null) {
            return null;
        }
        List<String> list = room.anomalies.get(anomalyType);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(random.nextInt(list.size()));
    }

    public List<AnomalyType> getAvailableAnomalyTypes(String roomKey) {
        RoomMedia room = roomsByKey.get(roomKey);
        if (room == null) {
            return List.of();
        }
        return new ArrayList<>(room.anomalies.keySet());
    }

    private static final class RoomMedia {
        final String roomKey;
        final List<String> normals = new ArrayList<>();
        final List<String> jumpscares = new ArrayList<>();
        final Map<AnomalyType, List<String>> anomalies = new HashMap<>();

        RoomMedia(String roomKey) {
            this.roomKey = roomKey;
        }
    }

    private static final class ParsedResource {
        final String roomKey;
        final boolean isJumpscare;
        final boolean isNormal;
        final AnomalyType anomalyType;

        private ParsedResource(String roomKey, boolean isJumpscare, boolean isNormal, AnomalyType anomalyType) {
            this.roomKey = roomKey;
            this.isJumpscare = isJumpscare;
            this.isNormal = isNormal;
            this.anomalyType = anomalyType;
        }

        static ParsedResource parse(String resourcePath) {
            if (resourcePath == null) {
                return null;
            }
            if (!resourcePath.startsWith(ROOMS_ROOT) || !resourcePath.toLowerCase(Locale.ROOT).endsWith(".mp4")) {
                return null;
            }
            String relative = resourcePath.substring(ROOMS_ROOT.length());
            int slashIndex = relative.indexOf('/');
            if (slashIndex <= 0) {
                return null;
            }
            String roomKey = relative.substring(0, slashIndex);
            String remaining = relative.substring(slashIndex + 1);
            boolean isJumpscare = remaining.toLowerCase(Locale.ROOT).startsWith("jumpscare/");
            String fileName = isJumpscare ? remaining.substring("jumpscare/".length()) : remaining;
            String lowerName = fileName.toLowerCase(Locale.ROOT);
            if (lowerName.endsWith(".mp4")) {
                lowerName = lowerName.substring(0, lowerName.length() - 4);
            }

            if (isJumpscare) {
                return new ParsedResource(roomKey, true, false, null);
            }

            if (lowerName.contains("normal")) {
                return new ParsedResource(roomKey, false, true, null);
            }

            int underscoreIndex = lowerName.indexOf('_');
            String typePart = underscoreIndex >= 0 ? lowerName.substring(underscoreIndex + 1) : lowerName;
            typePart = typePart.replace("_", "");
            typePart = typePart.replaceAll("\\d+$", "");

            AnomalyType anomalyType = mapType(typePart);
            return new ParsedResource(roomKey, false, false, anomalyType);
        }

        private static AnomalyType mapType(String normalizedType) {
            if (normalizedType == null) {
                return null;
            }
            switch (normalizedType) {
                case "missingobject":
                    return AnomalyType.MISSING_OBJECT;
                case "objectdisplacement":
                case "displacement":
                    return AnomalyType.OBJECT_DISPLACEMENT;
                case "shadowyfigure":
                case "shadowyfiguree":
                    return AnomalyType.SHADOWY_FIGURE;
                case "intruder":
                    return AnomalyType.INTRUDER;
                case "strangeimagery":
                case "strangeimageryy":
                case "strangerimagery":
                    return AnomalyType.STRANGE_IMAGERY;
                case "demonic":
                    return AnomalyType.DEMONIC;
                case "extraobject":
                case "newobject":
                case "newobj":
                case "newobjectt":
                    return AnomalyType.EXTRA_OBJECT;
                case "audiodisturbance":
                case "audiodisturbancee":
                case "audiodisturbance1":
                    return AnomalyType.AUDIO_DISTURBANCE;
                default:
                    return null;
            }
        }
    }

    private static List<String> listResources(Class<?> anchorClass, String rootPath, String extensionLower) {
        URL rootUrl = anchorClass.getResource(rootPath);
        if (rootUrl == null) {
            return List.of();
        }
        String protocol = rootUrl.getProtocol();
        if ("file".equals(protocol)) {
            try {
                Path root = Path.of(rootUrl.toURI());
                return listFromFileSystem(root, rootPath, extensionLower);
            } catch (URISyntaxException e) {
                return List.of();
            }
        }
        if ("jar".equals(protocol)) {
            return listFromJar(rootUrl, rootPath, extensionLower);
        }
        return List.of();
    }

    private static List<String> listFromFileSystem(Path root, String rootPath, String extensionLower) {
        try {
            List<String> results = new ArrayList<>();
            Files.walk(root).filter(Files::isRegularFile).forEach(path -> {
                String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
                if (!fileName.endsWith(extensionLower)) {
                    return;
                }
                String relative = root.relativize(path).toString().replace(File.separatorChar, '/');
                results.add(rootPath + relative);
            });
            return results;
        } catch (IOException e) {
            return List.of();
        }
    }

    private static List<String> listFromJar(URL rootUrl, String rootPath, String extensionLower) {
        try {
            JarURLConnection connection = (JarURLConnection) rootUrl.openConnection();
            JarFile jar = connection.getJarFile();
            String prefix = rootPath.startsWith("/") ? rootPath.substring(1) : rootPath;
            List<String> results = new ArrayList<>();
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (entry.isDirectory()) {
                    continue;
                }
                String lower = name.toLowerCase(Locale.ROOT);
                if (!lower.startsWith(prefix.toLowerCase(Locale.ROOT)) || !lower.endsWith(extensionLower)) {
                    continue;
                }
                results.add("/" + name);
            }
            return results;
        } catch (IOException e) {
            return List.of();
        }
    }
}

