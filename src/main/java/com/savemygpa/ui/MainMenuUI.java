package com.savemygpa.ui;

import com.savemygpa.audio.AudioManager;
import javafx.animation.*;
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

    private static final String BG_PATH      = "/images/menu/menu_bg.jpg";
    private static final String BTN_START    = "/images/menu/menu_start.png";
    private static final String BTN_CONTINUE = "/images/menu/menu_continue.png";
    private static final String BTN_HOW      = "/images/menu/menu_how.png";
    private static final String BTN_SETTING  = "/images/menu/menu_setting.png";
    private static final String BTN_CREDIT   = "/images/menu/menu_credit.png";
    private static final String BTN_QUIT     = "/images/menu/menu_quit.png";

    private static final double BTN_WIDTH = 420;

    private final boolean   hasSavedGame;
    private final Callbacks cb;

    public MainMenuUI(boolean hasSavedGame, Callbacks cb) {
        this.hasSavedGame = hasSavedGame;
        this.cb = cb;
    }

    public StackPane buildView() {
        StackPane root = new StackPane();

        // ── Background ────────────────────────────────────────────────────
        ImageView bg = new ImageView(load(BG_PATH));
        bg.setFitWidth(1920);
        bg.setFitHeight(1080);
        bg.setPreserveRatio(false);
        bg.setMouseTransparent(true);

        // ── Button column ─────────────────────────────────────────────────
        VBox col = new VBox(8);
        col.setAlignment(Pos.CENTER);
        col.setTranslateY(150);

        // Build ordered list of buttons with staggered fade+slide in
        java.util.List<ImageView> btns = new java.util.ArrayList<>();

        btns.add(makeImgBtn(BTN_START,   cb::onNewGame));

        if (hasSavedGame) {
            btns.add(makeImgBtn(BTN_CONTINUE, cb::onContinue));
        }

        btns.add(makeImgBtn(BTN_HOW,     cb::onHowToPlay));
        btns.add(makeImgBtn(BTN_SETTING, cb::onSettings));
        btns.add(makeImgBtn(BTN_CREDIT,  cb::onCredits));
        btns.add(makeImgBtn(BTN_QUIT,    cb::onQuit));

        for (int i = 0; i < btns.size(); i++) {
            ImageView iv = btns.get(i);
            iv.setOpacity(0);
            iv.setTranslateY(30);
            col.getChildren().add(iv);

            // Staggered entrance: fade + slide up
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

        root.getChildren().addAll(bg, col);
        return root;
    }

    // ── Image button with hover + press + bob effects ─────────────────────
    private ImageView makeImgBtn(String path, Runnable onClick) {
        ImageView iv = new ImageView(load(path));
        iv.setFitWidth(BTN_WIDTH);
        iv.setPreserveRatio(true);
        iv.setCursor(javafx.scene.Cursor.HAND);

        iv.setOnMouseEntered(e -> {
            iv.setTranslateY(0);
            iv.setOpacity(0.82);
            iv.setScaleX(1.04);
            iv.setScaleY(1.04);
        });
        iv.setOnMouseExited(e -> {
            iv.setOpacity(1.00);
            iv.setScaleX(1.0);
            iv.setScaleY(1.0);
        });
        iv.setOnMousePressed(e -> {
            iv.setScaleX(0.95);
            iv.setScaleY(0.95);
        });
        iv.setOnMouseReleased(e -> {
            iv.setScaleX(1.0);
            iv.setScaleY(1.0);
            AudioManager.getInstance().playClick();
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