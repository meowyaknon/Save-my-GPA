package com.savemygpa.exam;

import com.savemygpa.player.Player;
import com.savemygpa.player.StatTier;
import com.savemygpa.player.StatType;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import java.util.Random;

public class CaptchaMiniGame {

    private static final int TOTAL_ROUNDS = 5;
    private static final int POINTS_PER_ROUND = 10;
    private static final int SECONDS_PER_ROUND = 12;
    private static final double BAR_WIDTH = 400;

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";

    private final Player player;
    private final Random random = new Random();
    private final Runnable onFinish;

    private int totalScore = 0;
    private int currentRound = 0;
    private String currentCaptcha = "";

    private Timeline countdownTimer;
    private Timeline barAnim;

    // --- UI ---
    private final VBox root = new VBox(16);
    private final VBox contentArea = new VBox(16);
    private final Label roundLabel = new Label();
    private final Label timerLabel = new Label();
    private final Rectangle timerBarFill = new Rectangle(BAR_WIDTH, 18);
    private final Rectangle timerBarBg = new Rectangle(BAR_WIDTH, 18);
    private final Canvas captchaCanvas = new Canvas(360, 80);
    private final Label resultLabel = new Label();
    private final Label scoreLabel = new Label();
    private final TextField answerField = new TextField();
    private final Button submitButton = new Button("ยืนยัน ✔");

    public CaptchaMiniGame(Player player, Runnable onFinish) {
        this.player = player;
        this.onFinish = onFinish;
    }

    public VBox getView() {
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(24));
        root.setStyle("""
                -fx-background-color: #1a1a2e;
                -fx-background-radius: 16;
                """);

        Label title = new Label("🔐 เกม Captcha");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #f0e68c;");

        Label hint = new Label("พิมพ์ตัวอักษรที่เห็นในภาพให้ถูกต้องและทันเวลา!");
        hint.setStyle("-fx-font-size: 13px; -fx-text-fill: #aaaaaa;");

        roundLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #cccccc;");

        // --- Timer bar ---
        timerBarBg.setFill(Color.web("#2e2e4e"));
        timerBarBg.setArcWidth(12);
        timerBarBg.setArcHeight(12);
        timerBarFill.setFill(Color.web("#4fc3f7"));
        timerBarFill.setArcWidth(12);
        timerBarFill.setArcHeight(12);

        StackPane timerBar = new StackPane();
        timerBar.setAlignment(Pos.CENTER_LEFT);
        timerBar.getChildren().addAll(timerBarBg, timerBarFill);

        timerLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #4fc3f7;");

        VBox timerBox = new VBox(4, timerLabel, timerBar);
        timerBox.setAlignment(Pos.CENTER);
        timerBox.setMaxWidth(BAR_WIDTH);

        // --- Captcha canvas border ---
        StackPane captchaBox = new StackPane(captchaCanvas);
        captchaBox.setStyle("""
                -fx-background-color: #16213e;
                -fx-background-radius: 12;
                -fx-border-color: #0f3460;
                -fx-border-radius: 12;
                -fx-border-width: 2;
                -fx-padding: 12;
                """);
        captchaBox.setMaxWidth(400);

        // --- Input ---
        answerField.setMaxWidth(200);
        answerField.setPromptText("พิมพ์ที่นี่...");
        answerField.setStyle("""
                -fx-font-size: 16px;
                -fx-alignment: center;
                -fx-background-color: #16213e;
                -fx-text-fill: white;
                -fx-border-color: #4fc3f7;
                -fx-border-radius: 8;
                -fx-background-radius: 8;
                """);
        // กด Enter ได้เลย
        answerField.setOnAction(e -> handleAnswer());

        submitButton.setStyle("""
                -fx-font-size: 14px;
                -fx-background-color: #4fc3f7;
                -fx-text-fill: #1a1a2e;
                -fx-font-weight: bold;
                -fx-background-radius: 8;
                -fx-cursor: hand;
                -fx-padding: 8 20;
                """);
        submitButton.setOnAction(e -> handleAnswer());

        HBox inputRow = new HBox(12, answerField, submitButton);
        inputRow.setAlignment(Pos.CENTER);

        resultLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ffffff;");
        scoreLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #f0e68c;");

        contentArea.setAlignment(Pos.CENTER);
        contentArea.getChildren().addAll(
                roundLabel, timerBox, captchaBox, inputRow, resultLabel, scoreLabel
        );

        root.getChildren().addAll(title, hint, contentArea);

