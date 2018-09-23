package xyz.mrdeveloper.her;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * Distance in meters within which to notify protectors of an emergency
     */
    public static final int PROXIMITY_RADIUS = 1000;

    /**
     * Distance in meters within to show safe places
     * Is 10x protector radius
     */
    public static final int PROXIMITY_PLACES_RADIUS = 10000;

    private final String TAG = MainActivity.class.getSimpleName();

    /**
     * Code used in requesting runtime permissions.
     */
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    /**
     * Constant used in the location settings dialog.
     */
    private static final int REQUEST_CHECK_SETTINGS = 42;

    /**
     * Constant used in the location settings dialog.
     */
    private static final int REQUEST_OVERLAY_SETTINGS = 197;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    private final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    private final static String KEY_LOCATION = "location";

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Provides access to the Location Settings API.
     */
    private SettingsClient mSettingsClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private LocationSettingsRequest mLocationSettingsRequest;

    /**
     * Callback for Location events.
     */
    private LocationCallback mLocationCallback;

    /**
     * Represents a geographical location.
     */
//    private Location mCurrentLocation;

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    private Boolean mRequestingLocationUpdates;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        FirebaseMessaging.getInstance().subscribeToTopic("emergency")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Subscription Successful";
                        if (!task.isSuccessful()) {
                            msg = "Subscription Failed";
                        }
                        Log.d("Debug", msg);
//                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                    }
                });


        // Update values using data stored in the Bundle.
//        UpdateValuesFromBundle(savedInstanceState);

//        mRequestingLocationUpdates = false;
//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//        mSettingsClient = LocationServices.getSettingsClient(this);

//        //Kick off the process of building the LocationCallback, LocationRequest, and LocationSettingsRequest objects.
//        CreateLocationCallback();
//        CreateLocationRequest();
//        BuildLocationSettingsRequest();

//        RequestLocationPermissionAndStartService();

//        startService(new Intent(this, ScreenOnOffService.class));

//        VolumeChangeObserver volumeChangeObserver = new VolumeChangeObserver(this);
//        volumeChangeObserver.StartObserver(AudioManager.STREAM_NOTIFICATION);

//        if (CheckOverlayPermissions()) {
//            startService(new Intent(this, HardwareButtonsService.class));
//        }
//
//        Window window = this.getWindow();
//        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
//        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
//        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
//        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
//            setShowWhenLocked(true);
//            setTurnScreenOn(true);
//        }

        ImageButton alertButton = findViewById(R.id.emergency_button);
        ImageButton helpButton = findViewById(R.id.safe_places_button);
        ImageButton familyButton = findViewById(R.id.family_button);
        ImageButton tipsButton = findViewById(R.id.tips_button);
        ImageButton callButton = findViewById(R.id.call_button);
        ImageButton logoutButton = findViewById(R.id.log_out_button);
        ImageButton aboutButton = findViewById(R.id.about_button);

        alertButton.setOnClickListener(this);
        helpButton.setOnClickListener(this);
        familyButton.setOnClickListener(this);
        tipsButton.setOnClickListener(this);
        callButton.setOnClickListener(this);
        logoutButton.setOnClickListener(this);
        aboutButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.emergency_button:
                Intent intent = new Intent(this, EmergencyActivity.class);
                intent.putExtra("workingMode", "EMERGENCY");
                startActivity(intent);
                break;

            case R.id.safe_places_button:
                intent = new Intent(this, EmergencyActivity.class);
                intent.putExtra("workingMode", "SAFE_PLACES");
                startActivity(intent);
                break;

            case R.id.family_button:
                intent = new Intent(this, FamilyMemberActivity.class);
                startActivity(intent);
                break;

            case R.id.tips_button:
                intent = new Intent(this, TipsActivity.class);
                startActivity(intent);
                break;

            case R.id.call_button:
                new AlertDialog.Builder(this)
                        .setTitle("We get it ;)")
                        .setMessage("So we should call you in ..")
                        .setPositiveButton(R.string.ten_seconds, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                FakeCallMeInSeconds(0);
                            }
                        })
                        .setNeutralButton(R.string.one_minute, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                FakeCallMeInSeconds(60);
                            }
                        })
                        .setNegativeButton(R.string.five_minutes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                FakeCallMeInSeconds(60 * 5);
                            }
                        })
                        .create()
                        .show();

