package xyz.mrdeveloper.her;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class FakeCallActivity extends AppCompatActivity {

    private String networkCarrier;
    private MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_call);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

//        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
//        r.play();

        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(500);
        v.cancel();
        v.vibrate(500);
        v.cancel();
        v.vibrate(500);
        v.cancel();
        v.vibrate(500);

//        TextView fakeName = (TextView)findViewById(R.id.chosenfakename);
//        TextView fakeNumber = (TextView)findViewById(R.id.chosenfakenumber);

//        final TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
//        networkCarrier = tm.getNetworkOperatorName();

//        TextView titleBar = (TextView)findViewById(R.id.textView1);
//        if(networkCarrier != null){
//            titleBar.setText("Incoming call - " + networkCarrier);
//        }else{
//            titleBar.setText("Incoming call");
//        }

//        String callNumber = getContactNumber();
//        String callName = getContactName();

//        fakeName.setText(callName);
//        fakeNumber.setText(callNumber);

//        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
//        mp = MediaPlayer.create(getApplicationContext(), notification);
//        mp.start();

//        Button answerCall = (Button)findViewById(R.id.answercall);
//        Button rejectCall = (Button)findViewById(R.id.rejectcall);
//
//        answerCall.setOnClickListener(new View.OnClickListener(){
//
//            @Override
//            public void onClick(View v) {
//                mp.stop();
//            }
//        });
//        rejectCall.setOnClickListener(new View.OnClickListener(){
//
//            @Override
//            public void onClick(View v) {
//                mp.stop();
//                Intent homeIntent= new Intent(Intent.ACTION_MAIN);
//                homeIntent.addCategory(Intent.CATEGORY_HOME);
//                homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(homeIntent);
//            }
//        });
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
////        getMenuInflater().inflate(R.menu.menu_fake_ringing, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        //noinspection SimplifiableIfStatement
////        if (id == R.id.action_settings) {
////            return true;
////        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    private String getContactNumber() {
//        String contact = null;
//        Intent myIntent = getIntent();
//        Bundle mIntent = myIntent.getExtras();
//        if (mIntent != null) {
//            contact = mIntent.getString("myfakenumber");
//        }
//        return contact;
//    }
//
//    private String getContactName() {
//        String contactName = null;
//        Intent myIntent = getIntent();
//        Bundle mIntent = myIntent.getExtras();
//        if (mIntent != null) {
//            contactName = mIntent.getString("myfakename");
//        }
//        return contactName;
//    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_fake_call);
//    }
}