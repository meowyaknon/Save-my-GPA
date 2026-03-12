package com.savemygpa.launcher;

import javafx.stage.Modality;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;

public class GameLauncher extends Application {

    private Stage stage;

    @Override
    public void start(Stage stage) {

        this.stage = stage;
        stage.setTitle("Save My GPA");

        showAgreement();
        stage.show();
    }

    // ---------------- AGREEMENT ----------------

    private void showAgreement() {

        Label text = new Label("Do you accept the challenge?");

        Button accept = new Button("Accept");
        Button refuse = new Button("Refuse");

        accept.setOnAction(e -> showMainMenu());
        refuse.setOnAction(e -> stage.close());

        HBox buttons = new HBox(20, accept, refuse);
        buttons.setAlignment(Pos.CENTER);

        VBox root = new VBox(30, text, buttons);
        root.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(root, 600, 400));
    }

    // ---------------- MAIN MENU ----------------

    private void showMainMenu() {

        Label title = new Label("Main Menu");

        Button newGame = new Button("Start New Game");
        Button howTo = new Button("How To Play");
        Button credits = new Button("Credits");
        Button quit = new Button("Quit");

        newGame.setOnAction(e -> showMap());
        howTo.setOnAction(e -> showHowToPlay());
        credits.setOnAction(e -> showCredits());
        quit.setOnAction(e -> stage.close());

        VBox root = new VBox(15, title, newGame, howTo, credits, quit);
        root.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(root, 600, 400));
    }

    // ---------------- HOW TO PLAY SCREEN ----------------

    private void showHowToPlay() {

        Label title = new Label("How To Play");

        Label text = new Label(
                """
                Manage your time and stats.

                Study to increase Intelligence.
                Relax to restore Mood.
                Balance your Energy.

                Prepare before exam days!
                """
        );

        Button back = new Button("Back");

        back.setOnAction(e -> showMainMenu());

        VBox root = new VBox(20, title, text, back);
        root.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(root, 600, 400));
    }

    // ---------------- CREDITS SCREEN ----------------

    private void showCredits() {

        Label title = new Label("Credits");

        Label text = new Label(
                """
                Save My GPA

                Developer:
                Sirithep Bordikarn

                Built with JavaFX
                """
        );

        Button back = new Button("Back");

        back.setOnAction(e -> showMainMenu());

        VBox root = new VBox(20, title, text, back);
        root.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(root, 600, 400));
    }

    // ---------------- MAP ----------------

    private void showMap() {

        Button busStop = new Button("Bus Stop");
        Button itBuilding = new Button("IT Building");
        Button cafeteria = new Button("Cafeteria");

        busStop.setPrefWidth(150);
        itBuilding.setPrefWidth(150);
        cafeteria.setPrefWidth(150);

        busStop.setOnAction(e -> showBusStopPopup());
        itBuilding.setOnAction(e -> showITBuilding());
        cafeteria.setOnAction(e -> showActivityResult("Relaxing in Cafeteria"));

        HBox map = new HBox(30, busStop, itBuilding, cafeteria);
        map.setAlignment(Pos.CENTER);

        Scene scene = new Scene(map, 700, 400);

        // ESC key → return to main menu
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                showMainMenu();
            }
        });

        stage.setScene(scene);
    }

    // ---------------- BUS STOP ----------------

    private void showBusStopPopup() {

        Stage popup = new Stage();
        popup.initOwner(stage);
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Bus Stop");

        Button kllc = new Button("Go to KLLC (Study)");
        Button home = new Button("Go Home");
        Button cancel = new Button("Cancel");

        kllc.setOnAction(e -> {
            popup.close();
            showActivityResult("Studying at KLLC");
        });

        home.setOnAction(e -> {
            popup.close();
            showActivityResult("Going Home (End Day)");
        });

        cancel.setOnAction(e -> popup.close());

        VBox root = new VBox(15,
                new Label("Bus Stop"),
                kllc,
                home,
                cancel
        );

        root.setAlignment(Pos.CENTER);

        popup.setScene(new Scene(root, 300, 200));
        popup.show();
    }

    // ---------------- IT BUILDING ----------------

    private void showITBuilding() {

        Button classroom = new Button("Classroom (Study)");
        Button auditorium = new Button("Auditorium (Relax)");
        Button coworking = new Button("CoWorking Space");
        Button back = new Button("Back");

        classroom.setOnAction(e -> showActivityResult("Studying in Classroom"));
        auditorium.setOnAction(e -> showActivityResult("Relaxing in Auditorium"));
        coworking.setOnAction(e -> showCoworkingPopup());
        back.setOnAction(e -> showMap());

        VBox root = new VBox(20,
                new Label("IT Building"),
                classroom,
                auditorium,
                coworking,
                back
        );

        root.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(root, 600, 400));
    }

    // ---------------- COWORKING SPACE ----------------

    private void showCoworkingPopup() {

        Stage popup = new Stage();
        popup.initOwner(stage);
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("CoWorking Space");

        Button study = new Button("Study");
        Button relax = new Button("Relax");
        Button cancel = new Button("Cancel");

        study.setOnAction(e -> {
            popup.close();
            showActivityResult("Studying at CoWorking Space");
        });

        relax.setOnAction(e -> {
            popup.close();
            showActivityResult("Relaxing at CoWorking Space");
        });

        cancel.setOnAction(e -> popup.close());

        VBox root = new VBox(15,
                new Label("CoWorking Space"),
                study,
                relax,
                cancel
        );

        root.setAlignment(Pos.CENTER);

        popup.setScene(new Scene(root, 300, 200));
        popup.show();
    }

    // ---------------- ACTIVITY RESULT ----------------

    private void showActivityResult(String message) {

        Label result = new Label(message);

        Button back = new Button("Back to Map");

        back.setOnAction(e -> showMap());

        VBox root = new VBox(30, result, back);
        root.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(root, 600, 400));
    }

    public static void main(String[] args) {
        launch(args);
    }
}