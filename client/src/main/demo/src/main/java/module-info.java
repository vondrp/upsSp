module vondrovic.ups.sp.client.demo {
    requires javafx.controls;
    requires javafx.fxml;


    opens vondrovic.ups.sp.client.demo to javafx.fxml;
    exports vondrovic.ups.sp.client.demo;
}