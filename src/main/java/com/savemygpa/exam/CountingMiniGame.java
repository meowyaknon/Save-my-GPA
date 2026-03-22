package com.savemygpa.exam;

import com.savemygpa.player.Player;
import com.savemygpa.player.StatTier;
import com.savemygpa.player.StatType;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import java.util.Random;

public class CountingMiniGame {

    private static final int TOTAL_ROUNDS = 5;
    private static final int POINTS_PER_ROUND = 10;
    private static final int SECONDS_PER_ROUND = 5;
    private static final double BAR_WIDTH = 400;

    private final Player player;
    private final Random random = new Random();
    private final Runnable onFinish;

    private int totalScore = 0;
    private int currentRound = 0;
    private int currentDuckCount = 0;

    // FIX: เก็บ barAnim เป็น field เพื่อ stop ได้ทุกเวลา
    private Timeline countdownTimer;
    private Timeline barAnim;

    private final Image[] duckImages = new Image[5];
    private final Image[] lizardImages = new Image[4];

    // --- UI Components ---
    private final VBox root = new VBox(16);
    private final VBox contentArea = new VBox(16); // FIX: แยก content area ออกมาเพื่อ swap ได้
    private final Label roundLabel = new Label();
    private final Label timerLabel = new Label();
    private final Rectangle timerBarFill = new Rectangle(BAR_WIDTH, 18);
    private final Rectangle timerBarBg = new Rectangle(BAR_WIDTH, 18);
    private final FlowPane animalPane = new FlowPane(10, 10);
    private final Label resultLabel = new Label();
    private final Label scoreLabel = new Label();
    private final TextField answerField = new TextField();
    private final Button submitButton = new Button("ยืนยัน ✔");

    public CountingMiniGame(Player player, Runnable onFinish) {
        this.player = player;
        this.onFinish = onFinish;
        loadImages();
    }

    private void loadImages() {
        for (int i = 0; i < duckImages.length; i++) {
            duckImages[i] = new Image(
                    getClass().getResourceAsStream("/images/exam/math/ped/ped" + (i + 1) + ".png"),
                    58, 58, true, true
            );
        }
        for (int i = 0; i < lizardImages.length; i++) {
            lizardImages[i] = new Image(
                    getClass().getResourceAsStream("/images/exam/math/here/here" + (i + 1) + ".png"),
                    58, 58, true, true
            );
        }
    }

    public VBox getView() {
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(24));
        root.setStyle("""
                -fx-background-color: #1a1a2e;
                -fx-background-radius: 16;
                """);

        Label title = new Label("🦆 เกมนับเป็ด");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #f0e68c;");

        Label hint = new Label("นับเฉพาะเป็ด 🦆  อย่านับตัวเงินตัวทอง 🦎");
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

        // --- Animal pane ---
        animalPane.setAlignment(Pos.CENTER);
        animalPane.setPadding(new Insets(16));
        animalPane.setMaxWidth(500);
        animalPane.setStyle("""
                -fx-background-color: #16213e;
                -fx-background-radius: 12;
                -fx-border-color: #0f3460;
                -fx-border-radius: 12;
                -fx-border-width: 2;
                """);

        // --- Input ---
        answerField.setMaxWidth(100);
        answerField.setPromptText("0");
        answerField.setStyle("""
                -fx-font-size: 18px;
                -fx-alignment: center;
                -fx-background-color: #16213e;
                -fx-text-fill: white;
                -fx-border-color: #4fc3f7;
                -fx-border-radius: 8;
                -fx-background-radius: 8;
                """);

        submitButton.setStyle("""
                -fx-font-size: 14px;
                -fx-background-color: #4fc3f7;
                -fx-text-fill: #1a1a2e;
                -fx-font-weight: bold;
                -fx-background-radius: 8;
                -fx-cursor: hand;
                -fx-padding: 8 20;
                """);

        HBox inputRow = new HBox(12, answerField, submitButton);
        inputRow.setAlignment(Pos.CENTER);

        resultLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ffffff;");
        scoreLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #f0e68c;");

        // FIX: contentArea เก็บส่วนที่เปลี่ยนได้ แยกจาก title/hint
        contentArea.setAlignment(Pos.CENTER);
        contentArea.getChildren().addAll(
                roundLabel, timerBox,
                animalPane, inputRow, resultLabel, scoreLabel
        );

        root.getChildren().addAll(title, hint, contentArea);

