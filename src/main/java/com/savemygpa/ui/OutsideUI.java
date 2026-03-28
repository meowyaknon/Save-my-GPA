package com.savemygpa.ui;

import com.savemygpa.activity.*;
import com.savemygpa.audio.AudioManager;
import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.EventManager;
import com.savemygpa.event.Location;
import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;
import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

import java.util.Random;

public class OutsideUI {

    private static final String BG          = "/images/map/outside_it/map_outside.jpg";
    private static final String BUS_IMG     = "/images/map/outside_it/bus.png";
    private static final String CANTEEN_IMG = "/images/map/outside_it/canteen.png";
    private static final String IT_IMG      = "/images/map/outside_it/it_building.png";

    private static final String PLAYER_NORMAL  = "/images/player/player_normal.png";
    private static final String PLAYER_BADMOOD = "/images/player/Player_badmood.png";
    private static final String PLAYER_TIRED   = "/images/player/player_tired.png";

    static final String[] CLOCK_IMGS = {
            "/images/clock/No_time.png",
            "/images/clock/1_left.png", "/images/clock/2_left.png",
            "/images/clock/3_left.png", "/images/clock/4_left.png",
            "/images/clock/5_left.png", "/images/clock/6_left.png",
            "/images/clock/7_left.png", "/images/clock/8_left.png",
            "/images/clock/9_left.png", "/images/clock/full_time.png",
    };

    private static final String[] IDLE_NORMAL   = { "วันนี้จะทำอะไรดีนะ?", "ต้องสู้ต่อไป!", "หิวข้าวแล้วล่ะ...", "อยากนอนจัง" };
    private static final String[] IDLE_BADMOOD  = { "อารมณ์แย่มากเลย...", "ทำไมทุกอย่างยากจัง", "ต้องการกำลังใจด่วน!", "ไปฟัง music ดีกว่า" };
    private static final String[] IDLE_TIRED    = { "ง่วงมากเลย...", "พลังงานหมดแล้ว", "ขอนอนสักงีบได้ไหม", "ไป Coworking พักก่อน" };
    private static final String[] IDLE_LOW_TIME = { "เวลาใกล้หมดแล้ว!", "รีบๆ หน่อยนะ!", "จะกลับบ้านยัง?" };
    static final String[] SUCCESS_LINES = { "เยี่ยมมาก! ทำได้แล้ว ✨", "สำเร็จ! เก่งมากเลย!", "ดีมาก สู้ต่อไปนะ!" };
    static final String[] FAIL_LINES    = { "ทำไม่ได้... ต้องเติม stats ก่อน", "ไม่ไหวแล้ว ต้องพักก่อน", "stats ไม่พอ ไปเติมก่อน!" };

    private final Player       player;
    private final TimeSystem   timeSystem;
    private final EventManager eventManager;
    private final Callbacks    cb;
    private final Random       rng = new Random();

    ImageView  playerSprite;
    Label      speechBubbleLabel;
    StackPane  speechBubble;
    Label      effectsLabel;
    ImageView  clockImage;
    Label      dayLabel;
    StatsBarUI statsBar;

    private Timeline idleTimer;

    private StackPane rootPane;

    public interface Callbacks {
        void onBusStop();
        void onCanteen();
        void onITBuilding();
        void onGoHome();
        void onPause();
        void showMessage(String title, String message);
        boolean doActivity(Activity activity, Location location);
    }

