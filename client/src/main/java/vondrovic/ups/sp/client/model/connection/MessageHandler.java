package vondrovic.ups.sp.client.model.connection;

import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.stage.Window;

import java.io.IOException;
import java.util.ArrayList;

import vondrovic.ups.sp.client.AlertFactory;
import vondrovic.ups.sp.client.App;
import vondrovic.ups.sp.client.SceneEnum;
import vondrovic.ups.sp.client.controller.GameController;
import vondrovic.ups.sp.client.controller.LobbyController;
import vondrovic.ups.sp.client.model.game.GameModel;
import vondrovic.ups.sp.client.model.game.GameStatus;
import vondrovic.ups.sp.client.model.game.Player;
import vondrovic.ups.sp.client.model.game.SquareStatus;

public class MessageHandler {

    // possible error messages
    private final int ERROR_INTERNAL = 1;
    private final int ERROR_FORMAT = 2;
    private final int ERROR_USERNAME_WRONG_FORMAT = 3;
    private final int ERROR_USED_USERNAME = 4;
    private final int ERROR_USER_STATE = 5;
    private final int ERROR_ROOM_FULL = 6;
    private final int ERROR_ROOM_NOT_ACCESSIBLE = 7;
    private final int ERROR_OUT_OF_PLAY_FIELD = 8;
    private final int ERROR_ALREADY_HIT = 9;
    private final int ERROR_SHIP_PLACEMENT = 10;
    private final int ERROR_SHIP_NUMBER = 11;
    private final int ERROR_SERVER_LIMIT_REACHED = 12;
    private final int ERROR_SHIPS_WRONG_LENGTH = 13;

    //private final int ERROR_WAIT_TO_LONG_FOR_OPP = 12;

    // states of client
    private final int STATE_UNLOGGED = 0;
    private final int STATE_IN_LOBBY = 1;
    private final int STATE_IN_ROOM = 2;
    private final int STATE_IN_GAME_PREPARING = 3;
    private final int STATE_IN_GAME = 4;
    private final int STATE_IN_GAME_PLAYING = 5;

    Window window;
    public int invalidMessages = 0;

    /**
     * Constructor to create instance of Message Handler
     * @param window window to use
     */
    public MessageHandler(Window window)
    {
        this.window = window;
    }

