package com.savemygpa.audio;

/**
 * AudioManager — centralised audio + resolution state.
 *
 * Volumes (0.0 – 1.0):
 *   gameVolume  → master multiplier applied to all channels
 *   sfxVolume   → button-click and short sound effects
 *   musicVolume → looping background music per scene
 *
 * NOTE: This version does NOT depend on javafx.media.
 * If you want real audio later, add javafx.media to your module-info/pom
 * and replace the stub play* methods with real MediaPlayer calls.
 */
public class AudioManager {

    // ── Volume state ──────────────────────────────────────────────────────────
    private double gameVolume  = 1.0;
    private double sfxVolume   = 0.8;
    private double musicVolume = 0.6;

    // ── Resolution state ──────────────────────────────────────────────────────
    private int resolutionWidth  = 1920;
    private int resolutionHeight = 1080;

    // ── Current music key (for resume / skip-duplicate logic) ─────────────────
    private String currentMusicKey = "";

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

    public void setGameVolume(double v)  { gameVolume  = clamp(v); }
    public void setSfxVolume(double v)   { sfxVolume   = clamp(v); }
    public void setMusicVolume(double v) { musicVolume = clamp(v); }

    private double clamp(double v) { return Math.max(0.0, Math.min(1.0, v)); }

    // ═════════════════════════════════════════════════════════════════════════
    // Resolution
    // ═════════════════════════════════════════════════════════════════════════

    public int  getResolutionWidth()  { return resolutionWidth;  }
    public int  getResolutionHeight() { return resolutionHeight; }
    public void setResolution(int w, int h) { resolutionWidth = w; resolutionHeight = h; }

    // ═════════════════════════════════════════════════════════════════════════
    // Music stubs
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Start looping background music for a scene.
     * @param key          unique scene identifier  (e.g. "gameplay")
     * @param resourcePath classpath path           (e.g. "/audio/music/gameplay.mp3")
     */
    public void playMusic(String key, String resourcePath) {
        if (key.equals(currentMusicKey)) return;
        currentMusicKey = key;
        // TODO: use MediaPlayer when javafx.media is on the classpath
        System.out.println("[AudioManager] playMusic: " + key);
    }

    /** Stop the current background music. */
    public void stopMusic() {
        currentMusicKey = "";
        // TODO: stop/dispose MediaPlayer
        System.out.println("[AudioManager] stopMusic");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SFX stubs
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Play a one-shot sound effect.
     * @param resourcePath classpath path (e.g. "/audio/sfx/click.wav")
     */
    public void playSfx(String resourcePath) {
        // TODO: use MediaPlayer when javafx.media is on the classpath
        System.out.println("[AudioManager] playSfx: " + resourcePath);
    }

    /** Convenience: play the standard button-click SFX. */
    public void playClick() {
        playSfx("/audio/sfx/click.wav");
    }
}