package vondrovic.ups.sp.client.model.game;

/**
 * Enum of Game possible status
 */
public enum GameStatus {

    PREPARING(0),
    PLAYING(1),
    WAITING(2);

    private final Integer id;

    /**
     * Initialized game status
     * @param id    identifier
     */
    GameStatus(Integer id)
    {
        this.id = id;
    }

    /**
     * @return  game status identifier
     */
    public Integer getId() {
        return this.id;
    }
}
