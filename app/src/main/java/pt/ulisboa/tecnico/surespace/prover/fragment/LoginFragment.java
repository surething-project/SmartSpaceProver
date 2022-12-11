/*
 * Copyright (C) 2020 The SureThing project
 * @author João Tiago <joao.marques.tiago@tecnico.ulisboa.pt>
 * http://surething.tecnico.ulisboa.pt/en/
 */

package pt.ulisboa.tecnico.surespace.prover.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import pt.ulisboa.tecnico.surespace.prover.MainActivity;
import pt.ulisboa.tecnico.surespace.prover.R;
import pt.ulisboa.tecnico.surespace.prover.task.LoginTask;
import pt.ulisboa.tecnico.surespace.prover.task.RegisterTask;

public final class LoginFragment extends ActivityFragment {
  private EditText loginEmail;
  private EditText loginPassword;

  public LoginFragment(@NonNull MainActivity activity, @NonNull Bundle bundle) {
    super(activity, bundle);
  }

  private void attemptLogin(View view) {
    new Thread(new LoginTask(this)).start();
  }

  private void attemptRegister(View view) {
    new Thread(new RegisterTask(this)).start();
  }

  public String getEmail() {
    return loginEmail.getText().toString();
  }

  public String getPassword() {
    return loginPassword.getText().toString();
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_login, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    loginEmail = view.findViewById(R.id.fragment_login_email);
    loginPassword = view.findViewById(R.id.fragment_login_password);

    view.findViewById(R.id.fragment_login_login).setOnClickListener(this::attemptLogin);
    view.findViewById(R.id.fragment_login_sign_up).setOnClickListener(this::attemptRegister);
  }
}
