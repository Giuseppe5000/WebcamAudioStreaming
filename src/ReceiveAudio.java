import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.TimeUnit;

public class ReceiveAudio extends Thread
{
        private final DatagramSocket socket;
        private final DatagramPacket audiopacket;
        private boolean server;

        static boolean audiorecived = false;

        public ReceiveAudio(DatagramSocket socket,DatagramPacket audiopacket,boolean server)
        {
                this.socket = socket;
                this.audiopacket = audiopacket;
                this.server = server;
        }

        @Override
        public void run()
        {
                while (true){
                        try {
                                socket.receive(audiopacket);

                                if(server){
                                        audiorecived = true;
                                        server = false;
                                }

                                AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
                                float rate = 44100.0f;
                                int channels = 1;
                                int sampleSize = 8;
                                boolean bigEndian = true;
                                AudioFormat format = new AudioFormat(encoding, rate, sampleSize, channels, (sampleSize / 8) * channels, rate, bigEndian);
                                AudioInputStream stream = new AudioInputStream(new ByteArrayInputStream(audiopacket.getData()), format, audiopacket.getData().length);
                                Play(stream);

                        } catch (IOException e) {
                                e.printStackTrace();
                        }
                }


        }


        public static void Play(AudioInputStream data)
        {
                try (Clip clip = AudioSystem.getClip()) {
                        try {
                                clip.open(data);
                        } catch (IOException e) {
                                e.printStackTrace();
                        }
                        clip.loop(0);
                        try {
                                TimeUnit.MILLISECONDS.sleep(2000);
                        } catch (InterruptedException e) {
                                e.printStackTrace();
                        }
                }
                catch (LineUnavailableException e) {
                        e.printStackTrace();
                }

        }
}
