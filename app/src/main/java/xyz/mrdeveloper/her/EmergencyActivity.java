package xyz.mrdeveloper.her;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;
import com.hbb20.CountryCodePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import static xyz.mrdeveloper.her.MainActivity.PROXIMITY_PLACES_RADIUS;

/**
 * Created by Vaibhav on 24-11-2017.
 */

public class EmergencyActivity extends FragmentActivity implements OnMapReadyCallback {

    private enum WORKING_MODE {
        EMERGENCY,
        SAFE_PLACES,
        PROTECTOR,
        UNSPECIFIED
    }

    private WORKING_MODE workingMode;

    private static final String TAG = EmergencyActivity.class.getSimpleName();
    private static final int LOCATION_PERMISSIONS_REQUEST_CODE = 34;
    private static final int SMS_PERMISSIONS_REQUEST_CODE = 35;
    private static final int REQUEST_CHECK_SETTINGS = 42;
    private static final int EMERGENCY_NOTIFICATION_ID = 1337;

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private SettingsClient mSettingsClient;
    private LocationSettingsRequest mLocationSettingsRequest;

    private NotificationManager mNotificationManager;

    Location mCurrentLocation;
    LatLng mCurrentLatLng;
    PersonData personInEmergency;
    String mPhoneNumber;

    ArrayList<Marker> nearbyProtectorMarkers = new ArrayList<>();
    ArrayList<FamilyMemberData> familyMembersList = new ArrayList<>();

    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;

    MapPath pathOnMap;
    Marker mCurrLocationMarker;
    Marker personInEmergencyMarker;

    String hospital = "hospital";
    String police = "police";

    TextView textWorkingMode;
    TextView textMessage;
    Button cancelEmergency;

    boolean isBeaconActivated = false;
    boolean didFocusMap = false;
    boolean didMarkLocationsOnMap = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

//        getSupportActionBar().setTitle("Emergency Activity");

