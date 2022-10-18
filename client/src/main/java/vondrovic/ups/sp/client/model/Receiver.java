package vondrovic.ups.sp.client.model;

import vondrovic.ups.sp.client.model.connection.ConnectionModel;

import java.io.BufferedReader;

public class Receiver extends Thread{

    ConnectionModel connectionModel;
    BufferedReader bufferedReader;
}
