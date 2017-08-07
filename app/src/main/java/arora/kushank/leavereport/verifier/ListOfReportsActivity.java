package arora.kushank.leavereport.verifier;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import arora.kushank.leavereport.R;
import arora.kushank.leavereport.ReportClass;
import arora.kushank.leavereport.User;
import arora.kushank.leavereport.applicant.userDetails;
import arora.kushank.leavereport.util;

public class ListOfReportsActivity extends AppCompatActivity {

    //private static final String ANONYMOUS = "anonymous";
    //private static final String DEF_UID_RECIEVER = "sAMWYHS2sIWP31fHFUETyYeAG852";
    ListView listView;
    CustomListAdapter customListAdapter;
    public ArrayList<ReportClass> reportsList;
    //User mUser;
    //private User receiver;

    private static final String VC_UID = "IMMRRq6CW4P9XXJ6BWG25CrRsbk1";

    public static final String BAG_KEY_EXTRA_DATES_EXCEED ="datesExceedB";

    public static final String BAG_KEY_SENDER_DESIGNATION = "designationB";
    public static final String BAG_KEY_COMMENT_VC = "CommentVCB";
    public static final String BAG_KEY_SENDER_UID = "senderUIDB";
    public static final String BAG_KEY_COMMENT = "CommentB";
    public static final String BAG_KEY_STATUS = "StatusB";
    public static final String BAG_KEY_REPORTKEY = "reportKeyB";
    public static final String BAG_KEY_SUBJECT="subjectB";
    public static final String BAG_KEY_SENDER_NAME="nameB";
    public static final String BAG_KEY_SENDER_EMAIL="emailB";
    public static final String BAG_KEY_RECEIVER_UID = "receiverUIDB";

    public static final String BAG_KEY_DETAILS_TYPE="detailsTypeB";
    public static final String BAG_KEY_DETAILS_DUR_START="detailsDurStartB";
    public static final String BAG_KEY_DETAILS_DUR_END="detailsDurEndB";
    public static final String BAG_KEY_DETAILS_ADD="detailsAddB";
    public static final String BAG_KEY_DETAILS_ATTACH_URL="detailsAttachURLB";
    public static final String BAG_KEY_TIME="timeB";

    private FirebaseAuth firebaseAuth;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessagesDatabaseReference;
    private ChildEventListener mChildEventListener;


    private Map<Long, Integer> datesApplicantMap;

