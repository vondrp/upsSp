package vondrovic.ups.sp.client.model.game;

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
 * Class Board represents board of the game
 */
public class GameBoard {

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
    public GameBoard(Canvas canvasBoard, boolean isEnemy) {
        rand = new Random();
        this.boardCanvas = canvasBoard;
        this.isEnemy = isEnemy;

        for (int i = MIN; i <= MAX; i++)
        {
            for (int j = MIN; j <= MAX; j++)
            {
                squares[i][j] = new Square(i, j, SquareStatus.EMPTY);
            }
        }

        // if the board belongs to enemy ships not initialized
        if (!isEnemy)
        {
            initializeShips();
            populateShips();
        }
        else    // enemy board does not need drag function (used for moving ships)
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
            this.pickedUpShip = square.getShip();

            this.boardCanvas.setCursor(Cursor.HAND);
            repaint();
        }
    }

    /**
     * Initialize array of ships
     */
    private void initializeShips()
    {
        this.boardShips = new Ship[]{
                new Ship(ShipType.CARRIER, 1),
                new Ship(ShipType.CRUISER, 2),
                new Ship(ShipType.CRUISER, 3),
                new Ship(ShipType.DESTROYER, 4),
                new Ship(ShipType.DESTROYER, 5),
                new Ship(ShipType.DESTROYER, 6),
                new Ship(ShipType.SUBMARINE, 7)
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
                    if (isFriendlyShipDestroyed(square)) {
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
            // cancel picking up ship when click on the same position
            if (pickedUpShip != null && pickedUpShip.getBoardPosition() == p)
            {
                pickedUpShip.setPickedUp(false);
                pickedUpShip = null;
                this.boardCanvas.setCursor(null);
                repaint();
                return false;
            }

            // handling DROP
            // turning ship to (not ) be vertical, works only in no ship is picked up
            if (square.getSquareStatus() == SquareStatus.SHIP && square.getShip() != null && pickedUpShip == null)
            {
                turnShip(square.getShip());
                repaint();
                return true;
            }

            // if cursor is == null - nothing is picked up + checked if selected square belongs to a ship
            // protection against error which could occur lower
            if (this.boardCanvas.getCursor() == null &&
                    (square.getSquareStatus() != SquareStatus.SHIP || square.getShip() == null)
                    || this.pickedUpShip == null)
            {
                return false;
            }

            // if clicked on the currently picked up ship, ship picking up cancel
            if (belongsPositionToShip(p, this.pickedUpShip))
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
     * If ship is destroyed mark her neighbours as SquareStatus.MISSED
     * @param square    square, where destroyed ship should be
     */
    public void markDestroyedShip(Square square)
    {
        int x = square.getX();
        int y = square.getY();
        boolean isVertical = false;

        /*
        if (!isFriendlyShipDestroyed(square))
        {
            return;
        }*/
        if (square.getSquareStatus() != SquareStatus.HIT)
        {
            return;
        }

        //mark neighbours of the hit piece itself
        markNeighboursSquareAsMissed(squares[square.getX()][square.getY()]);

        // if square above or under are marked as hit -> ship is placed vertical
        if (isValidPoint(x, y + 1))
        {
            if (squares[x][y + 1].getSquareStatus() == SquareStatus.HIT)
            {
                isVertical = true;
            }
        }

        if (isValidPoint(x, y - 1))
        {
            if (squares[x][y - 1].getSquareStatus() == SquareStatus.HIT)
            {
                isVertical = true;
            }
        }

        int i = -1;
        if (isVertical)
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
            int length = ship.getShipType().getLength();

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
        int length = ship.getShipType().getLength();

        if (ship.isVertical()) {
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
                gridX = rand.nextInt(vertical ? MAX - boardShip.getShipType().getLength() : MAX);
                gridY = rand.nextInt(vertical ? MAX : MAX - boardShip.getShipType().getLength());
            } while (!canPlaceShip(boardShip, gridX, gridY));
            placeShip(boardShip, gridX, gridY);
        }
    }

    /**
     * Remove ship from the canvas of the board
     * @param ship  ship to be removed
     * @return      true - success, false- fail
     */
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
            markDestroyedShip(squares[x][y]);
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

    public void setBoard(Square[][] squares)
    {
        for (int x = 0; x < MAX; x++)
        {
            for (int y = 0; y < MAX; y++)
            {
                // set board squares - coordinates are inc + 1, because actual
                // game board includes additional 1 - 10 and A - J
                this.squares[x + 1][y + 1] = squares[x][y];
            }
        }
    }
}
