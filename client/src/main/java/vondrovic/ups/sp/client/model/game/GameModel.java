package vondrovic.ups.sp.client.model.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Class GameModel take care of holding data of game boards and game status
 */
public class GameModel {

    private Square[][] enemyBoard;
    private Square[][] myBoard;
    private GameStatus gameStatus;
    private String winner;
    private Integer pickedUpShipId = null;
    /** instance of random generato */
    private final Random rand;

    /**
     * ships located at board
     */
    private Ship[] boardShips;

    private final int MIN = 1;
    private final int MAX = 10;

    /**
     * Create instance of the game model
     */
    public GameModel() {
        rand = new Random();
        this.enemyBoard = new Square[MAX + 1][MAX + 1];
        this.myBoard = new Square[MAX + 1][MAX + 1];
        this.winner = null;
    }

    /**
     * Initialized board
     * @param squares   game board to be initialized
     */
    private void init_board(Square[][] squares) {
        for (int i = MIN; i <= MAX; i++) {
            for (int j = MIN; j <= MAX; j++) {
                squares[i][j] = new Square(i, j, SquareStatus.EMPTY);
            }
        }
    }

    /**
     * Initialize game - initialize both board, ships and populate them
     */
    public void init()
    {
        init_board(this.enemyBoard);
        init_board(this.myBoard);
        initializeShips();
        populateMyShips();
    }

    /**
     * @return  game status
     */
    public GameStatus getGameStatus()
    {
        return this.gameStatus;
    }

    /**
     * @return  opponent game board
     */
    public Square[][] getEnemyBoard() {
        return enemyBoard;
    }

    /**
     * @return  client game board
     */
    public Square[][] getMyBoard() {
        return myBoard;
    }

    /**
     * Set game status
     * @param gameStatus    game status to be set
     */
    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    /**
     * Set given position at enemy board to given status
     * @param x     x-coordinate
     * @param y     y-coordinate
     * @param status    game status
     */
    public void hitEnemy(int x, int y, SquareStatus status)
    {
        this.enemyBoard[x][y].setSquareStatus(status);
    }

    public void beingHit(int x, int y, SquareStatus status)
    {
        this.myBoard[x][y].setSquareStatus(status);
    }

