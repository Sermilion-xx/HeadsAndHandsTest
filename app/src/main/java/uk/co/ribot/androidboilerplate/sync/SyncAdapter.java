package uk.co.ribot.androidboilerplate.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.ribot.androidboilerplate.sync.Provider.DataContract;
import uk.co.ribot.androidboilerplate.sync.Provider.DataParser;


class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = "SyncAdapter";
    private static final int NET_CONNECT_TIMEOUT_MILLIS = 15000;
    private static final int NET_READ_TIMEOUT_MILLIS = 10000;
    private static final String API_URL = "http://api.handh.ru";

    private static final String[] PROJECTION = new String[] {
            DataContract.Entry._ID,
            DataContract.Entry.COLUMN_NAME_ENTRY_ID,
            DataContract.Entry.COLUMN_NAME_TITLE,
            DataContract.Entry.COLUMN_NAME_PUBLISHED};

    private static final int COLUMN_ID = 0;
    private static final int COLUMN_ENTRY_ID = 1;
    private static final int COLUMN_TITLE = 2;
    private static final int COLUMN_PUBLISHED = 4;

    private ContentResolver mContentResolver;

    SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account,
                              Bundle extras,
                              String authority,
                              ContentProviderClient provider,
                              SyncResult syncResult) {
        //1. Подключаемся к серверу
        //2. Произвести синхнонизацию и сохранить в контент провайдере
        //3. Проверяем конфликты и актуальность данных
        //5. Закрываем подключениеSyncService
        Log.i(TAG, "Синхронизация началась.");
        try {
            final URL location = new URL(API_URL);
            InputStream stream = null;

            try {
                Log.i(TAG, "Получаем данные из сети: " + location);
                stream = downloadUrl(location);
                updateLocalDataData(stream, syncResult);
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        } catch (MalformedURLException e) {
            Log.wtf(TAG, "URL не привильно составлен: ", e);
            syncResult.stats.numParseExceptions++;
            return;
        } catch (IOException e) {
            Log.e(TAG, "Ошибка при чтении сети: " + e.toString());
            syncResult.stats.numIoExceptions++;
            return;
        } catch (XmlPullParserException | ParseException e) {
            Log.e(TAG, "Ошибка при парсинге: " + e.toString());
            syncResult.stats.numParseExceptions++;
            return;
        } catch (RemoteException | OperationApplicationException e) {
            Log.e(TAG, "Ошибка при обновлении базы данных: " + e.toString());
            syncResult.databaseError = true;
            return;
        }
        Log.i(TAG, "Синхронизация завершена.");
    }
    private InputStream downloadUrl(final URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(NET_READ_TIMEOUT_MILLIS);
        conn.setConnectTimeout(NET_CONNECT_TIMEOUT_MILLIS);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        return conn.getInputStream();
    }


    //Только односторонняя синхронизация с сервером.
    private void updateLocalDataData(final InputStream stream, final SyncResult syncResult)
            throws IOException, XmlPullParserException, RemoteException,
            OperationApplicationException, ParseException {

        final DataParser DataParser = new DataParser();
        final ContentResolver contentResolver = getContext().getContentResolver();
        final List<DataParser.Entry> entries = DataParser.parse(stream);
        ArrayList<ContentProviderOperation> batch = new ArrayList<>();
        Map<String, DataParser.Entry> entryMap = new HashMap<>();
        for (DataParser.Entry e : entries) {
            entryMap.put(e.id, e);
        }
        // Получаем лист со всеми данными
        Uri uri = DataContract.Entry.CONTENT_URI; // Get all entries
        Cursor c = contentResolver.query(uri, PROJECTION, null, null, null);
        assert c != null;

        int id;
        String entryId;
        String title;
        long published;
        while (c.moveToNext()) {
            syncResult.stats.numEntries++;
            id = c.getInt(COLUMN_ID);
            entryId = c.getString(COLUMN_ENTRY_ID);
            title = c.getString(COLUMN_TITLE);
            published = c.getLong(COLUMN_PUBLISHED);
            DataParser.Entry match = entryMap.get(entryId);
            if (match != null) {
                // Данные существуют. Удаляем их их Map.
                entryMap.remove(entryId);
                // Нужно ли обновить данные?
                Uri existingUri = DataContract.Entry.CONTENT_URI.buildUpon()
                        .appendPath(Integer.toString(id)).build();
                if ((match.title != null && !match.title.equals(title)) ||
                        (match.published != published)) {
                    // Обновление данных
                    batch.add(ContentProviderOperation.newUpdate(existingUri)
                            .withValue(DataContract.Entry.COLUMN_NAME_TITLE, title)
                            .withValue(DataContract.Entry.COLUMN_NAME_PUBLISHED, published)
                            .build());
                    syncResult.stats.numUpdates++;
                }
            } else {
                // Данные не существуют. Удаляем их из базы.
                Uri deleteUri = DataContract.Entry.CONTENT_URI.buildUpon()
                        .appendPath(Integer.toString(id)).build();
                batch.add(ContentProviderOperation.newDelete(deleteUri).build());
                syncResult.stats.numDeletes++;
            }
        }
        c.close();

        // Добавляем новые данные
        for (DataParser.Entry e : entryMap.values()) {
            batch.add(ContentProviderOperation.newInsert(DataContract.Entry.CONTENT_URI)
                    .withValue(DataContract.Entry.COLUMN_NAME_ENTRY_ID, e.id)
                    .withValue(DataContract.Entry.COLUMN_NAME_TITLE, e.title)
                    .withValue(DataContract.Entry.COLUMN_NAME_PUBLISHED, e.published)
                    .build());
            syncResult.stats.numInserts++;
        }
       //Batch update, не синхронизируя с сервером
        mContentResolver.applyBatch(DataContract.CONTENT_AUTHORITY, batch);
        mContentResolver.notifyChange(
                DataContract.Entry.CONTENT_URI,
                null,
                false); //не синхронизировать с сервером
    }
}