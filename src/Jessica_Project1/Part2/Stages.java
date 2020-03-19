import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;

public class Stages {
    private Random random;
    private InputStream in;
    private OutputStream out;

    // Stage A
    private int numA;
    private int lenA;
    private int secretA;
    private int udp_port;

    // Stage B
    private int tcp_port;
    private int secretB;

    // Stage C
    private int num2;
    private int len2;
    private int secretC;
    private byte c;

    // Stage D
    private int secretD;

    public static final String HELLO_WORLD = "hello world\0";
    public static int HEADER_BYTE_LEN = 12;
    public static final int INT_SIZE = 4;
    public static final short RECEIVED_STUDENT_NUM = 989;
    public static final short SENDING_STUDENT_NUM = 667;
    public static final int TIMEOUT = 3000;

    public Stages() {
        random = new Random();
    }

    public DatagramPacket stageA(DatagramSocket socket, DatagramPacket packet) throws IOException {
        int origLength = HELLO_WORLD.getBytes().length;

        byte[] response = packet.getData();
        // verify the header
        if (!verifyHeader(origLength, 0, (short) 1, response)) {
            return null;
        }

        // verify the payload/length of packet
        String payloadMessage = new String(response, HEADER_BYTE_LEN, origLength);
        if (!payloadMessage.equals(HELLO_WORLD)) {
            return null;
        }

        // respond with UDP packet containing 4 numbers
        // generate 4 random numbers
        numA = random.nextInt(16);
        lenA = random.nextInt(16);
        udp_port = random.nextInt(30000) + 1024;
        secretA = random.nextInt(16);

        byte[] header = setHeader(4 * INT_SIZE, 0, (short) 2);
        ByteBuffer bb = ByteBuffer.allocate(4 * INT_SIZE);
        bb.putInt(numA);
        bb.putInt(lenA);
        bb.putInt(udp_port);
        bb.putInt(secretA);
        byte[] payload = bb.array();

        packet = sendPacket(socket, packet, header, payload);
        System.out.println("secretA: " + secretA);
        return packet;
    }

    public int stageB(DatagramPacket packet) throws IOException {
        DatagramSocket socket = new DatagramSocket(udp_port);
        socket.setSoTimeout(TIMEOUT);

        int payloadLengthAligned = getPacketLengthAligned(lenA);
        int packetLength = HEADER_BYTE_LEN + INT_SIZE + payloadLengthAligned;

        for (int i = 0; i < numA; i++) {
            byte[] response = new byte[packetLength];
            packet = new DatagramPacket(response, response.length);
            socket.receive(packet);

            // verify header
            if (!verifyHeader(lenA + INT_SIZE, secretA, (short) 1, response)) {
                return -1;
            }

            ByteBuffer wrapped = ByteBuffer.wrap(response);

            // verify packet_id
            int packet_id = wrapped.getInt(HEADER_BYTE_LEN);
            if (packet_id != i) {
                return -1;
            }

            // verify that the rest of the payload is 0s
            for (int j = HEADER_BYTE_LEN + INT_SIZE; j < packetLength; j++) {
                if (response[j] != (byte) 0) {
                    return -1;
                }
            }

            if (random.nextBoolean()) {
                byte[] acked_header = setHeader(INT_SIZE, secretA, (short) 2);
                ByteBuffer bb = ByteBuffer.allocate(INT_SIZE);
                bb.putInt(packet_id);
                byte[] payload = bb.array();
                sendPacket(socket, packet, acked_header, payload);
            } else {
                i--;
            }
        }

        tcp_port = random.nextInt(30000) + 1024;
        secretB = random.nextInt(16);

        byte[] header = setHeader(2 * INT_SIZE, this.secretA, (short) 2);
        ByteBuffer bb = ByteBuffer.allocate(2 * INT_SIZE);
        bb.putInt(tcp_port);
        bb.putInt(secretB);
        byte[] payload = bb.array();

        packet = sendPacket(socket, packet, header, payload);
        System.out.println("secretB: " + secretB);
        socket.close();
        return tcp_port;
    }

