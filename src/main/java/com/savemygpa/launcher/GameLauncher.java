package com.savemygpa.launcher;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import com.savemygpa.player.*;
import com.savemygpa.core.*;
import com.savemygpa.activity.*;

public class GameLauncher extends Application {

    private Stage stage;

    private Player player;
    private TimeSystem timeSystem;
    private boolean hasSavedGame = false;

    private Label energyLabel = new Label();
    private Label moodLabel = new Label();
    private Label intLabel = new Label();
    private Label timeLabel = new Label();

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        showAgreement();

        stage.setTitle("Save My GPA");
        stage.show();
    }

    private void startNewGame() {
        player = new Player(5,0,50);
        timeSystem = new TimeSystem();

        hasSavedGame = true;

        showGameplay();
    }

    private void updateStats() {

        energyLabel.setText("Energy: " + player.getStat(StatType.ENERGY));
        moodLabel.setText("Mood: " + player.getStat(StatType.MOOD));
        intLabel.setText("Intelligence: " + player.getStat(StatType.INTELLIGENCE));

        timeLabel.setText(
                "Day " + timeSystem.getCurrentDay() +
                        " | Hour " + timeSystem.getCurrentHour()
        );
    }

    private VBox statsPanel() {

        VBox box = new VBox(10,
                energyLabel,
                moodLabel,
                intLabel,
                timeLabel
        );

        box.setPrefWidth(150);
        updateStats();

        return box;
    }

    private void perform(Activity activity) {

        RequirementReason reason = activity.canPerform(player, timeSystem);

        if (reason != null) {
            showPopup(activity.getFailMessage(reason));
            return;
        }

        activity.performActivity(player,timeSystem);

        if(timeSystem.isDayOver()){
            forceGoHome();
            return;
        }

        updateStats();
    }

    private void forceGoHome() {
        Activity goHome = new GoHomeActivity();

        goHome.performActivity(player, timeSystem);

        showPopup("You are too tired... heading home.");

        showGameplay();

        updateStats();
    }

    private void showPopup(String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAgreement(){

        Label text = new Label("Agreement:\nPlay responsibly.");

        Button accept = new Button("Accept");
        Button refuse = new Button("Refuse");

        accept.setOnAction(e->showMainMenu());
        refuse.setOnAction(e->stage.close());

        VBox root = new VBox(20,text,accept,refuse);
        root.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(root,600,400));
    }

    private void showMainMenu(){

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);

        if (hasSavedGame) {
            Button cont = new Button("Continue");
            cont.setOnAction(e -> showGameplay());
            root.getChildren().add(cont);
        }

        Button start = new Button("Start New Game");
        Button how = new Button("How To Play");
        Button credits = new Button("Credits");
        Button quit = new Button("Quit");

        start.setOnAction(e->startNewGame());
        how.setOnAction(e->showHowToPlay());
        credits.setOnAction(e->showCredits());
        quit.setOnAction(e->stage.close());

        root.getChildren().addAll(start, how, credits, quit);

        stage.setScene(new Scene(root,600,400));
    }

    private void showHowToPlay(){

        Label text = new Label(
                "Increase intelligence before exam days.\n" +
                        "Manage your mood and energy."
        );

        Button back = new Button("Back");
        back.setOnAction(e->showMainMenu());

        VBox root = new VBox(20,text,back);
        root.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(root,600,400));
    }

    private void showCredits(){

        Label text = new Label("Game created by SaveMyGPA Team");

        Button back = new Button("Back");
        back.setOnAction(e->showMainMenu());

        VBox root = new VBox(20,text,back);
        root.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(root,600,400));
    }

    private void showGameplay(){

        Button busStop = new Button("Bus Stop");
        Button canteen = new Button("Canteen");
        Button itBuilding = new Button("IT Building");
        Button back =  new Button("Back");

        busStop.setOnAction(e->showBusStop());
        canteen.setOnAction(e->perform(new EatActivity()));
        itBuilding.setOnAction(e->showITBuilding());
        back.setOnAction(e->showMainMenu());

        VBox actions = new VBox(20,busStop,canteen,itBuilding,back);
        actions.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();

        root.setCenter(actions);
        root.setRight(statsPanel());

        stage.setScene(new Scene(root,800,500));

        updateStats();
    }

    private void showBusStop(){

        Button kllc = new Button("Go KLLC");
        Button home = new Button("Go Home");
        Button cancel = new Button("Cancel");

        kllc.setOnAction(e->{
            perform(new KLLCActivity());
            showGameplay();
        });

        home.setOnAction(e->{
            perform(new GoHomeActivity());
            showGameplay();
        });

        cancel.setOnAction(e->showGameplay());

        VBox root = new VBox(20,kllc,home,cancel);
        root.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(root,600,400));
    }

    private void showITBuilding(){

        Button classroom = new Button("Classroom");
        Button auditorium = new Button("Auditorium");
        Button cowork = new Button("Coworking Space");
        Button back = new Button("Back");

        classroom.setOnAction(e->{
            perform(new ClassroomActivity());
            showITBuilding();
        });

        auditorium.setOnAction(e->{
            perform(new AuditoriumActivity());
            showITBuilding();
        });

        cowork.setOnAction(e -> showCoworkingSpace());

        back.setOnAction(e->showGameplay());

        VBox root = new VBox(20,classroom,auditorium,cowork,back);
        root.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(root,600,400));
    }

    private void showCoworkingSpace(){

        Button relax = new Button("Relax");
        Button study = new Button("Study");
        Button cancel = new Button("Cancel");

        relax.setOnAction(e->{
            perform(new CoworkingRelaxActivity());
            showITBuilding();
        });

        study.setOnAction(e->{
            perform(new CoworkingStudyActivity());
            showITBuilding();
        });

        cancel.setOnAction(e->showITBuilding());

        VBox root = new VBox(20,relax,study,cancel);
        root.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(root,600,400));
    }

    public static void main(String[] args) {
        launch(args);
    }
}