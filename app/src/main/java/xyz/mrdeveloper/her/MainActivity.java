package xyz.mrdeveloper.her;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;

import static xyz.mrdeveloper.her.MapsActivity.PROXIMITY_RADIUS;
import static xyz.mrdeveloper.her.UpdateFromFirebase.allPeopleList;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener {
    Intent Emergency;
    private FirebaseAuth mAuth;
    public static ArrayList<LovedOnes> lovedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UpdateFromFirebase updateFromFirebase = new UpdateFromFirebase();
        //currentDatabase = FirebaseDatabase.getInstance().getReference();
        updateFromFirebase.UpdateAllLocations();

        mAuth = FirebaseAuth.getInstance();

        Emergency = new Intent(getApplicationContext(), LockService.class);
        startService(Emergency);

        ImageButton alertButton = findViewById(R.id.alert_button);
        ImageButton helpButton = findViewById(R.id.help_button);
        ImageButton addLovedOnesButton = findViewById(R.id.loved_button);
        ImageButton tipsButton = findViewById(R.id.tips_button);
        ImageButton callButton = findViewById(R.id.call_button);
        ImageButton logoutButton = findViewById(R.id.log_out_button);
        ImageButton aboutButton = findViewById(R.id.about_button);

        alertButton.setOnClickListener(this);
        helpButton.setOnClickListener(this);
        addLovedOnesButton.setOnClickListener(this);
        tipsButton.setOnClickListener(this);
        callButton.setOnClickListener(this);
        logoutButton.setOnClickListener(this);
        aboutButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.alert_button:
//                NotifyUsers();
                Intent myIntent = new Intent(MainActivity.this, MapsActivity.class);
                MainActivity.this.startActivity(myIntent);
                break;

            case R.id.help_button:
                myIntent = new Intent(MainActivity.this, HelpActivity.class);
                MainActivity.this.startActivity(myIntent);
                break;

            case R.id.loved_button:
                myIntent = new Intent(MainActivity.this, LovedOneActivity.class);
                startActivity(myIntent);
                break;

            case R.id.tips_button:
                Log.i("debug","CLICKED TIPS");
                myIntent = new Intent(MainActivity.this, Tips.class);
                startActivity(myIntent);
                break;

            case R.id.call_button:
//                myIntent = new Intent(MainActivity.this, FakeCallActivity.class);
//                MainActivity.this.startActivity(myIntent);
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(this, FakeCallActivity.class);
                startActivity(intent);
//
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

            case R.id.log_out_button:
                mAuth.signOut();
                finish();
                startActivity(new Intent(MainActivity.this, Signup.class));
                break;

            case R.id.about_button:
                Log.i("debug","CLICKED");
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                break;
        }
    }

    @Override
    protected void onDestroy() {
        stopService(Emergency);
        super.onDestroy();
    }
}
