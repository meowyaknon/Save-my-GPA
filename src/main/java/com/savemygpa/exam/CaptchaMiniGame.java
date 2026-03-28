package com.savemygpa.exam;

import java.util.Random;

import com.savemygpa.player.Player;
import com.savemygpa.player.StatType;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class CaptchaMiniGame {

    private static final int    TOTAL_ROUNDS       = 5;
    private static final int    POINTS_PER_ROUND   = 10;
    private static final double FULL_BAR_WIDTH      = 700;
    private static final long   ENTER_COOLDOWN_MS   = 400;
    private static final String CHARS = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";

    private final Player   player;
    private final Random   random   = new Random();
    private final Runnable onFinish;

    private int     totalScore    = 0;
    private int     currentRound  = 0;
    private String  currentCaptcha = "";

    // ── สถานะ ──────────────────────────────────────────────────────────────
    private boolean isWaitingForNext = false; // รอกด Enter ไปรอบถัดไป
    private boolean isProcessing     = false; // กำลังประมวลผล ห้ามแทรก
    private long    lastEnterTime    = 0;     // cooldown timestamp

    // ── Timer pause ────────────────────────────────────────────────────────
    private boolean isTimerPaused  = false;
    private double  pausedBarWidth = 0;

    private Timeline countdownTimer;
    private Timeline barAnim;

    // ── UI nodes ───────────────────────────────────────────────────────────
    private final StackPane root             = new StackPane();
    private final VBox      contentArea      = new VBox(20);
    private final ImageView screenImageView  = new ImageView();
    private final ImageView characterImageView = new ImageView();
    private final Label     warningLabel     = new Label("⚠ Warning Captcha");
    private final Rectangle timerBarFill     = new Rectangle(FULL_BAR_WIDTH, 20);
    private final Rectangle timerBarBg       = new Rectangle(FULL_BAR_WIDTH, 20);
    private final Canvas    captchaCanvas    = new Canvas(700, 120);
    private final TextField answerField      = new TextField();
    private final Button    submitButton     = new Button("ยืนยัน ✔");
    private final Label     resultLabel      = new Label();
    private final Label     roundTextLabel   = new Label();
    private final Label     countdownLabel   = new Label(); // นับถอยหลังตอนหยุดเวลา

    public CaptchaMiniGame(Player player, Runnable onFinish) {
        this.player   = player;
        this.onFinish = onFinish;
    }

    // =========================================================================
    // getView
    // =========================================================================
    public StackPane getView() {
        root.getChildren().clear();
        root.setStyle("-fx-background-color: black;");

        ImageView bg = new ImageView(new Image(getClass().getResourceAsStream("/images/exam/code/computer_screen.png")));
        bg.setFitWidth(1920); bg.setFitHeight(1080);

        screenImageView.setFitWidth(1100);
        screenImageView.setFitHeight(600);
        screenImageView.setPreserveRatio(false);
        screenImageView.setTranslateY(-115);

        characterImageView.setFitWidth(600);
        characterImageView.setPreserveRatio(true);
        characterImageView.setMouseTransparent(true);
        characterImageView.setTranslateX(-89);
        StackPane.setAlignment(characterImageView, Pos.BOTTOM_LEFT);

        contentArea.setAlignment(Pos.CENTER);
        contentArea.setMaxSize(780, 490);
        contentArea.setPrefSize(780, 490);
        contentArea.setTranslateY(-75);
        contentArea.setVisible(false);

        warningLabel.setStyle("-fx-font-size: 60px; -fx-text-fill: red; -fx-font-weight: bold; -fx-background-color: rgba(0,0,0,0.8); -fx-padding: 20;");
        warningLabel.setTranslateY(-75);
        warningLabel.setVisible(false);

        countdownLabel.setStyle("-fx-font-size: 32px; -fx-text-fill: #4fc3f7; -fx-font-weight: bold;");

        root.getChildren().addAll(bg, screenImageView, characterImageView, contentArea, warningLabel);

        // ── Enter handler กลาง (root) ─────────────────────────────────────
        root.setFocusTraversable(true);
        root.setOnKeyPressed(e -> {
            if (e.getCode() != javafx.scene.input.KeyCode.ENTER) return;
            long now = System.currentTimeMillis();
            if (now - lastEnterTime < ENTER_COOLDOWN_MS) return; // cooldown
            lastEnterTime = now;
            if (!isProcessing) handleAnswer();
        });

        // ── Enter ใน TextField (ส่งคำตอบ) ───────────────────────────────
        answerField.setOnAction(e -> {
            long now = System.currentTimeMillis();
            if (now - lastEnterTime < ENTER_COOLDOWN_MS) return;
            lastEnterTime = now;
            if (!isProcessing && !isWaitingForNext) handleAnswer();
        });

        startNextRoundSequence();
        return root;
    }

    // =========================================================================
    // Round sequence
    // =========================================================================
    private void startNextRoundSequence() {
        if (currentRound >= TOTAL_ROUNDS) {
            showScoreSummary();
            return;
        }

        currentRound++;
        isWaitingForNext = false;
        isProcessing     = true; // ล็อกระหว่าง GIF/Warning

        contentArea.setVisible(false);
        warningLabel.setVisible(false);
        screenImageView.setVisible(true);

        String fileName = currentRound + "round.gif";
        screenImageView.setImage(new Image(getClass().getResourceAsStream("/images/exam/code/" + fileName)));
        characterImageView.setImage(new Image(getClass().getResourceAsStream("/images/exam/code/before_captcha2.gif")));

        PauseTransition p1 = new PauseTransition(Duration.seconds(4));
        p1.setOnFinished(e -> {
            screenImageView.setVisible(false);
            warningLabel.setVisible(true);
            characterImageView.setImage(new Image(getClass().getResourceAsStream("/images/exam/code/met_captcha.png")));
            PauseTransition p2 = new PauseTransition(Duration.seconds(3));
            p2.setOnFinished(ev -> { warningLabel.setVisible(false); prepareExamTask(); });
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

        // ล้าง countdownLabel ถ้าค้างอยู่
        contentArea.getChildren().remove(countdownLabel);
        countdownLabel.setText("");

        setupExamUI();
        currentCaptcha = generateCaptchaText();
        drawCaptcha(currentCaptcha);
        startTimerBar();

        isTimerPaused = false;
        isProcessing  = false; // พร้อมรับ input
        answerField.requestFocus();
    }

    // =========================================================================
    // handleAnswer — จุดเดียวที่ตัดสินคำตอบ
    // =========================================================================
    private void handleAnswer() {
        // ── กด Enter ไปรอบถัดไป ──────────────────────────────────────────
        if (isWaitingForNext) {
            isProcessing = true;
            characterImageView.setImage(new Image(getClass().getResourceAsStream("/images/exam/code/before_captcha2.gif")));
            startNextRoundSequence();
            return;
        }

        // ── ช่องว่าง → แจ้งเตือน + หยุดเวลา ─────────────────────────────
        String input = answerField.getText().trim();
        if (input.isEmpty()) {
            showEmptyWarning();
            return;
        }

        // ── ตรวจคำตอบ ────────────────────────────────────────────────────
        isProcessing = true;
        stopTimers();
        answerField.setDisable(true);
        submitButton.setDisable(true);
        isWaitingForNext = true;

        if (input.equals(currentCaptcha)) {
            totalScore += POINTS_PER_ROUND;
            resultLabel.setText("✅ ถูกต้อง! (Enter เพื่อไปต่อ)");
            resultLabel.setStyle("-fx-font-size: 22px; -fx-text-fill: #66bb6a; -fx-font-weight: bold;");
            characterImageView.setImage(new Image(getClass().getResourceAsStream("/images/exam/code/pass.png")));
        } else {
            resultLabel.setText("❌ ผิด! เฉลย: " + currentCaptcha + "  (Enter เพื่อไปต่อ)");
            resultLabel.setStyle("-fx-font-size: 22px; -fx-text-fill: #ef5350; -fx-font-weight: bold;");
            characterImageView.setImage(new Image(getClass().getResourceAsStream("/images/exam/code/fail.png")));
        }

        // หน่วง 800ms ก่อนปลดล็อก "ไปต่อ"
        PauseTransition delay = new PauseTransition(Duration.millis(800));
        delay.setOnFinished(e -> {
            isProcessing = false;
            submitButton.setDisable(false);
            submitButton.setText("ไปต่อ (Enter) →");
            root.requestFocus();
        });
        delay.play();
    }

    // =========================================================================
    // Empty warning + timer pause/resume
    // =========================================================================
    private void showEmptyWarning() {
        // หยุดเวลาครั้งแรกที่เตือน (ถ้ายังไม่ได้หยุด)
        /*
        if (!isTimerPaused) {
            isTimerPaused  = true;
            pausedBarWidth = timerBarFill.getWidth();
            stopTimers();
        }

        answerField.setDisable(true);  // ← ล็อกไม่ให้พิมพ์
        submitButton.setDisable(true); // ← ล็อกปุ่มด้วย
        */
        resultLabel.setText("⚠ กรุณากรอก CAPTCHA ก่อนกด Enter!");
        resultLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #ffe082; -fx-font-weight: bold;");

        // ถ้า countdownLabel ยังไม่อยู่ใน contentArea ให้เพิ่มเข้าไป
        /*
        if (!contentArea.getChildren().contains(countdownLabel)) {
            contentArea.getChildren().add(countdownLabel);
        }

        isProcessing = true; // ล็อกระหว่างนับถอยหลัง

        Timeline countdown = new Timeline(
                new KeyFrame(Duration.millis(0),    e -> countdownLabel.setText("▶ เดินเวลาใน 3...")),
                new KeyFrame(Duration.millis(1000), e -> countdownLabel.setText("▶ เดินเวลาใน 2...")),
                new KeyFrame(Duration.millis(2000), e -> countdownLabel.setText("▶ เดินเวลาใน 1...")),
                new KeyFrame(Duration.millis(3000), e -> {
                    countdownLabel.setText("");
                    contentArea.getChildren().remove(countdownLabel);
                    resultLabel.setText("");
                    isProcessing = false; // ปลดล็อก
                    resumeTimerBar();
                    answerField.requestFocus();
                })
        );
        countdown.play()
         */
    }
    /*
    private void resumeTimerBar() {
        isTimerPaused = false;
        answerField.setDisable(false);  // ← ปลดล็อก
        submitButton.setDisable(false); // ← ปลดล็อก
        answerField.clear();            // ← ล้างช่องด้วยเผื่อมีอะไรค้าง
        double ratio = pausedBarWidth / FULL_BAR_WIDTH;
        int remainingSec = Math.max(1, (int) Math.ceil(ratio * 10));
        timerBarFill.setWidth(pausedBarWidth);
        barAnim = new Timeline(new KeyFrame(Duration.seconds(remainingSec), new KeyValue(timerBarFill.widthProperty(), 0)));
        barAnim.play();
        // เปลี่ยนจาก handleAnswer() → handleTimeout()
        countdownTimer = new Timeline(new KeyFrame(Duration.seconds(remainingSec), e -> handleTimeout()));
        countdownTimer.play();
    }
     */

    private void handleTimeout() {
        if (isProcessing) return;
        isProcessing = true;
        stopTimers();
        answerField.setDisable(true);
        submitButton.setDisable(true);
        isWaitingForNext = true;

        resultLabel.setText("⏰ หมดเวลา! เฉลย: " + currentCaptcha + "  (Enter เพื่อไปต่อ)");
        resultLabel.setStyle("-fx-font-size: 22px; -fx-text-fill: #ef5350; -fx-font-weight: bold;");
        characterImageView.setImage(new Image(getClass().getResourceAsStream("/images/exam/code/fail.png")));

        PauseTransition delay = new PauseTransition(Duration.millis(800));
        delay.setOnFinished(e -> {
            isProcessing = false;
            submitButton.setDisable(false);
            submitButton.setText("ไปต่อ (Enter) →");
            root.requestFocus();
        });
        delay.play();
    }

    // =========================================================================
    // UI setup
    // =========================================================================
    private void setupExamUI() {
        contentArea.getChildren().clear();

        roundTextLabel.setText("รอบที่ " + currentRound + " / " + TOTAL_ROUNDS);
        roundTextLabel.setStyle("-fx-font-size: 30px; -fx-text-fill: white; -fx-font-weight: bold;");

        timerBarBg.setFill(Color.web("#2e2e4e"));
        timerBarBg.setWidth(FULL_BAR_WIDTH);
        timerBarFill.setFill(Color.web("#4fc3f7"));
        timerBarFill.setWidth(FULL_BAR_WIDTH);

        StackPane timerBox = new StackPane(timerBarBg, timerBarFill);
        timerBox.setAlignment(Pos.CENTER_LEFT);
        timerBox.setMaxWidth(FULL_BAR_WIDTH);

        answerField.setMaxWidth(400);
        answerField.setStyle("-fx-font-size: 22px;");

        submitButton.setPrefWidth(220);
        submitButton.setStyle("-fx-font-size: 20px; -fx-background-color: #4fc3f7; -fx-font-weight: bold;");
        submitButton.setOnAction(e -> {
            long now = System.currentTimeMillis();
            if (now - lastEnterTime < ENTER_COOLDOWN_MS) return;
            lastEnterTime = now;
            if (!isProcessing) handleAnswer();
        });

        contentArea.getChildren().addAll(roundTextLabel, timerBox, captchaCanvas, answerField, submitButton, resultLabel);
    }

    // =========================================================================
    // Timer
    // =========================================================================
    private void startTimerBar() {
        stopTimers();
        timerBarFill.setWidth(FULL_BAR_WIDTH);
        barAnim = new Timeline(new KeyFrame(Duration.seconds(10), new KeyValue(timerBarFill.widthProperty(), 0)));
        barAnim.play();
        countdownTimer = new Timeline(new KeyFrame(Duration.seconds(10), e -> handleTimeout()));
        countdownTimer.play();
    }

    private void stopTimers() {
        if (countdownTimer != null) countdownTimer.stop();
        if (barAnim != null)        barAnim.stop();
    }

    // =========================================================================
    // CAPTCHA generation
    // =========================================================================
    private String generateCaptchaText() {
        int intel = player.getStat(StatType.INTELLIGENCE);
        int length;
        if      (intel >= 80) length = 8;
        else if (intel >= 40) length = 10;
        else                  length = 12;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        return sb.toString();
    }

    private void drawCaptcha(String text) {
        GraphicsContext gc = captchaCanvas.getGraphicsContext2D();
        double w = captchaCanvas.getWidth();
        double h = captchaCanvas.getHeight();
        gc.clearRect(0, 0, w, h);
        gc.setFill(Color.web("#0d1b2a", 0.95));
        gc.fillRoundRect(0, 0, w, h, 20, 20);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 50));

        // คำนวณความกว้างข้อความจริงๆ แทน hardcode 100
        // Consolas 50px แต่ละตัวกว้างประมาณ 30px
        double textWidth = text.length() * 30.0;
        double x = (w - textWidth) / 2;

        gc.fillText(text, x, h / 2 + 15);
    }

    // =========================================================================
    // Score summary
    // =========================================================================
    private void showScoreSummary() {
        isProcessing = true;
        contentArea.setVisible(false);
        screenImageView.setVisible(false);

        String charImg = (totalScore >= 30)
                ? "/images/exam/code/pass.png"
                : "/images/exam/code/fail.png";
        characterImageView.setImage(new Image(getClass().getResourceAsStream(charImg)));

        VBox summaryBox = new VBox(25);
        summaryBox.setAlignment(Pos.CENTER);
        summaryBox.setMaxSize(600, 450);
        summaryBox.setTranslateY(-115);
        summaryBox.setStyle("-fx-background-color: rgba(10,20,40,0.95); -fx-background-radius: 30; -fx-padding: 50;");

        Label title = new Label("สรุปผลคะแนน");
        title.setStyle("-fx-font-size: 35px; -fx-text-fill: #ffe082; -fx-font-weight: bold;");

        Label score = new Label(totalScore + " / 50");
        score.setStyle("-fx-font-size: 70px; -fx-font-weight: bold; -fx-text-fill: "
                + (totalScore >= 30 ? "#66bb6a" : "#ef5350") + ";");

        Button finishBtn = new Button("ดำเนินการต่อ  ▶");
        finishBtn.setStyle("-fx-font-size: 22px; -fx-background-color: #4fc3f7; -fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 10 28 10 28;");
        finishBtn.setOnAction(e -> onFinish.run());

        summaryBox.getChildren().addAll(title, score, finishBtn);
        root.getChildren().add(summaryBox);

        summaryBox.setOpacity(0);
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(Duration.millis(500), summaryBox);
        ft.setFromValue(0); ft.setToValue(1); ft.play();

        // หลัง 1.5 วิ ปลดล็อกให้กด Enter ได้
        PauseTransition p = new PauseTransition(Duration.seconds(1.5));
        p.setOnFinished(e -> {
            isProcessing = false;
            root.setOnKeyPressed(ev -> {
                if (ev.getCode() == javafx.scene.input.KeyCode.ENTER) onFinish.run();
            });
            root.requestFocus();
        });
        p.play();
    }

    public int getTotalScore() { return totalScore; }
}