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
import javafx.scene.text.Font;
import javafx.util.Duration;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CountingMiniGame {

    private static final int TOTAL_ROUNDS = 5;
    private static final int POINTS_PER_ROUND = 10;
    private static final double BAR_WIDTH = 400;

    private final Player player;
    private final Random random = new Random();
    private final Runnable onFinish;

    private int totalScore = 0;
    private int currentRound = 0;
    private int currentDuckCount = 0;

    private Timeline countdownTimer;
    private Timeline barAnim;

    private final Image[] duckImages = new Image[5];
    private final Image[] lizardImages = new Image[4];

    // --- UI Components ---
    private final StackPane root = new StackPane(); // เปลี่ยนเป็น StackPane เพื่อซ้อน layer ได้
    private final VBox contentArea = new VBox(16);
    private final Label roundLabel = new Label();
    private final Label timerLabel = new Label();
    private final Rectangle timerBarFill = new Rectangle(BAR_WIDTH, 18);
    private final Rectangle timerBarBg = new Rectangle(BAR_WIDTH, 18);
    private final Pane animalPane = new Pane();
    private final Label resultLabel = new Label();
    private final Label scoreLabel = new Label();
    private final TextField answerField = new TextField();
    private final Button submitButton = new Button("ยืนยัน ✔");

    private int getSecondsPerRound() {
        return 5 + (player.getStat(StatType.MOOD) / 20);
    }

    public CountingMiniGame(Player player, Runnable onFinish) {
        this.player = player;
        this.onFinish = onFinish;
        loadImages();
    }

    private void loadImages() {
        for (int i = 0; i < duckImages.length; i++) {
            duckImages[i] = new Image(
                    getClass().getResourceAsStream("/images/exam/math/ped/ped" + (i + 1) + ".png")
            );
        }
        for (int i = 0; i < lizardImages.length; i++) {
            lizardImages[i] = new Image(
                    getClass().getResourceAsStream("/images/exam/math/here/here" + (i + 1) + ".png")
            );
        }
    }

    public StackPane getView() {
        root.setStyle("-fx-background-color: #0a0a0a;");
        contentArea.setAlignment(Pos.CENTER);
        root.getChildren().add(contentArea);
        showReadyScreen();
        return root;
    }

    // --- Countdown ตัวเลขใหญ่กลางจอ ---
    private void showCountdown(Runnable after) {
        contentArea.getChildren().clear();

        Label countLabel = new Label("3");
        countLabel.setStyle("""
                -fx-font-size: 200px;
                -fx-font-weight: bold;
                -fx-text-fill: black;
                """);

        StackPane countPane = new StackPane(countLabel);
        countPane.setStyle("-fx-background-color: #0a0a0a;");
        countPane.setPrefSize(1920, 1080);

        contentArea.getChildren().add(countPane);

        String[] steps = {"3", "2", "1", "GO!"};
        Timeline tl = new Timeline();
        for (int i = 0; i < steps.length; i++) {
            final String s = steps[i];
            tl.getKeyFrames().add(new KeyFrame(Duration.seconds(i), e -> {
                countLabel.setText(s);
                // สีเปลี่ยนตามขั้น
                countLabel.setStyle("""
                        -fx-font-size: 200px;
                        -fx-font-weight: bold;
                        -fx-text-fill: """ + (s.equals("GO!") ? "#66bb6a" : "white") + ";"
                );
            }));
        }
        tl.getKeyFrames().add(new KeyFrame(Duration.seconds(steps.length), e -> after.run()));
        tl.play();
    }

    // --- Game Round (Dark theme เดิม) ---
    private void loadRound() {
        answerField.setOnAction(e -> handleAnswer());
        timerLabel.setText("");
        timerBarFill.setWidth(BAR_WIDTH);

        contentArea.getChildren().clear();

        // --- Timer bar ---
        timerBarBg.setFill(Color.web("#2e2e4e"));
        timerBarBg.setArcWidth(12); timerBarBg.setArcHeight(12);
        timerBarFill.setFill(Color.web("#4fc3f7"));
        timerBarFill.setArcWidth(12); timerBarFill.setArcHeight(12);

        StackPane timerBar = new StackPane();
        timerBar.setAlignment(Pos.CENTER_LEFT);
        timerBar.getChildren().addAll(timerBarBg, timerBarFill);

        timerLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #4fc3f7;");
        VBox timerBox = new VBox(4, timerLabel, timerBar);
        timerBox.setAlignment(Pos.CENTER);
        timerBox.setMaxWidth(BAR_WIDTH);

        roundLabel.setStyle("-fx-font-size: 22px; -fx-text-fill: #cccccc;");

        answerField.setMaxWidth(150);
        answerField.setPromptText("0");
        answerField.setStyle("""
            -fx-font-size: 24px;
            -fx-alignment: center;
            -fx-background-color: #16213e;
            -fx-text-fill: white;
            -fx-border-color: #4fc3f7;
            -fx-border-radius: 8;
            -fx-background-radius: 8;
            """);

        submitButton.setStyle("""
            -fx-font-size: 18px;
            -fx-background-color: #4fc3f7;
            -fx-text-fill: #1a1a2e;
            -fx-font-weight: bold;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            -fx-padding: 10 28;
            """);
        submitButton.setOnAction(e -> handleAnswer());

        HBox inputRow = new HBox(12, answerField, submitButton);
        inputRow.setAlignment(Pos.CENTER);

        resultLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #ffffff;");
        scoreLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #f0e68c;");

        // --- UI overlay บน bg ---
        VBox uiOverlay = new VBox(16, roundLabel, timerBox, inputRow, resultLabel, scoreLabel);
        uiOverlay.setAlignment(Pos.BOTTOM_CENTER);
        uiOverlay.setPadding(new Insets(0, 0, 40, 0));

        // --- animalPane วางบน bg ---
        animalPane.setPrefSize(1920, 1080);

        // --- BG ---
        ImageView bg = loadFullImg("/images/exam/math/Suan.jpg");

        // ซ้อน bg > animalPane > ui
        StackPane gameStack = new StackPane(bg, animalPane, uiOverlay);
        gameStack.setStyle("-fx-background-color: #0a0a0a;");

        contentArea.getChildren().add(gameStack);

        currentRound++;
        resultLabel.setText("");
        answerField.clear();
        answerField.setDisable(false);
        submitButton.setDisable(false);
        submitButton.setText("ยืนยัน ✔");

        int[] counts = generateCounts();
        currentDuckCount = counts[0];

        roundLabel.setText("รอบที่ " + currentRound + " / " + TOTAL_ROUNDS);
        scoreLabel.setText("คะแนน: " + totalScore + " / " + (TOTAL_ROUNDS * POINTS_PER_ROUND));

        buildAnimalPane(currentDuckCount, counts[1]);
        startTimerBar();
        answerField.requestFocus();
    }
    private static final int[][][] PED_POSITIONS = {
            {{647,284,1},{1206,188,0},{152,414,1},{446,703,0},{1779,928,0}},  // ped1
            {{1487,360,0},{1205,522,1},{1573,594,0},{1358,712,0},{1149,778,1}}, // ped2
            {{1331,448,0},{1699,516,0},{1720,637,1},{1613,777,0},{855,837,1}},  // ped3
            {{241,42,0},{580,105,0},{1494,83,1},{1733,73,1},{195,923,0}},       // ped4
            {{1245,388,1},{1663,434,0},{970,676,1},{1233,893,1},{1366,891,0}}   // ped5
    };

    private static final int[][][] HERE_POSITIONS = {
            {{1368,214,0},{447,304,1},{394,857,1}},   // here1
            {{1400,591,0},{1459,796,1},{1093,875,0}}, // here2
            {{763,163,0},{1059,168,1},{320,673,1}},   // here3
            {{1543,482,1},{1238,647,1},{821,673,0}}   // here4
    };

    private void buildAnimalPane(int ducks, int lizards) {
        animalPane.getChildren().clear();

        // รวมตำแหน่งทั้งหมดของเป็ด (imgIdx, slotIdx)
        List<int[]> allDuckSlots = new ArrayList<>();
        for (int img = 0; img < PED_POSITIONS.length; img++) {
            for (int slot = 0; slot < PED_POSITIONS[img].length; slot++) {
                allDuckSlots.add(new int[]{img, slot});
            }
        }
        Collections.shuffle(allDuckSlots, random);

        // รวมตำแหน่งทั้งหมดของเงินทอง
        List<int[]> allLizardSlots = new ArrayList<>();
        for (int img = 0; img < HERE_POSITIONS.length; img++) {
            for (int slot = 0; slot < HERE_POSITIONS[img].length; slot++) {
                allLizardSlots.add(new int[]{img, slot});
            }
        }
        Collections.shuffle(allLizardSlots, random);

        // จำกัดไม่เกินจำนวน slot ที่มี
        ducks   = Math.min(ducks,   allDuckSlots.size());
        lizards = Math.min(lizards, allLizardSlots.size());
        currentDuckCount = ducks;

        for (int i = 0; i < ducks; i++) {
            int imgIdx  = allDuckSlots.get(i)[0];
            int slotIdx = allDuckSlots.get(i)[1];
            int[] pos   = PED_POSITIONS[imgIdx][slotIdx];

            ImageView iv = new ImageView(duckImages[imgIdx]);
            iv.setLayoutX(pos[0]);
            iv.setLayoutY(pos[1]);
            if (pos[2] == 1) iv.setScaleX(-1);
            animalPane.getChildren().add(iv);
        }

        for (int i = 0; i < lizards; i++) {
            int imgIdx  = allLizardSlots.get(i)[0];
            int slotIdx = allLizardSlots.get(i)[1];
            int[] pos   = HERE_POSITIONS[imgIdx][slotIdx];

            ImageView iv = new ImageView(lizardImages[imgIdx]);
            iv.setLayoutX(pos[0]);
            iv.setLayoutY(pos[1]);
            if (pos[2] == 1) iv.setScaleX(-1);
            animalPane.getChildren().add(iv);
        }
    }

    private void startTimerBar() {
        if (countdownTimer != null) countdownTimer.stop();
        if (barAnim != null) barAnim.stop();

        timerBarFill.setWidth(BAR_WIDTH);
        timerBarFill.setFill(Color.web("#4fc3f7"));
        timerLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #4fc3f7;");

        barAnim = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(timerBarFill.widthProperty(), BAR_WIDTH)),
                new KeyFrame(Duration.seconds(getSecondsPerRound()), new KeyValue(timerBarFill.widthProperty(), 0))
        );
        barAnim.play();

        int[] timeLeft = {getSecondsPerRound()};
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
        countdownTimer.setCycleCount(getSecondsPerRound());
        countdownTimer.play();
    }

    private void updateTimerLabel(int t) {
        timerLabel.setText("⏱  " + t + " วินาที");
    }

    private void timeUp() {
        if (countdownTimer != null) countdownTimer.stop();
        if (barAnim != null) barAnim.stop();

        String input = answerField.getText().trim();
        answerField.setDisable(true);
        submitButton.setDisable(true);
        timerBarFill.setWidth(0);

        int answer;
        try { answer = Integer.parseInt(input); }
        catch (NumberFormatException e) { answer = -1; }

        if (!input.isEmpty() && answer == currentDuckCount) {
            totalScore += POINTS_PER_ROUND;
            resultLabel.setText("✅  ถูกต้อง! (ส่งทันเวลาพอดี) +" + POINTS_PER_ROUND + " คะแนน  |  เป็ด " + currentDuckCount + " ตัว");
            resultLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #66bb6a;");
        } else {
            resultLabel.setText("⏰  หมดเวลา! เป็ดมีทั้งหมด " + currentDuckCount + " ตัว");
            resultLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ef5350;");
        }

        scoreLabel.setText("คะแนน: " + totalScore + " / " + (TOTAL_ROUNDS * POINTS_PER_ROUND));
        proceedAfterAnswer();
    }

    private void handleAnswer() {
        String input = answerField.getText().trim();
        int answer;
        try {
            answer = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            resultLabel.setText("⚠ กรุณากรอกตัวเลข");
            return;
        }

        if (countdownTimer != null) countdownTimer.stop();
        if (barAnim != null) barAnim.stop();

        answerField.setDisable(true);
        submitButton.setDisable(true);
        timerBarFill.setWidth(0);

        if (answer == currentDuckCount) {
            totalScore += POINTS_PER_ROUND;
            resultLabel.setText("✅  ถูกต้อง! +" + POINTS_PER_ROUND + " คะแนน  |  เป็ด " + currentDuckCount + " ตัว");
            resultLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #66bb6a;");
        } else {
            resultLabel.setText("❌  ผิด! เป็ดมีทั้งหมด " + currentDuckCount + " ตัว");
            resultLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ef5350;");
        }

        scoreLabel.setText("คะแนน: " + totalScore + " / " + (TOTAL_ROUNDS * POINTS_PER_ROUND));
        proceedAfterAnswer();
    }

    private void proceedAfterAnswer() {
        if (currentRound >= TOTAL_ROUNDS) {
            timerLabel.setText("");
            submitButton.setText("จบเกม 🎉");
            submitButton.setDisable(false);
            submitButton.setOnAction(e -> onFinish.run());
        } else {
            boolean isCorrect = resultLabel.getText().startsWith("✅");
            if (isCorrect) showRightScreen();
            else showWrongScreen();
        }
    }

    // --- Right / Wrong Screens ---
    private void showRightScreen() {
        contentArea.getChildren().clear();

        ImageView bg       = loadFullImg("/images/exam/math/right/suan_right.jpg");
        ImageView rightImg = loadFullImg("/images/exam/math/right/right_right.png");
        ImageView bigPed   = loadFullImg("/images/exam/math/right/big_ped_right.png");
        ImageView btnImg   = loadFullImg("/images/exam/math/right/button_right.png");

        applyButtonEffect(btnImg, () -> loadRound()); // เอา countdown ออก

        Label answerLabel = new Label("ในสวนมีเป็ดทั้งหมด " + currentDuckCount + " ตัว");
        answerLabel.setStyle("""
            -fx-font-size: 39px;
            -fx-font-weight: bold;
            -fx-text-fill: black;
            """);
        StackPane.setAlignment(answerLabel, Pos.TOP_LEFT);
        answerLabel.setTranslateX(1176);
        answerLabel.setTranslateY(488);

        // answerLabel อยู่บน btnImg
        StackPane layered = new StackPane(bg, rightImg, bigPed, btnImg, answerLabel);
        layered.setStyle("-fx-background-color: #0a0a0a;");
        contentArea.getChildren().add(layered);
    }

    private void showWrongScreen() {
        contentArea.getChildren().clear();

        ImageView bg        = loadFullImg("/images/exam/math/wrong/suan_wrong.jpg");
        ImageView explosion = loadFullImg("/images/exam/math/wrong/explosion_wrong.png");
        ImageView wrongImg  = loadFullImg("/images/exam/math/wrong/wrong_wrong.png");
        ImageView bigPed    = loadFullImg("/images/exam/math/wrong/big_ped_wrong.png");
        ImageView btnImg    = loadFullImg("/images/exam/math/wrong/button_wrong.png");

        applyButtonEffect(btnImg, () -> loadRound()); // เอา countdown ออก

        Label answerLabel = new Label("ในสวนมีเป็ดทั้งหมด " + currentDuckCount + " ตัว");
        answerLabel.setStyle("""
            -fx-font-size: 39px;
            -fx-font-weight: bold;
            -fx-text-fill: black;
            """);
        StackPane.setAlignment(answerLabel, Pos.TOP_LEFT);
        answerLabel.setTranslateX(1176);
        answerLabel.setTranslateY(488);

        // answerLabel อยู่บน btnImg
        StackPane layered = new StackPane(bg, explosion, wrongImg, bigPed, btnImg, answerLabel);
        layered.setStyle("-fx-background-color: #0a0a0a;");
        contentArea.getChildren().add(layered);
    }

    private void showReadyScreen() {
        contentArea.getChildren().clear();

        ImageView bg         = loadFullImg("/images/exam/math/start/bg_start.jpg");
        ImageView blockStart = loadFullImg("/images/exam/math/start/block_start_com.png");
        ImageView bigPedHere = loadFullImg("/images/exam/math/start/big_ped_here_start.png");
        ImageView btnImg     = loadFullImg("/images/exam/math/start/button_start.png");

        Label timeLabel = new Label("โดยคุณมีเวลา " + getSecondsPerRound() + " วินาที");
        timeLabel.setStyle("""
            -fx-font-size: 49px;
            -fx-font-weight: bold;
            -fx-text-fill: black;
            """);
        StackPane.setAlignment(timeLabel, Pos.TOP_CENTER);
        timeLabel.setTranslateY(638);

        applyButtonEffect(btnImg, () -> loadRound()); // เอา countdown ออก

        // timeLabel อยู่บน btnImg
        StackPane layered = new StackPane(bg, blockStart, bigPedHere, btnImg, timeLabel);
        layered.setStyle("-fx-background-color: #0a0a0a;");
        contentArea.getChildren().add(layered);
    }

    // --- Helpers ---
    private void applyButtonEffect(ImageView btnImg, Runnable onClick) {
        btnImg.setOnMouseEntered(e -> { btnImg.setScaleX(0.97); btnImg.setScaleY(0.97); btnImg.setOpacity(0.85); });
        btnImg.setOnMouseExited(e  -> { btnImg.setScaleX(1.0);  btnImg.setScaleY(1.0);  btnImg.setOpacity(1.0);  });
        btnImg.setOnMousePressed(e -> { btnImg.setScaleX(0.94); btnImg.setScaleY(0.94); });
        btnImg.setOnMouseReleased(e -> { btnImg.setScaleX(1.0); btnImg.setScaleY(1.0); onClick.run(); });
    }

    private ImageView loadFullImg(String path) {
        ImageView iv = new ImageView(new Image(getClass().getResourceAsStream(path)));
        iv.setFitWidth(1920);
        iv.setFitHeight(1080);
        iv.setPreserveRatio(true);
        return iv;
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