package com.jjak0b.android.trackingmypantry.services;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.CalendarContract;
import android.util.Log;

import androidx.annotation.StringRes;
import androidx.preference.PreferenceManager;

import com.google.common.util.concurrent.ListenableFuture;
import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.PantryRepository;
import com.jjak0b.android.trackingmypantry.data.Preferences;
import com.jjak0b.android.trackingmypantry.data.model.relationships.ProductInstanceGroupInfo;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
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
    private static final String TAG = "ExpireDateSyncAdapter";

    public static final int OPERATION_EVENT_INSERT = 0;
    public static final int OPERATION_EVENT_UPDATE = 1;
    public static final int OPERATION_EVENT_REMOVE = 2;

    // Define a variable to contain a content resolver instance
    ContentResolver mContentResolver;
    PantryRepository pantryRepository;

    class EventCreator {
        private ContentProviderOperation.Builder builder;
        private ProductInstanceGroupInfo entry;
        public EventCreator(ContentProviderOperation.Builder builder) {
            this.builder = builder;
        }

        EventCreator setItem(ProductInstanceGroupInfo info) {
            entry = info;
            return this;
        }

        ContentProviderOperation.Builder assembleBuilder() {
            String eventTitle = getContext().getResources()
                    .getQuantityString(R.plurals.product_expire_message, entry.group.getQuantity(),
                            entry.product.getName(),
                            entry.pantry.getName(),
                            entry.group.getQuantity()
                    );
            String eventDescription = getContext().getResources().getString(R.string.product_expiration);
            String eventLocation = getContext().getString(R.string.location_inside_pantry, entry.pantry.getName());

            return builder.withValue(EVENTS_COLUMN_GROUP_ID, entry.group.getId() )
                    .withValue(CalendarContract.Events.TITLE, eventTitle )
                    .withValue(CalendarContract.Events.DESCRIPTION, eventDescription)
                    .withValue(CalendarContract.Events.EVENT_LOCATION, eventLocation)
                    .withValue(CalendarContract.Events.ALL_DAY, true)
                    .withValue(CalendarContract.Events.DTSTART, entry.group.getExpiryDate().getTime())
                    .withValue(CalendarContract.Events.DTEND, entry.group.getExpiryDate().getTime())
                    .withValue(CalendarContract.Events.EVENT_TIMEZONE, "UTC")
                    .withValue(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_FREE);
        }
    }
    /**
     * Set up the sync adapter
     */
    public ExpireDateSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
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
        mContentResolver = context.getContentResolver();
        pantryRepository = PantryRepository.getInstance(context);
    }

    class LocalEventEntry {
        public long eventID;
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


        Log.d(TAG, "sync started");
        mContentResolver = getContext().getContentResolver();
        pantryRepository = PantryRepository.getInstance(getContext());

        final List<LocalEventEntry> localEventsUpdated = new LinkedList<>();
        final ArrayList<ContentProviderOperation> toRemove = new ArrayList<>();
        final ArrayList<ContentProviderOperation> toUpdate = new ArrayList<>();
        final ArrayList<ContentProviderOperation> toCreate = new ArrayList<>();
        final ArrayList<ContentProviderOperation> toRemind = new ArrayList<>();
        ListenableFuture<List<ProductInstanceGroupInfo>> futureList;
        Long groupID = extras.getLong(EXTRA_EVENT_GROUP_ID);
        String productID = extras.getString(EXTRA_EVENT_PRODUCT_ID);
        Long pantryID = extras.getLong(EXTRA_EVENT_PANTRY_ID);
        HashMap<Long, LocalEventEntry> localEvents;

        futureList = pantryRepository.getInfoOfAll(null, null);

        try {
            // retrieve or create the calendar
            long calendarID = getCalendar( provider, account );
            if( calendarID < 0 ) {
                Log.d(TAG, "creating new calendar for "+ account.name );
                Uri newCalendar = createCalendarFor(provider, account);
                calendarID = Long.parseLong(newCalendar.getLastPathSegment());
            }
            else {
                Log.d(TAG, "calendar for "+ account.name  + ": " + calendarID );
            }

            // Current stored product groups
            List<ProductInstanceGroupInfo> infoGroups = futureList.get();
            Log.d(TAG, "syncing " + infoGroups.size() + " events" );

            // Current stored events of product groups
            localEvents = getLocalEvents(provider, account, calendarID );
            Log.d(TAG, "checking " + localEvents.size() + " local events" );

            // Detect if an infoGroup
            // exists in local and so should be updated
            // or doesn't exist in local and so should be added as new
            for (ProductInstanceGroupInfo info : infoGroups) {

                LocalEventEntry localEntry = localEvents.get(info.group.getId());

                // this entry is not in local
                if( localEntry == null) {
                    toCreate.add(createEvent(calendarID, account, info));
                }
                // update the entry in local
                else {
                    toUpdate.add(updateEvent(localEntry.eventID, account, info));
                    localEventsUpdated.add(localEntry);
                }
            }
            // Detect if an infoGroup
            // is obsolete in local and should be deleted
            Collection<LocalEventEntry> unprocessed = localEvents.values();
            unprocessed.removeAll(localEventsUpdated);
            Log.d(TAG, "removing " + unprocessed.size() + " obsolete events" );
            for (LocalEventEntry entry : unprocessed ) {
                toRemove.add(deleteEvent(entry.eventID, account));
            }

            // submit operations
            ArrayList<ArrayList<ContentProviderOperation>> operationsLists = new ArrayList<>(3);
            operationsLists.add(toRemove); // sync step 1
            operationsLists.add(toUpdate); // sync step 2
            operationsLists.add(toCreate); // sync step 3
            operationsLists.add(toRemind); // sync step 4 - will be populated at step 3

            final int STEP_CREATE_EVENTS = 3;
            long eventID = -1;
            int step = 1;
            for (ArrayList<ContentProviderOperation> operations : operationsLists) {
                try {
                    Log.d(TAG, "syncing step " + step + " ..." );
                    if( !operations.isEmpty()) {
                        Log.d(TAG, "syncing " + operations.size() + " operations" );
                        ContentProviderResult[] results = provider.applyBatch(operations);

                        // at this step all events are synced
                        // but we need to create reminders for each new created event
                        if( step == STEP_CREATE_EVENTS ) {
                            for ( ContentProviderResult result : results ) {
                                eventID = Long.parseLong(result.uri.getLastPathSegment());
                                if( eventID > 0){
                                    toRemind.add( createReminder(provider, account, eventID) );
                                }
                            }
                        }

                    }
                } catch (OperationApplicationException | RemoteException e){
                    Log.e(TAG, "syncing Exception occurred on step " + step , e );
                }

                // dispose resources
                operations.clear();

                Log.d(TAG, "syncing step " + step + " ended" );
                step++;
            }

        } catch (ExecutionException | InterruptedException | RemoteException e) {
            Log.e(TAG, "Error on fetching event items", e );
        }
        Log.d(TAG, "sync ended");
    }

    HashMap<Long, LocalEventEntry> getLocalEvents(ContentProviderClient provider, Account account, long calendarID) throws RemoteException {

        final String[] EVENT_PROJECTION = new String[] {
                CalendarContract.Events._ID,
                EVENTS_COLUMN_GROUP_ID
        };
        // The indexes for the projection array above.
        final int PROJECTION_EVENT_ID_INDEX = 0;
        final int PROJECTION_ITEM_ID_INDEX = 1;

        final HashMap<Long, LocalEventEntry> localEvents = new HashMap<>();
        Cursor cLocalEvents = provider.query(
                asSyncAdapter(CalendarContract.Events.CONTENT_URI, account),
                new String[] { CalendarContract.Events._ID, EVENTS_COLUMN_GROUP_ID},
                CalendarContract.Events.CALENDAR_ID + "=?",
                new String[] { String.valueOf( calendarID) },
                null
        );

        while (cLocalEvents != null && cLocalEvents.moveToNext()) {
            LocalEventEntry entry = new LocalEventEntry();
            entry.eventID = cLocalEvents.getLong(PROJECTION_EVENT_ID_INDEX);
            localEvents.put(cLocalEvents.getLong(PROJECTION_ITEM_ID_INDEX) , entry );
        }
        cLocalEvents.close();

        return localEvents;
    }

    private ContentProviderOperation deleteEvent(long eventID, Account account) {
        return ContentProviderOperation.newDelete(
                asSyncAdapter(
                        ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID),
                        account
                ).buildUpon().build()
        ).build();
    }

    private long getCalendar(ContentProviderClient provider, Account account ) throws RemoteException {

        final String[] EVENT_PROJECTION = new String[] {
                CalendarContract.Calendars._ID,                           // 0
        };
        // The index for the projection array above.
        final int PROJECTION_CALENDAR_ID_INDEX = 0;
        long eventID = -1;

        String selection = new StringBuilder()
                .append( "(" )
                .append( "(" + CalendarContract.Calendars.ACCOUNT_NAME + " = ? )")
                .append(" AND (").append( CalendarContract.Calendars.ACCOUNT_TYPE + " = ? )")
                .append(" AND (").append( CalendarContract.Calendars.OWNER_ACCOUNT + " = ? )")
                .append( ")" ).toString();

        String[] selectionArgs = new String[] {account.name, account.type, account.name};
        Cursor c = provider.query( CalendarContract.Calendars.CONTENT_URI, EVENT_PROJECTION, selection, selectionArgs, null );

        if( c!= null && c.moveToNext() ){
            eventID = c.getLong(PROJECTION_CALENDAR_ID_INDEX);
        }
        c.close();
        return eventID;
    }

    public String getCalendarName() {
        return CALENDAR_NAME;
    }
    public String getCalendarDisplayName() {
        return getContext().getString(CALENDAR_DISPLAY_NAME_VALUE);
    }

    public Uri createCalendarFor(ContentProviderClient provider, Account account) throws RemoteException {

        ContentValues values = new ContentValues();

        // required from docs
        values.put(CalendarContract.Calendars.ACCOUNT_TYPE, account.type);
        values.put(CalendarContract.Calendars.ACCOUNT_NAME, account.name);
        values.put(CalendarContract.Calendars.OWNER_ACCOUNT, account.name);
        values.put(CalendarContract.Calendars.NAME, getCalendarName());
        values.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, getCalendarDisplayName());
        values.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
        values.put(CalendarContract.Calendars.CALENDAR_COLOR, getContext().getColor(R.color.calendar_color));
        // Recommended
        values.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
        // others
        values.put(CalendarContract.Calendars.VISIBLE, 1);

        Uri target = Uri.parse(CalendarContract.Calendars.CONTENT_URI.toString());
        target = asSyncAdapter(target, account);

        Uri newCalendar = provider.insert(target, values);

        return newCalendar;
    }

    ContentProviderOperation createEvent(long calendarID, Account account, @NonNull ProductInstanceGroupInfo entry) {
        return new EventCreator(ContentProviderOperation.newInsert(asSyncAdapter(CalendarContract.Events.CONTENT_URI, account)))
                .setItem(entry)
                .assembleBuilder()
                .withValue(CalendarContract.Events.CALENDAR_ID, calendarID )
                .build();
    }

    ContentProviderOperation updateEvent( long eventID, Account account, @NonNull ProductInstanceGroupInfo entry) {
        return new EventCreator(
                ContentProviderOperation.newUpdate(
                        asSyncAdapter(
                                ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID),
                                account
                        )
                )
        )
                .setItem(entry)
                .assembleBuilder()
                .build();
    }

    ContentProviderOperation createReminder(ContentProviderClient provider, Account account, long eventID ) throws RemoteException {
        long reminderTime = getCalendarTimeForReminder();
        return ContentProviderOperation
                .newInsert(asSyncAdapter(CalendarContract.Reminders.CONTENT_URI, account))
                .withValue(CalendarContract.Reminders.EVENT_ID, eventID )
                .withValue(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
                .withValue(CalendarContract.Reminders.MINUTES, reminderTime)
                .build();
    }

    long getCalendarTimeForReminder() {
        int daysBefore = 2;
        int hour = 12;
        int minute = 00;
        
        SharedPreferences options = PreferenceManager.getDefaultSharedPreferences(getContext());
        String strTime = options.getString(Preferences.FEATURE_EXPIRATION_REMINDERS.KEY_TIME, Preferences.FEATURE_EXPIRATION_REMINDERS.TIME_DEFAULT);
        int days = options.getInt(Preferences.FEATURE_EXPIRATION_REMINDERS.KEY_DAYS_BEFORE, Preferences.FEATURE_EXPIRATION_REMINDERS.DAYS_BEFORE_DEFAULT);
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

        try {
            Date dateTime = dateFormat.parse(strTime);
            Calendar c = Calendar.getInstance();
            c.setTime( dateTime );

            hour = c.get( Calendar.HOUR_OF_DAY );
            minute = c.get( Calendar.MINUTE );
            daysBefore = days;
        }
        catch (Exception e) {
            Log.e(TAG, "Error parsing expiration reminders settings", e );
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(0);
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.add( Calendar.DATE, daysBefore-1);
        calendar.add( Calendar.HOUR_OF_DAY, 23-hour);
        calendar.add( Calendar.MINUTE, 60-minute);
        return TimeUnit.MILLISECONDS.toMinutes(calendar.getTimeInMillis()) ;

    }

    static Uri asSyncAdapter(Uri uri, Account account) {
        return uri.buildUpon()
                .appendQueryParameter(android.provider.CalendarContract.CALLER_IS_SYNCADAPTER,"true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, account.name)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, account.type).build();
    }
}
