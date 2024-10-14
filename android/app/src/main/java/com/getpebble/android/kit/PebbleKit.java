package com.getpebble.android.kit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import com.getpebble.android.kit.Constants.PebbleAppType;
import com.getpebble.android.kit.Constants.PebbleDataType;
import com.getpebble.android.kit.util.PebbleDictionary;

import org.json.JSONException;

import java.util.UUID;

import static com.getpebble.android.kit.Constants.APP_UUID;
import static com.getpebble.android.kit.Constants.CUST_APP_TYPE;
import static com.getpebble.android.kit.Constants.CUST_ICON;
import static com.getpebble.android.kit.Constants.CUST_NAME;
import static com.getpebble.android.kit.Constants.DATA_LOG_TAG;
import static com.getpebble.android.kit.Constants.DATA_LOG_TIMESTAMP;
import static com.getpebble.android.kit.Constants.DATA_LOG_UUID;
import static com.getpebble.android.kit.Constants.INTENT_APP_ACK;
import static com.getpebble.android.kit.Constants.INTENT_APP_CUSTOMIZE;
import static com.getpebble.android.kit.Constants.INTENT_APP_NACK;
import static com.getpebble.android.kit.Constants.INTENT_APP_RECEIVE;
import static com.getpebble.android.kit.Constants.INTENT_APP_RECEIVE_ACK;
import static com.getpebble.android.kit.Constants.INTENT_APP_RECEIVE_NACK;
import static com.getpebble.android.kit.Constants.INTENT_APP_SEND;
import static com.getpebble.android.kit.Constants.INTENT_APP_START;
import static com.getpebble.android.kit.Constants.INTENT_APP_STOP;
import static com.getpebble.android.kit.Constants.INTENT_DL_ACK_DATA;
import static com.getpebble.android.kit.Constants.INTENT_DL_FINISH_SESSION;
import static com.getpebble.android.kit.Constants.INTENT_DL_RECEIVE_DATA;
import static com.getpebble.android.kit.Constants.INTENT_DL_REQUEST_DATA;
import static com.getpebble.android.kit.Constants.INTENT_PEBBLE_CONNECTED;
import static com.getpebble.android.kit.Constants.INTENT_PEBBLE_DISCONNECTED;
import static com.getpebble.android.kit.Constants.KIT_STATE_COLUMN_APPMSG_SUPPORT;
import static com.getpebble.android.kit.Constants.KIT_STATE_COLUMN_CONNECTED;
import static com.getpebble.android.kit.Constants.KIT_STATE_COLUMN_DATALOGGING_SUPPORT;
import static com.getpebble.android.kit.Constants.KIT_STATE_COLUMN_VERSION_MAJOR;
import static com.getpebble.android.kit.Constants.KIT_STATE_COLUMN_VERSION_MINOR;
import static com.getpebble.android.kit.Constants.KIT_STATE_COLUMN_VERSION_POINT;
import static com.getpebble.android.kit.Constants.KIT_STATE_COLUMN_VERSION_TAG;
import static com.getpebble.android.kit.Constants.MSG_DATA;
import static com.getpebble.android.kit.Constants.PBL_DATA_ID;
import static com.getpebble.android.kit.Constants.PBL_DATA_OBJECT;
import static com.getpebble.android.kit.Constants.PBL_DATA_TYPE;
import static com.getpebble.android.kit.Constants.TRANSACTION_ID;

/**
 * A helper class providing methods for interacting with third-party Pebble Smartwatch applications. Pebble-enabled
 * Android applications may use this class to assist in sending/receiving data between the watch and the phone.
 */
public final class PebbleKit {

    /**
     * The Constant NAME_MAX_LENGTH.
     */
    private static final int NAME_MAX_LENGTH = 32;

    /**
     * The Constant ICON_MAX_DIMENSIONS.
     */
    private static final int ICON_MAX_DIMENSIONS = 25;

    /**
     * Instantiates a new pebble kit.
     */
    private PebbleKit() {

    }

