/*
 * Copyright (C) 2020 The SureThing project
 * @author João Tiago <joao.marques.tiago@tecnico.ulisboa.pt>
 * http://surething.tecnico.ulisboa.pt/en/
 */

package pt.ulisboa.tecnico.surespace.prover.fragment;

import static java.lang.String.format;
import static java.util.Locale.getDefault;
import static pt.ulisboa.tecnico.surespace.prover.MainActivity.FragmentTag.MAIN;
import static pt.ulisboa.tecnico.surespace.prover.MainActivity.FragmentTag.PROOF;
import static pt.ulisboa.tecnico.surespace.prover.R.id.fragment_authorization_authorization_details_duration_value;
import static pt.ulisboa.tecnico.surespace.prover.R.id.fragment_authorization_authorization_details_id_value;
import static pt.ulisboa.tecnico.surespace.prover.R.id.fragment_authorization_authorization_details_selected_fragment_count_value;
import static pt.ulisboa.tecnico.surespace.prover.R.id.fragment_authorization_authorization_details_selected_fragment_length_value;
import static pt.ulisboa.tecnico.surespace.prover.R.id.fragment_authorization_authorization_details_selected_producers_value;
import static pt.ulisboa.tecnico.surespace.prover.R.id.fragment_authorization_authorization_details_valid_until_value;
import static pt.ulisboa.tecnico.surespace.prover.R.id.fragment_authorization_prove_location_button;
import static pt.ulisboa.tecnico.surespace.prover.R.layout.fragment_authorization;
import static pt.ulisboa.tecnico.surespace.prover.util.Utils.timestampToString;
import static pt.ulisboa.tecnico.surespace.prover.util.Utils.toast;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.stream.Collectors;
import pt.ulisboa.tecnico.surespace.common.async.AsyncListener;
import pt.ulisboa.tecnico.surespace.common.exception.BroadException;
import pt.ulisboa.tecnico.surespace.common.message.SignedRequestAuthorizationResponse;
import pt.ulisboa.tecnico.surespace.common.message.SignedRequestAuthorizationResponse.RequestAuthorizationResponse;
import pt.ulisboa.tecnico.surespace.common.proof.Device;
import pt.ulisboa.tecnico.surespace.common.proof.LocationProofProperties;
import pt.ulisboa.tecnico.surespace.prover.MainActivity;
import pt.ulisboa.tecnico.surespace.prover.task.ProveLocationTask;

public final class RequestAuthorizationFragment extends ActivityFragment {
  private final SignedRequestAuthorizationResponse signedAuthorization;
  private TextView duration;
  private TextView fragmentCount;
  private TextView fragmentLength;
  private TextView id;
  private TextView selectedBeacons;
  private TextView validUntil;

  public RequestAuthorizationFragment(@NonNull MainActivity activity, @NonNull Bundle bundle) {
    super(activity, bundle);
    signedAuthorization =
        (SignedRequestAuthorizationResponse) bundle.getSerializable("signedAuthorization");
  }

  public SignedRequestAuthorizationResponse getSignedAuthorization() {
    return signedAuthorization.clone();
  }

  private void initAuthorizationCard() {
    final RequestAuthorizationResponse auth = signedAuthorization.getMessage();
    final LocationProofProperties properties = auth.getProperties();

    id.setText(properties.getIdentifier()); // Update identifier.

    int duration = properties.getFragmentCount() * properties.getFragmentLength();
    this.duration.setText(format(getDefault(), "%d ms", duration)); // Update duration.

    validUntil.setText(timestampToString(auth.getValidity().getNotAfter())); // Update validity.
    selectedBeacons.setText(
        auth.getSelectedBeacons().stream()
            .map(Device::getIdentifier)
            .collect(Collectors.joining(", "))); // Update beacons.

    // fragmentCount.setText(properties.getFragmentCount()); // Update fragment count.
    // fragmentLength.setText(properties.getFragmentLength()); // Update fragment length.
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(fragment_authorization, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    id = view.findViewById(fragment_authorization_authorization_details_id_value);
    duration = view.findViewById(fragment_authorization_authorization_details_duration_value);
    validUntil = view.findViewById(fragment_authorization_authorization_details_valid_until_value);
    selectedBeacons =
        view.findViewById(fragment_authorization_authorization_details_selected_producers_value);
    fragmentCount =
        view.findViewById(
            fragment_authorization_authorization_details_selected_fragment_count_value);
    fragmentLength =
        view.findViewById(
            fragment_authorization_authorization_details_selected_fragment_length_value);
    initAuthorizationCard();

    view.findViewById(fragment_authorization_prove_location_button)
        .setOnClickListener(this::proveLocation);
  }

  private void proveLocation(View view) {
    activity.spinner(true);
    submit(new ProveLocationTask(this, new ProveLocationListener(activity, bundle)));
  }

  private static final class ProveLocationListener implements AsyncListener<Void, BroadException> {
    private final MainActivity activity;
    private final Bundle bundle;

    public ProveLocationListener(MainActivity activity, Bundle bundle) {
      this.activity = activity;
      this.bundle = bundle;
    }

    @Override
    public void onComplete(Void aVoid) {
      activity.spinner(true);
      activity.setFragment(PROOF, bundle);
    }

    @Override
    public void onError(BroadException e) {
      activity.spinner(false);
      toast(activity, e.getMessage());
      activity.setFragment(MAIN, bundle);
    }
  }
}
