package arora.kushank.leavereport.applicant;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import arora.kushank.leavereport.R;
import arora.kushank.leavereport.util;
import arora.kushank.leavereport.ReportClass;

public class ApplicantActivity extends AppCompatActivity {

    RecyclerView listOfSubmittedApplications;
    private ArrayList<ReportClass> applicationsList;

    FirebaseAuth firebaseAuth;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessagesDatabaseReference;
    private ChildEventListener mChildEventListener;

    private CustomApplicant customListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applicant);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser==null) {
            Toast.makeText(this,"UnAuthorized Access",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        firebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("leave_reports").child(firebaseUser.getUid());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ApplicantActivity.this,ApplicantForm.class));
            }
        });

        applicationsList = new ArrayList<>();

        //setList();

        customListAdapter = new CustomApplicant(this, applicationsList);
        listOfSubmittedApplications= (RecyclerView) findViewById(R.id.rvApplicantActivity);
        listOfSubmittedApplications.setLayoutManager(new LinearLayoutManager(this));

        assert listOfSubmittedApplications != null;
        listOfSubmittedApplications.setAdapter(customListAdapter);
    }
    /*
    private void setList() {
        Calendar cal=Calendar.getInstance();
        cal.set(Calendar.DATE,12);
        long startDur=cal.getTimeInMillis();
        cal.set(Calendar.DATE,15);
        long endDur=cal.getTimeInMillis();
        //String details="Hello Kush,\n\nSince you were not amongst the top 1000 in Round 1A, you have not advanced to Online Round 2. However, you can still advance competing in Round 1B on Saturday, April 22, 2017 at 16:00 UTC.\nSince you were not amongst the top 1000 in Round 1A, you have not advanced to Online Round 2. However, you can still advance competing in Round 1B on Saturday, April 22, 2017 at 16:00 UTC.\nSince you were not amongst the top 1000 in Round 1A, you have not advanced to Online Round 2. However, you can still advance competing in Round 1B on Saturday, April 22, 2017 at 16:00 UTC.\nSince you were not amongst the top 1000 in Round 1A, you have not advanced to Online Round 2. However, you can still advance competing in Round 1B on Saturday, April 22, 2017 at 16:00 UTC.\n\nGood Luck,\n\nThe Code Jam Team";
        ReportDetail details=new ReportDetail("Casual Leave",startDur,endDur,"H.No.123\nSector 41\nFaridabad-121031");
        for (int i = 0; i < 11; i++) {
            ApplicationListClass sched = new ApplicationListClass("My daughter has her board exams this week.", details, Calendar.getInstance().getTimeInMillis());
            applicationsList.add(sched);
        }
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_applicant_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id){
            case R.id.SignOut:
                firebaseAuth.signOut();
                finish();
                return true;
            case R.id.seeHelp:
                break;
            case R.id.seeHistory:
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user==null)
                    break;
                Bundle extras = new Bundle();
                extras.putString(userDetails.KEY_BAG_USER_ID, user.getUid());
                extras.putBoolean(userDetails.KEY_BAG_APPLICANT, true);

                Intent i = new Intent(ApplicantActivity.this, userDetails.class);
                i.putExtras(extras);

                startActivity(i);
                break;
            default:
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        attachDatabaseReadListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        detachDatabaseReadListener();
        applicationsList.clear();
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mMessagesDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }


    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Log.d("ApplicantActivity","onChildAdded");
                    try {
                        ReportClass report = dataSnapshot.getValue(ReportClass.class);
                        //ApplicationListClass application = new ApplicationListClass(report.getSubject(),report.getDetail(),report.getTimeOfArrival());
                        //application.setReportKey(dataSnapshot.getKey());
                        //application.setStatus(report.getStatus());
                        assert report != null;
                        report.setReportKey(dataSnapshot.getKey());
                        applicationsList.add(report);
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    util.sort(applicationsList);
                    customListAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Log.d("ApplicantActivity","onChildChanged");
                    try {
                        ReportClass report = dataSnapshot.getValue(ReportClass.class);
                        for (int i = 0; i < applicationsList.size(); i++)
                            if (applicationsList.get(i).getReportKey().equals(dataSnapshot.getKey())) {
                                applicationsList.remove(i);
                                break;
                            }
                        report.setReportKey(dataSnapshot.getKey());
                        applicationsList.add(report);
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    util.sort(applicationsList);
                    customListAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d("ApplicantActivity","onChildRemoved");
                    try {
                        for (int i = 0; i < applicationsList.size(); i++)
                            if (applicationsList.get(i).getReportKey().equals(dataSnapshot.getKey())) {
                                applicationsList.remove(i);
                                customListAdapter.notifyDataSetChanged();
                                break;
                            }
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            mMessagesDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }
}
