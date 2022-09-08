package vondrovic.ups.sp.client;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Factory class to send easily alerts
 */
public class AlertFactory {

    /**
     * Method to send warning message from outside of JavaFX thread
     * @param title title
     * @param body content
     */
    public static void sendWarningMessageOutside(String title, String body) {
        Platform.runLater(() -> {
            sendWarningMessage(title, body);
        });
    }

    /**
     * Method to send warning message
     * @param title  title
     * @param body content
     */
    public static void sendWarningMessage(String title, String body) {
        sendMessage(Alert.AlertType.WARNING, title, body);
    }

    /**
     * Method to send error message from outside of JavaFX thread
     * @param title title
     * @param body content
     */
    public static void sendErrorMessageOutside(String title, String body) {
        Platform.runLater(() -> {
            sendErrorMessage(title, body);
        });
    }

    /**
     * Method to send error message
     * @param title  title
     * @param body content
     */
    public static void sendErrorMessage(String title, String body) {
        sendMessage(Alert.AlertType.ERROR, title, body);
    }

    /**
     * Method to send information message from outside of JavaFX thread
     * @param title title
     * @param body content
     */
    public static void sendMessageOutside(Alert.AlertType alertType, String title, String body) {
        Platform.runLater(() -> {
            sendMessage(alertType, title, body);
        });
    }

    /**
     * Method to send info message
     * @param title  title
     * @param body content
     */
    public static void sendMessage(Alert.AlertType alertType, String title, String body) {
        Alert a = new Alert(alertType);
        a.setTitle(title);
        a.setContentText(body);
        a.show();
    }

    /**
     * Method to send dialog with YES and CANCEL button
     * @param title title
     * @param body content
     * @return result of the alert (clicked button)
     */
    public static boolean sendConfirmation(String title, String body) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, body, ButtonType.YES, ButtonType.CANCEL);
        a.setTitle(title);
        a.showAndWait();
        if(a.getResult() == ButtonType.YES) {
            return true;
        } else {
            return false;
        }
    }
}
