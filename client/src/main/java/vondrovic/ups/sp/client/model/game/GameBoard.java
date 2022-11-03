package vondrovic.ups.sp.client.model.game;

import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import vondrovic.ups.sp.client.App;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Class Board represents board of the game
 */
public class GameBoard {


    private char command_split = ';';
    /**
     * Width of the board
     */
    private final double PAINT_WIDTH = 400.0;

    /**
     * amount of board squares - including marking
     */
    private final int BOARD_LENGTH = 11;


    /**
     * min value of coordinates
     */
    private final int MIN = 1;

    /**
     * max value of coordinates
     */
    private final int MAX = 10;

    /**
     * size of one square
     */
    private final double SQUARE_SIZE = PAINT_WIDTH / BOARD_LENGTH;

    /**
     * canvas of Board
     */
    private final Canvas boardCanvas;


    /**
     * information if the board belongs to enemy
     */
    private final boolean isEnemy;

    /**
     * status
     */
    private Square[][] squares;

    /**
     * instance of random generator
     */
    private final Random rand;


    private GameModel gameModel;

    /**
     * Create instance of the Board
     * @param canvasBoard   canvas, where board is painted
     * @param isEnemy       if board belong to enemy
     */
    public GameBoard(Canvas canvasBoard, GameModel gameModel, boolean isEnemy) {
        rand = new Random();
        this.boardCanvas = canvasBoard;
        this.isEnemy = isEnemy;
        this.gameModel = gameModel;

        if (isEnemy)
        {
            squares = gameModel.getEnemyBoard();
        }
        else
        {
            squares = gameModel.getMyBoard();
        }

        // if the board belongs to enemy ships not initialized + enemy does not need drag function
        if (!isEnemy)
        {
            // set on dragging start - drop is in
            this.boardCanvas.setOnDragDetected(this::dragEvent);
        }
    }

    /**
     * Set on drag event start
     * @param event used mouse event
     */
    private void dragEvent(MouseEvent event)
    {
        Position p = transform(event.getX(), event.getY());
        if (p == null)
        {
            return ;
        }

        Square square = squares[p.getX()][p.getY()];

        if (this.boardCanvas.getCursor() == null)
        {
            if (square.getSquareStatus() != SquareStatus.SHIP || square.getShip() == null)
            {
                return ;
            }

            square.getShip().setPickedUp(true);

            this.gameModel.setPickedUpShipId(square.getShip().getId());
            this.boardCanvas.setCursor(Cursor.HAND);
            repaint();
        }
    }


    /**
     * Method to repaint canvas
     */
    public void repaint() {
        GraphicsContext gc = this.boardCanvas.getGraphicsContext2D();
        drawBoard(gc);
    }

