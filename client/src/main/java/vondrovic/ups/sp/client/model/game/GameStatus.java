package vondrovic.ups.sp.client.model.game;

public enum GameStatus {

    PREPARING(0),
    PLAYING(1),
    WAITING(2);

    private final Integer id;

    GameStatus(Integer id)
    {
        this.id = id;
    }

    public Integer getId() {
        return this.id;
    }
}
