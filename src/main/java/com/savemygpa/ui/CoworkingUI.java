package com.savemygpa.ui;

import com.savemygpa.config.GameConfig;
import com.savemygpa.config.StatConfig;
import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

public class CoworkingUI {

    private static final String POPUP_BG   = "/images/popup/popup_bg_black.png";
    private static final String BTN_STUDY  = "/images/popup/button_study_black.png";
    private static final String BTN_RELAX  = "/images/popup/button_relax_black.png";
    private static final String BTN_CANCEL = "/images/popup/button_cancel_black.png";

    private static final double BTN_ACTION_W = 360;
    private static final double BTN_CANCEL_W = 200;
    private static final double CARD_W       = 520;

    public interface Callbacks {
        void onRelax();
        void onStudy();
        void onBack();
    }

    private final Callbacks cb;
    private final StackPane sceneRoot;
    private boolean showing = false;
    private TooltipOverlay tooltip;

    public CoworkingUI(StackPane sceneRoot, Callbacks cb) {
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

        String studyTip =
                "⏱ ใช้เวลา " + GameConfig.REVIEW_TIME_COST + " ชั่วโมง\n\n" +
                        "✅ ได้รับ:\n" +
                        "   🧠 INT +" + StatConfig.REVIEW_LOW_INTELLIGENCE_GAIN + "~" + StatConfig.REVIEW_HIGH_INTELLIGENCE_GAIN + "\n\n" +
                        "❌ เสียไป:\n" +
                        "   ⚡ Energy -" + StatConfig.REVIEW_ENERGY_LOSS + "\n" +
                        "   😊 Mood -"   + StatConfig.REVIEW_MOOD_LOSS + "\n\n" +
                        "🔒 ต้องการ:\n" +
                        "   ⚡ Energy ≥ " + StatConfig.REVIEW_ENERGY_REQUIREMENT + "\n" +
                        "   😊 Mood ≥ "   + StatConfig.REVIEW_MOOD_REQUIREMENT;

        String relaxTip =
                "⏱ ใช้เวลา " + GameConfig.RELAX_TIME_COST + " ชั่วโมง\n\n" +
                        "✅ ได้รับ:\n" +
                        "   ⚡ Energy +" + StatConfig.RELAX_ENERGY_GAIN + "\n" +
                        "   😊 Mood +"   + StatConfig.RELAX_MOOD_GAIN + "\n\n" +
                        "❌ เสียไป: ไม่มี\n\n" +
                        "🔒 ต้องการ: ไม่มีเงื่อนไข";

        ImageView studyBtn  = makeBtn(BTN_STUDY,  BTN_ACTION_W, "📖 Review (ทบทวน)", studyTip, () -> dismiss(overlay, cb::onStudy));
        ImageView relaxBtn  = makeBtn(BTN_RELAX,  BTN_ACTION_W, "😌 Relax (พักผ่อน)", relaxTip, () -> dismiss(overlay, cb::onRelax));
        ImageView cancelBtn = makeBtn(BTN_CANCEL, BTN_CANCEL_W, null, null,            () -> dismiss(overlay, cb::onBack));

        VBox btnStack = new VBox(16, studyBtn, relaxBtn, cancelBtn);
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

    private ImageView loadImg(String path) {
        var url = getClass().getResource(path);
        if (url == null) throw new IllegalStateException("Missing resource: " + path);
        return new ImageView(new Image(url.toExternalForm()));
    }
}