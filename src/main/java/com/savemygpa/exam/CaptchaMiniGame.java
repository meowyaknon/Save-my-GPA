package com.savemygpa.exam;

import com.savemygpa.player.Player;
import com.savemygpa.player.StatTier;
import com.savemygpa.player.StatType;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    private static final double BAR_WIDTH = 400;
    private static final String CHARS = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";

    private final Player player;
    private final Random random = new Random();
    private final Runnable onFinish;

    private int totalScore = 0;
    private int currentRound = 0;
    private String currentCaptcha = "";
    private boolean isWaitingForNext = false; // สำหรับหยุดรอการกด Enter เพื่อไปต่อ

    private Timeline countdownTimer;
    private Timeline barAnim;

    // --- UI Components ---
    private final StackPane root = new StackPane();
    private final VBox contentArea = new VBox(16);
    private final ImageView statusImageView = new ImageView();
    private final Label warningLabel = new Label("⚠ Warning Captcha");

    private final Label roundLabel = new Label();
    private final Label resultLabel = new Label();
    private final Label scoreLabel = new Label();
    private final Rectangle timerBarFill = new Rectangle(BAR_WIDTH, 18);
    private final Rectangle timerBarBg = new Rectangle(BAR_WIDTH, 18);
    private final Canvas captchaCanvas = new Canvas(360, 80);
    private final TextField answerField = new TextField();
    private final Button submitButton = new Button("ยืนยัน ✔");

    private static final String[] ROUND_FONTS = { "Consolas", "Courier New", "Lucida Console", "OCR A Extended", "Trebuchet MS" };

    public CaptchaMiniGame(Player player, Runnable onFinish) {
        this.player = player;
        this.onFinish = onFinish;
    }

    public StackPane getView() {
        root.getChildren().clear();
        root.setStyle("-fx-background-color: black;");

        // 1. พื้นหลังหน้าจอคอมพิวเตอร์
        ImageView bg = new ImageView(new Image(getClass().getResourceAsStream("/images/exam/code/computer_screen.png")));
        bg.setFitWidth(1920); bg.setFitHeight(1080);

        // 2. รูปสถานะมุมซ้ายล่าง
        statusImageView.setFitWidth(350);
        statusImageView.setPreserveRatio(true);
        statusImageView.setMouseTransparent(true);
        StackPane.setAlignment(statusImageView, Pos.BOTTOM_LEFT);
        StackPane.setMargin(statusImageView, new Insets(0, 0, 50, 50));

        // 3. ปรับขนาดและตำแหน่งให้พอดีช่องสีดำ
        contentArea.setAlignment(Pos.CENTER);
        contentArea.setMaxSize(740, 540);
        contentArea.setTranslateY(-40);

        warningLabel.setStyle("-fx-font-size: 60px; -fx-text-fill: #ff4444; -fx-font-weight: bold; -fx-background-color: rgba(0,0,0,0.8);");
        warningLabel.setVisible(false);

        root.getChildren().addAll(bg, statusImageView, contentArea, warningLabel);

        showReadySequence();
        return root;
    }

    private void showReadySequence() {
        isWaitingForNext = false;
        contentArea.getChildren().clear();
        // จังหวะที่ 1: กำลังเขียนโค้ด (before_captcha.gif)
        statusImageView.setImage(new Image(getClass().getResourceAsStream("/images/exam/code/before_cacptcha2.gif")));

        PauseTransition p1 = new PauseTransition(Duration.seconds(4));
        p1.setOnFinished(e -> {
            // จังหวะที่ 2: Warning + met_captcha.png
            warningLabel.setVisible(true);
            statusImageView.setImage(new Image(getClass().getResourceAsStream("/images/exam/code/met_captcha.png")));

            PauseTransition p2 = new PauseTransition(Duration.seconds(3));
            p2.setOnFinished(ev -> {
                warningLabel.setVisible(false);
                loadRound();
            });
            p2.play();
        });
        p1.play();
    }

    private void loadRound() {
        currentRound++;
        if (currentRound > TOTAL_ROUNDS) {
            onFinish.run();
            return;
        }

        // จังหวะที่ 3: ระหว่างทำ (while_type.png)
        statusImageView.setImage(new Image(getClass().getResourceAsStream("/images/exam/code/while_type.png")));

        resultLabel.setText("");
        answerField.clear();
        answerField.setDisable(false);
        submitButton.setDisable(false);
        submitButton.setText("ยืนยัน ✔");

        setupUI();

        currentCaptcha = generateCaptchaText();
        drawCaptcha(currentCaptcha);
        startTimerBar();
        answerField.requestFocus();
    }

    private void handleAnswer() {
        if (isWaitingForNext) {
            // Manual-advance: กด Enter เพื่อไปต่อรอบถัดไป
            if (currentRound < TOTAL_ROUNDS) {
                showReadySequence();
            } else {
                onFinish.run();
            }
            return;
        }

        stopTimers(); // หยุดเวลานาทีที่ตอบ
        String input = answerField.getText().trim();
        answerField.setDisable(true);
        isWaitingForNext = true;

        if (input.equals(currentCaptcha)) {
            // 4.2 ทำผ่าน (pass.png)
            totalScore += POINTS_PER_ROUND;
            resultLabel.setText("✅ ถูกต้อง! กด Enter เพื่อไปต่อ");
            resultLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #66bb6a; -fx-font-weight: bold;");
            statusImageView.setImage(new Image(getClass().getResourceAsStream("/images/exam/code/pass.png")));
        } else {
            // 4.1 ทำไม่ผ่าน (fail.png) พร้อมแสดงเฉลย
            resultLabel.setText("❌ ผิด! คำที่ถูกต้องคือ: " + currentCaptcha + "\n(กด Enter เพื่อเริ่มรอบใหม่)");
            resultLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #ef5350; -fx-font-weight: bold;");
            statusImageView.setImage(new Image(getClass().getResourceAsStream("/images/exam/code/fail.png")));
        }

        submitButton.setDisable(false);
        submitButton.setText("ไปต่อ (Enter) →");
        scoreLabel.setText("คะแนน: " + totalScore + " / " + (TOTAL_ROUNDS * POINTS_PER_ROUND));

        submitButton.requestFocus(); // ย้าย Focus มาที่ปุ่มเพื่อรับ Enter
    }

    private void setupUI() {
        contentArea.getChildren().clear();
        roundLabel.setText("ROUND " + currentRound + " / " + TOTAL_ROUNDS);
        roundLabel.setStyle("-fx-font-size: 22px; -fx-text-fill: white; -fx-font-weight: bold;");
        roundLabel.setAlignment(Pos.CENTER);
        roundLabel.setMaxWidth(Double.MAX_VALUE);

        answerField.setOnAction(e -> handleAnswer());
        submitButton.setOnAction(e -> handleAnswer());

        timerBarBg.setFill(Color.web("#2e2e4e"));
        timerBarFill.setFill(Color.web("#4fc3f7"));

        // จัด Timer Bar ให้อยู่กึ่งกลาง
        StackPane timerContainer = new StackPane(timerBarBg, timerBarFill);
        timerContainer.setAlignment(Pos.CENTER_LEFT);
        timerContainer.setMaxWidth(BAR_WIDTH);

        VBox layout = new VBox(15, roundLabel, timerContainer, new StackPane(captchaCanvas), answerField, submitButton, resultLabel, scoreLabel);
        layout.setAlignment(Pos.CENTER);
        contentArea.getChildren().add(layout);
    }

    private String generateCaptchaText() {
        StatTier tier = player.getStatTier(player.getStat(StatType.INTELLIGENCE));
        int length = switch (tier) { case HIGH -> 8; case MEDIUM -> 10; default -> 12; };
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        return sb.toString();
    }

    private void drawCaptcha(String text) {
        GraphicsContext gc = captchaCanvas.getGraphicsContext2D();
        double w = captchaCanvas.getWidth(); double h = captchaCanvas.getHeight();
        gc.setFill(Color.web("#0d1b2a")); gc.fillRoundRect(0, 0, w, h, 12, 12);
        gc.setStroke(Color.web("#1e3a5f"));
        for (int i = 0; i < 6; i++) gc.strokeLine(random.nextDouble()*w, random.nextDouble()*h, random.nextDouble()*w, random.nextDouble()*h);
        double charSpacing = (w - 40) / text.length();
        for (int i = 0; i < text.length(); i++) {
            gc.save();
            gc.setFill(Color.color(0.6+random.nextDouble()*0.4, 0.6+random.nextDouble()*0.4, 0.6+random.nextDouble()*0.4));
            gc.setFont(Font.font(ROUND_FONTS[4], FontWeight.BOLD, 24));
            double x = 20 + i * charSpacing; double y = h / 2 + 10;
            gc.translate(x, y); gc.rotate(-15 + random.nextDouble()*30);
            gc.fillText(String.valueOf(text.charAt(i)), 0, 0);
            gc.restore();
        }
    }

    private void startTimerBar() {
        int sec = 8 + (player.getStat(StatType.MOOD) / 20);
        barAnim = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(timerBarFill.widthProperty(), BAR_WIDTH)),
                new KeyFrame(Duration.seconds(sec), new KeyValue(timerBarFill.widthProperty(), 0)));
        barAnim.play();
        countdownTimer = new Timeline(new KeyFrame(Duration.seconds(sec), e -> handleAnswer()));
        countdownTimer.play();
    }

    private void stopTimers() {
        if (countdownTimer != null) countdownTimer.stop();
        if (barAnim != null) barAnim.stop();
    }

    public int getTotalScore() { return totalScore; }
}