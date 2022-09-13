package vondrovic.ups.sp.client.model;

import java.util.Objects;

public class Ship {

    public ShipType shipType;

    public boolean vertical = true;

    private Position boardPosition;

    private int health;

    private boolean isPickedUp = false;

    public Ship(ShipType shipType, boolean vertical)
    {
        this.shipType = shipType;
        this.vertical = vertical;

        this.health = this.shipType.getLength();
    }

    public Ship(ShipType shipType)
    {
        this(shipType, false);
    }

    public boolean isAlive()
    {
        return health > 0;
    }

    public boolean isVertical() {
        return vertical;
    }

    public void setVertical(boolean vertical) {
        this.vertical = vertical;
    }

    public void hit()
    {
        health--;
    }

    public Position getBoardPosition()
    {
        return this.boardPosition;
    }

    public void setBoardPosition(Position position)
    {
        this.boardPosition = position;
    }

    public ShipType getShipType() {
        return shipType;
    }

    public boolean isPickedUp() {
        return isPickedUp;
    }

    public void setPickedUp(boolean pickedUp) {
        isPickedUp = pickedUp;
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
