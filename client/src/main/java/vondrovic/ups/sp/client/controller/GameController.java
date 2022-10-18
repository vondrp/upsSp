package vondrovic.ups.sp.client.controller;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import vondrovic.ups.sp.client.AlertFactory;
import vondrovic.ups.sp.client.model.game.GameBoard;

/**
 * Controller of the Game fxml
 */
public class GameController extends AbstractController {

    @FXML
    Label userNameLabel;

    @FXML
    Canvas leftBoardCanvas;

    @FXML
    Canvas rightBoardCanvas;

    @FXML
    ToolBar toolBar;

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

    /**
     * Left
     */
    private GameBoard leftBoard;

    private GameBoard rightBoard;
   // GameModel gameModel = App.INSTANCE.getGameModel();

    @Override
    public void initialize() {
        this.leftBoard = new GameBoard(this.leftBoardCanvas, false);
        this.rightBoard = new GameBoard(this.rightBoardCanvas, true);

        // left side
        this.leftBoard.getBoardCanvas().setHeight(anchorPane.getHeight()/2);

        GraphicsContext gcLeft = this.leftBoard.getBoardCanvas().getGraphicsContext2D();

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
            //App.sendMessage("room_leave_req");
            System.out.println("leave");
        }
    }

    /**
     * Method to handle click on canvas.
     * Transform coordinations
     * @param event
     */
    @FXML
    public void handleLeftCanvasClick(MouseEvent event) {
        this.leftBoard.handleCanvasClick(event);
    }

    /**
     * Method to handle click on canvas.
     * Transform coordinations
     * @param event
     */
    @FXML
    public void handleRightCanvasClick(MouseEvent event) {
        this.rightBoard.handleCanvasClick(event);
    }

}
