package xyz.mrdeveloper.her;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.android.SphericalUtil;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static xyz.mrdeveloper.her.Signup.phoneNumber;
import static xyz.mrdeveloper.her.UpdateFromFirebase.allPeopleList;

import static xyz.mrdeveloper.her.MainActivity.lovedList;

/**
 * Created by Vaibhav on 24-11-2017.
 */

public class MapsActivity extends FragmentActivity implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback {

    final static int PROXIMITY_RADIUS = 10000;

    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    Polyline pathOnMap;

    LatLng currentUserPosition = new LatLng(0, 0);

    ArrayList<LatLng> locations = new ArrayList<>();
    ArrayList<Marker> nearbyMarkers = new ArrayList<>();

    String hospital = "hospital";
    String police = "police";

    boolean hasAlreadyFocused;

    TextView textSOS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

//        getSupportActionBar().setTitle("Her Maps Activity");

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

        textSOS = (TextView) findViewById(R.id.text_SOS);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    public void NotifyUsers() {
        final DatabaseReference mFirebaseDatabase;
        FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();

        mFirebaseDatabase = mFirebaseInstance.getReference("PersonData");

        for (int i = 0; i < allPeopleList.size(); ++i) {

            if (SphericalUtil.computeDistanceBetween(currentUserPosition, new LatLng(allPeopleList.get(i).latitude, allPeopleList.get(i).longitude))
                    > PROXIMITY_RADIUS) {
                nearbyMarkers.get(i).setVisible(false);
            } else {
                Log.d("Check", "In sphere " + phoneNumber);
                if (!allPeopleList.get(i).phoneNumber.equals(phoneNumber)) {
                    mFirebaseDatabase.child(allPeopleList.get(i).phoneNumber).child("AmIAlerted").setValue(true);
                    Log.d("Check", "In IFF " + mFirebaseDatabase.child(allPeopleList.get(i).phoneNumber));
                }
            }
        }

        for (int i = 0; i < lovedList.size(); ++i) {
            Log.d("Check", "Here in send sms");
            SendSMS(lovedList.get(i).getNumber());
            Log.d("Check", "SMS To: i: " + lovedList.get(i).getNumber() + " " + lovedList.get(i).getName() + " " + i);
        }
    }

    public void SendSMS(String phoneNo) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_DENIED) {

                Log.d("permission", "permission denied to SEND_SMS - requesting it");
                String[] permissions = {android.Manifest.permission.SEND_SMS};

                requestPermissions(permissions, 88);
            }
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, "Help me I'm stuck! See my location from app alert.", null, null);
            Toast.makeText(getApplicationContext(), "Message Sent",
                    Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(),
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker marker) {

                LatLng origin = currentUserPosition;
                LatLng dest = marker.getPosition();

                String url = getUrl(origin, dest);
                Log.d("debug", "MARKER CLICKED " + url);
                FetchUrl FetchUrl = new FetchUrl();

                FetchUrl.execute(url);

                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
                mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(13));

                marker.setTitle(marker.getTitle() + ":" +
                        String.valueOf((int) SphericalUtil.computeDistanceBetween(origin, dest)) + "m");
                return false;
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                checkLocationPermission();
            }
        } else {
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
        }

        FillUsersData();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentUserPosition);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

//        locations.add(0, new LatLng(currentUserPosition.latitude + 0.5, currentUserPosition.longitude + 0.5));
//        locations.add(1, new LatLng(currentUserPosition.latitude - 0.5, currentUserPosition.longitude + 0.5));
//        locations.add(2, new LatLng(currentUserPosition.latitude + 0.5, currentUserPosition.longitude - 0.5));

//        locations.add(0, new LatLng(currentUserPosition.latitude + 0.5, currentUserPosition.longitude + 0.15));
//        locations.add(1, new LatLng(currentUserPosition.latitude - 0.15, currentUserPosition.longitude + 0.5));
//        locations.add(2, new LatLng(currentUserPosition.latitude - 0.15, currentUserPosition.longitude - 0.15));
//        locations.add(3, new LatLng(currentUserPosition.latitude + 0.45, currentUserPosition.longitude - 0.25));


        locations.add(0, new LatLng(30.409222, 77.969290));
        locations.add(1, new LatLng(30.509222, 76.969290));
        locations.add(2, new LatLng(30.809222, 77.369290));
        locations.add(3, new LatLng(29.709222, 78.969290));

        int i = 0;
//        for (LatLng location : locations) {
        for (i = 0; i < 4; i++) {
            markerOptions = new MarkerOptions();
            markerOptions.position(locations.get(i));
            markerOptions.title("GUARDIAN");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
            Marker marker = mGoogleMap.addMarker(markerOptions);
            nearbyMarkers.add(i, marker);
//            Log.d("count", "Adding marker " + i);
        }
        textSOS.setText((i) + " PROTECTORS have been notified. They will reach you shortly.\nDon't panic. Move to a safe location.");
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, MapsActivity.this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

