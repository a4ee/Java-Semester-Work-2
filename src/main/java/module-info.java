module ru.itis.semester_work2 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;

    opens ru.itis.semester_work2 to javafx.fxml;
    exports ru.itis.semester_work2;
}