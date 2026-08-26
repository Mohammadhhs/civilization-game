package ir.ac.ap.civilization.audio;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.BufferedInputStream;
import java.io.InputStream;

public final class BackgroundMusicPlayer implements AutoCloseable {

    private static final String SOUNDTRACK_PATH =
            "/audio/background.wav";

    private Clip clip;
    private int volumePercent;

    public BackgroundMusicPlayer() {
        volumePercent = 55;
        loadSoundtrack();
    }

    public int getVolumePercent() {
        return volumePercent;
    }

    public boolean isAvailable() {
        return clip != null;
    }

    public void start() {
        if (clip == null) {
            return;
        }

        clip.setFramePosition(0);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        clip.start();
    }

    public void setVolumePercent(int volumePercent) {
        if (volumePercent < 0 || volumePercent > 100) {
            throw new IllegalArgumentException(
                    "Volume must be between 0 and 100"
            );
        }

        this.volumePercent = volumePercent;
        applyVolume();
    }

    @Override
    public void close() {
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
    }

    private void loadSoundtrack() {
        try {
            InputStream resourceStream =
                    BackgroundMusicPlayer.class
                            .getResourceAsStream(
                                    SOUNDTRACK_PATH
                            );

            if (resourceStream == null) {
                return;
            }

            try (
                    BufferedInputStream bufferedStream =
                            new BufferedInputStream(
                                    resourceStream
                            );
                    AudioInputStream audioInputStream =
                            AudioSystem.getAudioInputStream(
                                    bufferedStream
                            )
            ) {
                clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                applyVolume();
            }
        } catch (Exception exception) {
            clip = null;
        }
    }

    private void applyVolume() {
        if (clip == null ||
                !clip.isControlSupported(
                        FloatControl.Type.MASTER_GAIN
                )) {
            return;
        }

        FloatControl gainControl =
                (FloatControl) clip.getControl(
                        FloatControl.Type.MASTER_GAIN
                );

        float value;

        if (volumePercent == 0) {
            value = gainControl.getMinimum();
        } else {
            double linearVolume = volumePercent / 100.0;
            value = (float) (20.0 * Math.log10(linearVolume));
            value = Math.max(
                    gainControl.getMinimum(),
                    Math.min(gainControl.getMaximum(), value)
            );
        }

        gainControl.setValue(value);
    }
}
