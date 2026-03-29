package com.savemygpa.ui;

import com.savemygpa.audio.AudioManager;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

public class GameDialog {

    // ── Accent colours ────────────────────────────────────────────────────────
    private static final String ACCENT_EVENT   = "#d570e0";

    // ── Shared body text colour ───────────────
    private static final String TEXT_COLOR = "#3b1a1a";

    // ── Assets ────────────────────────────────────────────────────────────────
    private static final String POPUP_BG_V     = "/images/popup/big_block_black_V.png";
    private static final String BTN_CONTINUE   = "/images/menu/menu_continue.png";
    private static final double CARD_W         = 520;
    private static final double BTN_CONTINUE_W = 300;

    // ── Event image paths (per event name, null = no illustration) ────────────
    private static String eventImagePath(String eventName) {
        if (eventName == null) return null;
        String name = eventName.replaceAll("^[\\s🎲]+", "").trim();
        return switch (name) {
            case "รุ่นพี่เลี้ยงไก่ทอด"       -> "/images/event/free_food.png";
            case "ได้นั่งที่นั่ง \"มุมโปรด\""             -> "/images/event/good_seat.png";
            case "\"รถพระจอม\" ในตำนานไม่มาสักที"              -> "/images/event/slow_bus.png";
            case "เจอ \"พี่รหัส\" สายซัพพอร์ต"           -> "/images/event/free_meal.png";
            case "โรงอาหารแน่นยิ่งกว่าปลาป๋อง"            -> "/images/event/no_seat.png";
            case "รุ่นพี่มาชวนคุยเรื่องโปรเจค"  -> "/images/event/senior_advice.png";
            case "บัตร นศ. หายในห้องสอบ"               -> "/images/event/forget_id.png";
            case "Internet ล่ม"                -> "/images/event/internet_down.png";
            case "โปรเจค Compile ผ่านในครั้งเดียว!"    -> "/images/event/perfect_compile.png";
            case "เดินไม่ดูทาง! โป๊กกระไดเข้าให้"                  -> "/images/event/head_hurt.png";
            case "ประตูอัตโนมัติ \"ไม่เปิด\""       -> "/images/event/broken_door.png";
            case "ท่านคณบดีใจดี เลี้ยงไอติม"        -> "/images/event/dean_treats.png";
            case "ได้รับ \"Source Code\" จากรุ่นพี่"           -> "/images/event/source_code.png";
            case "กองทัพ \"เป็ด\" บุกเลนจักรยาน"                  -> "/images/event/duck.png";
            case "พรจาก \"น้องเงินทอง\""                 -> "/images/event/lucky_dragon.png";
            case "ฝนตกหนัก"                   -> "/images/event/rain.png";
            default                             -> null;
        };
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
        card.setAlignment(Pos.CENTER);

        // ── Background image (big_block_black_V.png) ──────────────────────────
        var bgUrl = GameDialog.class.getResource(POPUP_BG_V);
        if (bgUrl != null) {
            ImageView bgIv = new ImageView(new Image(bgUrl.toExternalForm()));
            bgIv.setFitWidth(CARD_W);
            bgIv.setPreserveRatio(true);
            bgIv.setSmooth(true);
            card.getChildren().add(bgIv);
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
                "-fx-effect:dropshadow(gaussian,rgba(213,112,224,0.55),14,0.40,0,0);");
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
        ImageView continueBtn = makeContinueBtn(() -> {dismissCard(root, overlay, onClose); AudioManager.getInstance().playAccept();});
        body.getChildren().add(continueBtn);

        StackPane.setAlignment(body, Pos.CENTER);
        card.getChildren().add(body);

        Scale cardScale = new Scale(1, 1);
        cardScale.pivotXProperty().bind(Bindings.divide(card.widthProperty(), 2));
        cardScale.pivotYProperty().bind(Bindings.divide(card.heightProperty(), 2));
        cardScale.xProperty().bind(Bindings.createDoubleBinding(
                () -> Math.min(root.getWidth() / 1920.0, root.getHeight() / 1080.0),
                root.widthProperty(), root.heightProperty()));
        cardScale.yProperty().bind(cardScale.xProperty());
        card.getTransforms().add(cardScale);

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

    private static void dismissCard(StackPane root, StackPane overlay, Runnable cb) {
        overlay.prefWidthProperty().unbind();
        overlay.prefHeightProperty().unbind();
        FadeTransition ft = new FadeTransition(Duration.millis(150), overlay);
        ft.setToValue(0);
        ft.setOnFinished(e -> { root.getChildren().remove(overlay); if (cb != null) cb.run(); });
        ft.play();
    }
}