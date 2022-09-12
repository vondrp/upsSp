package vondrovic.ups.sp.client.model;

public class Ship {

    public ShipType shipType;

    public boolean vertical = true;


    private int health;

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
}
