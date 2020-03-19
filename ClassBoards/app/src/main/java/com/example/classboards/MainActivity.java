package com.example.classboards;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private static String HOST_NAME = "attu6.cs.washington.edu";
    private static int PORT_NUM = 12345;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Socket server = setup();
        if (server == null) {
            Log.e("error", "null server");
        }


    }

    public Socket setup() {
        // Needs address and TCP port

        // We already know the IP address of ATTU6 but we can use another way
        //String ip_address = "128.208.1.135";
        try {
            InetAddress ip_address = InetAddress.getByName(HOST_NAME);
            Socket server = new Socket(ip_address, PORT_NUM);
            return server;
        } catch (Exception E) {
            // Do something
            E.printStackTrace();
            return null;
        }
    }

    public void ReceivingThread() {

        while (true) {


        }


    }

}