    /**
     * Send a message to the connected Pebble to "customize" a built-in PebbleKit watch-app. This is intended to allow
     * third-party Android applications to apply custom branding (both name and icon) on the watch without needing to
     * distribute a complete watch-app.
     *
     * @param context The context used to send the broadcast. (Protip: pass in the ApplicationContext here.)
     * @param appType The watch-app to be configured. Options are either
     * @param name    The custom name to be applied to the watch-app. Names must be less than 32 characters in length.
     * @param icon    The custom icon to be applied to the watch-app. Icons must be black-and-white bitmaps no larger than 32px
     *                in either dimension.
     * @throws IllegalArgumentException Thrown if the specified name or icon are invalid. {@link PebbleAppType#SPORTS} or {@link
     *                                  PebbleAppType#GOLF}.
     */
    public static void customizeWatchApp(final Context context, final PebbleAppType appType,
                                         final String name, final Bitmap icon) throws IllegalArgumentException {

        if (appType == null) {
            throw new IllegalArgumentException("app type cannot be null");
        }

        if (name.length() > NAME_MAX_LENGTH) {
            throw new IllegalArgumentException(String.format(
                    "app name exceeds maximum length (%d)", NAME_MAX_LENGTH));
        }

        if (icon.getHeight() > ICON_MAX_DIMENSIONS || icon.getWidth() > ICON_MAX_DIMENSIONS) {
            throw new IllegalArgumentException(String.format(
                    "app icon exceeds maximum dimensions (25px x 25px); got (%dpx x %dpx)",
                    icon.getWidth(), icon.getHeight()));
        }

        final Intent customizeAppIntent = new Intent(INTENT_APP_CUSTOMIZE);
        customizeAppIntent.putExtra(CUST_APP_TYPE, appType.ord);
        customizeAppIntent.putExtra(CUST_NAME, name);
        customizeAppIntent.putExtra(CUST_ICON, icon);
        context.sendBroadcast(customizeAppIntent);
    }

