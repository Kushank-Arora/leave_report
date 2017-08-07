package arora.kushank.leavereport.verifier;

import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import arora.kushank.leavereport.R;
import arora.kushank.leavereport.ReportClass;
import arora.kushank.leavereport.Status;
import arora.kushank.leavereport.util;

import static java.util.Arrays.sort;
import static java.util.Collections.reverse;

public class LeavesCalendarActivity extends AppCompatActivity {

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessagesDatabaseReference;
    private ChildEventListener mChildEventListener;
    private FirebaseAuth firebaseAuth;
    private Map<Long,Integer> datesApplicantMap;
    private ArrayList<ReportClass> reportsList;
    private TableLayout tlLeavesCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaves_calendar);

        firebaseAuth = FirebaseAuth.getInstance();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("leave_reports");
        mMessagesDatabaseReference.keepSynced(true);

        datesApplicantMap = new HashMap<>();
        reportsList = new ArrayList<>();

        tlLeavesCalendar = (TableLayout) findViewById(R.id.tlLeavesCalendar);
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
        datesApplicantMap.clear();
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
                    Log.d("LCA","got an Object");
                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                    for(DataSnapshot i: children)
                    {
                        try {
                            ReportClass report = i.getValue(ReportClass.class);
                            if (report == null)
                                continue;
                            assert firebaseAuth.getCurrentUser() != null;
                            if (!report.getReceiver().getUser_id().equals(firebaseAuth.getCurrentUser().getUid()))
                                continue;

                            report.setReportKey(i.getKey());
                            reportsList.add(report);
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    util.sort(reportsList);
                    updateDatesApplicantsMap(reportsList);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Log.d("LORA","got on Child Changed");

                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                    for(DataSnapshot i: children)
                    {
                        try {
                            ReportClass report = i.getValue(ReportClass.class);
                            if (report == null)
                                continue;
                            assert firebaseAuth.getCurrentUser() != null;
                            if (!report.getReceiver().getUser_id().equals(firebaseAuth.getCurrentUser().getUid()) &&
                                    !report.getCopyTo().getUser_id().equals(firebaseAuth.getCurrentUser().getUid()))
                                continue;

                            report.setReportKey(i.getKey());

                            for (int j = 0; j < reportsList.size(); j++)
                                if (reportsList.get(j).getReportKey().equals(report.getReportKey())) {
                                    reportsList.remove(j);
                                    break;
                                }

                            reportsList.add(report);
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    util.sort(reportsList);
                    updateDatesApplicantsMap(reportsList);
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d("LORA","got an Object to remove");
                    try {
                        for (ReportClass reportClass : reportsList) {
                            if (reportClass.getReportKey().equals(dataSnapshot.getKey())) {
                                reportsList.remove(reportClass);
                                break;
                            }
                        }
                    }catch (Exception e)
                    {
                        e.printStackTrace();
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
        setValues();
    }

    private void setValues() {
        tlLeavesCalendar.removeAllViews();
        TableRow.LayoutParams layoutParamsRow = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,0.5f);

        layoutParams.setMargins(10,10,10,10);

        TableRow tableRow=new TableRow(this);
        TextView tvDate = new TextView(this);
        TextView tvDays = new TextView(this);

        tableRow.setLayoutParams(layoutParamsRow);
        tvDays.setLayoutParams(layoutParams);
        tvDate.setLayoutParams(layoutParams);

        tvDays.setText("Days");
        tvDate.setText("Applicants");

        tvDays.setTextSize(13);
        tvDate.setTextSize(13);

        tvDays.setPadding(10,10,10,10);
        tvDate.setPadding(10,10,10,10);

        tableRow.addView(tvDate);
        tableRow.addView(tvDays);

        tableRow.setBackgroundColor(Color.rgb(68,114,196));

        tlLeavesCalendar.addView(tableRow,0);

        int c=0;
        Object[] dates = datesApplicantMap.keySet().toArray();
        sort(dates, new Comparator<Object>() {
            @Override
            public int compare(Object l, Object r) {
                long lhs = (long) l, rhs = (long) r;
                if(lhs>rhs)
                    return -1;
                else if(lhs<rhs)
                    return 1;
                else
                    return 0;
            }
        });
        for(Object dateO : dates){
            long date = (long) dateO;
            tableRow=new TableRow(this);
            tvDays = new TextView(this);
            tvDate = new TextView(this);

            tableRow.setLayoutParams(layoutParamsRow);
            tvDays.setLayoutParams(layoutParams);
            tvDate.setLayoutParams(layoutParams);

            tvDays.setText(String.valueOf(datesApplicantMap.get(date)));
            tvDate.setText(util.getDateString(date));

            tvDays.setTextSize(13);
            tvDate.setTextSize(13);

            tvDays.setPadding(10,10,10,10);
            tvDate.setPadding(10,10,10,10);

            tableRow.addView(tvDate);
            tableRow.addView(tvDays);

            if((datesApplicantMap.keySet().size()-c-1)%2==0)
                tableRow.setBackgroundColor(Color.rgb(217,217,217));
            tlLeavesCalendar.addView(tableRow,c+1);
            c++;
        }
    }
}
