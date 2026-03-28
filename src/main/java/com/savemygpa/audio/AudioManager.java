package com.savemygpa.audio;

import javafx.scene.media.Media;
import javafx.scene.media.AudioClip;
import javafx.scene.media.MediaPlayer;

import java.util.HashMap;
import java.util.Map;

public class AudioManager {

    // ── Named music constants ─────────────────────────────────────────────────
    public static final class Music {
        public static final String INTRO          = "intro";
        public static final String CUTSCENE       = "cutscene";
        public static final String ACCEPTANCE     = "acceptance";
        public static final String MAIN_MENU      = "mainmenu";
        public static final String OUTSIDE        = "outside";
        public static final String INSIDE         = "inside";
        // ── Ending music ──────────────────────────────────────────────────────
        public static final String ENDING_GREAT = "ending_great";
        public static final String ENDING_MID   = "ending_mid";
        public static final String ENDING_BAD   = "ending_bad";
        private Music() {}
    }

    // ── Named SFX constants ───────────────────────────────────────────────────
    public static final class Sfx {
        public static final String ACCEPT = "/audio/sfx/confirm.wav";
        public static final String REFUSE = "/audio/sfx/cancel.wav";
        public static final String TYPING = "/audio/sfx/typing.mp3";
        public static final String WRONG = "/audio/sfx/wrong.wav";
        private Sfx() {}
    }

    // ── Music resource paths (classpath) ──────────────────────────────────────
    private static final Map<String, String> MUSIC_PATHS = new HashMap<>();
    static {
        MUSIC_PATHS.put(Music.INTRO,          "/audio/music/intro.wav");
        MUSIC_PATHS.put(Music.CUTSCENE,       "/audio/music/intro.wav");
        MUSIC_PATHS.put(Music.ACCEPTANCE,     "/audio/music/acceptance.wav");
        MUSIC_PATHS.put(Music.MAIN_MENU,      "/audio/music/mainmenu.mp3");
        MUSIC_PATHS.put(Music.OUTSIDE,        "/audio/music/map.mp3");
        MUSIC_PATHS.put(Music.INSIDE,         "/audio/music/map.mp3");
        // ── Ending music ─────────
        MUSIC_PATHS.put(Music.ENDING_GREAT, "/audio/music/ending_good.mp3");
        MUSIC_PATHS.put(Music.ENDING_MID,   "/audio/music/ending_mid.mp3");
        MUSIC_PATHS.put(Music.ENDING_BAD,   "/audio/music/ending_bad.mp3");
    }

    // ── Volumes ───────────────────────────────────────────────────────────────
    private double gameVolume  = 1.0;
    private double sfxVolume   = 0.8;
    private double musicVolume = 0.6;

    // ── Internal state ────────────────────────────────────────────────────────
    private String      currentMusicKey = "";
    private MediaPlayer musicPlayer     = null;
    private final Map<String, AudioClip> sfxCache = new HashMap<>();

    // ── Singleton ─────────────────────────────────────────────────────────────
    private static final AudioManager INSTANCE = new AudioManager();
    public  static AudioManager getInstance() { return INSTANCE; }
    private AudioManager() {}

    // ═════════════════════════════════════════════════════════════════════════
    // Volumes
    // ═════════════════════════════════════════════════════════════════════════

    public double getGameVolume()  { return gameVolume;  }
    public double getSfxVolume()   { return sfxVolume;   }
    public double getMusicVolume() { return musicVolume; }

    public void setGameVolume(double v) {
        gameVolume = clamp(v);
        if (musicPlayer != null) musicPlayer.setVolume(effectiveMusicVol());
    }

    public void setSfxVolume(double v)   { sfxVolume   = clamp(v); }

    public void setMusicVolume(double v) {
        musicVolume = clamp(v);
        if (musicPlayer != null) musicPlayer.setVolume(effectiveMusicVol());
    }

    private double effectiveMusicVol() { return clamp(musicVolume * gameVolume); }
    private double effectiveSfxVol()   { return clamp(sfxVolume   * gameVolume); }
    private double clamp(double v)     { return Math.max(0.0, Math.min(1.0, v)); }

    // ═════════════════════════════════════════════════════════════════════════
    // Music
    // ═════════════════════════════════════════════════════════════════════════

    public void playMusic(String key) {
        if (key.equals(currentMusicKey)) return;
        stopMusic();

        String path = MUSIC_PATHS.get(key);
        if (path == null) {
            System.err.println("[AudioManager] Unknown music key: " + key);
            return;
        }

        var url = getClass().getResource(path);
        if (url == null) {
            System.err.println("[AudioManager] Music resource not found: " + path);
            currentMusicKey = key;
            return;
        }

        try {
            Media media = new Media(url.toExternalForm());
            musicPlayer = new MediaPlayer(media);
            musicPlayer.setVolume(effectiveMusicVol());
            musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            musicPlayer.play();
            currentMusicKey = key;
        } catch (Exception e) {
            System.err.println("[AudioManager] Failed to play music '" + key + "': " + e.getMessage());
        }
    }

    public void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
            musicPlayer.dispose();
            musicPlayer = null;
        }
        currentMusicKey = "";
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SFX
    // ═════════════════════════════════════════════════════════════════════════

    public void playSfx(String resourcePath) {
        try {
            AudioClip clip = sfxCache.get(resourcePath);

            if (clip == null) {
                var url = getClass().getResource(resourcePath);
                if (url == null) {
                    System.err.println("[AudioManager] SFX not found: " + resourcePath);
                    return;
                }

                clip = new AudioClip(url.toExternalForm());
                sfxCache.put(resourcePath, clip);
            }

            clip.setVolume(effectiveSfxVol());
            clip.play();

        } catch (Exception e) {
            System.err.println("[AudioManager] Failed SFX: " + e.getMessage());
        }
    }

    // ── Convenience shortcuts ─────────────────────────────────────────────────

    public void playAccept() { playSfx(Sfx.ACCEPT); }

    public void playRefuse() { playSfx(Sfx.REFUSE); }

    public void playTyping() { playSfx(Sfx.TYPING); }

    public void playWrong() { playSfx(Sfx.WRONG); }
}