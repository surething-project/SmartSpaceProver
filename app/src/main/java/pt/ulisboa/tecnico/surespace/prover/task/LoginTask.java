/*
 * Copyright (C) 2020 The SureThing project
 * @author João Tiago <joao.marques.tiago@tecnico.ulisboa.pt>
 * http://surething.tecnico.ulisboa.pt/en/
 */

package pt.ulisboa.tecnico.surespace.prover.task;

import static pt.ulisboa.tecnico.surespace.prover.MainActivity.FragmentTag.MAIN;
import static pt.ulisboa.tecnico.surespace.prover.domain.Prover.login;
import static pt.ulisboa.tecnico.surespace.prover.util.Utils.toast;

import androidx.annotation.NonNull;
import pt.ulisboa.tecnico.surespace.prover.MainActivity;
import pt.ulisboa.tecnico.surespace.prover.domain.Prover;
import pt.ulisboa.tecnico.surespace.prover.fragment.LoginFragment;

public final class LoginTask extends Task<LoginFragment> {
  public LoginTask(@NonNull LoginFragment fragment) {
    super(fragment);
  }

  @Override
  public void run() {
    MainActivity activity = fragment.activity;
    activity.spinner(true);

    try {
      Prover prover = login(activity, fragment().getEmail(), fragment().getPassword());
      activity.setProver(prover);
      activity.setFragment(MAIN);

    } catch (Exception e) {
      activity.spinner(false);
      toast(activity, e.getMessage());
    }
  }
}