    /**
     * Draw game board
     * @param gc    canvas graphicsContext2D
     */
    public void drawBoard(GraphicsContext gc)
    {
        Affine originalTransform = gc.getTransform();
        fillBackground(gc, originalTransform);

        double width = this.boardCanvas.getWidth();
        double height = this.boardCanvas.getHeight();

        double size = Math.min(width, height);
        //double max = Math.max(width, height);

        double tx = 0, ty = 0;
        //double scale = 1;

        if (width > height)
        {
            tx = (width - size) * 0.5;
        } else
        {
            ty = (height - size) * 0.5;
        }

        double scale = size / PAINT_WIDTH;


        gc.translate(tx, ty);
        gc.scale(scale, scale);

        Affine defaultTransform = gc.getTransform();

        char letter = 'A';
        for(int i = 0; i < BOARD_LENGTH; i++) {
            for(int j = 0; j < BOARD_LENGTH; j++) {
                if (j == 0 && i == 0) // top left corner
                {
                    // really nothing
                }
                else if (j == 0) // row with letters
                {
                    gc.setStroke(Color.BLACK);
                    gc.setFill(Color.BLACK);
                    gc.fillText(letter+"",
                            i * SQUARE_SIZE + (SQUARE_SIZE) / 2,
                            j * SQUARE_SIZE + (SQUARE_SIZE) / 2);
                    letter++;
                }
                else if (i == 0) // column with numbers
                {
                    gc.setStroke(Color.BLACK);
                    gc.setFill(Color.BLACK);
                    String string = (i + j)+"";
                    gc.fillText(string,
                            i * SQUARE_SIZE + (SQUARE_SIZE) / 2,
                            j * SQUARE_SIZE + (SQUARE_SIZE) / 2);
                }
                else
                {
                    switch (squares[i][j].getSquareStatus())
                    {
                        case SHIP:
                            if (!isEnemy)
                            {
                                gc.setStroke(Color.GREEN);
                                if (squares[i][j].getShip().isPickedUp())
                                {
                                    gc.setFill(Color.LIGHTGREEN);
                                }
                                else
                                {
                                    gc.setFill(Color.WHITE);
                                }
                            }
                            else
                            {
                                gc.setFill(Color.LIGHTGRAY);
                                gc.setStroke(Color.BLACK);
                            }
                            break;
                        case MISSED:
                            gc.setStroke(Color.DARKBLUE);
                            gc.setFill(Color.LIGHTBLUE);
                            break;
                        case HIT:
                            gc.setStroke(Color.BLACK);
                            gc.setFill(Color.RED);
                            break;
                        case EMPTY:
                        default:
                            gc.setFill(Color.LIGHTGRAY);
                            gc.setStroke(Color.BLACK);
                            break;
                    }
                    gc.fillRect(i * SQUARE_SIZE, j * SQUARE_SIZE, SQUARE_SIZE , SQUARE_SIZE);
                    gc.strokeRect(i * SQUARE_SIZE, j * SQUARE_SIZE, SQUARE_SIZE , SQUARE_SIZE);
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
     * Handle click event
     * @param event     mouse event
     * @return          false - action failed, true - success
     */
    public boolean handleCanvasClick(MouseEvent event)
    {
        Position p = transform(event.getX(), event.getY());
        if (p == null)
        {
            return false;
        }

        Square square = squares[p.getX()][p.getY()];
        // in enemy case player is firing
        if (isEnemy && gameModel.getGameStatus() == GameStatus.PLAYING)
        {
            int serverX = p.getX() - 1;
            int serverY = p.getY() - 1;
            App.sendMessage("game_fire_req"+command_split+""+serverX+""+command_split+""+serverY);
            gameModel.setGameStatus(GameStatus.WAITING);
            /*
            switch (square.getSquareStatus()) {
                case SHIP:
                    square.hitShip();
                    if (isFriendlyShipDestroyed(square)) {
                        gameModel.markDestroyedShip(square, squares);
                    }
                    break;
                case EMPTY:
                    square.setSquareStatus(SquareStatus.MISSED);
                    break;
                case MISSED:
                case HIT:
                default:
                    return false;
            }
            */
        }
        else if (gameModel.getGameStatus() == GameStatus.PREPARING)// setting up ship
        {
            // cancel picking up ship when click on the same position
            if (gameModel.getPickedUpShipId() != null)
            {
                if (gameModel.getPickedUpShip().getBoardPosition() == p)
                {
                    gameModel.getPickedUpShip().setPickedUp(false);
                    gameModel.setPickedUpShipId(null);

                    this.boardCanvas.setCursor(null);
                    repaint();
                    return false;
                }
            }

            // handling DROP
            // turning ship to (not ) be vertical, works only in no ship is picked up
            if (square.getSquareStatus() == SquareStatus.SHIP && square.getShip() != null && gameModel.getPickedUpShip() == null)
            {
                gameModel.turnShip(square.getShip());
                repaint();
                return true;
            }

            // if cursor is == null - nothing is picked up + checked if selected square belongs to a ship
            // protection against error which could occur lower
            if (this.boardCanvas.getCursor() == null &&
                    (square.getSquareStatus() != SquareStatus.SHIP || square.getShip() == null)
                    || gameModel.getPickedUpShip() == null)
            {
                return false;
            }

            // if clicked on the currently picked up ship, ship picking up cancel
            if (belongsPositionToShip(p, gameModel.getPickedUpShip()))
            {
                gameModel.getPickedUpShip().setPickedUp(false);
                gameModel.setPickedUpShipId(null);
                this.boardCanvas.setCursor(null);
            }
            else
            {
                relocatePickedUpShip(p);
            }
        }
        repaint();
        return true;
    }


    /**
     * Relocate picked up ship to give position
     * - will not success if ship cannot be placed on new position
     * @param p position, where ship try to be located
     */
    private void relocatePickedUpShip(Position p)
    {
        if (gameModel.canPlaceShip(gameModel.getPickedUpShip(), p.getX(), p.getY())) {
            gameModel.relocatePickedUpShip(p);
            this.boardCanvas.setCursor(null);
        }
    }

    /**
     * Method to transform coordination from canvas to game coordination
     * @param x x coordinate
     * @param y y coordinate
     * @return instance of Point that includes game coordination
     */
    public Position transform(double x, double y) {

        double width = this.boardCanvas.getWidth();
        double height = this.boardCanvas.getHeight();

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

        return new Position(nx, ny);
    }

    /**
     * Find if friendly (not enemy board) ship on given square is destroyed
     * @param square    square, where ship should be located
     * @return          false - not located ship or ship is destroyed, otherwise true
     */
    public boolean isFriendlyShipDestroyed(Square square)
    {
        if (!isEnemy) { //in case of the ship no being enemy one - faster ways of finding out if ship destroyed
            if (square.getShip() == null) {
                return false;
            } else {
                return !square.getShip().isAlive();
            }
        }

        return false;
    }


    /**
     * Find out if the point is valid
     * @param point point (position) to be checked
     * @return true - is valid, otherwise false
     */
    private boolean isValidPoint(Position point) {
        return isValidPoint(point.getX(), point.getY());
    }

    /**
     * Find out if the point is valid
     * @param x x-coordinate
     * @param y y-coordinate
     * @return  true - is valid, otherwise false
     */
    private boolean isValidPoint(int x, int y) {
        return x >= MIN && x <= MAX && y >= MIN && y <= MAX;
    }

    /**
     *
     * @return  board canvas
     */
    public Canvas getBoardCanvas() {
        return boardCanvas;
    }



    /**
     * Find out if given position, belongs to ship
     * @param p     examined position
     * @param ship  checked ship
     * @return      true - position belongs to ship, otherwise return false
     */
    private boolean belongsPositionToShip(Position p, Ship ship)
    {
        ArrayList<Position> shipPositions = getShipPositions(ship);

        return shipPositions.contains(p);
    }

    /**
     * Find all positions of board, where given ship is located
     * @param ship  ship, which position are looked for
     * @return  arrayList of ship positions
     */
    private ArrayList<Position> getShipPositions(Ship ship)
    {
        Position firstPosition = ship.getBoardPosition();

        int length = ship.getShipType().getLength();
        int x = firstPosition.getX();
        int y = firstPosition.getY();

        ArrayList<Position> positions = new ArrayList<>(length);
        if (ship.isVertical())
        {
            for (int i = y; i < y + length; i++) {
                positions.add(new Position(x, i));
            }
        }
        else
        {
            for (int i = x; i < x + length; i++) {
                positions.add(new Position(i, y));
            }
        }

        return positions;
    }

    /**
     * Square on giving coordinates been hit
     * change its status, if ship destroyed, mark it
     * @param x         x-coordinate
     * @param y         y-coordinate
     * @param status    square status after being hit
     * @param destroyed true - on the square was destroyed ship (last piece of it hit), otherwise false
     */
    public void hitSquare(int x, int y, SquareStatus status, boolean destroyed)
    {
        setSquareStatus(x, y, status);

        if (destroyed)
        {
            gameModel.markDestroyedShip(x, y, squares);
        }
    }

    /**
     * Set square on give coordinates status
     * @param x         x-coordinate
     * @param y         y-coordinate
     * @param status    new status of square
     */
    private void setSquareStatus(int x, int y, SquareStatus status)
    {
        this.squares[x][y].setSquareStatus(status);
        repaint();
    }
}