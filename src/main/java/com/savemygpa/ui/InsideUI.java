package com.savemygpa.ui;

import com.savemygpa.audio.AudioManager;
import com.savemygpa.core.TimeSystem;
import com.savemygpa.event.EventManager;
import com.savemygpa.player.Player;
import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

/**
 * InsideUI — IT Building interior.
 *
 * FIX: Pixel-perfect hit detection now converts mouse event coordinates from
 * the StackPane wrapper into ImageView local space via sceneToLocal() before
 * sampling the PixelReader.  The old code passed wrapper-local X/Y directly,
 * which was offset from the image when StackPane centred its child, causing
 * every click to land on a transparent pixel and silently do nothing.
 */
public class InsideUI {

    private static final String IT_BG         = "/images/map/inside_it/map_inside.jpg";
    private static final String BTN_CLASSROOM  = "/images/map/inside_it/classroom.png";
    private static final String BTN_AUDITORIUM = "/images/map/inside_it/auditorium.png";
    private static final String BTN_COMMON     = "/images/map/inside_it/common.png";

    private static final String GLOW_CLASSROOM  = "#4fc3f7";
    private static final String GLOW_AUDITORIUM = "#ce93d8";
    private static final String GLOW_COMMON     = "#80cbc4";
    private static final String GLOW_EXAM       = "#ef9a9a";

    private static final double CLASSROOM_X  =  60,  CLASSROOM_Y  = 20,  CLASSROOM_W  = 420;
    private static final double AUDITORIUM_X = 880,  AUDITORIUM_Y =  0,  AUDITORIUM_W = 480;
    private static final double COMMON_X     = 650,  COMMON_Y     = 520, COMMON_W     = 800;

    public interface Callbacks {
        void onClassroom();
        void onAuditorium();
        void onCoworking();
        void onProgExam();
        void onMathExam();
        void onBack();
        void onPause();
        boolean isProgExamDay();
        boolean isMathExamDay();
        Player       getPlayer();
        TimeSystem   getTimeSystem();
        EventManager getEventManager();
    }

    private final Callbacks cb;

