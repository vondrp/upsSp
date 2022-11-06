package vondrovic.ups.sp.client;

/**
 * Enumerate to scene views files
 */
public enum SceneEnum {

    CONNECT("/connection.fxml"),
    LOGIN("/login.fxml"),
    LOBBY("/lobby.fxml"),
    ROOM("/room.fxml"),
    GAME("/game.fxml"),
    GAME_RESULT("/result.fxml");

    public String path;

    /**
     * Constructor to enumerate
     * @param path
     */
    SceneEnum(String path) {
        this.path = path;
    }
}
