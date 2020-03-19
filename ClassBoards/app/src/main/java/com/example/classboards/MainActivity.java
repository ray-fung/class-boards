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
    private static Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.socket = setup();
        if (server == null) {
            Log.e("error", "null server");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                receivingThread();
            }
        }).start();

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

    public void onClassSelect() {
        Button courseButton = (Button) findViewById(r.id.course_textbox);
        String course = courseButton.getText().toString();
        String msg = "update: " + course;
        OutputStream out = new DataOutputStream(new BufferedOutputStream(this.socket.getOutputStream()));
        out.write(msg);
    }

    public void onMessageSend() {
        Button messageButton = (Button) findViewById(r.id.message_textbox);
        String msg = messageButton.getText().toString();
        Button nameButton = (Button) findViewById(r.id.name_textboox);
        String name = nameButton.getText().toString();
        String signed_msg = name + ": " + msg;
        OutputStream out = new DataOutputStream(new BufferedOutputStream(this.socket.getOutputStream()));
        out.write(signed_msg);
    }

    public void receivingThread() {
        InputStream in = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));
        while (true) {
            byte[] buffer = new byte[8192]; // or 4096, or more
            in.read(buffer);
            String chat_text = new String(buffer, "UTF-8");
            TextView chatbox = (TextView) findViewById(r.id.textView2);
            chatbox.setText(chat_text);
        }

    }

}
