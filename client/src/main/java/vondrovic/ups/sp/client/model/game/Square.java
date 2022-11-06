package vondrovic.ups.sp.client.model.game;

/**
 * Class Square represent one square of the Battleship game board
 */
public class Square {

    /** x-coordinates */
    private int x;

    /** y-coordinates */
    private int y;

    /**
     * status of square
     */
    private SquareStatus squareStatus;

    /**
     * reference on the ship at the square
     */
    private Ship ship = null;

    /**
     * Creates square
     * @param x
     * @param y
     * @param squareStatus
     */
    public Square(int x, int y, SquareStatus squareStatus) {
        this.x = x;
        this.y = y;
        this.squareStatus = squareStatus;
    }

    /**
     * Set status of the square
     * @param squareStatus  square status
     */
    public void setSquareStatus(SquareStatus squareStatus) {
        this.squareStatus = squareStatus;
    }

    /**
     * Place ship at the square
     * by setting ship and changing SquareStatus to Ship
     * @param ship  Ship to be placed
     */
    public void placeShip(Ship ship)
    {
        setSquareStatus(SquareStatus.SHIP);
        this.ship = ship;
    }

    /**
     * remove ship from the square
     * by setting ship on null
     * and setting SquareStatus to Empty
     */
    public void removeShip()
    {
        this.ship = null;
        setSquareStatus(SquareStatus.EMPTY);
    }

    public void hitShip()
    {
        setSquareStatus(SquareStatus.HIT);

        if (ship != null)
        {
            ship.hit();
        }
    }

    /**
     * @return  x-coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * @return  y-coordinate
     */
    public int getY() {
        return y;
    }

    /**
     * @return  SquareStatus
     */
    public SquareStatus getSquareStatus() {
        return squareStatus;
    }

    /**
     * @return located ship
     */
    public Ship getShip()
    {
        return this.ship;
    }

}
