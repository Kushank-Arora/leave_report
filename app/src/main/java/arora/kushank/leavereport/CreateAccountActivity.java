package arora.kushank.leavereport;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CreateAccountActivity extends AppCompatActivity {

    EditText etEmailId, etPassword, etUserName, etDesignation;
    RadioGroup rgGender;
    Button bSubmit;
    ProgressBar pbLogin;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private DatabaseReference mUsersDatabaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        initViews();

        bSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginPressed();
            }
        });
        mAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    //User is signed in
                    Toast.makeText(CreateAccountActivity.this,"Welcome "+firebaseUser.getDisplayName(),Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(CreateAccountActivity.this,MainActivity.class));
                } else {
                    //User is signed out
                }
            }
        };
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if(getCurrentFocus()!=null)
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
    }


    private void LoginPressed() {
        hideKeyboard();

        String email=etEmailId.getText().toString();
        String password = etPassword.getText().toString();
        final String username = etUserName.getText().toString();
        final String designation = etDesignation.getText().toString();
        final boolean isFemale = (rgGender.getCheckedRadioButtonId()==R.id.rbFemale);
        if(email.trim().equals("")) {
            etEmailId.setError("It is required");
        }else if(password.trim().equals("")) {
            etPassword.setError("It is required");
        }else if(designation.trim().equals("")) {
            etDesignation.setError("It is required");
        }else {
            pbLogin.setVisibility(View.VISIBLE);
            bSubmit.setEnabled(false);
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Toast.makeText(CreateAccountActivity.this, "Account Created", Toast.LENGTH_SHORT).show();
                                Log.d("CAA", "createUserWithEmail:success");
                                final FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(username).build();
                                assert firebaseUser != null;
                                firebaseUser.updateProfile(profileUpdates)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    User user=new User(null,firebaseUser.getDisplayName(),firebaseUser.getEmail(),firebaseUser.getUid(),designation,null);
                                                    user.setFemale(isFemale);
                                                    user.setHolidays();
                                                    mUsersDatabaseReference.child(firebaseUser.getUid()).push().setValue(user);
                                                    Log.d("CAA", "User profile updated.");
                                                    Toast.makeText(CreateAccountActivity.this, "User profile updated.", Toast.LENGTH_SHORT).show();
                                                }
                                                pbLogin.setVisibility(View.GONE);
                                                bSubmit.setEnabled(true);
                                            }
                                        });
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("CAA", "createUserWithEmail:failure", task.getException());
                                Toast.makeText(CreateAccountActivity.this, "Account Creation failed! " + task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                pbLogin.setVisibility(View.GONE);
                                bSubmit.setEnabled(true);
                            }
                        }
                    });
        }
    }

    private void initViews() {
        etEmailId = (EditText) findViewById(R.id.etEmailId);
        etPassword = (EditText) findViewById(R.id.etpassword);
        etUserName = (EditText) findViewById(R.id.etUserName);
        etDesignation = (EditText) findViewById(R.id.etDesignation);
        rgGender = (RadioGroup) findViewById(R.id.rgGender);

        bSubmit = (Button) findViewById(R.id.bSubmit);

        pbLogin = (ProgressBar) findViewById(R.id.pbLogin);

        mAuth = FirebaseAuth.getInstance();

        mUsersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
    }




    @Override
    protected void onPause() {
        super.onPause();
        mAuth.removeAuthStateListener(mAuthListener);
        finish();
    }


    @Override
    protected void onResume() {
        super.onResume();
        mAuth.addAuthStateListener(mAuthListener);
    }
}
