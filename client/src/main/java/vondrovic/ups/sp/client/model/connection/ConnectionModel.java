package vondrovic.ups.sp.client.model.connection;

import java.io.*;
import java.net.Socket;

public class ConnectionModel {

    private Socket socket;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    /**
     * Constructor that creates necessary structures and creates connection
     * @param address address to connect
     * @param port port of service to connect
     * @throws IOException
     */
    public ConnectionModel(String address, int port) throws IOException {
        socket = new Socket(address, port);
        printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.bufferedReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
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
        //Stats.INSTANCE.sentMessages++;
        //Stats.INSTANCE.sentBytes += message.length() + 1;
        this.printWriter.println(message);
        this.printWriter.flush();
    }
}