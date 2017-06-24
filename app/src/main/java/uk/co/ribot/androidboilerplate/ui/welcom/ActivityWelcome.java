package uk.co.ribot.androidboilerplate.ui.welcom;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.ribot.androidboilerplate.R;
import uk.co.ribot.androidboilerplate.ui.base.BaseActivity;
import uk.co.ribot.androidboilerplate.ui.signin.SignInActivity;

public class ActivityWelcome extends BaseActivity implements WelcomeMvpView{

    @BindView(R.id.login)
    Button mLoginButton;
    @BindView(R.id.register)
    Button mRegisterButton;
    @BindView(R.id.content)
    LinearLayout mContentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);
    }

    @OnClick({ R.id.login, R.id.register})
    public void onClick(Button button) {
        if (button.getId() == R.id.login) {
            startActivity(SignInActivity.createIntent(this));
        } else {
            showSnackbar(R.string.operation_not_supported);
        }
    }

    @Override
    public void showSnackbar(int message) {
        showSnackbar(message, mContentView);
    }
}
