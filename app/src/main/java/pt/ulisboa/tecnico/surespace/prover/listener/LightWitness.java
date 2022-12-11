/*
 * Copyright (C) 2020 The SureThing project
 * @author João Tiago <joao.marques.tiago@tecnico.ulisboa.pt>
 * http://surething.tecnico.ulisboa.pt/en/
 */

package pt.ulisboa.tecnico.surespace.prover.listener;

import static android.hardware.Sensor.TYPE_LIGHT;
import static android.hardware.SensorManager.SENSOR_DELAY_FASTEST;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import pt.ulisboa.tecnico.surespace.common.proof.Beacon;
import pt.ulisboa.tecnico.surespace.common.signal.property.Intensity;
import pt.ulisboa.tecnico.surespace.prover.MainActivity;

public final class LightWitness extends UntrustedWitness implements SensorEventListener {
  private final SensorManager manager;
  private final Sensor sensor;
  private final Intensity intensity = new Intensity();

  public LightWitness(Beacon beacon, MainActivity activity) {
    super(beacon, activity);

    manager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
    if (manager == null) sensor = null;
    else sensor = manager.getDefaultSensor(TYPE_LIGHT);
  }

  @Override
  protected void handleStart() {
    manager.registerListener(LightWitness.this, sensor, SENSOR_DELAY_FASTEST);
  }

  @Override
  protected void handleStop() {
    manager.unregisterListener(this);
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {}

  @Override
  public void onSensorChanged(SensorEvent event) {
    if (TYPE_LIGHT == event.sensor.getType()) {
      if (mustListen()) addReading(intensity, Float.toString(event.values[0]));
    }
  }

  @Override
  public boolean prepare() {
    return sensor != null;
  }
}
