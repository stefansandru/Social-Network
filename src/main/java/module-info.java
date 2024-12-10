module com.example.lab8_messages {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.jetbrains.annotations;
    requires java.desktop;


    opens com.example.lab8_messages to javafx.fxml;
    exports com.example.lab8_messages;
}