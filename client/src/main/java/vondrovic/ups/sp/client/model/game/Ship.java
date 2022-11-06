package vondrovic.ups.sp.client.model.game;

import java.util.Objects;

/**
 * Class Ship represents Ship object
 * - each Ship object has its shipType, can be vertical, health
 * - if placed should by set board position
 */
public class Ship {

    /**
     * ShipType of the ship
     */
    private ShipType shipType;

    /**
     * information if ship is placed vertical
     */
    private boolean vertical = true;

    /**
     * position where is placed first ship square
     */
    private Position boardPosition;

    /**
     * amount of ship health - max is according to shipType length
     */
    private int health;

    /**
     * information if ship is currently picked up
     */
    private boolean isPickedUp = false;

    /**
     * identifier of the ship
     */
    private int id;

    /**
     * Create instance of the ship
     * @param shipType  type of the ship
     * @param id        identifier of the ship
     * @param vertical  information if ship is placed vertical
     */
    public Ship(ShipType shipType, int id, boolean vertical)
    {
        this.shipType = shipType;
        this.vertical = vertical;
        this.health = this.shipType.getLength();
        this.id = id;
    }

    /**
     * Create instance of the ship
     * @param shipType  type of the ship
     */
    public Ship(ShipType shipType, int id)
    {
        this(shipType, id, false);
    }

    /**
     * @return  information if ship is alive
     */
    public boolean isAlive()
    {
        return health > 0;
    }

    /**
     * @return  if ship is (not) vertical
     */
    public boolean isVertical() {
        return vertical;
    }

    /**
     * set ship vertical status
     * @param vertical  new vertical status
     */
    public void setVertical(boolean vertical) {
        this.vertical = vertical;
    }

    /**
     * ship is being hit
     */
    public void hit()
    {
        health--;
    }

    /**
     * @return  ship first block position at the board
     */
    public Position getBoardPosition()
    {
        return this.boardPosition;
    }

    /**
     * Set position of the ship first square
     * @param position  ship position at the board
     */
    public void setBoardPosition(Position position)
    {
        this.boardPosition = position;
    }

    /**
     * @return  type of the ship
     */
    public ShipType getShipType() {
        return shipType;
    }

    /**
     * @return  information, if the ship is currently picked up
     */
    public boolean isPickedUp() {
        return isPickedUp;
    }

    /**
     * @param pickedUp  ship pickedUp status
     */
    public void setPickedUp(boolean pickedUp) {
        isPickedUp = pickedUp;
    }

    /**
     * @return  ship identifier
     */
    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ship ship = (Ship) o;
        return vertical == ship.vertical && health == ship.health && isPickedUp == ship.isPickedUp && shipType == ship.shipType && boardPosition.equals(ship.boardPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shipType, vertical, boardPosition, health, isPickedUp);
    }
}