    public InsideUI(Callbacks cb) {
        this.cb = cb;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Build
    // ═════════════════════════════════════════════════════════════════════════

    public StackPane buildView() {
        // Single root pane, fixed size, no Scale transform — identical to MainMenuUI
        StackPane root = new StackPane();
        root.setPrefSize(1920, 1080);
        root.setMaxSize(1920, 1080);   // ← THIS is the key line MainMenuUI effectively has
        //   because ImageView fills exactly 1920×1080

        // ── Background ────────────────────────────────────────────────────────
        var bgUrl = getClass().getResource(IT_BG);
        if (bgUrl != null) {
            ImageView bg = new ImageView(new Image(bgUrl.toExternalForm()));
            bg.setFitWidth(1920);
            bg.setFitHeight(1080);
            bg.setPreserveRatio(false);
            bg.setMouseTransparent(true);
            root.getChildren().add(bg);
        }

        // ── Game layer (AnchorPane for positioned buttons) ────────────────────
        AnchorPane gameLayer = new AnchorPane();
        gameLayer.setMaxSize(1920, 1080);
        gameLayer.setPrefSize(1920, 1080);
        gameLayer.setMouseTransparent(false);

        // ── HUD layer ─────────────────────────────────────────────────────────
        AnchorPane hudLayer = new AnchorPane();
        hudLayer.setMaxSize(1920, 1080);
        hudLayer.setPrefSize(1920, 1080);
        hudLayer.setMouseTransparent(true);

        // ── Room buttons ──────────────────────────────────────────────────────
        if (cb.isProgExamDay()) {
            addRoomButtons(gameLayer,
                    BTN_CLASSROOM,  CLASSROOM_W,  GLOW_EXAM,       CLASSROOM_X,  CLASSROOM_Y,  cb::onProgExam,
                    BTN_AUDITORIUM, AUDITORIUM_W, GLOW_AUDITORIUM, AUDITORIUM_X, AUDITORIUM_Y, cb::onAuditorium,
                    BTN_COMMON,     COMMON_W,     GLOW_COMMON,     COMMON_X,     COMMON_Y,     cb::onCoworking);
        } else if (cb.isMathExamDay()) {
            addRoomButtons(gameLayer,
                    BTN_CLASSROOM,  CLASSROOM_W,  GLOW_EXAM,       CLASSROOM_X,  CLASSROOM_Y,  cb::onMathExam,
                    BTN_AUDITORIUM, AUDITORIUM_W, GLOW_AUDITORIUM, AUDITORIUM_X, AUDITORIUM_Y, cb::onAuditorium,
                    BTN_COMMON,     COMMON_W,     GLOW_COMMON,     COMMON_X,     COMMON_Y,     cb::onCoworking);
        } else {
            addRoomButtons(gameLayer,
                    BTN_CLASSROOM,  CLASSROOM_W,  GLOW_CLASSROOM,  CLASSROOM_X,  CLASSROOM_Y,  cb::onClassroom,
                    BTN_AUDITORIUM, AUDITORIUM_W, GLOW_AUDITORIUM, AUDITORIUM_X, AUDITORIUM_Y, cb::onAuditorium,
                    BTN_COMMON,     COMMON_W,     GLOW_COMMON,     COMMON_X,     COMMON_Y,     cb::onCoworking);
        }

        // ── Back button ───────────────────────────────────────────────────────
        addBackButton(gameLayer);

        // ── HUD ───────────────────────────────────────────────────────────────
        OutsideUI hud = new OutsideUI(cb.getPlayer(), cb.getTimeSystem(), cb.getEventManager(), null);
        hud.buildHudNodes();
        hud.addHudToCanvas(hudLayer);
        hud.refresh();

        root.getChildren().addAll(gameLayer, hudLayer);

        // ── ESC ───────────────────────────────────────────────────────────────
        root.setFocusTraversable(true);
        root.setOnKeyPressed(e -> { if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) cb.onPause(); });
        root.requestFocus();

        return root;
    }

