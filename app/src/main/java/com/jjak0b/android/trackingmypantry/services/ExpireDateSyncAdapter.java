package com.jjak0b.android.trackingmypantry.services;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;

import androidx.annotation.StringRes;
import androidx.lifecycle.LiveData;

import com.google.common.util.concurrent.ListenableFuture;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.PantryRepository;
import com.jjak0b.android.trackingmypantry.data.model.Pantry;
import com.jjak0b.android.trackingmypantry.data.model.Product;
import com.jjak0b.android.trackingmypantry.data.model.ProductInstanceGroup;
import com.jjak0b.android.trackingmypantry.data.model.relationships.ProductInstanceGroupInfo;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 */
public class ExpireDateSyncAdapter extends AbstractThreadedSyncAdapter {
    // Global variables

    private static final String CALENDAR_NAME = "TrackingMyPantry";
    @StringRes
    private static final int CALENDAR_DISPLAY_NAME_VALUE = R.string.app_name;
    private static final String EVENTS_COLUMN_GROUP_ID = CalendarContract.Events.SYNC_DATA1;
    public static final String EXTRA_EVENT_GROUP_ID = "group_id";
    public static final String EXTRA_EVENT_PRODUCT_ID = "product_id";
    public static final String EXTRA_EVENT_PANTRY_ID = "event_item_id";
    public static final String EXTRA_OPERATION_EVENT = "operation_event_item";

    public static final int OPERATION_EVENT_INSERT = 0;
    public static final int OPERATION_EVENT_UPDATE = 1;
    public static final int OPERATION_EVENT_REMOVE = 2;

    // Define a variable to contain a content resolver instance
    ContentResolver contentResolver;
    PantryRepository pantryRepository;
    LiveData<List<ProductInstanceGroupInfo>> list;

    /**
     * Set up the sync adapter
     */
    public ExpireDateSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        contentResolver = context.getContentResolver();
        pantryRepository = PantryRepository.getInstance(context);
    }

    /**
     * Set up the sync adapter. This form of the
     * constructor maintains compatibility with Android 3.0
     * and later platform versions
     */
    public ExpireDateSyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        contentResolver = context.getContentResolver();
        pantryRepository = PantryRepository.getInstance(context);
    }

    /*
     * Specify the code you want to run in the sync adapter. The entire
     * sync adapter runs in a background thread, so you don't have to set
     * up your own background processing.
     */
    @Override
    public void onPerformSync(
            Account account,
            Bundle extras,
            String authority,
            ContentProviderClient provider,
            SyncResult syncResult) {


        ListenableFuture<List<ProductInstanceGroupInfo>> futureList = pantryRepository.getInfoOfAll(null, null);
        long calendarID = getOrCreateCalendarIDFor( account );

        try {
            List<ProductInstanceGroupInfo> infoGroups = futureList.get();
            Log.e("WARN", "got list of " + infoGroups.size() );
            for (ProductInstanceGroupInfo info : infoGroups) {
                Uri newEvent = createEvent( calendarID, info.product, info.group, info.pantry );
                long eventID = Long.parseLong(newEvent.getLastPathSegment());
                // Uri uriReminder = addRemindersForEvent( eventID );
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private long getOrCreateCalendarIDFor( Account account ) {

        final String[] EVENT_PROJECTION = new String[] {
                CalendarContract.Calendars._ID,                           // 0
                CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
                CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
        };
        // The indices for the projection array above.
        final int PROJECTION_CALENDAR_ID_INDEX = 0;
        final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
        final int PROJECTION_DISPLAY_NAME_INDEX = 2;
        final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;
        String selection = new StringBuilder()
                .append( "(" )
                .append( "(" + CalendarContract.Calendars.ACCOUNT_NAME + " = ? )")
                .append(" AND (").append( CalendarContract.Calendars.ACCOUNT_TYPE + " = ? )")
                .append(" AND (").append( CalendarContract.Calendars.OWNER_ACCOUNT + " = ? )")
                .append( ")" ).toString();

        String[] selectionArgs = new String[] {account.name, account.type, account.name};
        Cursor c = contentResolver.query( CalendarContract.Calendars.CONTENT_URI, EVENT_PROJECTION, selection, selectionArgs, null );
        long eventID = -1;
        if( c.moveToFirst() ){
            eventID = c.getLong(PROJECTION_CALENDAR_ID_INDEX);
        }
        else {
            Uri newCalendar = createCalendarFor(account);
            eventID = Long.parseLong(newCalendar.getLastPathSegment());
        }
        return eventID;
    }

    public String getCalendarName() {
        return CALENDAR_NAME;
    }
    public String getCalendarDisplayName() {
        return getContext().getString(CALENDAR_DISPLAY_NAME_VALUE);
    }

    public Uri createCalendarFor(Account account) {

        ContentValues values = new ContentValues();
        values.put(CalendarContract.Calendars.ACCOUNT_TYPE, account.type);
        values.put(CalendarContract.Calendars.ACCOUNT_NAME, account.name);
        values.put(CalendarContract.Calendars.OWNER_ACCOUNT, account.name);
        values.put(CalendarContract.Calendars.VISIBLE, 1);
        values.put(CalendarContract.Calendars.NAME, getCalendarName());
        values.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, getCalendarDisplayName());

        Uri target = Uri.parse(CalendarContract.Calendars.CONTENT_URI.toString());
        target = target.buildUpon().appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, account.name)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, account.type )
                .build();

        Uri newCalendar = contentResolver.insert(target, values);

        return newCalendar;
    }



    Uri createEvent( long calendarId, @NotNull Product product, @NotNull ProductInstanceGroup group, @NotNull Pantry pantry ) {
        String eventDescription = getContext().getResources()
                .getQuantityString(R.plurals.product_expire_message, group.getQuantity(),
                        product.getName(),
                        pantry.getName(),
                        group.getQuantity()
                );
        String eventTitle = getContext().getResources().getString(R.string.product_expiration);
        Log.e( ProductExpirationNotificationService.class.getName(), eventDescription );

        // Event related
        ContentValues event = new ContentValues();
        event.put(CalendarContract.Events.CALENDAR_ID, calendarId );
        event.put(EVENTS_COLUMN_GROUP_ID, group.getId() );
        event.put(CalendarContract.Events.TITLE, eventTitle );
        event.put(CalendarContract.Events.DESCRIPTION, eventDescription);
        event.put(CalendarContract.Events.EVENT_LOCATION, pantry.getName() );
        event.put(CalendarContract.Events.ALL_DAY, true);
        event.put(CalendarContract.EXTRA_EVENT_BEGIN_TIME, group.getExpiryDate().getTime());
        event.put(CalendarContract.EXTRA_EVENT_END_TIME, group.getExpiryDate().getTime());
        event.put(CalendarContract.Events.EVENT_TIMEZONE, "UTC");
        Uri uri = contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, event);

        return uri;
    }
