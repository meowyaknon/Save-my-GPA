package com.savemygpa.ui;

import com.savemygpa.config.GameConfig;
import com.savemygpa.config.StatConfig;
import com.savemygpa.util.GameCallbacks;
import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class InsideUI {

    private static final String IT_BG         = "/images/map/inside_it/map_inside.jpg";
    private static final String BTN_CLASSROOM  = "/images/map/inside_it/classroom.png";
    private static final String BTN_AUDITORIUM = "/images/map/inside_it/auditorium.png";
    private static final String BTN_COMMON     = "/images/map/inside_it/common.png";
    private static final String BTN_BACK_IMG   = "/images/menu/back_button.png";

    private static final String GLOW_CLASSROOM  = "#4fc3f7";
    private static final String GLOW_AUDITORIUM = "#ce93d8";
    private static final String GLOW_COMMON     = "#80cbc4";
    private static final String GLOW_EXAM       = "#ef9a9a";

    private static final double CLASSROOM_X  = 13,   CLASSROOM_Y  = 0,   CLASSROOM_W  = 545;
    private static final double AUDITORIUM_X = 1250, AUDITORIUM_Y = 0,   AUDITORIUM_W = 685;
    private static final double COMMON_X     = 870,  COMMON_Y     = 708, COMMON_W     = 1080;
    private static final double BACK_W       = 330,  BACK_RIGHT   = 20,  BACK_TOP     = 560;

    private final GameCallbacks cb;
    private TooltipOverlay tooltip;

    // ── Constructor now takes GameCallbacks ───────────────────────────────────
    public InsideUI(GameCallbacks cb) { this.cb = cb; }

    // =========================================================================
    // Build
    // =========================================================================
    public StackPane buildView() {
        StackPane root = new StackPane();
        root.setPrefSize(1920, 1080);
        root.setMaxSize(1920, 1080);

        var bgUrl = getClass().getResource(IT_BG);
        if (bgUrl != null) {
            ImageView bg = new ImageView(new javafx.scene.image.Image(bgUrl.toExternalForm()));
            bg.setFitWidth(1920); bg.setFitHeight(1080);
            bg.setPreserveRatio(false); bg.setMouseTransparent(true);
            root.getChildren().add(bg);
        }

        AnchorPane gameLayer = new AnchorPane();
        gameLayer.setMaxSize(1920, 1080); gameLayer.setPrefSize(1920, 1080);

        AnchorPane hudLayer = new AnchorPane();
        hudLayer.setMaxSize(1920, 1080); hudLayer.setPrefSize(1920, 1080);
        hudLayer.setMouseTransparent(true);

        tooltip = new TooltipOverlay(root);

        String classroomTip =
                "⏱ ใช้เวลา " + GameConfig.CLASSROOM_TIME_COST + " ชั่วโมง\n\n" +
                        "✅ ได้รับ:\n" +
                        "   🧠 INT +" + StatConfig.CLASSROOM_LOW_INTELLIGENCE_GAIN + "~" + StatConfig.CLASSROOM_HIGH_INTELLIGENCE_GAIN + "\n\n" +
                        "❌ เสียไป:\n" +
                        "   ⚡ Energy -" + StatConfig.CLASSROOM_ENERGY_LOSS + "\n" +
                        "   😊 Mood -"   + StatConfig.CLASSROOM_MOOD_LOSS + "\n\n" +
                        "🔒 ต้องการ:\n" +
                        "   ⚡ Energy ≥ " + StatConfig.CLASSROOM_ENERGY_REQUIREMENT + "\n" +
                        "   😊 Mood ≥ "   + StatConfig.CLASSROOM_MOOD_REQUIREMENT;

        String auditoriumTip =
                "⏱ ใช้เวลา " + GameConfig.AUDITORIUM_TIME_COST + " ชั่วโมง\n\n" +
                        "✅ ได้รับ:\n" +
                        "   😊 Mood +" + StatConfig.AUDITORIUM_MOOD_GAIN + "\n\n" +
                        "❌ เสียไป:\n" +
                        "   ⚡ Energy -" + StatConfig.AUDITORIUM_ENERGY_LOSS + "\n\n" +
                        "🔒 ต้องการ:\n" +
                        "   ⚡ Energy ≥ " + StatConfig.AUDITORIUM_ENERGY_REQUIREMENT;

        String coworkingTip =
                "เลือกกิจกรรมใน Coworking Space\n\n" +
                        "📖 Review (" + GameConfig.REVIEW_TIME_COST + " ชม.)\n" +
                        "   🧠 INT +" + StatConfig.REVIEW_LOW_INTELLIGENCE_GAIN + "~" + StatConfig.REVIEW_HIGH_INTELLIGENCE_GAIN +
                        "  ⚡-" + StatConfig.REVIEW_ENERGY_LOSS + "  😊-" + StatConfig.REVIEW_MOOD_LOSS + "\n\n" +
                        "😌 Relax (" + GameConfig.RELAX_TIME_COST + " ชม.)\n" +
                        "   ⚡ Energy +" + StatConfig.RELAX_ENERGY_GAIN + "  😊 Mood +" + StatConfig.RELAX_MOOD_GAIN;

        String progExamTip =
                "⏱ ใช้เวลา " + GameConfig.EXAM_TIME_COST + " ชั่วโมง\n\n" +
                        "🎮 Mini-game: Captcha challenge\n" +
                        "🧠 INT สูง = โอกาสคะแนนสูง\n\n" +
                        "⚠️ ทำได้ครั้งเดียวต่อวัน";

        String mathExamTip =
                "⏱ ใช้เวลา " + GameConfig.EXAM_TIME_COST + " ชั่วโมง\n\n" +
                        "🔢 Mini-game: Counting challenge\n" +
                        "🧠 INT สูง = โอกาสคะแนนสูง\n\n" +
                        "⚠️ ทำได้ครั้งเดียวต่อวัน";

        if (cb.isProgExamDay()) {
            addRoomButtons(gameLayer,
                    BTN_CLASSROOM,  CLASSROOM_W,  GLOW_EXAM,       CLASSROOM_X,  CLASSROOM_Y,  cb::onProgExam,   "📝 Programming Exam", progExamTip,
                    BTN_AUDITORIUM, AUDITORIUM_W, GLOW_AUDITORIUM, AUDITORIUM_X, AUDITORIUM_Y, cb::onAuditorium, "🎭 Auditorium",        auditoriumTip,
                    BTN_COMMON,     COMMON_W,     GLOW_COMMON,     COMMON_X,     COMMON_Y,     cb::onCoworking,  "☕ Coworking Space",   coworkingTip);
        } else if (cb.isMathExamDay()) {
            addRoomButtons(gameLayer,
                    BTN_CLASSROOM,  CLASSROOM_W,  GLOW_EXAM,       CLASSROOM_X,  CLASSROOM_Y,  cb::onMathExam,   "📐 Math Exam",         mathExamTip,
                    BTN_AUDITORIUM, AUDITORIUM_W, GLOW_AUDITORIUM, AUDITORIUM_X, AUDITORIUM_Y, cb::onAuditorium, "🎭 Auditorium",        auditoriumTip,
                    BTN_COMMON,     COMMON_W,     GLOW_COMMON,     COMMON_X,     COMMON_Y,     cb::onCoworking,  "☕ Coworking Space",   coworkingTip);
        } else {
            addRoomButtons(gameLayer,
                    BTN_CLASSROOM,  CLASSROOM_W,  GLOW_CLASSROOM,  CLASSROOM_X,  CLASSROOM_Y,  cb::onClassroom,  "🏫 Classroom",         classroomTip,
                    BTN_AUDITORIUM, AUDITORIUM_W, GLOW_AUDITORIUM, AUDITORIUM_X, AUDITORIUM_Y, cb::onAuditorium, "🎭 Auditorium",        auditoriumTip,
                    BTN_COMMON,     COMMON_W,     GLOW_COMMON,     COMMON_X,     COMMON_Y,     cb::onCoworking,  "☕ Coworking Space",   coworkingTip);
        }

        addBackButton(gameLayer);

        OutsideUI hud = cb.getOutsideUI();
        if (hud != null) {
            hud.buildHudNodes();
            hud.addHudToCanvas(hudLayer);
            hud.refresh();
        }

        root.getChildren().addAll(gameLayer, hudLayer);
        root.setFocusTraversable(true);
        root.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ESCAPE) cb.onPause(); });
        root.requestFocus();
        return root;
    }

    // =========================================================================
    // Room buttons
    // =========================================================================
    private void addRoomButtons(Pane gl,
                                String i1, double w1, String g1, double x1, double y1, Runnable a1, String t1, String tb1,
                                String i2, double w2, String g2, double x2, double y2, Runnable a2, String t2, String tb2,
                                String i3, double w3, String g3, double x3, double y3, Runnable a3, String t3, String tb3) {
        StackPane b1 = mapBtn(i1, w1, a1, t1, tb1);
        StackPane b2 = mapBtn(i2, w2, a2, t2, tb2);
        StackPane b3 = mapBtn(i3, w3, a3, t3, tb3);
        AnchorPane.setLeftAnchor(b1, x1); AnchorPane.setTopAnchor(b1, y1);
        AnchorPane.setLeftAnchor(b2, x2); AnchorPane.setTopAnchor(b2, y2);
        AnchorPane.setLeftAnchor(b3, x3); AnchorPane.setTopAnchor(b3, y3);
        gl.getChildren().addAll(b1, b2, b3);
    }

    // =========================================================================
    // Map button — pixel-perfect hit detection + tooltip
    // =========================================================================
    private StackPane mapBtn(String imgPath, double fitWidth, Runnable onClick,
                             String tipTitle, String tipBody) {
        var url = getClass().getResource(imgPath);
        if (url == null) throw new IllegalStateException("Missing resource: " + imgPath);
        javafx.scene.image.Image image = new javafx.scene.image.Image(url.toExternalForm());
        PixelReader reader = image.getPixelReader();

        ImageView iv = new ImageView(image);
        iv.setFitWidth(fitWidth); iv.setPreserveRatio(true);

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
                        new KeyValue(glow.radiusProperty(), 52),
                        new KeyValue(glow.spreadProperty(), 0.30)));
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);

        java.util.function.BiFunction<Double, Double, Boolean> isOpaque = (sx, sy) -> {
            javafx.geometry.Point2D local = iv.sceneToLocal(sx, sy);
            double lx = local.getX(), ly = local.getY();
            double ivW = iv.getBoundsInLocal().getWidth();
            double ivH = iv.getBoundsInLocal().getHeight();
            if (lx < 0 || ly < 0 || lx > ivW || ly > ivH) return false;
            int px = (int)(lx * image.getWidth()  / ivW);
            int py = (int)(ly * image.getHeight() / ivH);
            if (px < 0 || py < 0 || px >= (int)image.getWidth() || py >= (int)image.getHeight()) return false;
            return ((reader.getArgb(px, py) >> 24) & 0xff) > 10;
        };

        wrapper.setOnMouseMoved(e -> {
            if (isOpaque.apply(e.getSceneX(), e.getSceneY())) {
                if (iv.getEffect() == null) { iv.setEffect(glow); pulse.play(); }
                if (tooltip != null && tipTitle != null) tooltip.show(tipTitle, tipBody);
            } else {
                pulse.stop(); iv.setEffect(null);
                if (tooltip != null) tooltip.hide();
            }
        });
        wrapper.setOnMouseExited(e -> {
            pulse.stop(); iv.setEffect(null);
            if (tooltip != null) tooltip.hide();
        });
        wrapper.setOnMouseClicked(e -> {
            if (!isOpaque.apply(e.getSceneX(), e.getSceneY())) return;
            ColorAdjust flash = new ColorAdjust();
            flash.setBrightness(0.8);
            iv.setEffect(flash);
            Timeline fa = new Timeline(
                    new KeyFrame(Duration.ZERO,        new KeyValue(flash.brightnessProperty(), 0.8)),
                    new KeyFrame(Duration.millis(120), new KeyValue(flash.brightnessProperty(), 0.0)));
            fa.setOnFinished(ev -> { iv.setEffect(null); onClick.run(); });
            fa.play();
        });
        return wrapper;
    }

    // =========================================================================
    // Back button
    // =========================================================================
    private void addBackButton(Pane gameLayer) {
        var backUrl = getClass().getResource(BTN_BACK_IMG);
        if (backUrl != null) {
            StackPane backBtn = mapBtn(BTN_BACK_IMG, BACK_W, cb::onBack, null, null);
            AnchorPane.setRightAnchor(backBtn, BACK_RIGHT);
            AnchorPane.setTopAnchor(backBtn, BACK_TOP);
            gameLayer.getChildren().add(backBtn);
        } else {
            javafx.scene.control.Button back = new javafx.scene.control.Button("← กลับ");
            back.setStyle("-fx-font-family:'Comic Sans MS';-fx-font-size:18px;" +
                    "-fx-background-color:rgba(79,195,247,0.85);-fx-text-fill:#3b1a1a;" +
                    "-fx-font-weight:bold;-fx-background-radius:14;-fx-padding:10 28 10 28;-fx-cursor:hand;");
            back.setOnMouseEntered(e -> back.setOpacity(0.82));
            back.setOnMouseExited(e  -> back.setOpacity(1.00));
            back.setOnAction(e -> cb.onBack());
            AnchorPane.setRightAnchor(back, BACK_RIGHT);
            AnchorPane.setTopAnchor(back, BACK_TOP);
            gameLayer.getChildren().add(back);
        }
    }
}