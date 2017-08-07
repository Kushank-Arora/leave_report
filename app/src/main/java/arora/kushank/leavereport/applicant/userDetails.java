package arora.kushank.leavereport.applicant;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import arora.kushank.leavereport.R;
import arora.kushank.leavereport.Status;
import arora.kushank.leavereport.util;
import arora.kushank.leavereport.verifier.ListOfReportsActivity;
import arora.kushank.leavereport.ReportClass;
import arora.kushank.leavereport.User;

public class userDetails extends AppCompatActivity {

    public static final String KEY_BAG_USER_ID = "userIDB";
    public static final String KEY_BAG_APPLICANT = "applicantB";
    public static final java.lang.String KEY_BAG_FORVIEWING = "forviewingB";
    private DatabaseReference mUserDatabaseReference;
    private ChildEventListener mChildEventListener;

    private DatabaseReference mUserApplicationsDatabaseReference;
    private ChildEventListener mApplicationsChildEventListener;

    private User curUser;
    private TableLayout tlSummaryLeaves, tlHistoryApplications;
    private LinearLayout llHoldingCharts;
    private ArrayList<ReportClass> reports;

    private boolean gotBoth;
    boolean isApplicant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        gotBoth = false;
        reports = new ArrayList<>();

        Bundle bundle = getIntent().getExtras();
        String firebaseUserUID = bundle.getString(KEY_BAG_USER_ID);
        isApplicant = bundle.getBoolean(KEY_BAG_APPLICANT,true);

        if (FirebaseAuth.getInstance().getCurrentUser() == null || firebaseUserUID == null) {
            Toast.makeText(this, "UnAuthorized Access", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();

        mUserDatabaseReference = mFirebaseDatabase.getReference().child("users").child(firebaseUserUID);
        mUserDatabaseReference.keepSynced(true);

        mUserApplicationsDatabaseReference = mFirebaseDatabase.getReference().child("leave_reports").child(firebaseUserUID);
        mUserApplicationsDatabaseReference.keepSynced(true);

        initViews();
    }

    private void initViews() {
        tlSummaryLeaves = (TableLayout) findViewById(R.id.tlSummaryLeaves);
        tlHistoryApplications = (TableLayout) findViewById(R.id.tlHistoryLeaves);
        llHoldingCharts = (LinearLayout) findViewById(R.id.llHoldingPieCharts);
    }

    private void setValues() {
        setTable();
        setTableHistory();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setPieCharts();
        }
    }

