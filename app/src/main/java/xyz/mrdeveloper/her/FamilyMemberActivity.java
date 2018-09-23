package xyz.mrdeveloper.her;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

import java.util.ArrayList;

public class FamilyMemberActivity extends AppCompatActivity implements FamilyMemberAdapter.RecyclerViewOnClickListener {

    public String mPhoneNumber;
    public ArrayList<FamilyMemberData> familyMembersList = new ArrayList<>();

    private DatabaseReference familyMembersReference;
    private ChildEventListener familyMembersEventListener;
    private boolean didRemoveEventListener = false;

    private RecyclerView familyMembersView;
    private FamilyMemberAdapter familyMembersAdapter;

    private Button addNewButton;

    private EditText familyMemberName;
    private CountryCodePicker countryCodePicker;
    private EditText familyMemberNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_member);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPhoneNumber = sharedPreferences.getString("myPhoneNumber", "No phone number");
        Log.d("Check", "GOT PHONE NUMBER" + mPhoneNumber);

        familyMembersReference = FirebaseDatabase.getInstance()
                .getReference("personData").child(mPhoneNumber).child("familyMembers");

        familyMembersEventListener = familyMembersReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                FamilyMemberData familyMember = dataSnapshot.getValue(FamilyMemberData.class);
                if (familyMember != null) {
                    Log.i("FAMILY", "ADDING " + familyMember.getNumber());
                    familyMembersList.add(familyMember);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        addNewButton = findViewById(R.id.add_new_button);

        familyMembersAdapter = new FamilyMemberAdapter(this, familyMembersList);
        familyMembersView = findViewById(R.id.family_members_list);
        familyMembersView.setLayoutManager(new LinearLayoutManager(this));
        familyMembersView.setItemAnimator(new DefaultItemAnimator());
        familyMembersView.setAdapter(familyMembersAdapter);
        addNewButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ShowFamilyMemberDialog(true, -1);
                    }
                }
        );
    }

    @Override
    public void RecyclerViewOnClick(int position) {
        ShowFamilyMemberDialog(false, position);
    }

    private void ShowFamilyMemberDialog(boolean newMember, final int position) {
        View dialogAddFamily = getLayoutInflater().inflate(R.layout.dialog_add_family, null);
        familyMemberName = dialogAddFamily.findViewById(R.id.input_name);
        countryCodePicker = dialogAddFamily.findViewById(R.id.country_code_picker);
        familyMemberNumber = dialogAddFamily.findViewById(R.id.input_number);
        countryCodePicker.registerCarrierNumberEditText(familyMemberNumber);

        final FamilyMemberData familyMember = new FamilyMemberData();
        if (!newMember) {
            familyMember.setName(familyMembersList.get(position).getName());
            familyMember.setCountryCode(familyMembersList.get(position).getCountryCode());
            familyMember.setNumber(familyMembersList.get(position).getNumber());

            familyMemberName.setText(familyMember.getName());
            countryCodePicker.setCountryForPhoneCode(familyMember.getCountryCode());
            familyMemberNumber.setText(familyMember.getNumber());
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(FamilyMemberActivity.this)
                .setTitle((newMember) ? "Add Family Member" : "Edit Family Member")
                .setView(dialogAddFamily)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", null);

        if (!newMember)
            builder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    familyMembersList.remove(position);
                    familyMembersAdapter.notifyDataSetChanged();
                    familyMembersReference.child(familyMember.getNumber()).removeValue();
                }
            });

        builder.create();

        final AlertDialog addFamilyMemberDialog = builder.show();
        addFamilyMemberDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(familyMemberName.getText().toString().trim().length() == 0)
                {
                    familyMemberName.setError("Please enter a name");
                }
                if (countryCodePicker.isValidFullNumber()) {
                    AddFamilyMember(familyMemberName.getText().toString(), countryCodePicker.getSelectedCountryCodeAsInt(), familyMemberNumber.getText().toString());
                    addFamilyMemberDialog.dismiss();
                } else {
                    familyMemberNumber.setError("Invalid phone number");
                }
            }
        });
    }

    private void AddFamilyMember(String name, int countryCode, String number) {
        familyMembersList.add(new FamilyMemberData(name, countryCode, number));
        Log.i("FAMILY LIST", familyMembersList.toString());

        Toast.makeText(getApplicationContext(), "Successfully added " + name, Toast.LENGTH_SHORT).show();
        familyMemberNumber.setText("");
        familyMemberName.setText("");

        if (!didRemoveEventListener) {
            familyMembersReference.removeEventListener(familyMembersEventListener);
            didRemoveEventListener = true;
        }
        familyMembersReference.child(number).child("name").setValue(name);
        familyMembersReference.child(number).child("countryCode").setValue(countryCode);
        familyMembersReference.child(number).child("number").setValue(number);
    }
}