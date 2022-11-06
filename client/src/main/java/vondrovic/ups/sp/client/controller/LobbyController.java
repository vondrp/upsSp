package vondrovic.ups.sp.client.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import vondrovic.ups.sp.client.AlertFactory;
import vondrovic.ups.sp.client.App;
import vondrovic.ups.sp.client.model.connection.Room;
import vondrovic.ups.sp.client.model.connection.Stats;

import java.util.ArrayList;

public class LobbyController extends AbstractController
{
    @FXML
    ListView<Room> roomList;

    @FXML
    Label userNameLabel;

    @FXML
    Button joinButton;

    /**
     * Method for first initialization
     */
    @FXML
    @Override
    public void initialize()
    {
        userNameLabel.setText(App.INSTANCE.getPlayer().getName());

        joinButton.setDisable(true);

        roomList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Room>() {
            public void changed(ObservableValue<? extends Room> observable,
                                Room oldValue, Room newValue) {
                joinButton.setDisable(newValue == null);
            }
        });

        handleRefresh();
    }

    /**
     * Handle game request
     * by sending request to the server
     */
    public void handleRefresh()
    {
        App.INSTANCE.getStage().getScene().setCursor(Cursor.WAIT);
        App.sendMessage("room_list_req");
    }

    /**
     * Handle logout request
     * Send request to the server
     */
    public void handleLogOut()
    {
        if(AlertFactory.sendConfirmation("Log Out", "Are you sure you want to log out?")) {
            App.sendMessage("logout_req");
        }
    }

    public void handleRoomCreate()
    {
        App.sendMessage("room_create_req");
    }

    @FXML
    public void handleJoin()
    {
        Room room = roomList.getSelectionModel().getSelectedItem();

        App.sendMessage("room_join_req;" + room.getId());
    }

    @FXML
    public void handleStats()
    {
        AlertFactory.sendMessage(Alert.AlertType.INFORMATION, "Connection statistics",
                "Recieved messages: " + Stats.INSTANCE.receivedMessages +
                        "\nRecieved bytes: " + Stats.INSTANCE.receivedBytes +
                        "\nSent messages: " + Stats.INSTANCE.sentMessages +
                        "\nSent bytes: " + Stats.INSTANCE.sentBytes);
    }

    /**
     * Fills room list from argument list
     * @param rooms
     */
    public void loadList(ArrayList<Room> rooms) {
        roomList.getItems().clear();

        for(Room r : rooms) {
            roomList.getItems().add(r);
        }
    }
}
