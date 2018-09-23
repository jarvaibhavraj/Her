package xyz.mrdeveloper.her;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class FakeCallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("debug","RECEIVING FAKE CALL");

        Intent intentObject = new Intent(context, FakeCallActivity.class);
        intentObject.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intentObject);
    }
}