/*
 * Copyright (C) 2020 The SureThing project
 * @author João Tiago <joao.marques.tiago@tecnico.ulisboa.pt>
 * http://surething.tecnico.ulisboa.pt/en/
 */

package pt.ulisboa.tecnico.surespace.prover.manager;

import android.app.Activity;
import pt.ulisboa.tecnico.surespace.common.manager.LogManagerInterface;
import pt.ulisboa.tecnico.surespace.common.manager.PropertyManager;
import pt.ulisboa.tecnico.surespace.common.manager.exception.PropertyManagerException;
import pt.ulisboa.tecnico.surespace.prover.R;

public final class ProverPropertyManager extends PropertyManager {
  private final Activity activity;

  public ProverPropertyManager(LogManagerInterface logManager, Activity activity)
      throws PropertyManagerException {
    super(logManager);
    this.activity = activity;
    beforeLoading();
  }

  public Activity activity() {
    return activity;
  }

  @Override
  public void beforeLoading() throws PropertyManagerException {
    super.beforeLoading();
    extend(activity.getResources().openRawResource(R.raw.prover));
  }
}
