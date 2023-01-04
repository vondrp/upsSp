package vondrovic.ups.sp.client.model.connection;

import javafx.application.Platform;
import vondrovic.ups.sp.client.AlertFactory;
import vondrovic.ups.sp.client.App;
import vondrovic.ups.sp.client.SceneEnum;
import vondrovic.ups.sp.client.model.connection.ConnectionModel;
import vondrovic.ups.sp.client.model.connection.MessageHandler;
import vondrovic.ups.sp.client.model.connection.Stats;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Class that receiving incoming messages
 */
public class Receiver extends Thread {

    ConnectionModel connectionModel;
    BufferedReader bufferedReader;
    MessageHandler messageHandler;
    boolean running = false;

    /**
     * Contructor of reciever
     * @param connectionModel connection
     * @throws IOException
     */
    public Receiver(ConnectionModel connectionModel, MessageHandler messageHandler) throws IOException {
        this.connectionModel = connectionModel;
        this.messageHandler = messageHandler;

        this.bufferedReader = connectionModel.getBufferedReader();
    }

    /**
     * Setter of the parameter running that control "endless" look in run method
     * @param running
     */
    public void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * Method to listen incoming messages
     * Thread runs endless loop
     */
    @Override
    public void run() {
        this.setRunning(true);
        String line = "";

        while(this.running) {
            try {
                line = bufferedReader.readLine();

                // after first message disable socket timeout
                this.connectionModel.setSocketTimeout(0);
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    Thread.sleep(20);
                    Platform.runLater(() -> {
                        try {
                            App.INSTANCE.getConnectionModel().close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        App.INSTANCE.setSceneOutside(SceneEnum.CONNECT);
                        AlertFactory.sendErrorMessageOutside("Connection Loss", "Connection to the server was broken.");
                        App.INSTANCE.disconnect();
                    });
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                continue;
            }

            if(line == null) {
                App.INSTANCE.disconnect();
                AlertFactory.sendErrorMessageOutside("Connection Lost", "Connection to the server was broken.");
                return;
            }


            if(line.length() != 0) {

                Stats.INSTANCE.receivedMessages++;
                Stats.INSTANCE.receivedBytes += line.length() + 1;

                try {
                    messageHandler.processMessage(line);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
