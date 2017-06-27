package uk.co.ribot.androidboilerplate.sync;

import android.accounts.Account;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class AuthenticatorService extends Service {

    public static final String AUTHORITY = "ru.handh.network.sync";
    public static final String ACCOUNT_TYPE = "sync";
    public static String ACCOUNT = "handh_login";
    private static final String KEY_ACCOUNT_NAME = "accountName";
    private static String accountName = "handh";
    private Authenticator mAuthenticator;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        accountName = intent.getStringExtra(KEY_ACCOUNT_NAME);
        return super.onStartCommand(intent, flags, startId);
    }

    public static Account GetAccount() {
        return new Account(accountName, ACCOUNT_TYPE);
    }

    @Override
    public void onCreate() {
        mAuthenticator = new Authenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}

