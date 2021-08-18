package com.jjak0b.android.trackingmypantry.services;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.provider.CalendarContract;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleService;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.model.Pantry;
import com.jjak0b.android.trackingmypantry.data.model.Product;
import com.jjak0b.android.trackingmypantry.data.model.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.data.model.relationships.ProductInstanceGroupInfo;

import org.jetbrains.annotations.NotNull;

import java.util.TimeZone;


public class ProductExpirationNotificationService extends LifecycleService {
    private Looper serviceLooper;
    private ServiceHandler serviceHandler;
    ProductExpirationNotificationViewModel viewModel;
    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.


            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            // stopSelf(msg.arg1);
        }
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work doesn't disrupt our UI.
        super.onCreate();
        viewModel = new ProductExpirationNotificationViewModel(getApplication());
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();



        // Get the HandlerThread's Looper and use it for our Handler
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            serviceHandler.sendMessage(msg);
            viewModel.getAllProductInfo().observe(this, infoGroups -> {
                Log.e("WARN", "got list of " + infoGroups.size() );
                for (ProductInstanceGroupInfo info : infoGroups) {
                    insertExpirationReminder(this, info.product, info.group, info.pantry);
                }
            });
        }

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return super.onBind(intent);
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    public static Uri createCalendarWithName(Context ctx, String name,String accountName) {

        Uri target = Uri.parse(CalendarContract.Calendars.CONTENT_URI.toString());
        target = target.buildUpon().appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, accountName)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, "com.google").build();

        ContentValues values = new ContentValues();
        values.put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
        values.put(CalendarContract.Calendars.ACCOUNT_NAME, "test");
        values.put(CalendarContract.Calendars.NAME, name);
        values.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, name);
        values.put(CalendarContract.Calendars.OWNER_ACCOUNT, accountName);
        values.put(CalendarContract.Calendars.VISIBLE, 1);

        Uri newCalendar = ctx.getContentResolver().insert(target, values);

        return newCalendar;
    }
    static Uri asSyncAdapter(Uri uri, String account, String accountType) {
        return uri.buildUpon()
                .appendQueryParameter(android.provider.CalendarContract.CALLER_IS_SYNCADAPTER,"true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, "" )
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, "" )
                .build();
    }

    void insertExpirationReminder(@NotNull Context context, @NotNull Product product, @NotNull ProductInstanceGroup group, @NotNull Pantry pantry ) {
        String msg = context.getResources()
                .getQuantityString(R.plurals.product_expire_message, group.getQuantity(),
                        product.getName(),
                        pantry.getName(),
                        group.getQuantity()
                );
        Log.e( ProductExpirationNotificationService.class.getName(), msg );

        ContentResolver cr = context.getContentResolver();


        ContentValues event = new ContentValues();
        event.put(CalendarContract.Events.CALENDAR_ID, "" );
        event.put(CalendarContract.Events.TITLE, R.string.product_expiration);
        event.put(CalendarContract.Events.DESCRIPTION, msg);
        event.put(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
        event.put(CalendarContract.EXTRA_EVENT_BEGIN_TIME, group.getExpiryDate().getTime());
        event.put(CalendarContract.EXTRA_EVENT_END_TIME, group.getExpiryDate().getTime());
        event.put(CalendarContract.Events.EVENT_TIMEZONE, "UTC");
        event.put(CalendarContract.Reminders.EVENT_ID, group.getId());

        Uri uri = cr.insert(CalendarContract.Reminders.CONTENT_URI, event);


/*
        Intent intent = new Intent(Intent.ACTION_INSERT)
        .setData(CalendarContract.Events.CONTENT_URI)
        .putExtra(CalendarContract.Events.CALENDAR_ID, group.getId())
        .putExtra(CalendarContract.Events.TITLE, getResources().getString(R.string.product_expiration))
        .putExtra(CalendarContract.Events.DESCRIPTION, msg)
        .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, group.getExpiryDate().getTime())
        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, group.getExpiryDate().getTime())
        .putExtra(CalendarContract.Events.EVENT_TIMEZONE, "UTC")
        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
 */
    }

}