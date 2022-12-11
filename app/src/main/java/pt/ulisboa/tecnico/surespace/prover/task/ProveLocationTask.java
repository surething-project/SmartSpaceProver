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
import pt.ulisboa.tecnico.surespace.common.message.SignedProveLocationRequest;
import pt.ulisboa.tecnico.surespace.common.message.SignedProveLocationRequest.ProveLocationRequest;
import pt.ulisboa.tecnico.surespace.common.message.SignedProveLocationResponse;
import pt.ulisboa.tecnico.surespace.common.message.exception.MessageValidatorException;
import pt.ulisboa.tecnico.surespace.orchestrator.client.OrchestratorClientException;
import pt.ulisboa.tecnico.surespace.prover.fragment.RequestAuthorizationFragment;
import pt.ulisboa.tecnico.surespace.prover.manager.ProverManager;

public final class ProveLocationTask extends Task<RequestAuthorizationFragment> {
  private final AsyncListener<Void, BroadException> listener;
  private final ProverManager manager;
  private final Entity orchestrator;
  private final Entity prover;

  public ProveLocationTask(
      @NonNull RequestAuthorizationFragment fragment,
      AsyncListener<Void, BroadException> listener) {
    super(fragment);

    this.listener = listener;
    this.manager = fragment.activity.getProver().manager();
    this.prover = fragment.activity.getProverEntity();
    this.orchestrator = manager.getOrchestrator();
  }

  private ProveLocationRequest buildProveLocationRequest() throws BroadException {
    return ProveLocationRequest.newBuilder()
        .setSignedRequestAuthorizationResponse(fragment.getSignedAuthorization())
        .setSender(prover)
        .setCertificateBytes(manager.keyStore())
        .setReceiver(orchestrator)
        .setNonce(manager.nonce())
        .build();
  }

  private SignedProveLocationRequest buildSignedProveLocationRequest(ProveLocationRequest request)
      throws KeyStoreManagerException {
    return SignedProveLocationRequest.newBuilder()
        .setMessage(request)
        .setSignature(manager.keyStore())
        .build();
  }

  @Override
  public final void run() {
    try {
      ProveLocationRequest req = buildProveLocationRequest();
      SignedProveLocationRequest signedReq = buildSignedProveLocationRequest(req);
      SignedProveLocationResponse signedRes = sendSignedStartRequest(signedReq);
      validateResponse(signedRes);

      listener.onComplete(null);

    } catch (BroadException e) {
      e.printStackTrace();
      listener.onError(e);
    }
  }

  private void validateResponse(SignedProveLocationResponse signedRes)
      throws MessageValidatorException {
    new MessageValidator(manager)
        .init(signedRes.getMessage())
        .assertReceiver(prover)
        .assertSender(orchestrator)
        .assertCertificateValid()
        .assertNonceValid()
        .validate();

    new SignedMessageValidator(manager).init(signedRes).assertSignature().validate();
  }

  private SignedProveLocationResponse sendSignedStartRequest(SignedProveLocationRequest request)
      throws OrchestratorClientException {
    return manager.getOrchestratorClient().proveLocation(request);
  }
}
