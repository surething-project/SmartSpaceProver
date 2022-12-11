/*
 * Copyright (C) 2020 The SureThing project
 * @author João Tiago <joao.marques.tiago@tecnico.ulisboa.pt>
 * http://surething.tecnico.ulisboa.pt/en/
 */

package pt.ulisboa.tecnico.surespace.prover.util;

import static android.widget.Toast.LENGTH_LONG;

import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Locale;
import pt.ulisboa.tecnico.surespace.common.domain.Timestamp;
import pt.ulisboa.tecnico.surespace.prover.MainActivity;
import pt.ulisboa.tecnico.surespace.prover.manager.ProverManager;

public abstract class Utils {
  private static final SimpleDateFormat dateFormat =
      new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss", Locale.forLanguageTag("PT"));
  private static Toast toast;

  private static MainActivity activity(ProverManager m) {
    return m.activity();
  }

  public static String timestampToString(Timestamp timestamp) {
    return dateFormat.format(timestamp.toDate());
  }

  public static void toast(MainActivity activity, String text) {
    toast(activity, text, LENGTH_LONG);
  }

  public static void toast(MainActivity activity, String format, Object... args) {
    toast(activity, String.format(format, args));
  }

  public static void toast(ProverManager manager, String text) {
    toast(activity(manager), text);
  }

  public static void toast(MainActivity activity, String text, int duration) {
    activity.runOnUiThread(
        () -> {
          if (toast != null) toast.cancel();
          toast = Toast.makeText(activity, text, duration);
          toast.show();
        });
  }

  public static void toast(ProverManager manager, String string, int duration) {
    toast(activity(manager), string, duration);
  }
}
