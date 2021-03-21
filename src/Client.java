import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

public class Client {

        static JFrame frame = new JFrame();
        static JLabel lbl_myvid = new JLabel();
        static JLabel lbl_othervid = new JLabel();

        static DatagramSocket socket;
        static DatagramSocket audioSocket;

        public static void main(String[] args) {

                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.setLayout(new GridLayout());
                frame.add(lbl_myvid);
                frame.add(lbl_othervid);

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

                byte[] b = new byte[65536];
                try {
                        socket = new DatagramSocket();
                        audioSocket = new DatagramSocket();
                } catch (SocketException e) {
                        e.printStackTrace();
                }
                DatagramPacket rcampacket = new DatagramPacket(b, b.length);
                String ip = JOptionPane.showInputDialog("Enter server IP address");


                //Thread for receive cam
                Thread receivecam = new Thread(new ReceiveCam(socket,rcampacket,false));

                //Audio-send thread start
                Thread sendaudio = new SendAudio(ip,3001,audioSocket);
                sendaudio.start();

                //Audio-receive thread start
                byte[] audio = new byte[65536];
                DatagramPacket audiopacket= new DatagramPacket(audio,audio.length);
                Thread reciveaudio = new ReceiveAudio(audioSocket,audiopacket, false);
                reciveaudio.start();


                boolean flag = true;
                while (true){

                        try {
                                video.read(f);
                                Imgproc.resize(f, f, new Size(400,350));
                                MatOfByte m = new MatOfByte();
                                Imgcodecs.imencode(".jpg", f, m);
                                byte[] byteArray = m.toArray();

                                //Send packets
                                DatagramPacket campacket = new DatagramPacket(byteArray, byteArray.length, InetAddress.getByName(ip), 3000);
                                socket.send(campacket);

                                //View
                                showResult(byteArray,lbl_myvid);

                                //Send a packet and after receive
                                if (flag) {
                                        receivecam.start();
                                        flag = false;
                                }

                        } catch (IOException e) {
                                e.printStackTrace();
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