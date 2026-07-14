package ui;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Loads minimal fields from piece state config.json. */
public final class SpriteConfigLoader {
    private static final Pattern SPEED = Pattern.compile("\\\"speed_m_per_sec\\\"\\s*:\\s*([0-9]+(?:\\\\.[0-9]+)?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern FPS = Pattern.compile("\\\"frames_per_sec\\\"\\s*:\\s*([0-9]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern LOOP = Pattern.compile("\\\"is_loop\\\"\\s*:\\s*(true|false)", Pattern.CASE_INSENSITIVE);
    private static final Pattern NEXT = Pattern.compile("\\\"next_state_when_finished\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"", Pattern.CASE_INSENSITIVE);

    private SpriteConfigLoader() {
    }

    public static SpriteStateConfig load(Path configPath) {
        SpriteStateConfig d = SpriteStateConfig.defaults();
        if (configPath == null || !Files.exists(configPath)) {
            return d;
        }

        String text;
        try {
            text = Files.readString(configPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return d;
        }

        double speed = extractDouble(text, SPEED, d.getSpeedCellsPerSec());
        int fps = extractInt(text, FPS, d.getFramesPerSec());
        boolean loop = extractBoolean(text, LOOP, d.isLoop());
        String next = extractString(text, NEXT, d.getNextState());

        if (fps <= 0) {
            fps = d.getFramesPerSec();
        }
        if (next == null || next.isEmpty()) {
            next = d.getNextState();
        }

        return new SpriteStateConfig(speed, fps, loop, next.toLowerCase());
    }

    private static double extractDouble(String text, Pattern pattern, double fallback) {
        Matcher m = pattern.matcher(text);
        if (!m.find()) return fallback;
        try {
            return Double.parseDouble(m.group(1));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static int extractInt(String text, Pattern pattern, int fallback) {
        Matcher m = pattern.matcher(text);
        if (!m.find()) return fallback;
        try {
            return Integer.parseInt(m.group(1));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static boolean extractBoolean(String text, Pattern pattern, boolean fallback) {
        Matcher m = pattern.matcher(text);
        if (!m.find()) return fallback;
        return Boolean.parseBoolean(m.group(1));
    }

    private static String extractString(String text, Pattern pattern, String fallback) {
        Matcher m = pattern.matcher(text);
        if (!m.find()) return fallback;
        return m.group(1);
    }
}