    /**
     * Synchronously query the Pebble application to see if an active Bluetooth connection to a watch currently exists.
     *
     * @param context The Android context used to perform the query.
     *
     *                <em>Protip:</em> You probably want to use your ApplicationContext here.
     * @return true if an active connection to the watch currently exists, otherwise false. This method will also return
     * false if the Pebble application is not installed on the user's handset.
     */
    public static boolean isWatchConnected(final Context context) {
        Cursor c = null;
        try {
            c = queryProvider(context);
            if (c == null || !c.moveToNext()) {
                return false;
            }
            return c.getInt(KIT_STATE_COLUMN_CONNECTED) == 1;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    /**
     * Synchronously query the Pebble application to see if the connected watch is running a firmware version that
     * supports PebbleKit messages.
     *
     * @param context The Android context used to perform the query.
     *
     *                <em>Protip:</em> You probably want to use your ApplicationContext here.
     * @return true if the watch supports PebbleKit messages, otherwise false. This method will always return false if
     * no Pebble is currently connected to the handset.
     */
    public static boolean areAppMessagesSupported(final Context context) {
        Cursor c = null;
        try {
            c = queryProvider(context);
            if (c == null || !c.moveToNext()) {
                return false;
            }
            return c.getInt(KIT_STATE_COLUMN_APPMSG_SUPPORT) == 1;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }


    /**
     * Get the version information of the firmware running on a connected watch.
     *
     * @param context The Android context used to perform the query.
     *
     *                <em>Protip:</em> You probably want to use your ApplicationContext here.
     * @return null if the watch is disconnected or we can't get the version. Otherwise,
     * a FirmwareVersionObject containing info on the watch FW version
     */
    public static FirmwareVersionInfo getWatchFWVersion(final Context context) {
        Cursor c = null;
        try {
            c = queryProvider(context);
            if (c == null || !c.moveToNext()) {
                return null;
            }

            int majorVersion = c.getInt(KIT_STATE_COLUMN_VERSION_MAJOR);
            int minorVersion = c.getInt(KIT_STATE_COLUMN_VERSION_MINOR);
            int pointVersion = c.getInt(KIT_STATE_COLUMN_VERSION_POINT);
            String versionTag = c.getString(KIT_STATE_COLUMN_VERSION_TAG);

            return new FirmwareVersionInfo(majorVersion, minorVersion, pointVersion, versionTag);
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    /**
     * Synchronously query the Pebble application to see if the connected watch is running a firmware version that
     * supports PebbleKit data logging.
     *
     * @param context The Android context used to perform the query.
     *
     *                <em>Protip:</em> You probably want to use your ApplicationContext here.
     * @return true if the watch supports PebbleKit messages, otherwise false. This method will always return false if
     * no Pebble is currently connected to the handset.
     */
    public static boolean isDataLoggingSupported(final Context context) {
        Cursor c = null;
        try {
            c = queryProvider(context);
            if (c == null || !c.moveToNext()) {
                return false;
            }
            return c.getInt(KIT_STATE_COLUMN_DATALOGGING_SUPPORT) == 1;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    /**
     * Send a message to the connected Pebble to launch an application identified by a UUID. If another application is
     * currently running it will be terminated and the new application will be brought to the foreground.
     *
     * @param context      The context used to send the broadcast.
     * @param watchappUuid A UUID uniquely identifying the target application. UUIDs for the stock PebbleKit applications are
     *                     available in {@link Constants}.
     * @throws IllegalArgumentException Thrown if the specified UUID is invalid.
     */
    public static void startAppOnPebble(final Context context, final UUID watchappUuid)
            throws IllegalArgumentException {

        if (watchappUuid == null) {
            throw new IllegalArgumentException("uuid cannot be null");
        }

        final Intent startAppIntent = new Intent(INTENT_APP_START);
        startAppIntent.putExtra(APP_UUID, watchappUuid);
        context.sendBroadcast(startAppIntent);
    }

    /**
     * Send a message to the connected Pebble to close an application identified by a UUID. If this application is not
     * currently running, the message is ignored.
     *
     * @param context      The context used to send the broadcast.
     * @param watchappUuid A UUID uniquely identifying the target application. UUIDs for the stock kit applications are available in
     *                     {@link Constants}.
     * @throws IllegalArgumentException Thrown if the specified UUID is invalid.
     */
    public static void closeAppOnPebble(final Context context, final UUID watchappUuid)
            throws IllegalArgumentException {

        if (watchappUuid == null) {
            throw new IllegalArgumentException("uuid cannot be null");
        }

        final Intent stopAppIntent = new Intent(INTENT_APP_STOP);
        stopAppIntent.putExtra(APP_UUID, watchappUuid);
        context.sendBroadcast(stopAppIntent);
    }

    /**
     * Send one-or-more key-value pairs to the watch-app identified by the provided UUID. This is the primary method for
     * sending data from the phone to a connected Pebble.
     * <p>
     * The watch-app and phone-app must agree of the set and type of key-value pairs being exchanged. Type mismatches or
     * missing keys will cause errors on the receiver's end.
     *
     * @param context      The context used to send the broadcast.
     * @param watchappUuid A UUID uniquely identifying the target application. UUIDs for the stock kit applications are available in
     *                     {@link Constants}.
     * @param data         A dictionary containing one-or-more key-value pairs. For more information about the types of data that
     *                     can be stored, see {@link PebbleDictionary}.
     * @throws IllegalArgumentException Thrown in the specified PebbleDictionary or UUID is invalid.
     */
    public static void sendDataToPebble(final Context context, final UUID watchappUuid,
                                        final PebbleDictionary data) throws IllegalArgumentException {

        sendDataToPebbleWithTransactionId(context, watchappUuid, data, -1);
    }

    /**
     * Send one-or-more key-value pairs to the watch-app identified by the provided UUID.
     * <p>
     * The watch-app and phone-app must agree of the set and type of key-value pairs being exchanged. Type mismatches or
     * missing keys will cause errors on the receiver's end.
     *
     * @param context       The context used to send the broadcast.
     * @param watchappUuid  A UUID uniquely identifying the target application. UUIDs for the stock kit applications are available in
     *                      {@link Constants}.
     * @param data          A dictionary containing one-or-more key-value pairs. For more information about the types of data that
     *                      can be stored, see {@link PebbleDictionary}.
     * @param transactionId An integer uniquely identifying the transaction. This can be used to correlate messages sent to the
     *                      Pebble and ACK/NACKs received from the Pebble.
     * @throws IllegalArgumentException Thrown in the specified PebbleDictionary or UUID is invalid.
     */
    public static void sendDataToPebbleWithTransactionId(final Context context,
                                                         final UUID watchappUuid, final PebbleDictionary data, final int transactionId)
            throws IllegalArgumentException {

        if (watchappUuid == null) {
            throw new IllegalArgumentException("uuid cannot be null");
        }

        if (data == null) {
            throw new IllegalArgumentException("data cannot be null");
        }

        if (data.size() == 0) {
            return;
        }

        final Intent sendDataIntent = new Intent(INTENT_APP_SEND);
        sendDataIntent.putExtra(APP_UUID, watchappUuid);
        sendDataIntent.putExtra(TRANSACTION_ID, transactionId);
        sendDataIntent.putExtra(MSG_DATA, data.toJsonString());
        context.sendBroadcast(sendDataIntent);
    }

    /**
     * Send a message to the connected watch acknowledging the receipt of a PebbleDictionary. To avoid protocol timeouts
     * on the watch, applications <em>must</em> ACK or NACK all received messages.
     *
     * @param context       The context used to send the broadcast.
     * @param transactionId The transaction id of the message in which the data was received. Valid transaction IDs are between (0,
     *                      255).
     * @throws IllegalArgumentException Thrown if an invalid transaction id is specified.
     */
    public static void sendAckToPebble(final Context context, final int transactionId)
            throws IllegalArgumentException {

        if ((transactionId & ~0xff) != 0) {
            throw new IllegalArgumentException(String.format(
                    "transaction id must be between (0, 255); got '%d'", transactionId));
        }

        final Intent ackIntent = new Intent(INTENT_APP_ACK);
        ackIntent.putExtra(TRANSACTION_ID, transactionId);
        context.sendBroadcast(ackIntent);
    }

    /**
     * Send a message to the connected watch that the previously sent PebbleDictionary was not received successfully. To
     * avoid protocol timeouts on the watch, applications <em>must</em> ACK or NACK all received messages.
     *
     * @param context       The context used to send the broadcast.
     * @param transactionId The transaction id of the message in which the data was received. Valid transaction IDs are between (0,
     *                      255).
     * @throws IllegalArgumentException Thrown if an invalid transaction id is specified.
     */
    public static void sendNackToPebble(final Context context, final int transactionId)
            throws IllegalArgumentException {

        if ((transactionId & ~0xff) != 0) {
            throw new IllegalArgumentException(String.format(
                    "transaction id must be between (0, 255); got '%d'", transactionId));
        }

        final Intent nackIntent = new Intent(INTENT_APP_NACK);
        nackIntent.putExtra(TRANSACTION_ID, transactionId);
        context.sendBroadcast(nackIntent);
    }

    /**
     * A convenience function to assist in programatically registering a broadcast receiver for the 'CONNECTED' intent.
     * <p>
     * To avoid leaking memory, activities registering BroadcastReceivers <em>must</em> unregister them in the
     * Activity's {@link android.app.Activity#onPause()} method.
     *
     * @param context  The context in which to register the BroadcastReceiver.
     * @param receiver The receiver to be registered.
     * @return The registered receiver.
     * @see Constants#INTENT_PEBBLE_CONNECTED
     */
    public static BroadcastReceiver registerPebbleConnectedReceiver(final Context context,
                                                                    final BroadcastReceiver receiver) {
        return registerBroadcastReceiverInternal(context, INTENT_PEBBLE_CONNECTED, receiver);
    }

    /**
     * A convenience function to assist in programatically registering a broadcast receiver for the 'DISCONNECTED'
     * intent.
     * <p>
     * Go avoid leaking memory, activities registering BroadcastReceivers <em>must</em> unregister them in the
     * Activity's {@link android.app.Activity#onPause()} method.
     *
     * @param context  The context in which to register the BroadcastReceiver.
     * @param receiver The receiver to be registered.
     * @return The registered receiver.
     * @see Constants#INTENT_PEBBLE_DISCONNECTED
     */
    public static BroadcastReceiver registerPebbleDisconnectedReceiver(final Context context,
                                                                       final BroadcastReceiver receiver) {
        return registerBroadcastReceiverInternal(context, INTENT_PEBBLE_DISCONNECTED, receiver);
    }

    /**
     * A convenience function to assist in programatically registering a broadcast receiver for the 'RECEIVE' intent.
     * <p>
     * To avoid leaking memory, activities registering BroadcastReceivers <em>must</em> unregister them in the
     * Activity's {@link android.app.Activity#onPause()} method.
     *
     * @param context  The context in which to register the BroadcastReceiver.
     * @param receiver The receiver to be registered.
     * @return The registered receiver.
     * @see Constants#INTENT_APP_RECEIVE
     */
    public static BroadcastReceiver registerReceivedDataHandler(final Context context,
                                                                final PebbleDataReceiver receiver) {
        return registerBroadcastReceiverInternal(context, INTENT_APP_RECEIVE, receiver);
    }


    /**
     * A convenience function to assist in programatically registering a broadcast receiver for the 'RECEIVE_ACK'
     * intent.
     * <p>
     * To avoid leaking memory, activities registering BroadcastReceivers <em>must</em> unregister them in the
     * Activity's {@link android.app.Activity#onPause()} method.
     *
     * @param context  The context in which to register the BroadcastReceiver.
     * @param receiver The receiver to be registered.
     * @return The registered receiver.
     * @see Constants#INTENT_APP_RECEIVE_ACK
     */
    public static BroadcastReceiver registerReceivedAckHandler(final Context context,
                                                               final PebbleAckReceiver receiver) {
        return registerBroadcastReceiverInternal(context, INTENT_APP_RECEIVE_ACK, receiver);
    }

    /**
     * A convenience function to assist in programatically registering a broadcast receiver for the 'RECEIVE_NACK'
     * intent.
     * <p>
     * To avoid leaking memory, activities registering BroadcastReceivers <em>must</em> unregister them in the
     * Activity's {@link android.app.Activity#onPause()} method.
     *
     * @param context  The context in which to register the BroadcastReceiver.
     * @param receiver The receiver to be registered.
     * @return The registered receiver.
     * @see Constants#INTENT_APP_RECEIVE_NACK
     */
    public static BroadcastReceiver registerReceivedNackHandler(final Context context,
                                                                final PebbleNackReceiver receiver) {
        return registerBroadcastReceiverInternal(context, INTENT_APP_RECEIVE_NACK, receiver);
    }

    /**
     * Register broadcast receiver internal.
     *
     * @param context  the context
     * @param action   the action
     * @param receiver the receiver
     * @return the broadcast receiver
     */
    private static BroadcastReceiver registerBroadcastReceiverInternal(final Context context,
                                                                       final String action, final BroadcastReceiver receiver) {
        if (receiver == null) {
            return null;
        }

        IntentFilter filter = new IntentFilter(action);
        context.registerReceiver(receiver, filter);
        return receiver;
    }

    /**
     * A convenience function to assist in programatically registering a broadcast receiver for the 'DATA_AVAILABLE'
     * intent.
     * <p>
     * To avoid leaking memory, activities registering BroadcastReceivers <em>must</em> unregister them in the
     * Activity's {@link android.app.Activity#onPause()} method.
     *
     * @param context  The context in which to register the BroadcastReceiver.
     * @param receiver The receiver to be registered.
     * @return The registered receiver.
     * @see Constants#INTENT_DL_RECEIVE_DATA
     */
    public static BroadcastReceiver registerDataLogReceiver(final Context context,
                                                            final PebbleDataLogReceiver receiver) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_DL_RECEIVE_DATA);
        filter.addAction(INTENT_DL_FINISH_SESSION);
        context.registerReceiver(receiver, filter);

        return receiver;
    }

    /**
     * A convenience function to emit an intent to pebble.apk to request the data logs for a particular app. If data
     * is available, pebble.apk will advertise the data via 'INTENT_DL_RECEIVE_DATA' intents.
     * <p>
     * To avoid leaking memory, activities registering BroadcastReceivers <em>must</em> unregister them in the
     * Activity's {@link android.app.Activity#onPause()} method.
     *
     * @param context The context in which to register the BroadcastReceiver.
     * @param appUuid The app for which to request data logs.
     * @see Constants#INTENT_DL_RECEIVE_DATA
     * @see Constants#INTENT_DL_REQUEST_DATA
     */
    public static void requestDataLogsForApp(final Context context, final UUID appUuid) {
        final Intent requestIntent = new Intent(INTENT_DL_REQUEST_DATA);
        requestIntent.putExtra(APP_UUID, appUuid);
        context.sendBroadcast(requestIntent);
    }

    /**
     * Query the Pebble ContentProvider - utility method for various PebbleKit helper methods
     * <p>
     * This attempts first to query the Basalt app provider, then if that is non-existant or disconnected
     * queries the primary (i.e. original) provider authority
     */
    private static Cursor queryProvider(final Context context) {
        Cursor c = context.getContentResolver().query(Constants.URI_CONTENT_BASALT, null, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                // If Basalt app is connected, talk to that (return the open cursor)
                if (c.getInt(KIT_STATE_COLUMN_CONNECTED) == 1) {
                    c.moveToPrevious();
                    return c;
                }
            }
            // Else close the cursor and try the primary authority
            c.close();
        }

        c = context.getContentResolver().query(Constants.URI_CONTENT_PRIMARY, null, null, null, null);
        // Do nothing with this cursor - the calling method will check it for whatever it is looking for
        return c;
    }

    /**
     * A special-purpose BroadcastReceiver that makes it easy to handle 'RECEIVE' intents broadcast from pebble.apk.
     */
    public static abstract class PebbleDataReceiver extends BroadcastReceiver {

        /**
         * The subscribed uuid.
         */
        private final UUID subscribedUuid;

        /**
         * Instantiates a new pebble data receiver.
         *
         * @param subscribedUuid the subscribed uuid
         */
        protected PebbleDataReceiver(final UUID subscribedUuid) {
            this.subscribedUuid = subscribedUuid;
        }

        /**
         * Perform some work on the data received from the connected watch.
         *
         * @param context       The BroadcastReceiver's context.
         * @param transactionId The transaction ID of the message in which the data was received. This is required when ACK/NACKing
         *                      the received message.
         * @param data          A dictionary of one-or-more key-value pairs received from the connected watch.
         */
        public abstract void receiveData(final Context context, final int transactionId,
                                         final PebbleDictionary data);

        /**
         * {@inheritDoc}
         */
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final UUID receivedUuid = (UUID) intent.getSerializableExtra(APP_UUID);

            // Pebble-enabled apps are expected to be good citizens and only inspect broadcasts
            // containing their UUID
            if (!subscribedUuid.equals(receivedUuid)) {
                return;
            }

            final int transactionId = intent.getIntExtra(TRANSACTION_ID, -1);
            final String jsonData = intent.getStringExtra(MSG_DATA);
            if (jsonData == null || jsonData.isEmpty()) {
                return;
            }

            try {
                final PebbleDictionary data = PebbleDictionary.fromJson(jsonData);
                receiveData(context, transactionId, data);
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    /**
     * A special-purpose BroadcastReceiver that makes it easy to handle 'RECEIVE_ACK' intents broadcast from pebble
     * .apk.
     */
    public static abstract class PebbleAckReceiver extends BroadcastReceiver {

        /**
         * The subscribed uuid.
         */
        private final UUID subscribedUuid;

        /**
         * Instantiates a new pebble ack receiver.
         *
         * @param subscribedUuid the subscribed uuid
         */
        protected PebbleAckReceiver(final UUID subscribedUuid) {
            this.subscribedUuid = subscribedUuid;
        }

        /**
         * Handle the ACK received from the connected watch.
         *
         * @param context       The BroadcastReceiver's context.
         * @param transactionId The transaction ID of the message for which the ACK was received. This indicates which message was
         *                      successfully received.
         */
        public abstract void receiveAck(final Context context, final int transactionId);

        /**
         * {@inheritDoc}
         */
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final int transactionId = intent.getIntExtra(TRANSACTION_ID, -1);
            receiveAck(context, transactionId);

        }
    }

    /**
     * A special-purpose BroadcastReceiver that makes it easy to handle 'RECEIVE_NACK' intents broadcast from pebble
     * .apk.
     */
    public static abstract class PebbleNackReceiver extends BroadcastReceiver {

        /**
         * The subscribed uuid.
         */
        private final UUID subscribedUuid;

        /**
         * Instantiates a new pebble nack receiver.
         *
         * @param subscribedUuid the subscribed uuid
         */
        protected PebbleNackReceiver(final UUID subscribedUuid) {
            this.subscribedUuid = subscribedUuid;
        }

        /**
         * Handle the NACK received from the connected watch.
         *
         * @param context       The BroadcastReceiver's context.
         * @param transactionId The transaction ID of the message for which the NACK was received. This indicates which message was
         *                      not received.
         */
        public abstract void receiveNack(final Context context, final int transactionId);

        /**
         * {@inheritDoc}
         */
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final int transactionId = intent.getIntExtra(TRANSACTION_ID, -1);
            receiveNack(context, transactionId);

        }
    }

    /**
     * A special-purpose BroadcastReceiver that makes it easy to handle 'DATA_AVAILABLE' data logging intents broadcast from pebble.apk.
     */
    public static abstract class PebbleDataLogReceiver extends BroadcastReceiver {

        /**
         * The subscribed uuid.
         */
        private final UUID subscribedUuid;

        /**
         * The last data ID we've seen. Ignore subsequent intents for this same ID.
         */
        private int lastDataId;

        /**
         * Instantiates a new pebble nack receiver.
         *
         * @param subscribedUuid the subscribed uuid
         */
        protected PebbleDataLogReceiver(final UUID subscribedUuid) {
            this.subscribedUuid = subscribedUuid;
        }

        /**
         * Handle an UnsignedInteger data unit that was logged the watch and broadcast by pebble.apk.
         *
         * @param context   The BroadcastReceiver's context.
         * @param logUuid   The UUID that uniquely identifies a data log.
         * @param timestamp The timestamp when a data log was first created.
         * @param tag       The user-defined tag for the corresponding data log.
         * @param data      The unit of data that was logged on the watch.
         * @throws UnsupportedOperationException Thrown if data is received and this handler is not implemented.
         */
        public void receiveData(final Context context, UUID logUuid,
                                final Long timestamp, final Long tag,
                                final Long data) {
            throw new UnsupportedOperationException("UnsignedInteger handler not implemented");

        }

        /**
         * Handle a byte array data unit that was logged the watch and broadcast by pebble.apk.
         *
         * @param context   The BroadcastReceiver's context.
         * @param logUuid   The UUID that uniquely identifies a data log.
         * @param timestamp The timestamp when a data log was first created.
         * @param tag       The user-defined tag for the corresponding data log.
         * @param data      The unit of data that was logged on the watch.
         * @throws UnsupportedOperationException Thrown if data is received and this handler is not implemented.
         */
        public void receiveData(final Context context, UUID logUuid,
                                final Long timestamp, final Long tag,
                                final byte[] data) {
            throw new UnsupportedOperationException("Byte array handler not implemented");
        }

        /**
         * Handle an int data unit that was logged the watch and broadcast by pebble.apk.
         *
         * @param context   The BroadcastReceiver's context.
         * @param logUuid   The UUID that uniquely identifies a data log.
         * @param timestamp The timestamp when a data log was first created.
         * @param tag       The user-defined tag for the corresponding data log.
         * @param data      The unit of data that was logged on the watch.
         * @throws UnsupportedOperationException Thrown if data is received and this handler is not implemented.
         */
        public void receiveData(final Context context, UUID logUuid,
                                final Long timestamp, final Long tag, final int data) {
            throw new UnsupportedOperationException("int handler not implemented");

        }

        /**
         * Called when a session has been finished on the watch and all data has been transmitted by pebble.apk
         *
         * @param context   The BroadcastReceiver's context.
         * @param logUuid   The UUID that uniquely identifies a data log.
         * @param timestamp The timestamp when a data log was first created.
         * @param tag       The user-defined tag for the corresponding data log.
         */
        public void onFinishSession(final Context context, UUID logUuid, final Long timestamp,
                                    final Long tag) {
            // Do nothing by default
        }

        private void handleReceiveDataIntent(final Context context, final Intent intent, final UUID logUuid,
                                             final Long timestamp, final Long tag) {
            final int dataId = intent.getIntExtra(PBL_DATA_ID, -1);
            if (dataId < 0) throw new IllegalArgumentException();

            Log.i("pebble", "DataID: " + dataId + " LastDataID: " + lastDataId);

            if (dataId == lastDataId) {
                // If we see the same dataId multiple times, just ignore it.
                return;
            }

            final PebbleDataType type = PebbleDataType.fromByte(intent.getByteExtra(PBL_DATA_TYPE, PebbleDataType.INVALID.ord));
            if (type == null) throw new IllegalArgumentException();

            switch (type) {
                case BYTES:
                    byte[] bytes = Base64.decode(intent.getStringExtra(PBL_DATA_OBJECT), Base64.NO_WRAP);
                    if (bytes == null) {
                        throw new IllegalArgumentException();
                    }

                    receiveData(context, logUuid, timestamp, tag, bytes);
                    break;
                case UINT:
                    Long uint = (Long) intent.getSerializableExtra(PBL_DATA_OBJECT);
                    if (uint == null) {
                        throw new IllegalArgumentException();
                    }

                    receiveData(context, logUuid, timestamp, tag, uint);
                    break;
                case INT:
                    Integer i = (Integer) intent.getSerializableExtra(PBL_DATA_OBJECT);
                    if (i == null) {
                        throw new IllegalArgumentException();
                    }

                    receiveData(context, logUuid, timestamp, tag, i.intValue());
                    break;
                default:
                    throw new IllegalArgumentException("Invalid type:" + type.toString());
            }

            lastDataId = dataId;

            final Intent ackIntent = new Intent(INTENT_DL_ACK_DATA);
            ackIntent.putExtra(DATA_LOG_UUID, logUuid);
            ackIntent.putExtra(PBL_DATA_ID, dataId);
            context.sendBroadcast(ackIntent);
        }

        private void handleFinishSessionIntent(final Context context, final Intent intent, final UUID logUuid,
                                               final Long timestamp, final Long tag) {
            onFinishSession(context, logUuid, timestamp, tag);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final UUID receivedUuid = (UUID) intent.getSerializableExtra(APP_UUID);
            // Pebble-enabled apps are expected to be good citizens and only inspect broadcasts
            // containing their UUID
            if (!subscribedUuid.equals(receivedUuid)) {
                return;
            }

            try {
                final UUID logUuid;
                final Long timestamp;
                final Long tag;

                logUuid = (UUID) intent.getSerializableExtra(DATA_LOG_UUID);
                if (logUuid == null) throw new IllegalArgumentException();

                timestamp = (Long) intent.getSerializableExtra(DATA_LOG_TIMESTAMP);
                if (timestamp == null) throw new IllegalArgumentException();

                tag = (Long) intent.getSerializableExtra(DATA_LOG_TAG);
                if (tag == null) throw new IllegalArgumentException();

                if (intent.getAction() == INTENT_DL_RECEIVE_DATA) {
                    handleReceiveDataIntent(context, intent, logUuid, timestamp, tag);
                } else if (intent.getAction() == INTENT_DL_FINISH_SESSION) {
                    handleFinishSessionIntent(context, intent, logUuid, timestamp, tag);
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public static class FirmwareVersionInfo {
        private final int major;
        private final int minor;
        private final int point;
        private final String tag;

        FirmwareVersionInfo(int major, int minor, int point, String tag) {
            this.major = major;
            this.minor = minor;
            this.point = point;
            this.tag = tag;
        }

        public final int getMajor() {
            return major;
        }

        public final int getMinor() {
            return minor;
        }

        public final int getPoint() {
            return point;
        }

        public final String getTag() {
            return tag;
        }
    }
}
