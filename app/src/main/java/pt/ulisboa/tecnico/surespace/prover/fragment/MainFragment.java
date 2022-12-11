/*
 * Copyright (C) 2020 The SureThing project
 * @author João Tiago <joao.marques.tiago@tecnico.ulisboa.pt>
 * http://surething.tecnico.ulisboa.pt/en/
 */

package pt.ulisboa.tecnico.surespace.prover.fragment;

import static java.util.stream.Collectors.toList;
import static pt.ulisboa.tecnico.surespace.prover.MainActivity.FragmentTag.AUTHORIZATION;
import static pt.ulisboa.tecnico.surespace.prover.R.drawable.status_updating;
import static pt.ulisboa.tecnico.surespace.prover.R.id.fragment_main_request_authorization_button;
import static pt.ulisboa.tecnico.surespace.prover.R.id.fragment_main_user_details_name;
import static pt.ulisboa.tecnico.surespace.prover.R.id.label;
import static pt.ulisboa.tecnico.surespace.prover.R.layout.fragment_main;
import static pt.ulisboa.tecnico.surespace.prover.R.layout.fragment_main_listview;
import static pt.ulisboa.tecnico.surespace.prover.util.Utils.toast;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;
import pt.ulisboa.tecnico.surespace.common.async.AsyncListener;
import pt.ulisboa.tecnico.surespace.common.domain.Entity;
import pt.ulisboa.tecnico.surespace.common.exception.BroadException;
import pt.ulisboa.tecnico.surespace.common.manager.exception.EntityManagerException;
import pt.ulisboa.tecnico.surespace.common.message.SignedRequestAuthorizationResponse;
import pt.ulisboa.tecnico.surespace.prover.MainActivity;
import pt.ulisboa.tecnico.surespace.prover.R;
import pt.ulisboa.tecnico.surespace.prover.manager.ProverManager;
import pt.ulisboa.tecnico.surespace.prover.task.RequestAuthorizationTask;

public final class MainFragment extends ActivityFragment {
  private ListView connectionCardBodyList;
  private Button requestAuthorizationButton;
  private TextView userCardBodyName;

  public MainFragment(@NonNull MainActivity activity, @NonNull Bundle bundle) {
    super(activity, bundle);
  }

  private void initConnectionCard() {
    // This is the original list of entities the Prover knows.
    List<Entity> entities = manager().entity().list();
    try {
      // Remove self.
      entities.remove(manager().entity().current());

    } catch (EntityManagerException e) {
      manager().log().error("The Prover is not a known entity.");
    }

    // But each entity must be mapped to a string (consider its path).
    List<String> entitiesPath = entities.stream().map(Entity::getPath).collect(toList());

    // This adapter converts each item into a list entry.
    ArrayAdapter<String> arrayAdapter =
        new ArrayAdapter<>(activity, fragment_main_listview, label, entitiesPath);

    // Attach the adapter to the list view.
    connectionCardBodyList.setAdapter(arrayAdapter);
    connectionCardBodyList.setOnItemClickListener(
        (parent, view, position, id) -> {
          // Disable the entry.
          view.setEnabled(false);

          // Start waiting.
          final ImageView status = view.findViewById(R.id.status);
          status.setImageResource(status_updating);
        });
    connectionCardBodyList.setEnabled(false);
  }

  private void initRequestAuthorizationButton() {
    requestAuthorizationButton.setOnClickListener(this::requestAuthorization);
  }

  private void initUserCard() {
    userCardBodyName.setText(activity.getProverEntity().getName());
  }

  private ProverManager manager() {
    return activity.getProver().manager();
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(fragment_main, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    userCardBodyName = view.findViewById(fragment_main_user_details_name);
    initUserCard();

    requestAuthorizationButton = view.findViewById(fragment_main_request_authorization_button);
    initRequestAuthorizationButton();
  }

  private void requestAuthorization(View v) {
    activity.spinner(true);
    submit(new RequestAuthorizationTask(this, new ReceiveAuthorizationListener(activity)));
  }

  private static final class ReceiveAuthorizationListener
      implements AsyncListener<SignedRequestAuthorizationResponse, BroadException> {
    private final MainActivity activity;

    public ReceiveAuthorizationListener(MainActivity activity) {
      this.activity = activity;
    }

    @Override
    public void onComplete(SignedRequestAuthorizationResponse auth) {
      // Update fragment.
      final Bundle bundle = new Bundle();
      bundle.putSerializable("signedAuthorization", auth);
      activity.setFragment(AUTHORIZATION, bundle);
      activity.spinner(false);
    }

    @Override
    public void onError(BroadException e) {
      activity.spinner(false);
      toast(activity, e.getMessage());
    }
  }
}
