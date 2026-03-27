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
    private static final double BAR_WIDTH = 350;
    private static final String CHARS = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";

    private final Player player;
    private final Random random = new Random();
    private final Runnable onFinish;

    private int totalScore = 0;
    private int currentRound = 0;
    private String currentCaptcha = "";
    private boolean isWaitingForNext = false;

    private Timeline countdownTimer;
    private Timeline barAnim;

    private final StackPane root = new StackPane();
    // [FIX 2] ขยาย contentArea ให้เต็มพื้นที่จอคอมสีดำ (ประมาณ 835x515)
    private final VBox contentArea = new VBox(20);
    private final ImageView screenImageView = new ImageView();
    private final ImageView characterImageView = new ImageView();
    private final Label warningLabel = new Label("⚠ Warning Captcha");

    // [FIX 2] ขยาย BAR_WIDTH ให้เต็มจอ
    private static final double FULL_BAR_WIDTH = 700;
    private final Rectangle timerBarFill = new Rectangle(FULL_BAR_WIDTH, 20);
    private final Rectangle timerBarBg = new Rectangle(FULL_BAR_WIDTH, 20);
    // [FIX 2] ขยาย Canvas ให้เต็มจอ
    private final Canvas captchaCanvas = new Canvas(700, 120);
    private final TextField answerField = new TextField();
    private final Button submitButton = new Button("ยืนยัน ✔");
    private final Label resultLabel = new Label();
    private final Label roundTextLabel = new Label();

    private boolean isProcessing = false; // ตัวล็อกกัน Enter รัว

    public CaptchaMiniGame(Player player, Runnable onFinish) {
        this.player = player;
        this.onFinish = onFinish;
    }

    public StackPane getView() {
        root.getChildren().clear();
        root.setStyle("-fx-background-color: black;");

        // 1. Background
        ImageView bg = new ImageView(new Image(getClass().getResourceAsStream("/images/exam/code/computer_screen.png")));
        bg.setFitWidth(1920); bg.setFitHeight(1080);

        // 2. จอคอม (1round, 2round...)
        screenImageView.setFitWidth(1100);
        screenImageView.setFitHeight(600);
        screenImageView.setPreserveRatio(false);
        screenImageView.setTranslateY(-115);

        // 3. ตัวละคร (ซ้ายล่าง)
        characterImageView.setFitWidth(600);
        characterImageView.setPreserveRatio(true);
        characterImageView.setMouseTransparent(true);
        characterImageView.setTranslateX(-89);
        StackPane.setAlignment(characterImageView, Pos.BOTTOM_LEFT);

        // [FIX 2] ขยาย contentArea ให้เต็มพื้นที่จอคอมสีดำ
        contentArea.setAlignment(Pos.CENTER);
        contentArea.setMaxSize(780, 490);
        contentArea.setPrefSize(780, 490);
        contentArea.setTranslateY(-75);
        contentArea.setVisible(false);

        // 5. Warning
        warningLabel.setStyle("-fx-font-size: 60px; -fx-text-fill: red; -fx-font-weight: bold; -fx-background-color: rgba(0,0,0,0.8); -fx-padding: 20;");
        warningLabel.setTranslateY(-75);
        warningLabel.setVisible(false);

        root.getChildren().addAll(bg, screenImageView, characterImageView, contentArea, warningLabel);

        // 1. ดัก Enter ที่ Root (สำหรับจังหวะ "ไปต่อ")
        root.setFocusTraversable(true);
        root.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                if (!isProcessing && isWaitingForNext) {
                    handleAnswer();
                }
            }
        });

        // 2. ดัก Enter ที่ TextField (สำหรับจังหวะ "ส่งคำตอบ")
        answerField.setOnAction(e -> {
            if (!isProcessing && !isWaitingForNext) {
                handleAnswer();
            }
        });

        startNextRoundSequence();
        return root;
    }

    private void startNextRoundSequence() {
        isProcessing = true;
        if (currentRound >= TOTAL_ROUNDS) {
            showScoreSummary();
            return;
        }

        currentRound++;
        isWaitingForNext = false;
        isProcessing = true; // ล็อกทันทีห้ามกดแทรกจังหวะ GIF/Warning

        contentArea.setVisible(false);
        warningLabel.setVisible(false);
        screenImageView.setVisible(true);

        String fileName = currentRound + "round.gif";
        try {
            screenImageView.setImage(new Image(getClass().getResourceAsStream("/images/exam/code/" + fileName)));
            characterImageView.setImage(new Image(getClass().getResourceAsStream("/images/exam/code/before_captcha2.gif")));
        } catch (Exception e) { System.err.println("File not found: " + fileName); }

        PauseTransition p1 = new PauseTransition(Duration.seconds(4));
        p1.setOnFinished(e -> {
            screenImageView.setVisible(false);
            warningLabel.setVisible(true);
            characterImageView.setImage(new Image(getClass().getResourceAsStream("/images/exam/code/met_captcha.png")));

            PauseTransition p2 = new PauseTransition(Duration.seconds(3));
            p2.setOnFinished(ev -> {
                warningLabel.setVisible(false);
                prepareExamTask();
            });
            p2.play();
        });
        p1.play();
    }

    private void prepareExamTask() {
        contentArea.setVisible(true);
        characterImageView.setImage(new Image(getClass().getResourceAsStream("/images/exam/code/while_type.png")));

        resultLabel.setText("");
        answerField.clear();
        answerField.setDisable(false);
        submitButton.setDisable(false);
        submitButton.setText("ยืนยัน ✔");

        setupExamUI();
        currentCaptcha = generateCaptchaText();
        drawCaptcha(currentCaptcha);
        startTimerBar();

        isProcessing = false; // พร้อมให้กด Enter ส่งคำตอบได้แล้ว
        answerField.requestFocus();
    }

    private void handleAnswer() {
        if (isProcessing) return; // บล็อกรัวๆ

        // ถ้าอยู่ในหน้าเฉลยแล้วกด Enter ให้ไปรอบถัดไป
        if (isWaitingForNext) {
            isProcessing = true;
            characterImageView.setImage(new Image(getClass().getResourceAsStream("/images/exam/code/before_captcha2.gif")));
            startNextRoundSequence();
            return;
        }

        isProcessing = true; // ล็อกทันทีเพื่อประมวลผลคำตอบ
        stopTimers();

        String input = answerField.getText().trim();
        answerField.setDisable(true);
        submitButton.setDisable(true);
        isWaitingForNext = true;

        if (input.equals(currentCaptcha)) {
            totalScore += POINTS_PER_ROUND;
            resultLabel.setText("✅ ถูกต้อง! (Enter เพื่อไปต่อ)");
            resultLabel.setStyle("-fx-font-size: 22px; -fx-text-fill: #66bb6a; -fx-font-weight: bold;");
            characterImageView.setImage(new Image(getClass().getResourceAsStream("/images/exam/code/pass.png")));
        } else {
            resultLabel.setText("❌ ผิด! เฉลย: " + currentCaptcha + " (Enter เพื่อไปต่อ)");
            resultLabel.setStyle("-fx-font-size: 22px; -fx-text-fill: #ef5350; -fx-font-weight: bold;");
            characterImageView.setImage(new Image(getClass().getResourceAsStream("/images/exam/code/fail.png")));
        }

        // หน่วงเวลา 0.8 วินาที ก่อนจะอนุญาตให้กด Enter "ไปต่อ" ได้
        PauseTransition delay = new PauseTransition(Duration.millis(800));
        delay.setOnFinished(e -> {
            isProcessing = false; // ปลดล็อก
            submitButton.setDisable(false);
            submitButton.setText("ไปต่อ (Enter) →");
            root.requestFocus();
        });
        delay.play();
    }

    private void setupExamUI() {
        contentArea.getChildren().clear();
        roundTextLabel.setText("ROUND " + currentRound + " / " + TOTAL_ROUNDS);
        roundTextLabel.setStyle("-fx-font-size: 30px; -fx-text-fill: white; -fx-font-weight: bold;");

        timerBarBg.setFill(Color.web("#2e2e4e"));
        timerBarFill.setFill(Color.web("#4fc3f7"));
        timerBarFill.setWidth(FULL_BAR_WIDTH);
        StackPane timerBox = new StackPane(timerBarBg, timerBarFill);
        timerBox.setAlignment(Pos.CENTER_LEFT);
        timerBox.setMaxWidth(FULL_BAR_WIDTH);

        answerField.setMaxWidth(400);
        answerField.setStyle("-fx-font-size: 22px;");
        answerField.setOnAction(null); // ยกเลิก Action ภายในเพื่อกันรัว

        submitButton.setPrefWidth(220);
        submitButton.setStyle("-fx-font-size: 20px; -fx-background-color: #4fc3f7; -fx-font-weight: bold;");
        submitButton.setOnAction(e -> { if(!isProcessing) handleAnswer(); });

        contentArea.getChildren().addAll(roundTextLabel, timerBox, captchaCanvas, answerField, submitButton, resultLabel);
    }

    private void startTimerBar() {
        stopTimers();
        int sec = 10;
        timerBarFill.setWidth(FULL_BAR_WIDTH); // [FIX 2] ใช้ FULL_BAR_WIDTH

        barAnim = new Timeline(new KeyFrame(Duration.seconds(sec), new KeyValue(timerBarFill.widthProperty(), 0)));
        barAnim.play();

        countdownTimer = new Timeline(new KeyFrame(Duration.seconds(sec), e -> handleAnswer()));
        countdownTimer.play();
    }

    private String generateCaptchaText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        return sb.toString();
    }

    private void drawCaptcha(String text) {
        GraphicsContext gc = captchaCanvas.getGraphicsContext2D();
        double w = captchaCanvas.getWidth(); double h = captchaCanvas.getHeight();
        gc.clearRect(0, 0, w, h);
        gc.setFill(Color.web("#0d1b2a", 0.95)); gc.fillRoundRect(0, 0, w, h, 20, 20);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 50));
        gc.fillText(text, w/2 - 100, h/2 + 15);
    }

    private void stopTimers() {
        if (countdownTimer != null) countdownTimer.stop();
        if (barAnim != null) barAnim.stop();
    }

    private void showScoreSummary() {
        isProcessing = true; // บล็อกถาวรจนกว่าจะพร้อม
        contentArea.setVisible(false);
        screenImageView.setVisible(false);

        // เปลี่ยนรูปตัวละครตามผลคะแนน
        String charImg = (totalScore >= 30)
                ? "/images/exam/code/pass.png"
                : "/images/exam/code/fail.png";
        characterImageView.setImage(new Image(getClass().getResourceAsStream(charImg)));

        VBox summaryBox = new VBox(25);
        summaryBox.setAlignment(Pos.CENTER);
        summaryBox.setMaxSize(600, 450);
        summaryBox.setTranslateY(-115);
        summaryBox.setStyle("-fx-background-color: rgba(10,20,40,0.95); -fx-background-radius: 30; -fx-padding: 50;");

        Label title = new Label("Summary Score");
        title.setStyle("-fx-font-size: 35px; -fx-text-fill: #ffe082; -fx-font-weight: bold;");

        Label score = new Label(totalScore + " / 50");
        score.setStyle("-fx-font-size: 70px; -fx-font-weight: bold; -fx-text-fill: " + (totalScore >= 30 ? "#66bb6a" : "#ef5350"));

        Button finishBtn = new Button("ดำเนินการต่อ  ▶");
        finishBtn.setStyle("-fx-font-size: 22px; -fx-background-color: #4fc3f7; -fx-font-weight: bold;");
        finishBtn.setOnAction(e -> onFinish.run());

        summaryBox.getChildren().addAll(title, score, finishBtn);
        root.getChildren().add(summaryBox);

        PauseTransition p = new PauseTransition(Duration.seconds(1.5));
        p.setOnFinished(e -> {
            isProcessing = false;
            root.setOnKeyPressed(ev -> { if(ev.getCode() == javafx.scene.input.KeyCode.ENTER) onFinish.run(); });
        });
        p.play();
    }

    public int getTotalScore() { return totalScore; }
}