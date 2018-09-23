package xyz.mrdeveloper.her;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Keep;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class HardwareButtonsService extends Service {

    public HardwareButtonsService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("key", "HARDWARE SERVICE CREATED");
        LinearLayout overlayLayout = new LinearLayout(this) {

            //home or recent button
            @Keep
            public void onCloseSystemDialogs(String reason) {
                if ("globalactions".equals(reason)) {
                    Log.i("key", "Long press on power button");
                } else if ("homekey".equals(reason)) {
                    Log.i("key", "Home button pressed");
                    //home key pressed
                } else if ("recentapss".equals(reason)) {
                    // recent apps button clicked
                }
            }

            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                Log.i("key", "keycode " + event.getKeyCode());
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                        || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP
                        || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN
                        || event.getKeyCode() == KeyEvent.KEYCODE_CAMERA
                        || event.getKeyCode() == KeyEvent.KEYCODE_POWER) {
                    Log.i("key", "CODEEEE " + event.getKeyCode());
                }
                return super.dispatchKeyEvent(event);
            }
        };

        overlayLayout.setFocusable(true);

        View mView = LayoutInflater.from(this).inflate(R.layout.service_overlay, overlayLayout);
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        //params
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                100, 100, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        if (windowManager != null) {
            Log.i("key", "WINDOW MANAGER ADDED VIEW");
            windowManager.addView(mView, params);
        } else {
            Log.i("key", "WINDOW MANAGER NULL");
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
