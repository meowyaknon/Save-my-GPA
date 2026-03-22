package com.savemygpa.exam;

import com.savemygpa.player.Player;
import com.savemygpa.player.StatTier;
import com.savemygpa.player.StatType;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.Random;

public class CountingMiniGame {

    private static final int TOTAL_ROUNDS = 5;
    private static final int POINTS_PER_ROUND = 10;

    private final Player player;
    private final Random random = new Random();
    private final Runnable onFinish;

    private int totalScore = 0;
    private int currentRound = 0;
    private int currentDuckCount = 0;

    private final VBox root = new VBox(20);
    private final Label roundLabel = new Label();
    private final Label animalDisplay = new Label();
    private final Label resultLabel = new Label();
    private final Label scoreLabel = new Label();
    private final TextField answerField = new TextField();
    private final Button submitButton = new Button("ยืนยัน");

    public CountingMiniGame(Player player, Runnable onFinish) {
        this.player = player;
        this.onFinish = onFinish;
    }

    public VBox getView() {
        root.setAlignment(Pos.CENTER);

        submitButton.setOnAction(e -> handleAnswer());

        root.getChildren().addAll(
                new Label("=== เกมนับเป็ด ==="),
                new Label("นับเฉพาะเป็ด 🦆 อย่านับตัวเงินตัวทอง 🦎"),
                roundLabel,
                animalDisplay,
                answerField,
                submitButton,
                resultLabel,
                scoreLabel
        );

        loadRound();
        return root;
    }

    private void loadRound() {
        currentRound++;
        resultLabel.setText("");
        answerField.clear();
        answerField.setDisable(false);
        submitButton.setDisable(false);

        int[] counts = generateCounts();
        currentDuckCount = counts[0];
        int lizardCount = counts[1];

        roundLabel.setText("รอบที่ " + currentRound + " / " + TOTAL_ROUNDS);
        animalDisplay.setText(buildAnimalString(currentDuckCount, lizardCount));
        scoreLabel.setText("คะแนน: " + totalScore + " / " + (TOTAL_ROUNDS * POINTS_PER_ROUND));
    }

    private void handleAnswer() {
        String input = answerField.getText().trim();
        int answer;

        try {
            answer = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            resultLabel.setText("กรุณากรอกตัวเลข");
            return;
        }

        answerField.setDisable(true);
        submitButton.setDisable(true);

        if (answer == currentDuckCount) {
            totalScore += POINTS_PER_ROUND;
            resultLabel.setText("✅ ถูกต้อง! +" + POINTS_PER_ROUND + " คะแนน");
        } else {
            resultLabel.setText("❌ ผิด! เป็ดมีทั้งหมด " + currentDuckCount + " ตัว");
        }

        scoreLabel.setText("คะแนน: " + totalScore + " / " + (TOTAL_ROUNDS * POINTS_PER_ROUND));

        if (currentRound >= TOTAL_ROUNDS) {
            submitButton.setText("จบเกม");
            submitButton.setDisable(false);
            submitButton.setOnAction(e -> onFinish.run());
        } else {
            Button nextButton = new Button("รอบถัดไป →");
            nextButton.setOnAction(e -> {
                root.getChildren().remove(nextButton);
                loadRound();
            });
            root.getChildren().add(nextButton);
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

    private String buildAnimalString(int ducks, int lizards) {
        String[] animals = new String[ducks + lizards];
        for (int i = 0; i < ducks; i++) animals[i] = "🦆";
        for (int i = 0; i < lizards; i++) animals[ducks + i] = "🦎";

        for (int i = animals.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            String tmp = animals[i]; animals[i] = animals[j]; animals[j] = tmp;
        }

        return String.join(" ", animals);
    }

    public int getTotalScore() { return totalScore; }
}