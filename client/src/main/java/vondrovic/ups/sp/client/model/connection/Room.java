package vondrovic.ups.sp.client.model.connection;

import vondrovic.ups.sp.client.model.game.Player;

/**
 * Class to represent room data
 */
public class Room {
    private String name;
    private int id;
    private Player player1;
    private Player player2;

    /**
     * Construtor to create new room instance
     * @param name name of the first connected player
     * @param id id of the room
     */
    public Room(String name, int id) {
        this.name = name;
        this.id = id;
    }

    /**
     * Getter for name
     * @return
     */
    public String getName() {
        return this.name;
    }

    /**
     * Getter for room identifier
     * @return
     */
    public int getId() {
        return this.id;
    }

    /**
     * To string conversion to pretty print
     * @return
     */
    @Override
    public String toString() {
        return "#" + this.id + " - " + this.name;
    }
}
