package arora.kushank.leavereport.applicant;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import arora.kushank.leavereport.Attachment;
import arora.kushank.leavereport.R;
import arora.kushank.leavereport.ReportDetail;
import arora.kushank.leavereport.Status;
import arora.kushank.leavereport.util;
import arora.kushank.leavereport.ReportClass;

public class ApplicantFormDetailsActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_WRITE_STORAGE = 2;
    TextView tvTOL,tvStatus, tvComment;
    LinearLayout llFileAttachedContainer;
    EditText etSubject,etFrom,etTo,etTAdd;
    private ReportClass myObj;

    Map<Integer,String> downloadName;
    private File def_Path;
    ProgressBar pbDownload;
    private DatabaseReference mMessagesDatabaseReference;
    private ChildEventListener mChildEventListener;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applicant_form_details);
        initViews();

        Bundle myBag = getIntent().getExtras();
        Attachment attachment = new Attachment();
        if(myBag.getStringArrayList(CustomApplicant.KEY_BAG_ATTACH_URL)==null)
            attachment=null;
        else {
            attachment.setURL(myBag.getStringArrayList(CustomApplicant.KEY_BAG_ATTACH_URL));
        }

        ReportDetail detail = new ReportDetail();
        detail.setAddress(myBag.getString(CustomApplicant.KEY_BAG_TADD));
        detail.setDurationStart(myBag.getLong(CustomApplicant.KEY_BAG_FROM));
        detail.setDurationEnd(myBag.getLong(CustomApplicant.KEY_BAG_TO));
        detail.setType(myBag.getString(CustomApplicant.KEY_BAG_TOL));
        detail.setAttachment(attachment);


        myObj = new ReportClass();
        myObj.setCommentChairman(myBag.getString(CustomApplicant.KEY_BAG_COMMENT_CHAIRMAN));
        myObj.setCommentVC(myBag.getString(CustomApplicant.KEY_BAG_COMMENT_VC));
        myObj.setStatus(myBag.getInt(CustomApplicant.KEY_BAG_STATUS));
        myObj.setSubject(myBag.getString(CustomApplicant.KEY_BAG_SUBJECT));
        myObj.setReportKey(myBag.getString(CustomApplicant.KEY_BAG_REPORT_KEY));
        myObj.setDetail(detail);

        if(myObj.getDetail().getAttachment()==null || myObj.getDetail().getAttachment().getURL()==null ||myObj.getDetail().getAttachment().getURL().size()==0){
            llFileAttachedContainer.setVisibility(View.GONE);
        }

        setValues();

        downloadName=new HashMap<>();
        try {
            def_Path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        }catch(Exception e){
            Toast.makeText(this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
            Log.d("AFDA",e.getLocalizedMessage());
        }

        mMessagesDatabaseReference = FirebaseDatabase.getInstance().getReference().child("leave_reports").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mMessagesDatabaseReference.keepSynced(true);

        dialog = new ProgressDialog(ApplicantFormDetailsActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setMax(100);
        dialog.setTitle("Downloading File...");

    }

    private void setValues() {
        tvTOL.setText(myObj.getDetail().getType());
        tvStatus.setText(Status.getStatusMsg(myObj.getStatus()));
        String comment = "";
        if(myObj.getCommentChairman()!=null && !myObj.getCommentChairman().trim().equals(""))
            comment += "Chairman: "+myObj.getCommentChairman().trim();

        if(myObj.getCommentVC()!=null && !myObj.getCommentVC().trim().equals(""))
            comment += "VC: "+myObj.getCommentVC().trim();

        if(comment.trim().equals(""))
            tvComment.setText("No Comments");
        else
            tvComment.setText(comment);


        tvTOL.setFocusable(true);
        tvTOL.setFocusableInTouchMode(true);

        etSubject.setText(myObj.getSubject());
        etFrom.setText(util.getDateString(myObj.getDetail().getDurationStart()));
        etTo.setText(util.getDateString(myObj.getDetail().getDurationEnd()));
        etTAdd.setText(myObj.getDetail().getAddress());

        llFileAttachedContainer.removeAllViews();

        TextView tv = new TextView(this);
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        tv.setText("Attachment");
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,15);

        llFileAttachedContainer.addView(tv);

        if(myObj.getDetail().getAttachment()!=null && myObj.getDetail().getAttachment().getURL()!=null)
            for(int i=0; i<myObj.getDetail().getAttachment().getURL().size(); i++)
            {
                llFileAttachedContainer.addView(createTextViewForDownload(i));
            }
    }

    private View createTextViewForDownload(final int index) {
        TextView tv = new TextView(this);
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        tv.setPadding((int)getResources().getDimension(R.dimen.paddingForm),
                (int)getResources().getDimension(R.dimen.paddingForm),
                (int)getResources().getDimension(R.dimen.paddingForm),
                (int)getResources().getDimension(R.dimen.paddingForm));
        tv.setText("Download");
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadFile(index);
            }
        });

        return tv;
    }


    private boolean ifPermissionNotGrantedAsk() {
        boolean got = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED ;
        if(!got)
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_WRITE_STORAGE
            );
        return got;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for(int res : grantResults)
            if(res!=PackageManager.PERMISSION_GRANTED)
                return;
        def_Path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }

    private void openFile(File file) {
        Uri path= Uri.fromFile(file);

        //Get File Extension
        String parts[] = path.toString().replace('.','/').split("/");
        String exx=parts[parts.length-1];

        //Get File MIME type
        String type= MimeTypeMap.getSingleton().getMimeTypeFromExtension(exx);

        //Open the file
        Intent fileOpenIntent=new Intent(Intent.ACTION_VIEW);
        fileOpenIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        fileOpenIntent.setDataAndType(path,type);
        startActivity(fileOpenIntent);
    }


    private void downloadFile(final int index) {
        boolean got = ifPermissionNotGrantedAsk();
        if(!got) {
            Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show();
            return;
        }

        if(downloadName.containsKey(index)){
            File downloadedFile=new File(def_Path, downloadName.get(index));
            if(downloadedFile.exists()) {
                openFile(downloadedFile);
                return;
            }
        }

        dialog.show();

        String attachmentURL = myObj.getDetail().getAttachment().getURL(index);

        StorageReference islandRef = FirebaseStorage.getInstance().getReferenceFromUrl(attachmentURL);
        String attachmentName = islandRef.getName();

        File localFile = new File(def_Path, attachmentName);
        String attachName = attachmentName.substring(0, attachmentName.indexOf("."));
        String attachExtension = attachmentName.substring(attachmentName.indexOf(".") + 1);

        int tries = 1;
        while (localFile.exists()) {
            localFile = new File(def_Path, attachName + " (" + tries + ")." + attachExtension);
            tries += 1;
        }

        final String finalLocalFileName = localFile.getName();
        final StorageTask<FileDownloadTask.TaskSnapshot> promise = islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(ApplicantFormDetailsActivity.this, "File Downloaded!", Toast.LENGTH_SHORT).show();
                dialog.hide();
                downloadName.put(index, finalLocalFileName);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(ApplicantFormDetailsActivity.this, "Operation Failed! " + exception.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                dialog.hide();
            }
        }).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                pbDownload.setVisibility(View.GONE);
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

    private void initViews() {
        tvTOL = (TextView) findViewById(R.id.tvTOL);
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        tvComment = (TextView) findViewById(R.id.tvComment);

        etSubject = (EditText) findViewById(R.id.etSubject);
        etFrom = (EditText) findViewById(R.id.etFrom);
        etTo = (EditText) findViewById(R.id.etTo);
        etTAdd = (EditText) findViewById(R.id.etTAdd);

        pbDownload = (ProgressBar) findViewById(R.id.pbDownload);

        llFileAttachedContainer = (LinearLayout) findViewById(R.id.llFileAttachedContainer);
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
                    Log.d("ApplicantFormDetails", "onChildAdded");
                    try {
                        if (dataSnapshot.getKey().equals(myObj.getReportKey())) {
                            myObj = (dataSnapshot.getValue(ReportClass.class));
                            myObj.setReportKey(dataSnapshot.getKey());
                            setValues();
                            Log.d("ApplicantFormDetails", "onChildAddedMyObj");
                        }
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Log.d("ApplicantFormDetails", "onChildChanged");
                    //Log.d("ApplicantFormDetails", dataSnapshot.getKey());
                    try {
                        if (dataSnapshot.getKey().equals(myObj.getReportKey())) {
                            myObj = (dataSnapshot.getValue(ReportClass.class));
                            myObj.setReportKey(dataSnapshot.getKey());
                            setValues();
                            Log.d("ApplicantFormDetails", "onChildChangedMyObj");
                        }
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
            mMessagesDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }
}
