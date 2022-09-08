package vondrovic.ups.sp.client;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.fxml.FXMLLoader;
import vondrovic.ups.sp.client.controller.AbstractController;

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
    private Stage stage;
    private FXMLLoader fxmlLoader;
    public SceneEnum actualScene;
    private SceneEnum sceneEnum;

    private I18Support bundle;

    /**
     * Initialization of program
     */
    public void init()
    {
        Locale.setDefault(new Locale("cs", "CZ"));
        bundle = new I18Support();
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
        stage.setTitle(bundle.getString("title"));
        this.setScene(SceneEnum.MAIN_MENU);
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

        this.fxmlLoader = new FXMLLoader(url, this.bundle.getResourceBundle());

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
        stage.show();
        /*if(scene == SceneEnum.GAME) {
            AbstractController controller = (AbstractController) this.getController();
            controller.initialize();
        }*/
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
}
