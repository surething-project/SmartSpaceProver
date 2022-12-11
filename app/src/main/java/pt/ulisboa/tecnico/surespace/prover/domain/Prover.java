/*
 * Copyright (C) 2020 The SureThing project
 * @author João Tiago <joao.marques.tiago@tecnico.ulisboa.pt>
 * http://surething.tecnico.ulisboa.pt/en/
 */

package pt.ulisboa.tecnico.surespace.prover.domain;

import static android.util.Patterns.EMAIL_ADDRESS;
import static pt.ulisboa.tecnico.surespace.common.domain.Entity.PATH_PREFIX;
import static pt.ulisboa.tecnico.surespace.prover.manager.ProverKeyStoreManager.correctKeyStoreCredentials;

import org.apache.commons.lang3.StringUtils;
import pt.ulisboa.tecnico.surespace.common.domain.Entity;
import pt.ulisboa.tecnico.surespace.common.domain.exception.ObjectException;
import pt.ulisboa.tecnico.surespace.common.manager.exception.EntityManagerException;
import pt.ulisboa.tecnico.surespace.common.manager.exception.KeyStoreManagerException;
import pt.ulisboa.tecnico.surespace.prover.MainActivity;
import pt.ulisboa.tecnico.surespace.prover.domain.exception.ProverException;
import pt.ulisboa.tecnico.surespace.prover.manager.ProverManager;
import pt.ulisboa.tecnico.surespace.prover.manager.ProverManager.ProverManagerInitializer;

public final class Prover {
  private final ProverManager manager;

  private Prover(ProverManager manager) {
    this.manager = manager;
  }

  public static Entity createEntity(String name) throws ObjectException {
    validateName(name);
    return new Entity(name, PATH_PREFIX + "rca/ltca/" + name);
  }

  private static Prover createProver(
      ProverManagerInitializer bundle, Entity prover, String password) throws ProverException {
    try {
      // The authentication was successful, so we create the manager.
      return new Prover(new ProverManager(bundle, prover, password));

    } catch (KeyStoreManagerException | EntityManagerException e) {
      e.printStackTrace();
      throw new ProverException("Could not prepare the environment: %s", e.getMessage());
    }
  }

  public static Prover login(MainActivity activity, String name, String password)
      throws ProverException, ObjectException {
    Entity prover = createEntity(name);
    validatePassword(password);
    ProverManagerInitializer bundle = new ProverManagerInitializer(activity);

    // Check if the provided credentials are valid.
    if (!correctKeyStoreCredentials(bundle.propertyManager(), prover, password))
      throw new ProverException("Invalid credentials");

    return createProver(bundle, prover, password);
  }

  public static Prover register(MainActivity activity, String name, String password)
      throws ProverException, ObjectException {
    return createProver(new ProverManagerInitializer(activity), createEntity(name), password);
  }

  private static void validateName(String name) {
    if (StringUtils.isBlank(name))
      throw new IllegalArgumentException("Blank email addresses are not allowed");

    if (!EMAIL_ADDRESS.matcher(name).matches())
      throw new IllegalArgumentException("'" + name + "' is not a valid email address");
  }

  private static void validatePassword(String password) {
    if (StringUtils.isBlank(password) || password.length() < 3 || password.length() > 10)
      throw new IllegalArgumentException("The password must be between 3 and 10 characters");
  }

  public ProverManager manager() {
    return manager;
  }
}
