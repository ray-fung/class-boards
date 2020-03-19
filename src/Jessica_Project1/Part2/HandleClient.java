import java.io.IOException;
import java.net.*;

public class HandleClient extends Thread {
    private DatagramSocket socket;
    private DatagramPacket packet;
    private Stages stage;

    private ServerSocket ss;
    private Socket s;


    public HandleClient(DatagramSocket socket, DatagramPacket packet) throws IOException {
        this.socket = socket;
        this.packet = packet;
        stage = new Stages();
        ss = null;
        s = null;
    }

    @Override
    public void run() {
        try {
            packet = stage.stageA(socket, packet);
            int tcp_port = stage.stageB(packet);
            ss = new ServerSocket(tcp_port);
            s = ss.accept();
            stage.stageC(s);
            stage.stageD(s);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            try {
                s.close();
                ss.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
