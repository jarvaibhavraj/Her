package xyz.mrdeveloper.her;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.WindowManager;

public class VolumeChangeObserver {

    private final Context mContext;

    private VolumeChangeContentObserver mVolumeChangeContentObserver;

    public VolumeChangeObserver(@NonNull Context context) {
        mContext = context;
    }

    private static class VolumeChangeContentObserver extends ContentObserver {

        private Context mContext;

        private final AudioManager mAudioManager;
        private final int mAudioStreamType;

        private int mLastVolume;

        public VolumeChangeContentObserver(Context context, @NonNull Handler handler, @NonNull AudioManager audioManager, int audioStreamType) {
            super(handler);

            mContext = context;

            mAudioManager = audioManager;
            mAudioStreamType = audioStreamType;

            mLastVolume = mAudioManager.getStreamVolume(mAudioStreamType);
        }

        @Override
        public void onChange(boolean selfChange) {
            int currentVolume = mAudioManager.getStreamVolume(mAudioStreamType);

            Log.i("change","Something changed");
            if (currentVolume != mLastVolume) {
                mLastVolume = currentVolume;

                Intent intent = new Intent(mContext, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(intent);
            }
            else{
                Intent intent = new Intent(mContext, EmergencyActivity.class);
                intent.putExtra("workingMode", "SAFE_PLACES");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(intent);
            }
        }

        @Override
        public boolean deliverSelfNotifications() {
            Log.i("notification","notified");
            return super.deliverSelfNotifications();
        }
    }
    public void StartObserver(int audioStreamType) {
        StopObserver();

        Handler handler = new Handler();
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        mVolumeChangeContentObserver = new VolumeChangeContentObserver(mContext, handler, audioManager, audioStreamType);

        mContext.getContentResolver()
                .registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mVolumeChangeContentObserver);
    }

    public void StopObserver() {
        if (mVolumeChangeContentObserver == null) {
            return;
        }

        mContext.getContentResolver().unregisterContentObserver(mVolumeChangeContentObserver);
        mVolumeChangeContentObserver = null;
    }
}
