package vondrovic.ups.sp.client.model.game;

public class Player {

    private String name;
    private int state;

    public Player(String name)
    {
        this.name = name;
        this.state = 1;
    }

    /**
     * Constructor for new instance of player
     * @param name player name
     * @param state player state
     */
    public Player(String name, int state) {
        this.name = name;
        this.state = state;
    }

    /**
     * Getter for player name
     * @return player name
     */
    public String getName() {
        return this.name;
    }
}
