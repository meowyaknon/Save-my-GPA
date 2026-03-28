package com.savemygpa.ui;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

public class GameDialog {

    // ── Accent colours ────────────────────────────────────────────────────────
    private static final String ACCENT_INFO    = "#4fc3f7";
    private static final String ACCENT_EVENT   = "#3151B0FF";

    // ── Shared body text colour — matches StatsBarUI COL_LABEL ───────────────
    private static final String TEXT_COLOR = "#3b1a1a";

    // ── Assets ────────────────────────────────────────────────────────────────
    private static final String POPUP_BG_V     = "/images/popup/big_block_black_V.png";
    private static final String BTN_CONTINUE   = "/images/menu/menu_continue.png";
    private static final double CARD_W         = 460;
    private static final double BTN_CONTINUE_W = 300;

    // ── Event image paths (per event name, null = no illustration) ────────────
    private static String eventImagePath(String eventName) {
        if (eventName == null) return null;
        String name = eventName.replaceAll("^[\\s🎲]+", "").trim();
        return switch (name) {
            case "รุ่นพี่เลี้ยงไก่ทอด"       -> "/images/event/free_food.png";
            case "ได้นั่งมุมโปรด"             -> "/images/event/good_seat.png";
            case "รถพระจอมไม่มา"              -> "/images/event/slow_bus.png";
            case "เจอพี่รหัสสายซัพ"           -> "/images/event/free_meal.png";
            case "โรงอาหารแน่นมาก"            -> "/images/event/no_seat.png";
            case "รุ่นพี่ชวนคุยเรื่องโปรเจค"  -> "/images/event/senior_advice.png";
            case "บัตร นศ. หาย"               -> "/images/event/forget_id.png";
            case "Internet ล่ม"                -> "/images/event/internet_down.png";
            case "Compile ผ่านในครั้งเดียว"    -> "/images/event/perfect_compile.png";
            case "โป๊กกระได"                  -> "/images/event/hurt_head.png";
            case "ประตูอัตโนมัติไม่เปิด"       -> "/images/event/broken_door.png";
            case "ท่านคณบดีเลี้ยงไอติม"        -> "/images/event/dean_treats.png";
            case "ได้รับ Source Code"           -> "/images/event/source_code.png";
            case "กองทัพเป็ด"                  -> "/images/event/duck.png";
            case "น้องเงินทอง"                 -> "/images/event/lucky_dragon.png";
            case "ฝนตกหนัก"                   -> "/images/event/rain.png";
            default                             -> null;
        };
    }

