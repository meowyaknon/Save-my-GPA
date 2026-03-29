package com.savemygpa.ui;

import com.savemygpa.audio.AudioManager;
import com.savemygpa.config.GameConfig;
import com.savemygpa.config.StatConfig;
import com.savemygpa.util.GameCallbacks;
import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

public class BusStopUI {

    private static final String POPUP_BG   = "/images/popup/popup_bg_black.png";
    private static final String BTN_KLLC   = "/images/popup/button_kllc_black.png";
    private static final String BTN_HOME   = "/images/popup/button_home_black.png";
    private static final String BTN_CANCEL = "/images/popup/button_cancel_black.png";

    private static final double BTN_ACTION_W = 360;
    private static final double BTN_CANCEL_W = 200;
    private static final double CARD_W       = 520;

    private final GameCallbacks cb;
    private final StackPane     sceneRoot;
    private boolean showing = false;
    private TooltipOverlay tooltip;

    // ── Constructor now takes GameCallbacks ───────────────────────────────────
    public BusStopUI(StackPane sceneRoot, GameCallbacks cb) {
        this.sceneRoot = sceneRoot;
        this.cb = cb;
    }

    public void show() {
        if (showing) return;
        showing = true;

        tooltip = new TooltipOverlay(sceneRoot);

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.55);");
        overlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        overlay.prefWidthProperty().bind(sceneRoot.widthProperty());
        overlay.prefHeightProperty().bind(sceneRoot.heightProperty());

        StackPane card = buildCard(overlay);

        Scale cardScale = new Scale(1, 1);
        cardScale.pivotXProperty().bind(Bindings.divide(card.widthProperty(), 2));
        cardScale.pivotYProperty().bind(Bindings.divide(card.heightProperty(), 2));
        cardScale.xProperty().bind(Bindings.createDoubleBinding(
                () -> Math.min(sceneRoot.getWidth() / 1920.0, sceneRoot.getHeight() / 1080.0),
                sceneRoot.widthProperty(), sceneRoot.heightProperty()));
        cardScale.yProperty().bind(cardScale.xProperty());
        card.getTransforms().add(cardScale);

        overlay.getChildren().add(card);
        sceneRoot.getChildren().add(overlay);

        card.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(220), card);
        ft.setToValue(1);
        ft.play();
    }

    private StackPane buildCard(StackPane overlay) {
        ImageView bg = loadImg(POPUP_BG);
        bg.setFitWidth(CARD_W);
        bg.setPreserveRatio(true);

        String kllcTip =
                "⏱ ใช้เวลา " + GameConfig.KLLC_TIME_COST + " ชั่วโมง\n\n" +
                        "✅ ได้รับ :\n" +
                        "-> 🧠 INT +" + StatConfig.KLLC_LOW_INTELLIGENCE_GAIN    + " | เมื่อ Mood น้อย" + "\n" +
                        "-> 🧠 INT +" + StatConfig.KLLC_MEDIUM_INTELLIGENCE_GAIN + " | เมื่อ Mood กลางๆ" + "\n" +
                        "-> 🧠 INT +" + StatConfig.KLLC_HIGH_INTELLIGENCE_GAIN   + " | เมื่อ Mood มาก" +"\n\n" +
                        "❌ เสียไป :\n" +
                        "-> ⚡ Energy -" + StatConfig.KLLC_ENERGY_LOSS + "\n" +
                        "-> 😊 Mood -"   + StatConfig.KLLC_MOOD_LOSS + "\n\n" +
                        "🔒 ต้องการ :\n" +
                        "-> ⚡ Energy " + StatConfig.KLLC_ENERGY_REQUIREMENT + "\n" +
                        "-> 😊 Mood "   + StatConfig.KLLC_MOOD_REQUIREMENT;

        String homeTip =
                "⚠️ เมื่อคลิกจะจบวันนี้ทันที !\n\n" +
                        "✅ ได้รับ:\n" +
                        "-> ⚡ Energy มหาศาล\n" +
                        "-> 😊 Mood มหาศาล\n" +
                        "(ขึ้นอยู่กับเวลาที่เหลือและ Mood ในวันนั้นๆ)";

        ImageView kllcBtn   = makeBtn(BTN_KLLC,   BTN_ACTION_W, "🏛 KLLC", kllcTip, () -> dismiss(overlay, cb::onKLLC));
        ImageView homeBtn   = makeBtn(BTN_HOME,   BTN_ACTION_W, "🏠 กลับบ้าน",     homeTip, () -> dismiss(overlay, cb::onGoHome));
        ImageView cancelBtn = makeBtn(BTN_CANCEL, BTN_CANCEL_W, null,               null,    () -> dismissNoCallback(overlay));

        VBox btnStack = new VBox(16, kllcBtn, homeBtn, cancelBtn);
        btnStack.setAlignment(Pos.CENTER);
        btnStack.setStyle("-fx-padding: 48 40 36 40;");

        StackPane card = new StackPane(bg, btnStack);
        card.setMaxWidth(CARD_W);
        return card;
    }

    private ImageView makeBtn(String path, double width,
                              String tipTitle, String tipBody,
                              Runnable onClick) {
        ImageView iv = loadImg(path);
        iv.setFitWidth(width);
        iv.setPreserveRatio(true);
        iv.setCursor(javafx.scene.Cursor.HAND);
        iv.setOnMouseEntered(e -> {
            iv.setScaleX(1.05); iv.setScaleY(1.05); iv.setOpacity(0.88);
            if (tipTitle != null && tooltip != null) tooltip.show(tipTitle, tipBody);
        });
        iv.setOnMouseExited(e -> {
            iv.setScaleX(1.0); iv.setScaleY(1.0); iv.setOpacity(1.0);
            if (tooltip != null) tooltip.hide();
        });
        iv.setOnMousePressed(e  -> { iv.setScaleX(0.95); iv.setScaleY(0.95); e.consume(); });
        iv.setOnMouseReleased(e -> { iv.setScaleX(1.0);  iv.setScaleY(1.0); });
        iv.setOnMouseClicked(e  -> { e.consume(); onClick.run(); });
        return iv;
    }

    private void dismiss(StackPane overlay, Runnable callback) {
        if (tooltip != null) { tooltip.dispose(); tooltip = null; }
        overlay.prefWidthProperty().unbind();
        overlay.prefHeightProperty().unbind();
        FadeTransition ft = new FadeTransition(Duration.millis(150), overlay);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            sceneRoot.getChildren().remove(overlay);
            showing = false;
            if (callback != null) callback.run();
        });
        ft.play();
    }

    private void dismissNoCallback(StackPane overlay) {
        AudioManager.getInstance().playRefuse();
        if (tooltip != null) { tooltip.dispose(); tooltip = null; }
        overlay.prefWidthProperty().unbind();
        overlay.prefHeightProperty().unbind();
        sceneRoot.getChildren().remove(overlay);
        showing = false;
    }

    private ImageView loadImg(String path) {
        var url = getClass().getResource(path);
        if (url == null) throw new IllegalStateException("Missing resource: " + path);
        return new ImageView(new Image(url.toExternalForm()));
    }
}