module com.meowyaknon.savemygpa {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    opens com.savemygpa.launcher to javafx.fxml;
    opens com.savemygpa.ui to javafx.fxml;

    exports com.savemygpa.launcher;
}