package com.example.classboards;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    private static String HOST_NAME = "attu6.cs.washington.edu";
    private static int PORT_NUM = 21217;
    public static Socket socket;
    public static EditText courseText;
    public static String course;
    public static EditText nameText;
    public static EditText messageText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // socket = setup();
        // Log.e("After setup", "SET UP");

        courseText = (EditText) findViewById(R.id.course_textbox);
        nameText = (EditText) findViewById(R.id.name_textbox);
        messageText = (EditText) findViewById(R.id.message_textbox);

        try {
            AsyncTaskSetup temp = new AsyncTaskSetup();
            temp.execute("hello");
            temp.wait();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (socket == null) {
                Log.e("error", "null server");
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                receivingThread();
            }
        }).start();

    }

    public void onClassSelect(View view) {
        String course = courseText.getText().toString();
        String msg = "refresh: " + course;

        AsyncTaskCourse temp = new AsyncTaskCourse();
        temp.execute(msg);
    }

    public void onMessageSend(View view) {
        Log.e("Message Send", "Pressed");
        String msg = messageText.getText().toString();
        String name = nameText.getText().toString();
        String signed_msg = name + ": " + msg;

        AsyncTaskSend temp = new AsyncTaskSend();
        temp.execute(signed_msg);
    }

    public void receivingThread() {
        while (true) {
            try {
                if (socket != null) {
                    InputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                    byte[] buffer = new byte[8192]; // or 4096, or more
                    in.read(buffer);
                    String chat_text = new String(buffer, StandardCharsets.UTF_8);
                    TextView chatbox = (TextView) findViewById(R.id.textView2);
                    chatbox.setText(chat_text);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class AsyncTaskSetup extends AsyncTask<String, String, Socket> {
        @Override
        protected Socket doInBackground(String... strings) {
            try {
                InetAddress ip_address = InetAddress.getByName(HOST_NAME);
                return new Socket(ip_address, PORT_NUM);
            } catch (Exception E) {
                // Do something
                E.printStackTrace();
                Log.e("this is the problem1", E.getMessage() + "");
                Log.e("Setup1", E.getLocalizedMessage() + "");
                return null;
            }
        }

        @Override
        protected void onPostExecute(Socket socket1) {
            super.onPostExecute(socket);
            socket = socket1;
            Log.e("onpostExecute", "Past post execute");
        }
    }

    private static class AsyncTaskCourse extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                Log.e("Class Select", "Pressed");
                if (socket == null) {
                    Log.e("Class Select", "Socket is null still");
                }
                try {
                    String msg = strings[0];
                    PrintWriter out1 = new PrintWriter(socket.getOutputStream(), true);
                    out1.println(msg);
                    Log.e("Sent message", msg);
                    return msg;
                } catch (Exception e) {
                    //Log.e("OnClassSelect", e.getMessage());
                    e.printStackTrace();
                }
            } catch (Exception E) {
                // Do something
                E.printStackTrace();
                Log.e("this is the problem1", E.getMessage() + "");
                Log.e("Setup1", E.getLocalizedMessage() + "");
                return null;
            }
            return "";
        }
    }

    private static class AsyncTaskSend extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                Log.e("Class Select", "Pressed");
                if (socket == null) {
                    Log.e("Class Select", "Socket is null still");
                }
                try {
                    Log.e("Message Send", "Pressed");
                    String signed_msg = strings[0];
                    try {
                        PrintWriter out1 = new PrintWriter(socket.getOutputStream(), true);
                        out1.println(signed_msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    //Log.e("OnClassSelect", e.getMessage());
                    e.printStackTrace();
                }
            } catch (Exception E) {
                // Do something
                E.printStackTrace();
                Log.e("this is the problem2", E.getMessage() + "");
                Log.e("Setup2", E.getLocalizedMessage() + "");
                return null;
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s1) {
            super.onPostExecute(s1);
            messageText.setText("");
        }
    }
}

