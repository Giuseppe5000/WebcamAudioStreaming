import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

//Thread for audio stream receive
public class ReceiveCam extends Thread
{
        DatagramSocket socket;
        DatagramPacket messaggioUDP;
        boolean server;

        public ReceiveCam(DatagramSocket socket, DatagramPacket messaggioUDP,boolean server){
                this.socket = socket;
                this.messaggioUDP = messaggioUDP;
                this.server = server;
        }

        @Override
        public void run()
        {
                while(true){
                        try {
                                socket.receive(messaggioUDP);
                                if(server) {
                                        Server.recived = true;
                                        Server.showResult(messaggioUDP.getData(), Server.lbl_othervid);
                                } else
                                        Client.showResult(messaggioUDP.getData(), Client.lbl_othervid);

                        } catch (IOException e) {
                                e.printStackTrace();
                        }

                }
        }
}
