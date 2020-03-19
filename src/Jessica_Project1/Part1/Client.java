import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Client {
    public static final String HOSTNAME = "attu2.cs.washington.edu";
    private static final String HELLO_WORLD = "hello world\0";
    public static int PORT = 12235;
    public static int HEADER_BYTE_LEN = 12;
    public static final int RETRANSMISSION_TIME = 1000;
    public static int STUDENT_NUM = 989;

    public static void main(String[] args) throws IOException {
        InetAddress address = InetAddress.getByName(HOSTNAME);

        DatagramPacket packetA2 = stageA(address);
        DatagramPacket packetB2 = stageB(address, packetA2);

        // extract data from packetB2
        byte[] received = packetB2.getData();
        ByteBuffer wrapped = ByteBuffer.wrap(received);
        int tcp_port = wrapped.getInt(HEADER_BYTE_LEN);

        int secretB = wrapped.getInt(HEADER_BYTE_LEN + 4);

        // client opens a tcp connection to server on port tcp_port
        Socket clientSocket = new Socket(address, tcp_port);

        byte[] packetC2 = new byte[HEADER_BYTE_LEN + 16];
        InputStream in = stageC(clientSocket, secretB, packetC2);

        byte[] packetD2 = stageD(clientSocket, packetC2, in);
        // extract data from packetD2
        wrapped = ByteBuffer.wrap(packetD2);
        int secretD = wrapped.getInt(HEADER_BYTE_LEN);
        System.out.println("secretD: " + secretD);

        // close socket
        clientSocket.close();
    }

    public static DatagramPacket stageA(InetAddress address) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        String payload = HELLO_WORLD;
        ByteBuffer bbuf = setHeader(payload.getBytes().length, 0);
        byte[] payloadBytes = payload.getBytes();
        for (int i = 0; i < payloadBytes.length; i++) {
            bbuf.put(HEADER_BYTE_LEN + i, payloadBytes[i]);
        }
        //bbuf.put(12, payload.getBytes());
        byte[] buf = new byte[bbuf.remaining()];
        bbuf.get(buf);

        // Step a1: send single UDP packet
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, PORT);
        socket.send(packet);

        /* Receive UDP packet from server, containing, in the following order */
        buf = new byte[HEADER_BYTE_LEN + 16];
        packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        socket.close();
        return packet;
    }

    public static DatagramPacket stageB(InetAddress address, DatagramPacket packetA2) throws IOException {
        // extract data from packetA2
        byte[] received = packetA2.getData();
        ByteBuffer wrapped = ByteBuffer.wrap(received);
        int num = wrapped.getInt(HEADER_BYTE_LEN);
        int len = wrapped.getInt(HEADER_BYTE_LEN + 4);
        int udp_port = wrapped.getInt(HEADER_BYTE_LEN + 8);
        int secretA = wrapped.getInt(HEADER_BYTE_LEN + 12);

        System.out.println("secretA: " + secretA);

        // set up socket
        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(RETRANSMISSION_TIME);  // set retransmission interval

        // send num packets
        for (int i = 0; i < num; i++) {
            ByteBuffer bbuf = setHeader(len + 4, secretA);
            bbuf.putInt(12, i);
            int padding = 0;
            if (len % 4 != 0) {
                padding = 4 - (len % 4);
            }
            byte[] buf = new byte[HEADER_BYTE_LEN + len + 4 + padding];
            bbuf.get(buf);

            byte[] ackPacket = null;
            while (ackPacket == null || ByteBuffer.wrap(ackPacket).getInt(12) != i) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length, address, udp_port);
                socket.send(packet);

                byte[] receiveBuf = new byte[HEADER_BYTE_LEN + 4];
                packet = new DatagramPacket(receiveBuf, receiveBuf.length);
                try {
                    socket.receive(packet);
                    ackPacket = packet.getData();
//                    System.out.println("Received " + i);
                } catch (SocketTimeoutException s) {
                    ackPacket = null;
//                    System.out.println("Timeout");
                }
            }
        }

        byte[] buf = new byte[HEADER_BYTE_LEN + 8];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        socket.close();
        return packet;
    }

    public static InputStream stageC(Socket clientSocket, int secretB, byte[] packetC2) throws IOException {
        System.out.println("secretB: " + secretB);

        InputStream in = clientSocket.getInputStream();
        in.read(packetC2, 0, HEADER_BYTE_LEN + 16);
        return in;
    }

    public static byte[] stageD(Socket clientSocket, byte[] packetC2, InputStream in) throws IOException {
        // extract data from packetC
        ByteBuffer wrapped = ByteBuffer.wrap(packetC2);
        int num2 = wrapped.getInt(HEADER_BYTE_LEN);
        int len2 = wrapped.getInt(HEADER_BYTE_LEN + 4);
        int secretC = wrapped.getInt(HEADER_BYTE_LEN + 8);
        byte c = wrapped.get(HEADER_BYTE_LEN + 12);

        System.out.println("secretC: " + secretC);

        int padding = 0;
        if (len2 % 4 != 0) {
            padding = 4 - (len2 % 4);
        }

        for (int i = 0; i < num2; i++) {
            ByteBuffer bbuf = setHeader(len2, secretC);
            for (int j = HEADER_BYTE_LEN; j < HEADER_BYTE_LEN + len2; j++) {
                bbuf.put(j, c);
            }
            byte[] buf = new byte[HEADER_BYTE_LEN + len2 + padding];
            bbuf.get(buf);

            OutputStream out = clientSocket.getOutputStream();
            out.write(buf);
        }

        byte[] packetD2 = new byte[HEADER_BYTE_LEN + 4];
        in.read(packetD2, 0, HEADER_BYTE_LEN + 4);
        return packetD2;
    }

    // sets the header of the given client packets given len and secret
    public static ByteBuffer setHeader(int len, int secret) {
        int padding = 0;
        if (len % 4 != 0) {
            padding = 4 - (len % 4);
        }
        ByteBuffer bbuf = ByteBuffer.allocate(HEADER_BYTE_LEN + len + padding).order(ByteOrder.BIG_ENDIAN);
        bbuf.putInt(0, len);
        bbuf.putInt(4, secret);
        bbuf.putShort(8, (short) 1);
        bbuf.putShort(10, (short) STUDENT_NUM);
        return bbuf;
    }
}
