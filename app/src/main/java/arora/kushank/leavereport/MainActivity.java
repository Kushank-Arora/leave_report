package arora.kushank.leavereport;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import arora.kushank.leavereport.applicant.ApplicantActivity;
import arora.kushank.leavereport.verifier.ListOfReportsActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String CLASS_COCERNED_KEY = "class_concerned_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.bloginApplicant).setOnClickListener(this);
        findViewById(R.id.bloginVerifier).setOnClickListener(this);

        String[] holidays=new String[]{"Casual Leave","Academic Leave","Compensatory Leave","On Duty Leave"};
        util.typeOfHolidays = new ArrayList<String>();
        for(String holiday: holidays)
            util.typeOfHolidays.add(holiday);
    }

    @Override
    public void onClick(View v) {
        boolean ApplicantClass=true;
        switch(v.getId()){
            case R.id.bloginApplicant:
                ApplicantClass=true;
                break;
            case R.id.bloginVerifier:
                ApplicantClass=false;
                break;
        }
        Bundle myBundle = new Bundle();
        myBundle.putBoolean(CLASS_COCERNED_KEY,ApplicantClass);

        Intent i = new Intent(this,LoginActivity.class);
        i.putExtras(myBundle);

        startActivity(i);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(FirebaseAuth.getInstance().getCurrentUser()==null) {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){
            case R.id.SignOut:
                FirebaseAuth.getInstance().signOut();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
