package com.savemygpa.ui;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

public class PauseMenuUI {

    // ── Image paths ───────────────────────────────────────────────────────────
    private static final String BG_V      = "/images/popup/big_block_black_V.png";
    private static final String BTN_RESUME = "/images/menu/menu_continue.png";
    private static final String BTN_SETTINGS= "/images/menu/menu_setting.png";
    private static final String BTN_MENU  = "/images/menu/back_to_menu.png";

    private static final double BTN_W = 340;
    private static final double CARD_W = 420;

    public interface Callbacks {
        void onResume();
        void onSettings();
        void onMainMenu();
    }

    private final Callbacks cb;

    public PauseMenuUI(Callbacks cb) { this.cb = cb; }

    public StackPane buildView() {
        StackPane root = new StackPane();
        root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        // Semi-transparent dim overlay — game is still visible behind it
        root.setStyle("-fx-background-color: rgba(0,0,0,0.58);");

        // ── Card (vertical image bg + buttons stacked inside) ─────────────────
        StackPane card = new StackPane();
        card.setMaxWidth(CARD_W);

        var bgUrl = getClass().getResource(BG_V);
        if (bgUrl != null) {
            ImageView bgIv = new ImageView(new Image(bgUrl.toExternalForm()));
            bgIv.setFitWidth(CARD_W);
            bgIv.setPreserveRatio(true);
            card.getChildren().add(bgIv);
        } else {
            // Fallback plain dark card if image is missing
            card.setStyle("""
                -fx-background-color: rgba(6,10,28,0.92);
                -fx-background-radius: 22;
                -fx-min-width: 420; -fx-min-height: 520;
                -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.85),36,0.65,0,6);
            """);
        }

        // ── Button column inside the card ─────────────────────────────────────
        VBox btnCol = new VBox(20);
        btnCol.setAlignment(Pos.CENTER);
        // Top padding gives room for any title area in the image;
        // adjust if the image has a different internal layout.
        btnCol.setStyle("-fx-padding: 60 30 50 30;");

        ImageView resumeBtn   = makeBtn(BTN_RESUME,   BTN_W, cb::onResume);
        ImageView settingsBtn = makeBtn(BTN_SETTINGS, BTN_W, cb::onSettings);
        ImageView menuBtn     = makeBtn(BTN_MENU,     BTN_W, cb::onMainMenu);

        btnCol.getChildren().addAll(resumeBtn, settingsBtn, menuBtn);
        StackPane.setAlignment(btnCol, Pos.CENTER);
        card.getChildren().add(btnCol);

        root.getChildren().add(card);

        // ESC closes the pause menu (resume)
        root.setFocusTraversable(true);
        root.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) cb.onResume();
        });

        // ── Entrance animation ────────────────────────────────────────────────
        card.setScaleX(0.88); card.setScaleY(0.88); card.setOpacity(0);
        ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
        st.setToX(1); st.setToY(1);
        FadeTransition ft = new FadeTransition(Duration.millis(200), card);
        ft.setToValue(1);
        st.play(); ft.play();

        root.requestFocus();
        return root;
    }

    // ── Image button factory ──────────────────────────────────────────────────

    private ImageView makeBtn(String path, double width, Runnable onClick) {
        var url = getClass().getResource(path);
        if (url == null) {
            // Invisible placeholder — caller should supply the asset
            ImageView placeholder = new ImageView();
            placeholder.setFitWidth(width);
            placeholder.setFitHeight(60);
            return placeholder;
        }
        ImageView iv = new ImageView(new Image(url.toExternalForm()));
        iv.setFitWidth(width);
        iv.setPreserveRatio(true);
        iv.setCursor(javafx.scene.Cursor.HAND);

        iv.setOnMouseEntered(e -> { iv.setScaleX(1.05); iv.setScaleY(1.05); iv.setOpacity(0.88); });
        iv.setOnMouseExited (e -> { iv.setScaleX(1.0);  iv.setScaleY(1.0);  iv.setOpacity(1.0);  });
        iv.setOnMousePressed (e -> { iv.setScaleX(0.95); iv.setScaleY(0.95); });
        iv.setOnMouseReleased(e -> { iv.setScaleX(1.0);  iv.setScaleY(1.0);  onClick.run(); });
        return iv;
    }
}