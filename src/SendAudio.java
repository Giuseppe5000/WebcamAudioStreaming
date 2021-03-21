import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

//Thread for send audio stream
public class SendAudio extends Thread
{
        private final String ip;
        private final DatagramSocket socket;
        private final int port;

        public SendAudio(String ip,int port, DatagramSocket socket)
        {
                this.ip = ip;
                this.socket = socket;
                this.port = port;
        }

        @Override
        public void run()
        {
               while (true){
                       try {
                               DatagramPacket audio = new DatagramPacket(Record().readAllBytes(),Record().readAllBytes().length, InetAddress.getByName(ip), port);
                               socket.send(audio);
                               System.out.println("SENDAUDIO");
                       } catch (IOException e) {
                               e.printStackTrace();
                       }
               }
        }

        public static AudioInputStream Record()
        {
                AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
                float rate = 44100.0f;
                int channels = 1;//2
                int sampleSize = 8;//16
                boolean bigEndian = true;
                AudioFormat format = new AudioFormat(encoding, rate, sampleSize, channels, (sampleSize / 8) * channels, rate, bigEndian);

                TargetDataLine line = null;
                DataLine.Info info = new DataLine.Info(TargetDataLine.class,format); // format is an AudioFormat object
                if (!AudioSystem.isLineSupported(info)) {
                        System.out.println("TargetDataLine error");
                }

                // Obtain and open the line.
                try {
                        line = (TargetDataLine) AudioSystem.getLine(info);
                        line.open(format);
                } catch (LineUnavailableException ex) {
                        ex.printStackTrace();
                        System.out.println("Line open error");
                }

                // Assume that the TargetDataLine, line, has already
                // been obtained and opened.
                ByteArrayOutputStream out  = new ByteArrayOutputStream();
                int numBytesRead;
                assert line != null;
                byte[] data = new byte[line.getBufferSize() / 5];

                // Begin audio capture.
                line.start();

                // Here, stopped is a global boolean set by another thread.
                long initTime = System.currentTimeMillis();
                long finalTime = initTime + 1000;
                //Ciclo per 2 secondi
                while (initTime < finalTime) {
                        // Read the next chunk of data from the TargetDataLine.
                        numBytesRead =  line.read(data, 0, data.length);
                        // Save this chunk of data.
                        out.write(data, 0, numBytesRead);

                        initTime = System.currentTimeMillis();
                }

                byte[] audioBytes = out.toByteArray();
                ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
                int frameSizeInBytes = format.getFrameSize();

                line.close();

                return new AudioInputStream(bais,format,audioBytes.length / frameSizeInBytes);

        }

}
