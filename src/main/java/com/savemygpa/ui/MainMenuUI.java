package com.savemygpa.ui;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

public class MainMenuUI {

    public interface Callbacks {
        void onContinue();
        void onNewGame();
        void onHowToPlay();
        void onSettings();
        void onCredits();
        void onQuit();
    }

    private final boolean hasSavedGame;
    private final Callbacks cb;

    private static final String BG_PATH      = "/images/menu/menu_no_logo.jpg";
    private static final String LOGO_PATH    = "/images/menu/logo.png";
    private static final String BTN_START    = "/images/menu/menu_start.png";
    private static final String BTN_START2   = "/images/menu/menu_start2.png";
    private static final String BTN_CONTINUE = "/images/menu/menu_continue2.png";
    private static final String BTN_HOW      = "/images/menu/menu_how.png";
    private static final String BTN_SETTING  = "/images/menu/menu_setting.png";
    private static final String BTN_CREDIT   = "/images/menu/menu_credit.png";
    private static final String BTN_QUIT     = "/images/menu/menu_quit.png";

    private static final double BTN_WIDTH  = 420;
    private static final double LOGO_WIDTH = 1306;


    public MainMenuUI(boolean hasSavedGame, Callbacks cb) {
        this.hasSavedGame = hasSavedGame;
        this.cb = cb;
    }

    public StackPane buildView() {
        StackPane root = new StackPane();
        root.setPrefSize(1920, 1080);

        // ── Background ────────────────────────────────────────────────────────
        ImageView bg = new ImageView(load(BG_PATH));
        bg.setFitWidth(1920);
        bg.setFitHeight(1080);
        bg.setPreserveRatio(false);
        bg.setMouseTransparent(true);

        // ── Logo — sits above the button column ───────────────────────────────
        ImageView logo = buildLogo();

        // ── Button column ─────────────────────────────────────────────────────
        VBox col = new VBox(8);
        col.setAlignment(Pos.CENTER);

        java.util.List<ImageView> btns = new java.util.ArrayList<>();
        if (hasSavedGame) btns.add(makeImgBtn(BTN_CONTINUE, cb::onContinue));
        if (hasSavedGame) btns.add(makeImgBtn(BTN_START2, cb::onNewGame));
        else btns.add(makeImgBtn(BTN_START, cb::onNewGame));
        btns.add(makeImgBtn(BTN_HOW,     cb::onHowToPlay));
        btns.add(makeImgBtn(BTN_SETTING, cb::onSettings));
        btns.add(makeImgBtn(BTN_CREDIT,  cb::onCredits));
        btns.add(makeImgBtn(BTN_QUIT,    cb::onQuit));

        for (int i = 0; i < btns.size(); i++) {
            ImageView iv = btns.get(i);
            iv.setOpacity(0);
            iv.setTranslateY(40);
            col.getChildren().add(iv);

            int delay = 80 + i * 70;
            FadeTransition ft = new FadeTransition(Duration.millis(400), iv);
            ft.setToValue(1);
            ft.setDelay(Duration.millis(delay));

            TranslateTransition tt = new TranslateTransition(Duration.millis(400), iv);
            tt.setToY(0);
            tt.setDelay(Duration.millis(delay));

            ft.play();
            tt.play();
        }

        // ── Vertical layout: logo on top, buttons below ───────────────────────
        VBox layout = new VBox(18, logo, col);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(10));

        root.getChildren().addAll(bg, layout);
        return root;
    }

    // ── Logo with wiggle animation (like AcceptanceUI) ────────────────────────

    private ImageView buildLogo() {
        ImageView logo = new ImageView(load(LOGO_PATH));
        logo.setFitWidth(LOGO_WIDTH);
        logo.setPreserveRatio(true);

        // Start invisible, fade+slide in before wiggle begins
        logo.setOpacity(0);
        logo.setTranslateY(-50);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), logo);
        fadeIn.setToValue(1);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(500), logo);
        slideIn.setToY(0);

        ParallelTransition entrance = new ParallelTransition(fadeIn, slideIn);
        entrance.setOnFinished(e -> startLogoWiggle(logo));
        entrance.play();

        return logo;
    }

    private void startLogoWiggle(ImageView logo) {
        Timeline wiggle = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(logo.rotateProperty(), -3, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(480),
                        new KeyValue(logo.rotateProperty(),  3, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(960),
                        new KeyValue(logo.rotateProperty(), -3, Interpolator.EASE_BOTH))
        );
        wiggle.setCycleCount(Animation.INDEFINITE);
        wiggle.play();

        // Subtle vertical bob layered on top of the wiggle
        Timeline bob = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(logo.translateYProperty(),  0, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(700),
                        new KeyValue(logo.translateYProperty(), -8, Interpolator.EASE_BOTH))
        );
        bob.setAutoReverse(true);
        bob.setCycleCount(Animation.INDEFINITE);
        bob.play();
    }

    // ── Image button with hover + press effects ────────────────────────────────

    private ImageView makeImgBtn(String path, Runnable onClick) {
        ImageView iv = new ImageView(load(path));
        iv.setFitWidth(BTN_WIDTH);
        iv.setPreserveRatio(true);
        iv.setCursor(javafx.scene.Cursor.HAND);

        iv.setOnMouseEntered(e -> { iv.setOpacity(0.82); iv.setScaleX(1.04); iv.setScaleY(1.04); });
        iv.setOnMouseExited (e -> { iv.setOpacity(1.00); iv.setScaleX(1.0);  iv.setScaleY(1.0);  });
        iv.setOnMousePressed (e -> { iv.setScaleX(0.95); iv.setScaleY(0.95); });
        iv.setOnMouseReleased(e -> {
            iv.setScaleX(1.0); iv.setScaleY(1.0);
            onClick.run();
        });

        return iv;
    }

    private Image load(String path) {
        var url = getClass().getResource(path);
        if (url == null) throw new IllegalStateException("Missing resource: " + path);
        return new Image(url.toExternalForm());
    }
}