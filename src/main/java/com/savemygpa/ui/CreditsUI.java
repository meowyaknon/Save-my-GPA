package com.savemygpa.ui;

import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.util.Duration;

public class CreditsUI {

    private static final String BG_PATH  = "/images/menu/menu_no_logo.jpg";
    private static final String CARD_BG  = "/images/popup/big_block_black.png";
    private static final String BTN_BACK = "/images/menu/backward.png";
    private static final double BTN_W    = 300;
    private static final double CARD_W   = 820;

    private final Runnable onBack;

    public CreditsUI(Runnable onBack) { this.onBack = onBack; }

    public StackPane buildView() {
        StackPane root = new StackPane();

        ImageView bg = loadImg(BG_PATH);
        bg.setFitWidth(1920); bg.setFitHeight(1080);
        bg.setPreserveRatio(false); bg.setMouseTransparent(true);

        // ── Card with image background ────────────────────────────────────────
        StackPane card = new StackPane();
        card.setMaxWidth(CARD_W);

        var cardBgUrl = getClass().getResource(CARD_BG);
        if (cardBgUrl != null) {
            ImageView cardBgIv = new ImageView(new Image(cardBgUrl.toExternalForm()));
            cardBgIv.setFitWidth(CARD_W);
            cardBgIv.setPreserveRatio(true);
            card.getChildren().add(cardBgIv);
        } else {
            card.setStyle("""
                -fx-background-color: rgba(0,0,0,0.82);
                -fx-background-radius: 24;
                -fx-min-width: 700; -fx-min-height: 420;
                -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.85),36,0.65,0,6);
            """);
        }

        // ── Inner content ─────────────────────────────────────────────────────
        VBox inner = new VBox(24);
        inner.setAlignment(Pos.CENTER);
        inner.setStyle("-fx-padding: 48 60 44 60;");

        Text title = new Text("👥  เครดิต");
        title.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 40));
        title.setFill(Color.web("#ce93d8"));
        title.setStyle("-fx-effect: dropshadow(gaussian,rgba(206,147,216,0.55),16,0.4,0,0);");

        javafx.scene.shape.Line sep = new javafx.scene.shape.Line(0, 0, 540, 0);
        sep.setStroke(Color.web("#ce93d8", 0.30)); sep.setStrokeWidth(1.5);

        Label body = new Label(
                "🎮  Save My GPA\n\n" +
                        "สร้างโดย SaveMyGPA Team\n\n" +
                        "ขอบคุณทุกคนที่เล่น! 🎉\n\n" +
                        "© 2024 SaveMyGPA Team"
        );
        body.setStyle("""
            -fx-font-family: 'Comic Sans MS';
            -fx-font-size: 24px;
            -fx-text-fill: #e8d8f8;
            -fx-line-spacing: 8;
        """);
        body.setTextAlignment(TextAlignment.CENTER); body.setWrapText(true);

        ImageView backBtn = makeImgBtn(BTN_BACK, BTN_W, onBack);

        inner.getChildren().addAll(title, sep, body, backBtn);
        StackPane.setAlignment(inner, Pos.CENTER);
        card.getChildren().add(inner);

        card.setOpacity(0);
        root.getChildren().addAll(bg, card);
        FadeTransition ft = new FadeTransition(Duration.millis(400), card);
        ft.setToValue(1); ft.play();

        return root;
    }

    private ImageView makeImgBtn(String path, double width, Runnable onClick) {
        var url = getClass().getResource(path);
        if (url == null) return new ImageView();
        ImageView iv = new ImageView(new Image(url.toExternalForm()));
        iv.setFitWidth(width); iv.setPreserveRatio(true); iv.setCursor(javafx.scene.Cursor.HAND);
        iv.setOnMouseEntered(e -> { iv.setOpacity(0.82); iv.setScaleX(1.04); iv.setScaleY(1.04); });
        iv.setOnMouseExited (e -> { iv.setOpacity(1.00); iv.setScaleX(1.0);  iv.setScaleY(1.0);  });
        iv.setOnMousePressed (e -> { iv.setScaleX(0.95); iv.setScaleY(0.95); });
        iv.setOnMouseReleased(e -> { iv.setScaleX(1.0);  iv.setScaleY(1.0);  onClick.run(); });
        return iv;
    }

    private ImageView loadImg(String path) {
        var url = getClass().getResource(path);
        if (url == null) throw new IllegalStateException("Missing resource: " + path);
        return new ImageView(new Image(url.toExternalForm()));
    }
}