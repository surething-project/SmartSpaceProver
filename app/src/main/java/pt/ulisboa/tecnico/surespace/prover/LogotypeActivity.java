/*
 * Copyright (C) 2020 The SureThing project
 * @author João Tiago <joao.marques.tiago@tecnico.ulisboa.pt>
 * http://surething.tecnico.ulisboa.pt/en/
 */

package pt.ulisboa.tecnico.surespace.prover;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public final class LogotypeActivity extends AppCompatActivity {
  private AlphaAnimation createAlphaAnimation() {
    // Animation properties.
    AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
    alphaAnimation.setFillAfter(true);
    alphaAnimation.setDuration(1000);
    alphaAnimation.setAnimationListener(new LogotypeAnimation());

    return alphaAnimation;
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_opening);

    // Animate layout.
    findViewById(R.id.opening_wrapper).startAnimation(createAlphaAnimation());
  }

  private final class LogotypeAnimation implements AnimationListener {
    @Override
    public void onAnimationEnd(Animation animation) {
      startActivity(new Intent(LogotypeActivity.this, MainActivity.class));
      LogotypeActivity.this.finish();
    }

    @Override
    public void onAnimationRepeat(Animation animation) {}

    @Override
    public void onAnimationStart(Animation animation) {}
  }
}
