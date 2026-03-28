package com.savemygpa.ui;

import com.savemygpa.audio.AudioManager;
import com.savemygpa.util.GameCallbacks;
import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

public class PauseMenuUI {

    private static final String BG_V         = "/images/popup/big_block_black_V.png";
    private static final String BTN_RESUME   = "/images/menu/menu_continue.png";
    private static final String BTN_SETTINGS = "/images/menu/menu_setting.png";
    private static final String BTN_MENU     = "/images/menu/back_to_menu.png";

    private static final double BTN_W  = 340;
    private static final double CARD_W = 420;

    private final GameCallbacks cb;
    private final StackPane     sceneRoot;

    // ── Constructor now takes GameCallbacks ───────────────────────────────────
    public PauseMenuUI(StackPane sceneRoot, GameCallbacks cb) {
        this.sceneRoot = sceneRoot;
        this.cb        = cb;
    }

    public StackPane buildView() {
        StackPane overlay = new StackPane();
        overlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.58);");
        overlay.prefWidthProperty().bind(sceneRoot.widthProperty());
        overlay.prefHeightProperty().bind(sceneRoot.heightProperty());

        StackPane card = new StackPane();
        card.setMaxWidth(CARD_W);

        var bgUrl = getClass().getResource(BG_V);
        if (bgUrl != null) {
            ImageView bgIv = new ImageView(new Image(bgUrl.toExternalForm()));
            bgIv.setFitWidth(CARD_W);
            bgIv.setPreserveRatio(true);
            card.getChildren().add(bgIv);
        } else {
            card.setStyle("""
                -fx-background-color: rgba(6,10,28,0.92);
                -fx-background-radius: 22;
                -fx-min-width: 420; -fx-min-height: 520;
                -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.85),36,0.65,0,6);
            """);
        }

        VBox btnCol = new VBox(20);
        btnCol.setAlignment(Pos.CENTER);
        btnCol.setStyle("-fx-padding: 60 30 50 30;");
        btnCol.getChildren().addAll(
                makeBtn(BTN_RESUME,   BTN_W, true,  cb::onResume),
                makeBtn(BTN_SETTINGS, BTN_W, true,  cb::onSettings),
                makeBtn(BTN_MENU,     BTN_W, false, cb::onMainMenu)
        );
        StackPane.setAlignment(btnCol, Pos.CENTER);
        card.getChildren().add(btnCol);

        Scale cardScale = new Scale(1, 1);
        cardScale.pivotXProperty().bind(Bindings.divide(card.widthProperty(), 2));
        cardScale.pivotYProperty().bind(Bindings.divide(card.heightProperty(), 2));
        cardScale.xProperty().bind(Bindings.createDoubleBinding(
                () -> Math.min(sceneRoot.getWidth() / 1920.0, sceneRoot.getHeight() / 1080.0),
                sceneRoot.widthProperty(), sceneRoot.heightProperty()));
        cardScale.yProperty().bind(cardScale.xProperty());
        card.getTransforms().add(cardScale);

        overlay.getChildren().add(card);

        overlay.setFocusTraversable(true);
        overlay.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) cb.onResume();
        });

        card.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(200), card);
        ft.setToValue(1);
        ft.play();

        overlay.requestFocus();
        return overlay;
    }

    public static void dismiss(StackPane sceneRoot, StackPane overlay, Runnable onDone) {
        FadeTransition ft = new FadeTransition(Duration.millis(180), overlay);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            overlay.prefWidthProperty().unbind();
            overlay.prefHeightProperty().unbind();
            sceneRoot.getChildren().remove(overlay);
            if (onDone != null) onDone.run();
        });
        ft.play();
    }

    private ImageView makeBtn(String path, double width, boolean playAcceptSfx, Runnable onClick) {
        var url = getClass().getResource(path);
        if (url == null) {
            ImageView ph = new ImageView();
            ph.setFitWidth(width); ph.setFitHeight(60);
            return ph;
        }
        ImageView iv = new ImageView(new Image(url.toExternalForm()));
        iv.setFitWidth(width);
        iv.setPreserveRatio(true);
        iv.setCursor(javafx.scene.Cursor.HAND);
        iv.setOnMouseEntered(e  -> { iv.setScaleX(1.05); iv.setScaleY(1.05); iv.setOpacity(0.88); });
        iv.setOnMouseExited (e  -> { iv.setScaleX(1.0);  iv.setScaleY(1.0);  iv.setOpacity(1.0);  });
        iv.setOnMousePressed (e -> {
            if (playAcceptSfx) AudioManager.getInstance().playAccept();
            else               AudioManager.getInstance().playRefuse();
            iv.setScaleX(0.95); iv.setScaleY(0.95);
        });
        iv.setOnMouseReleased(e -> {
            iv.setScaleX(1.0); iv.setScaleY(1.0);
            onClick.run();
        });
        return iv;
    }
}