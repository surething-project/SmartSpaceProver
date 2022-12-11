/*
 * Copyright (C) 2020 The SureThing project
 * @author João Tiago <joao.marques.tiago@tecnico.ulisboa.pt>
 * http://surething.tecnico.ulisboa.pt/en/
 */

package pt.ulisboa.tecnico.surespace.prover.task;

import androidx.annotation.NonNull;
import java.util.ArrayList;
import pt.ulisboa.tecnico.surespace.common.async.AsyncListener;
import pt.ulisboa.tecnico.surespace.common.async.exception.AsyncException;
import pt.ulisboa.tecnico.surespace.common.domain.Entity;
import pt.ulisboa.tecnico.surespace.common.exception.BroadException;
import pt.ulisboa.tecnico.surespace.common.location.LocationGPS;
import pt.ulisboa.tecnico.surespace.common.location.LocationOLC;
import pt.ulisboa.tecnico.surespace.common.message.MessageValidator;
import pt.ulisboa.tecnico.surespace.common.message.SignedMessageValidator;
import pt.ulisboa.tecnico.surespace.common.message.SignedRequestAuthorizationRequest;
import pt.ulisboa.tecnico.surespace.common.message.SignedRequestAuthorizationRequest.RequestAuthorizationRequest;
import pt.ulisboa.tecnico.surespace.common.message.SignedRequestAuthorizationRequest.RequestAuthorizationRequest.RequestAuthorizationRequestBuilder;
import pt.ulisboa.tecnico.surespace.common.message.SignedRequestAuthorizationResponse;
import pt.ulisboa.tecnico.surespace.common.message.exception.MessageValidatorException;
import pt.ulisboa.tecnico.surespace.common.proof.Beacon;
import pt.ulisboa.tecnico.surespace.prover.collector.LocationGPSAsyncCollector;
import pt.ulisboa.tecnico.surespace.prover.collector.SupportedBeaconsAsyncCollector;
import pt.ulisboa.tecnico.surespace.prover.domain.exception.ProverException;
import pt.ulisboa.tecnico.surespace.prover.fragment.MainFragment;
import pt.ulisboa.tecnico.surespace.prover.manager.ProverManager;

public final class RequestAuthorizationTask extends Task<MainFragment> {
  private final AsyncListener<SignedRequestAuthorizationResponse, BroadException> listener;
  private final ProverManager manager;
  private final Entity orchestrator;
  private final Entity prover;

  public RequestAuthorizationTask(
      @NonNull MainFragment fragment,
      AsyncListener<SignedRequestAuthorizationResponse, BroadException> listener) {
    super(fragment);

    this.listener = listener;
    this.manager = fragment.activity.getProver().manager();
    this.prover = fragment.activity.getProverEntity();
    this.orchestrator = fragment.activity.getProver().manager().getOrchestrator();
  }

  private RequestAuthorizationRequest buildAuthRequest() throws BroadException {
    RequestAuthorizationRequestBuilder reqBuilder =
        RequestAuthorizationRequest.newBuilder()
            .setSender(prover)
            .setReceiver(orchestrator)
            .setNonce(manager.nonce())
            .setCertificateBytes(manager.keyStore());

    // Retrieve current location.
    //        reqBuilder.setLocation(new LocationOLCConverter().convert(computeLocation()));
    reqBuilder.setLocation(new LocationOLC("8CCGPRW2+9HC"));

    // Discover supported beacons.
    ArrayList<Beacon> beacons = discoverSupportedBeacons();
    reqBuilder.setSupportedBeacons(beacons);

    return reqBuilder.build();
  }

  private SignedRequestAuthorizationRequest buildSignedAuthRequest(
      RequestAuthorizationRequest request) throws BroadException {
    return SignedRequestAuthorizationRequest.newBuilder()
        .setMessage(request)
        .setSignature(manager.keyStore())
        .build();
  }

  private LocationGPS computeLocation() throws ProverException {
    try {
      return new LocationGPSAsyncCollector(manager).start().get();

    } catch (AsyncException e) {
      throw new ProverException(e.getMessage());
    }
  }

  private ArrayList<Beacon> discoverSupportedBeacons() throws ProverException {
    try {
      return new SupportedBeaconsAsyncCollector(fragment().activity).start().get();

    } catch (AsyncException e) {
      throw new ProverException(e.getMessage());
    }
  }

  @Override
  public void run() {
    try {
      RequestAuthorizationRequest req = buildAuthRequest();
      SignedRequestAuthorizationRequest signedReq = buildSignedAuthRequest(req);
      SignedRequestAuthorizationResponse signedAuth = sendSignedAuthRequest(signedReq);
      validateResponse(signedReq, signedAuth);

      listener.onComplete(signedAuth);

    } catch (BroadException e) {
      e.printStackTrace();
      listener.onError(e);

    } catch (Exception e) {
      e.printStackTrace();
      listener.onError(new BroadException(e.getMessage()));
    }
  }

  private SignedRequestAuthorizationResponse sendSignedAuthRequest(
      SignedRequestAuthorizationRequest signedRequest) throws BroadException {
    return manager.getOrchestratorClient().requestAuthorization(signedRequest);
  }

  private void validateResponse(
      SignedRequestAuthorizationRequest signedRequest,
      SignedRequestAuthorizationResponse signedResponse)
      throws BroadException {

    new MessageValidator(manager)
        .init(signedResponse.getMessage())
        .assertReceiver(prover)
        .assertSender(orchestrator)
        .assertCertificateValid()
        .assertNonceValid()
        .validate();

    new SignedMessageValidator(manager).init(signedResponse).assertSignature().validate();

    if (!signedRequest.equals(signedResponse.getMessage().getSignedRequestAuthorizationRequest()))
      throw new MessageValidatorException("Authorization for a different request");
  }
}
