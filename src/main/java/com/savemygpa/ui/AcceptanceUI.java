package com.savemygpa.ui;

import com.savemygpa.util.GameCallbacks;
import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

public class AcceptanceUI {

    private static final String BG_PATH     = "/images/acceptance/acceptance_page.jpg";
    private static final String ACCEPT_PATH = "/images/acceptance/accept_button.png";
    private static final String REFUSE_PATH = "/images/acceptance/refuse_button.png";

    private final GameCallbacks cb;

    // ── Constructor now takes GameCallbacks ───────────────────────────────────
    public AcceptanceUI(GameCallbacks cb) {
        this.cb = cb;
    }

    public StackPane buildView() {
        StackPane root = new StackPane();
        root.setOpacity(0);

        ImageView bg = loadImg(BG_PATH);
        bg.setPreserveRatio(false);
        bg.setFitWidth(1920);
        bg.setFitHeight(1080);
        root.getChildren().add(bg);

        ImageView acceptBtn = makeButton(ACCEPT_PATH, 350, cb::onAccept);
        ImageView refuseBtn = makeButton(REFUSE_PATH, 350, cb::onRefuse);
        acceptBtn.setOpacity(0);
        refuseBtn.setOpacity(0);

        VBox buttons = new VBox(80, acceptBtn, refuseBtn);
        buttons.setAlignment(Pos.CENTER);
        buttons.setTranslateY(240);
        root.getChildren().add(buttons);

        FadeTransition screenIn = new FadeTransition(Duration.millis(500), root);
        screenIn.setFromValue(0); screenIn.setToValue(1);
        screenIn.setOnFinished(e -> {
            slideIn(acceptBtn, 0);
            slideIn(refuseBtn, 140);

            PauseTransition bobDelay = new PauseTransition(Duration.millis(500));
            bobDelay.setOnFinished(ev -> {
                startWiggle(acceptBtn, 0);
                startWiggle(refuseBtn, 400);
            });
            bobDelay.play();
        });
        screenIn.play();

        return root;
    }

    private void slideIn(ImageView iv, int delayMs) {
        iv.setTranslateY(20);
        TranslateTransition tt = new TranslateTransition(Duration.millis(380), iv);
        tt.setToY(0);
        tt.setDelay(Duration.millis(delayMs));
        FadeTransition ft = new FadeTransition(Duration.millis(380), iv);
        ft.setToValue(1);
        ft.setDelay(Duration.millis(delayMs));
        tt.play(); ft.play();
    }

    private ImageView makeButton(String path, double fitWidth, Runnable onClick) {
        ImageView iv = loadImg(path);
        iv.setFitWidth(fitWidth);
        iv.setPreserveRatio(true);
        iv.setCursor(javafx.scene.Cursor.HAND);
        iv.setOnMouseEntered(e -> { iv.setScaleX(1.07); iv.setScaleY(1.07); iv.setOpacity(0.88); });
        iv.setOnMouseExited (e -> { iv.setScaleX(1.0);  iv.setScaleY(1.0);  iv.setOpacity(1.0);  });
        iv.setOnMousePressed (e -> { iv.setScaleX(0.94); iv.setScaleY(0.94); });
        iv.setOnMouseReleased(e -> { iv.setScaleX(1.0);  iv.setScaleY(1.0);  onClick.run(); });
        return iv;
    }

    private void startWiggle(ImageView iv, int delayMs) {
        Timeline wiggle = new Timeline(
                new KeyFrame(Duration.millis(0),   new KeyValue(iv.rotateProperty(), -3, Interpolator.DISCRETE)),
                new KeyFrame(Duration.millis(400), new KeyValue(iv.rotateProperty(),  3, Interpolator.DISCRETE)),
                new KeyFrame(Duration.millis(800), new KeyValue(iv.rotateProperty(), -3, Interpolator.DISCRETE))
        );
        wiggle.setCycleCount(Animation.INDEFINITE);
        wiggle.setDelay(Duration.millis(delayMs));
        wiggle.play();
    }

    private ImageView loadImg(String path) {
        var url = getClass().getResource(path);
        if (url == null) throw new IllegalStateException("Missing resource: " + path);
        return new ImageView(new Image(url.toExternalForm()));
    }
}