/*
    Cursor getEvent(long calendarID, long groupID ) {
        String selection = new StringBuilder()
                .append( "(" )
                .append( "(" + CalendarContract.Events.+ " = ? )")
                .append(" AND (").append( CalendarContract.Calendars.ACCOUNT_TYPE + " = ? )")
                .append(" AND (").append( CalendarContract.Calendars.OWNER_ACCOUNT + " = ? )")
                .append( ")" ).toString();

        String[] selectionArgs = new String[] {calendarID, account.type, account.name};
        Cursor c = contentResolver.query( CalendarContract.Calendars.CONTENT_URI, EVENT_PROJECTION, selection, selectionArgs, null );
    }

    long getOrCreateEvent( long calendarID, long groupID ) {

        Cursor c = getEvent(calendarID, groupID );
        long eventID = -1;
        if( c.moveToFirst() ){
            eventID = c.getLong(PROJECTION_CALENDAR_ID_INDEX);
        }
        else {
            Uri newCalendar = createCalendarFor(account);
            eventID = Long.parseLong(newCalendar.getLastPathSegment());
        }
        return eventID;
    }
*/
    Uri addRemindersForEvent(long eventID ) {
        ContentValues values = new ContentValues();
        Date reminderTime = getCalendarTimeForReminder().getTime();
        values.put(CalendarContract.Reminders.EVENT_ID, eventID );
        values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
        values.put(CalendarContract.Reminders.MINUTES, TimeUnit.MILLISECONDS.toMinutes(reminderTime.getTime() ) );
        Uri uri = contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, values);
        return uri;
    }

    Calendar getCalendarTimeForReminder() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(0);
        calendar.add( Calendar.DAY_OF_MONTH, 7);
        calendar.add( Calendar.HOUR_OF_DAY, 12);
        calendar.add( Calendar.MINUTE, 30);
        return calendar;
    }
}
