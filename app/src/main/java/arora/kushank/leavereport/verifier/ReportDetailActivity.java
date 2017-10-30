package arora.kushank.leavereport.verifier;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import arora.kushank.leavereport.Attachment;
import arora.kushank.leavereport.R;
import arora.kushank.leavereport.ReportClass;
import arora.kushank.leavereport.User;
import arora.kushank.leavereport.ReportDetail;
import arora.kushank.leavereport.applicant.userDetails;
import arora.kushank.leavereport.util;

public class ReportDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String VC_UID = "IMMRRq6CW4P9XXJ6BWG25CrRsbk1";
//    private static final String DEF_REC_UID = "sAMWYHS2sIWP31fHFUETyYeAG852";
    private static final int MY_PERMISSIONS_REQUEST_READ_WRITE_STORAGE = 2;
    private static final String TAG = "ReportDetailActivity";

    private ImageView imageView;
    private TextView acceptTV;
    private TextView rejectTV;
    private LinearLayout containerTime;

    ReportClass curReport;
    private TextView subjectTV;
    private ListView listView;
    private EditText commentET;

    private Attachment attachment;
    private File def_Path;
    private Map<Integer, String> downloadNames;

    private ProgressBar pbDownload;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessagesDatabaseReference;
    private boolean isVC;
    private ArrayList<String> reportDetailList;
    private CustomListAdapterDetail customListAdapter;
    private TextView designationTV;
    private DatabaseReference mUserDatabaseReference;
    private ChildEventListener mChildEventListener;
    private User applicantUser;
    private LinearLayout llFileDownloadContainer;
    private ProgressDialog dialog;

    boolean forViewing;

    ArrayList<Long> daysExceed;
    private TextView etWarning;
    private DatabaseReference connectedRef;
    private ValueEventListener mChildOnlineOfflineEventListener;
    private boolean isOnline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        //w.setExitTransition(new Slide(Gravity.LEFT));
        //w.setSharedElementEnterTransition(new PathInterpolator(new FastOutSlowInInterpolator()));
        //w.setSharedElementEnterTransition(w.getSharedElementExitTransition());

        setContentView(R.layout.activity_report_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);

        def_Path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("leave_reports");
        connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");

        isVC = FirebaseAuth.getInstance().getCurrentUser().getUid().equals(VC_UID);

        pbDownload = (ProgressBar) findViewById(R.id.pbDownload);

        listView = ((ListView) findViewById(R.id.detailsLV));
        View header = getLayoutInflater().inflate(R.layout.header_list_view, null);
        header.setEnabled(false);
        View footer = getLayoutInflater().inflate(R.layout.footer_list_view, null);
        listView.addHeaderView(header);
        listView.addFooterView(footer);


        commentET = (EditText) findViewById(R.id.etComment);

        designationTV = (TextView) findViewById(R.id.tvDesignation);
        imageView = (ImageView) findViewById(R.id.image);
        acceptTV = (TextView) findViewById(R.id.acceptTV);
        rejectTV = (TextView) findViewById(R.id.rejectTV);
        subjectTV = (TextView) findViewById(R.id.subject);
        containerTime = (LinearLayout) findViewById(R.id.containerTime);
        llFileDownloadContainer = (LinearLayout) findViewById(R.id.llFileAttachedContainer);
        etWarning = (TextView) findViewById(R.id.tvWarning);
        //llFileDownloadContainer.setVisibility(View.GONE);

        reportDetailList = new ArrayList<>();
        customListAdapter = new CustomListAdapterDetail(this, reportDetailList);
        listView.setAdapter(customListAdapter);

        Bundle myBag = getIntent().getExtras();
        if (myBag == null) {
            curReport = new ReportClass("Subject", 0, new User(null, "Name", "Email", null, null,null), null, null, false, false);
        } else {
            forViewing = myBag.getBoolean(userDetails.KEY_BAG_FORVIEWING, false);
            if(forViewing) {
                Toast.makeText(this,"This window is just for viewing the application",Toast.LENGTH_SHORT).show();
                changeUIForBlocking();
            }

            attachment = new Attachment(myBag.getStringArrayList(ListOfReportsActivity.BAG_KEY_DETAILS_ATTACH_URL));

            ReportDetail gotDetails = new ReportDetail(myBag.getString(ListOfReportsActivity.BAG_KEY_DETAILS_TYPE),
                    myBag.getLong(ListOfReportsActivity.BAG_KEY_DETAILS_DUR_START),
                    myBag.getLong(ListOfReportsActivity.BAG_KEY_DETAILS_DUR_END),
                    myBag.getString(ListOfReportsActivity.BAG_KEY_DETAILS_ADD),
                    attachment);

            User sender = new User(null,
                    myBag.getString(ListOfReportsActivity.BAG_KEY_SENDER_NAME),
                    myBag.getString(ListOfReportsActivity.BAG_KEY_SENDER_EMAIL),
                    myBag.getString(ListOfReportsActivity.BAG_KEY_SENDER_UID),
                    myBag.getString(ListOfReportsActivity.BAG_KEY_SENDER_DESIGNATION),
                    new User(null,null,null,myBag.getString(ListOfReportsActivity.BAG_KEY_RECEIVER_UID),null,null));

            curReport = new ReportClass(myBag.getString(ListOfReportsActivity.BAG_KEY_SUBJECT),
                    myBag.getLong(ListOfReportsActivity.BAG_KEY_TIME),
                    sender,
                    null,
                    gotDetails,
                    false,
                    false);

            String repKey = myBag.getString(ListOfReportsActivity.BAG_KEY_REPORTKEY);
            curReport.setReportKey(repKey);

            int status = myBag.getInt(ListOfReportsActivity.BAG_KEY_STATUS);
            curReport.setStatus(status);

            String comment = myBag.getString(ListOfReportsActivity.BAG_KEY_COMMENT);
            curReport.setCommentChairman(comment);

            String commentVC = myBag.getString(ListOfReportsActivity.BAG_KEY_COMMENT_VC);
            curReport.setCommentVC(commentVC);


            long[] datesExceedArray =  myBag.getLongArray(ListOfReportsActivity.BAG_KEY_EXTRA_DATES_EXCEED);
            if(datesExceedArray!=null)
            {
                daysExceed = new ArrayList<>();
                for(long i:datesExceedArray)
                    daysExceed.add(i);
                etWarning.setVisibility(View.VISIBLE);
                etWarning.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        displayWarning();
                    }
                });
            }else {
                daysExceed = null;
                etWarning.setVisibility(View.GONE);
            }

            if (attachment.getURL() == null || attachment.getURL().size() == 0)
                llFileDownloadContainer.setVisibility(View.GONE);
            else {
                llFileDownloadContainer.setVisibility(View.VISIBLE);
                createDownloadsAndSetOnClickListener();
            }
        }

        mUserDatabaseReference = mFirebaseDatabase.getReference().child("users").child(curReport.getSender().getUser_id());
        mUserDatabaseReference.keepSynced(true);


        final LetterTileProvider tileProvider = new LetterTileProvider(this);
        final int tileSize = getResources().getDimensionPixelSize(R.dimen.letter_tile_size);
        final RoundedBitmapDrawable roundedBitmap = tileProvider.getLetterTile(curReport.getSender().getName(), curReport.getSender().getEmail_id(), tileSize, tileSize);
        imageView.setImageDrawable(roundedBitmap);
        acceptTV.setOnClickListener(this);
        rejectTV.setOnClickListener(this);
        imageView.setOnClickListener(this);

        findViewById(R.id.senderLayout).setOnClickListener(this);

        loadData();
        doInitBasedOnCurrentStatus();

        pbDownload.setVisibility(View.GONE);
        downloadNames = new HashMap<>();


        dialog = new ProgressDialog(ReportDetailActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setMax(100);
        dialog.setTitle("Downloading File...");
    }

    private void displayWarning() {
        String message="There are more leaves for ";
        for(int i=0; i<daysExceed.size(); i++) {
            if(i==daysExceed.size()-2)
                message += util.getDateString(daysExceed.get(i)) + " and ";
            else if(i==daysExceed.size()-1)
                message += util.getDateString(daysExceed.get(i));
            else
                message += util.getDateString(daysExceed.get(i)) + ", ";
        }
        new AlertDialog.Builder(this)
                .setTitle("Leaves Exceeded")
                .setMessage(message)
                .setNeutralButton("Close", null)
                .setNegativeButton("Details", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(ReportDetailActivity.this, LeavesCalendarActivity.class));
                    }
                })
                .show();
    }

    private void createDownloadsAndSetOnClickListener() {
        for (int i = 0; i < attachment.getURL().size(); i++) {
            TextView tv = new TextView(this);
            tv.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));

            tv.setBackground(getResources().getDrawable(R.drawable.illute_button));
            tv.setClickable(true);
            tv.setEnabled(true);
            tv.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_attach_file_black_24dp1), null, null, null);
            tv.setPadding((int) getResources().getDimension(R.dimen.paddingForm),
                    (int) getResources().getDimension(R.dimen.paddingForm),
                    (int) getResources().getDimension(R.dimen.paddingForm),
                    (int) getResources().getDimension(R.dimen.paddingForm));

            tv.setText("Download Attachment");

            tv.setTextColor(Color.rgb(0x66, 0x66, 0x66));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);

            final int finalI = i;
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    downloadFile(finalI);
                }
            });

            llFileDownloadContainer.addView(tv);
        }
    }

    private void doInitBasedOnCurrentStatus() {
        //if (!isVC) {
            switch (curReport.getStatus()) {
                case 0:
                case 1:
                case 5:
                    break;
                case 2:
                case 4:
                    if (!isVC) {
                        if (curReport.getCommentChairman() != null)
                            commentET.setText(curReport.getCommentChairman());
                        changeUIForAcceptance();
                    }
                    break;
                case 3:
                    if (!isVC) {
                        if (curReport.getCommentChairman() != null)
                            commentET.setText(curReport.getCommentChairman());
                        changeUIForRejection();
                    }
                    break;
                case 6:
                    if (!isVC)
                    {
                        if (curReport.getCommentVC() != null)
                            reportDetailList.add("VC's Comment: " + curReport.getCommentVC());
                        else
                            reportDetailList.add("No VC's Comments");
                        customListAdapter.notifyDataSetChanged();
                        if (curReport.getCommentChairman() != null)
                            commentET.setText(curReport.getCommentChairman());
                        changeUIForAcceptance();
                    }else{
                        if (curReport.getCommentChairman() != null)
                            commentET.setText(curReport.getCommentChairman());
                        changeUIForAcceptance();
                    }
                    break;
                case 7:
                    if(!isVC) {
                        if (curReport.getCommentVC() != null)
                            reportDetailList.add("VC's Comment: " + curReport.getCommentVC());
                        else
                            reportDetailList.add("No VC's Comments");
                        customListAdapter.notifyDataSetChanged();
                        if (curReport.getCommentChairman() != null)
                            commentET.setText(curReport.getCommentChairman());
                        changeUIForAcceptance();
                    }else{
                        if (curReport.getCommentChairman() != null)
                            commentET.setText(curReport.getCommentChairman());
                        changeUIForRejection();
                    }
                    break;
                default:
                    changeUIForBlocking();
                    break;
            }
        //}
        if(forViewing && curReport.getStatus()<=1)
            changeUIForBlocking();
    }

    private void loadData() {
        if (curReport.getSender().getDesignation() == null)
            designationTV.setText("Assistant Professor");
        else
            designationTV.setText(curReport.getSender().getDesignation());

        subjectTV.setText(curReport.getSubject());
        ((TextView) findViewById(R.id.sender)).setText(curReport.getSender().getName());

        CharSequence formattedString = util.getTimeString(curReport.getTimeOfArrival(), this);
        ((TextView) findViewById(R.id.time)).setText(formattedString);

        fillReportDetailList(reportDetailList);
        customListAdapter.notifyDataSetChanged();

        if (!util.canBeAcceptedByChairman(curReport.getDetail().getType()) && !isVC) {
            acceptTV.setText("Forward");
        } else
            acceptTV.setText("Accept");
    }

    private void fillReportDetailList(ArrayList<String> reportDetailList) {
        reportDetailList.add(curReport.getDetail().getType());

        String smallDetailString = "From ";
        smallDetailString += util.getDateString(curReport.getDetail().getDurationStart());
        smallDetailString += " To ";
        smallDetailString += util.getDateString(curReport.getDetail().getDurationEnd());
        reportDetailList.add(smallDetailString);

        reportDetailList.add(curReport.getDetail().getAddress());
        if (isVC) {
            if (curReport.getCommentChairman() != null)
                reportDetailList.add("Chairman's Comment" + curReport.getCommentChairman());
            else
                reportDetailList.add("No Chairman's Comment");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_report_detail_activity, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                //finishAfterTransition();
                finish();
                return true;
            case R.id.reportDelete:
                return true;
            case R.id.reportForward:
                Intent i=new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                String message=util.convertToString(curReport);
                i.putExtra(Intent.EXTRA_TEXT,message);
                startActivity(i);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private boolean ifPermissionNotGrantedAsk() {
        boolean got = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED;
        if (!got)
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_WRITE_STORAGE
            );
        return got;
    }

    void downloadFile(final int index) {
        boolean got = ifPermissionNotGrantedAsk();
        if (!got) {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            return;
        }

        if (downloadNames.containsKey(index)) {
            File downloadedFile = new File(def_Path, downloadNames.get(index));
            if (downloadedFile.exists()) {
                openFile(downloadedFile);
                return;
            }
        }

        if(!isOnline)
        {
            Toast.makeText(this, "Sorry! You are Offline!", Toast.LENGTH_SHORT).show();
            return;
        }

        dialog.show();
        dialog.setProgress(0);

        String downloadURL = curReport.getDetail().getAttachment().getURL(index);

        StorageReference islandRef = FirebaseStorage.getInstance().getReferenceFromUrl(downloadURL);
        String downloadName = islandRef.getName();
        Log.d(TAG, downloadName);

        File localFile = new File(def_Path, downloadName);
        String attachName = downloadName.substring(0, downloadName.indexOf("."));
        String attachExtension = downloadName.substring(downloadName.indexOf(".") + 1);

        int tries = 1;
        while (localFile.exists()) {
            localFile = new File(def_Path, attachName + " (" + tries + ")." + attachExtension);
            tries += 1;
        }

        final String finalLocalFileName = localFile.getName();
        final StorageTask<FileDownloadTask.TaskSnapshot> promise = islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(ReportDetailActivity.this, "File Downloaded!", Toast.LENGTH_SHORT).show();
                dialog.hide();
                downloadNames.put(index, finalLocalFileName);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(ReportDetailActivity.this, "Operation Failed! " + exception.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                dialog.hide();
            }
        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                dialog.setProgress((int) (taskSnapshot.getBytesTransferred() * 100 / taskSnapshot.getTotalByteCount()));
            }
        });

        final File finalLocalFile = localFile;
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                promise.cancel();
                finalLocalFile.delete();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if(!isOnline)
            Toast.makeText(this, "Sorry! You are Offline!", Toast.LENGTH_SHORT).show();
        switch (v.getId()) {
            case R.id.acceptTV:
                if (!rejectTV.isEnabled())
                    return;
                if(!isOnline)
                {
                    Toast.makeText(this, "Sorry! You are Offline!", Toast.LENGTH_SHORT).show();
                    return;
                }
                changeUIForAcceptance();
                pbDownload.setVisibility(View.VISIBLE);
                updateStatus(curReport.getReportKey(), curReport.getSender(), true);
                break;
            case R.id.rejectTV:
                if (!acceptTV.isEnabled())
                    return;
                if(!isOnline)
                {
                    Toast.makeText(this, "Sorry! You are Offline!", Toast.LENGTH_SHORT).show();
                    return;
                }
                changeUIForRejection();
                pbDownload.setVisibility(View.VISIBLE);
                updateStatus(curReport.getReportKey(), curReport.getSender(), false);
                break;
            case R.id.senderLayout:
                /*String old=timeTV.getText().toString();
                Calendar cal=Calendar.getInstance();
                cal.setTimeInMillis(curReport.getTimeOfArrival());
                if(old.length()<10)
                    timeTV.setText("Date: "+ cal.getTime().toLocaleString()
                            +"\n"
                            +"Last Leave: 23 Jan 2017"
                    );
                else
                    timeTV.setText(CustomListAdapter.getTimeString(cal.getTimeInMillis()));
                */
                int numViews = containerTime.getChildCount();
                if (numViews == 1) {
                    containerTime.removeAllViews();
                    addDetailViewTimeContainer(curReport.getTimeOfArrival(), containerTime);
                } else {
                    containerTime.removeAllViews();
                    addSingleViewTimeContainer(curReport.getTimeOfArrival(), containerTime);
                }
                return;
            case R.id.image:
                loadUserDetails(curReport.getSender().getUser_id());
                break;
        }
        //Toast.makeText(this, "Operation Successful", Toast.LENGTH_SHORT).show();
    }

    private void loadUserDetails(String user_id) {
        Bundle extras = new Bundle();
        extras.putString(userDetails.KEY_BAG_USER_ID, user_id);
        extras.putBoolean(userDetails.KEY_BAG_APPLICANT, false);

        Intent i = new Intent(ReportDetailActivity.this, userDetails.class);
        i.putExtras(extras);

        startActivity(i);
    }

    private void changeUIForRejection() {
        acceptTV.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_accept_disable, 0, 0);
        acceptTV.setTextColor(Color.rgb(0xaa, 0xaa, 0xaa));
        acceptTV.setEnabled(false);
        rejectTV.setClickable(false);
        commentET.setEnabled(false);
    }

    private void changeUIForAcceptance() {
        rejectTV.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_reject_disable, 0, 0);
        rejectTV.setTextColor(Color.rgb(0xaa, 0xaa, 0xaa));
        rejectTV.setEnabled(false);
        acceptTV.setClickable(false);
        commentET.setEnabled(false);
    }

    private void changeUIForBlocking() {
        rejectTV.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_reject_disable, 0, 0);
        rejectTV.setTextColor(Color.rgb(0xaa, 0xaa, 0xaa));
        rejectTV.setEnabled(false);
        rejectTV.setClickable(false);

        acceptTV.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_accept_disable, 0, 0);
        acceptTV.setTextColor(Color.rgb(0xaa, 0xaa, 0xaa));
        acceptTV.setEnabled(false);
        acceptTV.setClickable(false);

        commentET.setEnabled(false);

    }


    private void changeUIForNeutral() {
        rejectTV.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_reject, 0, 0);
        rejectTV.setTextColor(Color.rgb(0x66, 0x66, 0x66));
        rejectTV.setEnabled(true);
        rejectTV.setClickable(true);

        acceptTV.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_accept, 0, 0);
        acceptTV.setTextColor(Color.rgb(0x66, 0x66, 0x66));
        acceptTV.setEnabled(true);
        acceptTV.setClickable(true);

        commentET.setEnabled(true);
    }


    private void updateStatus(final String reportKey, final User sender, boolean accepted) {
        if (isVC) {
            if (accepted) {
                mMessagesDatabaseReference.child(sender.getUser_id()).child(reportKey).child("status").setValue(6)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    updateLeaves(true);
                                    Toast.makeText(ReportDetailActivity.this, "Application Accepted!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ReportDetailActivity.this, "Task Unsuccessful!", Toast.LENGTH_SHORT).show();
                                    changeUIForNeutral();
                                }
                                pbDownload.setVisibility(View.GONE);
                            }
                        });
            } else {
                mMessagesDatabaseReference.child(sender.getUser_id()).child(reportKey).child("status").setValue(7);
                updateLeaves(false);
            }
            mMessagesDatabaseReference.child(sender.getUser_id()).child(reportKey).child("receiver").child("user_id").setValue(sender.getReportingTo().getUser_id());
            if (commentET.getText() != null)
                mMessagesDatabaseReference.child(sender.getUser_id()).child(reportKey).child("commentVC").setValue(commentET.getText().toString());
            mMessagesDatabaseReference.child(sender.getUser_id()).child(reportKey).child("readChairman").setValue(false);

        } else {
            if (accepted) {
                if (!util.canBeAcceptedByChairman(curReport.getDetail().getType())) {
                    mMessagesDatabaseReference.child(sender.getUser_id()).child(reportKey).child("status").setValue(2)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ReportDetailActivity.this, "Status Updating Successful!", Toast.LENGTH_SHORT).show();
                                        mMessagesDatabaseReference.child(sender.getUser_id()).child(reportKey).child("receiver").child("user_id").setValue(VC_UID)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            mMessagesDatabaseReference.child(sender.getUser_id()).child(reportKey).child("copyTo").setValue(new User(null,null,null,FirebaseAuth.getInstance().getCurrentUser().getUid(),null,null));
                                                            Toast.makeText(ReportDetailActivity.this, "Form Forwarded!", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(ReportDetailActivity.this, "Form Forwarding Unsuccessful!", Toast.LENGTH_SHORT).show();
                                                            changeUIForNeutral();
                                                        }
                                                        pbDownload.setVisibility(View.GONE);
                                                    }
                                                });
                                    } else {
                                        changeUIForNeutral();
                                        Toast.makeText(ReportDetailActivity.this, "Status Updation UnSuccessful!", Toast.LENGTH_SHORT).show();
                                        pbDownload.setVisibility(View.GONE);
                                    }
                                }
                            });
                } else {
                    mMessagesDatabaseReference.child(sender.getUser_id()).child(reportKey).child("status").setValue(4)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ReportDetailActivity.this, "Form Accepted Successfully!", Toast.LENGTH_SHORT).show();
                                        updateLeaves(true);
                                    } else {
                                        changeUIForNeutral();
                                        Toast.makeText(ReportDetailActivity.this, "Form Acceptance Failed!", Toast.LENGTH_SHORT).show();
                                    }
                                    pbDownload.setVisibility(View.GONE);
                                }
                            });
                }

            } else {
                mMessagesDatabaseReference
                        .child(sender.getUser_id())
                        .child(reportKey)
                        .child("status")
                        .setValue(3)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    updateLeaves(false);
                                } else {
                                    pbDownload.setVisibility(View.GONE);
                                    changeUIForNeutral();
                                }
                            }
                        });
            }
            if (commentET.getText() != null)
                mMessagesDatabaseReference.child(sender.getUser_id()).child(reportKey).child("commentChairman").setValue(commentET.getText().toString());
        }
    }

    private void updateLeaves(boolean accepted) {
        String typeLeave = curReport.getDetail().getType();
        int alreadyLeaves = applicantUser.getHolidaysLeft(typeLeave);
        int pendingLeaves = applicantUser.getHolidayPending(typeLeave);
        int leavesTried = (int) util.getDaysCount(curReport.getDetail().getDurationStart(), curReport.getDetail().getDurationEnd());

        if(accepted)
            applicantUser.setHolidayfor(curReport.getDetail().getType(), alreadyLeaves - leavesTried);
        applicantUser.setHolidayPendingFor(curReport.getDetail().getType(), pendingLeaves - leavesTried);
        mUserDatabaseReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mUserDatabaseReference.push().setValue(applicantUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                                Toast.makeText(ReportDetailActivity.this, "Days Updated in database successfully!", Toast.LENGTH_SHORT).show();
                            else {
                                Toast.makeText(ReportDetailActivity.this, "There is an Error! Please Report it to the admin immediately!", Toast.LENGTH_SHORT).show();
                                changeUIForNeutral();
                            }
                            pbDownload.setVisibility(View.GONE);
                        }
                    });
                } else {
                    Toast.makeText(ReportDetailActivity.this, "There is an Error! Please Report it to the admin immediately!", Toast.LENGTH_SHORT).show();
                    changeUIForNeutral();
                    pbDownload.setVisibility(View.GONE);
                }
            }
        });
    }

    private void openFile(File file) {
        Uri path = Uri.fromFile(file);

        //Get File Extension
        String parts[] = path.toString().replace('.', '/').split("/");
        String exx = parts[parts.length - 1];

        //Get File MIME type
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(exx);

        //Open the file
        Intent fileOpenIntent = new Intent(Intent.ACTION_VIEW);
        fileOpenIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        fileOpenIntent.setDataAndType(path, type);
        startActivity(fileOpenIntent);
    }

    private void addSingleViewTimeContainer(long timeOfArrival, LinearLayout containerTime) {
        TextView timeTV = new TextView(this, null);
        timeTV.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        timeTV.setText(util.getTimeString(timeOfArrival, this));
        containerTime.addView(timeTV);
    }

    private void addDetailViewTimeContainer(long timeOfArrival, LinearLayout containerTime) {

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeOfArrival);

        String[] arg = new String[2];
        String[] value = new String[arg.length];
        arg[0] = "Date:";
        value[0] = DateFormat.getDateInstance(DateFormat.FULL).format(cal.getTime());
        arg[1] = "Time:";
        value[1] = DateFormat.getTimeInstance().format(cal.getTime());
        //arg[2] = "How Are:";
        //value[2] = "Khana Khakr Jana Haan?";
        for (int i = 0; i < arg.length; i++) {
            LinearLayout mainLinearLayout = new LinearLayout(this, null);
            mainLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            mainLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

            TextView tvHead = new TextView(this, null);
            tvHead.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.20f));
            tvHead.setText(arg[i]);
            mainLinearLayout.addView(tvHead);

            TextView tvValue = new TextView(this, null);
            tvValue.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.80f));
            tvValue.setText(value[i]);
            mainLinearLayout.addView(tvValue);

            containerTime.addView(mainLinearLayout);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        attachDatabaseReadListener();
        attackOnlineOfflineReadListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        detachDatabaseReadListener();
        detachOnlineOfflineReadListener();
    }


    private void detachOnlineOfflineReadListener() {
        if (mChildOnlineOfflineEventListener != null) {
            connectedRef.removeEventListener(mChildOnlineOfflineEventListener);
            mChildOnlineOfflineEventListener = null;
        }
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mUserDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    private void attackOnlineOfflineReadListener() {
        if (mChildOnlineOfflineEventListener == null) {
            mChildOnlineOfflineEventListener  = new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    try {
                        isOnline = snapshot.getValue(Boolean.class);
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    if (isOnline) {
                        System.out.println("connected");
                    } else {
                        System.out.println("not connected");
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    System.err.println("Listener was cancelled");
                }
            };
        }
        connectedRef.addValueEventListener(mChildOnlineOfflineEventListener);
    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Log.d("ApplicantForm", "got an Object");
                    try {
                        applicantUser = dataSnapshot.getValue(User.class);
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
                        applicantUser = dataSnapshot.getValue(User.class);
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
}
