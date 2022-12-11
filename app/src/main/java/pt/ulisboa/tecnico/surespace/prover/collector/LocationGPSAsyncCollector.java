/*
 * Copyright (C) 2020 The SureThing project
 * @author João Tiago <joao.marques.tiago@tecnico.ulisboa.pt>
 * http://surething.tecnico.ulisboa.pt/en/
 */

package pt.ulisboa.tecnico.surespace.prover.collector;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

import android.annotation.SuppressLint;
import android.location.Location;
import androidx.annotation.NonNull;
import com.google.android.gms.location.FusedLocationProviderClient;
import pt.ulisboa.tecnico.surespace.common.async.AsyncCollector;
import pt.ulisboa.tecnico.surespace.common.exception.BroadException;
import pt.ulisboa.tecnico.surespace.common.location.LocationGPS;
import pt.ulisboa.tecnico.surespace.common.location.exception.LocationException;
import pt.ulisboa.tecnico.surespace.prover.manager.ProverManager;

public final class LocationGPSAsyncCollector extends AsyncCollector<LocationGPS> {
  private final ProverManager manager;

  public LocationGPSAsyncCollector(ProverManager manager) {
    this.manager = manager;
  }

  @SuppressLint("MissingPermission")
  @Override
  protected void compute() {
    FusedLocationProviderClient client = getFusedLocationProviderClient(manager.activity());
    client
        .getLastLocation()
        .addOnSuccessListener(this::onSuccessListener)
        .addOnFailureListener(this::onFailureListener);
  }

  private void onFailureListener(@NonNull Exception e) {
    e.printStackTrace();

    if (e instanceof BroadException) cancel((BroadException) e);
    else if (e.getMessage() != null) cancel(new LocationException(e.getMessage()));
  }

  private void onSuccessListener(Location location) {
    if (location == null) cancel(new LocationException("Null location received"));
    else {
      double latitude = location.getLatitude();
      double longitude = location.getLongitude();
      float threshold = location.getAccuracy();

      // complete(new LocationGPS(latitude, longitude, threshold));
      complete(new LocationGPS(38.745914, -9.198570, threshold));
    }
  }
}
