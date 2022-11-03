package vondrovic.ups.sp.client.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import vondrovic.ups.sp.client.App;
import vondrovic.ups.sp.client.SceneEnum;
import vondrovic.ups.sp.client.model.game.GameModel;

/**
 * Controller for result view
 */
public class ResultController extends AbstractController {

    @FXML
    Label winnerLabel;

    /**
     * Initialization method
     */
    @Override
    public void initialize() {
        GameModel gameModel = App.INSTANCE.gameModel;
        winnerLabel.setText(gameModel.getWinner());
    }

    /**
     * Method that handle continue button
     */
    @FXML
    public void handleContinue() {
        Platform.runLater(() -> {
            App.INSTANCE.setScene(SceneEnum.LOBBY);
            App.INSTANCE.gameModel = null;
        });
    }
}
