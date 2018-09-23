package xyz.mrdeveloper.her;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class FakeCallActivity extends AppCompatActivity {

    Ringtone ringtone;
    Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_call);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorStatusBarBlue));
        }

        Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), ringtoneUri);
        ringtone.play();

        Log.i("debug", "Playing Ringtone");

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] vibrationPattern = {0, 250, 250, 250, 250, 250, 250, 250, 250};
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, -1));

            } else {
                vibrator.vibrate(vibrationPattern, -1);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ringtone != null) ringtone.stop();
        if (vibrator != null) vibrator.cancel();
    }
}