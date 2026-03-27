package com.savemygpa.ui;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

/**
 * AcceptanceUI — agreement/refusal screen with fade-in entrance animation.
 * The whole screen fades in from black, then the buttons slide up into place.
 */
public class AcceptanceUI {

    private static final String BG_PATH     = "/images/acceptance/acceptance_page.jpg";
    private static final String ACCEPT_PATH = "/images/acceptance/accept_button.png";
    private static final String REFUSE_PATH = "/images/acceptance/refuse_button.png";

    public interface Callbacks {
        void onAccept();
        void onRefuse();
    }

    private final Callbacks cb;

    public AcceptanceUI(Callbacks cb) {
        this.cb = cb;
    }

    public StackPane buildView() {
        StackPane root = new StackPane();
        root.setOpacity(0);   // start invisible — we fade in below

        // Background
        ImageView bg = loadImg(BG_PATH);
        bg.setPreserveRatio(false);
        bg.setFitWidth(1920);
        bg.setFitHeight(1080);
        root.getChildren().add(bg);

        // Buttons — start hidden, will slide+fade in after the root fades in
        ImageView acceptBtn = makeButton(ACCEPT_PATH, 350, cb::onAccept);
        ImageView refuseBtn = makeButton(REFUSE_PATH, 350, cb::onRefuse);
        acceptBtn.setOpacity(0);
        refuseBtn.setOpacity(0);

        VBox buttons = new VBox(80, acceptBtn, refuseBtn);
        buttons.setAlignment(Pos.CENTER);
        buttons.setTranslateY(240);
        root.getChildren().add(buttons);

        // Phase 1: fade the whole screen in
        FadeTransition screenIn = new FadeTransition(Duration.millis(500), root);
        screenIn.setFromValue(0); screenIn.setToValue(1);
        screenIn.setOnFinished(e -> {
            // Phase 2: slide + fade each button in with stagger
            slideIn(acceptBtn, 0);
            slideIn(refuseBtn, 140);

            // Phase 3: start wiggle after buttons are visible
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

    /** Slide a button up from +20px and fade from 0→1. */
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
                new KeyFrame(Duration.millis(0),
                        new KeyValue(iv.rotateProperty(), -3, Interpolator.DISCRETE)
                ),
                new KeyFrame(Duration.millis(400),
                        new KeyValue(iv.rotateProperty(), 3, Interpolator.DISCRETE)
                ),
                new KeyFrame(Duration.millis(800),
                        new KeyValue(iv.rotateProperty(), -3, Interpolator.DISCRETE)
                )
        );

        wiggle.setCycleCount(Animation.INDEFINITE);
        wiggle.setDelay(Duration.millis(delayMs));
        wiggle.play();
    }

    private ImageView loadImg(String path) {
        var url = getClass().getResource(path);
        if (url == null) throw new IllegalStateException("Missing resource: " + path);
        return new ImageView(new javafx.scene.image.Image(url.toExternalForm()));
    }
}