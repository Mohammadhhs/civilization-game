package ir.ac.ap.civilization.audio;

import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.BufferedInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackgroundMusicResourceTest {

    @Test
    void originalSoundtrackIsPresentAndReadable() throws Exception {
        InputStream resource = BackgroundMusicResourceTest.class
                .getResourceAsStream("/audio/background.wav");
        assertNotNull(resource);

        try (BufferedInputStream buffered = new BufferedInputStream(resource);
             AudioInputStream audio = AudioSystem.getAudioInputStream(buffered)) {
            AudioFormat format = audio.getFormat();
            assertEquals(AudioFormat.Encoding.PCM_SIGNED, format.getEncoding());
            assertEquals(22050.0f, format.getSampleRate());
            assertEquals(1, format.getChannels());
            assertTrue(audio.getFrameLength() > 0);
        }
    }

    @Test
    void originalPlayerKeepsVolumeContract() {
        BackgroundMusicPlayer player = new BackgroundMusicPlayer();
        try {
            player.setVolumePercent(65);
            assertEquals(65, player.getVolumePercent());
            assertThrows(IllegalArgumentException.class,
                    () -> player.setVolumePercent(101));
        } finally {
            player.close();
        }
    }
}
