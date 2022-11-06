package vondrovic.ups.sp.client.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import vondrovic.ups.sp.client.AlertFactory;
import vondrovic.ups.sp.client.App;
import vondrovic.ups.sp.client.model.game.Player;

import java.io.IOException;

public class ConnectionController extends AbstractController{


    private final int MAX_PORT_NUMBER = 65535;
    @FXML
    TextField addressInput;

    @FXML
    TextField portInput;

    @FXML
    TextField usernameInput;

    @FXML
    Button connectButton;

    @FXML
    VBox vbox;

    @FXML
    StackPane stackPane;

    @Override
    public void initialize() {
        if(App.INSTANCE.lastEnteredHostname != null) {
            this.addressInput.setText(App.INSTANCE.lastEnteredHostname);
        }
        if(App.INSTANCE.lastEnteredPort != null) {
            this.portInput.setText(App.INSTANCE.lastEnteredPort);
        }
        if(App.INSTANCE.lastEnteredUsername != null) {
            this.usernameInput.setText(App.INSTANCE.lastEnteredUsername);
        }
    }

    /**
     * Method to handle connect button. Connects to the server.
     * @throws IOException
     */
    @FXML
    public void connect() throws IOException {

        if(App.INSTANCE.getConnectionModel() != null && App.INSTANCE.getConnectionModel().isConnected()) {
            return;
        }
        String address = "localhost";
        int port = 9123;

        if(addressInput.getText().length() > 0) {
            address = addressInput.getText();
        }

        if(portInput.getText().length() != 0) {
            try {
                port = Integer.parseInt(portInput.getText());
                if(port < 1 || port > MAX_PORT_NUMBER) {
                    AlertFactory.sendWarningMessage("Invalid Format", "Port have to be in range from 1 to "+ MAX_PORT_NUMBER);
                    return;
                }
            } catch (NumberFormatException e) {
                AlertFactory.sendWarningMessage("Invalid Format", "Port have to be a number");
                return;
            }
        }

        if(usernameInput.getText().length() == 0) {
            AlertFactory.sendWarningMessage("Invalid Format", "Username is required");
            return;
        }

        App.INSTANCE.setPlayer(new Player(usernameInput.getText()));

        VBox box = new VBox(new ProgressIndicator());
        box.setAlignment(Pos.CENTER);
        vbox.setDisable(true);
        stackPane.getChildren().add(box);

        if(App.INSTANCE.getConnectionModel() != null) {
            try {
                App.INSTANCE.getConnectionModel().close();
                App.INSTANCE.setConnectionModel(null);
            } catch (IOException e) {
                stackPane.getChildren().remove(box);
                vbox.setDisable(false);
                AlertFactory.sendErrorMessage("An Error Occurred", "An error occurred while terminating the previous connection");
                return;
            }
        }

        if(addressInput.getText().length() > 0) {
            App.INSTANCE.lastEnteredHostname = addressInput.getText();
        }
        if(portInput.getText().length() > 0) {
            App.INSTANCE.lastEnteredPort = portInput.getText();
        }
        if(usernameInput.getText().length() > 0) {
            App.INSTANCE.lastEnteredUsername = usernameInput.getText();
        }

        try {
            App.INSTANCE.connect(address, port);
        } catch (IOException e) {
            stackPane.getChildren().remove(box);
            vbox.setDisable(false);
            AlertFactory.sendErrorMessage("Connection error", "The connection was not established");
            return;
        }

    }
}
