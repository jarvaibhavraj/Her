package xyz.mrdeveloper.her;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;

import static xyz.mrdeveloper.her.MainActivity.lovedList;
import static xyz.mrdeveloper.her.Signup.mContext;
import static xyz.mrdeveloper.her.Signup.phoneNumber;

/**
 * Created by Vaibhav on 24-11-2017.
 */

public class UpdateFromFirebase extends android.app.Application {

    public static ArrayList<PersonData> allPeopleList;
    public boolean isDatabaseNull = false;
    // public Context mContext;


    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        FirebaseApp.initializeApp(this);

        lovedList = new ArrayList<>();
    }

    public UpdateFromFirebase() {

    }

    public UpdateFromFirebase(Context context) {
        //mContext = context;
    }

    public void UpdateAllLocations() {
        final DatabaseReference mFirebaseDatabase;
        FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();

        // get reference to 'users' node
        mFirebaseDatabase = mFirebaseInstance.getReference("PersonData");

        if (mFirebaseDatabase == null) {
            isDatabaseNull = true;
        }

        if (mFirebaseDatabase != null) {
            mFirebaseDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    GetLocationsFromFirebase(dataSnapshot);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.e("Check", "Failed to read app title value.", error.toException());
                }
            });
        }
    }

    public void GetLocationsFromFirebase(DataSnapshot dataSnapshot) {
        // Get event categories names and descriptions.

        final DatabaseReference mFirebaseDatabase;
        FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseDatabase = mFirebaseInstance.getReference("PersonData");

        for (DataSnapshot alert : dataSnapshot.getChildren()) {
            if (alert == null) {
                Log.i("Check", "NULLLLLLLLL!!!!!");
            }
            Log.d("Check", "List size: " + lovedList.size());
            PersonData personData = new PersonData();

            if (alert != null) {
                allPeopleList = new ArrayList<>();

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    personData.phoneNumber = data.child("PhoneNumber").getValue(String.class);
                    personData.longitude = data.child("Longitude").getValue(Float.class);
                    personData.latitude = data.child("Latitude").getValue(Float.class);
                    personData.amIAlerted = data.child("AmIAlerted").getValue(Boolean.class);

                    Log.d("Check", "Phone Number: " + phoneNumber);

                    if (personData.phoneNumber.equals(phoneNumber)) {
                        String key, value;

                        Log.d("Check", "here if");

                        for (DataSnapshot childDataSnapshot : data.getChildren()) {

                            key = childDataSnapshot.getKey();
                            if (key.equals("LovedOnes")) {
                                lovedList = new ArrayList<>();
                                Log.d("Check", "key: " + key);
                                for (DataSnapshot grandchildDataSnapshot : childDataSnapshot.getChildren()) {
                                    key = grandchildDataSnapshot.getKey();
                                    Log.d("Check", "number : " + key);
                                    Log.d("Check", "Name: " + grandchildDataSnapshot.child("name").getValue(String.class));
                                    LovedOnes lovedOnes = new LovedOnes(grandchildDataSnapshot.child("name").getValue(String.class), key);
                                    //Log.d("Check","Name and Number: "+lovedOnes)
                                    lovedList.add(lovedOnes);
                                }
                            }
                        }
                    }

                    //Log.d("Check", "in update: & size: " + MainActivity.lovedList.get(0).getName() + MainActivity.lovedList.get(1).getName() + MainActivity.lovedList.size());

                    if (personData.amIAlerted) {
                        personData.amIAlerted = false;
                        PushNotificationForHelp();
                        mFirebaseDatabase.child(personData.phoneNumber).child("AmIAlerted").setValue(false);
                    }
                    allPeopleList.add(personData);
                }
            }
        }
    }

    public void SetLocationsToFirebase() {
        final DatabaseReference mFirebaseDatabase;
        FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseDatabase = mFirebaseInstance.getReference("PersonData");


        for (int i = 0; i < 3; i++) {
            PersonData personData = allPeopleList.get(i);

            mFirebaseDatabase.child(phoneNumber).child("Latitude").setValue(personData.latitude);
            mFirebaseDatabase.child(phoneNumber).child("Longitude").setValue(personData.longitude);
        }
    }

    public void PushNotificationForHelp() {
        //create notification
        Uri notificationSoundURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Intent intent = new Intent(mContext, GuardianActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("ToOpen", 1);
        PendingIntent resultIntent = PendingIntent.getActivity(mContext, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setSound(notificationSoundURI)
                .setContentTitle(" ")
                .setVibrate(new long[]{1000, 1000})
                .setLights(Color.RED, 0, 0)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(resultIntent)
                .setAutoCancel(true)
                .setContentText("Tap to view updates");

        Random random = new Random();

        NotificationManager mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (mNotificationManager != null) {
            mNotificationManager.notify(random.nextInt(), mBuilder.build());
        }
    }
}