//        if (mCurrLocationMarker != null) {
//            mCurrLocationMarker.remove();
//        }

//        Log.d("debug", "LOCATION CHANGED");
        //Place updated location marker
        currentUserPosition = new LatLng(location.getLatitude(), location.getLongitude());

        FillUsersData();
        UpdateMyLocation();

        locations.add(0, new LatLng(30.409222, 77.969290));
        locations.add(1, new LatLng(30.509222, 76.969290));
        locations.add(2, new LatLng(30.809222, 77.369290));
        locations.add(3, new LatLng(29.709222, 78.969290));

        mCurrLocationMarker.setPosition(currentUserPosition);

        if (!hasAlreadyFocused) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentUserPosition, 11));
            hasAlreadyFocused = true;
            NotifyUsers();
        }

        ShowNearbyUsers();
        ShowNearbyHospitals();
        ShowNearbyPoliceStations();
    }

    public void FillUsersData() {
//        for (int i = 0; i < allPeopleList.size(); ++i) {
////            Log.d("count", "GUARDS " + i);
//            locations.add(i, new LatLng(allPeopleList.get(i).latitude, allPeopleList.get(i).longitude));
//        }
    }

    public void UpdateMyLocation() {
        final DatabaseReference mFirebaseDatabase;
        FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();

        // get reference to 'users' node
        mFirebaseDatabase = mFirebaseInstance.getReference("PersonData");
        mFirebaseDatabase.child(phoneNumber).child("Latitude").setValue(currentUserPosition.latitude);
        mFirebaseDatabase.child(phoneNumber).child("Longitude").setValue(currentUserPosition.longitude);
    }

    private void ShowNearbyPoliceStations() {
        String url = getUrl(mCurrLocationMarker.getPosition().latitude, mCurrLocationMarker.getPosition().longitude,
                hospital);
        Object[] DataTransfer = new Object[3];
        DataTransfer[0] = mGoogleMap;
        DataTransfer[1] = url;
        DataTransfer[2] = hospital;
        Log.d("debug", url);
        GetNearbyPlaces getNearbyPlacesData = new GetNearbyPlaces();
        getNearbyPlacesData.execute(DataTransfer);
    }

    private void ShowNearbyHospitals() {
        String url = getUrl(mCurrLocationMarker.getPosition().latitude, mCurrLocationMarker.getPosition().longitude,
                police);
        Object[] DataTransfer = new Object[3];
        DataTransfer[0] = mGoogleMap;
        DataTransfer[1] = url;
        DataTransfer[2] = police;
        Log.d("debug", url);
        GetNearbyPlaces getNearbyPlacesData = new GetNearbyPlaces();
        getNearbyPlacesData.execute(DataTransfer);
    }

    private void ShowNearbyUsers() {
//        for (int i = 0; i < allPeopleList.size(); i++) {
        for (int i = 0; i < 4; i++) {
//            Log.d("count", "LOCATIONS " + i);
            LatLng temp = locations.get(i);
            nearbyMarkers.get(i).setPosition(temp);
            Log.d("count", "Nearby People : " + i);
//            if (SphericalUtil.computeDistanceBetween(currentUserPosition, nearbyMarkers.get(i).getPosition())
//                    > PROXIMITY_RADIUS) {
//                nearbyMarkers.get(i).setVisible(false);
//            }
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mGoogleMap.setMyLocationEnabled(true);
                    }

                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private String getUrl(double latitude, double longitude, String nearbyPlace) {

        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlacesUrl.append("&type=" + nearbyPlace);
        googlePlacesUrl.append("&key=" + "AIzaSyA3F3N5lrtkFfEIiIiLgLDdf53Y1gbLjG4");
        googlePlacesUrl.append("&sensor=true");
        Log.d("getUrl", googlePlacesUrl.toString());
        return (googlePlacesUrl.toString());
    }

    private String getUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String sensor = "sensor=false";
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            parserTask.execute(result);

        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0].toString());
                PathDataParser parser = new PathDataParser();
                Log.d("ParserTask", parser.toString());

                routes = parser.parse(jObject);
                Log.d("ParserTask", "Executing routes");
                Log.d("ParserTask", routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;
            if (pathOnMap != null)
                pathOnMap.remove();

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);

                Log.d("onPostExecute", "onPostExecute lineoptions decoded");

            }

            if (lineOptions != null) {
                pathOnMap = mGoogleMap.addPolyline(lineOptions);
            } else {
                Log.d("onPostExecute", "without Polylines drawn");
            }
        }
    }
}