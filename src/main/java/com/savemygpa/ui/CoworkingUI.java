package com.savemygpa.ui;

import com.savemygpa.audio.AudioManager;
import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

public class CoworkingUI {

    private static final String POPUP_BG   = "/images/popup/popup_bg.png";
    private static final String BTN_STUDY  = "/images/popup/button_study.png";
    private static final String BTN_RELAX  = "/images/popup/button_relax.png";
    private static final String BTN_CANCEL = "/images/popup/button_cancel.png";

    private static final double BTN_ACTION_W = 360;
    private static final double BTN_CANCEL_W = 200;

    public interface Callbacks {
        void onRelax();
        void onStudy();
        void onBack();
    }

    private final Callbacks cb;
    private final StackPane sceneRoot;

    public CoworkingUI(StackPane sceneRoot, Callbacks cb) {
        this.sceneRoot = sceneRoot;
        this.cb = cb;
    }

    public void show() {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.55);");
        overlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        StackPane card = buildCard(overlay);
        overlay.getChildren().add(card);
        sceneRoot.getChildren().add(overlay);

        card.setScaleX(0.88); card.setScaleY(0.88); card.setOpacity(0);
        ScaleTransition st = new ScaleTransition(Duration.millis(220), card);
        st.setToX(1); st.setToY(1);
        FadeTransition ft = new FadeTransition(Duration.millis(220), card);
        ft.setToValue(1);
        st.play(); ft.play();
    }

    private StackPane buildCard(StackPane overlay) {
        ImageView bg = loadImg(POPUP_BG);
        bg.setFitWidth(520);
        bg.setPreserveRatio(true);

        // Reference: Study (ทบทวนบทเรียน) on top, Relax (พักผ่อน) below, then cancel
        ImageView studyBtn  = makeBtn(BTN_STUDY,  BTN_ACTION_W, () -> dismiss(overlay, cb::onStudy));
        ImageView relaxBtn  = makeBtn(BTN_RELAX,  BTN_ACTION_W, () -> dismiss(overlay, cb::onRelax));
        ImageView cancelBtn = makeBtn(BTN_CANCEL, BTN_CANCEL_W, () -> dismiss(overlay, cb::onBack));

        VBox btnStack = new VBox(16, studyBtn, relaxBtn, cancelBtn);
        btnStack.setAlignment(Pos.CENTER);
        btnStack.setStyle("-fx-padding: 48 40 36 40;");

        StackPane card = new StackPane(bg, btnStack);
        card.setMaxWidth(520);
        return card;
    }

    private ImageView makeBtn(String path, double width, Runnable onClick) {
        ImageView iv = loadImg(path);
        iv.setFitWidth(width);
        iv.setPreserveRatio(true);
        iv.setCursor(javafx.scene.Cursor.HAND);

        iv.setOnMouseEntered(e -> { iv.setScaleX(1.05); iv.setScaleY(1.05); iv.setOpacity(0.88); });
        iv.setOnMouseExited (e -> { iv.setScaleX(1.0);  iv.setScaleY(1.0);  iv.setOpacity(1.0);  });
        iv.setOnMousePressed (e -> { iv.setScaleX(0.95); iv.setScaleY(0.95); });
        iv.setOnMouseReleased(e -> {
            iv.setScaleX(1.0); iv.setScaleY(1.0);
            AudioManager.getInstance().playClick();
            onClick.run();
        });
        return iv;
    }

    private void dismiss(StackPane overlay, Runnable callback) {
        FadeTransition ft = new FadeTransition(Duration.millis(150), overlay);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            sceneRoot.getChildren().remove(overlay);
            if (callback != null) callback.run();
        });
        ft.play();
    }

    private ImageView loadImg(String path) {
        var url = getClass().getResource(path);
        if (url == null) throw new IllegalStateException("Missing resource: " + path);
        return new ImageView(new Image(url.toExternalForm()));
    }
}