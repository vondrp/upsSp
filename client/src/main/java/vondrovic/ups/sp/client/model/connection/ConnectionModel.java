package vondrovic.ups.sp.client.model.connection;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * Connection model containing socket, printwriter and bufferedreader
 */
public class ConnectionModel {

    private Socket socket;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;
    /**
     * Constructor that creates necessary structures and creates connection
     * @param address address to connect
     * @param port port of service to connect
     * @throws IOException
     */
    public ConnectionModel(String address, int port) throws IOException {
        socket = new Socket(address, port);
        socket.setSoTimeout(5000); // waiting max 5s for first message
        printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.bufferedReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }

    /**
     * Method to gen information about connection is open or close
     * @return is this connection opened
     */
    public boolean isConnected() {
        return socket.isConnected();
    }

    /**
     * Method to close connection
     * @throws IOException
     */
    public void close() throws IOException {
        socket.close();
    }

    /**
     * Method to get reader to read data into stream
     * @return reader to data input
     */
    public BufferedReader getBufferedReader() {
        return this.bufferedReader;
    }

    /**
     * Method to get a writer to write data into stream
     * @return writer to data output
     */
    public PrintWriter getWriter() {
        return this.printWriter;
    }

    /**
     * Method to send message
     * @param message message
     */
    public void sendMessage(String message) {
        Stats.INSTANCE.sentMessages++;
        Stats.INSTANCE.sentBytes += message.length() + 1;
        this.printWriter.println(message);
        this.printWriter.flush();
    }

    /**
     * Set socket timeout - how long max waiting for message from server
     * - with value 0 -> not waiting
     * @param time          time to wait
     * @throws SocketException
     */
    public void setSocketTimeout(int time) throws SocketException {
        this.socket.setSoTimeout(time);
    }
}