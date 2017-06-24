package uk.co.ribot.androidboilerplate;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import rx.Observable;
import uk.co.ribot.androidboilerplate.data.DataManager;
import uk.co.ribot.androidboilerplate.data.model.Ribot;
import uk.co.ribot.androidboilerplate.data.model.WeatherResponse;
import uk.co.ribot.androidboilerplate.test.common.TestDataFactory;
import uk.co.ribot.androidboilerplate.ui.main.MainPresenter;
import uk.co.ribot.androidboilerplate.ui.signin.SignInMvpView;
import uk.co.ribot.androidboilerplate.ui.signin.SignInPresenter;
import uk.co.ribot.androidboilerplate.util.RxSchedulersOverrideRule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ---------------------------------------------------
 * Created by Sermilion on 23/06/2017.
 * Project: android-boilerplate-master
 * ---------------------------------------------------
 * <a href="http://www.ucomplex.org">www.ucomplex.org</a>
 * <a href="http://www.github.com/sermilion>github</a>
 * ---------------------------------------------------
 */
@RunWith(MockitoJUnitRunner.class)
public class SignInPresenterTest {


    @Mock
    SignInMvpView mMockMainMvpView;
    @Mock
    DataManager mMockDataManager;
    private SignInPresenter mSignInPresenter;

    @Rule
    public final RxSchedulersOverrideRule mOverrideSchedulersRule = new RxSchedulersOverrideRule();

    @Before
    public void setUp() {
        mSignInPresenter = new SignInPresenter(mMockDataManager);
        mSignInPresenter.attachView(mMockMainMvpView);
    }

    @After
    public void tearDown() {
        mSignInPresenter.detachView();
    }

    @Test
    public void signInReturnsTemperature() {
        WeatherResponse dummyResponse = new WeatherResponse();
        when(mMockDataManager.login())
                .thenReturn(Observable.just(dummyResponse));

        mSignInPresenter.loginRequest("", "");
        verify(mMockMainMvpView).showLoading();
        verify(mMockMainMvpView).signInSuccess(dummyResponse);
        verify(mMockMainMvpView).hideLoading();
        verify(mMockMainMvpView, never()).signInFail();
    }


    @Test
    public void SignInFails() {
        when(mMockDataManager.login())
                .thenReturn(Observable.<WeatherResponse>error(new RuntimeException()));
        mSignInPresenter.loginRequest("","");
        verify(mMockMainMvpView).showLoading();
        verify(mMockMainMvpView).signInFail();
        verify(mMockMainMvpView).hideLoading();
        verify(mMockMainMvpView, never()).signInSuccess(new WeatherResponse());
    }

    @Test
    public void correctCredentialsReturnsEmptyErrorList() {
        List<SignInPresenter.SignInError> errorList = mSignInPresenter.checkCredentials("mail@mail.com", "Password1");
        assertThat("Лист ошибок должен быть пустым", errorList.size() == 0);
    }

    @Test
    public void wrongEmailReturnsListWithError() {
        List<SignInPresenter.SignInError> errorList0 = mSignInPresenter.checkCredentials("", "Password1");
        List<SignInPresenter.SignInError> errorList1 = mSignInPresenter.checkCredentials("mail", "Password1");
        List<SignInPresenter.SignInError> errorList2 = mSignInPresenter.checkCredentials("mail@", "Password1");
        List<SignInPresenter.SignInError> errorList3 = mSignInPresenter.checkCredentials("mail@m", "Password1");
        List<SignInPresenter.SignInError> errorList4 = mSignInPresenter.checkCredentials("mail@m.", "Password1");
        assertThat("Должно быть 5 ошибок", errorList0.size() + errorList1.size() + errorList2.size() + errorList3.size() +errorList4.size() == 5);
    }

    @Test
    public void wrongPasswordReturnsListWithErrors() {
        List<SignInPresenter.SignInError> errorList0 = mSignInPresenter.checkCredentials("mail@m.com", "");
        List<SignInPresenter.SignInError> errorList1 = mSignInPresenter.checkCredentials("mail@m.com", "pass");
        List<SignInPresenter.SignInError> errorList2 = mSignInPresenter.checkCredentials("mail@m.com", "password");
        List<SignInPresenter.SignInError> errorList3 = mSignInPresenter.checkCredentials("mail@m.com", "Password");
        List<SignInPresenter.SignInError> errorList4 = mSignInPresenter.checkCredentials("mail@m.com", "Password1");
        assertThat("Размер раждого листа ошибок должен быть 4, 3, 2, 1, 0",
                        errorList0.size() == 4
                        && errorList1.size() == 3
                        && errorList2.size() == 2
                        && errorList3.size() == 1
                        && errorList4.size() == 0);
    }

}
