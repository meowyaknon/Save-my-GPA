package com.savemygpa.ui;

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

    // Natural (1920-px-space) sizes
    private static final double BTN_ACTION_W = 360;
    private static final double BTN_CANCEL_W = 200;
    private static final double CARD_W       = 520;

    public interface Callbacks {
        void onKLLC();
        void onGoHome();
        void onBack();
    }

    private final Callbacks cb;
    private final StackPane sceneRoot;
    private boolean showing = false;

    public BusStopUI(StackPane sceneRoot, Callbacks cb) {
        this.sceneRoot = sceneRoot;
        this.cb = cb;
    }

    public void show() {
        if (showing) return;
        showing = true;

        // ── Full-window dim overlay ───────────────────────────────────────────
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.55);");
        overlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        overlay.prefWidthProperty().bind(sceneRoot.widthProperty());
        overlay.prefHeightProperty().bind(sceneRoot.heightProperty());

        // ── Card at natural size ──────────────────────────────────────────────
        StackPane card = buildCard(overlay);

        // ── Scale card to match current window zoom ───────────────────────────
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

        // Entrance animation
        card.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(220), card);
        ft.setToValue(1);
        ft.play();
    }

    private StackPane buildCard(StackPane overlay) {
        ImageView bg = loadImg(POPUP_BG);
        bg.setFitWidth(CARD_W);
        bg.setPreserveRatio(true);

        ImageView kllcBtn   = makeBtn(BTN_KLLC,   BTN_ACTION_W, () -> dismiss(overlay, cb::onKLLC));
        ImageView homeBtn   = makeBtn(BTN_HOME,   BTN_ACTION_W, () -> dismiss(overlay, cb::onGoHome));
        ImageView cancelBtn = makeBtn(BTN_CANCEL, BTN_CANCEL_W, () -> dismiss(overlay, cb::onBack));

        VBox btnStack = new VBox(16, kllcBtn, homeBtn, cancelBtn);
        btnStack.setAlignment(Pos.CENTER);
        btnStack.setStyle("-fx-padding: 48 40 36 40;");

        StackPane card = new StackPane(bg, btnStack);
        card.setMaxWidth(CARD_W);
        return card;
    }

    private ImageView makeBtn(String path, double width, Runnable onClick) {
        ImageView iv = loadImg(path);
        iv.setFitWidth(width);
        iv.setPreserveRatio(true);
        iv.setCursor(javafx.scene.Cursor.HAND);
        iv.setOnMouseEntered(e  -> { iv.setScaleX(1.05); iv.setScaleY(1.05); iv.setOpacity(0.88); });
        iv.setOnMouseExited(e   -> { iv.setScaleX(1.0);  iv.setScaleY(1.0);  iv.setOpacity(1.0);  });
        iv.setOnMousePressed(e  -> { iv.setScaleX(0.95); iv.setScaleY(0.95); e.consume(); });
        iv.setOnMouseReleased(e -> { iv.setScaleX(1.0);  iv.setScaleY(1.0); });
        iv.setOnMouseClicked(e  -> { e.consume(); onClick.run(); });
        return iv;
    }

    private void dismiss(StackPane overlay, Runnable callback) {
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