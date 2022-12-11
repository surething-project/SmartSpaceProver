/*
 * Copyright (C) 2020 The SureThing project
 * @author João Tiago <joao.marques.tiago@tecnico.ulisboa.pt>
 * http://surething.tecnico.ulisboa.pt/en/
 */

package pt.ulisboa.tecnico.surespace.prover.collector;

import static android.content.Context.SENSOR_SERVICE;
import static pt.ulisboa.tecnico.surespace.prover.proof.ConstantPool.LIGHT_BEACON_IDENTIFIER;
import static pt.ulisboa.tecnico.surespace.prover.proof.ConstantPool.SOUND_BEACON_IDENTIFIER;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import java.util.ArrayList;
import pt.ulisboa.tecnico.surespace.common.async.AsyncCollector;
import pt.ulisboa.tecnico.surespace.common.proof.Beacon;
import pt.ulisboa.tecnico.surespace.prover.MainActivity;

public final class SupportedBeaconsAsyncCollector extends AsyncCollector<ArrayList<Beacon>> {
  private final MainActivity activity;

  public SupportedBeaconsAsyncCollector(MainActivity activity) {
    this.activity = activity;
  }

  private void checkCompatibilityLightProducer(ArrayList<Beacon> beacons) {
    SensorManager service = (SensorManager) activity.getSystemService(SENSOR_SERVICE);
    if (service != null && service.getDefaultSensor(Sensor.TYPE_LIGHT) != null)
      beacons.add(new Beacon(LIGHT_BEACON_IDENTIFIER));
  }

  private void checkCompatibilitySoundProducer(ArrayList<Beacon> beacons) {
    beacons.add(new Beacon(SOUND_BEACON_IDENTIFIER));
  }

  @Override
  protected void compute() {
    ArrayList<Beacon> supportedBeacons = new ArrayList<>();
    checkCompatibilityLightProducer(supportedBeacons);
    checkCompatibilitySoundProducer(supportedBeacons);

    complete(supportedBeacons);
  }
}