        // FIX: เริ่มด้วย ready screen แทน countdown ทันที
        showReadyScreen();
        return root;
    }

    // FIX: เพิ่ม ready screen ให้ผู้เล่นกดก่อนเริ่ม
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
        // FIX: rebuild contentArea แทนการ add/remove ปุ่มแบบเดิม
        answerField.setOnAction(e -> handleAnswer());
        timerLabel.setText("");
        timerBarFill.setWidth(BAR_WIDTH);

        contentArea.getChildren().clear();

        StackPane timerBar = new StackPane();
        timerBar.setAlignment(Pos.CENTER_LEFT);
        timerBar.getChildren().addAll(timerBarBg, timerBarFill);

        VBox timerBox = new VBox(4, timerLabel, timerBar);
        timerBox.setAlignment(Pos.CENTER);
        timerBox.setMaxWidth(BAR_WIDTH);

        HBox inputRow = new HBox(12, answerField, submitButton);
        inputRow.setAlignment(Pos.CENTER);

        contentArea.getChildren().addAll(
                roundLabel, timerBox, animalPane, inputRow, resultLabel, scoreLabel
        );

        currentRound++;
        resultLabel.setText("");
        resultLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ffffff;");
        answerField.clear();
        answerField.setDisable(false);
        submitButton.setDisable(false);
        submitButton.setText("ยืนยัน ✔");
        submitButton.setOnAction(e -> handleAnswer());

        int[] counts = generateCounts();
        currentDuckCount = counts[0];

        roundLabel.setText("รอบที่ " + currentRound + " / " + TOTAL_ROUNDS);
        scoreLabel.setText("คะแนน: " + totalScore + " / " + (TOTAL_ROUNDS * POINTS_PER_ROUND));

        buildAnimalPane(currentDuckCount, counts[1]);
        startTimerBar();
    }

    private void buildAnimalPane(int ducks, int lizards) {
        ImageView[] views = new ImageView[ducks + lizards];

        for (int i = 0; i < ducks; i++) {
            ImageView iv = new ImageView(duckImages[random.nextInt(duckImages.length)]);
            if (random.nextBoolean()) iv.setScaleX(-1); // flip
            views[i] = iv;
        }
        for (int i = 0; i < lizards; i++) {
            ImageView iv = new ImageView(lizardImages[random.nextInt(lizardImages.length)]);
            if (random.nextBoolean()) iv.setScaleX(-1); // flip
            views[ducks + i] = iv;
        }

        for (int i = views.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            ImageView tmp = views[i]; views[i] = views[j]; views[j] = tmp;
        }

        animalPane.getChildren().setAll(views);
    }

    private void startTimerBar() {
        if (countdownTimer != null) countdownTimer.stop();
        // FIX: barAnim เป็น field หยุดได้เวลาตอบก่อนหมดเวลา
        if (barAnim != null) barAnim.stop();

        timerBarFill.setWidth(BAR_WIDTH);
        timerBarFill.setFill(Color.web("#4fc3f7"));
        timerLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #4fc3f7;");

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
            } else if (timeLeft[0] <= 6) {
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
        resultLabel.setText("⏰  หมดเวลา! เป็ดมีทั้งหมด " + currentDuckCount + " ตัว");
        scoreLabel.setText("คะแนน: " + totalScore + " / " + (TOTAL_ROUNDS * POINTS_PER_ROUND));
        proceedAfterAnswer();
    }

    private void handleAnswer() {
        if (countdownTimer != null) countdownTimer.stop();
        // FIX: หยุด barAnim ด้วยเวลาตอบ
        if (barAnim != null) barAnim.stop();

        int answer;
        try {
            answer = Integer.parseInt(answerField.getText().trim());
        } catch (NumberFormatException e) {
            resultLabel.setText("⚠ กรุณากรอกตัวเลข");
            startTimerBar(); // เริ่มนับใหม่ถ้ากรอกผิด
            return;
        }

        answerField.setDisable(true);
        submitButton.setDisable(true);
        timerBarFill.setWidth(0);

        if (answer == currentDuckCount) {
            totalScore += POINTS_PER_ROUND;
            resultLabel.setText("✅  ถูกต้อง! +" + POINTS_PER_ROUND + " คะแนน");
            resultLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #66bb6a;");
        } else {
            resultLabel.setText("❌  ผิด! เป็ดมีทั้งหมด " + currentDuckCount + " ตัว");
            resultLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ef5350;");
        }

        scoreLabel.setText("คะแนน: " + totalScore + " / " + (TOTAL_ROUNDS * POINTS_PER_ROUND));
        proceedAfterAnswer();
    }

    // FIX: ใช้ submitButton แทนการเพิ่ม nextBtn เข้า root
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

    private int[] generateCounts() {
        StatTier tier = player.getStatTier(player.getStat(StatType.INTELLIGENCE));
        int duckMin, duckMax, lizardMin, lizardMax;
        switch (tier) {
            case HIGH   -> { duckMin = 5;  duckMax = 8;  lizardMin = 0; lizardMax = 2; }
            case MEDIUM -> { duckMin = 8;  duckMax = 12; lizardMin = 2; lizardMax = 4; }
            default     -> { duckMin = 12; duckMax = 16; lizardMin = 4; lizardMax = 6; }
        }
        return new int[]{
                random.nextInt(duckMax - duckMin + 1) + duckMin,
                random.nextInt(lizardMax - lizardMin + 1) + lizardMin
        };
    }

    public int getTotalScore() { return totalScore; }
}