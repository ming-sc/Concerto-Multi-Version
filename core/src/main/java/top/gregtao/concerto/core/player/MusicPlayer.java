package top.gregtao.concerto.core.player;

import top.gregtao.concerto.core.ConcertoCore;
import top.gregtao.concerto.core.bridge.ComponentImpl;
import top.gregtao.concerto.core.util.ConcertoRunner;
import top.gregtao.concerto.core.util.FunctionUtil;
import top.gregtao.concerto.core.music.Music;
import top.gregtao.concerto.core.player.streamplayer.enums.Status;
import top.gregtao.concerto.core.player.streamplayer.stream.StreamPlayer;
import top.gregtao.concerto.core.player.streamplayer.stream.StreamPlayerEvent;
import top.gregtao.concerto.core.player.streamplayer.stream.StreamPlayerException;
import top.gregtao.concerto.core.player.streamplayer.stream.StreamPlayerListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class MusicPlayer extends StreamPlayer implements StreamPlayerListener {

    public static MusicPlayer INSTANCE;
    public static final Logger PLAYER_LOGGER;

    public Runnable onPlay = FunctionUtil.emptyRunnable();
    public Runnable onForceResume = FunctionUtil.emptyRunnable();
    public Runnable onPause = FunctionUtil.emptyRunnable();
    public Runnable onResume = FunctionUtil.emptyRunnable();
    public Consumer<Music> onPlayNext = FunctionUtil.emptyConsumer();
    public Supplier<Double> volumeSupplier = FunctionUtil.emptySupplier();

    static {
        PLAYER_LOGGER = Logger.getLogger(MusicPlayer.class.getName());
        File file = new File("Concerto");
        if (!file.exists() && !file.isDirectory() && !file.mkdir()) {
            throw new RuntimeException("Cannot mkdir!");
        }
        FileHandler fileHandler;
        try {
            fileHandler = new FileHandler("Concerto/player.log", false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        fileHandler.setFormatter(new SimpleFormatter());
        PLAYER_LOGGER.addHandler(fileHandler);
        PLAYER_LOGGER.setLevel(Level.ALL);
        resetInstance();
    }

    public static void resetInstance() {
        try {
            if (MusicPlayerHandler.INSTANCE.currentSource != null) {
                MusicPlayerHandler.INSTANCE.currentSource.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        INSTANCE = new MusicPlayer(PLAYER_LOGGER);
    }

    public boolean forcePaused = false;

    public boolean started = false;

    public AtomicBoolean playNextLock = new AtomicBoolean(false);

    public boolean isPlayingTemp = false;

    public MusicPlayer() {
        super();
        this.addStreamPlayerListener(this);
    }

    public MusicPlayer(Logger logger) {
        super(logger);
        this.addStreamPlayerListener(this);
    }

    public void addMusic(Music music) {
        this.addMusic(music, () -> {});
    }

    public void addMusic(List<Music> musics) {
        this.addMusic(musics, () -> {});
    }

    public void addMusic(Music music, Runnable callback) {
        ConcertoRunner.run(() -> MusicPlayerHandler.INSTANCE.addMusic(music), callback);
    }

    public void addMusic(List<Music> musics, Runnable callback) {
        ConcertoRunner.run(() -> MusicPlayerHandler.INSTANCE.addMusic(musics), callback);
    }

    public void addMusic(Supplier<List<Music>> musicListAdder, Runnable callback) {
        ConcertoRunner.run(() -> MusicPlayerHandler.INSTANCE.addMusic(musicListAdder.get()), callback);
    }

    public void addMusicHere(Music music, boolean skip) {
        this.addMusicHere(music, skip, () -> {});
    }

    public void addMusicHere(Music music, boolean skip, Runnable callback) {
        ConcertoRunner.run(() -> {
            MusicPlayerHandler.INSTANCE.addMusicHere(music);
            if (skip) {
                this.skipTo(MusicPlayerHandler.INSTANCE.getCurrentIndex() + 1);
            }
        }, callback);
    }

    @Override
    public void play() throws StreamPlayerException {
        onPlay.run();
        super.play();
        this.syncVolume();
    }

    public void forcePause() {
        this.forcePaused = true;
        this.pause();
    }

    public void forceResume() {
        this.forcePaused = false;
        onForceResume.run();
        super.resume();
    }

    @Override
    public boolean pause() {
        if (!super.isPaused()) {
            MusicPlayerHandler.INSTANCE.writeConfig();
            onPause.run();
            return super.pause();
        } else {
            return false;
        }
    }

    @Override
    public boolean resume() {
        if (this.forcePaused) return false;
        if (super.isPaused()) {
            onResume.run();
            return super.resume();
        } else {
            return false;
        }
    }

    public boolean musicRoomPause() {
        this.forcePaused = true;
        return super.pause();
    }

    public boolean musicRoomResume() {
        this.forcePaused = false;
        return super.resume();
    }

    public void syncVolume() {
        try {
            this.setGain(getProperVolume());
        } catch (NullPointerException ignore) {}
    }

    public double getProperVolume() {
        return volumeSupplier.get();
    }

    @Override
    public void opened(Object dataSource, Map<String, Object> properties) {}

    @Override
    public void progress(int nEncodedBytes, long microsecondPosition, byte[] pcmData, Map<String, Object> properties) {
        MusicPlayerHandler.INSTANCE.updateDisplayTexts(microsecondPosition / 1000);
    }

    @Override
    public void statusUpdated(StreamPlayerEvent event) {
        Status status = event.getPlayerStatus();
        if (status == Status.EOM) {
            if (!this.playNextLock.get()) {
                MusicPlayerHandler.INSTANCE.resetInfo();
            }
            if (MusicPlayerHandler.INSTANCE.isEmpty()) {
                this.started = false;
            } else if (!this.playNextLock.get() && !this.isPlayingTemp) {
                this.playNext(1);
            }
            this.forcePaused = this.isPlayingTemp = false;
        }
    }

    public void playTempMusic(Music music, Runnable callback) {
        ConcertoRunner.run(() -> {
            InputStream source = music.getMusicSourceOrNull();
            if (source == null) return;
            this.forcePaused = false;
            this.playNextLock.set(true);
            this.started = true;
            this.stop();
            MusicPlayerHandler status = MusicPlayerHandler.INSTANCE;
            status.resetInfo();
            status.currentMusic = music;
            status.currentSource = source;
            status.initMusicStatus();
            status.updateDisplayTexts();
            try {
                this.open(source);
                this.play();
                ConcertoCore.CLIENT_LOGGER.info(
                    "Start playing temporary music {} - {} from {}",
                    music.getMeta().title(), music.getMeta().author(),
                    music.getMeta().getSource()
                );
            } catch (StreamPlayerException e) {
                this.started = this.isPlayingTemp = this.forcePaused = false;
                ConcertoCore.CLIENT_LOGGER.error(e.toString());
                ComponentImpl.displayClientMessage(
                        false,
                        "concerto.player.error",
                        e.toString()
                );
            }
            this.isPlayingTemp = true;
            this.playNextLock.set(false);
        }, callback);
    }

    public void playTempMusic(Music music) {
        this.playTempMusic(music, () -> {});
    }

    public void playNext(int forward) {
        this.playNext(forward, () -> {});
    }

    public void playNext(int forward, Runnable callback) {
        this.playNext(forward, index -> callback.run());
    }

    public void playNext(int forward, Consumer<Integer> callback) {
        ConcertoRunner.run(() -> {
            try {
                if (!this.started || MusicPlayerHandler.INSTANCE.isEmpty()) {
                    this.started = false;
                    return;
                }
                this.playNextLock.set(true);
                this.stop();
                Music music = MusicPlayerHandler.INSTANCE.playNext(forward);
                if (music != null) {
                    InputStream source;
                    while ((source = music.getMusicSourceOrNull()) == null) {
                        ConcertoCore.CLIENT_LOGGER.error(
                            "Unable to play music: {} - {} from {}",
                            music.getMeta().title(), music.getMeta().author(),
                            music.getMeta().getSource()
                        );
                        ComponentImpl.displayClientMessage(
                                false,
                                "concerto.player.unable",
                                music.getMeta().title(), music.getMeta().author(),
                                music.getMeta().getSource()
                        );
                        MusicPlayerHandler.INSTANCE.setCurrentIndex((MusicPlayerHandler.INSTANCE.getCurrentIndex() + 1)
                                % MusicPlayerHandler.INSTANCE.getMusicList().size());
                        MusicPlayerHandler.INSTANCE.resetInfo();
                        music = MusicPlayerHandler.INSTANCE.playNext(0);
                        if (music == null) {
                            return;
                        }
                    }
                    MusicPlayerHandler.INSTANCE.currentSource = source;
                    this.open(source);
                    this.play();
                    ConcertoCore.CLIENT_LOGGER.info("Start playing music {} - {}", music.getMeta().title(), music.getMeta().author());
                    onPlayNext.accept(music);
                    callback.accept(MusicPlayerHandler.INSTANCE.getCurrentIndex());
                }
                this.playNextLock.set(false);
                this.isPlayingTemp = this.forcePaused = false;
            } catch (Exception e) {
                ConcertoCore.CLIENT_LOGGER.error(e.toString());
                ComponentImpl.displayClientMessage(
                        false,
                        "concerto.player.error",
                        e.toString()
                );
                this.playNextLock.set(false);
                this.isPlayingTemp = this.forcePaused = false;
                playNext(1);
            }
        });
    }

    public void skipTo(int index) {
        MusicPlayerHandler.INSTANCE.setCurrentIndex(
                Math.min(MusicPlayerHandler.INSTANCE.getMusicList().size(), index));
        MusicPlayerHandler.INSTANCE.resetInfo();
        this.start();
    }

    public void start() {
        this.started = true;
        this.forcePaused = false;
        this.playNextLock.set(false);
        this.playNext(0);
    }

    public void clear() {
        ConcertoRunner.run(() -> {
            this.started = false;
            this.stop();
            MusicPlayerHandler.INSTANCE.clear();
        });
    }

    public void reloadConfig(Runnable callback) {
        ConcertoRunner.run(() -> {
            this.started = false;
            this.stop();
            MusicPlayerHandler.INSTANCE = MusicPlayerHandler.fromRaw(ConcertoCore.MUSIC_CONFIG.read());
        }, callback);
    }

    public void cut(Runnable callback) {
        ConcertoRunner.run(() -> {
            if (!this.isPlayingTemp) {
                MusicPlayerHandler.INSTANCE.removeCurrent();
            }
            this.playNext(0);
        }, callback);
    }

    public void remove(int index, Runnable callback) {
        if (index == MusicPlayerHandler.INSTANCE.getCurrentIndex()) this.cut(callback);
        else {
            ConcertoRunner.run(() -> {
                MusicPlayerHandler.INSTANCE.remove(index);
                if (MusicPlayerHandler.INSTANCE.isEmpty()) this.cut(() -> {});
            }, callback);
        }
    }
}