package xyz.mrdeveloper.her;

import android.app.Application;
import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

public class HerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        FirebaseApp.initializeApp(this);
    }
}