//                myIntent = new Intent(MainActivity.this, FakeCallActivity.class);
//                MainActivity.this.startActivity(myIntent);

//                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

//                Intent intent = new Intent(this, FakeMainActivity.class);
//                startActivity(intent);

//                intent.putExtra("FAKENAME", "ME");
//                intent.putExtra("FAKENUMBER", "7409150503");
//
//                PendingIntent fakePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//                alarmManager.set(AlarmManager.RTC_WAKEUP, 2000, fakePendingIntent);
//                Toast.makeText(getApplicationContext(), "Your fake call time has been set", Toast.LENGTH_SHORT).show();
//
//                Intent intents = new Intent(this, MainActivity.class);
//                startActivity(intents);
                break;

            case R.id.about_button:
                startActivity(new Intent(this, AboutActivity.class));
                break;

            case R.id.log_out_button:
                mAuth.signOut();
                finish();
                startActivity(new Intent(this, SignupActivity.class));
                break;

        }
    }

    private void FakeCallMeInSeconds(int selectedTimeInSeconds) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, FakeCallReceiver.class);

        intent.setAction("xyz.mrdeveloper.her.ACTION_FAKE_CALL");

        PendingIntent fakePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + selectedTimeInSeconds * 1000, fakePendingIntent);
        }
        Toast.makeText(getApplicationContext(), "We will take care of the problem in " + selectedTimeInSeconds + " seconds ;)", Toast.LENGTH_SHORT).show();
    }
//    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

//    private void RequestLocationPermissionAndStartService() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.ACCESS_FINE_LOCATION)) {
//
//                new AlertDialog.Builder(this)
//                        .setTitle("Location Permission Needed")
//                        .setMessage("This app needs the Location permission, please accept to use location functionality")
//                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                //Prompt the user once explanation has been shown
//                                ActivityCompat.RequestPermissions(MainActivity.this,
//                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                                        MY_PERMISSIONS_REQUEST_LOCATION);
//                            }
//                        })
//                        .create()
//                        .show();
//
//
//            } else {
//                ActivityCompat.RequestPermissions(this,
//                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                        MY_PERMISSIONS_REQUEST_LOCATION);
//            }
////        } else {
////            Log.i("PERMISSION", "PERMISSION IS GRANTED :)");
////            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
////            if (!sharedPreferences.getBoolean("isFirebaseCheckServiceRunning", false)) {
////                startService(new Intent(MainActivity.this, EmergencyCheckService.class));
////            }
//        }
//    }


