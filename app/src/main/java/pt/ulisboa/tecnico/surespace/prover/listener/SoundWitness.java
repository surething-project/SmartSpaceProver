/*
 * Copyright (C) 2020 The SureThing project
 * @author João Tiago <joao.marques.tiago@tecnico.ulisboa.pt>
 * http://surething.tecnico.ulisboa.pt/en/
 */

package pt.ulisboa.tecnico.surespace.prover.listener;

import static android.media.AudioRecord.ERROR;
import static android.media.AudioRecord.ERROR_BAD_VALUE;
import static android.media.MediaRecorder.AudioSource.MIC;
import static android.os.Process.setThreadPriority;
import static java.lang.Math.abs;

import android.media.AudioFormat;
import android.media.AudioRecord;
import pt.ulisboa.tecnico.surespace.common.proof.Beacon;
import pt.ulisboa.tecnico.surespace.common.signal.property.Amplitude;
import pt.ulisboa.tecnico.surespace.prover.MainActivity;

public final class SoundWitness extends UntrustedWitness {
  private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
  private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
  private static final int SAMPLE_RATE = 44100;
  private final Amplitude amplitude = new Amplitude();
  private AudioRecord audioRecord;
  private int bufferSize;

  public SoundWitness(Beacon beacon, MainActivity activity) {
    super(beacon, activity);
    updateBufferSize();
  }

  private double calculateAverage(short[] buffer, int readSize) {
    double sum = 0.0;
    for (int i = 0; i < readSize; i++) sum += abs(buffer[i]);
    return sum / (1.0 * readSize);
  }

  private double getAmplitude() {
    short[] buffer = new short[bufferSize];
    return calculateAverage(buffer, audioRecord.read(buffer, 0, bufferSize));
  }

  @Override
  protected void handleStart() {
    setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

    audioRecord = new AudioRecord(MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize);
    audioRecord.startRecording();

    // Start reading data in a new thread.
    new Thread(new SoundWitnessRunnable()).start();
  }

  @Override
  protected void handleStop() {
    audioRecord.stop();
  }

  @Override
  public boolean prepare() {
    return true;
  }

  private void updateBufferSize() {
    int size = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
    if (size == ERROR || size == ERROR_BAD_VALUE)
      throw new RuntimeException("Invalid min buffer size.");

    bufferSize = size;
  }

  private final class SoundWitnessRunnable implements Runnable {
    @Override
    public void run() {
      setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

      while (true) {
        synchronized (SoundWitness.this) {
          if (mustListen()) {
            addReading(amplitude, Double.toString(getAmplitude()));
          } else break;
        }
      }
    }
  }
}