    public void stageC(Socket socket) throws IOException {
        // packets are aligned on a 4-byte boundary
        byte[] header = setHeader(3 * INT_SIZE + 1, secretB, (short) 2);

        int payloadLengthAligned = getPacketLengthAligned(3 * INT_SIZE + 1);
        ByteBuffer bb = ByteBuffer.allocate(payloadLengthAligned);

        num2 = random.nextInt(16);
        len2 = random.nextInt(16);
        secretC = random.nextInt(16);
        byte[] b = new byte[1];
        random.nextBytes(b);
        this.c = b[0];

        bb.putInt(num2);
        bb.putInt(len2);
        bb.putInt(secretC);
        bb.put(c);
        byte[] payload = bb.array();
        out = socket.getOutputStream();
        System.out.println("secretC: " + secretC);
        writePacket(header, payload);
    }

    public void stageD(Socket socket) throws IOException {
        int payloadLengthAligned = getPacketLengthAligned(this.len2);
        int packetLength = HEADER_BYTE_LEN + payloadLengthAligned;

        in = socket.getInputStream();

        // iterate through the payload bits
        for (int i = 0; i < num2; i++) {
            byte[] packetC = new byte[packetLength];
            in.read(packetC);

            // verify header
            verifyHeader(len2, secretC, (short)1, packetC);

            // check the rest of payload bytes in packet is filled with char c
            for (int j = HEADER_BYTE_LEN; j < HEADER_BYTE_LEN + len2; j++) {
                if (packetC[j] != c) {
                    in.close();
                    return;
                }
            }
        }

        byte[] header = setHeader(INT_SIZE, secretC, (short) 2);
        ByteBuffer bb = ByteBuffer.allocate(INT_SIZE);
        secretD = random.nextInt(16);
        bb.putInt(secretD);
        byte[] payload = bb.array();
        System.out.println("secretD: " + secretD);
        writePacket(header, payload);
        out.close();
        in.close();
    }

    // returns the remaining padding length as the length of the packet
    // has to be 4-byte aligned
    public static int getPacketLengthAligned(int packetLength) {
        int remPad = 0;
        if (packetLength % 4 != 0) {
            remPad = 4 - (packetLength % 4);
        }
        return remPad + packetLength;
    }

    // verifies the header of byte array
    private boolean verifyHeader(int payloadLen, int psecret, short step, byte[] header) {
        // extract data from header
        ByteBuffer wrapped = ByteBuffer.wrap(header);
        int len = wrapped.getInt();
        int secret = wrapped. getInt();
        short serverStep = wrapped.getShort();
        short studentNum = wrapped.getShort();
        return (len == payloadLen) && (psecret == secret) &&
                (step == serverStep) && (RECEIVED_STUDENT_NUM == studentNum);
    }

    private byte[] setHeader(int payload_len, int psecret, short step) {
        ByteBuffer b = ByteBuffer.allocate(HEADER_BYTE_LEN);
        b.putInt(payload_len);
        b.putInt(psecret);
        b.putShort(step);
        b.putShort(SENDING_STUDENT_NUM);
        return b.array();
    }

    private DatagramPacket sendPacket(DatagramSocket socket, DatagramPacket packet, byte[] header, byte[] payload) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(header.length + payload.length).order(ByteOrder.BIG_ENDIAN);
        bb.put(header);
        bb.put(payload);
        byte[] buf = bb.array();
        InetAddress address = packet.getAddress();
        int port = packet.getPort();
        packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
        return packet;
    }

    private void writePacket(byte[] header, byte[] payload) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(header.length + payload.length).order(ByteOrder.BIG_ENDIAN);
        bb.put(header);
        bb.put(payload);
        byte[] buf = bb.array();
        out.write(buf);
    }
}