    private void addRoomButtons(Pane gameLayer,
                                String img1, double w1, String glow1, double x1, double y1, Runnable action1,
                                String img2, double w2, String glow2, double x2, double y2, Runnable action2,
                                String img3, double w3, String glow3, double x3, double y3, Runnable action3) {

        StackPane btn1 = mapBtn(img1, w1, glow1, action1);
        StackPane btn2 = mapBtn(img2, w2, glow2, action2);
        StackPane btn3 = mapBtn(img3, w3, glow3, action3);
        AnchorPane.setLeftAnchor(btn1, x1);
        AnchorPane.setTopAnchor(btn1, y1);

        AnchorPane.setLeftAnchor(btn2, x2);
        AnchorPane.setTopAnchor(btn2, y2);

        AnchorPane.setLeftAnchor(btn3, x3);
        AnchorPane.setTopAnchor(btn3, y3);
        gameLayer.getChildren().addAll(btn1, btn2, btn3);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Map button — FIXED pixel-perfect hit detection
    // ═════════════════════════════════════════════════════════════════════════

    private StackPane mapBtn(String imgPath, double fitWidth, String glowColor, Runnable onClick) {
        var url = getClass().getResource(imgPath);
        if (url == null) throw new IllegalStateException("Missing resource: " + imgPath);
        Image image = new Image(url.toExternalForm());
        PixelReader reader = image.getPixelReader();

        ImageView iv = new ImageView(image);
        iv.setFitWidth(fitWidth);
        iv.setPreserveRatio(true);
        iv.setMouseTransparent(false);

        StackPane wrapper = new StackPane(iv);
        StackPane.setAlignment(iv, Pos.TOP_LEFT);
        wrapper.setPickOnBounds(false);
        wrapper.setCursor(javafx.scene.Cursor.HAND);

        // Pulsing glow
        DropShadow glow = new DropShadow(20, Color.web(glowColor));
        Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(glow.radiusProperty(), 20),
                        new KeyValue(glow.spreadProperty(), 0.0)),
                new KeyFrame(Duration.millis(650),
                        new KeyValue(glow.radiusProperty(), 52),
                        new KeyValue(glow.spreadProperty(), 0.30))
        );
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);

        // Opaque-pixel test using scene coordinates → ImageView local coords
        java.util.function.BiFunction<Double, Double, Boolean> isOpaque = (sceneX, sceneY) -> {
            javafx.geometry.Point2D local = iv.sceneToLocal(sceneX, sceneY);
            double lx = local.getX(), ly = local.getY();
            double ivW = iv.getBoundsInLocal().getWidth();
            double ivH = iv.getBoundsInLocal().getHeight();
            if (lx < 0 || ly < 0 || lx > ivW || ly > ivH) return false;
            double sx = image.getWidth()  / ivW;
            double sy = image.getHeight() / ivH;
            int px = (int)(lx * sx), py = (int)(ly * sy);
            if (px < 0 || py < 0 || px >= (int) image.getWidth() || py >= (int) image.getHeight()) return false;
            return ((reader.getArgb(px, py) >> 24) & 0xff) > 10;
        };

        wrapper.setOnMouseMoved(e -> {
            if (isOpaque.apply(e.getSceneX(), e.getSceneY())) {
                if (iv.getEffect() == null) { iv.setEffect(glow); pulse.play(); }
            } else { pulse.stop(); iv.setEffect(null); }
        });
        wrapper.setOnMouseExited(e -> { pulse.stop(); iv.setEffect(null); });
        wrapper.setOnMouseClicked(e -> {
            if (!isOpaque.apply(e.getSceneX(), e.getSceneY())) return;
            ColorAdjust flash = new ColorAdjust();
            flash.setBrightness(0.8);
            iv.setEffect(flash);
            Timeline flashAnim = new Timeline(
                    new KeyFrame(Duration.ZERO,        new KeyValue(flash.brightnessProperty(), 0.8)),
                    new KeyFrame(Duration.millis(120), new KeyValue(flash.brightnessProperty(), 0.0))
            );
            flashAnim.setOnFinished(ev -> { iv.setEffect(null); onClick.run(); });
            flashAnim.play();
        });

        return wrapper;
    }

    // ── Back button ───────────────────────────────────────────────────────────

    private void addBackButton(Pane gameLayer) {
        String backImgPath = "/images/map/inside_it/button_back.png";
        var backUrl = getClass().getResource(backImgPath);
        if (backUrl != null) {
            StackPane backBtn = mapBtn(backImgPath, 200, "#4fc3f7", cb::onBack);
            AnchorPane.setLeftAnchor(backBtn, 24.0);
            AnchorPane.setBottomAnchor(backBtn, 20.0);
            gameLayer.getChildren().add(backBtn);
        } else {
            javafx.scene.control.Button back = new javafx.scene.control.Button("← กลับ");
            back.setStyle("""
                -fx-font-family: 'Comic Sans MS';
                -fx-font-size: 18px;
                -fx-background-color: rgba(79,195,247,0.85);
                -fx-text-fill: #0a1628;
                -fx-font-weight: bold;
                -fx-background-radius: 14;
                -fx-padding: 10 28 10 28;
                -fx-cursor: hand;
            """);
            back.setLayoutX(24);
            back.setLayoutY(1080 - 80);
            back.setOnMouseEntered(e -> back.setOpacity(0.82));
            back.setOnMouseExited(e  -> back.setOpacity(1.00));
            back.setOnAction(e -> cb.onBack());
            gameLayer.getChildren().add(back);
        }
    }
}