import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Server {

        static JFrame frame = new JFrame();
        static JLabel lbl_myvid = new JLabel();
        static JLabel lbl_othervid = new JLabel();
        static boolean recived = false;

        static DatagramSocket server;
        static DatagramSocket audioSocket;

        public static void main (String[] args){

                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.setLayout(new GridLayout());
                frame.add(lbl_myvid);
                frame.add(lbl_othervid);

                try {
                        server = new DatagramSocket(3000);
                        audioSocket = new DatagramSocket(3001);
                } catch (SocketException e) {
                        e.printStackTrace();
                }

                byte[] b = new byte[65536];
                DatagramPacket messaggioUDP = new DatagramPacket(b, b.length);

                //Load opencv lib
                System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
                VideoCapture video = new VideoCapture();
                Mat f = new Mat();

                int video_index = 0;
                try {
                        video_index = Integer.parseInt(JOptionPane.showInputDialog("Enter webcam num-id (0/1/2/3)"));
                }catch (NumberFormatException e){
                        JOptionPane.showMessageDialog(frame,"You must enter a number!");
                        System.exit(1);
                }

                video.open(video_index);

                byte[] audio = new byte[65536];
                DatagramPacket audiopacket= new DatagramPacket(audio,audio.length);
                Thread reciveaudio = new ReceiveAudio(audioSocket,audiopacket,true); //Server receive first
                reciveaudio.start();

                //Audio stream init
                while(true){
                        if(ReceiveAudio.audiorecived){
                                Thread sendaudio = new SendAudio(audiopacket.getAddress().toString().replace("/",""),audiopacket.getPort(),audioSocket);
                                sendaudio.start();
                                break;
                        }
                        else{
                                try {
                                        TimeUnit.MILLISECONDS.sleep(500);
                                } catch (InterruptedException e) {
                                        e.printStackTrace();
                                }
                        }
                }

                //Thread for receive cam
                Thread t = new Thread(new ReceiveCam(server,messaggioUDP,true));
                t.start();

                while(true)
                {

                        if(recived){
                                video.read(f);
                                Imgproc.resize(f, f, new Size(400, 350));
                                MatOfByte m = new MatOfByte();
                                Imgcodecs.imencode(".jpg", f, m);
                                byte[] byteArray = m.toArray();

                                //Send packets
                                DatagramPacket rispostaUDP = new DatagramPacket(byteArray, byteArray.length, messaggioUDP.getAddress(), messaggioUDP.getPort());
                                try {
                                        assert server != null;
                                        server.send(rispostaUDP);
                                } catch (IOException e) {
                                        e.printStackTrace();
                                }

                                //View
                                showResult(rispostaUDP.getData(),lbl_myvid);
                        }
                        else{
                                try {
                                        TimeUnit.MILLISECONDS.sleep(500);
                                } catch (InterruptedException e) {
                                        e.printStackTrace();
                                }
                        }
                }

        }

        //View cam on JFrame
        public static void showResult(byte[] byteArray,JLabel lbl) {
                try {
                        InputStream in = new ByteArrayInputStream(byteArray);
                        BufferedImage bufImage = ImageIO.read(in);
                        lbl.setIcon(new ImageIcon(bufImage));
                        lbl.revalidate();
                        frame.pack();
                        frame.setVisible(true);
                        frame.revalidate();
                }
                catch (Exception e) {
                        e.printStackTrace();
                }


        }

}