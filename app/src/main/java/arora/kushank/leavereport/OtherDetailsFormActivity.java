package arora.kushank.leavereport;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class OtherDetailsFormActivity extends AppCompatActivity {

    private Spinner spAuthority;
    private Button bSubmit;
    private ProgressBar pbLogin;
    private DatabaseReference mUsersDatabaseReference;
    private DatabaseReference mChairmansDatabaseReference;
    private ArrayAdapter<CharSequence> adapter;
    private ArrayList<String> chairman_uid;
    private ArrayList<User> users;
    private ArrayList<User> chairman_user_adapter;

    private ChildEventListener mChildEventListener;
    private ChildEventListener mChildEventListenerUsers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_details_form);

        initViews();

        bSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginPressed();
            }
        });
    }

    private void initViews() {
        spAuthority = (Spinner) findViewById(R.id.spAuthority);

        bSubmit = (Button) findViewById(R.id.bSubmit);

        pbLogin = (ProgressBar) findViewById(R.id.pbLogin);

        mUsersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        mChairmansDatabaseReference = FirebaseDatabase.getInstance().getReference().child("verifier");

        mUsersDatabaseReference.keepSynced(true);
        mChairmansDatabaseReference.keepSynced(true);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAuthority.setAdapter(adapter);

        chairman_uid = new ArrayList<>();
        users = new ArrayList<>();
        chairman_user_adapter = new ArrayList<>();

    }

    void LoginPressed(){
        FirebaseUser firebaseCurUser = FirebaseAuth.getInstance().getCurrentUser();
        if(adapter.getCount()==0 || firebaseCurUser==null)
        {
            Toast.makeText(OtherDetailsFormActivity.this, "Unable to connect to server", Toast.LENGTH_SHORT).show();
            return;
        }

        final User authority = chairman_user_adapter.get(spAuthority.getSelectedItemPosition());
        pbLogin.setVisibility(View.VISIBLE);
        bSubmit.setEnabled(false);

        User curUser = null;
        for(User user : users)
            if(user.getUser_id().equals(firebaseCurUser.getUid())) {
                curUser = user;
                break;
            }

        assert curUser != null;
        curUser.setReportingTo(new User(null,null,null,authority.getUser_id(),null,null));
        final User finalCurUser = curUser;
        mUsersDatabaseReference.child(curUser.getUser_id()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mUsersDatabaseReference.child(finalCurUser.getUser_id()).push().setValue(finalCurUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d("ODFA", "User profile updated.");
                            Toast.makeText(OtherDetailsFormActivity.this, "User profile updated.", Toast.LENGTH_SHORT).show();
                            finish();
                        }else{
                            Log.d("ODFA", "User profile updatation failed.");
                            Toast.makeText(OtherDetailsFormActivity.this, "User profile updatation failed.", Toast.LENGTH_SHORT).show();
                        }
                        pbLogin.setVisibility(View.GONE);
                        bSubmit.setEnabled(true);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("ODFA", "User profile deletion failed.");
                Toast.makeText(OtherDetailsFormActivity.this, "User profile updatation failed.", Toast.LENGTH_SHORT).show();
                pbLogin.setVisibility(View.GONE);
                bSubmit.setEnabled(true);
            }
        });
    }

    private void updateList() {
        if(users ==null || chairman_uid==null)
            return;

        adapter.clear();
        chairman_user_adapter.clear();

        Set<String> mp = new HashSet<>();

        for (String c_id : chairman_uid) {
            if(mp.contains(c_id))
                continue;
            mp.add(c_id);

            for(User user: users) {
                Log.d("ODFA","updateList");
                Log.d("ODFA",user.getName());
                if (user.getUser_id().equals(c_id)) {
                    adapter.add(user.getName());
                    chairman_user_adapter.add(user);
                    break;
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        detachDatabaseReadListener();
        chairman_uid.clear();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        attachDatabaseReadListener();
        attachDatabaseReadListenerUsers();
    }
    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mChairmansDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
        if (mChildEventListenerUsers != null) {
            mUsersDatabaseReference.removeEventListener(mChildEventListenerUsers);
            mChildEventListenerUsers = null;
        }
    }


    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Log.d("ODFAChairman", "got an Object");
                    try {
                        chairman_uid.add(dataSnapshot.getValue(String.class));
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    Log.d("ODFAChairman", "Got user info");
                    updateList();
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Log.d("CreateAccountOnChange", "got an Object");
                    try {
                        chairman_uid.remove(dataSnapshot.getValue(String.class));
                        chairman_uid.add(dataSnapshot.getValue(String.class));
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    Log.d("CreateAccountOnChange", "Got user info");
                    updateList();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            mChairmansDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    private void attachDatabaseReadListenerUsers() {
        if (mChildEventListenerUsers == null) {
            mChildEventListenerUsers = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Log.d("ODFA", "got an Object");

                    for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                        Log.d("ODFA", messageSnapshot.toString());
                        try {
                            users.add(messageSnapshot.getValue(User.class));
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    //HashMap<String,User> hp = dataSnapshot.getValue(GenericTypeIndicator<>.class);
                    //User user = (User) hp.values().toArray()[0];
                    //users.add(user);
                    //Log.d("ODFA", dataSnapshot.toString());

                    updateList();
                    Log.d("ODFA", "Got user info");
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Log.d("ODFAOnChange", "Not doing anything got an Object");
                    /*
                    for(DataSnapshot child : dataSnapshot.getChildren())
                        if(child.getValue(User.class)!=null) {
                            users.add(child.getValue(User.class));
                        }
                        */
                    Log.d("ODFAOnChange", "Not doing anything Got user info");
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            mUsersDatabaseReference.addChildEventListener(mChildEventListenerUsers);
        }
    }
}
