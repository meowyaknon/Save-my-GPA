package com.savemygpa.ui;

import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class StatsBarUI {

    // ── Rendered panel size (used by OutsideUI for positioning) ──────────────
    public static final double IMG_W = 620;
    public static final double IMG_H = 310;

    // ── Bar geometry ──────────────────────────────────────────────────────────
    private static final double PAD_H       = 36;
    private static final double PAD_V       = 6;
    private static final double BAR_W       = IMG_W - PAD_H * 2;
    private static final double BAR_H       = 38;
    private static final double BAR_RADIUS  = 20;
    private static final double V_SPACING   = 16;

    // ── Colours ───────────────────────────────────────────────────────────────
    private static final String COL_INT_FILL     = "#d32f2f";
    private static final String COL_INT_TRACK    = "#7b4040";
    private static final String COL_MOOD_FILL    = "#e6d44a";
    private static final String COL_MOOD_TRACK   = "#7a7040";
    private static final String COL_ENERGY_FILL  = "#4caf50";
    private static final String COL_ENERGY_TRACK = "#2e5e30";
    private static final String COL_LABEL        = "#3b1a1a";
    private static final String FONT             = "Comic Sans MS";

    // ── Nodes ─────────────────────────────────────────────────────────────────
    private final VBox root;
    private final BarRow intRow;
    private final BarRow moodRow;
    private final BarRow energyRow;

    public StatsBarUI() {
        intRow    = new BarRow("INT",    100, BAR_W, COL_INT_FILL,    COL_INT_TRACK);
        moodRow   = new BarRow("MOOD",   100, BAR_W, COL_MOOD_FILL,   COL_MOOD_TRACK);
        energyRow = new BarRow("ENERGY",  10, BAR_W, COL_ENERGY_FILL, COL_ENERGY_TRACK);

        root = new VBox(V_SPACING, intRow.node, moodRow.node, energyRow.node);
        root.setAlignment(Pos.TOP_LEFT);
        root.setPadding(new Insets(PAD_V, PAD_H, PAD_V, PAD_H));
        root.setPrefSize(IMG_W, IMG_H);
        root.setMaxSize(IMG_W, IMG_H);
        root.setStyle(
                "-fx-background-color: rgba(255,255,255,0.93);" +
                        "-fx-background-radius: 28 80 28 28;" +
                        "-fx-effect: dropshadow(gaussian,rgba(80,40,10,0.30),18,0.4,3,4);"
        );
    }

    public VBox getNode() { return root; }

    public void refresh(Player player) {
        intRow   .animateTo(player.getStat(StatType.INTELLIGENCE), 100);
        moodRow  .animateTo(player.getStat(StatType.MOOD),         100);
        energyRow.animateTo(player.getStat(StatType.ENERGY),        10);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Inner: one stat row
    // ═════════════════════════════════════════════════════════════════════════
    private static final class BarRow {
        final VBox node;
        private final Rectangle fill;
        private final Label      valLabel;
        private final double     trackW;
        private final double     maxVal;

        BarRow(String name, double max, double tW, String fillCol, String trackCol) {
            this.maxVal = max;
            this.trackW = tW;

            // Name label
            Label nameLbl = new Label(name);
            nameLbl.setFont(Font.font(FONT, FontWeight.BOLD, 26));
            nameLbl.setTextFill(Color.web(COL_LABEL));

            // Value label (right-aligned)
            valLabel = new Label("0 / " + (int) max);
            valLabel.setFont(Font.font(FONT, FontWeight.BOLD, 22));
            valLabel.setTextFill(Color.web(COL_LABEL));

            Region spc = new Region();
            HBox.setHgrow(spc, Priority.ALWAYS);
            HBox labelRow = new HBox(nameLbl, spc, valLabel);
            labelRow.setAlignment(Pos.CENTER_LEFT);
            labelRow.setMaxWidth(tW);

            // Track
            Rectangle track = new Rectangle(tW, BAR_H);
            track.setArcWidth(BAR_RADIUS * 2); track.setArcHeight(BAR_RADIUS * 2);
            track.setFill(Color.web(trackCol));

            // Fill (animated)
            fill = new Rectangle(0, BAR_H);
            fill.setArcWidth(BAR_RADIUS * 2); fill.setArcHeight(BAR_RADIUS * 2);
            fill.setFill(Color.web(fillCol));
            Rectangle clip = new Rectangle(tW, BAR_H);
            clip.setArcWidth(BAR_RADIUS * 2); clip.setArcHeight(BAR_RADIUS * 2);
            fill.setClip(clip);

            // Shine
            Rectangle shine = new Rectangle(tW * 0.40, BAR_H * 0.33);
            shine.setArcWidth(BAR_RADIUS); shine.setArcHeight(BAR_RADIUS);
            shine.setFill(Color.color(1, 1, 1, 0.18));
            shine.setTranslateX(tW * 0.07); shine.setTranslateY(BAR_H * 0.10);

            StackPane bar = new StackPane(track, fill, shine);
            bar.setAlignment(Pos.CENTER_LEFT);
            bar.setMaxWidth(tW);

            node = new VBox(4, labelRow, bar);
            node.setAlignment(Pos.TOP_LEFT);
        }

        void animateTo(int current, int max) {
            double target = trackW * Math.max(0, Math.min(current, (int) maxVal)) / maxVal;
            valLabel.setText(current + " / " + max);
            new Timeline(
                    new KeyFrame(Duration.ZERO,         new KeyValue(fill.widthProperty(), fill.getWidth())),
                    new KeyFrame(Duration.millis(360),  new KeyValue(fill.widthProperty(), target))
            ).play();
        }
    }
}