    public static boolean isVC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);

        setContentView(R.layout.activity_list_of_reports);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        datesApplicantMap = new HashMap<>();

        firebaseAuth = FirebaseAuth.getInstance();
        listView = (ListView) findViewById(R.id.lvReports);
        if(firebaseAuth.getCurrentUser() == null){
            finish();
            return;
        }

        isVC = firebaseAuth.getCurrentUser().getUid().equals(VC_UID);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("leave_reports");
        mMessagesDatabaseReference.keepSynced(true);


        reportsList = new ArrayList<>();
        //mUser = new User(null, firebaseAuth.getCurrentUser().getDisplayName(), firebaseAuth.getCurrentUser().getEmail(), firebaseAuth.getCurrentUser().getUid(),null,null);
        //receiver = new User(null,null,null,DEF_UID_RECIEVER,null,null);

        //setList();
        //setHasRead(hasRead);

        customListAdapter = new CustomListAdapter(this, reportsList);

        listView.setAdapter(customListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView subject = (TextView) view.findViewById(R.id.subject);
                TextView sender = (TextView) view.findViewById(R.id.sender);
                TextView time = (TextView) view.findViewById(R.id.time);
                subject.setTypeface(null, Typeface.NORMAL);
                sender.setTypeface(null, Typeface.NORMAL);
                time.setTypeface(null, Typeface.NORMAL);
                subject.setTextColor(Color.rgb(0x66, 0x66, 0x66));
                time.setTextColor(Color.rgb(0x66, 0x66, 0x66));

                if(isVC)
                    reportsList.get(position).setReadVC(true);
                else
                    reportsList.get(position).setReadChairman(true);

                updateRead(reportsList.get(position).getReportKey(),reportsList.get(position).getSender(),true);
                int newStatus = updateStatus(reportsList.get(position).getReportKey(),reportsList.get(position).getSender(),reportsList.get(position).getStatus());
                reportsList.get(position).setStatus(newStatus);

                ArrayList<Long> datesExceed = new ArrayList<>();
                long start = reportsList.get(position).detail.getDurationStart();
                long duration = util.getDaysCount(reportsList.get(position).detail.getDurationStart(),reportsList.get(position).detail.getDurationEnd());
                for(int i=0; i<duration; i++) {
                    long date = start + i * 24 * 60 * 60 * 1000;
                    if(datesApplicantMap.get(date)!=null && datesApplicantMap.get(date)>=1 &&(newStatus==0 || newStatus==1))
                        datesExceed.add(date);
                }
                if(datesExceed.size() == 0)
                    datesExceed = null;

                onClickOfReportItem(reportsList.get(position), ListOfReportsActivity.this, datesExceed,false);
            }
        });
    }

    public static void onClickOfReportItem(ReportClass tempValues, Context context, ArrayList<Long> datesExceed, boolean forViewing) {
        Intent i=new Intent(context,ReportDetailActivity.class);
        Bundle myBag=new Bundle();
        myBag.putString(BAG_KEY_REPORTKEY,tempValues.getReportKey());
        myBag.putString(BAG_KEY_SUBJECT,tempValues.getSubject());
        myBag.putString(BAG_KEY_SENDER_NAME,tempValues.getSender().getName());
        myBag.putString(BAG_KEY_SENDER_EMAIL,tempValues.getSender().getEmail_id());
        myBag.putString(BAG_KEY_SENDER_UID,tempValues.getSender().getUser_id());
        myBag.putString(BAG_KEY_SENDER_DESIGNATION,tempValues.getSender().getDesignation());
        myBag.putString(BAG_KEY_RECEIVER_UID, tempValues.getSender().getReportingTo().getUser_id());
        myBag.putString(BAG_KEY_DETAILS_ADD,tempValues.getDetail().getAddress());
        myBag.putLong(BAG_KEY_DETAILS_DUR_END,tempValues.getDetail().getDurationEnd());
        myBag.putLong(BAG_KEY_DETAILS_DUR_START,tempValues.getDetail().getDurationStart());

        if(tempValues.getDetail().getAttachment()==null){
            myBag.putStringArrayList(BAG_KEY_DETAILS_ATTACH_URL,null);
        }else{
            myBag.putStringArrayList(BAG_KEY_DETAILS_ATTACH_URL,tempValues.getDetail().getAttachment().getURL());
        }
        myBag.putString(BAG_KEY_DETAILS_TYPE,tempValues.getDetail().getType());
        myBag.putLong(BAG_KEY_TIME,tempValues.getTimeOfArrival());
        myBag.putInt(BAG_KEY_STATUS,tempValues.getStatus());
        myBag.putString(BAG_KEY_COMMENT,tempValues.getCommentChairman());
        myBag.putString(BAG_KEY_COMMENT_VC,tempValues.getCommentVC());

        if(datesExceed != null) {
            long[] datesExceedArray = new long[datesExceed.size()];
            int c=0;
            for(Object ele : datesExceed.toArray())
                datesExceedArray[c++]= (long) ele;
            myBag.putLongArray(BAG_KEY_EXTRA_DATES_EXCEED, datesExceedArray);
        }else
            myBag.putLongArray(BAG_KEY_EXTRA_DATES_EXCEED, null);

        myBag.putBoolean(userDetails.KEY_BAG_FORVIEWING,forViewing);

        i.putExtras(myBag);
        context.startActivity(i);//, ActivityOptions.makeSceneTransitionAnimation(ListOfReportsActivity.this,listView.getChildAt(position),"itemTrans").toBundle());
    }

    private int updateStatus(String reportKey, User sender,int status) {
        if(isVC){
            if(status<5) {
                mMessagesDatabaseReference.child(sender.getUser_id()).child(reportKey).child("status").setValue(5);
                return 5;
            }
            else
                return status;
        }else {
            if (status == 0) {
                mMessagesDatabaseReference.child(sender.getUser_id()).child(reportKey).child("status").setValue(1);
                return 1;
            } else
                return status;
        }
    }

    private void updateRead(String reportKey, User sender, boolean b) {
        if(isVC)
            mMessagesDatabaseReference.child(sender.getUser_id()).child(reportKey).child("readVC").setValue(b);
        else
            mMessagesDatabaseReference.child(sender.getUser_id()).child(reportKey).child("readChairman").setValue(b);
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
            User user= new User(null, "Sapna Ma'am", "sapna97@gmail.com"+i, "1233");
            final ReportClass sched = new ReportClass("My daughter has her board exams this week.", Calendar.getInstance().getTimeInMillis(), user,receiver,  details, false);
            reportsList.add(sched);
        }
    }
