package xyz.mrdeveloper.her;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import static xyz.mrdeveloper.her.MainActivity.lovedList;
import static xyz.mrdeveloper.her.Signup.phoneNumber;

public class LovedOneActivity extends AppCompatActivity {

    private RecyclerView lovedView;
    private Button addNew;
    private LoveAdapter loveAdapter;

    private RelativeLayout loved_list;
    private LinearLayout add_new;
    private Button addConfirm;

    private EditText lovedName;
    private EditText lovedNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loved_one);

        loved_list = (RelativeLayout) findViewById(R.id.loved_list_view);
        add_new = (LinearLayout) findViewById(R.id.add_new_form);
        add_new.setVisibility(View.GONE);

        lovedName = (EditText) findViewById(R.id.input_name);
        lovedNumber = (EditText) findViewById(R.id.input_number);

        loveAdapter = new LoveAdapter(lovedList);

        lovedView = (RecyclerView) findViewById(R.id.loved_list);
        addNew = (Button) findViewById(R.id.add_new);
        addConfirm = (Button) findViewById(R.id.add_new_confirm);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        lovedView.setLayoutManager(mLayoutManager);
        lovedView.setItemAnimator(new DefaultItemAnimator());
        lovedView.setAdapter(loveAdapter);

        addNew.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        loved_list.setVisibility(View.GONE);
                        add_new.setVisibility(View.VISIBLE);
                    }
                }
        );

        addConfirm.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        final DatabaseReference mFirebaseDatabase;
                        FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();
                        mFirebaseDatabase = mFirebaseInstance.getReference("PersonData");

                        String name, number;
                        name = lovedName.getText().toString();
                        number = lovedNumber.getText().toString();


                        if (number.length() == 10) {
                            lovedList.add(new LovedOnes(name, number));

                            loved_list.setVisibility(View.VISIBLE);
                            add_new.setVisibility(View.GONE);

                            Toast.makeText(getApplicationContext(), "Successfully Added", Toast.LENGTH_LONG).show();
                            lovedNumber.setText("");
                            lovedName.setText("");

                            mFirebaseDatabase.child(phoneNumber).child("LovedOnes").child(number).child("number").setValue(number);
                            mFirebaseDatabase.child(phoneNumber).child("LovedOnes").child(number).child("name").setValue(name);
                        } else {
                            Toast.makeText(getApplicationContext(), "Invalid number. Try again", Toast.LENGTH_LONG).show();
                            lovedNumber.setText("");
                        }
                    }
                }
        );
    }
}
