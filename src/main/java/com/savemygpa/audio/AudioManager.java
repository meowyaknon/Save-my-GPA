package com.savemygpa.audio;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

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
        // ── Counting Mini Game music ───────────────────────────────────────────
        public static final String COUNTING_ACTIVE = "counting_active";
        public static final String COUNTING_IDLE   = "counting_idle";
        // ── Captcha Mini Game music ────────────────────────────────────────────
        public static final String CAPTCHA_ACTIVE = "captcha_active";
        public static final String CAPTCHA_IDLE   = "captcha_idle";
        private Music() {}
    }

    // ── Named SFX constants ───────────────────────────────────────────────────
    public static final class Sfx {
        public static final String ACCEPT = "/audio/sfx/confirm.wav";
        public static final String REFUSE = "/audio/sfx/cancel.wav";
        public static final String TYPING = "/audio/sfx/typing.mp3";
        public static final String WRONG = "/audio/sfx/wrong.wav";
        // ── Counting Mini Game SFX ─────────────────────────────────────────────
        public static final String CORRECT    = "/audio/sfx/correct.mp3"; // use for both minigame
        public static final String EXPLOSION  = "/audio/sfx/explosion_4.wav";
        // ── Captcha Mini Game SFX ──────────────────────────────────────────────
        public static final String KEYBOARD_TYPING = "/audio/sfx/keyboard-typing-sound-effect.mp3";
        public static final String ERROR           = "/audio/sfx/error_CDOxCYm.mp3";
        public static final String ANSWER_WRONG    = "/audio/sfx/answer-wrong.mp3";
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
        // ── Counting Mini Game music ──
        MUSIC_PATHS.put(Music.COUNTING_ACTIVE, "/audio/music/Run! - Toby Fox (128k).wav");
        MUSIC_PATHS.put(Music.COUNTING_IDLE,   "/audio/music/Snowdin Town - Toby Fox (128k) (1).wav");
        // ── Captcha Mini Game music ───
        MUSIC_PATHS.put(Music.CAPTCHA_ACTIVE, "/audio/music/Death Report - Toby Fox (128k).wav");
        MUSIC_PATHS.put(Music.CAPTCHA_IDLE,   "/audio/music/Hotel - Toby Fox (128k).wav");
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

    public void playCorrect() { playSfx(Sfx.CORRECT); }

    public void playExplosion() { playSfx(Sfx.EXPLOSION); }

    public void playKeyboardTyping(){ playSfx(Sfx.KEYBOARD_TYPING);}

    public void playError() { playSfx(Sfx.ERROR);}

    public void playAnswerWrong() { playSfx(Sfx.ANSWER_WRONG);}

    // ── ดึง AudioClip ของ keyboard typing เพื่อ stop ได้จากภายนอก ─────────
    public javafx.scene.media.AudioClip getKeyboardClip() {
        try {
            var url = getClass().getResource(Sfx.KEYBOARD_TYPING);
            if (url == null) return null;
            AudioClip clip = sfxCache.computeIfAbsent(
                Sfx.KEYBOARD_TYPING, k -> new AudioClip(url.toExternalForm()));
            clip.setVolume(effectiveSfxVol());
            return clip;
        } catch (Exception e) {
            System.err.println("[AudioManager] Failed getKeyboardClip: " + e.getMessage());
            return null;
        }
    }
}