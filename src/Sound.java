import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

/**
 * Retro sound effects, made by generating square waves in code (the same kind of
 * sound old game consoles made), so no audio files are needed. If the computer
 * has no sound device, the game just stays silent.
 *
 * In the web version, Java's sound output doesn't work, so the clips are handed
 * to the browser to play instead (see webPlay in web/index.html).
 */
public final class Sound {
    public enum Effect { TYPE, MOVE, SELECT, CLUE, BAD, START, WIN, GAME_OVER }

    // 16-bit samples at CD quality: the most widely supported format, which the web version needs.
    private static final float RATE = 44100f;
    private static final int BYTES_PER_SAMPLE = 2;
    private static final boolean WEB = Boolean.getBoolean("mysteryhunters.web");

    private final Map<Effect, byte[]> clips = new EnumMap<>(Effect.class);
    private final BlockingQueue<byte[]> queue = new LinkedBlockingQueue<>();
    private volatile boolean cut;
    private volatile boolean enabled;
    private long busyUntil;
    private SourceDataLine line;

    public Sound(boolean enabled) {
        this.enabled = enabled;

        // Each effect is a list of notes: start pitch (Hz), end pitch (Hz), length (ms). Pitch 0 = silence.
        clips.put(Effect.TYPE, notes(0.10, 0.5, 1200, 1200, 14));
        clips.put(Effect.MOVE, notes(0.18, 0.25, 880, 880, 30));
        clips.put(Effect.SELECT, notes(0.22, 0.5, 660, 660, 45, 990, 990, 70));
        clips.put(Effect.CLUE, notes(0.22, 0.5, 523, 523, 70, 659, 659, 70, 784, 784, 70, 1047, 1047, 180));
        clips.put(Effect.BAD, notes(0.28, 0.5, 220, 110, 300));
        clips.put(Effect.START, notes(0.22, 0.25, 392, 392, 80, 523, 523, 80, 659, 659, 80, 784, 784, 220));
        clips.put(Effect.WIN, notes(0.22, 0.5, 523, 523, 110, 659, 659, 110, 784, 784, 110, 1047, 1047, 220,
                0, 0, 60, 784, 784, 110, 1047, 1047, 450));
        clips.put(Effect.GAME_OVER, notes(0.28, 0.5, 392, 392, 220, 370, 370, 220, 349, 349, 220, 330, 247, 700));

        if (WEB) {
            return;
        }
        try {
            AudioFormat format = new AudioFormat(RATE, 16, 1, true, false);
            line = AudioSystem.getSourceDataLine(format);
            line.open(format, 8192);
            line.start();
            Thread worker = new Thread(this::run, "sound");
            worker.setDaemon(true);
            worker.start();
        } catch (Exception | LinkageError e) {
            System.err.println("No sound device found, playing silently (" + e + ")");
            line = null;
        }
    }

    /** Plays an effect right away, cutting off whatever was playing. Typing blips never interrupt. */
    public void play(Effect effect) {
        if (!canPlay()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (effect == Effect.TYPE) {
            if (now >= busyUntil && queue.isEmpty()) {
                output(clips.get(effect), true);
            }
            return;
        }
        output(clips.get(effect), false);
        busyUntil = now + millis(effect);
    }

    /** Plays an effect once the current one has finished. */
    public void then(Effect effect) {
        if (!canPlay()) {
            return;
        }
        output(clips.get(effect), true);
        busyUntil = Math.max(busyUntil, System.currentTimeMillis()) + millis(effect);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean on) {
        enabled = on;
        if (!on) {
            if (WEB) {
                webStop();
            }
            queue.clear();
            cut = true;
        }
    }

    private boolean canPlay() {
        return enabled && (WEB || line != null);
    }

    /** Sends a clip to the speakers, either after what's playing or cutting it off. */
    private void output(byte[] clip, boolean afterCurrent) {
        if (WEB) {
            webPlay(clip, afterCurrent);
            return;
        }
        if (!afterCurrent) {
            queue.clear();
            cut = true;
        }
        queue.offer(clip);
    }

    // Written in JavaScript, in web/index.html.
    private static native void webPlay(byte[] clip, boolean afterCurrent);

    private static native void webStop();

    private long millis(Effect effect) {
        return (long) (clips.get(effect).length / BYTES_PER_SAMPLE / RATE * 1000);
    }

    // Runs on its own thread, feeding sound to the speakers in small chunks
    // so a new effect can cut in quickly.
    private void run() {
        try {
            while (true) {
                byte[] clip = queue.take();
                cut = false;
                for (int offset = 0; offset < clip.length && !cut; offset += 256) {
                    line.write(clip, offset, Math.min(256, clip.length - offset));
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (RuntimeException e) {
            System.err.println("Sound stopped working, playing silently (" + e + ")");
            line = null;
        }
    }

    /** Builds a square-wave clip. parts is groups of three: start Hz, end Hz, milliseconds. */
    private static byte[] notes(double volume, double duty, double... parts) {
        int total = 0;
        for (int i = 0; i < parts.length; i += 3) {
            total += samples(parts[i + 2]);
        }
        byte[] out = new byte[total * BYTES_PER_SAMPLE];
        int pos = 0;
        double phase = 0;
        for (int i = 0; i < parts.length; i += 3) {
            double from = parts[i];
            double to = parts[i + 1];
            int n = samples(parts[i + 2]);
            for (int s = 0; s < n; s++) {
                double progress = (double) s / n;
                double freq = from + (to - from) * progress;
                double value = 0;
                if (freq > 0) {
                    phase = (phase + freq / RATE) % 1.0;
                    value = phase < duty ? 1 : -1;
                }
                double fade = 1 - progress * 0.6;
                short sample = (short) (value * fade * volume * 32767);
                out[pos++] = (byte) sample;
                out[pos++] = (byte) (sample >> 8);
            }
        }
        return out;
    }

    private static int samples(double ms) {
        return (int) (RATE * ms / 1000);
    }
}
