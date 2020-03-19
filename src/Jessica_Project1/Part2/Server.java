import java.io.IOException;
import java.net.*;

public class Server {

    public static final String HELLO_WORLD = "hello world\0";
    public static int HEADER_BYTE_LEN = 12;
    public static final int INT_SIZE = 4;
    public static final short RECEIVED_STUDENT_NUM = 989;
    public static final short SENDING_STUDENT_NUM = 667;

    public static void main (String[] args) throws IOException {
        int port = 12235;

        if (args.length != 0) {
            port = Integer.parseInt(args[0]);
            System.out.println(port);
        }
        DatagramSocket socket = new DatagramSocket(port);

        Stages stage = new Stages();
        int origLength = HELLO_WORLD.getBytes().length;
        int payloadLengthAligned = Stages.getPacketLengthAligned(origLength);
        int packetLength = HEADER_BYTE_LEN + payloadLengthAligned;

        while (true) {
            byte[] response = new byte[packetLength];
            DatagramPacket packet = new DatagramPacket(response, response.length);
            try {
                socket.receive(packet);
                Thread t = new HandleClient(socket, packet);
                t.start();
            } catch (Exception e) {
                break;
            }
        }
    }
}
