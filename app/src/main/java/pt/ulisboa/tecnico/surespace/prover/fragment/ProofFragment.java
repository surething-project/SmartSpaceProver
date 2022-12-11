/*
 * Copyright (C) 2020 The SureThing project
 * @author João Tiago <joao.marques.tiago@tecnico.ulisboa.pt>
 * http://surething.tecnico.ulisboa.pt/en/
 */

package pt.ulisboa.tecnico.surespace.prover.fragment;

import static pt.ulisboa.tecnico.surespace.prover.MainActivity.FragmentTag.MAIN;
import static pt.ulisboa.tecnico.surespace.prover.proof.ConstantPool.LIGHT_BEACON_IDENTIFIER;
import static pt.ulisboa.tecnico.surespace.prover.proof.ConstantPool.SOUND_BEACON_IDENTIFIER;
import static pt.ulisboa.tecnico.surespace.prover.util.Utils.toast;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import pt.ulisboa.tecnico.surespace.common.async.AsyncListener;
import pt.ulisboa.tecnico.surespace.common.exception.BroadException;
import pt.ulisboa.tecnico.surespace.common.message.SignedRequestAuthorizationResponse;
import pt.ulisboa.tecnico.surespace.common.proof.Beacon;
import pt.ulisboa.tecnico.surespace.common.proof.LocationProof;
import pt.ulisboa.tecnico.surespace.common.proof.LocationProofProperties;
import pt.ulisboa.tecnico.surespace.prover.MainActivity;
import pt.ulisboa.tecnico.surespace.prover.R;
import pt.ulisboa.tecnico.surespace.prover.listener.LightWitness;
import pt.ulisboa.tecnico.surespace.prover.listener.SoundWitness;
import pt.ulisboa.tecnico.surespace.prover.listener.UntrustedWitness;
import pt.ulisboa.tecnico.surespace.prover.task.VerifyProofTask;
import pt.ulisboa.tecnico.surespace.verifier.message.SignedVerifyProofResponse;

public final class ProofFragment extends ActivityFragment {
  private SignedRequestAuthorizationResponse signedAuthorization;

  public ProofFragment(@NonNull MainActivity activity, @NonNull Bundle bundle) {
    super(activity, bundle);
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_proof, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    activity.spinner(true);

    Serializable serialized = bundle.getSerializable("signedAuthorization");
    if (serialized == null) {
      toast(activity, "No authorization was found");
      activity.setFragment(MAIN, bundle);

    } else {
      signedAuthorization = (SignedRequestAuthorizationResponse) serialized;
      new Thread(this::handleProof).start();
    }
  }

  private void handleProof() {
    ArrayList<UntrustedWitness> witnesses = createWitnesses();

    // If witnesses cannot be prepared, the proof of location cannot proceed.
    if (!prepareWitnesses(witnesses)) {
      toast(activity, "Could not prepare the required witnesses");
      activity.setFragment(MAIN, bundle);

    } else {
      // Start witnesses.
      for (UntrustedWitness witness : witnesses) witness.start();
      // Wait for proof completion.
      waitForProofCompletion();
      // Stop all witnesses.
      for (UntrustedWitness witness : witnesses) witness.stop();

      // Generate a location proof.
      LocationProof proof = new LocationProof(signedAuthorization);
      for (UntrustedWitness witness : witnesses) proof.addSignal(witness.getSignal());

      // Submit it for assessment.
      submit(new VerifyProofTask(this, new VerifyProofListener(activity, bundle), proof));
    }
  }

  private ArrayList<UntrustedWitness> createWitnesses() {
    ArrayList<Beacon> selectedBeacons = signedAuthorization.getMessage().getSelectedBeacons();
    ArrayList<UntrustedWitness> witnesses = new ArrayList<>();

    for (Beacon beacon : selectedBeacons) {
      switch (beacon.getIdentifier()) {
        case LIGHT_BEACON_IDENTIFIER:
          witnesses.add(new LightWitness(beacon, activity));
          break;

        case SOUND_BEACON_IDENTIFIER:
          witnesses.add(new SoundWitness(beacon, activity));
          break;
      }
    }

    return witnesses;
  }

  private boolean prepareWitnesses(ArrayList<UntrustedWitness> witnesses) {
    for (UntrustedWitness witness : witnesses) if (!witness.prepare()) return false;
    return true;
  }

  private void waitForProofCompletion() {
    LocationProofProperties properties = signedAuthorization.getMessage().getProperties();
    long duration = properties.getFragmentCount() * properties.getFragmentLength();

    try {
      TimeUnit.MILLISECONDS.sleep(duration);

    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private static final class VerifyProofListener
      implements AsyncListener<SignedVerifyProofResponse, BroadException> {
    private final MainActivity activity;
    private final Bundle bundle;

    public VerifyProofListener(MainActivity activity, Bundle bundle) {
      this.activity = activity;
      this.bundle = bundle;
    }

    @Override
    public void onComplete(SignedVerifyProofResponse signedVerifyProofResponse) {
      activity.spinner(false);

      // Get the result of the evaluation of the location proof.
      if (signedVerifyProofResponse.getMessage().isProofAccepted())
        toast(activity, "Your location proof was accepted!");
      else toast(activity, "Your location proof was rejected.");

      // Back to the main fragment.
      activity.setFragment(MAIN, bundle);
    }

    @Override
    public void onError(BroadException e) {
      activity.spinner(false);
      toast(activity, e.getMessage());
      activity.setFragment(MAIN, bundle);
    }
  }
}
