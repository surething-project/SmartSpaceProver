/*
 * Copyright (C) 2020 The SureThing project
 * @author João Tiago <joao.marques.tiago@tecnico.ulisboa.pt>
 * http://surething.tecnico.ulisboa.pt/en/
 */

package pt.ulisboa.tecnico.surespace.prover.domain.exception;

import pt.ulisboa.tecnico.surespace.common.exception.BroadException;

public class ProverException extends BroadException {
  private static final long serialVersionUID = -8386704061322867003L;

  public ProverException(String format, Object... objects) {
    super(format, objects);
  }
}
