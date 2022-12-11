/*
 * Copyright (C) 2020 The SureThing project
 * @author João Tiago <joao.marques.tiago@tecnico.ulisboa.pt>
 * http://surething.tecnico.ulisboa.pt/en/
 */

package pt.ulisboa.tecnico.surespace.prover.manager;

import pt.ulisboa.tecnico.surespace.common.domain.Entity;
import pt.ulisboa.tecnico.surespace.common.manager.EntityManager;
import pt.ulisboa.tecnico.surespace.common.manager.GlobalManagerInterface;
import pt.ulisboa.tecnico.surespace.common.manager.NonceManager;
import pt.ulisboa.tecnico.surespace.common.manager.exception.EntityManagerException;
import pt.ulisboa.tecnico.surespace.common.manager.exception.KeyStoreManagerException;
import pt.ulisboa.tecnico.surespace.common.manager.exception.LogManagerException;
import pt.ulisboa.tecnico.surespace.common.manager.exception.PropertyManagerException;
import pt.ulisboa.tecnico.surespace.long_term_ca.client.LongTermCAClient;
import pt.ulisboa.tecnico.surespace.orchestrator.client.OrchestratorClient;
import pt.ulisboa.tecnico.surespace.prover.MainActivity;
import pt.ulisboa.tecnico.surespace.prover.domain.exception.ProverException;
import pt.ulisboa.tecnico.surespace.verifier.client.VerifierClient;

public final class ProverManager implements GlobalManagerInterface {
  private final EntityManager entityManager;
  private final ProverKeyStoreManager keyStoreManager;
  private final ProverLogManager logManager;
  private final LongTermCAClient ltcaClient;
  private final NonceManager nonceManager;
  private final Entity orchestrator;
  private final OrchestratorClient orchestratorClient;
  private final ProverPropertyManager propertyManager;
  private final Entity verifier;
  private final VerifierClient verifierClient;
  private transient MainActivity activity;

  public ProverManager(ProverManagerInitializer bundle, Entity prover, String password)
      throws KeyStoreManagerException, EntityManagerException {
    // Security.addProvider(new BouncyCastleProvider());
    activity(bundle.activity());

    this.logManager = bundle.logManager();
    this.propertyManager = bundle.propertyManager();

    // Get a LTCA client.
    String ltcaHost = propertyManager.get("ltca", "host").asString();
    int ltcaPort = propertyManager.get("ltca", "port").asInt();
    ltcaClient = new LongTermCAClient(ltcaHost, ltcaPort);

    // Register the Prover.
    entityManager = new EntityManager(propertyManager);
    entityManager.current(prover);

    // Get a client for Orchestrator 1.
    String orchestratorHost = propertyManager.get("orchestrator", "host").asString();
    int orchestratorPort = propertyManager.get("orchestrator", "port").asInt();
    orchestratorClient = new OrchestratorClient(orchestratorHost, orchestratorPort);

    String orchestratorPath = propertyManager.get("orchestrator", "path").asString();
    orchestrator = entityManager.getByPath(orchestratorPath);

    // Get a client for Verifier 1.
    String verifierHost = propertyManager.get("verifier", "host").asString();
    int verifierPort = propertyManager.get("verifier", "port").asInt();
    verifierClient = new VerifierClient(verifierHost, verifierPort);

    String verifierPath = propertyManager.get("verifier", "path").asString();
    verifier = entityManager.getByPath(verifierPath);

    // Update the password of the keystore.
    propertyManager.set("password", password);
    nonceManager = new NonceManager();

    // We must check if the keystore contains the private key of the Prover.
    // If it doesn't the Prover is not registered yet.
    keyStoreManager = new ProverKeyStoreManager(this);
    if (!keyStoreManager.containsKey(prover)) keyStoreManager.registerToLtca(prover);
  }

  public MainActivity activity() {
    return activity;
  }

  public void activity(final MainActivity activity) {
    this.activity = activity;
  }

  @Override
  public EntityManager entity() {
    return entityManager;
  }

  public LongTermCAClient getLtcaClient() {
    return ltcaClient;
  }

  public Entity getOrchestrator() {
    return orchestrator;
  }

  public OrchestratorClient getOrchestratorClient() {
    return orchestratorClient;
  }

  public Entity getVerifier() {
    return verifier;
  }

  public VerifierClient getVerifierClient() {
    return verifierClient;
  }

  @Override
  public ProverKeyStoreManager keyStore() {
    return keyStoreManager;
  }

  @Override
  public ProverLogManager log() {
    return logManager;
  }

  @Override
  public NonceManager nonce() {
    return nonceManager;
  }

  @Override
  public ProverPropertyManager property() {
    return propertyManager;
  }

  public static final class ProverManagerInitializer {
    private final MainActivity activity;
    private final ProverLogManager logManager;
    private final ProverPropertyManager propertyManager;

    public ProverManagerInitializer(MainActivity activity) throws ProverException {
      try {
        this.activity = activity;
        logManager = new ProverLogManager();
        propertyManager = new ProverPropertyManager(logManager, activity);

      } catch (PropertyManagerException | LogManagerException e) {
        throw new ProverException("Invalid properties were provided");
      }
    }

    public MainActivity activity() {
      return activity;
    }

    public ProverLogManager logManager() {
      return logManager;
    }

    public ProverPropertyManager propertyManager() {
      return propertyManager;
    }
  }
}
