package vondrovic.ups.sp.client.model;

import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 */
public class Board {

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
     * ships located at board
     */
    private Ship[] boardShips;

    /**
     * information if the board belongs to enemy
     */
    private final boolean isEnemy;

    /**
     * status
     */
    private final Square[][] squares = new Square[MAX+1][MAX+1];

    /**
     * instance of random generator
     */
    private final Random rand;

    /**
     * currently picked up ship
     */
    private Ship pickedUpShip = null;

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

    /**
     * Initialize array of ships
     */
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
        if (isEnemy)
        {
            switch (square.getSquareStatus()) {
                case SHIP:
                    square.hitShip();
                    if (isShipDestroyed(square)) {
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
        }
        else // setting up ship
        {
            if (pickedUpShip != null && pickedUpShip.getBoardPosition() == p)
            {
                pickedUpShip.setPickedUp(false);
                pickedUpShip = null;
                this.boardCanvas.setCursor(null);
                repaint();
                return false;
            }

            if (event.getClickCount() == 2)
            {
                if (square.getSquareStatus() == SquareStatus.SHIP && square.getShip() != null)
                {
                    turnShip(square.getShip());
                }
            }
            else if (event.getClickCount() == 1)
            {
                if (this.boardCanvas.getCursor() == null)
                {
                    if (square.getSquareStatus() != SquareStatus.SHIP || square.getShip() == null)
                    {
                        return false;
                    }

                    square.getShip().setPickedUp(true);
                    this.pickedUpShip = square.getShip();

                    this.boardCanvas.setCursor(Cursor.HAND);
                }
                else if (belongsPositionToShip(p, this.pickedUpShip))
                {
                    this.pickedUpShip.setPickedUp(false);
                    this.pickedUpShip = null;
                    this.boardCanvas.setCursor(null);
                }
                else
                {
                    relocatePickedUpShip(p);
                }
            }

        }
        repaint();
        return true;
    }

    /**
     * Turn given ship to be vertical/not vertical
     * @param ship  ship to be turned
     */
    private void turnShip(Ship ship)
    {
        int firstX = ship.getBoardPosition().getX();
        int firstY = ship.getBoardPosition().getY();
        boolean result = removeShip(ship);
        if (result)
        {
            ship.setVertical(!ship.isVertical());
            result = placeShip(ship, firstX, firstY);

            if (!result)
            {
                ship.setVertical(!ship.isVertical());
                placeShip(ship, firstX, firstY);
            }
        }
    }

    /**
     * Relocate picked up ship to give position
     * - will not success if ship cannot be placed on new position
     * @param p position, where ship try to be located
     */
    private void relocatePickedUpShip(Position p)
    {
        if (canPlaceShip(pickedUpShip, p.getX(), p.getY()))
        {
            removeShip(pickedUpShip);
            placeShip(pickedUpShip, p.getX(), p.getY());
            pickedUpShip.setPickedUp(false);
            pickedUpShip = null;
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
     * Find if ship on given square is destroyed
     * @param square    square, where ship should be located
     * @return          false - not located ship or ship is destroyed, otherwise true
     */
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

    /**
     * If ship is destroyed mark her neighbours as SquareStatus.MISSED
     * @param square    square, where destroyed ship should be
     */
    public void markDestroyedShip(Square square)
    {
        if (!isShipDestroyed(square))
        {
            return;
        }

        Ship ship = square.getShip();

        if (ship == null)
        {
            return;
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

            ship.setBoardPosition(new Position(x, y));
            if (ship.isVertical()) {
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

                    if (pickedUpShip != null)
                    {
                        if (neighbor.getSquareStatus() == SquareStatus.SHIP && neighbor.getShip().equals(pickedUpShip))
                        {
                            continue;
                        }
                    }

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

                    if (pickedUpShip != null)
                    {
                        if (neighbor.getSquareStatus() == SquareStatus.SHIP && neighbor.getShip().equals(pickedUpShip))
                        {
                            continue;
                        }
                    }

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

    private boolean removeShip(Ship ship)
    {
        Position firstBlockPosition = ship.getBoardPosition();

        if (firstBlockPosition == null || !isValidPoint(firstBlockPosition))
        {
            return false;
        }

        int x = firstBlockPosition.getX();
        int y = firstBlockPosition.getY();
        Square firstBlock = squares[x][y];

        if (firstBlock.getShip() == null || firstBlock.getSquareStatus() != SquareStatus.SHIP)
        {
            return false;
        }

        int length = ship.getShipType().getLength();

        if (ship.isVertical()) {
            for (int i = y; i < y + length; i++) {
                Square square = squares[x][i];
                square.removeShip();
            }
        }
        else {
            for (int i = x; i < x + length; i++) {
                Square square = squares[i][y];
                square.removeShip();
            }
        }

        return true;
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
}
