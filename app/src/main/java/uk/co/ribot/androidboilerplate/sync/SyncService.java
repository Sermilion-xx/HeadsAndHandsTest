package uk.co.ribot.androidboilerplate.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class SyncService extends Service {

    private static SyncAdapter sSyncAdapter = null;
    // Лок для тредовой безопасности
    private static final Object sSyncAdapterLock = new Object();

    @Override
    public void onCreate() {
        //Запрещаем паралельную инхронизацию, так как SyncAdapter может иметь в очереди
        //несколько запусков
        //SyncAdapter синглтон
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
    }

    //IBinder который позволяет вызывать SyncAdapter
    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}