        UpdateValuesFromBundle(savedInstanceState);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);
        CreateLocationRequest();
        CreateLocationCallback();
        BuildLocationSettingsRequest();
        StartLocationUpdates();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPhoneNumber = sharedPreferences.getString("myPhoneNumber", "No phone number");
        Log.d("Check", "GOT PHONE NUMBER" + mPhoneNumber);


        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

        textWorkingMode = findViewById(R.id.text_working_mode);
        textMessage = findViewById(R.id.text_message);
        cancelEmergency = findViewById(R.id.cancel_emergency);


        Intent intent = getIntent();
        switch (intent.getStringExtra("workingMode")) {
            case "EMERGENCY":
                workingMode = WORKING_MODE.EMERGENCY;
                break;
            case "SAFE_PLACES":
                workingMode = WORKING_MODE.SAFE_PLACES;
                break;
            case "PROTECTOR":
                workingMode = WORKING_MODE.PROTECTOR;
                break;
            default:
                workingMode = WORKING_MODE.UNSPECIFIED;
                break;
        }

        personInEmergency = new PersonData();

        if (workingMode == WORKING_MODE.EMERGENCY) {
            personInEmergency.phoneNumber = mPhoneNumber;
            final DatabaseReference personInEmergencyReference = FirebaseDatabase.getInstance().getReference("personData").child(personInEmergency.phoneNumber);
            personInEmergencyReference.child("isInEmergency").setValue(true);

            SendSMSToFamilyMembers();

            cancelEmergency.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ShowCancelEmergencyDialog();
                }
            });

            textWorkingMode.setText(R.string.sos_mode_on);
            textMessage.setText(R.string.sos_mode_no_protectors);


        } else if (workingMode == WORKING_MODE.SAFE_PLACES) {
            personInEmergency.phoneNumber = mPhoneNumber;
            cancelEmergency.setVisibility(View.GONE);
            textWorkingMode.setText(R.string.safe_places_mode_on);
            textMessage.setText(R.string.safe_places_mode_message);


        } else if (workingMode == WORKING_MODE.PROTECTOR) {
            personInEmergency.phoneNumber = intent.getStringExtra("personInEmergencyNumber");
            personInEmergency.latitude = intent.getFloatExtra("personInEmergencyLatitude", 0f);
            personInEmergency.longitude = intent.getFloatExtra("personInEmergencyLongitude", 0f);

            cancelEmergency.setVisibility(View.GONE);

            textWorkingMode.setText(R.string.rescue_mode_on);
            textMessage.setText(R.string.rescue_mode_message);
        }

        final DatabaseReference personInEmergencyReference = FirebaseDatabase.getInstance().getReference("personData").child(personInEmergency.phoneNumber);
        personInEmergencyReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                personInEmergency = dataSnapshot.getValue(PersonData.class);
                if (personInEmergency != null && !personInEmergency.isInEmergency) {

                    if (workingMode == WORKING_MODE.PROTECTOR)
                        new AlertDialog.Builder(EmergencyActivity.this)
                                .setTitle("Emergency Over")
                                .setMessage(R.string.emergency_over)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                })
                                .create()
                                .show();

                    personInEmergencyReference.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we remove location updates. Here, we resume receiving
        // location updates if the user has requested them.
        if (CheckLocationPermissions()) {
            StartLocationUpdates();
        } else {
            RequestLocationPermissions();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Remove location updates to save battery.
        StopLocationUpdates();
    }

    @Override
    public void onBackPressed() {
        if (workingMode != WORKING_MODE.PROTECTOR)
            ShowCancelEmergencyDialog();
        else
            super.onBackPressed();
    }


    private void ShowCancelEmergencyDialog() {
        final DatabaseReference personInEmergencyReference = FirebaseDatabase.getInstance().getReference("personData").child(personInEmergency.phoneNumber);

        new AlertDialog.Builder(EmergencyActivity.this)
                .setTitle("WARNING!")
                .setMessage(R.string.cancel_emergency)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (mNotificationManager != null) {
                            mNotificationManager.cancel(EMERGENCY_NOTIFICATION_ID);
                        }
                        personInEmergencyReference.child("isInEmergency").setValue(false);
                        personInEmergencyReference.child("nearbyProtectors").removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            }
                        });
                        finish();
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .create()
                .show();
    }

    private void SetEmergencyNotificationForMe() {

        Intent intent = new Intent(this, EmergencyActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("workingMode", "EMERGENCY");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);


        String channelId = getString(R.string.default_notification_channel_id);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.her_logo_final)
                        .setContentTitle("Her Emergency Mode ON")
                        .setContentText(
                                nearbyProtectorMarkers.size() > 0
                                        ? nearbyProtectorMarkers.size() + getResources().getString(R.string.sos_mode_protectors_notified)
                                        : getResources().getString(R.string.sos_mode_no_protectors)
                        )
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setLights(Color.RED, 1000, 100)
                        .setContentIntent(pendingIntent);


        if (mNotificationManager != null) {
            mNotificationManager.notify(EMERGENCY_NOTIFICATION_ID /* ID of notification */, notificationBuilder.build());
        }
    }

    private void UpdateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains("location")) {
                // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable("location");
                if (mCurrentLocation != null) {
                    mCurrentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                }
            }
        }
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable("location", mCurrentLocation);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker marker) {

                LatLng origin = mCurrentLatLng;
                LatLng dest = marker.getPosition();

                pathOnMap = new MapPath(origin, dest, mGoogleMap);
                pathOnMap.draw();

                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
                mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(16));

                marker.setTitle(marker.getTitle() + " : " +
                        String.valueOf((int) SphericalUtil.computeDistanceBetween(origin, dest)) + " m");
                return false;
            }
        });
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

                UpdateMyLocationOnFirebase(locationResult);

                if (workingMode == WORKING_MODE.EMERGENCY) {
                    /*
                      There is a chance that personInEmergency's location has not been updated in firebase yet.
                      So we take the local location value to use in EmergencyBeacon
                     */
                    personInEmergency.latitude = (float) locationResult.getLastLocation().getLatitude();
                    personInEmergency.longitude = (float) locationResult.getLastLocation().getLongitude();

                    if (!isBeaconActivated) {
                        EmergencyBeacon emergencyBeacon = new EmergencyBeacon();
                        emergencyBeacon.execute(personInEmergency);
                        isBeaconActivated = true;
                    }
                    UpdateMapForEmergency();
                    SetEmergencyNotificationForMe();

                } else if (workingMode == WORKING_MODE.SAFE_PLACES) {
                    UpdateMapForHelp();
                } else if (workingMode == WORKING_MODE.PROTECTOR) {
                    UpdateMapForProtector();
                }

                if (!didMarkLocationsOnMap) {
                    MarkNearbyHospitalsOnMap();
                    MarkNearbyPoliceStationsOnMap();
                    if (workingMode == WORKING_MODE.EMERGENCY) {
                        MarkNearbyProtectorsOnMap();
                    }
                    didMarkLocationsOnMap = true;
                }
            }
        };
    }

    private void BuildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        break;
                }
                break;
        }
    }

    private void StartLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(EmergencyActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(EmergencyActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void StopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    public void UpdateMyLocationOnFirebase(LocationResult locationResult) {
        mCurrentLocation = locationResult.getLastLocation();
        mCurrentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

        Log.i("debug", "Location: " + mCurrentLocation);

        DatabaseReference mFirebaseDatabase = FirebaseDatabase.getInstance().getReference("personData").child(mPhoneNumber);
        mFirebaseDatabase.child("latitude").setValue(mCurrentLatLng.latitude);
        mFirebaseDatabase.child("longitude").setValue(mCurrentLatLng.longitude);

        if (workingMode == WORKING_MODE.PROTECTOR) {
            mFirebaseDatabase = FirebaseDatabase.getInstance().getReference("personData")
                    .child(personInEmergency.phoneNumber).child("nearbyProtectors").child(mPhoneNumber);
            mFirebaseDatabase.child("latitude").setValue(mCurrentLatLng.latitude);
            mFirebaseDatabase.child("longitude").setValue(mCurrentLatLng.longitude);
        }
    }

    private void UpdateMapForEmergency() {
        if (mCurrLocationMarker != null)
            mCurrLocationMarker.remove();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(mCurrentLatLng);
        markerOptions.title("Your Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

        FocusMyPositionOnMap();
    }

    private void UpdateMapForHelp() {
        if (mCurrLocationMarker != null)
            mCurrLocationMarker.remove();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(mCurrentLatLng);
        markerOptions.title("Your Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

        FocusMyPositionOnMap();
    }

    private void UpdateMapForProtector() {
        if (mCurrLocationMarker != null)
            mCurrLocationMarker.remove();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(mCurrentLatLng);
        markerOptions.title("Your Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

        if (personInEmergencyMarker != null)
            personInEmergencyMarker.remove();
        markerOptions = new MarkerOptions();
        LatLng personInEmergencyLatLng = new LatLng(personInEmergency.latitude, personInEmergency.longitude);
        markerOptions.position(personInEmergencyLatLng);
        markerOptions.title("Someone is in danger!");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        personInEmergencyMarker = mGoogleMap.addMarker(markerOptions);
        if (!didFocusMap)
            personInEmergencyMarker.showInfoWindow();

        pathOnMap = new MapPath(mCurrentLatLng, personInEmergencyLatLng, mGoogleMap);
        pathOnMap.draw();

        FocusMyPositionOnMap();
    }

    private void FocusMyPositionOnMap() {
        if (!didFocusMap) {
            int zoom = 16;
            if (workingMode == WORKING_MODE.SAFE_PLACES)
                zoom = 13;

            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, zoom));
            didFocusMap = true;
        }

    }

    public void SendSMSToFamilyMembers() {

        if (CheckSMSPermissions()) {
            DatabaseReference familyMembersReference = FirebaseDatabase.getInstance()
                    .getReference("personData").child(mPhoneNumber).child("familyMembers");

            familyMembersReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    FamilyMemberData familyMember = dataSnapshot.getValue(FamilyMemberData.class);
                    if (familyMember != null) {
                        String phoneNumber = "+" + familyMember.getCountryCode() + familyMember.getNumber();
                        Log.i("debug", "SMS to: " + phoneNumber);
                        SendSMS(phoneNumber);
                        familyMembersList.add(familyMember);
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("The read failed: ", databaseError.getMessage());
                }
            });

        } else {
            RequestSMSPermissions();
        }
    }

    public void SendSMS(String phoneNo) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, "Help me I'm stuck! See my location from app alert.", null, null);
            Toast.makeText(this, "Message sent to " + phoneNo, Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
        }
    }

    private void MarkNearbyProtectorsOnMap() {

        final DatabaseReference nearbyUsersData;
        nearbyUsersData = FirebaseDatabase.getInstance()
                .getReference("personData").child(personInEmergency.phoneNumber).child("nearbyProtectors");

        nearbyUsersData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                int i = 0;

                for (Marker marker : nearbyProtectorMarkers) {
                    marker.remove();
                }
                nearbyProtectorMarkers.clear();

                for (DataSnapshot person : dataSnapshot.getChildren()) {
                    Log.i("DATA", "Person Data: " + person);
                    MarkerOptions markerOptions = new MarkerOptions();
                    Float nearbyProtectorLatitude = person.child("latitude").getValue(Float.class);
                    Float nearbyProtectorLongitude = person.child("longitude").getValue(Float.class);

                    if (nearbyProtectorLatitude != null && nearbyProtectorLongitude != null) {
                        markerOptions.position(new LatLng(nearbyProtectorLatitude, nearbyProtectorLongitude));
                        markerOptions.title("PROTECTOR " + i);
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        Marker marker = mGoogleMap.addMarker(markerOptions);

                        nearbyProtectorMarkers.add(i, marker);
                        i++;
                    }
                }
                Log.i("debug", "Marking " + i + " protectors on map");
                if (i > 0)
                    textMessage.setText((i) + getString(R.string.sos_mode_protectors_notified));
                else
                    textMessage.setText(R.string.sos_mode_no_protectors);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.e("Check", "Failed to read database for nearby users", error.toException());
            }
        });
    }

    private void MarkNearbyPoliceStationsOnMap() {
        String url = getUrl(mCurrLocationMarker.getPosition().latitude, mCurrLocationMarker.getPosition().longitude, hospital);
        Object[] DataTransfer = new Object[3];
        DataTransfer[0] = mGoogleMap;
        DataTransfer[1] = url;
        DataTransfer[2] = hospital;
        Log.d("debug", url);
        GetNearbyPlaces getNearbyPlacesData = new GetNearbyPlaces();
        getNearbyPlacesData.execute(DataTransfer);
    }

    private void MarkNearbyHospitalsOnMap() {
        String url = getUrl(mCurrLocationMarker.getPosition().latitude, mCurrLocationMarker.getPosition().longitude, police);
        Object[] DataTransfer = new Object[3];
        DataTransfer[0] = mGoogleMap;
        DataTransfer[1] = url;
        DataTransfer[2] = police;
        Log.d("debug", url);
        GetNearbyPlaces getNearbyPlacesData = new GetNearbyPlaces();
        getNearbyPlacesData.execute(DataTransfer);
    }

    private String getUrl(double latitude, double longitude, String nearbyPlace) {

        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + PROXIMITY_PLACES_RADIUS);
        googlePlacesUrl.append("&type=" + nearbyPlace);
        googlePlacesUrl.append("&key=" + "AIzaSyA3F3N5lrtkFfEIiIiLgLDdf53Y1gbLjG4");
        googlePlacesUrl.append("&sensor=true");
        Log.d("getUrl", googlePlacesUrl.toString());
        return (googlePlacesUrl.toString());
    }

    private void ShowSnackbar(final int mainTextStringId, final int actionStringId, View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    private boolean CheckSMSPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void RequestSMSPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.SEND_SMS);

        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            ShowSnackbar(R.string.sms_permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(EmergencyActivity.this,
                                    new String[]{Manifest.permission.SEND_SMS},
                                    SMS_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            Log.i(TAG, "Requesting SMS permission");
            ActivityCompat.requestPermissions(EmergencyActivity.this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSIONS_REQUEST_CODE);
        }

    }

    private boolean CheckLocationPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void RequestLocationPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            ShowSnackbar(R.string.location_permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(EmergencyActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    LOCATION_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            ActivityCompat.requestPermissions(EmergencyActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == LOCATION_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission granted, updates requested, starting location updates");
                StartLocationUpdates();

            } else {
                ShowSnackbar(R.string.permission_denied_explanation,
                        R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        } else if (requestCode == SMS_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission granted, updates requested, starting location updates");
                SendSMSToFamilyMembers();

            } else {
                ShowSnackbar(R.string.permission_denied_explanation,
                        R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }


    private static class EmergencyBeacon extends AsyncTask<PersonData, Void, Void> {

        @Override
        protected Void doInBackground(PersonData... args) {

            String auth_key = "AAAA9CFNe4U:APA91bEDBrNIjdBleDn1jIIQEcfPHXec00BKiXt2yqK3eH3V-Mee1YodiA-F2taL57to3jdUIjTKPj7tSWt-0UO-j0y9XDRr3yYT3vsOS2eDYEfm7y_-0TpsnYntWJkOsh9LiN4iC3_A";
            PersonData personInEmergency = args[0];

            //Send Push Notification
            HttpsURLConnection connection = null;
            try {

                URL url = new URL("https://fcm.googleapis.com/fcm/send");
                connection = (HttpsURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Authorization", "key=" + auth_key);

//                JSONObject notification = new JSONObject();
//                notification.put("title", "SAFE_PLACES");
//                notification.put("body", "Someone needs help");

                JSONObject data = new JSONObject();
                data.put("personInEmergencyNumber", personInEmergency.phoneNumber);
                data.put("personInEmergencyLatitude", personInEmergency.latitude);
                data.put("personInEmergencyLongitude", personInEmergency.longitude);

                JSONObject root = new JSONObject();
//                root.put("notification", notification);
                root.put("data", data);
                root.put("to", "/topics/emergency");

                byte[] outputBytes = root.toString().getBytes("UTF-8");
                OutputStream os = connection.getOutputStream();
                os.write(outputBytes);
                os.flush();
                os.close();
                connection.getInputStream(); //Do not remove this line. Request will not work without it.
                Log.i("debug", "POST Response " + connection.getResponseMessage());

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        }
    }
}