    /**
     * process given messages
     * @param line  message
     * @throws IOException
     */
    public void processMessage(String line) throws IOException
    {
        if (line.length() == 0) return;

        boolean receive_msg = false;
        String[] message = line.split(";");

        if (message[0].equalsIgnoreCase("connected"))
        {
            receive_msg = true;
            this.invalidMessages = 0;
            App.sendMessage("login_req;" + App.INSTANCE.getPlayer().getName());
            return;
        }

        if (message[0].equalsIgnoreCase("login_ok"))
        {
            receive_msg = true;
            this.invalidMessages = 0;
            if(message.length <= 2)
            {
                return;
            }

            if(App.INSTANCE.getSceneEnum() != SceneEnum.CONNECT)
            {
                return;
            }

            int state = Integer.parseInt(message[2]);

            Platform.runLater(() -> {
                App.INSTANCE.setPlayer(new Player(message[1]));
                App.INSTANCE.setOpponent(new Player("--not defined--"));


                if(state == STATE_IN_ROOM) {
                    App.INSTANCE.gameModel = new GameModel();
                    App.INSTANCE.setScene(SceneEnum.ROOM);
                } else if(state == STATE_IN_GAME || state == STATE_IN_GAME_PLAYING ||
                        state == STATE_IN_GAME_PREPARING) {

                    App.INSTANCE.gameModel = new GameModel();
                    App.INSTANCE.getGameModel().init();
                    App.INSTANCE.setScene(SceneEnum.GAME);
                    App.INSTANCE.getScene().getWindow().setWidth(App.INSTANCE.getScene().getWidth() + 0.001);
                    App.INSTANCE.getScene().getWindow().setWidth(App.INSTANCE.getScene().getHeight() + 0.001);
                    App.sendMessage("game_info_req");
                } else {
                    App.INSTANCE.setScene(SceneEnum.LOBBY);
                }
            });
        }

        if (message[0].equalsIgnoreCase("game_info_data"))
        {
            receive_msg = true;
            this.invalidMessages = 0;

            if (message.length <= 4) return;

            int state = Integer.parseInt(message[1]);
            String myBoard = message[2];
            String opponentName = message[3];
            String opponentBoard = message[4];

            GameStatus gameStatus;
            switch(state)
            {
                case STATE_IN_GAME_PREPARING:
                    gameStatus = GameStatus.PREPARING;
                    break;
                case STATE_IN_GAME_PLAYING:
                    gameStatus = GameStatus.PLAYING;
                    break;
                case STATE_IN_GAME:
                default:
                    gameStatus = GameStatus.WAITING;
                    break;
            }

            App.INSTANCE.setOpponent(new Player(opponentName));
            App.INSTANCE.getGameModel().convertStringToBoard(myBoard, App.INSTANCE.getGameModel().getMyBoard());
            App.INSTANCE.getGameModel().convertStringToBoard(opponentBoard,  App.INSTANCE.getGameModel().getEnemyBoard());

            Platform.runLater(() -> {
                App.INSTANCE.getGameModel().setGameStatus(gameStatus);
                ((GameController) App.INSTANCE.getController()).protocolAdd("Game reconnected");
                if (gameStatus == GameStatus.PREPARING)
                {
                    App.INSTANCE.getGameModel().populateMyShips();
                    ((GameController) App.INSTANCE.getController()).protocolAdd("Prepare your game board");
                }

                if (gameStatus == GameStatus.WAITING)
                    ((GameController) App.INSTANCE.getController()).protocolAdd("Please wait for your opponent turn");

                if (gameStatus == GameStatus.PLAYING)
                    ((GameController) App.INSTANCE.getController()).protocolAdd("It is your turn to fire.");

                ((GameController) App.INSTANCE.getController()).reloadOpponentName();
                ((GameController) App.INSTANCE.getController()).repaint();
            });


        }

        if(message[0].equalsIgnoreCase("login_err")) {
            this.invalidMessages = 0;
            receive_msg = true;
            if (message.length < 2)
            {
                AlertFactory.sendErrorMessageOutside("Login error", "An error occurred while logging in.");
                return;
            }

            switch(Integer.parseInt(message[1]))
            {
                case ERROR_USED_USERNAME:
                    AlertFactory.sendErrorMessageOutside("Login error", "Chosen username is already being used on server.");
                    break;
                case ERROR_FORMAT:
                    AlertFactory.sendErrorMessageOutside("Login error", "Login command was used in wrong format.");
                    break;
                case ERROR_USERNAME_WRONG_FORMAT:
                    AlertFactory.sendErrorMessageOutside("Login error", "Username contains illegal characters or has an invalid length (more than 20 characters or is empty).");
                    break;
                case ERROR_SERVER_LIMIT_REACHED:
                    AlertFactory.sendErrorMessageOutside("Login error", "Player capacity of the server has been reach.");
                    break;
                case ERROR_INTERNAL:
                default:
                    AlertFactory.sendErrorMessageOutside("Login error", "An error occurred while logging in.");
                    break;
            }

            return;
        }

        if(message[0].equalsIgnoreCase("room_create_err")) {
            this.invalidMessages = 0;
            receive_msg = true;
            if (message.length < 2)
            {
                AlertFactory.sendErrorMessageOutside("Room create error", "An error occurred while creating a room.");
                return;
            }

            if (Integer.parseInt(message[1]) == ERROR_USER_STATE)
            {
                AlertFactory.sendErrorMessageOutside("Room create error", "Client is in wrong state to be able create a room.");
            }
            else if (Integer.parseInt(message[1]) == ERROR_SERVER_LIMIT_REACHED)
            {
                AlertFactory.sendErrorMessageOutside("Room create error", "Rooms capacity of the server has been reach.");
            }
            else
            {
                AlertFactory.sendErrorMessageOutside("Room create error", "An error occurred while creating a room.");
            }
            return;
        }

        if(message[0].equalsIgnoreCase("room_list_err")) {
            this.invalidMessages = 0;
            receive_msg = true;

            AlertFactory.sendErrorMessageOutside("Room list error", "An error occurred while getting list of rooms");
            return;
        }

        if(message[0].equalsIgnoreCase("room_join_err")) {
            this.invalidMessages = 0;
            receive_msg = true;

            if (message.length < 2)
            {
                AlertFactory.sendErrorMessageOutside("Login error", "An error occurred while logging in.");
                return;
            }
            switch(Integer.parseInt(message[1]))
            {
                case ERROR_USER_STATE:
                    AlertFactory.sendErrorMessageOutside("Room join error", "Client is in wrong state to join a room.");
                    break;
                case ERROR_FORMAT:
                    AlertFactory.sendErrorMessageOutside("Room join error", "Room join failed due to message format.");
                case ERROR_ROOM_NOT_ACCESSIBLE:
                    AlertFactory.sendErrorMessageOutside("Room join error", "Room was not found or is not accessible.");
                    break;
                case ERROR_ROOM_FULL:
                    AlertFactory.sendErrorMessageOutside("Room join error", "Room is full.");
                    break;
                case ERROR_INTERNAL:
                default:
                    AlertFactory.sendErrorMessageOutside("Room join error", "An error occurred while joining to room.");
                    break;

            }
            return;
        }

        if(message[0].equalsIgnoreCase("room_list_data")) {
            receive_msg = true;
            this.invalidMessages = 0;
            if(message.length % 2 != 1) {
                AlertFactory.sendErrorMessageOutside("Server data error", "An error occurred in the server message");

                return;
            }


            if(!(App.INSTANCE.getController() instanceof LobbyController)) {
                return;
            }
            LobbyController controller = (LobbyController) App.INSTANCE.getController();

            Platform.runLater(() -> {

                ArrayList<Room> rooms = new ArrayList<Room>();
                for(int i = 2; i < message.length; i = i + 2) {
                    rooms.add(new Room(message[i], Integer.parseInt(message[i - 1])));
                }

                Platform.runLater(() -> {
                    controller.loadList(rooms);
                });
                App.INSTANCE.getStage().getScene().setCursor(Cursor.DEFAULT);

            });

            return;

        }

        if(message[0].equalsIgnoreCase("room_create_ok")) {
            this.invalidMessages = 0;
            receive_msg = true;

            App.INSTANCE.gameModel = new GameModel();

            Platform.runLater(() -> {
                App.INSTANCE.setScene(SceneEnum.ROOM);
            });

            return;
        }

        if(message[0].equalsIgnoreCase("room_join_ok")) {
            this.invalidMessages = 0;
            receive_msg = true;

            if(message.length < 2) {

                return;
            }

            App.INSTANCE.gameModel = new GameModel();
            App.INSTANCE.setOpponent(new Player(message[1]));

            Platform.runLater(() -> {
                App.INSTANCE.setScene(SceneEnum.ROOM);
            });

            return;

        }

        if(message[0].equalsIgnoreCase("room_join_opp")) {
            this.invalidMessages = 0;
            receive_msg = true;

            if(message.length < 2) {

                return;
            }

            if(App.INSTANCE.getSceneEnum() != SceneEnum.GAME && App.INSTANCE.getSceneEnum() != SceneEnum.ROOM) {

                return;
            }

            App.INSTANCE.setOpponent(new Player(message[1]));

            if(App.INSTANCE.getSceneEnum() == SceneEnum.GAME) {
                ((GameController) App.INSTANCE.getController()).protocolAdd("Teammate " + App.INSTANCE.getOpponent().getName() + " joined the game.");
            }

            return;
        }

        if(message[0].equalsIgnoreCase("room_leave_ok")) {
            this.invalidMessages = 0;
            receive_msg = true;

            App.INSTANCE.gameModel = null;

            Platform.runLater(() -> {
                App.INSTANCE.setScene(SceneEnum.LOBBY);
            });

            return;
        }

        if(message[0].equalsIgnoreCase("room_leave_err")) {
            this.invalidMessages = 0;
            receive_msg = true;
            AlertFactory.sendErrorMessageOutside("Room leave error", "An error occurred while trying to leave a room.");
        }

        if(message[0].equalsIgnoreCase("logout_err")) {
            this.invalidMessages = 0;
            receive_msg = true;
            AlertFactory.sendErrorMessageOutside("Logout error", "An error occurred. Client is already disconnected.");
            return;
        }

        if(message[0].equalsIgnoreCase("room_leave_opp")) {
            this.invalidMessages = 0;
            receive_msg = true;
            if(App.INSTANCE.getSceneEnum() == SceneEnum.GAME) {
                ((GameController) App.INSTANCE.getController()).protocolAdd("Teammate " + App.INSTANCE.getOpponent().getName() + " left the game.");
            }
            return;
        }

        if(message[0].equalsIgnoreCase("game_conn")) {
            this.invalidMessages = 0;
            receive_msg = true;
            if(App.INSTANCE.getSceneEnum() != SceneEnum.LOBBY && App.INSTANCE.getSceneEnum() != SceneEnum.ROOM) {

                return;
            }

            App.INSTANCE.getGameModel().init();
            App.INSTANCE.getGameModel().setGameStatus(GameStatus.PREPARING);
            Platform.runLater(() -> {

                App.INSTANCE.setScene(SceneEnum.GAME);
                App.INSTANCE.getScene().getWindow().setWidth(App.INSTANCE.getScene().getWidth() + 0.001);
                App.INSTANCE.getScene().getWindow().setWidth(App.INSTANCE.getScene().getHeight() + 0.001);
            });

            return;
        }

        if (message[0].equalsIgnoreCase("game_prepared_ok"))
        {
            this.invalidMessages = 0;
            receive_msg = true;

            ((GameController) App.INSTANCE.getController()).protocolAdd("Game prepared ok");
            App.INSTANCE.getGameModel().setGameStatus(GameStatus.WAITING);
            ((GameController) App.INSTANCE.getController()).disablePrepareButton();
            return;
        }

        if (message[0].equalsIgnoreCase("game_prepared_err"))
        {
            this.invalidMessages = 0;
            receive_msg = true;

            if (message.length < 2)
            {
                AlertFactory.sendErrorMessageOutside("Game prepare error", "An error occurred while preparing a game.");
                return;
            }
            switch(Integer.parseInt(message[1]))
            {
                case ERROR_USER_STATE:
                    AlertFactory.sendErrorMessageOutside("Game prepare error", "Client is in wrong state to prepare a game.");
                    ((GameController) App.INSTANCE.getController()).protocolAdd("The server returned game preparation as invalid");
                    break;
                case ERROR_FORMAT:
                    AlertFactory.sendErrorMessageOutside("Game prepare error", "Game preparation failed due to message format.");
                    ((GameController) App.INSTANCE.getController()).protocolAdd("The server returned game preparation as invalid");
                case ERROR_SHIP_NUMBER:
                    AlertFactory.sendErrorMessageOutside("Game prepare error", "Given game board contains invalid ship number.");
                    ((GameController) App.INSTANCE.getController()).protocolAdd("The server returned game preparation as invalid. It contained invalid ship number.");
                    break;
                case ERROR_SHIP_PLACEMENT:
                    AlertFactory.sendErrorMessageOutside("Game prepare error", "Two ships cannot be placed directly next to each other.");
                    ((GameController) App.INSTANCE.getController()).protocolAdd("The server returned game preparation as invalid. Two ships cannot be place directly next to each other.");
                    break;
                case ERROR_SHIPS_WRONG_LENGTH:
                    AlertFactory.sendErrorMessageOutside("Game prepare error", "Wrong length of the ship");
                    ((GameController) App.INSTANCE.getController()).protocolAdd("The server returned game preparation as invalid. Lengths of the ships is not valid.");
                    break;
                case ERROR_INTERNAL:
                default:
                    ((GameController) App.INSTANCE.getController()).protocolAdd("The server returned game preparation as invalid");
                    AlertFactory.sendErrorMessageOutside("Game prepare error", "An error occurred while preparing a game.");
                    break;
            }
            App.INSTANCE.getGameModel().setGameStatus(GameStatus.PREPARING);

            Platform.runLater(() -> {
                App.INSTANCE.setScene(SceneEnum.GAME);
                App.INSTANCE.getScene().getWindow().setWidth(App.INSTANCE.getScene().getWidth() + 0.001);
                App.INSTANCE.getScene().getWindow().setWidth(App.INSTANCE.getScene().getHeight() + 0.001);
            });
            return;
        }


        if (message[0].equalsIgnoreCase("game_play"))
        {
            this.invalidMessages = 0;
            App.INSTANCE.gameModel.setGameStatus(GameStatus.PLAYING);
            receive_msg = true;

            Platform.runLater(() -> {
                ((GameController) App.INSTANCE.getController()).protocolAdd("Your turn to fire");
                ((GameController) App.INSTANCE.getController()).repaint();
            });

            return;
        }


        if (message[0].equalsIgnoreCase("game_fire_ok"))
        {
            this.invalidMessages = 0;
            receive_msg = true;

            if (message.length < 4) return;

            int x = Integer.parseInt(message[1]) + 1;
            int y = Integer.parseInt(message[2]) + 1;
            char status = message[3].charAt(0);

            App.INSTANCE.gameModel.setGameStatus(GameStatus.PLAYING);
            SquareStatus squareStatus;
            switch (status)
            {
                case 'H':
                    squareStatus = SquareStatus.HIT;
                    break;
                case 'M':
                default:
                    squareStatus = SquareStatus.MISSED;
                    break;
            }
            App.INSTANCE.gameModel.hitEnemy(x, y, squareStatus);

            // is square was hit check if ship is destroyed
            if (squareStatus == SquareStatus.HIT && Integer.parseInt(message[4]) == 1)
            {
                App.INSTANCE.getGameModel().markDestroyedShip(x, y, App.INSTANCE.getGameModel().getEnemyBoard());
            }

            App.INSTANCE.getGameModel().setGameStatus(GameStatus.WAITING);

            Platform.runLater(() -> {
                if (squareStatus == SquareStatus.HIT && Integer.parseInt(message[4]) == 1)
                {
                    ((GameController) App.INSTANCE.getController()).protocolAdd("You hit ["+ x +"]["+ y +"] status: ship destroyed");
                }
                else
                {
                    ((GameController) App.INSTANCE.getController()).protocolAdd("You hit ["+ x +"]["+ y +"], status: " + status);
                }
                ((GameController) App.INSTANCE.getController()).repaint();
            });
            return;
        }

        if (message[0].equalsIgnoreCase("game_fire_err"))
        {
            invalidMessages = 0;
            receive_msg = true;
            if (message.length < 2)
            {
                AlertFactory.sendErrorMessageOutside("Game fire error", "An error occurred while firing.");
                return;
            }

            switch(Integer.parseInt(message[1])) {
                case ERROR_FORMAT:
                    AlertFactory.sendErrorMessageOutside("Game fire error", "Game fire request failed due to wrong message format.");
                case ERROR_OUT_OF_PLAY_FIELD:
                    AlertFactory.sendErrorMessageOutside("Game fire error", "Given position is out of board restrictions.");
                    break;
                case ERROR_ALREADY_HIT:
                    AlertFactory.sendErrorMessageOutside("Game fire error", "Given position is already hit.");
                    break;
                case ERROR_INTERNAL:
                default:
                    AlertFactory.sendErrorMessageOutside("Game fire error", "An error occurred while firing.");
                    break;
            }

            App.INSTANCE.getGameModel().setGameStatus(GameStatus.PLAYING);

            Platform.runLater(() -> {
                ((GameController) App.INSTANCE.getController()).protocolAdd("Fire request failed. Try fire again");
            });
        }

        if (message[0].equalsIgnoreCase("game_opp_fire"))
        {
            this.invalidMessages = 0;
            receive_msg = true;

            if (message.length < 4) return;

            int x = Integer.parseInt(message[1]) + 1;
            int y = Integer.parseInt(message[2]) + 1;
            char status = message[3].charAt(0);

            App.INSTANCE.gameModel.setGameStatus(GameStatus.PLAYING);
            SquareStatus squareStatus;
            switch (status)
            {
                case 'H':
                    squareStatus = SquareStatus.HIT;
                    break;
                case 'M':
                default:
                    squareStatus = SquareStatus.MISSED;
                    break;
            }
            App.INSTANCE.gameModel.beingHit(x, y, squareStatus);

            // is square was hit check if ship is destroyed
            if (squareStatus == SquareStatus.HIT && Integer.parseInt(message[4]) == 1)
            {
                App.INSTANCE.getGameModel().markDestroyedShip(x, y, App.INSTANCE.getGameModel().getMyBoard());
            }
            App.INSTANCE.getGameModel().setGameStatus(GameStatus.PLAYING);

            Platform.runLater(() -> {
                if (squareStatus == SquareStatus.HIT && Integer.parseInt(message[4]) == 1)
                {
                    ((GameController) App.INSTANCE.getController()).protocolAdd("Opponent hit ["+ x +"]["+ y +"] status: ship destroyed");
                }
                else
                {
                    ((GameController) App.INSTANCE.getController()).protocolAdd("Opponent hit ["+ x +"]["+ y +"] status: " + status);
                }

                ((GameController) App.INSTANCE.getController()).repaint();
            });
            return;
        }

        if (message[0].equalsIgnoreCase("game_end"))
        {
            this.invalidMessages = 0;
            receive_msg = true;

            if (message.length < 2) return;

            if(App.INSTANCE.getSceneEnum() != SceneEnum.GAME) {
                return;
            }

            App.INSTANCE.getGameModel().setWinner(message[1]);

            Platform.runLater(() -> {
                App.INSTANCE.setScene(SceneEnum.GAME_RESULT);
            });

            return;
        }

        if(message[0].equalsIgnoreCase("logout_ok")) {
            this.invalidMessages = 0;
            receive_msg = true;

            return;
        }

        if (!receive_msg)
        {
            AlertFactory.sendErrorMessageOutside("Message error", "Received invalid message. Probably connected to the wrong server.");
            App.INSTANCE.disconnect();
            return;
        }

        this.invalidMessages++;
        if(this.invalidMessages > App.MAX_INVALID_MESSAGES)
        {
            AlertFactory.sendErrorMessageOutside("Message error", "Too many invalid messages");
            App.INSTANCE.disconnect();
        };
    }
}
