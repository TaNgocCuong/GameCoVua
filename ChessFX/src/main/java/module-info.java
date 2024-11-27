module org.example.chessfx {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires javafx.media;
    requires static lombok;


    opens org.example.chessfx to javafx.fxml;
    exports org.example.chessfx;

}