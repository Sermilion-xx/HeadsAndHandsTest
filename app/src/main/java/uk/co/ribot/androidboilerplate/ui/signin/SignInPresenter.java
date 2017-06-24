package uk.co.ribot.androidboilerplate.ui.signin;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import uk.co.ribot.androidboilerplate.R;
import uk.co.ribot.androidboilerplate.data.DataManager;
import uk.co.ribot.androidboilerplate.data.model.WeatherResponse;
import uk.co.ribot.androidboilerplate.injection.ConfigPersistent;
import uk.co.ribot.androidboilerplate.ui.base.BasePresenter;
import uk.co.ribot.androidboilerplate.util.RxUtil;

/**
 * ---------------------------------------------------
 * Created by Sermilion on 23/06/2017.
 * Project: android-boilerplate-master
 * ---------------------------------------------------
 * <a href="http://www.ucomplex.org">www.ucomplex.org</a>
 * <a href="http://www.github.com/sermilion>github</a>
 * ---------------------------------------------------
 */
@ConfigPersistent
public class SignInPresenter extends BasePresenter<SignInMvpView> {

    private final DataManager mDataManager;
    private Subscription mSubscription;

    @Inject
    public SignInPresenter(DataManager dataManager) {
        mDataManager = dataManager;
    }

    @Override
    public void detachView() {
        super.detachView();
        if (mSubscription != null) mSubscription.unsubscribe();
    }

    private boolean checkEmail(String email) {
        if (email.length() == 0)
            return false;
        int indexOfDot = email.indexOf(".");
        return email.indexOf("@") > 0
                && email.indexOf("@") < indexOfDot - 1
                && email.length() - 1 > indexOfDot;
    }

    public enum SignInError {
        NO_NUMBER, NO_LOWER, NO_UPPER, LENGTH, EMAIL
    }

    private List<SignInError> checkPassword(String password) {
        List<SignInError> passwordErrors = new ArrayList<>();
        boolean hasUppercase = !password.equals(password.toLowerCase());
        boolean hasLowercase = !password.equals(password.toUpperCase());
        if (password.length() < 6)
            passwordErrors.add(SignInError.LENGTH);
        if (!password.matches(".*\\d+.*"))
            passwordErrors.add(SignInError.NO_NUMBER);
        if (!hasUppercase)
            passwordErrors.add(SignInError.NO_UPPER);
        if (!hasLowercase)
            passwordErrors.add(SignInError.NO_LOWER);
        return passwordErrors;
    }

    public String PasswordErrorsToString(List<SignInPresenter.SignInError> errors, Context context) {
        StringBuilder errorBuilder = new StringBuilder();
        for (int i = 0; i < errors.size(); i++) {
            SignInPresenter.SignInError error = errors.get(i);
            switch (error) {
                case LENGTH:
                    errorBuilder.append(context.getString(R.string.error_password_length)).append("\n");
                    break;
                case NO_NUMBER:
                    errorBuilder.append(context.getString(R.string.error_password_no_number)).append("\n");
                    break;
                case NO_UPPER:
                    errorBuilder.append(context.getString(R.string.error_password_no_upper)).append("\n");
                    break;
                case NO_LOWER:
                    errorBuilder.append(context.getString(R.string.error_password_no_lower)).append("\n");
                    break;
            }
        }
        return errorBuilder.toString();
    }

    public List<SignInError> checkCredentials(String email, String password) {
        List<SignInError> errorList = new ArrayList<>();
        boolean emailCorrect = checkEmail(email);
        List<SignInError> passwordErrors = checkPassword(password);
        if (!emailCorrect) {
            errorList.add(SignInError.EMAIL);
        }
        if (passwordErrors.size() > 0) {
            errorList.addAll(passwordErrors);
        }
        return errorList;
    }

    public void loginRequest(String email, String password) {
        checkViewAttached();
        RxUtil.unsubscribe(mSubscription);
        mSubscription = mDataManager.login()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<WeatherResponse>() {

                    @Override
                    public void onStart() {
                        super.onStart();
                        getMvpView().showLoading();
                    }

                    @Override
                    public void onCompleted() {
                        getMvpView().hideLoading();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        getMvpView().hideLoading();
                        getMvpView().signInFail();
                    }

                    @Override
                    public void onNext(WeatherResponse weatherResponse) {
                        getMvpView().signInSuccess(weatherResponse);
                    }
                });
    }

    public void forgotPassword(String email) {
        if (checkEmail(email)) {
            getMvpView().showSnackbar(R.string.password_restore_request_sent);
        } else {
            getMvpView().showSnackbar(R.string.error_email_format);
        }
    }

}
