/*
 * Copyright (C) 2020 The SureThing project
 * @author João Tiago <joao.marques.tiago@tecnico.ulisboa.pt>
 * http://surething.tecnico.ulisboa.pt/en/
 */

package pt.ulisboa.tecnico.surespace.prover;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static pt.ulisboa.tecnico.surespace.prover.MainActivity.FragmentTag.LOGIN;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import pt.ulisboa.tecnico.surespace.common.domain.Entity;
import pt.ulisboa.tecnico.surespace.common.manager.exception.EntityManagerException;
import pt.ulisboa.tecnico.surespace.prover.domain.Prover;
import pt.ulisboa.tecnico.surespace.prover.fragment.ActivityFragment;
import pt.ulisboa.tecnico.surespace.prover.fragment.LoginFragment;
import pt.ulisboa.tecnico.surespace.prover.fragment.MainFragment;
import pt.ulisboa.tecnico.surespace.prover.fragment.ProofFragment;
import pt.ulisboa.tecnico.surespace.prover.fragment.RequestAuthorizationFragment;

public final class MainActivity extends AppCompatActivity {
  private static final String[] PERMISSIONS =
      new String[] {ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, RECORD_AUDIO};
  private static final int PERMISSIONS_REQUEST = 200;
  private final FragmentManager fragmentManager = getSupportFragmentManager();
  private View container;
  private Prover prover;
  private Entity proverEntity;
  private View spinner;

  public boolean dismissKeyboardOnClick(View view) {
    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
    View currentFocus;

    if (imm != null && (currentFocus = getCurrentFocus()) != null)
      imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);

    return view.performClick();
  }

  private void enableViewHierarchy(View view, boolean enabled) {
    view.setEnabled(enabled);
    if (view instanceof ViewGroup) {
      ViewGroup viewGroup = (ViewGroup) view;
      for (int i = 0; i < viewGroup.getChildCount(); i++) {
        View child = viewGroup.getChildAt(i);
        enableViewHierarchy(child, enabled);
      }
    }
  }

  private ActivityFragment getFragmentFromTag(@NonNull FragmentTag tag, @NonNull Bundle bundle) {
    switch (tag) {
      case MAIN:
        return new MainFragment(this, bundle);

      case LOGIN:
        return new LoginFragment(this, bundle);

      case AUTHORIZATION:
        return new RequestAuthorizationFragment(this, bundle);

      case PROOF:
        return new ProofFragment(this, bundle);

      default:
        return null;
    }
  }

  public Prover getProver() {
    return prover;
  }

  public void setProver(Prover prover) {
    this.prover = prover;
    try {
      proverEntity = prover.manager().entity().current();

    } catch (EntityManagerException e) {
      e.printStackTrace();
    }
  }

  public Entity getProverEntity() {
    return proverEntity.clone();
  }

  private void launchNoPermissionsActivity() {
    startActivity(new Intent(this, NoPermissionsActivity.class));
    finish();
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    spinner = findViewById(R.id.loader);
    container = findViewById(R.id.container);

    // Enable spinner.
    setFragment(LOGIN);
    spinner(true);

    // Request required permissions.
    requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST);
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode == PERMISSIONS_REQUEST) {
      if (grantResults.length == PERMISSIONS.length) {
        for (int grantResult : grantResults) {
          if (grantResult != PERMISSION_GRANTED) {
            launchNoPermissionsActivity();
            break;
          }
        }

        // All permissions were granted.
        spinner(false);

      } else launchNoPermissionsActivity();

    } else super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  public void setFragment(FragmentTag tag, @NonNull Bundle bundle) {
    runOnUiThread(
        () -> {
          ActivityFragment fragment = getFragmentFromTag(tag, bundle);
          if (fragment == null) return;

          spinner(true);
          fragmentManager
              .beginTransaction()
              .replace(R.id.container, fragment, tagToString(tag))
              .commitNow();

          spinner(false);
        });
  }

  public void setFragment(FragmentTag tag) {
    setFragment(tag, new Bundle());
  }

  public void spinner(boolean enabled) {
    runOnUiThread(
        () -> {
          spinner.setVisibility(enabled ? VISIBLE : GONE);
          enableViewHierarchy(container, !enabled);
        });
  }

  private String tagToString(@NonNull FragmentTag tag) {
    return tag.name().toUpperCase();
  }

  public enum FragmentTag {
    LOGIN,
    MAIN,
    AUTHORIZATION,
    PROOF
  }
}
