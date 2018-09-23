package xyz.mrdeveloper.her;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.maps.android.SphericalUtil;

import static xyz.mrdeveloper.her.MainActivity.PROXIMITY_RADIUS;

public class EmergencyCheckService extends FirebaseMessagingService {

    PersonData personInEmergency;

    String mPhoneNumber;

    private static final String TAG = EmergencyCheckService.class.getSimpleName();
    private static final String ACTION_PROCESS_UPDATES = "xyz.mrdeveloper.her.ACTION_PROCESS_UPDATES";

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("debug", "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            personInEmergency = new PersonData();
            personInEmergency.phoneNumber = remoteMessage.getData().get("personInEmergencyNumber");
            personInEmergency.latitude = Float.parseFloat(remoteMessage.getData().get("personInEmergencyLatitude"));
            personInEmergency.longitude = Float.parseFloat(remoteMessage.getData().get("personInEmergencyLongitude"));

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            mPhoneNumber = sharedPreferences.getString("myPhoneNumber", "No phone number");

            Log.d(TAG, personInEmergency.phoneNumber + " != " + mPhoneNumber);
            if (!personInEmergency.phoneNumber.equals(mPhoneNumber)) {
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                CreateLocationRequest();
                CreateLocationCallback();
                StartLocationUpdates();

//                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                    mFusedLocationClient.getLastLocation()
//                            .addOnSuccessListener(new OnSuccessListener<Location>() {
//                                @Override
//                                public void onSuccess(Location location) {
//                                    // Got last known location. In some rare situations this can be null.
//                                    if (location != null) {
//                                        mCurrentLocation = location;
//                                        Log.i(TAG, "In on location result");
//                                        if (SphericalUtil.computeDistanceBetween(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()),
//                                                new LatLng(personInEmergency.latitude, personInEmergency.longitude)) < PROXIMITY_RADIUS) {
//                                            Log.i(TAG, "Emergency is in proximity");
//                                            SendEmergencyNotification();
//                                        }
//                                    }else{
//                                        Log.i(TAG, "Location is null");
//                                    }
//                                }
//                            });
//                }
            }

//            Intent intent = new Intent("android.intent.category.LAUNCHER");
//            intent.setClassName("xyz.mrdeveloper.her", "xyz.mrdeveloper.her.MainActivity");
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d("debug", "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

    }

    @Override
    public void onNewToken(String token) {
        Log.d("debug", "Refreshed token: " + token);

        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }

    private void CreateLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void CreateLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null) {
                    mCurrentLocation = locationResult.getLastLocation();
                    Log.i(TAG, "In on location result");
                    if (SphericalUtil.computeDistanceBetween(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()),
                            new LatLng(personInEmergency.latitude, personInEmergency.longitude)) < PROXIMITY_RADIUS) {
                        Log.i(TAG, "Emergency is in proximity");
                        SendEmergencyNotification();
                    }
                    StopLocationUpdates();
                }
            }
        };
    }

//    private PendingIntent getPendingIntent() {
//        Intent intent = new Intent(this, LocalBroadcastManager.class);
//        intent.setAction("xyz.mrdeveloper.her.ACTION_PROCESS_UPDATES");
//        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//
//        Intent intent = new Intent(this, LocationUpdatesBroadcastReceiver.class);
//        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
////        intent.putExtra("personInEmergencyNumber", personInEmergency.phoneNumber);
////        intent.putExtra("personInEmergencyLatitude", personInEmergency.latitude);
////        intent.putExtra("personInEmergencyLongitude", personInEmergency.longitude);
//        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//    }

    @SuppressLint("MissingPermission")
    private void StartLocationUpdates() {
//        mFusedLocationClient.requestLocationUpdates(mLocationRequest, getPendingIntent());
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper());
        Log.i(TAG, "Starting location updates");
    }

    private void StopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
//        mFusedLocationClient.removeLocationUpdates(getPendingIntent());
        Log.i(TAG, "Stopping location updates");
    }

    private void SendEmergencyNotification() {

        Intent intent = new Intent(this, EmergencyActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("workingMode", "PROTECTOR");
        intent.putExtra("personInEmergencyNumber", personInEmergency.phoneNumber);
        intent.putExtra("personInEmergencyLatitude", personInEmergency.latitude);
        intent.putExtra("personInEmergencyLongitude", personInEmergency.longitude);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        String channelId = getString(R.string.default_notification_channel_id);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.her_logo_final)
                        .setContentTitle("Someone near you is in danger!")
                        .setContentText("Open this notification to help them")
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setSound(defaultSoundUri)
                        .setVibrate(new long[]{1000, 1000})
                        .setLights(Color.RED, 1000, 100)
                        .setContentIntent(pendingIntent);


        if (notificationManager != null) {
            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        }
    }
}