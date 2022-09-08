package vondrovic.ups.sp.client.model;

/**
 * Class to save position coordinates
 */
public class Position {

    /**
     * y-position
     */
    private final int x;

    /**
     * y-position
     */
    private final int y;

    /**
     * Constructor to create new instance of position
     * @param x x position coordinate
     * @param y y position coordinate
     */
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Getter for x coordinate
     * @return  x-coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Getter for y coordinate
     * @return  y-coordinate
     */
    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return "Position{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}