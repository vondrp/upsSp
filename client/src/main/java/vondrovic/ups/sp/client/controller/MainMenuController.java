package vondrovic.ups.sp.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import vondrovic.ups.sp.client.App;
import vondrovic.ups.sp.client.SceneEnum;

public class MainMenuController extends AbstractController{

    @FXML
    BorderPane borderPane;

    @FXML
    Button gameBtn;

    @FXML
    Button connectBtn;

    @FXML
    Button roomBtn;

    @FXML
    Button lobbyBtn;

    @FXML
    Button closeBtn;

    @Override
    public void initialize() {

    }

    /**
     *
     */
    public void setGameScene()
    {
        App.INSTANCE.setScene(SceneEnum.GAME);
    }

    public void setConnectionScene()
    {
        App.INSTANCE.setScene(SceneEnum.CONNECT);
    }

    public void setRoomScene()
    {
        App.INSTANCE.setScene(SceneEnum.ROOM);
    }

    public void setLobbyScene()
    {
        App.INSTANCE.setScene(SceneEnum.LOBBY);
    }
}