        showReadyScreen();
        return root;
    }

    // --- Ready screen ---
    private void showReadyScreen() {
        contentArea.getChildren().clear();

        roundLabel.setText("รอบที่ " + (currentRound + 1) + " / " + TOTAL_ROUNDS);
        roundLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #cccccc;");

        Label readyLabel = new Label("พร้อมสำหรับรอบถัดไปหรือยัง?");
        readyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #ffffff;");

        Button readyBtn = new Button("พร้อมแล้ว! 🚀");
        readyBtn.setStyle("""
                -fx-font-size: 16px;
                -fx-background-color: #4fc3f7;
                -fx-text-fill: #1a1a2e;
                -fx-font-weight: bold;
                -fx-background-radius: 8;
                -fx-cursor: hand;
                -fx-padding: 10 24;
                """);
        readyBtn.setOnAction(e -> showCountdown(() -> loadRound()));

        contentArea.getChildren().addAll(roundLabel, readyLabel, readyBtn);
    }

    // --- 3..2..1..GO! ---
    private void showCountdown(Runnable after) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(roundLabel);

        String[] steps = {"3", "2", "1", "GO! 🚀"};
        Timeline tl = new Timeline();
        for (int i = 0; i < steps.length; i++) {
            final String s = steps[i];
            tl.getKeyFrames().add(new KeyFrame(Duration.seconds(i),
                    e -> roundLabel.setText(s)));
        }
        tl.getKeyFrames().add(new KeyFrame(Duration.seconds(steps.length), e -> after.run()));
        tl.play();
    }

    private void loadRound() {
        currentRound++;
        resultLabel.setText("");
        resultLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ffffff;");
        answerField.clear();
        answerField.setDisable(false);
        submitButton.setDisable(false);
        submitButton.setText("ยืนยัน ✔");
        submitButton.setOnAction(e -> handleAnswer());
        answerField.setOnAction(e -> handleAnswer());

        timerBarFill.setWidth(BAR_WIDTH);
        timerBarFill.setFill(Color.web("#4fc3f7"));
        timerLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #4fc3f7;");

        // rebuild contentArea
        contentArea.getChildren().clear();

        StackPane timerBar = new StackPane();
        timerBar.setAlignment(Pos.CENTER_LEFT);
        timerBar.getChildren().addAll(timerBarBg, timerBarFill);

        VBox timerBox = new VBox(4, timerLabel, timerBar);
        timerBox.setAlignment(Pos.CENTER);
        timerBox.setMaxWidth(BAR_WIDTH);

        StackPane captchaBox = new StackPane(captchaCanvas);
        captchaBox.setStyle("""
                -fx-background-color: #16213e;
                -fx-background-radius: 12;
                -fx-border-color: #0f3460;
                -fx-border-radius: 12;
                -fx-border-width: 2;
                -fx-padding: 12;
                """);

        HBox inputRow = new HBox(12, answerField, submitButton);
        inputRow.setAlignment(Pos.CENTER);

        contentArea.getChildren().addAll(
                roundLabel, timerBox, captchaBox, inputRow, resultLabel, scoreLabel
        );

        currentCaptcha = generateCaptchaText();
        roundLabel.setText("รอบที่ " + currentRound + " / " + TOTAL_ROUNDS);
        scoreLabel.setText("คะแนน: " + totalScore + " / " + (TOTAL_ROUNDS * POINTS_PER_ROUND));

        drawCaptcha(currentCaptcha);
        startTimerBar();

        answerField.requestFocus();
    }

    // --- สร้างข้อความ Captcha ตาม INT ---
    private String generateCaptchaText() {
        StatTier tier = player.getStatTier(player.getStat(StatType.INTELLIGENCE));
        int length = switch (tier) {
            case HIGH   -> 8;
            case MEDIUM -> 10;
            default     -> 12;
        };

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    // --- วาด Captcha บน Canvas ---
    private void drawCaptcha(String text) {
        GraphicsContext gc = captchaCanvas.getGraphicsContext2D();
        double w = captchaCanvas.getWidth();
        double h = captchaCanvas.getHeight();

        // พื้นหลัง
        gc.setFill(Color.web("#0d1b2a"));
        gc.fillRoundRect(0, 0, w, h, 12, 12);

        // เส้น noise
        gc.setStroke(Color.web("#1e3a5f"));
        gc.setLineWidth(1.5);
        for (int i = 0; i < 6; i++) {
            gc.strokeLine(
                    random.nextDouble() * w, random.nextDouble() * h,
                    random.nextDouble() * w, random.nextDouble() * h
            );
        }

        // จุด noise
        gc.setFill(Color.web("#2a4a6f"));
        for (int i = 0; i < 30; i++) {
            gc.fillOval(random.nextDouble() * w, random.nextDouble() * h, 3, 3);
        }

        // วาดตัวอักษรแต่ละตัวแบบเอียงสุ่ม
        double charSpacing = (w - 40) / text.length();
        for (int i = 0; i < text.length(); i++) {
            gc.save();

            // สีสุ่มในโทนสว่าง
            double r = 0.6 + random.nextDouble() * 0.4;
            double g = 0.6 + random.nextDouble() * 0.4;
            double b = 0.6 + random.nextDouble() * 0.4;
            gc.setFill(Color.color(r, g, b));

            // ขนาด font สุ่มเล็กน้อย
            int fontSize = 22 + random.nextInt(8);
            gc.setFont(Font.font("Times New Roman", FontWeight.BOLD, fontSize));

            // ตำแหน่งและการหมุน
            double x = 20 + i * charSpacing + random.nextDouble() * 4;
            double y = h / 2 + 8 + random.nextDouble() * 10 - 5;
            double angle = -15 + random.nextDouble() * 30;

            gc.translate(x, y);
            gc.rotate(angle);
            gc.fillText(String.valueOf(text.charAt(i)), 0, 0);
            gc.restore();
        }
    }

    // --- Timer bar ---
    private void startTimerBar() {
        if (countdownTimer != null) countdownTimer.stop();
        if (barAnim != null) barAnim.stop();

        barAnim = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(timerBarFill.widthProperty(), BAR_WIDTH)),
                new KeyFrame(Duration.seconds(SECONDS_PER_ROUND),
                        new KeyValue(timerBarFill.widthProperty(), 0))
        );
        barAnim.play();

        int[] timeLeft = {SECONDS_PER_ROUND};
        updateTimerLabel(timeLeft[0]);

        countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeLeft[0]--;
            updateTimerLabel(timeLeft[0]);

            if (timeLeft[0] <= 3) {
                timerBarFill.setFill(Color.web("#ef5350"));
                timerLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #ef5350;");
            } else if (timeLeft[0] <= 7) {
                timerBarFill.setFill(Color.web("#ffa726"));
                timerLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #ffa726;");
            }

            if (timeLeft[0] <= 0) {
                countdownTimer.stop();
                barAnim.stop();
                timeUp();
            }
        }));
        countdownTimer.setCycleCount(SECONDS_PER_ROUND);
        countdownTimer.play();
    }

    private void updateTimerLabel(int t) {
        timerLabel.setText("⏱  " + t + " วินาที");
    }

    private void timeUp() {
        answerField.setDisable(true);
        submitButton.setDisable(true);
        resultLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ef5350;");
        resultLabel.setText("⏰  หมดเวลา! คำตอบคือ: " + currentCaptcha);
        scoreLabel.setText("คะแนน: " + totalScore + " / " + (TOTAL_ROUNDS * POINTS_PER_ROUND));
        proceedAfterAnswer();
    }

    private void handleAnswer() {
        String input = answerField.getText().trim();

        if (input.isEmpty()) {
            resultLabel.setText("⚠ กรุณากรอกคำตอบ");
            return; // ไม่ stop timer
        }

        // stop timer หลังจากมีค่าแล้วเท่านั้น
        if (countdownTimer != null) countdownTimer.stop();
        if (barAnim != null) barAnim.stop();

        answerField.setDisable(true);
        submitButton.setDisable(true);
        timerBarFill.setWidth(0);

        if (input.equals(currentCaptcha)) {
            totalScore += POINTS_PER_ROUND;
            resultLabel.setText("✅  ถูกต้อง! +" + POINTS_PER_ROUND + " คะแนน");
            resultLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #66bb6a;");
        } else {
            resultLabel.setText("❌  ผิด! คำตอบคือ: " + currentCaptcha);
            resultLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ef5350;");
        }

        scoreLabel.setText("คะแนน: " + totalScore + " / " + (TOTAL_ROUNDS * POINTS_PER_ROUND));
        proceedAfterAnswer();
    }

    private void proceedAfterAnswer() {
        submitButton.setDisable(false);
        if (currentRound >= TOTAL_ROUNDS) {
            timerLabel.setText("");
            submitButton.setText("จบเกม 🎉");
            submitButton.setOnAction(e -> onFinish.run());
        } else {
            submitButton.setText("รอบถัดไป →");
            submitButton.setOnAction(e -> showReadyScreen());
        }
    }

    public int getTotalScore() { return totalScore; }
}