    private void setTable() {
        tlSummaryLeaves.removeAllViews();
        TableRow.LayoutParams layoutParamsRow = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams layoutParamsType = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,1.0f);
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,0.4f);

        layoutParams.setMargins(10,10,10,10);
        layoutParamsType.setMargins(10,10,10,10);

        TableRow tableRow=new TableRow(this);
        TextView tvType = new TextView(this);
        TextView tvAllot = new TextView(this);
        TextView tvUsed = new TextView(this);
        TextView tvLeft = new TextView(this);
        TextView tvPending = new TextView(this);

        tableRow.setLayoutParams(layoutParamsRow);
        tvType.setLayoutParams(layoutParamsType);
        tvAllot.setLayoutParams(layoutParams);
        tvUsed.setLayoutParams(layoutParams);
        tvLeft.setLayoutParams(layoutParams);
        tvPending.setLayoutParams(layoutParams);

        tvType.setText("Type of Leave");
        tvAllot.setText("Allotted");
        tvUsed.setText("Utilised");
        tvLeft.setText("Left");
        tvPending.setText("Pending");

        tvType.setTextSize(13);
        tvAllot.setTextSize(13);
        tvUsed.setTextSize(13);
        tvLeft.setTextSize(13);
        tvPending.setTextSize(13);

        tvType.setPadding(10,10,10,10);
        tvAllot.setPadding(10,10,10,10);
        tvUsed.setPadding(10,10,10,10);
        tvLeft.setPadding(10,10,10,10);
        tvPending.setPadding(10,10,10,10);

        tableRow.addView(tvType);
        tableRow.addView(tvAllot);
        tableRow.addView(tvUsed);
        tableRow.addView(tvPending);
        tableRow.addView(tvLeft);

        tableRow.setBackgroundColor(Color.rgb(68,114,196));

        tlSummaryLeaves.addView(tableRow,0);

        for(int i=0;i< util.typeOfHolidays.size();i++){
            tableRow=new TableRow(this);
            tvType = new TextView(this);
            tvAllot = new TextView(this);
            tvUsed = new TextView(this);
            tvPending = new TextView(this);
            tvLeft = new TextView(this);


            tableRow.setLayoutParams(layoutParamsRow);
            tvType.setLayoutParams(layoutParamsType);
            tvAllot.setLayoutParams(layoutParams);
            tvUsed.setLayoutParams(layoutParams);
            tvPending.setLayoutParams(layoutParams);
            tvLeft.setLayoutParams(layoutParams);

            int holAllot, holLeft, holPending;
            holPending = curUser.getHolidayPending(util.typeOfHolidays.get(i));
            holAllot = util.holidaysAlloted.get(new Pair<>(util.typeOfHolidays.get(i),curUser.isFemale()));
            holLeft = curUser.getHolidaysLeft(util.typeOfHolidays.get(i));
            tvType.setText(util.typeOfHolidays.get(i));

            if(holAllot==Integer.MAX_VALUE) {
                tvLeft.setText("-");
                tvAllot.setText("-");
            }
            else {
                tvLeft.setText(holLeft+"");
                tvAllot.setText(holAllot + "");
            }

            tvPending.setText(holPending+"");
            tvUsed.setText((holAllot-holLeft)+"");

            tvType.setTextSize(13);
            tvAllot.setTextSize(13);
            tvUsed.setTextSize(13);
            tvPending.setTextSize(13);
            tvLeft.setTextSize(13);

            tvType.setPadding(10,10,10,10);
            tvAllot.setPadding(10,10,10,10);
            tvUsed.setPadding(10,10,10,10);
            tvPending.setPadding(10,10,10,10);
            tvLeft.setPadding(10,10,10,10);

            tableRow.addView(tvType);
            tableRow.addView(tvAllot);
            tableRow.addView(tvUsed);
            tableRow.addView(tvPending);
            tableRow.addView(tvLeft);

            if((util.typeOfHolidays.size()-i-1)%2==0)
                tableRow.setBackgroundColor(Color.rgb(217,217,217));
            tlSummaryLeaves.addView(tableRow,i+1);
        }
    }

    private void setTableHistory() {
        tlHistoryApplications.removeAllViews();
        TableRow.LayoutParams layoutParamsRow = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,0.25f);

        layoutParams.setMargins(10,10,10,10);

        TableRow tableRow=new TableRow(this);
        TextView tvType = new TextView(this);
        TextView tvDays = new TextView(this);
        TextView tvDate = new TextView(this);
        TextView tvStatus = new TextView(this);

        tableRow.setLayoutParams(layoutParamsRow);
        tvType.setLayoutParams(layoutParams);
        tvDays.setLayoutParams(layoutParams);
        tvDate.setLayoutParams(layoutParams);
        tvStatus.setLayoutParams(layoutParams);

        tvType.setText("Type of Leave");
        tvDays.setText("Days");
        tvDate.setText("Application Date");
        tvStatus.setText("Status");

        tvType.setTextSize(13);
        tvDays.setTextSize(13);
        tvDate.setTextSize(13);
        tvStatus.setTextSize(13);

        tvType.setPadding(10,10,10,10);
        tvDays.setPadding(10,10,10,10);
        tvDate.setPadding(10,10,10,10);
        tvStatus.setPadding(10,10,10,10);

        tableRow.addView(tvType);
        tableRow.addView(tvDays);
        tableRow.addView(tvDate);
        tableRow.addView(tvStatus);

        tableRow.setBackgroundColor(Color.rgb(68,114,196));

        tlHistoryApplications.addView(tableRow,0);

        int c=0;
        for(final ReportClass report : reports){
            tableRow=new TableRow(this);
            tvType = new TextView(this);
            tvDays = new TextView(this);
            tvDate = new TextView(this);
            tvStatus = new TextView(this);

            tvDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isApplicant)
                        CustomApplicant.onClickOfUserApplicationDetails(report,userDetails.this);
                    else
                        ListOfReportsActivity.onClickOfReportItem(report,userDetails.this, null, true);
                }
            });

            tableRow.setLayoutParams(layoutParamsRow);
            tvType.setLayoutParams(layoutParams);
            tvDays.setLayoutParams(layoutParams);
            tvDate.setLayoutParams(layoutParams);
            tvStatus.setLayoutParams(layoutParams);

            tvType.setText(report.getDetail().getType());
            tvDays.setText(String.valueOf(util.getDaysCount(report.getDetail().getDurationStart(), report.getDetail().getDurationEnd())));
            tvDate.setText(util.getTimeString(report.getTimeOfArrival(),this));
            tvStatus.setText(Status.getStatusMsg(report.getStatus()));

            tvType.setTextSize(13);
            tvDays.setTextSize(13);
            tvDate.setTextSize(13);
            tvStatus.setTextSize(13);

            tvType.setPadding(10,10,10,10);
            tvDays.setPadding(10,10,10,10);
            tvDate.setPadding(10,10,10,10);
            tvStatus.setPadding(10,10,10,10);

            tvDate.setPaintFlags(tvDate.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);

            tableRow.addView(tvType);
            tableRow.addView(tvDays);
            tableRow.addView(tvDate);
            tableRow.addView(tvStatus);

            if((reports.size()-c-1)%2==0)
                tableRow.setBackgroundColor(Color.rgb(217,217,217));
            tlHistoryApplications.addView(tableRow,c+1);
            c++;
        }
    }

    private void setPieCharts() {
        llHoldingCharts.removeAllViews();

        LinearLayout.LayoutParams layoutParamstv = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        layoutParamstv.gravity = Gravity.CENTER_HORIZONTAL;
        layoutParamstv.setMargins(10,100,10,10);

        LinearLayout.LayoutParams layoutParamsIv = new LinearLayout.LayoutParams(
                (int)getResources().getDimension(R.dimen.dim_pie_char),
                (int)getResources().getDimension(R.dimen.dim_pie_char));

        layoutParamsIv.gravity = Gravity.CENTER_HORIZONTAL;
        layoutParamsIv.setMargins(10,10,10,10);

        for(int i=0 ; i<util.typeOfHolidays.size(); i++)
        {
            if(util.holidaysAlloted.get(new Pair<>(util.typeOfHolidays.get(i),curUser.isFemale()))==Integer.MAX_VALUE)
                continue;

            TextView tv = new TextView(this);
            tv.setLayoutParams(layoutParamstv);
            tv.setTextSize(17);
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            tv.setText(util.typeOfHolidays.get(i));

            float holidaysLeft = curUser.getHolidaysLeft(util.typeOfHolidays.get(i));
            float holidaysAlloted = util.holidaysAlloted.get(new Pair<>(util.typeOfHolidays.get(i),curUser.isFemale()));

            Bitmap img = getImage(
                    (holidaysAlloted-holidaysLeft)/holidaysAlloted,
                    getResources().getDimension(R.dimen.dim_pie_char),
                    getResources().getDimension(R.dimen.dim_pie_char));

            ImageView iv = new ImageView(this);
            iv.setLayoutParams(layoutParamsIv);
            iv.setImageBitmap(img);

            llHoldingCharts.addView(tv);
            llHoldingCharts.addView(iv);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        attachDatabaseReadListener();
        attachDatabaseApplicationReadListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        detachDatabaseReadListener();
        reports.clear();
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mUserDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }

        if(mApplicationsChildEventListener!=null){
            mUserApplicationsDatabaseReference.removeEventListener(mApplicationsChildEventListener);
            mApplicationsChildEventListener = null;
        }
    }

    private void attachDatabaseApplicationReadListener() {
        if (mApplicationsChildEventListener == null) {
            mApplicationsChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    try {
                        ReportClass report = dataSnapshot.getValue(ReportClass.class);
                        assert report != null;
                        report.setReportKey(dataSnapshot.getKey());
                        reports.add(report);
                        util.sort(reports);
                        setValues();
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    try {
                        ReportClass report = dataSnapshot.getValue(ReportClass.class);
                        if (report == null)
                            return;

                        for (int i = 0; i < reports.size(); i++)
                            if (reports.get(i).getReportKey().equals(dataSnapshot.getKey())) {
                                reports.remove(i);
                                break;
                            }

                        report.setReportKey(dataSnapshot.getKey());
                        reports.add(report);
                        util.sort(reports);
                        setValues();
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
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
            mUserApplicationsDatabaseReference.addChildEventListener(mApplicationsChildEventListener);
        }
    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Log.d("ApplicantForm", "got an Object");
                    try {
                        curUser = dataSnapshot.getValue(User.class);
                        setValues();
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    Log.d("ApplicantForm", "Got user info");
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Log.d("ApplicantFormOnChange", "got an Object");
                    try {
                        curUser = dataSnapshot.getValue(User.class);
                        setValues();
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    Log.d("ApplicantFormOnChange", "Got user info");
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
            mUserDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    Bitmap getImage(float per_occupied, float width, float height){
        int color1 = Color.rgb(237,125,49);
        int color2 = Color.rgb(91,155,213);

        float angleOcc = per_occupied*360;

        int margin=50;

        Bitmap imageBitmap = Bitmap.createBitmap((int)width ,
                (int) height,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageBitmap);
        float scale = getResources().getDisplayMetrics().density;
        Paint p = new Paint();
        p.setColor(color1);
        p.setTextSize(24*scale);
        canvas.drawArc(margin,margin,width-margin,height-margin-20,0,angleOcc,true,p);
        p.setColor(color2);
        canvas.drawArc(margin,margin,width-margin,height-margin-20,angleOcc,360-angleOcc,true,p);

        p.setColor(Color.WHITE);
        p.setStrokeWidth(10);
        if(width-margin!=width/2+(width/2-margin)*(float)Math.cos(angleOcc/180*Math.PI) ||
                height/2-10!=height/2-10+(width/2-margin)*(float)Math.sin(angleOcc/180*Math.PI))
        {
            canvas.drawLine(width/2,height/2-10,width-margin,height/2-10,p);
            canvas.drawLine(width/2,height/2-10,width/2+(width/2-margin)*(float)Math.cos(angleOcc/180*Math.PI),height/2-10+(width/2-margin)*(float)Math.sin(angleOcc/180*Math.PI),p);
            canvas.drawCircle(width/2,height/2-10,5,p);
        }

        Paint.Style temp = p.getStyle();
        p.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(width/2,height/2-10,width/2-margin-5,p);
        p.setStyle(temp);
        //p.setStrokeWidth(1);

        p.setTextSize(18*scale);

        p.setColor(color1);
        canvas.drawRect(0,height-margin+7,30,height-margin+30 +7,p);

        p.setColor(Color.BLACK);
        canvas.drawText("Leaves Taken",50,height-margin-10 + 18*scale,p);

        p.setColor(color2);
        canvas.drawRect(width/2,height-margin+7,width/2+30,height-margin+30 +7,p);

        p.setColor(Color.BLACK);
        canvas.drawText("Leaves Left",width/2+50,height-margin-10 + 18*scale,p);

        return imageBitmap;
    }

}