    public OutsideUI(Player player, TimeSystem timeSystem,
                     EventManager eventManager, Callbacks cb) {
        this.player       = player;
        this.timeSystem   = timeSystem;
        this.eventManager = eventManager;
        this.cb           = cb;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Build
    // ═════════════════════════════════════════════════════════════════════════

    public StackPane buildView() {
        AnchorPane canvas = new AnchorPane();
        canvas.setPrefSize(1920, 1080);

        Scale scale = new Scale();
        canvas.getTransforms().add(scale);

        ImageView bg = loadImg(BG);
        bg.fitWidthProperty().bind(canvas.widthProperty());
        bg.fitHeightProperty().bind(canvas.heightProperty());
        bg.setPreserveRatio(false); bg.setMouseTransparent(true);
        canvas.getChildren().add(bg);

        StackPane busBtn     = mapBtn(BUS_IMG,     295,  cb::onBusStop);
        StackPane itBtn      = mapBtn(IT_IMG,      1450, this::doITTransition);
        StackPane canteenBtn = mapBtn(CANTEEN_IMG, 765,  cb::onCanteen);
        AnchorPane.setLeftAnchor(busBtn, 172.0);
        AnchorPane.setTopAnchor(busBtn, 431.0);

        AnchorPane.setLeftAnchor(itBtn, 319.0);
        AnchorPane.setTopAnchor(itBtn, 0.0);

        AnchorPane.setLeftAnchor(canteenBtn, 1155.0);
        AnchorPane.setTopAnchor(canteenBtn, 786.0);
        canvas.getChildren().addAll(busBtn, itBtn, canteenBtn);

        buildHudNodes();
        addHudToCanvas(canvas);

        rootPane = new StackPane(canvas);

        rootPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            double scaleX = newVal.doubleValue() / 1920;
            double scaleY = rootPane.getHeight() / 1080;
            double scaleFactor = Math.min(scaleX, scaleY);
            scale.setX(scaleFactor);
            scale.setY(scaleFactor);
        });

        rootPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            double scaleX = rootPane.getWidth() / 1920;
            double scaleY = newVal.doubleValue() / 1080;
            double scaleFactor = Math.min(scaleX, scaleY);
            scale.setX(scaleFactor);
            scale.setY(scaleFactor);
        });

        rootPane.setFocusTraversable(true);
        rootPane.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ESCAPE && cb != null) cb.onPause(); });
        rootPane.requestFocus();

        refresh();
        scheduleIdleSpeech();
        return rootPane;
    }

    // ── IT Building transition ────────────────────────────────────────────────
    // SFX is played in mapBtn's onMousePressed so it fires instantly.
    // This method only handles the visual fade — no extra audio call needed here.
    private void doITTransition() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), rootPane);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> cb.onITBuilding());
        fadeOut.play();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // HUD
    // ═════════════════════════════════════════════════════════════════════════

    void buildHudNodes() {
        clockImage = new ImageView();
        clockImage.setFitWidth(150); clockImage.setFitHeight(150); clockImage.setPreserveRatio(true);

        dayLabel = new Label();
        dayLabel.setStyle("""
            -fx-font-family: 'Comic Sans MS';
            -fx-font-size: 30px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
            -fx-effect: dropshadow(gaussian, black, 4, 0.8, 0, 0);
        """);

        effectsLabel = new Label("✨ ไม่มี effect");
        effectsLabel.setWrapText(true); effectsLabel.setMaxWidth(580);
        effectsLabel.setStyle("""
            -fx-font-family: 'Comic Sans MS';
            -fx-font-size: 24px;
            -fx-text-fill: #ffe082;
            -fx-effect: dropshadow(gaussian, black, 3, 0.9, 0, 0);
        """);

        statsBar = new StatsBarUI();

        speechBubbleLabel = new Label("สวัสดี!");
        speechBubbleLabel.setWrapText(true);
        speechBubbleLabel.setMaxWidth(320);
        speechBubbleLabel.setStyle("""
            -fx-font-family: 'IBMplexSansThai-Regular', 'Comic Sans MS';
            -fx-font-size: 24px;
            -fx-text-fill: #ffffff;
            -fx-padding: 16 20 16 20;
            -fx-line-spacing: 3;
        """);
        speechBubble = new StackPane(speechBubbleLabel);
        speechBubble.setStyle("""
            -fx-background-color: rgba(0,0,0,0.60);
            -fx-background-radius: 40;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.50), 12, 0.5, 0, 3);
            -fx-border-color: rgba(255,255,255,0.18);
            -fx-border-radius: 20;
            -fx-border-width: 1.5;
        """);
        speechBubble.setMaxWidth(360);
        speechBubble.setOpacity(0);

        playerSprite = new ImageView();
        playerSprite.setFitHeight(470); playerSprite.setPreserveRatio(true);
    }

    void addHudToCanvas(Pane canvas) {
        VBox clockPanel = new VBox(4, clockImage, dayLabel);
        clockPanel.setAlignment(Pos.TOP_LEFT);
        AnchorPane.setTopAnchor(clockPanel, 18.0);
        AnchorPane.setLeftAnchor(clockPanel, 18.0);
        clockPanel.setMouseTransparent(true);

        VBox statsNode = statsBar.getNode();
        AnchorPane.setBottomAnchor(statsNode, 10.0);
        AnchorPane.setLeftAnchor(statsNode, 10.0);
        statsNode.setMouseTransparent(true);

        AnchorPane.setBottomAnchor(effectsLabel, 330.0);
        AnchorPane.setLeftAnchor(effectsLabel, 16.0);
        effectsLabel.setMouseTransparent(true);

        VBox charStack = new VBox(8, speechBubble, playerSprite);
        charStack.setAlignment(Pos.BOTTOM_CENTER);
        HBox charArea = new HBox(charStack);
        AnchorPane.setBottomAnchor(charArea, -20.0);
        AnchorPane.setLeftAnchor(charArea, 610.0);
        charArea.setMouseTransparent(true);

        canvas.getChildren().addAll(clockPanel, effectsLabel, statsNode, charArea);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Map button
    // ═════════════════════════════════════════════════════════════════════════

    private StackPane mapBtn(String imgPath, double fitWidth, Runnable onClick) {
        Image image = loadImgObj(imgPath);
        PixelReader reader = image.getPixelReader();

        ImageView iv = new ImageView(image);
        iv.setFitWidth(fitWidth);
        iv.setPreserveRatio(true);
        iv.setMouseTransparent(false);

        StackPane wrapper = new StackPane(iv);
        StackPane.setAlignment(iv, Pos.TOP_LEFT);
        wrapper.setPickOnBounds(false);
        wrapper.setCursor(javafx.scene.Cursor.HAND);

        DropShadow glow = new DropShadow(20, Color.LIGHTBLUE);
        Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(glow.radiusProperty(), 20),
                        new KeyValue(glow.spreadProperty(), 0.0)),
                new KeyFrame(Duration.millis(650),
                        new KeyValue(glow.radiusProperty(), 50),
                        new KeyValue(glow.spreadProperty(), 0.28))
        );
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);

        java.util.function.BiFunction<Double, Double, Boolean> isOpaque = (sceneX, sceneY) -> {
            javafx.geometry.Point2D local = iv.sceneToLocal(sceneX, sceneY);
            double lx = local.getX(), ly = local.getY();
            double ivW = iv.getBoundsInLocal().getWidth();
            double ivH = iv.getBoundsInLocal().getHeight();
            if (lx < 0 || ly < 0 || lx > ivW || ly > ivH) return false;
            double scaleX = image.getWidth()  / ivW;
            double scaleY = image.getHeight() / ivH;
            int px = (int)(lx * scaleX), py = (int)(ly * scaleY);
            if (px < 0 || py < 0 || px >= (int) image.getWidth() || py >= (int) image.getHeight()) return false;
            return ((reader.getArgb(px, py) >> 24) & 0xff) > 10;
        };

        wrapper.setOnMouseMoved(e -> {
            if (isOpaque.apply(e.getSceneX(), e.getSceneY())) {
                if (iv.getEffect() == null) { iv.setEffect(glow); pulse.play(); }
            } else {
                pulse.stop(); iv.setEffect(null);
            }
        });
        wrapper.setOnMouseExited(e -> { pulse.stop(); iv.setEffect(null); });

        // ── SFX fires immediately on press so there is zero perceived delay ───
        wrapper.setOnMousePressed(e -> {
            if (!isOpaque.apply(e.getSceneX(), e.getSceneY())) return;
            AudioManager.getInstance().playAccept();
        });

        wrapper.setOnMouseClicked(e -> {
            if (!isOpaque.apply(e.getSceneX(), e.getSceneY())) return;
            javafx.scene.effect.ColorAdjust flash = new javafx.scene.effect.ColorAdjust();
            flash.setBrightness(0.8);
            iv.setEffect(flash);
            Timeline flashAnim = new Timeline(
                    new KeyFrame(Duration.ZERO,        new KeyValue(flash.brightnessProperty(), 0.8)),
                    new KeyFrame(Duration.millis(120), new KeyValue(flash.brightnessProperty(), 0.0))
            );
            // onClick fires after the flash — the SFX has already started above
            flashAnim.setOnFinished(ev -> { iv.setEffect(null); onClick.run(); });
            flashAnim.play();
        });

        return wrapper;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Refresh
    // ═════════════════════════════════════════════════════════════════════════

    public void refresh() {
        updateClock(); updateEffects(); updateCharacterSprite();
        if (statsBar != null) statsBar.refresh(player);
    }

    private void updateClock() {
        int idx = Math.max(0, Math.min(10, 10 - timeSystem.getTimeLeft()));
        clockImage.setImage(loadImgObj(CLOCK_IMGS[idx]));
        dayLabel.setText("Day " + timeSystem.getCurrentDay());
    }

    private void updateEffects() {
        var effects = player.getActiveEffects();
        if (effects.isEmpty()) { effectsLabel.setText("✨ ไม่มี effect"); return; }
        StringBuilder sb = new StringBuilder();
        effects.forEach(ef -> sb.append("• ").append(ef.getName()).append("  "));
        effectsLabel.setText(sb.toString().trim());
    }

    private void updateCharacterSprite() {
        int mood = player.getStat(StatType.MOOD), energy = player.getStat(StatType.ENERGY);
        String path;
        if      (energy <= 3)  path = PLAYER_TIRED;
        else if (mood   <= 30) path = PLAYER_BADMOOD;
        else                   path = PLAYER_NORMAL;
        playerSprite.setImage(loadImgObj(path));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Speech bubble
    // ═════════════════════════════════════════════════════════════════════════

    public void say(String message) {
        cancelIdle();
        speechBubbleLabel.setText(message);
        speechBubble.setOpacity(1);
        FadeTransition out = new FadeTransition(Duration.seconds(0.6), speechBubble);
        out.setFromValue(1); out.setToValue(0);
        out.setDelay(Duration.seconds(4.2));
        out.setOnFinished(e -> scheduleIdleSpeech());
        out.play();
    }

    public void saySuccess()              { say(SUCCESS_LINES[rng.nextInt(SUCCESS_LINES.length)]); }
    public void sayFail()                 { say(FAIL_LINES[rng.nextInt(FAIL_LINES.length)]); }
    public void sayFail(String message)   { say("❌ " + message); }
    public void sayEvent(String desc)     { say("🎲 " + desc); }

    private void scheduleIdleSpeech() {
        cancelIdle();
        int delay = 8 + rng.nextInt(5);
        idleTimer = new Timeline(new KeyFrame(Duration.seconds(delay), e -> fireIdleSpeech()));
        idleTimer.play();
    }

    private void fireIdleSpeech() {
        String line = pickIdleLine();
        speechBubbleLabel.setText(line);
        FadeTransition fi = new FadeTransition(Duration.millis(300), speechBubble);
        fi.setFromValue(0); fi.setToValue(1); fi.play();
        FadeTransition fo = new FadeTransition(Duration.millis(500), speechBubble);
        fo.setFromValue(1); fo.setToValue(0);
        fo.setDelay(Duration.seconds(4.0));
        fo.setOnFinished(ev -> scheduleIdleSpeech());
        fo.play();
    }

    private void cancelIdle() {
        if (idleTimer != null) { idleTimer.stop(); idleTimer = null; }
        speechBubble.setOpacity(0);
    }

    private String pickIdleLine() {
        int mood = player.getStat(StatType.MOOD), energy = player.getStat(StatType.ENERGY);
        int tLeft = timeSystem.getTimeLeft();
        String[] pool;
        if      (tLeft  <= 2)  pool = IDLE_LOW_TIME;
        else if (energy <= 2)  pool = IDLE_TIRED;
        else if (mood   <= 25) pool = IDLE_BADMOOD;
        else                   pool = IDLE_NORMAL;
        return pool[rng.nextInt(pool.length)];
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Helpers
    // ═════════════════════════════════════════════════════════════════════════

    private ImageView loadImg(String path) { return new ImageView(loadImgObj(path)); }

    Image loadImgObj(String path) {
        var url = getClass().getResource(path);
        if (url == null) throw new IllegalStateException("Missing resource: " + path);
        return new Image(url.toExternalForm());
    }
}