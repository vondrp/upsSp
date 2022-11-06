package vondrovic.ups.sp.client.model.connection;

/**
 *  Class to save statistics
 */
public class Stats {

    public static final Stats INSTANCE = new Stats();

    public int sentMessages = 0;
    public int receivedMessages = 0;
    public int sentBytes = 0;
    public int receivedBytes = 0;
}
