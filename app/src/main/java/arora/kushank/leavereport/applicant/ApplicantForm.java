package arora.kushank.leavereport.applicant;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import arora.kushank.leavereport.Attachment;
import arora.kushank.leavereport.R;
import arora.kushank.leavereport.ReportDetail;
import arora.kushank.leavereport.util;
import arora.kushank.leavereport.ReportClass;
import arora.kushank.leavereport.User;

public class ApplicantForm extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener {

    private static final int GOT_FILE = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_WRITE_STORAGE = 2;
    boolean date1sel, date2sel;
    int mYear, mMonth, mDay;
    long mTime1, mTime2;
//    String DEF_REC_UID = "sAMWYHS2sIWP31fHFUETyYeAG852";

    EditText etTimeFrom, etTimeTo, etSubject, etTempAdd;
    Spinner spTOL;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessagesDatabaseReference;
    private ProgressBar pbForm;

    private LinearLayout fileAttachedContainer;

    ArrayList<Uri> fileUploads;

    private DatabaseReference mUserDatabaseReference;
    private ChildEventListener mChildEventListener;
    private FirebaseAuth firebaseAuth;

    User curUser;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applicant_form);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "UnAuthorized Access", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etSubject = (EditText) findViewById(R.id.etForm);
        etTempAdd = (EditText) findViewById(R.id.etAddress);
        etTimeFrom = (EditText) findViewById(R.id.etDateFrom);
        etTimeTo = (EditText) findViewById(R.id.etDateTo);
        spTOL = (Spinner) findViewById(R.id.spSelectTOL);
        pbForm = (ProgressBar) findViewById(R.id.pbForm);
        fileAttachedContainer = (LinearLayout) findViewById(R.id.llFileAttachedContainer);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("leave_reports").child(firebaseUser.getUid());
        mUserDatabaseReference = mFirebaseDatabase.getReference().child("users").child(firebaseUser.getUid());
        mUserDatabaseReference.keepSynced(true);

        etTimeFrom.setOnClickListener(this);
        etTimeTo.setOnClickListener(this);
        etTimeFrom.setOnFocusChangeListener(this);
        etTimeTo.setOnFocusChangeListener(this);
        date1sel = date2sel = false;


        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
        for (String holiday : util.typeOfHolidays) {
            adapter.add(holiday);
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spTOL.setAdapter(adapter);
        pbForm.setVisibility(View.INVISIBLE);
        fileAttachedContainer.setVisibility(View.GONE);
        mChildEventListener = null;
        curUser = null;
        fileUploads = new ArrayList<>();

        dialog = new ProgressDialog(ApplicantForm.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setMax(100);
        dialog.setTitle("Uploading File...");

    }

    @Override
    public void onClick(final View v) {
        Calendar mcurrentDate = Calendar.getInstance();
        if (v.getId() == R.id.etDateTo)
            if (date2sel)
                mcurrentDate.setTimeInMillis(mTime2);
            else if (date1sel)
                mcurrentDate.setTimeInMillis(mTime1);

        if (v.getId() == R.id.etDateFrom)
            if (date1sel)
                mcurrentDate.setTimeInMillis(mTime1);
            else if (date2sel)
                mcurrentDate.setTimeInMillis(mTime2);

        mYear = mcurrentDate.get(Calendar.YEAR);
        mMonth = mcurrentDate.get(Calendar.MONTH);
        mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog mDatePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                Calendar myCalendar = Calendar.getInstance();
                myCalendar.setTimeInMillis(0);
                myCalendar.set(Calendar.YEAR, selectedyear);
                myCalendar.set(Calendar.MONTH, selectedmonth);
                myCalendar.set(Calendar.DAY_OF_MONTH, selectedday);
                String myFormat = "dd/MM/yy"; //Change as you need
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ENGLISH);
                ((EditText) v).setText(sdf.format(myCalendar.getTime()));

                mDay = selectedday;
                mMonth = selectedmonth;
                mYear = selectedyear;
                if (v.getId() == R.id.etDateFrom) {
                    date1sel = true;
                    mTime1 = myCalendar.getTimeInMillis();
                } else {
                    date2sel = true;
                    mTime2 = myCalendar.getTimeInMillis();
                }

                if(date1sel && date2sel){

                    boolean isValid = isDatesConsistentWithDB(mTime1,mTime2,spTOL.getSelectedItem().toString());
                    if(!isValid && curUser!=null ) {
                        final int numDays = (int) util.getDaysCount(mTime1, mTime2);
                        final int numDaysLeft = curUser.getHolidaysLeft(spTOL.getSelectedItem().toString()) - curUser.getHolidayPending(spTOL.getSelectedItem().toString());

                        new AlertDialog.Builder(ApplicantForm.this)
                                .setTitle("Days Exceeded")
                                .setMessage("Sorry! You are permitted "+numDaysLeft+" days, but you are trying to have "+numDays+" days leave!")
                                .setNeutralButton("Close", null)
                                .show();
                        ((EditText)v).setText("");
                        if (v.getId() == R.id.etDateFrom) {
                            date1sel = false;
                            mTime1 = 0;
                        } else {
                            date2sel = false;
                            mTime2 = 0;
                        }
                    }
                }
            }
        }, mYear, mMonth, mDay);
        mDatePicker.setTitle("Select date");
        if (date1sel && v.getId() == R.id.etDateTo) {
            mDatePicker.getDatePicker().setMinDate(mTime1);
        }
        if (date2sel && v.getId() == R.id.etDateFrom) {
            mDatePicker.getDatePicker().setMaxDate(mTime2);
        }
        mDatePicker.getDatePicker().setCalendarViewShown(true);
        mDatePicker.show();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v.getId() == R.id.etDateFrom || v.getId() == R.id.etDateTo)
            if (hasFocus)
                onClick(v);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_applicant_form, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.sendForm:
                if(!isFormFilled())
                    return true;
                pbForm.setVisibility(View.VISIBLE);
                item.setEnabled(false);
                hideKeyboard();

                boolean isValid = isDatesConsistentWithDB(mTime1,mTime2,spTOL.getSelectedItem().toString());

                if(!isValid) {
                    pbForm.setVisibility(View.GONE);
                    item.setEnabled(true);
                    return true;
                }

                if(curUser.getReportingTo()==null)
                {
                    Toast.makeText(this,"Please Update your profile with Reporting person!",Toast.LENGTH_SHORT).show();
                    Toast.makeText(this,"Form Discarded!",Toast.LENGTH_SHORT).show();
                    pbForm.setVisibility(View.GONE);
                    item.setEnabled(true);
                    return true;
                }

                final int numDays = (int) util.getDaysCount(mTime1, mTime2);
                final int numPending = curUser.getHolidayPending(spTOL.getSelectedItem().toString());
                //final int numDaysLeft = curUser.getHolidaysLeft(spTOL.getSelectedItem().toString()) - curUser.getHolidayPending(spTOL.getSelectedItem().toString());


                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                User user = new User(null, firebaseUser.getDisplayName(), firebaseUser.getEmail(), firebaseUser.getUid(), curUser.getDesignation(),curUser.getReportingTo());
                User receiver = new User(null, null, null, curUser.getReportingTo().getUser_id(), null,null);

                Attachment attachment = new Attachment();
                for(Uri file : fileUploads)
                    attachment.addURL(file.toString());

                ReportDetail reportDetail = new ReportDetail(spTOL.getSelectedItem().toString(), mTime1, mTime2, etTempAdd.getText().toString(), attachment);
                final ReportClass report = new ReportClass(etSubject.getText().toString(), Calendar.getInstance().getTimeInMillis(), user, receiver, reportDetail, false, false);
                report.setStatus(0);

                mMessagesDatabaseReference.push().setValue(report).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ApplicantForm.this, "Form Sent Successfully", Toast.LENGTH_SHORT).show();
                            curUser.setHolidayPendingFor(report.getDetail().getType(), numPending + numDays);
                            mUserDatabaseReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    mUserDatabaseReference.push().setValue(curUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(ApplicantForm.this, "Days Updated in database successfully!", Toast.LENGTH_SHORT).show();
                                            pbForm.setVisibility(View.GONE);
                                            item.setEnabled(true);
                                            finish();
                                        }
                                    });
                                }
                            });

                        } else {
                            Toast.makeText(ApplicantForm.this, "Unsuccessful Termination! " + task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            pbForm.setVisibility(View.GONE);
                            item.setEnabled(true);
                        }
                    }
                });
                return true;
            case R.id.discard:
                break;
            case R.id.attachFile:
                boolean granted = ifPermissionNotGrantedAsk();
                if (!granted) {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    return true;
                }
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("*/*");
                startActivityForResult(i, GOT_FILE);
                return true;
            default:
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isFormFilled() {
        if(etSubject.getText().toString().trim().equals("")) {
            Toast.makeText(this, "It is required", Toast.LENGTH_SHORT).show();
            return false;
        }else if(etTimeFrom.getText().toString().trim().equals("")) {
            Toast.makeText(this, "It is required", Toast.LENGTH_SHORT).show();
            return false;
        }else if(etTimeTo.getText().toString().trim().equals("")) {
            Toast.makeText(this, "It is required", Toast.LENGTH_SHORT).show();
            return false;
        }else if(etTempAdd.getText().toString().trim().equals("")) {
            Toast.makeText(this, "It is required", Toast.LENGTH_SHORT).show();
            return false;
        }else
            return true;
    }

    private boolean isDatesConsistentWithDB(long mTime1, long mTime2, String typeOfLeave) {
        if (curUser == null) {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            return false;
        }
        final int numDays = (int) util.getDaysCount(mTime1, mTime2);
        final int numDaysLeft = curUser.getHolidaysLeft(typeOfLeave)-curUser.getHolidayPending(typeOfLeave);
        if (numDays > numDaysLeft) {
            Toast.makeText(this, "No. of days permitted exceeded!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (getCurrentFocus() != null)
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOT_FILE) {
            if (resultCode == RESULT_OK) {
                Uri gotDataIncorrect = data.getData();
                String filepath = gotDataIncorrect.getPath();
                filepath=modifyIfInvalid(this,filepath,gotDataIncorrect);

                String filename = filepath.substring(filepath.lastIndexOf("/")+1);
                final Uri gotData = Uri.fromFile(new File(filepath));

                StorageReference photoref = FirebaseStorage.getInstance().getReference().child(filename);
                dialog.show();
                final StorageTask<UploadTask.TaskSnapshot> promise = photoref.putFile(gotData).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        fileUploads.add(downloadUrl);

                        dialog.hide();

                        fileAttachedContainer.setVisibility(View.VISIBLE);
                        fileAttachedContainer.addView(createFileAttachedView(gotData.getLastPathSegment()));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ApplicantForm.this, "Upload Failed", Toast.LENGTH_SHORT).show();
                        dialog.hide();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        dialog.setProgress((int) (taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount()));
                    }
                });
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        promise.cancel();
                    }
                });
            }
        }
    }

    private View createFileAttachedView(String lastPathSegment) {
        LinearLayout ll = new LinearLayout(this);
        ll.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        ll.setOrientation(LinearLayout.HORIZONTAL);

        TextView label = new TextView(this);
        label.setLayoutParams(new LinearLayout.LayoutParams(
                (int)getResources().getDimension(R.dimen.labelWidthForm),
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        label.setPadding((int)getResources().getDimension(R.dimen.paddingForm),
                (int)getResources().getDimension(R.dimen.paddingForm),
                (int)getResources().getDimension(R.dimen.paddingForm),
                (int)getResources().getDimension(R.dimen.paddingForm));
        label.setText(getResources().getString(R.string.file_attached));

        TextView content = new TextView(this);
        content.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        content.setPadding((int)getResources().getDimension(R.dimen.paddingForm),
                (int)getResources().getDimension(R.dimen.paddingForm),
                (int)getResources().getDimension(R.dimen.paddingForm),
                (int)getResources().getDimension(R.dimen.paddingForm));
        content.setTextColor(Color.BLACK);
        content.setText(lastPathSegment);

        View view = new View(this);
        view.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int)getResources().getDimension(R.dimen.margin_width)
        ));
        view.setBackgroundColor(0xdddddd);

        ll.addView(label);
        ll.addView(content);
        ll.addView(view);

        return ll;
    }

    public static String modifyIfInvalid(Context context, String filename, Uri dataURI) {
        boolean gotError = false;
        try {
            File myFile = new File(filename);
            Log.d("AForm", myFile.length() + "");
            if (myFile.length() == 0)
                gotError = true;

            FileInputStream fis = new FileInputStream(filename);
            fis.close();
        } catch (Exception ex) {
            gotError = true;
        }

        if (gotError) {
            Log.d("AForm", "gotError");
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = context.getContentResolver().query(dataURI, filePathColumn, null, null, null);
            Log.d("AForm", "Trying to read it as image");
            if (cursor == null) {
                filePathColumn[0] = MediaStore.Video.Media.DATA;
                cursor = context.getContentResolver().query(dataURI, filePathColumn, null, null, null);
                Log.d("AForm", "Trying to read it as video");
            }
            if (cursor == null) {
                filePathColumn[0] = MediaStore.Audio.Media.DATA;
                cursor = context.getContentResolver().query(dataURI, filePathColumn, null, null, null);
                Log.d("AForm", "Trying to read it as audio");
            }
            if (cursor == null) {
                filePathColumn[0] = MediaStore.Files.FileColumns.DATA;
                cursor = context.getContentResolver().query(dataURI, filePathColumn, null, null, null);
                Log.d("AForm", "Trying to read it as file");
            }
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                filename = cursor.getString(columnIndex);
                cursor.close();
            }
        }
        return filename;
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
            mUserDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
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
