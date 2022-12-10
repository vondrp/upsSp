package vondrovic.ups.sp.client.controller;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import vondrovic.ups.sp.client.AlertFactory;
import vondrovic.ups.sp.client.App;
import vondrovic.ups.sp.client.model.game.GameBoard;
import vondrovic.ups.sp.client.model.game.GameModel;
import vondrovic.ups.sp.client.model.game.GameStatus;

/**
 * Controller of the Game fxml
 */
public class GameController extends AbstractController {

    @FXML
    Label userNameLabel;

    @FXML
    Label opponentNameLabel;

    @FXML
    Button roomLeaveButton;

    @FXML
    Canvas leftBoardCanvas;

    @FXML
    Canvas rightBoardCanvas;

    @FXML
    ToolBar toolBar;

    //@FXML
    //HBox readyHBox;

    @FXML
    Button readyButton;

    @FXML
    AnchorPane anchorPane;

    @FXML
    AnchorPane sideBar;

    @FXML
    BorderPane borderPane;

    @FXML
    Text textLabel;

    @FXML
    TextArea protocol;

    private GameModel gameModel = App.INSTANCE.getGameModel();

    /**
     * Board located at left - used for player
     */
    private GameBoard leftBoard;

    /**
     * Board located at right - used for opponent
     */
    private GameBoard rightBoard;

    @FXML
    @Override
    public void initialize() {
        //App.INSTANCE.gameModel = new GameModel();
        //App.INSTANCE.gameModel.init();
        //this.gameModel = App.INSTANCE.getGameModel();

        this.userNameLabel.setText(App.INSTANCE.getPlayer().getName());

        this.opponentNameLabel.setText(App.INSTANCE.getOpponent().getName());
        this.leftBoard = new GameBoard(this.leftBoardCanvas, this.gameModel, false);
        this.rightBoard = new GameBoard(this.rightBoardCanvas, this.gameModel, true);

        // left side
        this.leftBoard.getBoardCanvas().setHeight(anchorPane.getHeight()/2);

        // right side
        this.rightBoard.getBoardCanvas().setWidth(anchorPane.getWidth()/2 - 5);
        this.rightBoard.getBoardCanvas().setHeight(anchorPane.getHeight()/2);
        GraphicsContext gcRight = this.rightBoard.getBoardCanvas().getGraphicsContext2D();

        borderPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            if(!oldVal.equals(newVal)) {
                this.leftBoard.getBoardCanvas().setWidth( (newVal.doubleValue() - sideBar.getWidth()-10)/2);
                this.rightBoard.getBoardCanvas().setWidth( (newVal.doubleValue() - sideBar.getWidth()-10)/2);
                this.rightBoard.getBoardCanvas().setLayoutX(this.leftBoard.getBoardCanvas().getLayoutX() + this.leftBoard.getBoardCanvas().getWidth() + 20);
                this.rightBoard.repaint();
                this.leftBoard.repaint();
            }
        });

        borderPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            if(!oldVal.equals(newVal)) {
                this.leftBoard.getBoardCanvas().setHeight(newVal.doubleValue() - toolBar.getPrefHeight());
                this.rightBoard.getBoardCanvas().setHeight(newVal.doubleValue() - toolBar.getPrefHeight());
                this.rightBoard.getBoardCanvas().setLayoutX(this.leftBoard.getBoardCanvas().getLayoutX() + this.leftBoard.getBoardCanvas().getWidth() + 20);
                this.rightBoard.repaint();
                this.leftBoard.repaint();
            }
        });

    }


    /**
     * Handle leaves button. Sends request to the server.
     */
    @FXML
    public void handleLeave() {
        if(AlertFactory.sendConfirmation("Leave The Game", "Are you sure you want to leave the game? The game will end.")) {
            App.sendMessage("room_leave_req");
        }
    }

    @FXML
    public void readyToPlay()
    {
        if (this.gameModel.getGameStatus() == GameStatus.PREPARING)
        {
            String myBoard = this.gameModel.getMyBoardStringForm();
            App.sendMessage("game_prepared;"+myBoard);
        }
    }

    /**
     * disable ready to play button
     */
    public void disablePrepareButton()
    {
        this.readyButton.setDisable(true);
    }

    /**
     * Method to handle click on canvas.
     * Transform coordinations
     * @param event
     */
    @FXML
    public void handleLeftCanvasClick(MouseEvent event) {
        if (gameModel.getGameStatus() == GameStatus.PREPARING)
        {
            this.leftBoard.handleCanvasClick(event);
        }
    }

    /**
     * Method to handle click on right canvas (opponent board)
     * @param event
     */
    @FXML
    public void handleRightCanvasClick(MouseEvent event) {
        if (gameModel.getGameStatus() == GameStatus.PLAYING)
        {
            this.rightBoard.handleCanvasClick(event);
        }
    }

    /**
     * Method to add message to the protocol pane
     * @param s string to append to protocol
     */
    public void protocolAdd(String s) {
        protocol.appendText(s + "\n");
    }

    /**
     * Repaint game board
     */
    public void repaint()
    {
        this.leftBoard.repaint();
        this.rightBoard.repaint();
    }

    /**
     * Reload opponent name text
     */
    public void reloadOpponentName()
    {
        this.opponentNameLabel.setText(App.INSTANCE.getOpponent().getName());
    }
}