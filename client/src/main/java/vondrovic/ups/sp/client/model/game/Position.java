package vondrovic.ups.sp.client.model.game;

import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Position{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}