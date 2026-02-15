package top.gregtao.concerto.core.player.streamplayer.stream;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

public class StreamDataSource implements DataSource {

    private final InputStream source;

    StreamDataSource(InputStream source) {
        this.source = source;
    }

    @Override
    public AudioFileFormat getAudioFileFormat() throws UnsupportedAudioFileException, IOException {
        return AudioSystem.getAudioFileFormat(source);
    }

    @Override
    public AudioInputStream getAudioInputStream() throws UnsupportedAudioFileException, IOException {
//        Logger logger = MusicPlayer.INSTANCE.logger;
//        logger.info("Creating AudioInputStream from StreamDataSource");
//
//        final PrivilegedAction<Iterator<AudioFileReader>> psAction =
//                new PrivilegedAction<Iterator<AudioFileReader>>() {
//                    @Override
//                    public Iterator<AudioFileReader> run() {
//                        return ServiceLoader.load(AudioFileReader.class).iterator();
//                    }
//                };
//        final Iterator<AudioFileReader> ps = AccessController.doPrivileged(psAction);
//        PrivilegedAction<Boolean> hasNextAction = new PrivilegedAction<Boolean>() {
//            @Override
//            public Boolean run() {
//                return ps.hasNext();
//            }
//        };
//
//        while (AccessController.doPrivileged(hasNextAction)) {
//            try {
//                AudioFileReader provider = ps.next();
//                if (AudioFileReader.class.isInstance(provider)) {
//                    logger.info("AudioInputStream created successfully using " + provider.getClass().getName());
//                }
//            } catch (Throwable t) {
//            }
//        }
//        throw new UnsupportedAudioFileException("No suitable AudioFileReader found for the provided InputStream.");
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(source);
//        logger.info("AudioInputStream created successfully");
        return audioInputStream;
    }

    @Override
    public int getDurationInSeconds() {
        return -1;
    }
    
    @Override
    public long getDurationInMilliseconds() {
        return -1;
    }
    
    @Override
    public Duration getDuration() {
        return null;
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "StreamDataSource with " + source.toString();
    }

    @Override
    public boolean isFile() {
       return false;
   }
}
