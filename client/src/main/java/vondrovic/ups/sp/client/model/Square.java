package vondrovic.ups.sp.client.model;

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

    public void setSquareStatus(SquareStatus squareStatus) {
        this.squareStatus = squareStatus;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public SquareStatus getSquareStatus() {
        return squareStatus;
    }
}
