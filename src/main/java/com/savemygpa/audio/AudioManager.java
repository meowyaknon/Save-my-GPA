package com.savemygpa.audio;

import javafx.scene.media.Media;
import javafx.scene.media.AudioClip;
import javafx.scene.media.MediaPlayer;

import java.util.HashMap;
import java.util.Map;

/**
 * AudioManager — centralised audio + resolution state.
 *
 * Volumes (0.0 – 1.0):
 *   gameVolume  → master multiplier applied to all channels
 *   sfxVolume   → button-click and short sound effects
 *   musicVolume → looping background music per scene
 *
 * Requires javafx.media on the classpath / module-info.
 *
 * ── Music keys (use AudioManager.Music.*) ────────────────────────────────────
 *   INTRO        → played when game first opens / intro sequence
 *   CUTSCENE     → quiet ambient music under typing cutscenes
 *   ACCEPTANCE   → acceptance screen background
 *   MAIN_MENU    → main menu background
 *   OUTSIDE      → campus map (OutsideUI)
 *   INSIDE       → IT Building (InsideUI)
 *
 * ── SFX keys (use AudioManager.Sfx.*) ────────────────────────────────────────
 *   CLICK        → generic button (accept / confirm)
 *   ACCEPT       → accept / positive action button
 *   REFUSE       → refuse / cancel / quit button
 *   TYPING       → single key-tap played per character in typewriter effect
 */
public class AudioManager {

    // ── Named music constants ─────────────────────────────────────────────────
    public static final class Music {
        public static final String INTRO      = "intro";
        public static final String CUTSCENE   = "cutscene";
        public static final String ACCEPTANCE = "acceptance";
        public static final String MAIN_MENU  = "mainmenu";
        public static final String OUTSIDE    = "outside";
        public static final String INSIDE     = "inside";
        private Music() {}
    }

    // ── Named SFX constants ───────────────────────────────────────────────────
    public static final class Sfx {
        public static final String ACCEPT = "/audio/sfx/confirm.wav";
        public static final String REFUSE = "/audio/sfx/cancel.wav";
        public static final String TYPING = "/audio/sfx/typing.mp3";
        private Sfx() {}
    }

    // ── Music resource paths (classpath) ──────────────────────────────────────
    private static final Map<String, String> MUSIC_PATHS = new HashMap<>();
    static {
        MUSIC_PATHS.put(Music.INTRO,      "/audio/music/intro.wav");
        MUSIC_PATHS.put(Music.CUTSCENE,   "/audio/music/intro.wav");
        MUSIC_PATHS.put(Music.ACCEPTANCE, "/audio/music/acceptance.wav");
        MUSIC_PATHS.put(Music.MAIN_MENU,  "/audio/music/mainmenu.mp3");
        MUSIC_PATHS.put(Music.OUTSIDE,    "/audio/music/map.mp3");
        MUSIC_PATHS.put(Music.INSIDE,     "/audio/music/map.mp3");
    }

    // ── Volumes ───────────────────────────────────────────────────────────────
    private double gameVolume  = 1.0;
    private double sfxVolume   = 0.8;
    private double musicVolume = 0.6;

    // ── Resolution state ──────────────────────────────────────────────────────
    private int resolutionWidth  = 1920;
    private int resolutionHeight = 1080;

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
    // Resolution
    // ═════════════════════════════════════════════════════════════════════════

    public int  getResolutionWidth()  { return resolutionWidth;  }
    public int  getResolutionHeight() { return resolutionHeight; }
    public void setResolution(int w, int h) { resolutionWidth = w; resolutionHeight = h; }

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
            currentMusicKey = key; // mark as "playing" so we don't retry every frame
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

    public void fadeOutMusic(int ms) {
        if (musicPlayer == null) return;
        MediaPlayer mp = musicPlayer;
        musicPlayer = null;
        currentMusicKey = "";

        double startVol = mp.getVolume();
        long startTime  = System.currentTimeMillis();

        javafx.animation.AnimationTimer timer = new javafx.animation.AnimationTimer() {
            @Override public void handle(long now) {
                double elapsed = System.currentTimeMillis() - startTime;
                double frac    = Math.min(elapsed / ms, 1.0);
                mp.setVolume(startVol * (1.0 - frac));
                if (frac >= 1.0) {
                    mp.stop();
                    mp.dispose();
                    stop();
                }
            }
        };
        timer.start();
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

    /**
     * Typewriter tick — plays the typing SFX once.
     * Call this once per character revealed in a typewriter animation.
     * The SFX file should be very short (< 80 ms) so overlapping instances
     * feel like rapid key-taps rather than a pile-up of sounds.
     */
    public void playTyping() { playSfx(Sfx.TYPING); }
}