package uk.co.ribot.androidboilerplate.ui.signin;

import android.view.View;

import uk.co.ribot.androidboilerplate.data.model.WeatherResponse;
import uk.co.ribot.androidboilerplate.ui.base.MvpView;

/**
 * ---------------------------------------------------
 * Created by Sermilion on 23/06/2017.
 * Project: android-boilerplate-master
 * ---------------------------------------------------
 * <a href="http://www.ucomplex.org">www.ucomplex.org</a>
 * <a href="http://www.github.com/sermilion>github</a>
 * ---------------------------------------------------
 */

public interface SignInMvpView extends MvpView {

    void showLoading();

    void hideLoading();

    void signInSuccess(WeatherResponse weatherResponse);

    void signInFail();

    void showSnackbar(int message);

    void showSnackbar(String  message);

}
