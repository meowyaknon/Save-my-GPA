package com.savemygpa.launcher;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.Stage;

import com.savemygpa.player.*;
import com.savemygpa.player.effect.buff.SeniorNoteBuff;
import com.savemygpa.core.*;
import com.savemygpa.activity.*;
import com.savemygpa.event.*;

public class GameLauncher extends Application {

    // ── Core objects ─────────────────────────────────────────────────────────
    private Stage stage;
    private Player player;
    private TimeSystem timeSystem;
    private EventManager eventManager;

    // ── Game state ────────────────────────────────────────────────────────────
    private boolean hasSavedGame  = false;
    private boolean agreedToTerms = false;

    // Exam scores captured at end of each exam day
    private int progExam1Score = 0;  // end of day 6
    private int mathExam1Score = 0;  // end of day 7
    private int progExam2Score = 0;  // end of day 13
    private int mathExam2Score = 0;  // end of day 14

    private static final int TOTAL_DAYS = 14;

    // ── Stat display labels ──────────────────────────────────────────────────
    private final Label energyLabel   = new Label();
    private final Label moodLabel     = new Label();
    private final Label intLabel      = new Label();
    private final Label timeLabel     = new Label();
    private final Label timeLeftLabel = new Label();
    private final Label effectsLabel  = new Label();

    // ═════════════════════════════════════════════════════════════════════════
    // JavaFX entry
    // ═════════════════════════════════════════════════════════════════════════

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("Save My GPA");
        showAgreement();
        stage.show();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Game initialisation
    // ═════════════════════════════════════════════════════════════════════════

