/*
 * Copyright (C) 2020 The SureThing project
 * @author João Tiago <joao.marques.tiago@tecnico.ulisboa.pt>
 * http://surething.tecnico.ulisboa.pt/en/
 */

package pt.ulisboa.tecnico.surespace.prover.listener;

import static java.time.Instant.now;

import java.util.concurrent.atomic.AtomicBoolean;
import pt.ulisboa.tecnico.surespace.common.proof.Beacon;
import pt.ulisboa.tecnico.surespace.common.proof.Witness;
import pt.ulisboa.tecnico.surespace.common.signal.Fragment;
import pt.ulisboa.tecnico.surespace.common.signal.Reading;
import pt.ulisboa.tecnico.surespace.common.signal.Signal;
import pt.ulisboa.tecnico.surespace.common.signal.property.Property;
import pt.ulisboa.tecnico.surespace.prover.MainActivity;

public abstract class UntrustedWitness {
  protected final MainActivity activity;
  protected final Signal signal;
  private final Fragment fragment;
  private final AtomicBoolean stopped = new AtomicBoolean(false);
  private final Beacon beacon;

  public UntrustedWitness(Beacon beacon, MainActivity activity) {
    this.beacon = beacon;
    this.activity = activity;

    signal = new Signal(beacon);
    fragment = new Fragment(1);
  }

  protected final void addReading(Property property, String value) {
    fragment.addReading(new Reading(new Witness(beacon), property, now().toEpochMilli(), value));
  }

  public final Signal getSignal() {
    return signal.clone();
  }

  protected abstract void handleStart();

  protected abstract void handleStop();

  protected final boolean mustListen() {
    return !stopped.get();
  }

  public abstract boolean prepare();

  public final void start() {
    stopped.set(false);
    handleStart();
  }

  public final void stop() {
    stopped.set(true);
    handleStop();

    signal.addFragment(fragment);
  }
}