    // ── Simple info dialog ────────────────────────────────────────────────────
    public static void show(StackPane root, String title, String message, Runnable onClose) {
        StackPane overlay = overlay();
        HBox box = buildBox(ACCENT_INFO);
        VBox text = textCol(title, message);
        javafx.scene.control.Button ok = btn("ตกลง  ✔", ACCENT_INFO);
        ok.setOnAction(e -> dismiss(root, overlay, onClose));
        overlay.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE)
                dismiss(root, overlay, onClose);
        });
        box.getChildren().addAll(text, spacer(), ok);
        HBox.setHgrow(text, Priority.ALWAYS);
        finish(root, overlay, box);
    }


    // ── Event notification (custom card with popup background image) ──────────
    public static void event(StackPane root, String eventName, String description, Runnable onClose) {

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.68);");
        overlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        overlay.prefWidthProperty().bind(root.widthProperty());
        overlay.prefHeightProperty().bind(root.heightProperty());

        StackPane centerWrapper = new StackPane();
        centerWrapper.setAlignment(Pos.CENTER);
        centerWrapper.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        centerWrapper.prefWidthProperty().bind(root.widthProperty());
        centerWrapper.prefHeightProperty().bind(root.heightProperty());

        // Card
        StackPane card = new StackPane();
        card.setMaxWidth(CARD_W);
        card.setAlignment(Pos.TOP_CENTER);

        // ── Background image (big_block_black_V.png) ──────────────────────────
        var bgUrl = GameDialog.class.getResource(POPUP_BG_V);
        if (bgUrl != null) {
            ImageView bgIv = new ImageView(new Image(bgUrl.toExternalForm()));
            bgIv.setFitWidth(CARD_W);
            bgIv.setPreserveRatio(true);
            bgIv.setSmooth(true);
            card.getChildren().add(bgIv);   // background — first child
        } else {
            card.setStyle("-fx-background-color:rgba(8,8,24,0.97);-fx-background-radius:24;" +
                    "-fx-min-width:460;-fx-min-height:640;" +
                    "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.85),28,0.65,0,5);");
        }

        // ── Content stacked on top ────────────────────────────────────────────
        VBox body = new VBox(0);
        body.setAlignment(Pos.CENTER);
        body.setStyle("-fx-padding: 28 28 40 28;");
        body.setMaxWidth(CARD_W);

        // 1. Event illustration at the TOP — shown if the image file exists
        String imgPath = eventImagePath(eventName);
        if (imgPath != null) {
            var evUrl = GameDialog.class.getResource(imgPath);
            if (evUrl != null) {
                ImageView evImg = new ImageView(new Image(evUrl.toExternalForm()));
                double imgW = CARD_W - 56;
                evImg.setFitWidth(imgW);
                evImg.setFitHeight(200);
                evImg.setPreserveRatio(false);
                evImg.setSmooth(true);
                Rectangle clip = new Rectangle(imgW, 200);
                clip.setArcWidth(18); clip.setArcHeight(18);
                evImg.setClip(clip);
                VBox.setMargin(evImg, new javafx.geometry.Insets(0, 0, 16, 0));
                body.getChildren().add(evImg);
            }
        }

        // 2. Gold separator
        javafx.scene.shape.Line sep = new javafx.scene.shape.Line(0, 0, CARD_W - 80, 0);
        sep.setStroke(Color.web(ACCENT_EVENT, 0.45));
        sep.setStrokeWidth(1.5);
        VBox.setMargin(sep, new javafx.geometry.Insets(0, 0, 14, 0));
        body.getChildren().add(sep);

        // 3. Event name
        String cleanName = eventName == null ? "" : eventName.replaceAll("^[\\s🎲]+", "").trim();
        Label nameLbl = new Label("🎲  " + cleanName);
        nameLbl.setWrapText(true);
        nameLbl.setMaxWidth(CARD_W - 56);
        nameLbl.setStyle("-fx-font-family:'Comic Sans MS';-fx-font-size:24px;-fx-font-weight:bold;" +
                "-fx-text-fill:" + ACCENT_EVENT + ";" +
                "-fx-effect:dropshadow(gaussian,rgba(255,224,130,0.55),14,0.40,0,0);");
        nameLbl.setTextAlignment(TextAlignment.CENTER);
        nameLbl.setAlignment(Pos.CENTER);
        VBox.setMargin(nameLbl, new javafx.geometry.Insets(0, 0, 10, 0));
        body.getChildren().add(nameLbl);

        // 4. Description — TEXT_COLOR
        Label descLbl = new Label(description == null ? "" : description);
        descLbl.setWrapText(true);
        descLbl.setMaxWidth(CARD_W - 56);
        descLbl.setStyle("-fx-font-family:'Comic Sans MS';-fx-font-size:17px;" +
                "-fx-text-fill:" + TEXT_COLOR + ";-fx-line-spacing:5;");
        descLbl.setTextAlignment(TextAlignment.CENTER);
        descLbl.setAlignment(Pos.CENTER);
        VBox.setMargin(descLbl, new javafx.geometry.Insets(0, 0, 24, 0));
        body.getChildren().add(descLbl);

        // 5. Continue button
        ImageView continueBtn = makeContinueBtn(() -> dismissCard(root, overlay, onClose));
        body.getChildren().add(continueBtn);

        StackPane.setAlignment(body, Pos.TOP_CENTER);
        card.getChildren().add(body);   // content — second child (on top of bg)

        // FIX: Scale card relative to the root pane dimensions so it centers
        // correctly in both windowed and fullscreen mode.
        Scale cardScale = new Scale(1, 1);
        cardScale.pivotXProperty().bind(Bindings.divide(card.widthProperty(), 2));
        cardScale.pivotYProperty().bind(Bindings.divide(card.heightProperty(), 2));
        cardScale.xProperty().bind(Bindings.createDoubleBinding(
                () -> Math.min(root.getWidth() / 1920.0, root.getHeight() / 1080.0),
                root.widthProperty(), root.heightProperty()));
        cardScale.yProperty().bind(cardScale.xProperty());
        card.getTransforms().add(cardScale);

        // FIX: Card goes into the centering wrapper, not directly onto overlay
        centerWrapper.getChildren().add(card);
        overlay.getChildren().add(centerWrapper);
        root.getChildren().add(overlay);

        // Entrance animation
        card.setScaleX(0.88); card.setScaleY(0.88); card.setOpacity(0);
        ScaleTransition st = new ScaleTransition(Duration.millis(220), card);
        st.setToX(1); st.setToY(1);
        FadeTransition ft = new FadeTransition(Duration.millis(220), card);
        ft.setToValue(1);
        st.play(); ft.play();

        overlay.setFocusTraversable(true);
        overlay.requestFocus();
        overlay.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE)
                dismissCard(root, overlay, onClose);
        });
    }

    // =========================================================================
    // Shared builders
    // =========================================================================
    static HBox buildBox(String accentColor) {
        javafx.scene.shape.Rectangle accentBar = new javafx.scene.shape.Rectangle(6, 120);
        accentBar.setFill(Color.web(accentColor));
        accentBar.setArcWidth(6); accentBar.setArcHeight(6);
        HBox box = new HBox(20);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMaxWidth(820); box.setMinHeight(100);
        box.setStyle("""
            -fx-background-color: linear-gradient(to right, rgba(12,12,32,0.98), rgba(22,28,52,0.96));
            -fx-background-radius: 16;
            -fx-padding: 24 28 24 0;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.80), 28, 0.65, 0, 6);
            -fx-border-color: rgba(255,255,255,0.07);
            -fx-border-radius: 16;
            -fx-border-width: 1;
        """);
        box.getChildren().add(accentBar);
        return box;
    }

    static VBox textCol(String title, String message) {
        VBox v = new VBox(8, titleLabel(title, ACCENT_INFO), bodyLabel(message));
        v.setAlignment(Pos.CENTER_LEFT);
        return v;
    }

    static Label titleLabel(String text, String color) {
        Label l = new Label(text);
        l.setWrapText(true); l.setMaxWidth(480);
        l.setStyle("-fx-font-family:'Comic Sans MS';-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
        return l;
    }

    static Label bodyLabel(String text) {
        Label l = new Label(text);
        l.setWrapText(true); l.setMaxWidth(480);
        l.setStyle("-fx-font-family:'Comic Sans MS';-fx-font-size:14px;-fx-text-fill:" + TEXT_COLOR + ";-fx-line-spacing:3;");
        return l;
    }

    static javafx.scene.control.Button btn(String text, String accentColor) {
        javafx.scene.control.Button b = new javafx.scene.control.Button(text);
        b.setStyle("-fx-font-family:'Comic Sans MS';-fx-font-size:14px;" +
                "-fx-background-color:" + accentColor + ";-fx-text-fill:" + TEXT_COLOR + ";" +
                "-fx-font-weight:bold;-fx-background-radius:10;-fx-padding:8 20 8 20;-fx-cursor:hand;");
        b.setOnMouseEntered(e -> b.setOpacity(0.80));
        b.setOnMouseExited(e  -> b.setOpacity(1.00));
        return b;
    }

    private static ImageView makeContinueBtn(Runnable onClick) {
        var url = GameDialog.class.getResource(BTN_CONTINUE);
        if (url == null) {
            ImageView ph = new ImageView();
            ph.setFitWidth(BTN_CONTINUE_W); ph.setFitHeight(60);
            return ph;
        }
        ImageView iv = new ImageView(new Image(url.toExternalForm()));
        iv.setFitWidth(BTN_CONTINUE_W); iv.setPreserveRatio(true); iv.setSmooth(true);
        iv.setCursor(javafx.scene.Cursor.HAND);
        iv.setOnMouseEntered(e  -> { iv.setScaleX(1.06); iv.setScaleY(1.06); iv.setOpacity(0.88); });
        iv.setOnMouseExited(e   -> { iv.setScaleX(1.0);  iv.setScaleY(1.0);  iv.setOpacity(1.0);  });
        iv.setOnMousePressed(e  -> { iv.setScaleX(0.94); iv.setScaleY(0.94); e.consume(); });
        iv.setOnMouseReleased(e -> { iv.setScaleX(1.0);  iv.setScaleY(1.0); });
        iv.setOnMouseClicked(e  -> { e.consume(); onClick.run(); });
        return iv;
    }

    private static Region spacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.SOMETIMES);
        r.setMinWidth(16);
        return r;
    }

    static StackPane overlay() {
        StackPane p = new StackPane();
        p.setStyle("-fx-background-color: rgba(0,0,0,0.55);");
        p.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        return p;
    }

    static void finish(StackPane root, StackPane overlay, HBox box) {
        overlay.getChildren().add(box);
        root.getChildren().add(overlay);
        overlay.setFocusTraversable(true);
        overlay.requestFocus();
        animateIn(box);
    }

    static void animateIn(javafx.scene.Node box) {
        box.setScaleX(0.90); box.setScaleY(0.90); box.setOpacity(0);
        ScaleTransition st = new ScaleTransition(Duration.millis(220), box);
        st.setToX(1); st.setToY(1);
        FadeTransition ft = new FadeTransition(Duration.millis(220), box);
        ft.setToValue(1);
        st.play(); ft.play();
    }

    static void dismiss(StackPane root, StackPane overlay, Runnable cb) {
        FadeTransition ft = new FadeTransition(Duration.millis(150), overlay);
        ft.setToValue(0);
        ft.setOnFinished(e -> { root.getChildren().remove(overlay); if (cb != null) cb.run(); });
        ft.play();
    }

    private static void dismissCard(StackPane root, StackPane overlay, Runnable cb) {
        overlay.prefWidthProperty().unbind();
        overlay.prefHeightProperty().unbind();
        FadeTransition ft = new FadeTransition(Duration.millis(150), overlay);
        ft.setToValue(0);
        ft.setOnFinished(e -> { root.getChildren().remove(overlay); if (cb != null) cb.run(); });
        ft.play();
    }
}