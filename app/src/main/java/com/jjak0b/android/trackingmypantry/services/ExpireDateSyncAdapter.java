package com.jjak0b.android.trackingmypantry.services;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
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

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.preference.PreferenceManager;

import com.jjak0b.android.trackingmypantry.R;
import com.jjak0b.android.trackingmypantry.data.db.PantryDB;
import com.jjak0b.android.trackingmypantry.data.db.daos.ProductInstanceDao;
import com.jjak0b.android.trackingmypantry.data.db.results.ExpirationInfo;
import com.jjak0b.android.trackingmypantry.data.preferences.Preferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
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
    private static final String EVENTS_COLUMN_PRODUCT_ID = CalendarContract.Events.SYNC_DATA1;
    private static final String EVENTS_COLUMN_PANTRY_ID = CalendarContract.Events.SYNC_DATA2;
    public static final String EXTRA_EVENT_DATE = "date";
    public static final String EXTRA_EVENT_PRODUCT_ID = "product_id";
    public static final String EXTRA_EVENT_PANTRY_ID = "pantry_id";
    public static final String EXTRA_OPERATION_EVENT = "operation_event_item";
    private static final String TAG = "ExpireDateSyncAdapter";

    public static final int OPERATION_EVENT_INSERT = 0;
    public static final int OPERATION_EVENT_UPDATE = 1;
    public static final int OPERATION_EVENT_REMOVE = 2;

    private ProductInstanceDao groupDao;

    class EventCreator {
        private ContentProviderOperation.Builder builder;
        private ExpirationInfo entry;
        public EventCreator(ContentProviderOperation.Builder builder) {
            this.builder = builder;
        }

        EventCreator setItem(ExpirationInfo info) {
            entry = info;
            return this;
        }

        ContentProviderOperation.Builder assembleBuilder() {
            String eventTitle = getContext().getResources()
                    .getQuantityString(R.plurals.product_expire_message, entry.quantity,
                            entry.product_name,
                            entry.pantry_name,
                            entry.quantity
                    );
            String eventDescription = getContext().getResources().getString(R.string.product_expiration);
            String eventLocation = getContext().getString(R.string.location_inside_pantry, entry.pantry_name);

            return builder
                    // .withValue(EVENTS_COLUMN_GROUP_ID, entry.group.getId() )
                    .withValue(EVENTS_COLUMN_PANTRY_ID, entry.pantry_id )
                    .withValue(EVENTS_COLUMN_PRODUCT_ID, entry.product_id )
                    .withValue(CalendarContract.Events.TITLE, eventTitle )
                    .withValue(CalendarContract.Events.DESCRIPTION, eventDescription)
                    .withValue(CalendarContract.Events.EVENT_LOCATION, eventLocation)
                    .withValue(CalendarContract.Events.ALL_DAY, true)
                    .withValue(CalendarContract.Events.DTSTART, entry.expiryDate.getTime())
                    .withValue(CalendarContract.Events.DTEND, entry.expiryDate.getTime())
                    .withValue(CalendarContract.Events.EVENT_TIMEZONE, "UTC")
                    .withValue(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_FREE);
        }
    }
    /**
     * Set up the sync adapter
     */
    public ExpireDateSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        PantryDB db = PantryDB.getInstance(getContext());
        groupDao = db.getProductInstanceDao();
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

        PantryDB db = PantryDB.getInstance(getContext());
        groupDao = db.getProductInstanceDao();
    }

    class LocalEventEntry {
        public long eventID;

        public long pantryID;
        @NonNull
        public String productID;
        @NonNull
        public Date expireDate;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LocalEventEntry that = (LocalEventEntry) o;
            return pantryID == that.pantryID
                    && Objects.equals(productID, that.productID)
                    && Objects.equals(expireDate, that.expireDate);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pantryID, productID, expireDate);
        }
    }

    class Filter {
        public Long pantryID;
        public String productID;
        public Date expireDate;

        @Override
        public String toString() {
            return "Filter{" +
                    "pantryID=" + pantryID +
                    ", productID='" + productID + '\'' +
                    ", expireDate=" + expireDate +
                    '}';
        }
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
        AccountManager accountManager = AccountManager.get(getContext());

        final List<LocalEventEntry> localEventsUpdated = new LinkedList<>();
        final ArrayList<ContentProviderOperation> toRemove = new ArrayList<>();
        final ArrayList<ContentProviderOperation> toUpdate = new ArrayList<>();
        final ArrayList<ContentProviderOperation> toCreate = new ArrayList<>();
        final ArrayList<ContentProviderOperation> toRemind = new ArrayList<>();
        String userID = accountManager.getUserData(account, Authenticator.ACCOUNT_ID );
        Filter filter = new Filter();
        filter.productID = extras.getString(EXTRA_EVENT_PRODUCT_ID);
        long tmp = extras.getLong(EXTRA_EVENT_PANTRY_ID);
        filter.pantryID = tmp > 0 ? tmp : null;
        tmp = extras.getLong(EXTRA_EVENT_DATE);
        filter.expireDate = tmp > 0 ? new Date(tmp) : null;
        HashMap<LocalEventEntry, Long> localEvents;

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

            Log.d(TAG, "Syncing events matching with" + filter );
            // Current stored product groups
            List<ExpirationInfo> infoGroups = groupDao.getInfoOfAll(userID, filter.productID, filter.pantryID, filter.expireDate);
            Log.d(TAG, "Syncing " + infoGroups.size() + " events" );

            // Current stored events of product groups
            localEvents = getLocalEvents(provider, account, calendarID, filter);
            Log.d(TAG, "Locally we have " + localEvents.size() + " events matching" );


            // Detect if an infoGroup
            // exists in local and so should be updated
            // or doesn't exist in local and so should be added as new
            for (ExpirationInfo info : infoGroups) {
                LocalEventEntry test = new LocalEventEntry();
                test.productID = info.product_id;
                test.pantryID = info.pantry_id;
                test.expireDate = info.expiryDate;
                // the tuple <productID, pantryID, expireDate> is the unique identifier for each event
                Long value = localEvents.get(test);
                // even if this this is the real event identifier for the calendar, see #createEvent
                test.eventID = value != null ? value : 0;

                // this entry is not in local
                if( test.eventID == 0 ) {
                    toCreate.add(createEvent(calendarID, account, info));
                }
                // update the entry in local
                else {
                    toUpdate.add(updateEvent(test.eventID, account, info));
                    localEventsUpdated.add(test);
                }
            }
            // Detect if an infoGroup
            // is obsolete in local and should be deleted
            Collection<LocalEventEntry> unprocessed = localEvents.keySet();
            unprocessed.removeAll(localEventsUpdated);
            for (LocalEventEntry entry : unprocessed ) {
                toRemove.add(deleteEvent(entry.eventID, account));
            }

            // submit operations
            ArrayList<ArrayList<ContentProviderOperation>> operationsLists = new ArrayList<>(3);
            operationsLists.add(toRemove); // sync step 1
            operationsLists.add(toUpdate); // sync step 2
            operationsLists.add(toCreate); // sync step 3
            operationsLists.add(toRemind); // sync step 4 - will be populated at step 3

            Log.d(TAG, "Summary:\n" +
                    "Removing " + toRemove.size() + " obsolete events\n" +
                    "Updating " + toUpdate.size() + " events\n" +
                    "Adding " + toCreate.size() + " events\n"
            );

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

        } catch ( RemoteException e) {
            Log.e(TAG, "Error on fetching event items", e );
        }
        Log.d(TAG, "sync ended");
    }

    HashMap<LocalEventEntry, Long> getLocalEvents(ContentProviderClient provider, Account account, long calendarID, Filter filter) throws RemoteException {

        final String[] EVENT_PROJECTION = new String[] {
                CalendarContract.Events._ID,
                EVENTS_COLUMN_PANTRY_ID,
                EVENTS_COLUMN_PRODUCT_ID,
                CalendarContract.Events.DTSTART
        };
        // The indexes for the projection array above.
        final int PROJECTION_EVENT_ID_INDEX = 0;
        final int PROJECTION_PANTRY_ID_INDEX = 1;
        final int PROJECTION_PRODUCT_ID_INDEX = 2;
        final int PROJECTION_DATE_INDEX = 3;

        final HashMap<LocalEventEntry, Long> localEvents = new HashMap<>();

        ArrayList<String> EVENT_SELECTION_ARGS_BUILDER = new ArrayList<>(EVENT_PROJECTION.length);
        StringBuilder EVENT_SELECTION_BUILDER = new StringBuilder();
        EVENT_SELECTION_BUILDER
                .append( "(" )
                .append( "(" + CalendarContract.Events.CALENDAR_ID  + " = ? )");
        EVENT_SELECTION_ARGS_BUILDER.add( String.valueOf(calendarID) );
        // filter for pantry if defined
        if( filter.pantryID != null ) {
            EVENT_SELECTION_BUILDER
                    .append(" AND (").append( EVENTS_COLUMN_PANTRY_ID + " = ? )");
            EVENT_SELECTION_ARGS_BUILDER
                    .add( String.valueOf(filter.pantryID) );
        }
        // filter for product if defined
        if( filter.productID != null ) {
            EVENT_SELECTION_BUILDER
                    .append(" AND (").append( EVENTS_COLUMN_PRODUCT_ID + " = ? )");
            EVENT_SELECTION_ARGS_BUILDER
                    .add( String.valueOf(filter.productID) );
        }
        if( filter.expireDate != null ) {
            EVENT_SELECTION_BUILDER
                    .append(" AND (")
                        .append( CalendarContract.Events.DTSTART + " <= ? ")
                        .append("AND ? <= " + CalendarContract.Events.DTEND + " )");
            EVENT_SELECTION_ARGS_BUILDER
                    .add( String.valueOf(filter.expireDate.getTime()) );
            EVENT_SELECTION_ARGS_BUILDER
                    .add( String.valueOf(filter.expireDate.getTime()) );
        }
        EVENT_SELECTION_BUILDER
                .append( ")" );

        String EVENT_SELECTION = EVENT_SELECTION_BUILDER.toString();
        String[] EVENT_SELECTION_ARGS = EVENT_SELECTION_ARGS_BUILDER.toArray(new String[0]);

        Cursor cLocalEvents = provider.query(
                asSyncAdapter(CalendarContract.Events.CONTENT_URI, account),
                EVENT_PROJECTION,
                EVENT_SELECTION,
                EVENT_SELECTION_ARGS,
                null
        );

        while (cLocalEvents != null && cLocalEvents.moveToNext()) {
            LocalEventEntry entry = new LocalEventEntry();
            entry.eventID = cLocalEvents.getLong(PROJECTION_EVENT_ID_INDEX);
            entry.pantryID = cLocalEvents.getLong(PROJECTION_PANTRY_ID_INDEX);
            entry.productID = cLocalEvents.getString(PROJECTION_PRODUCT_ID_INDEX);
            entry.expireDate = new Date(cLocalEvents.getLong(PROJECTION_DATE_INDEX));
            localEvents.put(entry, entry.eventID);
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

    ContentProviderOperation createEvent(long calendarID, Account account, @NonNull ExpirationInfo entry) {
        return new EventCreator(ContentProviderOperation.newInsert(asSyncAdapter(CalendarContract.Events.CONTENT_URI, account)))
                .setItem(entry)
                .assembleBuilder()
                .withValue(CalendarContract.Events.CALENDAR_ID, calendarID )
                .build();
    }

    ContentProviderOperation updateEvent( long eventID, Account account, @NonNull ExpirationInfo entry) {
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
