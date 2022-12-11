/*
 * Copyright (C) 2020 The SureThing project
 * @author João Tiago <joao.marques.tiago@tecnico.ulisboa.pt>
 * http://surething.tecnico.ulisboa.pt/en/
 */

package pt.ulisboa.tecnico.surespace.prover.task;

import androidx.annotation.NonNull;
import pt.ulisboa.tecnico.surespace.common.async.AsyncListener;
import pt.ulisboa.tecnico.surespace.common.domain.Entity;
import pt.ulisboa.tecnico.surespace.common.exception.BroadException;
import pt.ulisboa.tecnico.surespace.common.manager.exception.KeyStoreManagerException;
import pt.ulisboa.tecnico.surespace.common.message.MessageValidator;
import pt.ulisboa.tecnico.surespace.common.message.SignedMessageValidator;
import pt.ulisboa.tecnico.surespace.common.message.exception.MessageValidatorException;
import pt.ulisboa.tecnico.surespace.common.proof.LocationProof;
import pt.ulisboa.tecnico.surespace.prover.fragment.ProofFragment;
import pt.ulisboa.tecnico.surespace.prover.manager.ProverManager;
import pt.ulisboa.tecnico.surespace.verifier.client.VerifierClientException;
import pt.ulisboa.tecnico.surespace.verifier.message.SignedVerifyProofRequest;
import pt.ulisboa.tecnico.surespace.verifier.message.SignedVerifyProofRequest.VerifyProofRequest;
import pt.ulisboa.tecnico.surespace.verifier.message.SignedVerifyProofResponse;

public final class VerifyProofTask extends Task<ProofFragment> {
  private final ProverManager manager;
  private final Entity verifier;
  private final Entity prover;
  private final LocationProof proof;
  private final AsyncListener<SignedVerifyProofResponse, BroadException> listener;

  public VerifyProofTask(
      @NonNull ProofFragment fragment,
      AsyncListener<SignedVerifyProofResponse, BroadException> listener,
      LocationProof proof) {
    super(fragment);

    this.listener = listener;
    this.manager = fragment.activity.getProver().manager();
    this.proof = proof;
    this.prover = fragment.activity.getProverEntity();
    this.verifier = fragment.activity.getProver().manager().getVerifier();
  }

  private SignedVerifyProofRequest buildRequest() throws KeyStoreManagerException {
    return SignedVerifyProofRequest.newBuilder()
        .setMessage(
            VerifyProofRequest.newBuilder()
                .setSender(prover)
                .setReceiver(verifier)
                .setCertificateBytes(manager.keyStore())
                .setNonce(manager.nonce())
                .setLocationProof(proof)
                .build())
        .setSignature(manager.keyStore())
        .build();
  }

  private void sendRequest(SignedVerifyProofRequest request) {
    manager
        .getVerifierClient()
        .verifyProof(
            request,
            new AsyncListener<SignedVerifyProofResponse, VerifierClientException>() {
              @Override
              public void onComplete(SignedVerifyProofResponse response) {
                processResponse(response);
              }

              @Override
              public void onError(VerifierClientException e) {
                e.printStackTrace();
                listener.onError(e);
              }
            });
  }

  private void processResponse(SignedVerifyProofResponse response) {
    try {
      validateResponse(response);
      listener.onComplete(response);

    } catch (MessageValidatorException e) {
      e.printStackTrace();
      listener.onError(e);

    } catch (Exception e) {
      e.printStackTrace();
      listener.onError(new BroadException(e.getMessage()));
    }
  }

  private void validateResponse(SignedVerifyProofResponse response)
      throws MessageValidatorException {

    new MessageValidator(manager)
        .init(response.getMessage())
        .assertSender(verifier)
        .assertReceiver(prover)
        .assertCertificateValid()
        .assertNonceValid()
        .validate();

    new SignedMessageValidator(manager).init(response).assertSignature().validate();
  }

  @Override
  public void run() {
    try {
      sendRequest(buildRequest());

    } catch (KeyStoreManagerException e) {
      e.printStackTrace();
      listener.onError(e);

    } catch (Exception e) {
      e.printStackTrace();
      listener.onError(new BroadException(e.getMessage()));
    }
  }
}
