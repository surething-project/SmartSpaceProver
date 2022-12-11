/*
 * Copyright (C) 2020 The SureThing project
 * @author João Tiago <joao.marques.tiago@tecnico.ulisboa.pt>
 * http://surething.tecnico.ulisboa.pt/en/
 */

package pt.ulisboa.tecnico.surespace.prover.fragment;

import static java.util.concurrent.Executors.newCachedThreadPool;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import pt.ulisboa.tecnico.surespace.prover.MainActivity;

public abstract class ActivityFragment extends Fragment {
  public final MainActivity activity;
  protected final Bundle bundle;

  public ActivityFragment(@NonNull MainActivity activity, @NonNull Bundle bundle) {
    this.activity = activity;
    this.bundle = bundle;
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  @CallSuper
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    // Dismiss keyboard when needed.
    view.setOnTouchListener((v, event) -> activity.dismissKeyboardOnClick(view));
  }

  protected final void submit(Runnable task) {
    newCachedThreadPool().submit(task);
  }
}
