module ru.itis.semester_work2 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires com.google.gson;

    opens ru.itis.semester_work2 to javafx.fxml;
    opens ru.itis.semester_work2.protocol to com.google.gson;
    opens ru.itis.semester_work2.model to com.google.gson;
    opens ru.itis.semester_work2.ui to javafx.fxml;

    exports ru.itis.semester_work2;
    exports ru.itis.semester_work2.protocol;
    exports ru.itis.semester_work2.model;
    exports ru.itis.semester_work2.network;
    exports ru.itis.semester_work2.ui;
    exports ru.itis.semester_work2.game;
}