    private void startNewGame() {
        player       = new Player(6, 0, 60);
        timeSystem   = new TimeSystem();
        eventManager = new EventManager();
        EventRegistry.registerAll(eventManager);

        hasSavedGame   = true;
        progExam1Score = 0;
        mathExam1Score = 0;
        progExam2Score = 0;
        mathExam2Score = 0;

        showGameplay();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Day classification helpers
    // ═════════════════════════════════════════════════════════════════════════

    /** Day 6 or 13 = Programming Exam. */
    private boolean isProgExamDay() {
        int d = timeSystem.getCurrentDay();
        return d == 6 || d == 13;
    }

    /** Day 7 or 14 = Math Exam. */
    private boolean isMathExamDay() {
        int d = timeSystem.getCurrentDay();
        return d == 7 || d == 14;
    }

    private boolean isExamDay() {
        return isProgExamDay() || isMathExamDay();
    }

    private boolean isGameOver() {
        return timeSystem.getCurrentDay() > TOTAL_DAYS;
    }

    private String getDayTypeLabel() {
        if (isProgExamDay()) return "⚠️ วันสอบ Programming!";
        if (isMathExamDay()) return "⚠️ วันสอบ Math!";
        return "วันเรียนปกติ";
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Day-end logic
    // ═════════════════════════════════════════════════════════════════════════

    private void onDayEnd() {
        int day = timeSystem.getCurrentDay(); // the day that is ending NOW

        // Capture exam scores
        if (day == 6)  progExam1Score = player.getStat(StatType.INTELLIGENCE);
        if (day == 7)  mathExam1Score = player.getStat(StatType.INTELLIGENCE);
        if (day == 13) progExam2Score = player.getStat(StatType.INTELLIGENCE);
        if (day == 14) mathExam2Score = player.getStat(StatType.INTELLIGENCE);

        // Tick day-based buffs (SeniorNote)
        player.getEffect(SeniorNoteBuff.class).ifPresent(b -> b.tickDay(player));

        // Reset daily event counter
        eventManager.newDayReset();

        // Advance TimeSystem
        timeSystem.endDay();

        // INT reset at the start of day 8 (after first exam block days 6-7)
        if (timeSystem.getCurrentDay() == 8) {
            int current = player.getStat(StatType.INTELLIGENCE);
            player.changeStat(StatType.INTELLIGENCE, -current);
            showPopup(
                    "📚 รอบสอบแรกจบแล้ว!\n\n" +
                            "💻 Programming Score: " + progExam1Score + "\n" +
                            "📐 Math Score: "        + mathExam1Score + "\n\n" +
                            "Intelligence รีเซ็ตเป็น 0 สำหรับรอบสอบถัดไป\n" +
                            "Mood และ Energy ยังคงเดิม — สู้ต่อไป!"
            );
        }

        if (isGameOver()) {
            showEnding();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Activity execution
    // ═════════════════════════════════════════════════════════════════════════

    private boolean perform(Activity activity, Location location) {
        RequirementReason reason = activity.canPerform(player, timeSystem);
        if (reason != null) {
            showPopup(activity.getFailMessage(reason));
            return false;
        }

        activity.performActivity(player, timeSystem, eventManager);

        if (location != null) {
            eventManager.trigger(player, timeSystem, new EventContext(location, false));
        }

        // No auto-force: just update stats and let player click Go Home themselves
        updateStats();
        return true;
    }

    private void doGoHome() {
        player.changeStat(StatType.MOOD,   10 + timeSystem.getCurrentHour());
        player.changeStat(StatType.ENERGY,  3 + (player.getStat(StatType.MOOD) / 40) + timeSystem.getCurrentHour());

        onDayEnd();

        if (!isGameOver()) {
            showGameplay();
            updateStats();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Stat panel
    // ═════════════════════════════════════════════════════════════════════════

    private void updateStats() {
        energyLabel.setText  ("⚡ Energy: "  + player.getStat(StatType.ENERGY));
        moodLabel.setText    ("😊 Mood: "    + player.getStat(StatType.MOOD));
        intLabel.setText     ("🧠 INT: "     + player.getStat(StatType.INTELLIGENCE));
        timeLabel.setText    ("📅 Day "      + timeSystem.getCurrentDay()
                + " | 🕐 "     + timeSystem.getCurrentHour() + ":00");
        timeLeftLabel.setText("⏳ เหลือ: "  + timeSystem.getTimeLeft() + " ชม.");

        if (player.getActiveEffects().isEmpty()) {
            effectsLabel.setText("✨ ไม่มี effect");
        } else {
            StringBuilder sb = new StringBuilder("🎭 Effects:\n");
            player.getActiveEffects().forEach(e ->
                    sb.append("  • ").append(e.getName()).append("\n"));
            effectsLabel.setText(sb.toString());
        }
    }

    private VBox statsPanel() {
        effectsLabel.setWrapText(true);
        VBox box = new VBox(8,
                energyLabel, moodLabel, intLabel,
                new Separator(),
                timeLabel, timeLeftLabel,
                new Separator(),
                effectsLabel);
        box.setPrefWidth(185);
        box.setStyle("-fx-padding: 10; -fx-background-color: #f0f0f0;");
        updateStats();
        return box;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Screens
    // ═════════════════════════════════════════════════════════════════════════

    private void showAgreement() {
        Text title = new Text("📜 ข้อตกลงการเล่น");
        title.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        Label text = new Label(
                "ยินดีต้อนรับสู่ Save My GPA!\n\n" +
                        "คุณมี 14 วันในการเตรียมตัวสอบ:\n\n" +
                        "  วัน 1-5  →  เรียนปกติ\n" +
                        "  วัน 6    →  สอบ Programming รอบ 1\n" +
                        "  วัน 7    →  สอบ Math รอบ 1\n" +
                        "  วัน 8    →  INT รีเซ็ต, เรียนต่อ\n" +
                        "  วัน 8-12 →  เรียนปกติ\n" +
                        "  วัน 13   →  สอบ Programming รอบ 2\n" +
                        "  วัน 14   →  สอบ Math รอบ 2\n\n" +
                        "คุณยอมรับเงื่อนไขการเล่นหรือไม่?"
        );
        text.setWrapText(true);
        text.setTextAlignment(TextAlignment.CENTER);

        Button accept = new Button("✅ ยอมรับ");
        Button refuse = new Button("❌ ปฏิเสธ");
        accept.setPrefWidth(130);
        refuse.setPrefWidth(130);

        accept.setOnAction(e -> { agreedToTerms = true; showMainMenu(); });
        refuse.setOnAction(e -> showRefusalEnding());

        HBox buttons = new HBox(20, accept, refuse);
        buttons.setAlignment(Pos.CENTER);

        VBox root = new VBox(20, title, text, buttons);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 40;");
        stage.setScene(new Scene(root, 620, 460));
    }

    private void showMainMenu() {
        Text title = new Text("🎓 Save My GPA");
        title.setStyle("-fx-font-size: 24; -fx-font-weight: bold;");

        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 40;");
        root.getChildren().add(title);

        if (hasSavedGame) {
            Button cont = new Button("▶ Continue");
            cont.setPrefWidth(210);
            cont.setOnAction(e -> showGameplay());
            root.getChildren().add(cont);
        }

        Button start   = new Button("🆕 Start New Game");
        Button how     = new Button("📖 How To Play");
        Button credits = new Button("👥 Credits");
        Button quit    = new Button("🚪 Quit");
        for (Button b : new Button[]{start, how, credits, quit}) b.setPrefWidth(210);

        start.setOnAction(e -> startNewGame());
        how.setOnAction(e -> showHowToPlay());
        credits.setOnAction(e -> showCredits());
        quit.setOnAction(e -> stage.close());

        root.getChildren().addAll(start, how, credits, quit);
        stage.setScene(new Scene(root, 600, 450));
    }

    private void showHowToPlay() {
        Label text = new Label(
                "🕹️ วิธีเล่น Save My GPA\n\n" +
                        "📅 ตารางวัน:\n" +
                        "  วัน 1-5  → เรียนปกติ\n" +
                        "  วัน 6    → สอบ Programming รอบ 1\n" +
                        "  วัน 7    → สอบ Math รอบ 1\n" +
                        "  วัน 8    → INT รีเซ็ตเป็น 0 (Mood/Energy คงเดิม)\n" +
                        "  วัน 8-12 → เรียนปกติ\n" +
                        "  วัน 13   → สอบ Programming รอบ 2\n" +
                        "  วัน 14   → สอบ Math รอบ 2\n\n" +
                        "📊 Stats:\n" +
                        "  🧠 INT (max 100)   — ยิ่งสูง คะแนนสอบยิ่งดี\n" +
                        "  😊 Mood (max 100)  — ส่งผลต่อ INT ที่ได้จากการเรียน\n" +
                        "  ⚡ Energy (max 10) — ต้องใช้ทำกิจกรรม\n\n" +
                        "🎲 Event สุ่ม: สูงสุด 3 ครั้ง/วัน เมื่อเปลี่ยนสถานที่\n\n" +
                        "🏆 เกรด: INT เฉลี่ย ≥70 → A | ≥40 → C | <40 → F"
        );
        text.setWrapText(true);
        text.setStyle("-fx-font-size: 13;");

        Button back = new Button("← กลับ");
        back.setOnAction(e -> showMainMenu());

        VBox root = new VBox(20, text, back);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 30;");
        stage.setScene(new Scene(root, 650, 530));
    }

    private void showCredits() {
        Label text = new Label("🎮 Save My GPA\n\nCreated by the SaveMyGPA Team\n\nขอบคุณทุกคนที่เล่น!");
        text.setTextAlignment(TextAlignment.CENTER);
        Button back = new Button("← กลับ");
        back.setOnAction(e -> showMainMenu());
        VBox root = new VBox(20, text, back);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 40;");
        stage.setScene(new Scene(root, 500, 280));
    }

    // ── Gameplay ─────────────────────────────────────────────────────────────

    private void showGameplay() {
        if (isGameOver()) { showEnding(); return; }

        eventManager.trigger(player, timeSystem, new EventContext(Location.OUTSIDE, false));
        updateStats();

        int day = timeSystem.getCurrentDay();
        Label header = new Label("📅 Day " + day + " — " + getDayTypeLabel());
        header.setStyle("-fx-font-size: 15; -fx-font-weight: bold;");

        Button busStop    = new Button("🚌 Bus Stop (KLLC)");
        Button canteen    = new Button("🍽️ Canteen");
        Button itBuilding = new Button("🏫 IT Building");
        Button mainMenu   = new Button("⏸ Main Menu");
        for (Button b : new Button[]{busStop, canteen, itBuilding, mainMenu}) b.setPrefWidth(210);

        // When time is up, disable all activity buttons — player must click Go Home
        boolean outOfTime = timeSystem.isDayOver();
        busStop.setDisable(outOfTime);
        canteen.setDisable(outOfTime);
        itBuilding.setDisable(outOfTime);

        busStop.setOnAction(e -> showBusStop());
        canteen.setOnAction(e -> { perform(new EatActivity(), Location.CANTEEN); showGameplay(); });
        itBuilding.setOnAction(e -> showITBuilding());
        mainMenu.setOnAction(e -> showMainMenu());

        VBox actions = new VBox(12, header, new Separator(),
                busStop, canteen, itBuilding, mainMenu);
        actions.setAlignment(Pos.CENTER);
        actions.setStyle("-fx-padding: 20;");

        BorderPane root = new BorderPane();
        root.setCenter(actions);
        root.setRight(statsPanel());
        stage.setScene(new Scene(root, 850, 530));
        updateStats();
    }

    private void showBusStop() {
        eventManager.trigger(player, timeSystem, new EventContext(Location.BUS_STOP, false));
        updateStats();

        Label header = new Label("🚌 ป้ายรถเมล์");
        header.setStyle("-fx-font-size: 15; -fx-font-weight: bold;");

        Button kllc   = new Button("📚 ไป KLLC (เรียน)");
        Button goHome = new Button("🏠 Go Home");
        Button cancel = new Button("← กลับ");
        kllc.setPrefWidth(210);
        goHome.setPrefWidth(210);
        cancel.setPrefWidth(210);

        kllc.setOnAction(e -> { perform(new KLLCActivity(), Location.BUS_STOP); showGameplay(); });
        goHome.setOnAction(e -> doGoHome());
        cancel.setOnAction(e -> showGameplay());

        VBox center = new VBox(15, header, kllc, goHome, cancel);
        center.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();
        root.setCenter(center);
        root.setRight(statsPanel());
        stage.setScene(new Scene(root, 850, 530));
    }

    private void showITBuilding() {
        eventManager.trigger(player, timeSystem, new EventContext(Location.IT_BUILDING, false));
        updateStats();

        Label header = new Label("🏫 IT Building — " + getDayTypeLabel());
        header.setStyle("-fx-font-size: 15; -fx-font-weight: bold;");

        VBox center = new VBox(12);
        center.setAlignment(Pos.CENTER);
        center.setStyle("-fx-padding: 20;");
        center.getChildren().addAll(header, new Separator());

        if (isProgExamDay() || isMathExamDay()) {
            // Exam day: Classroom → Exam button; Auditorium and Coworking still available
            String examLabel = isProgExamDay() ? "💻 เข้าห้องสอบ Programming" : "📐 เข้าห้องสอบ Math";
            String examSubject = isProgExamDay() ? "Programming" : "Math";

            Button examBtn    = new Button(examLabel);
            Button auditorium = new Button("🎭 Auditorium");
            Button cowork     = new Button("💻 Coworking Space");
            for (Button b : new Button[]{examBtn, auditorium, cowork}) b.setPrefWidth(210);

            examBtn.setOnAction(e -> doExam(examSubject));
            auditorium.setOnAction(e -> { perform(new AuditoriumActivity(), Location.AUDITORIUM); showITBuilding(); });
            cowork.setOnAction(e -> showCoworkingSpace());

            center.getChildren().addAll(examBtn, auditorium, cowork);

        } else {
            Button classroom  = new Button("📖 Classroom");
            Button auditorium = new Button("🎭 Auditorium");
            Button cowork     = new Button("💻 Coworking Space");
            for (Button b : new Button[]{classroom, auditorium, cowork}) b.setPrefWidth(210);

            classroom.setOnAction(e -> { perform(new ClassroomActivity(), Location.CLASSROOM); showITBuilding(); });
            auditorium.setOnAction(e -> { perform(new AuditoriumActivity(), Location.AUDITORIUM); showITBuilding(); });
            cowork.setOnAction(e -> showCoworkingSpace());

            center.getChildren().addAll(classroom, auditorium, cowork);
        }

        Button back = new Button("← กลับ");
        back.setPrefWidth(210);
        back.setOnAction(e -> showGameplay());
        center.getChildren().addAll(new Separator(), back);

        BorderPane root = new BorderPane();
        root.setCenter(center);
        root.setRight(statsPanel());
        stage.setScene(new Scene(root, 850, 530));
    }

    private void showCoworkingSpace() {
        eventManager.trigger(player, timeSystem, new EventContext(Location.COWORKING, false));
        updateStats();

        Label header = new Label("💻 Coworking Space");
        header.setStyle("-fx-font-size: 15; -fx-font-weight: bold;");

        Button relax  = new Button("😴 Relax (+Energy, +Mood)");
        Button study  = new Button("📝 Study (+INT)");
        Button cancel = new Button("← กลับ");
        for (Button b : new Button[]{relax, study, cancel}) b.setPrefWidth(210);

        relax.setOnAction(e -> { perform(new CoworkingRelaxActivity(), Location.COWORKING); showITBuilding(); });
        study.setOnAction(e -> { perform(new CoworkingStudyActivity(), Location.COWORKING); showITBuilding(); });
        cancel.setOnAction(e -> showITBuilding());

        VBox center = new VBox(12, header, relax, study, cancel);
        center.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();
        root.setCenter(center);
        root.setRight(statsPanel());
        stage.setScene(new Scene(root, 850, 530));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Exam execution
    // ═════════════════════════════════════════════════════════════════════════

    private void doExam(String subject) {
        // ForgetID event may fire here on exam days
        eventManager.trigger(player, timeSystem, new EventContext(Location.CLASSROOM, true));
        updateStats();

        ExamActivity exam = new ExamActivity();
        RequirementReason reason = exam.canPerform(player, timeSystem);
        if (reason != null) {
            showPopup(exam.getFailMessage(reason));
            return;
        }

        perform(exam, Location.CLASSROOM);

        int day = timeSystem.getCurrentDay();
        String round = (day <= 7) ? "รอบ 1" : "รอบ 2";
        showPopup(
                "✅ สอบ " + subject + " " + round + " เสร็จแล้ว!\n\n" +
                        "🧠 Intelligence ปัจจุบัน: " + player.getStat(StatType.INTELLIGENCE) + "\n\n" +
                        "(คะแนนจะถูกบันทึกเมื่อคุณกลับบ้านสิ้นวันนี้)"
        );

        showITBuilding();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Endings
    // ═════════════════════════════════════════════════════════════════════════

    private void showEnding() {
        if (!agreedToTerms) { showRefusalEnding(); return; }

        int progAvg    = (progExam1Score + progExam2Score) / 2;
        int mathAvg    = (mathExam1Score + mathExam2Score) / 2;
        int overallAvg = (progAvg + mathAvg) / 2;

        String grade, emoji, message;
        if (overallAvg >= 70) {
            grade = "A"; emoji = "🏆";
            message = "คุณทำได้ยอดเยี่ยม!\nการบริหารเวลาและการเรียนของคุณดีมาก\nGPA Saved! 🎉";
        } else if (overallAvg >= 40) {
            grade = "C"; emoji = "😅";
            message = "คุณผ่านไปได้... แต่หวุดหวิด\nครั้งหน้าต้องพยายามมากกว่านี้!";
        } else {
            grade = "F"; emoji = "💀";
            message = "GPA ไม่รอด...\nลองใหม่อีกครั้ง — คุณทำได้!";
        }

        Text title = new Text(emoji + " ผลการเรียน " + emoji);
        title.setStyle("-fx-font-size: 22; -fx-font-weight: bold;");

        Label scores = new Label(
                "📊 สรุปผลการสอบ\n\n" +
                        "💻 Programming:\n" +
                        "   รอบ 1: " + progExam1Score + "  |  รอบ 2: " + progExam2Score + "  →  เฉลี่ย: " + progAvg + "\n\n" +
                        "📐 Math:\n" +
                        "   รอบ 1: " + mathExam1Score + "  |  รอบ 2: " + mathExam2Score + "  →  เฉลี่ย: " + mathAvg + "\n\n" +
                        "📈 คะแนนรวมเฉลี่ย: " + overallAvg + "  |  🎓 เกรด: " + grade + "\n\n" +
                        message
        );
        scores.setWrapText(true);
        scores.setTextAlignment(TextAlignment.CENTER);
        scores.setStyle("-fx-font-size: 13;");

        Button playAgain = new Button("🔄 เล่นใหม่");
        Button quit      = new Button("🚪 ออกจากเกม");
        playAgain.setPrefWidth(150);
        quit.setPrefWidth(150);

        playAgain.setOnAction(e -> startNewGame());
        quit.setOnAction(e -> stage.close());

        HBox buttons = new HBox(20, playAgain, quit);
        buttons.setAlignment(Pos.CENTER);

        VBox root = new VBox(20, title, scores, buttons);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 40;");
        stage.setScene(new Scene(root, 650, 520));
    }

    private void showRefusalEnding() {
        Text title = new Text("❌ จบเกม: ปฏิเสธข้อตกลง");
        title.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        Label text = new Label(
                "คุณปฏิเสธที่จะเล่นเกม\n\n" +
                        "บางครั้งการไม่ทำอะไรเลย\nก็เป็นทางเลือกหนึ่ง...\n\n" +
                        "แต่ GPA ก็ไม่รอดเหมือนกัน 😢"
        );
        text.setTextAlignment(TextAlignment.CENTER);
        text.setStyle("-fx-font-size: 14;");

        Button tryAgain = new Button("← ลองใหม่");
        tryAgain.setOnAction(e -> showAgreement());

        VBox root = new VBox(20, title, text, tryAgain);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 40;");
        stage.setScene(new Scene(root, 500, 320));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Utility
    // ═════════════════════════════════════════════════════════════════════════

    private void showPopup(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}