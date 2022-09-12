package vondrovic.ups.sp.client.model;

import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 */
public class Board {

    Affine originalTransform;

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

    private final double SQUARE_SIZE = PAINT_WIDTH / BOARD_LENGTH;

    private final Canvas boardCanvas;


    private Ship[] boardShips;

    /**
     * information if the board belongs to enemy
     */
    private final boolean isEnemy;

    /**
     * status
     */
    private final Square[][] squares = new Square[MAX+1][MAX+1];

    private final Random rand;

    /**
     * Create instance of the Board
     * @param canvasBoard   canvas, where board is painted
     * @param isEnemy       if board belong to enemy
     */
    public Board(Canvas canvasBoard, boolean isEnemy) {
        rand = new Random();
        this.boardCanvas = canvasBoard;
        this.isEnemy = isEnemy;

        toStartForm();

        this.boardCanvas.setOnDragDetected( new EventHandler<MouseEvent>() {
            @Override
            public void handle( MouseEvent event ) {
                dragEvent(event);
            }
        });

        this.boardCanvas.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                dragDroppedEvent(event);
            }
        });
    }

    /**
     * Initialized board to start form
     */
    public void toStartForm()
    {
        for (int i = MIN; i <= MAX; i++)
        {
            for (int j = MIN; j <= MAX; j++)
            {
                squares[i][j] = new Square(i, j, SquareStatus.EMPTY);
            }
        }

        initializeShips();
        populateShips();
    }

    private void initializeShips()
    {
        this.boardShips = new Ship[]{
                new Ship(ShipType.CARRIER),
                new Ship(ShipType.CRUISER),
                new Ship(ShipType.CRUISER),
                new Ship(ShipType.DESTROYER),
                new Ship(ShipType.DESTROYER),
                new Ship(ShipType.DESTROYER),
                new Ship(ShipType.SUBMARINE)
        };
    }


    private void dragEvent(MouseEvent event)
    {
        System.out.println("drag");
    }

    private void dragDroppedEvent(DragEvent event)
    {
        System.out.println("drop");
    }


    /**
     * Method to repaint canvas
     */
    public void repaint() {
        GraphicsContext gc = this.boardCanvas.getGraphicsContext2D();
        drawBoard(gc);
    }


    public void drawBoard(GraphicsContext gc)
    {
        this.originalTransform = gc.getTransform();
        fillBackground(gc, this.originalTransform);

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
                                gc.setFill(Color.WHITE);
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


    public boolean handleCanvasClick(MouseEvent event)
    {
        Position p = transform(event.getX(), event.getY());

        if (p != null)
        {
            Square square = squares[p.getX()][p.getY()];
            switch(square.getSquareStatus())
            {
                case SHIP:
                    square.hitShip();
                    if (isShipDestroyed(square))
                    {
                        markDestroyedShip(square);
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

            repaint();
            return true;
        }

        return false;
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


    public boolean isShipDestroyed(Square square)
    {
        if (square.getShip() == null)
        {
            return false;
        }
        else
        {
            return !square.getShip().isAlive();
        }

    }

    public boolean markDestroyedShip(Square square)
    {
        if (!isShipDestroyed(square))
        {
            return false;
        }

        Ship ship = square.getShip();

        if (ship == null)
        {
            return false;
        }

        int i = -1;
        if (ship.isVertical())
        {
            // mark below neighbours
            while (isValidPoint(square.getX(), square.getY()+i)
                    && squares[square.getX()][square.getY()+i].getSquareStatus() == SquareStatus.HIT)
            {
                markNeighboursSquareAsMissed(squares[square.getX()][square.getY()+i]);
                i--;
            }


            i = 1;
            // mark above neighbour
            while (isValidPoint(square.getX(), square.getY()+i)
                    && squares[square.getX()][square.getY()+i].getSquareStatus() == SquareStatus.HIT)
            {
               markNeighboursSquareAsMissed(squares[square.getX()][square.getY()+i]);
               i++;
            }
        }
        else
        {
            // mark left pieces neighbours
            while (isValidPoint(square.getX()+i, square.getY())
                    && squares[square.getX()+i][square.getY()].getSquareStatus() == SquareStatus.HIT)
            {
                markNeighboursSquareAsMissed(squares[square.getX()+i][square.getY()]);
                i--;
            }

            i = 1;
            // mark right pieces neighbour
            while (isValidPoint(square.getX()+i, square.getY()))
            {
                System.out.println(squares[square.getX()+i][square.getY()].getSquareStatus());
                if( squares[square.getX()+i][square.getY()].getSquareStatus() == SquareStatus.HIT)
                {
                    markNeighboursSquareAsMissed(squares[square.getX()+i][square.getY()]);
                    i++;
                }
                else
                {
                    break;
                }

            }
        }

        //mark neighbours of the hit piece itself
        markNeighboursSquareAsMissed(squares[square.getX()][square.getY()]);

        return true;
    }


    /**
     * Mark square neighbours as MISSED, if they are not HIT
     * @param square        central square
     */
    private void markNeighboursSquareAsMissed(Square square)
    {
        Square[] squareNeighbors = getNeighbors(square.getX(), square.getY());
        for (Square squareNeighbor : squareNeighbors) {
            if (squareNeighbor.getSquareStatus() != SquareStatus.HIT) {
                squareNeighbor.setSquareStatus(SquareStatus.MISSED);
            }
        }
    }


    /**
     * Place ship at with first point at given coordinates
     * @param ship  ship to place
     * @param x     x-coordinate
     * @param y     y-coordinate
     * @return      true - successfully place, otherwise return false
     */
    public boolean placeShip(Ship ship, int x, int y) {
        if (canPlaceShip(ship, x, y)) {
            int length = ship.shipType.getLength();

            if (ship.vertical) {
                for (int i = y; i < y + length; i++) {
                    Square square = squares[x][i];
                    square.placeShip(ship);
                    //square.setSquareStatus(SquareStatus.SHIP);
                }
            }
            else {
                for (int i = x; i < x + length; i++) {
                    Square square = squares[i][y];
                    square.placeShip(ship);
                    //square.setSquareStatus(SquareStatus.SHIP);
                }
            }

            return true;
        }

        return false;
    }

    /**
     * Can ship be placed at given coordinates?
     * @param ship      ship
     * @param x         x-coordinate
     * @param y         y-coordinate
     * @return          true - can be place, false - cannot be
     */
    private boolean canPlaceShip(Ship ship, int x, int y) {
        int length = ship.shipType.getLength();

        if (ship.vertical) {
            for (int i = y; i < y + length; i++) {
                if (!isValidPoint(x, i))
                    return false;

                Square square = squares[x][y];
                if (square.getSquareStatus() != SquareStatus.EMPTY)
                    return false;

                for (Square neighbor : getNeighbors(x, i)) {
                    if (!isValidPoint(x, i))
                        return false;

                    if (neighbor.getSquareStatus() != SquareStatus.EMPTY)
                        return false;
                }
            }
        }
        else {
            for (int i = x; i < x + length; i++) {
                if (!isValidPoint(i, y))
                    return false;

                Square square = squares[i][y];
                if (square.getSquareStatus() != SquareStatus.EMPTY)
                    return false;

                for (Square neighbor : getNeighbors(i, y)) {
                    if (!isValidPoint(i, y))
                        return false;

                    if (neighbor.getSquareStatus() != SquareStatus.EMPTY)
                        return false;
                }
            }
        }

        return true;
    }

    /**
     * Find neighbours of square on given position
     * @param x x-coordinate
     * @param y y-coordinate
     * @return  array of neighbours from 8 sides
     */
    private Square[] getNeighbors(int x, int y) {
        Position[] points = new Position[] {
                new Position(x - 1, y),
                new Position(x + 1, y),
                new Position(x, y - 1),
                new Position(x, y + 1),

                new Position(x + 1, y + 1),
                new Position(x - 1, y - 1),
                new Position(x + 1, y - 1),
                new Position(x - 1, y + 1)
        };

        List<Square> neighbors = new ArrayList<>();

        for (Position p : points) {
            if (isValidPoint(p)) {
                neighbors.add(squares[p.getX()][p.getY()]);
            }
        }

        return neighbors.toArray(new Square[0]);
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
     * Clears all current ships, and then randomly places all the ships. The ships
     * will not be placed over the top of other ships. This method assumes there is
     * plenty of space to place all the ships regardless of configuration.
     */
    public void populateShips() {
        for (Ship boardShip : boardShips) {
            boolean vertical = rand.nextBoolean();

            boardShip.setVertical(vertical);
            int gridX, gridY;
            do {
                gridX = rand.nextInt(vertical ? MAX - boardShip.shipType.getLength() : MAX);
                gridY = rand.nextInt(vertical ? MAX : MAX - boardShip.shipType.getLength());
            } while (!canPlaceShip(boardShip, gridX, gridY));
            placeShip(boardShip, gridX, gridY);
        }
    }
}