*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_of_reports, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){
            case R.id.SignOut:
                firebaseAuth.signOut();
                finish();
                return true;
            case R.id.action_leave_calendar:
                startActivity(new Intent(this, LeavesCalendarActivity.class));
                return true;
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
        reportsList.clear();
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
                    Log.d("LORA","got an Object");
                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                    for(DataSnapshot i: children)
                    {
                        Log.d("LORA",i.toString());
                        try {
                            ReportClass report = i.getValue(ReportClass.class);

                            if (report == null)
                                continue;
                            assert firebaseAuth.getCurrentUser() != null;
                            if (report.getCopyTo() != null &&
                                    report.getCopyTo().getUser_id().equals(firebaseAuth.getCurrentUser().getUid()))
                                ;
                            else if (!report.getReceiver().getUser_id().equals(firebaseAuth.getCurrentUser().getUid()))
                                continue;

                            report.setReportKey(i.getKey());
                            reportsList.add(report);
                        }catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    util.sort(reportsList);
                    customListAdapter.notifyDataSetChanged();
                    updateDatesApplicantsMap(reportsList);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Log.d("LORA","got on Child Changed");

                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                    for(DataSnapshot i: children) {
                        try {
                            ReportClass report = i.getValue(ReportClass.class);
                            if (report == null)
                                continue;
                            assert firebaseAuth.getCurrentUser() != null;
                            if (report.getCopyTo() != null &&
                                    report.getCopyTo().getUser_id().equals(firebaseAuth.getCurrentUser().getUid()))
                                ;
                            else if (!report.getReceiver().getUser_id().equals(firebaseAuth.getCurrentUser().getUid()))
                                continue;

                            report.setReportKey(i.getKey());

                            for (int j = 0; j < reportsList.size(); j++)
                                if (reportsList.get(j).getReportKey().equals(report.getReportKey())) {
                                    reportsList.remove(j);
                                    break;
                                }

                            reportsList.add(report);
                        } catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    util.sort(reportsList);
                    customListAdapter.notifyDataSetChanged();
                    updateDatesApplicantsMap(reportsList);
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d("LORA","got an Object to remove");
                    for(ReportClass reportClass: reportsList){
                        try {
                            if (reportClass.getReportKey().equals(dataSnapshot.getKey())) {
                                reportsList.remove(reportClass);
                                break;
                            }
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    updateDatesApplicantsMap(reportsList);
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

    private void updateDatesApplicantsMap(ArrayList<ReportClass> reportsList) {
        datesApplicantMap.clear();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        for(ReportClass report: reportsList)
        {
            if(report.getStatus() == 4 ||
                    report.getStatus() == 6)
            {
                long start = report.detail.getDurationStart();
                long duration = util.getDaysCount(report.detail.getDurationStart(),report.detail.getDurationEnd());
                for(int i=0; i<duration; i++) {
                    long date = start + i * 24 * 60 * 60 * 1000;
                    int count = 0 ;
                    if(datesApplicantMap.containsKey(date))
                        count = datesApplicantMap.get(date);
                    datesApplicantMap.put(date , count + 1);
                }
            }
        }
    }
}
