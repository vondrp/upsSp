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
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import vondrovic.ups.sp.client.AlertFactory;
import vondrovic.ups.sp.client.model.Position;

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

   // Affine originalTransform;
    Affine originalLeftTransform;
    Affine originalRightTransform;
    private final double PAINT_WIDTH = 400.0;

    private final int BOARD_LENGTH = 11;
    private final int SQUARES_IN_ROW = 10;

    /**
     * min value of coordinates
     */
    private final int MIN = 1;

    /**
     * max value of coordinates
     */
    private final int MAX = 10;

    private final double SQUARE_SIZE = PAINT_WIDTH / BOARD_LENGTH;

   // GameModel gameModel = App.INSTANCE.getGameModel();

   // LinkedList<Point> path = new LinkedList<Point>();

    @Override
    public void initialize() {
        // left side
        leftBoardCanvas.setWidth(anchorPane.getWidth()/2 - 5);
        leftBoardCanvas.setHeight(anchorPane.getHeight()/2);

        GraphicsContext gcLeft = leftBoardCanvas.getGraphicsContext2D();

        // right side
        rightBoardCanvas.setWidth(anchorPane.getWidth()/2 - 5);
        rightBoardCanvas.setHeight(anchorPane.getHeight()/2);
        GraphicsContext gcRight = rightBoardCanvas.getGraphicsContext2D();

        borderPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            if(!oldVal.equals(newVal)) {
                leftBoardCanvas.setWidth( (newVal.doubleValue() - sideBar.getWidth()-10)/2);
                rightBoardCanvas.setWidth( (newVal.doubleValue() - sideBar.getWidth()-10)/2);
                rightBoardCanvas.setLayoutX(leftBoardCanvas.getLayoutX() + leftBoardCanvas.getWidth() + 20);
                repaint();
            }
        });

        borderPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            if(!oldVal.equals(newVal)) {
                leftBoardCanvas.setHeight(newVal.doubleValue() - toolBar.getPrefHeight());
                rightBoardCanvas.setHeight(newVal.doubleValue() - toolBar.getPrefHeight());
                rightBoardCanvas.setLayoutX(leftBoardCanvas.getLayoutX() + leftBoardCanvas.getWidth() + 20);
                repaint();
            }
        });

        //rightBoardCanvas.setLayoutX(leftBoardCanvas.getLayoutX()+leftBoardCanvas.getWidth()+10);

        //boardCanvas.setWidth(borderPane.getWidth() - sideBar.getWidth());
        //boardCanvas.setHeight(borderPane.getHeight() - toolBar.getPrefHeight());
        //userNameLabel.setText(App.INSTANCE.player.getName());

        //repaint();
    }

    /**
     * Method to repaint canvas
     */
    public void repaint() {
        GraphicsContext gcLeft = this.leftBoardCanvas.getGraphicsContext2D();
        GraphicsContext gcRight = this.rightBoardCanvas.getGraphicsContext2D();
        drawBoard(gcLeft, this.originalLeftTransform, this.leftBoardCanvas);
        drawBoard(gcRight, this.originalRightTransform, this.rightBoardCanvas);
    }

    public void drawBoard(GraphicsContext gc, Affine originalTransform, Canvas boardCanvas)
    {
        originalTransform = gc.getTransform();
        fillBackground(gc, originalTransform);

        double width = boardCanvas.getWidth();
        double height = boardCanvas.getHeight();

        double size = Math.min(width, height);
        double max = Math.max(width, height);

        double tx = 0, ty = 0;
        double scale = 1;

        if(width > height) {
            tx = (width - size) * 0.5;
        } else {
            ty = (height - size) * 0.5;
        }

        scale = size / PAINT_WIDTH;


        gc.translate(tx, ty);
        gc.scale(scale, scale);

        Affine defaultTransform = gc.getTransform();

        gc.setFill(Color.GRAY);

        char letter = 'A';
        for(int i = 0; i < BOARD_LENGTH; i++) {
            for(int j = 0; j < BOARD_LENGTH; j++) {
                if (j == 0 && i == 0) // top left corner
                {
                    // really nothing
                }
                else if (j == 0) // row with letters
                {
                    gc.fillText(letter+"",
                            i * SQUARE_SIZE + (SQUARE_SIZE -1) / 2,
                            j * SQUARE_SIZE + (SQUARE_SIZE -1) / 2);
                    letter++;
                }
                else if (i == 0) // column with numbers
                {
                    String string = (i + j)+"";
                    gc.fillText(string,
                            i * SQUARE_SIZE + (SQUARE_SIZE -1) / 2,
                            j * SQUARE_SIZE + (SQUARE_SIZE -1) / 2);
                }
                else
                {
                    gc.fillRect(i * SQUARE_SIZE, j * SQUARE_SIZE, SQUARE_SIZE -1, SQUARE_SIZE -1);
                }
            }
        }

        gc.setTransform(defaultTransform);
        gc.setTransform(originalTransform);
    }

    /**
     * Method to paint background on the canvas
     * @param gc graphic context
     */
    public void fillBackground(GraphicsContext gc, Affine originalTransform) {
        Affine transform = gc.getTransform();
        gc.setTransform(originalTransform);
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

        gc.setTransform(transform);
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
        System.out.println("click left event");
        Position p = transform(event.getX(), event.getY(), this.leftBoardCanvas);
        System.out.println(p);
    }

    /**
     * Method to handle click on canvas.
     * Transform coordinations
     * @param event
     */
    @FXML
    public void handleRightCanvasClick(MouseEvent event) {
        System.out.println("right click event");
        Position p = transform(event.getX(), event.getY(), this.rightBoardCanvas);
        System.out.println(p);
    }

    /**
     * Method to transform coordination from canvas to game coordination
     * @param x x coordinate
     * @param y y coordinate
     * @return instance of Point that includes game coordination
     */
    public Position transform(double x, double y, Canvas boardCanvas) {

        double width = boardCanvas.getWidth();
        double height = boardCanvas.getHeight();

        double size = Math.min(width, height);

        double tx = 0, ty = 0;
        double scale = 1;

        if(width > height) {
            tx = (width - size) * 0.5;
        } else {
            ty = (height - size) * 0.5;
        }

        scale = PAINT_WIDTH / size;

        x -= tx;
        y -= ty;

        x *= scale;
        y *= scale;

        x /= SQUARE_SIZE;
        y /= SQUARE_SIZE;

        int nx = (int) Math.floor(x);
        int ny = (int) Math.floor(y);

        if(nx > MAX || nx < MIN) return null;
        if(ny > MAX || ny < MIN) return null;

        /*if(this.gameModel.getPlayerColor() == GameModel.PlayerColor.BLACK) {
            nx = 7 - nx;
            ny = 7 - ny;
        }*/

        return new Position(nx, ny);
    }
}
