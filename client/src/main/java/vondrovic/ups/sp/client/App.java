package vondrovic.ups.sp.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.fxml.FXMLLoader;
import vondrovic.ups.sp.client.controller.AbstractController;
import vondrovic.ups.sp.client.model.connection.ConnectionModel;
import vondrovic.ups.sp.client.model.connection.MessageHandler;
import vondrovic.ups.sp.client.model.connection.Receiver;
import vondrovic.ups.sp.client.model.game.GameModel;
import vondrovic.ups.sp.client.model.game.Player;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;

/**
 * Main application class
 */
public class App extends Application {

    /**
     * Instance of App
     */
    public static App INSTANCE;

    private Player player;

    private Player opponent;

    private Stage stage;
    private FXMLLoader fxmlLoader;

    private SceneEnum actualScene;
    private SceneEnum sceneEnum;

    private ConnectionModel connectionModel;
    private Receiver reciever;
    private MessageHandler messageHandler;
    public String lastEnteredUsername;
    public String lastEnteredHostname;
    public String lastEnteredPort;

    public static final int MAX_INVALID_MESSAGES = 5;

    public GameModel gameModel;
    /**
     * Initialization of program
     */
    public void init()
    {
        Locale.setDefault(new Locale("cs", "CZ"));
    }
    /**
     * Method that starts the application
     * @param stage initial stage
     * @throws IOException
     */
    @Override
    public void start(Stage stage) throws IOException {
        INSTANCE = this;
        this.stage = stage;

        this.messageHandler = new MessageHandler(stage);
        stage.setTitle("Ships");

        //"bundle.getString("title")"
        this.setScene(SceneEnum.CONNECT);
    }

    /**
     * Used to set scene of the application
     * @param scene
     */
    public void setScene(SceneEnum scene)
    {
        URL url = getClass().getResource(scene.path);

        Parent root = null;
        SceneEnum backScene = this.actualScene;

        this.actualScene = scene;
        this.fxmlLoader = new FXMLLoader(url);

        try
        {
            root = this.fxmlLoader.load();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            this.actualScene = backScene;
            return;
        }

        Scene s = new Scene(root);
        stage.setScene(s);

        this.sceneEnum = scene;
        stage.sizeToScene();
        stage.show();
        if(scene == SceneEnum.GAME) {
            AbstractController controller = (AbstractController) this.getController();
            controller.initialize();
        }
    }
    
    public Scene getScene()
    {
        return stage.getScene();
    }

    /**
     * Sets scene from outside of JavaFX thread
     * @param scene scene type
     */
    public void setSceneOutside(SceneEnum scene) {
        Platform.runLater(() -> {
            setScene(scene);
        });
    }

    /**
     * Connect to the server running on given ip address and port
     * @param address   ip address of the server
     * @param port      port number
     * @throws IOException
     */
    public void connect(String address, int port) throws IOException {
        this.connectionModel = new ConnectionModel(address, port);
        this.reciever = new Receiver(this.connectionModel, this.messageHandler);
        this.reciever.start();
    }


    /**
     * Method that provides disconnection from the server
     */
    public void disconnect() {
        this.reciever.setRunning(false);
        try {
            this.connectionModel.close();
        } catch (IOException e) {
            this.connectionModel = null;
        }
        this.connectionModel = null;

        this.setSceneOutside(SceneEnum.CONNECT);
    }

    /**
     * Entry point of the application
     * @param args  arguments from console (not used)
     */
    public static void main(String[] args) {
        launch();
    }

    /**
     * Getter for controller of actual scene
     * @return controller of actual scene
     */
    public AbstractController getController() {
        return this.fxmlLoader.getController();
    }


    public ConnectionModel getConnectionModel() {
        return connectionModel;
    }

    public void setConnectionModel(ConnectionModel connectionModel) {
        this.connectionModel = connectionModel;
    }

    /**
     * Sends a message to a server
     * @param message
     */
    public static void sendMessage(String message) {
        if(App.INSTANCE.connectionModel != null) {
            App.INSTANCE.connectionModel.sendMessage(message);
        }
    }

    /**
     * Method to close the application
     * @param event
     */
    @FXML
    public void exitApplication(ActionEvent event)
    {
        Platform.exit();
    }


    /**
     * Method to stop the program
     */
    @Override
    public void stop(){
        System.exit(0);
    }

    public SceneEnum getSceneEnum() {
        return sceneEnum;
    }

    public Stage getStage() {
        return stage;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getOpponent() {
        return opponent;
    }

    public void setOpponent(Player opponent) {
        this.opponent = opponent;
    }

    public GameModel getGameModel() {
        return gameModel;
    }

}
