package vondrovic.ups.sp.client.model.game;

/**
 * Enum ShipType contains type of ships with their length
 */
public enum ShipType
{
    CARRIER(4),
    CRUISER(3),
    BATTLESHIP(4),
    DESTROYER(2),
    SUBMARINE(1);

    private final Integer length;

    ShipType(Integer length)
    {
        this.length = length;
    }

    public Integer getLength() {
        return length;
    }
}
