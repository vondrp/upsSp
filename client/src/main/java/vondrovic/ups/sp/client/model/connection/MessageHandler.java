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

    // states of client
    private final int STATE_UNLOGGED = 0;
    private final int STATE_IN_LOBBY = 1;
    private final int STATE_IN_ROOM = 2;
    private final int STATE_IN_GAME = 3;
    private final int STATE_IN_GAME_PLAYING = 4;


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

    public void processMessage(String line) throws IOException
    {

        System.out.println("Message: "+line);
        if (line.length() == 0) return;

        System.out.println(line);

        String[] message = line.split(";");

        if (message[0].equalsIgnoreCase("connected"))
        {
            this.invalidMessages = 0;
            App.sendMessage("login_req;" + App.INSTANCE.player.getName());
            return;
        }

        if (message[0].equalsIgnoreCase("login_ok"))
        {
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
                App.INSTANCE.player = new Player(message[1], state);
                if(state == STATE_IN_ROOM) {
                    App.INSTANCE.gameModel = new GameModel();
                    App.INSTANCE.setScene(SceneEnum.ROOM);
                } else if(state == STATE_IN_GAME || state == STATE_IN_GAME_PLAYING) {
                    // TODO nastaveni game model
                    App.INSTANCE.gameModel = new GameModel();
                    App.INSTANCE.setScene(SceneEnum.GAME);
                    App.sendMessage("game_info_req");
                } else {
                    App.INSTANCE.setScene(SceneEnum.LOBBY);
                }
            });
        }

        if(message[0].equalsIgnoreCase("login_err")) {
            this.invalidMessages = 0;
            App.INSTANCE.disconnect();
            AlertFactory.sendErrorMessageOutside("Login error", "An error occurred while logging in.");

            return;
        }

        if(message[0].equalsIgnoreCase("room_list_data")) {

            this.invalidMessages = 0;
            if(message.length % 2 != 1) {
                AlertFactory.sendErrorMessageOutside("Server data error", "An error occurred in the server message");

                return;
            }

            if(message.length == 1) {
                App.INSTANCE.getStage().getScene().setCursor(Cursor.DEFAULT);

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

            App.INSTANCE.gameModel = new GameModel();

            Platform.runLater(() -> {
                App.INSTANCE.setScene(SceneEnum.ROOM);
            });

            return;
        }

        if(message[0].equalsIgnoreCase("room_join_ok")) {
            this.invalidMessages = 0;
            if(message.length < 2) {

                return;
            }

            App.INSTANCE.gameModel = new GameModel();
            App.INSTANCE.getGameModel().setOpponentName(message[1]);

            Platform.runLater(() -> {
                App.INSTANCE.setScene(SceneEnum.ROOM);
            });

            return;

        }

        if(message[0].equalsIgnoreCase("room_join_opp")) {
            this.invalidMessages = 0;
            if(message.length < 2) {

                return;
            }

            if(App.INSTANCE.getSceneEnum() != SceneEnum.GAME && App.INSTANCE.getSceneEnum() != SceneEnum.ROOM) {

                return;
            }

            App.INSTANCE.getGameModel().setOpponentName(message[1]);
            if(App.INSTANCE.getSceneEnum() == SceneEnum.GAME) {
                ((GameController) App.INSTANCE.getController()).protocolAdd("Teammate " + App.INSTANCE.getGameModel().getOpponentName() + " joined the game.");
            }

            return;
        }

        if(message[0].equalsIgnoreCase("room_leave_ok")) {
            this.invalidMessages = 0;
            App.INSTANCE.gameModel = null;

            Platform.runLater(() -> {
                App.INSTANCE.setScene(SceneEnum.LOBBY);
            });

            return;
        }

        if(message[0].equalsIgnoreCase("logout_err")) {
            this.invalidMessages = 0;

            return;
        }

        if(message[0].equalsIgnoreCase("room_leave_opp")) {
            this.invalidMessages = 0;
            if(App.INSTANCE.getSceneEnum() == SceneEnum.GAME) {
                ((GameController) App.INSTANCE.getController()).protocolAdd("Teammate " + App.INSTANCE.getGameModel().getOpponentName() + " left the game.");
            }
            return;
        }

        if(message[0].equalsIgnoreCase("game_conn")) {
            this.invalidMessages = 0;

            if(App.INSTANCE.getSceneEnum() != SceneEnum.LOBBY && App.INSTANCE.getSceneEnum() != SceneEnum.ROOM) {

                return;
            }

            App.INSTANCE.getGameModel().init();
            App.INSTANCE.getGameModel().setGameStatus(GameStatus.PREPARING);
            Platform.runLater(() -> {
                App.INSTANCE.setScene(SceneEnum.GAME);
            });

            return;
        }

        if (message[0].equalsIgnoreCase("game_prepared_ok"))
        {
            System.out.println("Jsem v game prepared ok");
            this.invalidMessages = 0;

            ((GameController) App.INSTANCE.getController()).protocolAdd("Game prepared ok");
            App.INSTANCE.getGameModel().setGameStatus(GameStatus.WAITING);

            return;
        }

        if (message[0].equalsIgnoreCase("game_prepared_err"))
        {
            AlertFactory.sendErrorMessageOutside("Game prepare error", "An error occurred while preparing a game. Try again.");
            ((GameController) App.INSTANCE.getController()).protocolAdd("The server returned game preparation as invalid");
            App.INSTANCE.getGameModel().setGameStatus(GameStatus.PREPARING);

            return;
        }


        if (message[0].equalsIgnoreCase("game_play"))
        {
            this.invalidMessages = 0;
            this.invalidMessages = 0;
            App.INSTANCE.gameModel.setGameStatus(GameStatus.PLAYING);

            Platform.runLater(() -> {
                ((GameController) App.INSTANCE.getController()).protocolAdd("Your turn to fire");
                ((GameController) App.INSTANCE.getController()).repaint();
            });

            return;
        }


        if (message[0].equalsIgnoreCase("game_fire_ok"))
        {
            this.invalidMessages = 0;

            if (message.length < 3) return;

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
            App.INSTANCE.getGameModel().setGameStatus(GameStatus.WAITING);

            Platform.runLater(() -> {
                ((GameController) App.INSTANCE.getController()).protocolAdd("Opponent hit x = " + x + " y = " + y + " status: " + status);
                ((GameController) App.INSTANCE.getController()).repaint();
            });
            return;
        }

        if (message[0].equalsIgnoreCase("game_fire_err"))
        {
            App.INSTANCE.getGameModel().setGameStatus(GameStatus.PLAYING);

            Platform.runLater(() -> {
                ((GameController) App.INSTANCE.getController()).protocolAdd("Fire request failed. Try fire again");
            });

        }

        if (message[0].equalsIgnoreCase("game_opp_fire"))
        {
            this.invalidMessages = 0;
            System.out.println("V game opp fire");
            if (message.length < 3) return;

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
            App.INSTANCE.getGameModel().setGameStatus(GameStatus.PLAYING);

            Platform.runLater(() -> {
                ((GameController) App.INSTANCE.getController()).protocolAdd("Opponent hit x = " + x + " y = " + y + " status: " + status);
                ((GameController) App.INSTANCE.getController()).repaint();
            });
            return;
        }


        this.invalidMessages++;
        if(this.invalidMessages > App.MAX_INVALID_MESSAGES) {
            App.INSTANCE.disconnect();
        };
    }
}
