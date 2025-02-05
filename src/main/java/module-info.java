module org.example.memoryfx {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.almasb.fxgl.all;

    opens org.example.memoryfx to javafx.fxml;
    exports org.example.memoryfx;
}