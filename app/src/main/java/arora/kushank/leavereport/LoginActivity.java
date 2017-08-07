package arora.kushank.leavereport;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;

import arora.kushank.leavereport.applicant.ApplicantActivity;
import arora.kushank.leavereport.verifier.ListOfReportsActivity;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String CLASS_COCERNED_KEY = "class_concerned_key";
    private static final String FILENAME = "shared_pref";
    EditText etEmailID, etPassword;
    Button bSubmit;
    FirebaseAuth mAuth;
    static final String TAG= "LoginActivity";
    boolean isApplicant;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private ProgressBar pbLogin;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessagesDatabaseReference;

    static boolean calledAlready = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(!calledAlready){
            try {
                FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            }catch(Exception e)
            {
                e.printStackTrace();
            }
            calledAlready=true;
        }

        Bundle myBundle = getIntent().getExtras();

        if(myBundle==null) {
            isApplicant = true;
        }else{
            isApplicant = myBundle.getBoolean(CLASS_COCERNED_KEY,true);
        }

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("users");

        etEmailID = (EditText) findViewById(R.id.etEmailId);
        etPassword = (EditText) findViewById(R.id.etpassword);
        bSubmit = (Button) findViewById(R.id.bSubmit);
        pbLogin = (ProgressBar) findViewById(R.id.pbLogin);


        assert bSubmit != null;
        bSubmit.setOnClickListener(this);

        pbLogin.setVisibility(View.INVISIBLE);

        mAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    //User is signed in
                    FirebaseMessaging.getInstance().subscribeToTopic("user_"+firebaseUser.getUid());
                    Log.d(TAG, FirebaseInstanceId.getInstance().getToken()+" Subscribed to "+ "user_"+firebaseUser.getUid());
                    onSignedInInitialise(firebaseUser.getDisplayName());

                } else {
                    //User is signed out
                    Log.d(TAG,"User signed out");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    FirebaseInstanceId.getInstance().deleteInstanceId();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth.addAuthStateListener(mAuthListener);
    }

    private void onSignedInInitialise(String displayName) {
        Toast.makeText(this,"Welcome "+displayName,Toast.LENGTH_SHORT).show();
        if(isApplicant)
            startActivity(new Intent(LoginActivity.this, ApplicantActivity.class));
        else
            startActivity(new Intent(LoginActivity.this, ListOfReportsActivity.class));

    }


    @Override
    public void onClick(View v) {
        hideKeyboard();

        String email=etEmailID.getText().toString();
        String password = etPassword.getText().toString();
        if(email.trim().equals("")) {
            etEmailID.setError("It is required");
        }else if(password.trim().equals("")) {
            etPassword.setError("It is required");
        }else{
            pbLogin.setVisibility(View.VISIBLE);
            bSubmit.setEnabled(false);
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "signInWithEmail:success");
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, "Authentication failed! "+task.getException().getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                            }
                            pbLogin.setVisibility(View.GONE);
                            bSubmit.setEnabled(true);
                        }
                    });
        }
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if(getCurrentFocus()!=null)
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAuth.removeAuthStateListener(mAuthListener);
        finish();
    }
}
