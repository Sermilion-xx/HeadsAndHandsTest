package uk.co.ribot.androidboilerplate.ui.signin;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.ribot.androidboilerplate.R;
import uk.co.ribot.androidboilerplate.data.model.WeatherResponse;
import uk.co.ribot.androidboilerplate.ui.base.BaseActivity;

import static uk.co.ribot.androidboilerplate.ui.signin.SignInPresenter.SignInError.EMAIL;

public class SignInActivity extends BaseActivity implements SignInMvpView {

    @Inject
    SignInPresenter mMainPresenter;
    @BindView(R.id.email)
    EditText mEmailField;
    @BindView(R.id.password)
    AppCompatEditText mPasswordField;
    @BindView(R.id.login)
    Button mLoginButton;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.content)
    FrameLayout mContentView;

    public static Intent createIntent(Context context) {
        return new Intent(context, SignInActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        ButterKnife.bind(this);
        setupToolbar(R.string.authorization);
        activityComponent().inject(this);
        mMainPresenter.attachView(this);
        mEmailField.addTextChangedListener(buttonStateChanger);
        mPasswordField.addTextChangedListener(buttonStateChanger);
        mPasswordField.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (mPasswordField.getRight() - mPasswordField.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        showPasswordHints();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    protected void setupToolbar(int title) {
        mToolbar.setTitle(title);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_blue);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMainPresenter.detachView();
    }

    @OnClick({R.id.forgotPassword, R.id.login})
    public void onClick(Button button) {
        switch (button.getId()) {
            case R.id.login:
                clickLoginButton();
                break;
            case R.id.forgotPassword:
                showRestorePasswordDialog();
                break;
        }
    }

    @Override
    public void showLoading() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        mProgressBar.setVisibility(View.GONE);
    }


    public void showRestorePasswordDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.dialog_input, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);
        final EditText editText = (EditText) promptView.findViewById(R.id.email);
        editText.setTextColor(ContextCompat.getColor(this, R.color.colorGray));
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton(SignInActivity.this.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String email = editText.getText().toString();
                        mMainPresenter.forgotPassword(email);
                    }
                }).setNegativeButton(SignInActivity.this.getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    void clickLoginButton() {
        hideKeyboard();
        mEmailField.setError(null);
        mPasswordField.setError(null);
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();

        List<SignInPresenter.SignInError> error = mMainPresenter.checkCredentials(email, password);

        if (error.size() == 0) {
            mMainPresenter.loginRequest(email, password);
        } else {
            String passwordError = mMainPresenter.PasswordErrorsToString(error, this);
            if (passwordError.length() > 0) {
                mPasswordField.setError(passwordError);
            }
            if (error.contains(EMAIL)) {
                mEmailField.setError(getString(R.string.error_email_format));
            }
        }
    }

    private void showPasswordHints() {
        CharSequence[] items = {getString(R.string.error_email_format),
                getString(R.string.error_password_length),
                getString(R.string.error_password_no_lower),
                getString(R.string.error_password_no_upper),
                getString(R.string.error_password_no_number)};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(items, null);
        AlertDialog dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.show();
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void signInSuccess(WeatherResponse weatherResponse) {
        showSnackbar(getString(R.string.temperature_in_london) + String.valueOf(weatherResponse.getWeather()));
    }

    @Override
    public void signInFail() {
        showSnackbar(R.string.error_login);
    }

    TextWatcher buttonStateChanger = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mEmailField.getText().length() > 0 && mPasswordField.getText().length() > 0) {
                mLoginButton.setEnabled(true);
            } else {
                mLoginButton.setEnabled(false);
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sign_in, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.create:
                //создать что-то
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showSnackbar(int message) {
        showSnackbar(message, mContentView);
    }

    @Override
    public void showSnackbar(String message) {
        showSnackbar(message, mContentView);
    }

}
