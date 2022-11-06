package vondrovic.ups.sp.client.model.game;

/**
 * Class Player is messanger representing player
 */
public class Player {

    /**
     * player name
     */
    private String name;

    /**
     * Create player
     * @param name  player name
     */
    public Player(String name)
    {
        this.name = name;
    }

    /**
     * Getter for player name
     * @return player name
     */
    public String getName() {
        return this.name;
    }
}