//    /**
//     * Updates fields based on data stored in the bundle.
//     *
//     * @param savedInstanceState The activity state saved in the Bundle.
//     */
//    private void UpdateValuesFromBundle(Bundle savedInstanceState) {
//        if (savedInstanceState != null) {
//            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
//            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
//            if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
//                mRequestingLocationUpdates = savedInstanceState.getBoolean(
//                        KEY_REQUESTING_LOCATION_UPDATES);
//            }
//
////            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
////            // correct latitude and longitude.
////            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
////                // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
////                // is not null.
////                mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
////            }
//        }
//    }
//
//    /**
//     * Sets up the location request. Android has two location request settings:
//     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
//     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
//     * the AndroidManifest.xml.
//     * <p/>
//     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
//     * interval (5 seconds), the Fused Location Provider API returns location updates that are
//     * accurate to within a few feet.
//     * <p/>
//     * These settings are appropriate for mapping applications that show real-time location
//     * updates.
//     */
//    private void CreateLocationRequest() {
//        mLocationRequest = new LocationRequest();
//
//        // Sets the desired interval for active location updates. This interval is
//        // inexact. You may not receive updates at all if no location sources are available, or
//        // you may receive them slower than requested. You may also receive updates faster than
//        // requested if other applications are requesting location at a faster interval.
//        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
//
//        // Sets the fastest rate for active location updates. This interval is exact, and your
//        // application will never receive updates faster than this value.
//        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
//
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//    }
//
//    /**
//     * Creates a callback for receiving location events.
//     */
//    private void CreateLocationCallback() {
//        mLocationCallback = new LocationCallback() {
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                super.onLocationResult(locationResult);
//
////                mCurrentLocation = locationResult.getLastLocation();
//            }
//        };
//    }
//
//    /**
//     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
//     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
//     * if a device has the needed location settings.
//     */
//    private void BuildLocationSettingsRequest() {
//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
//        builder.addLocationRequest(mLocationRequest);
//        mLocationSettingsRequest = builder.build();
//    }
//
//    /**
//     * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
//     * runtime permission has been granted.
//     */
//    private void StartLocationUpdates() {
//        // Begin by checking if the device has the necessary location settings.
//        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
//                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
//                    @Override
//                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
//                        Log.i(TAG, "All location settings are satisfied.");
//
//                        //noinspection MissingPermission
////                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
////                                mLocationCallback, null);
//                    }
//                })
//                .addOnFailureListener(this, new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        int statusCode = ((ApiException) e).getStatusCode();
//                        switch (statusCode) {
//                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
//                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade location settings");
//                                try {
//                                    // Show the dialog by calling startResolutionForResult(), and check the
//                                    // result in onActivityResult().
//                                    ResolvableApiException rae = (ResolvableApiException) e;
//                                    rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
//                                } catch (IntentSender.SendIntentException sie) {
//                                    Log.i(TAG, "PendingIntent unable to execute request.");
//                                }
//                                break;
//                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
//                                String errorMessage = "Location settings are inadequate, and cannot be fixed here. Fix in Settings.";
//                                Log.e(TAG, errorMessage);
//                                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
//                                mRequestingLocationUpdates = false;
//                        }
//                    }
//                });
//    }
//
//    /**
//     * Removes location updates from the FusedLocationApi.
//     */
//    private void StopLocationUpdates() {
//        if (!mRequestingLocationUpdates) {
//            Log.d(TAG, "StopLocationUpdates: updates never requested, no-op.");
//            return;
//        }
//
//        // It is a good practice to remove location requests when the activity is in a paused or
//        // stopped state. Doing so helps battery performance and is especially
//        // recommended in applications that request frequent location updates.
//        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
//                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        mRequestingLocationUpdates = false;
//                    }
//                });
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        // Within {@code onPause()}, we remove location updates. Here, we resume receiving
//        // location updates if the user has requested them.
//        if (mRequestingLocationUpdates && CheckPermissions()) {
//            StartLocationUpdates();
//        } else if (!CheckPermissions()) {
//            RequestPermissions();
//        }
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        // Remove location updates to save battery.
//        StopLocationUpdates();
//    }
//
//    /**
//     * Stores activity data in the Bundle.
//     */
//    public void onSaveInstanceState(Bundle savedInstanceState) {
//        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);
////        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
//        super.onSaveInstanceState(savedInstanceState);
//    }
//
//    /**
//     * Shows a {@link Snackbar}.
//     *
//     * @param mainTextStringId The id for the string resource for the Snackbar text.
//     * @param actionStringId   The text of the action item.
//     * @param listener         The listener associated with the Snackbar action.
//     */
//    private void ShowSnackbar(final int mainTextStringId, final int actionStringId, View.OnClickListener listener) {
//        Snackbar.make(
//                findViewById(android.R.id.content),
//                getString(mainTextStringId),
//                Snackbar.LENGTH_INDEFINITE)
//                .setAction(getString(actionStringId), listener).show();
//    }
//
//    /**
//     * Return the current state of the permissions needed.
//     */
//    private boolean CheckPermissions() {
//        int permissionState = ActivityCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION);
//        return permissionState == PackageManager.PERMISSION_GRANTED;
//    }
//
//    private void RequestPermissions() {
//        boolean shouldProvideRationale =
//                ActivityCompat.shouldShowRequestPermissionRationale(this,
//                        Manifest.permission.ACCESS_FINE_LOCATION);
//
//        // Provide an additional rationale to the user. This would happen if the user denied the
//        // request previously, but didn't check the "Don't ask again" checkbox.
//        if (shouldProvideRationale) {
//            Log.i(TAG, "Displaying permission rationale to provide additional context.");
//            ShowSnackbar(R.string.location_permission_rationale,
//                    android.R.string.ok, new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            // Request permission
//                            ActivityCompat.requestPermissions(MainActivity.this,
//                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                                    REQUEST_PERMISSIONS_REQUEST_CODE);
//                        }
//                    });
//        } else {
//            Log.i(TAG, "Requesting permission");
//            // Request permission. It's possible this can be auto answered if device policy
//            // sets the permission in a given state or the user denied the permission
//            // previously and checked "Never ask again".
//            ActivityCompat.requestPermissions(MainActivity.this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    REQUEST_PERMISSIONS_REQUEST_CODE);
//        }
//    }
//
//    public boolean CheckOverlayPermissions() {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//            return true;
//        }
//        if (!Settings.canDrawOverlays(this)) {
//            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
//            startActivityForResult(intent, REQUEST_OVERLAY_SETTINGS);
//            return false;
//        } else {
//            return true;
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        switch (requestCode) {
//            // Check for the integer request code originally supplied to startResolutionForResult().
//            case REQUEST_CHECK_SETTINGS:
//                switch (resultCode) {
//                    case Activity.RESULT_OK:
//                        Log.i(TAG, "User agreed to make required location settings changes.");
//                        // Nothing to do. startLocationupdates() gets called in onResume again.
//                        break;
//                    case Activity.RESULT_CANCELED:
//                        Log.i(TAG, "User chose not to make required location settings changes.");
//                        mRequestingLocationUpdates = false;
//                        break;
//                }
//                break;
//            case REQUEST_OVERLAY_SETTINGS:
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    if (Settings.canDrawOverlays(this)) {
//                        startService(new Intent(this, HardwareButtonsService.class));
//                    }
//                }
//        }
//    }
//
//    /**
//     * Callback received when a permissions request has been completed.
//     */
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        Log.i(TAG, "onRequestPermissionResult");
//        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
//            if (grantResults.length <= 0) {
//                // If user interaction was interrupted, the permission request is cancelled and you
//                // receive empty arrays.
//                Log.i(TAG, "User interaction was cancelled.");
//            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                if (mRequestingLocationUpdates) {
//                    Log.i(TAG, "Permission granted, updates requested, starting location updates");
//                    StartLocationUpdates();
//                }
//            } else {
//                // Permission denied.
//
//                // Notify the user via a SnackBar that they have rejected a core permission for the
//                // app, which makes the Activity useless. In a real app, core permissions would
//                // typically be best requested during a welcome-screen flow.
//
//                // Additionally, it is important to remember that a permission might have been
//                // rejected without asking the user for permission (device policy or "Never ask
//                // again" prompts). Therefore, a user interface affordance is typically implemented
//                // when permissions are denied. Otherwise, your app could appear unresponsive to
//                // touches or interactions which have required permissions.
//                ShowSnackbar(R.string.permission_denied_explanation,
//                        R.string.settings, new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                // Build intent that displays the App settings screen.
//                                Intent intent = new Intent();
//                                intent.setAction(
//                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                                Uri uri = Uri.fromParts("package",
//                                        BuildConfig.APPLICATION_ID, null);
//                                intent.setData(uri);
//                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                startActivity(intent);
//                            }
//                        });
//            }
//        }
//    }
}
