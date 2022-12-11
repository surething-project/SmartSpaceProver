/*
 * Copyright (C) 2020 The SureThing project
 * @author João Tiago <joao.marques.tiago@tecnico.ulisboa.pt>
 * http://surething.tecnico.ulisboa.pt/en/
 */

package pt.ulisboa.tecnico.surespace.prover.task;

import androidx.annotation.NonNull;
import pt.ulisboa.tecnico.surespace.prover.fragment.ActivityFragment;

public abstract class Task<Fragment extends ActivityFragment> implements Runnable {
  protected final Fragment fragment;

  public Task(@NonNull Fragment fragment) {
    this.fragment = fragment;
  }

  @NonNull
  public final Fragment fragment() {
    return fragment;
  }
}
