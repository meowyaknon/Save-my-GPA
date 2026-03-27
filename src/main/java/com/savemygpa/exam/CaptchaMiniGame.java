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
        //StackPane.setMargin(characterImageView, new Insets(0, 500, 0, 0));

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

        // [FIX 3] ดัก Enter ที่ root StackPane เพื่อให้ทำงานได้ไม่ว่า focus จะอยู่ที่ไหน
        root.setFocusTraversable(true);
        root.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) handleAnswer();
        });

        startNextRoundSequence();
        return root;
    }

    private void startNextRoundSequence() {
        if (currentRound >= TOTAL_ROUNDS) {
            showScoreSummary();
            return;
        }

        currentRound++;
        isWaitingForNext = false;

        contentArea.setVisible(false);
        warningLabel.setVisible(false);
        screenImageView.setVisible(true);

        // [FIX 1] แก้ typo: "2rount.gif" → "2round.gif"
        String fileName = currentRound + "round.gif";
        screenImageView.setImage(new Image(getClass().getResourceAsStream("/images/exam/code/" + fileName)));

        // [FIX 1] เซ็ตรูปตัวละครเป็น before_captcha.gif ก่อน และเว้นเวลาให้แสดงก่อนที่จะเปลี่ยน
        characterImageView.setImage(new Image(getClass().getResourceAsStream("/images/exam/code/before_captcha2.gif")));

        PauseTransition p1 = new PauseTransition(Duration.seconds(4));
        p1.setOnFinished(e -> {
            screenImageView.setVisible(false);
            warningLabel.setVisible(true);
            // [FIX 1] เปลี่ยนรูปตัวละครหลังจาก before_captcha แสดงครบ 4 วินาทีแล้ว
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
        answerField.requestFocus();
    }

    private void handleAnswer() {
        if (isWaitingForNext) {
            // [FIX 1] ก่อนเริ่มรอบใหม่ รีเซ็ตรูปตัวละครกลับเป็น before_captcha.gif ให้ชัดเจน
            characterImageView.setImage(new Image(getClass().getResourceAsStream("/images/exam/code/before_captcha2.gif")));
            startNextRoundSequence();
            return;
        }

        stopTimers();
        String input = answerField.getText().trim();
        answerField.setDisable(true);
        answerField.setOnAction(null); // [FIX 3] ตัด Enter listener ของ TextField ออก
        submitButton.setDisable(true);
        isWaitingForNext = true;

        if (input.equals(currentCaptcha)) {
            totalScore += POINTS_PER_ROUND;
            resultLabel.setText("✅ ถูกต้อง! (Enter เพื่อไปต่อ)");
            resultLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #66bb6a; -fx-font-weight: bold;");
            characterImageView.setImage(new Image(getClass().getResourceAsStream("/images/exam/code/pass.png")));
        } else {
            // แยกกรณี "หมดเวลา" vs "ตอบผิด"
            if (input.isEmpty()) {
                resultLabel.setText("⏰ หมดเวลา! เฉลย: " + currentCaptcha + "\n(Enter เพื่อไปต่อ)");
            } else {
                resultLabel.setText("❌ ผิด! เฉลย: " + currentCaptcha + "\n(Enter เพื่อไปต่อ)");
            }
            resultLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #ef5350; -fx-font-weight: bold;");
            characterImageView.setImage(new Image(getClass().getResourceAsStream("/images/exam/code/fail.png")));
        }

        submitButton.setDisable(false);
        submitButton.setText("ไปต่อ (Enter)");
        root.requestFocus(); // [FIX 4] คืน focus ให้ root เพื่อให้ root.setOnKeyPressed รับ Enter ได้
    }

    private void setupExamUI() {
        contentArea.getChildren().clear();

        roundTextLabel.setText("ROUND " + currentRound + " / " + TOTAL_ROUNDS);
        // [FIX 2] ขยาย font ให้เหมาะกับพื้นที่ใหม่
        roundTextLabel.setStyle("-fx-font-size: 26px; -fx-text-fill: white; -fx-font-weight: bold;");

        timerBarBg.setFill(Color.web("#2e2e4e"));
        timerBarBg.setWidth(FULL_BAR_WIDTH);
        timerBarFill.setFill(Color.web("#4fc3f7"));
        timerBarFill.setWidth(FULL_BAR_WIDTH);

        StackPane timerBox = new StackPane(timerBarBg, timerBarFill);
        timerBox.setAlignment(Pos.CENTER_LEFT);
        timerBox.setMaxWidth(FULL_BAR_WIDTH);

        // [FIX 2] ขยาย TextField ให้กว้างขึ้น
        answerField.setMaxWidth(400);
        answerField.setPrefWidth(400);
        answerField.setStyle("-fx-font-size: 20px;");
        // [FIX 3] ไม่ set onAction ที่ TextField เพื่อป้องกัน double-call
        answerField.setOnAction(null);

        // [FIX 2] ขยายปุ่มให้ใหญ่ขึ้น
        submitButton.setPrefWidth(200);
        submitButton.setStyle("-fx-font-size: 18px;");
        // [FIX 3] ไม่ set onAction/onKeyPressed ที่ปุ่ม ใช้ root handler แทนทั้งหมด
        submitButton.setOnAction(null);
        submitButton.setOnKeyPressed(null);

        resultLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-font-weight: bold;");

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
        double w = captchaCanvas.getWidth();
        double h = captchaCanvas.getHeight();
        gc.clearRect(0, 0, w, h);
        gc.setFill(Color.web("#0d1b2a", 0.95));
        gc.fillRoundRect(0, 0, w, h, 15, 15);
        gc.setFill(Color.WHITE);
        // [FIX 2] ขยาย font ใน Canvas ให้ใหญ่ขึ้น
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 48));
        // จัดตัวอักษรให้อยู่กึ่งกลาง Canvas
        gc.fillText(text, w / 2 - (text.length() * 14), h / 2 + 18);
    }

    private void stopTimers() {
        if (countdownTimer != null) countdownTimer.stop();
        if (barAnim != null) barAnim.stop();
    }

    private void showScoreSummary() {
        contentArea.setVisible(false);
        screenImageView.setVisible(false);
        warningLabel.setVisible(false);

        // เปลี่ยนรูปตัวละครตามผลคะแนน
        String charImg = (totalScore >= 30)
                ? "/images/exam/code/pass.png"
                : "/images/exam/code/fail.png";
        characterImageView.setImage(new Image(getClass().getResourceAsStream(charImg)));

        // สร้างหน้าสรุปคะแนน
        VBox summaryBox = new VBox(24);
        summaryBox.setAlignment(javafx.geometry.Pos.CENTER);
        summaryBox.setMaxSize(600, 400);
        summaryBox.setTranslateY(-75);
        summaryBox.setStyle(
                "-fx-background-color: rgba(10,20,40,0.92);" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 40;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 24, 0.6, 0, 4);"
        );

        javafx.scene.text.Text title = new javafx.scene.text.Text("สรุปผลการสอบ");
        title.setFill(javafx.scene.paint.Color.web("#ffe082"));
        title.setFont(javafx.scene.text.Font.font("Comic Sans MS", javafx.scene.text.FontWeight.BOLD, 32));

        javafx.scene.control.Label scoreLabel = new javafx.scene.control.Label(totalScore + "  /  " + (TOTAL_ROUNDS * POINTS_PER_ROUND));
        scoreLabel.setStyle(
                "-fx-font-family: Comic Sans MS;" +
                        "-fx-font-size: 64px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + (totalScore >= 30 ? "#66bb6a" : "#ef5350") + ";"
        );

        javafx.scene.control.Label subLabel = new javafx.scene.control.Label("คะแนน");
        subLabel.setStyle("-fx-font-family: Comic Sans MS; -fx-font-size: 20px; -fx-text-fill: #aaaaaa;");

        javafx.scene.control.Button continueBtn = new javafx.scene.control.Button("ดำเนินการต่อ  ▶");
        continueBtn.setStyle(
                "-fx-font-family: Comic Sans MS;" +
                        "-fx-font-size: 18px;" +
                        "-fx-background-color: #4fc3f7;" +
                        "-fx-text-fill: #0a1628;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 10 28 10 28;" +
                        "-fx-cursor: hand;"
        );
        continueBtn.setOnAction(e -> onFinish.run());

        // Enter ก็ไปต่อได้
        root.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) onFinish.run();
        });

        summaryBox.getChildren().addAll(title, scoreLabel, subLabel, continueBtn);
        root.getChildren().add(summaryBox);

        // Fade in
        summaryBox.setOpacity(0);
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(500), summaryBox);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    public int getTotalScore() { return totalScore; }
}