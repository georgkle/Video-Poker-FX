module com.example.videopokerfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.media;


    opens com.example.videopokerfx to javafx.fxml;
    exports com.example.videopokerfx;
}