    /**
     * Clears all current ships, and then randomly places all the ships. The ships
     * will not be placed over the top of other ships. This method assumes there is
     * plenty of space to place all the ships regardless of configuration.
     */
    public void populateMyShips() {
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
     * Initialize array of ships
     */
    public void initializeShips() {
        this.boardShips = new Ship[]{
                new Ship(ShipType.CARRIER, 0),
                new Ship(ShipType.CRUISER, 1),
                new Ship(ShipType.CRUISER, 2),
                new Ship(ShipType.DESTROYER, 3),
                new Ship(ShipType.DESTROYER, 4),
                new Ship(ShipType.DESTROYER, 5),
                new Ship(ShipType.SUBMARINE, 6)
        };
    }

    /**
     * Place ship at with first point at given coordinates
     *
     * @param ship ship to place
     * @param x    x-coordinate
     * @param y    y-coordinate
     * @return true - successfully place, otherwise return false
     */
    public boolean placeShip(Ship ship, int x, int y) {
        if (canPlaceShip(ship, x, y)) {
            int length = ship.getShipType().getLength();

            ship.setBoardPosition(new Position(x, y));
            if (ship.isVertical()) {
                for (int i = y; i < y + length; i++) {
                    Square square = myBoard[x][i];
                    square.placeShip(ship);
                    //square.setSquareStatus(SquareStatus.SHIP);
                }
            } else {
                for (int i = x; i < x + length; i++) {
                    Square square = myBoard[i][y];
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
     *
     * @param ship ship
     * @param x    x-coordinate
     * @param y    y-coordinate
     * @return true - can be place, false - cannot be
     */
    public boolean canPlaceShip(Ship ship, int x, int y) {
        int length = ship.getShipType().getLength();

        if (ship.isVertical()) {
            for (int i = y; i < y + length; i++) {
                if (!isValidPoint(x, i))
                    return false;

                Square square = myBoard[x][y];
                if (square.getSquareStatus() != SquareStatus.EMPTY)
                    return false;

                for (Square neighbor : getNeighbors(x, i, myBoard)) {
                    if (!isValidPoint(x, i))
                        return false;

                    if (pickedUpShipId != null) {
                        if (neighbor.getSquareStatus() == SquareStatus.SHIP && neighbor.getShip().getId() == pickedUpShipId) {
                            continue;
                        }
                    }

                    if (neighbor.getSquareStatus() != SquareStatus.EMPTY)
                        return false;
                }
            }
        } else {
            for (int i = x; i < x + length; i++) {
                if (!isValidPoint(i, y))
                    return false;

                Square square = myBoard[i][y];
                if (square.getSquareStatus() != SquareStatus.EMPTY)
                    return false;

                for (Square neighbor : getNeighbors(i, y, myBoard)) {
                    if (!isValidPoint(i, y))
                        return false;

                    if (pickedUpShipId != null) {
                        if (neighbor.getSquareStatus() == SquareStatus.SHIP && neighbor.getShip().getId() == pickedUpShipId) {
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
     *
     * @param x x-coordinate
     * @param y y-coordinate
     * @return array of neighbours from 8 sides
     */
    private Square[] getNeighbors(int x, int y, Square[][] board) {
        Position[] points = new Position[]{
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
                neighbors.add(board[p.getX()][p.getY()]);
            }
        }

        return neighbors.toArray(new Square[0]);
    }

    /**
     * Find out if the point is valid
     *
     * @param point point (position) to be checked
     * @return true - is valid, otherwise false
     */
    private boolean isValidPoint(Position point) {
        return isValidPoint(point.getX(), point.getY());
    }

    /**
     * Find out if the point is valid
     *
     * @param x x-coordinate
     * @param y y-coordinate
     * @return true - is valid, otherwise false
     */
    private boolean isValidPoint(int x, int y) {
        return x >= MIN && x <= MAX && y >= MIN && y <= MAX;
    }

    /**
     * Remove ship from the canvas of the board
     *
     * @param ship ship to be removed
     * @return true - success, false- fail
     */
    public boolean removeShip(Ship ship) {
        Position firstBlockPosition = ship.getBoardPosition();

        if (firstBlockPosition == null || !isValidPoint(firstBlockPosition)) {
            return false;
        }

        int x = firstBlockPosition.getX();
        int y = firstBlockPosition.getY();
        Square firstBlock = myBoard[x][y];

        if (firstBlock.getShip() == null || firstBlock.getSquareStatus() != SquareStatus.SHIP) {
            return false;
        }

        int length = ship.getShipType().getLength();

        if (ship.isVertical()) {
            for (int i = y; i < y + length; i++) {
                Square square = myBoard[x][i];
                square.removeShip();
            }
        } else {
            for (int i = x; i < x + length; i++) {
                Square square = myBoard[i][y];
                square.removeShip();
            }
        }

        return true;
    }

    /**
     * Turn given ship to be vertical/not vertical
     *
     * @param ship ship to be turned
     */
    public void turnShip(Ship ship) {
        int firstX = ship.getBoardPosition().getX();
        int firstY = ship.getBoardPosition().getY();
        boolean result = removeShip(ship);
        if (result) {
            ship.setVertical(!ship.isVertical());
            result = placeShip(ship, firstX, firstY);

            if (!result) {
                ship.setVertical(!ship.isVertical());
                placeShip(ship, firstX, firstY);
            }
        }
    }

    /**
     * Relocate picked up ship to give position
     * - will not success if ship cannot be placed on new position
     *
     * @param p position, where ship try to be located
     */
    public void relocatePickedUpShip(Position p) {
        if (canPlaceShip(boardShips[pickedUpShipId], p.getX(), p.getY())) {
            removeShip(boardShips[pickedUpShipId]);
            placeShip(boardShips[pickedUpShipId], p.getX(), p.getY());
            boardShips[pickedUpShipId].setPickedUp(false);
            pickedUpShipId = null;
        }
    }

    public Integer getPickedUpShipId() {
        return pickedUpShipId;
    }

    public void setPickedUpShipId(Integer pickedUpShipId) {
        this.pickedUpShipId = pickedUpShipId;
    }

    public Ship getPickedUpShip() {
        if (pickedUpShipId == null) {
            return null;
        }

        return this.boardShips[pickedUpShipId];
    }

    /**
     * Mark square neighbours as MISSED, if they are not HIT
     *
     * @param square central square
     */
    private void markNeighboursSquareAsMissed(Square square, Square[][] board) {
        Square[] squareNeighbors = getNeighbors(square.getX(), square.getY(), board);
        for (Square squareNeighbor : squareNeighbors) {
            if (squareNeighbor.getSquareStatus() != SquareStatus.HIT) {
                squareNeighbor.setSquareStatus(SquareStatus.MISSED);
            }
        }
    }

    /**
     * If ship is destroyed mark her neighbours as SquareStatus.MISSED
     * @param x     x-coordinate of one of ship squares
     * @param y     y-coordinate of one of ship squares
     * @param board board in which se square is located
     */
    public void markDestroyedShip(int x, int y, Square[][] board) {

        Square square = board[x][y];
        boolean isVertical = false;

        if (square.getSquareStatus() != SquareStatus.HIT) {
            return;
        }

        //mark neighbours of the hit piece itself
        markNeighboursSquareAsMissed(board[square.getX()][square.getY()], board);

        // if square above or under are marked as hit -> ship is placed vertical
        if (isValidPoint(x, y + 1)) {
            if (board[x][y + 1].getSquareStatus() == SquareStatus.HIT) {
                isVertical = true;
            }
        }

        if (isValidPoint(x, y - 1)) {
            if (board[x][y - 1].getSquareStatus() == SquareStatus.HIT) {
                isVertical = true;
            }
        }

        int i = -1;
        if (isVertical) {
            // mark below neighbours
            while (isValidPoint(square.getX(), square.getY() + i)
                    && board[square.getX()][square.getY() + i].getSquareStatus() == SquareStatus.HIT) {
                markNeighboursSquareAsMissed(board[square.getX()][square.getY() + i], board);
                i--;
            }


            i = 1;
            // mark above neighbour
            while (isValidPoint(square.getX(), square.getY() + i)
                    && board[square.getX()][square.getY() + i].getSquareStatus() == SquareStatus.HIT) {
                markNeighboursSquareAsMissed(board[square.getX()][square.getY() + i], board);
                i++;
            }
        } else {
            // mark left pieces neighbours
            while (isValidPoint(square.getX() + i, square.getY())
                    && board[square.getX() + i][square.getY()].getSquareStatus() == SquareStatus.HIT) {
                markNeighboursSquareAsMissed(board[square.getX() + i][square.getY()], board);
                i--;
            }

            i = 1;
            // mark right pieces neighbour
            while (isValidPoint(square.getX() + i, square.getY())) {
                if (board[square.getX() + i][square.getY()].getSquareStatus() == SquareStatus.HIT) {
                    markNeighboursSquareAsMissed(board[square.getX() + i][square.getY()], board);
                    i++;
                } else {
                    break;
                }

            }
        }
    }


    /**
     * Get player board in string form
     * @return  Player (mine) board in string form
     */
    public String getMyBoardStringForm() {
        StringBuilder stringForm = new StringBuilder();
        Square sq;
        SquareStatus squareStatus;
        for (int i = MIN; i <= MAX; i++) {
            for (int j = MIN; j <= MAX; j++) {
                sq = myBoard[j][i];
                squareStatus = sq.getSquareStatus();
                switch(squareStatus)
                {
                    case SHIP:
                        stringForm.append(sq.getShip().getId());
                        break;
                    case HIT:
                        stringForm.append("H");
                        break;
                    case MISSED:
                        stringForm.append("M");
                        break;
                    case EMPTY:
                        stringForm.append("E");
                        break;
                    default:
                        break;
                }
            }
            //stringForm.append(",");
        }
        stringForm.append("\0");
        return stringForm.toString();
    }

    /**
     * Convert given string representing game board to board
     * @param string_form   board in string form
     * @param board         board to which the string board is written to
     * @return              true - success, false - string was in wrong format
     */
    public void convertStringToBoard(String string_form, Square[][] board)
    {

        /*if (string_form.length() != ((MAX * MAX))) // + 1 is for \0
        {
            return;
        }*/

        init_board(board);
        int x = MIN;
        int y = MIN;
        for (int i = 0; i < string_form.length(); i++)
        {
            /*
            if (string_form.charAt(i) == ',' || string_form.charAt(i) == '\0')
            {
                continue;
            }*/

            switch(string_form.charAt(i))
            {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                    board[x][y].setSquareStatus(SquareStatus.SHIP);
                    board[x][y].placeShip(boardShips[Character.getNumericValue(string_form.charAt(i))]);
                    break;
                case 'H':
                case 'h':
                    board[x][y].setSquareStatus(SquareStatus.HIT);
                    break;
                case 'M':
                case 'm':
                    board[x][y].setSquareStatus(SquareStatus.MISSED);
                    break;
                case 'E':
                case 'e':
                default:
                    board[x][y].setSquareStatus(SquareStatus.EMPTY);
                    break;
            }
            x++;
            if (x > MAX)
            {
                x = MIN;
                y++;

                if (y > MAX)
                    break;
            }
        }
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }
}