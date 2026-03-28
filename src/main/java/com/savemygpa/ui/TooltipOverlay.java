package com.savemygpa.ui;

import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class TooltipOverlay {

    private static final double WIDTH  = 340;
    private static final double PAD    = 18;
    private static final double MARGIN = 16;

    private final StackPane root;
    private final StackPane card;
    private final Label     titleLbl;
    private final Label     bodyLbl;

    private FadeTransition activeFade;

    public TooltipOverlay(StackPane root) {
        this.root = root;

        titleLbl = new Label();
        titleLbl.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 20));
        titleLbl.setTextFill(Color.web("#ffe082"));
        titleLbl.setWrapText(true);
        titleLbl.setMaxWidth(WIDTH - PAD * 2);
        titleLbl.setTextAlignment(TextAlignment.LEFT);

        bodyLbl = new Label();
        bodyLbl.setFont(Font.font("Comic Sans MS", FontWeight.NORMAL, 16));
        bodyLbl.setTextFill(Color.web("#e0e0e0"));
        bodyLbl.setWrapText(true);
        bodyLbl.setMaxWidth(WIDTH - PAD * 2);
        bodyLbl.setTextAlignment(TextAlignment.LEFT);

        VBox content = new VBox(8, titleLbl, bodyLbl);
        content.setAlignment(Pos.TOP_LEFT);
        content.setStyle("-fx-padding: " + PAD + ";");

        card = new StackPane(content);
        card.setMaxWidth(WIDTH);
        card.setMaxHeight(Double.MAX_VALUE);
        card.setPrefHeight(USE_COMPUTED_SIZE);
        card.setStyle(
                "-fx-background-color: rgba(10,10,24,0.85);" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-color: rgba(255,255,255,0.15);" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-width: 1.2;"
        );
        card.setMouseTransparent(true);
        card.setOpacity(0);

        StackPane.setAlignment(card, Pos.TOP_RIGHT);
        card.setTranslateX(-MARGIN);
        card.setTranslateY(MARGIN);
    }

    private static final double USE_COMPUTED_SIZE = javafx.scene.layout.Region.USE_COMPUTED_SIZE;

    public void show(String title, String body) {
        stopFade();
        titleLbl.setText(title);
        bodyLbl.setText(body);

        if (!root.getChildren().contains(card)) {
            root.getChildren().add(card);
        }

        activeFade = new FadeTransition(Duration.millis(160), card);
        activeFade.setToValue(1.0);
        activeFade.play();
    }

    public void hide() {
        stopFade();
        activeFade = new FadeTransition(Duration.millis(120), card);
        activeFade.setToValue(0.0);
        activeFade.setOnFinished(e -> root.getChildren().remove(card));
        activeFade.play();
    }

    public void dispose() {
        stopFade();
        root.getChildren().remove(card);
    }

    private void stopFade() {
        if (activeFade != null) {
            activeFade.stop();
            activeFade = null;
        }
    }
}