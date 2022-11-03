package vondrovic.ups.sp.client.controller;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import vondrovic.ups.sp.client.App;

public class RoomController extends AbstractController{

    @FXML
    Label userNameLabel;

    @FXML
    @Override
    public void initialize() {
        userNameLabel.setText(App.INSTANCE.getPlayer().getName());
    }

    @FXML
    public void handleLeave()
    {
        App.sendMessage("room_leave_req");
        App.INSTANCE.getStage().getScene().setCursor(Cursor.WAIT);
    }
}
