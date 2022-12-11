/*
 * Copyright (C) 2020 The SureThing project
 * @author João Tiago <joao.marques.tiago@tecnico.ulisboa.pt>
 * http://surething.tecnico.ulisboa.pt/en/
 */

package pt.ulisboa.tecnico.surespace.prover.manager;

import android.util.Log;
import pt.ulisboa.tecnico.surespace.common.manager.LogManager;
import pt.ulisboa.tecnico.surespace.common.manager.exception.LogManagerException;

public final class ProverLogManager extends LogManager {
  public ProverLogManager() throws LogManagerException {
    super();
  }

  @Override
  public void debug(Object o) {
    Log.d("Debug", String.valueOf(o));
  }

  @Override
  public void error(Object o) {
    Log.e("Error", String.valueOf(o));
  }

  @Override
  public void info(Object o) {
    Log.i("Info", String.valueOf(o));
  }

  @Override
  public void warning(Object o) {
    Log.w("Warn", String.valueOf(o));
  }
}
