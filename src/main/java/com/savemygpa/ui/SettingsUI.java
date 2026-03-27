package com.savemygpa.ui;

import com.savemygpa.audio.AudioManager;
import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.util.Duration;

/**
 * SettingsUI — Settings page.
 *
 * CHANGE: The content card now uses /images/popup/big_block_black.png as a
 * horizontal image background (instead of a plain rgba rounded rectangle).
 * The menu background image remains unchanged.
 * Back button is still image-based (/images/menu/menu_quit.png).
 */
public class SettingsUI {

    private static final String BG_PATH    = "/images/menu/menu_no_logo.jpg";
    // CHANGE: horizontal block image for the card overlay
    private static final String CARD_BG    = "/images/popup/big_block_black.png";
    private static final String BTN_BACK   = "/images/menu/menu_quit.png";
    private static final double BTN_W      = 300;
    private static final double CARD_W     = 820;

    private final AudioManager audio;
    private final Callbacks    cb;

    public interface Callbacks { void onBack(); }

    public SettingsUI(AudioManager audio, Callbacks cb) {
        this.audio = audio; this.cb = cb;
    }

    public StackPane buildView() {
        StackPane root = new StackPane();

        // ── Full-screen background (main menu art) ────────────────────────────
        ImageView bg = loadImg(BG_PATH);
        bg.setFitWidth(1920); bg.setFitHeight(1080);
        bg.setPreserveRatio(false); bg.setMouseTransparent(true);

        // ── Content card ──────────────────────────────────────────────────────
        // CHANGE: replace dark rgba card with big_block_black.png image card
        StackPane card = new StackPane();
        card.setMaxWidth(CARD_W);

        var cardBgUrl = getClass().getResource(CARD_BG);
        if (cardBgUrl != null) {
            ImageView cardBgIv = new ImageView(new Image(cardBgUrl.toExternalForm()));
            cardBgIv.setFitWidth(CARD_W);
            cardBgIv.setPreserveRatio(true);
            card.getChildren().add(cardBgIv);
        } else {
            // Fallback plain dark card
            card.setStyle("""
                -fx-background-color: rgba(0,0,0,0.82);
                -fx-background-radius: 24;
                -fx-min-width: 820; -fx-min-height: 380;
                -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.85),36,0.65,0,6);
            """);
        }

        // ── Inner content on top of card image ───────────────────────────────
        VBox inner = new VBox(26);
        inner.setAlignment(Pos.CENTER);
        inner.setStyle("-fx-padding: 48 64 44 64;");

        Text title = new Text("⚙  ตั้งค่า");
        title.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 40));
        title.setFill(Color.web("#80cbc4"));
        title.setStyle("-fx-effect: dropshadow(gaussian,rgba(128,203,196,0.55),16,0.4,0,0);");

        javafx.scene.shape.Line sep = new javafx.scene.shape.Line(0, 0, 620, 0);
        sep.setStroke(Color.web("#80cbc4", 0.35)); sep.setStrokeWidth(1.5);

        VBox sliders = new VBox(22,
                volRow("🔊  Master Volume", audio.getGameVolume(),  audio::setGameVolume),
                volRow("🎵  SFX Volume",     audio.getSfxVolume(),   audio::setSfxVolume),
                volRow("🎶  Music Volume",    audio.getMusicVolume(), audio::setMusicVolume)
        );
        sliders.setAlignment(Pos.CENTER_LEFT); sliders.setMaxWidth(640);

        ImageView backBtn = makeImgBtn(BTN_BACK, BTN_W, () -> {
            AudioManager.getInstance().playAccept(); cb.onBack();
        });

        inner.getChildren().addAll(title, sep, sliders, backBtn);
        StackPane.setAlignment(inner, Pos.CENTER);
        card.getChildren().add(inner);

        card.setOpacity(0);
        root.getChildren().addAll(bg, card);
        FadeTransition ft = new FadeTransition(Duration.millis(400), card);
        ft.setToValue(1); ft.play();

        return root;
    }

    private HBox volRow(String name, double initial, java.util.function.DoubleConsumer onChange) {
        Label lbl = new Label(name);
        lbl.setMinWidth(230);
        lbl.setStyle("""
            -fx-font-family: 'Comic Sans MS';
            -fx-font-size: 20px;
            -fx-font-weight: bold;
            -fx-text-fill: #e8f4f0;
        """);
        Slider s = new Slider(0, 1, initial);
        s.setPrefWidth(280);
        s.setStyle("-fx-accent: #80cbc4; -fx-control-inner-background: rgba(255,255,255,0.15);");
        Label pct = new Label(pctStr(initial));
        pct.setMinWidth(56);
        pct.setStyle("""
            -fx-font-family: 'Comic Sans MS';
            -fx-font-size: 18px;
            -fx-text-fill: #80cbc4;
            -fx-font-weight: bold;
        """);
        s.valueProperty().addListener((obs, o, n) -> { onChange.accept(n.doubleValue()); pct.setText(pctStr(n.doubleValue())); });
        HBox row = new HBox(20, lbl, s, pct);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private static String pctStr(double v) { return (int) Math.round(v * 100) + "%"; }

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