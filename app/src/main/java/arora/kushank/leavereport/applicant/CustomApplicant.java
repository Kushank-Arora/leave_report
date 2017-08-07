package arora.kushank.leavereport.applicant;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import arora.kushank.leavereport.R;
import arora.kushank.leavereport.util;
import arora.kushank.leavereport.verifier.LetterTileProvider;
import arora.kushank.leavereport.ReportClass;
import arora.kushank.leavereport.User;

/**
 * Created by Kushank on 12-07-2017.
 */
public class CustomApplicant extends RecyclerView.Adapter {

    public static final String KEY_BAG_TOL = "BTOL";
    public static final String KEY_BAG_FROM = "BFROM";
    public static final String KEY_BAG_TO = "BTO";
    public static final String KEY_BAG_SUBJECT = "BSUBJECT";
    public static final String KEY_BAG_STATUS = "BSTATUS";
    public static final String KEY_BAG_TADD = "BTADD";
    public static final String KEY_BAG_ATTACH_URL = "BATTACHURL";
    public static final String KEY_BAG_REPORT_KEY = "REPORTKEY";
    private final ArrayList data;
    private final LayoutInflater inflater;
    private final Context context;
    private final int tileSize;

    public CustomApplicant(Context a, ArrayList d) {
        context = a;
        data = d;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        tileSize = context.getResources().getDimensionPixelSize(R.dimen.letter_tile_size);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout rl=(RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item,parent,false);
        //rl.setPadding(10,10,10,10);
        return new ViewHolder(rl);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder vh= (ViewHolder) holder;
        final ReportClass tempValues = (ReportClass) data.get(position);
        if(!tempValues.isReadApplicant()){
            vh.sub.setTypeface(null, Typeface.BOLD);
            vh.typeOfLeave.setTypeface(null, Typeface.BOLD);
            vh.typeOfLeave.setTextColor(Color.rgb(0x00, 0x00, 0x00));
            vh.time.setTypeface(null, Typeface.BOLD);
            vh.time.setTextColor(Color.rgb(0x44, 0x66, 0xdd));
            vh.status.setTypeface(null, Typeface.BOLD);
        }else{
            vh.sub.setTypeface(null, Typeface.NORMAL);
            vh.typeOfLeave.setTypeface(null, Typeface.NORMAL);
            vh.typeOfLeave.setTextColor(Color.rgb(0x66, 0x66, 0x66));
            vh.time.setTypeface(null, Typeface.NORMAL);
            vh.time.setTextColor(Color.rgb(0x66, 0x66, 0x66));
            vh.status.setTypeface(null, Typeface.NORMAL);
        }


        vh.sub.setText(tempValues.getSubject());
        vh.typeOfLeave.setText(tempValues.getDetail().getType());
        String smallDetailString="";
        smallDetailString+= util.getDateString(tempValues.getDetail().getDurationStart());
        smallDetailString+=" to ";
        smallDetailString+=util.getDateString(tempValues.getDetail().getDurationEnd());
        vh.smallDetail.setText(smallDetailString);

        final LetterTileProvider tileProvider = new LetterTileProvider(context);
        final RoundedBitmapDrawable roundedBitmap = tileProvider.getLetterTile(tempValues.getDetail().getType(), tempValues.getDetail().getType(), tileSize, tileSize);

        //holder.imageView.setImageResource(R.drawable.img2);
        vh.imageView.setImageDrawable(roundedBitmap);
        vh.time.setText(util.getTimeString(tempValues.getTimeOfArrival(),context));

        String approve_reject_inque;
        switch(tempValues.getStatus()){
            case 4:
            case 6:
                approve_reject_inque="Approved";
                break;
            case 3:
            case 7:
                approve_reject_inque="Rejected";
                break;
            default:
                approve_reject_inque="In-queue";
        }
        vh.status.setText(approve_reject_inque);

        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(v.getContext(),"Status: "+Status.getStatusMsg(tempValues.getStatus()), Toast.LENGTH_SHORT).show();

                updateRead(tempValues.getReportKey(),tempValues.getSender());
                Log.d("CustomApplicant","Unread msg");
                tempValues.setReadApplicant(true);

                onClickOfUserApplicationDetails(tempValues, context);
            }
        });
    }

    public static void onClickOfUserApplicationDetails(ReportClass tempValues, Context context) {

        Bundle myBag = new Bundle();
        myBag.putString(KEY_BAG_TOL,tempValues.getDetail().getType());
        myBag.putLong(KEY_BAG_FROM,tempValues.getDetail().getDurationStart());
        myBag.putLong(KEY_BAG_TO,tempValues.getDetail().getDurationEnd());
        myBag.putString(KEY_BAG_SUBJECT,tempValues.getSubject());
        myBag.putInt(KEY_BAG_STATUS,tempValues.getStatus());
        myBag.putString(KEY_BAG_TADD,tempValues.getDetail().getAddress());
        myBag.putString(KEY_BAG_REPORT_KEY,tempValues.getReportKey());
        if(tempValues.getDetail().getAttachment()==null) {
            myBag.putStringArrayList(KEY_BAG_ATTACH_URL, null);
        }
        else {
            myBag.putStringArrayList(KEY_BAG_ATTACH_URL, tempValues.getDetail().getAttachment().getURL());
        }

        Intent i=new Intent(context,ApplicantFormDetailsActivity.class);
        i.putExtras(myBag);
        context.startActivity(i);
    }

    private void updateRead(String reportKey, User sender) {

        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("leave_reports");
        mMessagesDatabaseReference.child(sender.getUser_id()).child(reportKey).child("readApplicant").setValue(true);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        if (data.size() <= 0)
            return 0;
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        RelativeLayout rl;
        public TextView typeOfLeave;
        public TextView sub;
        public TextView time;
        public ImageView imageView;
        public TextView smallDetail;
        public TextView status;

        public ViewHolder(RelativeLayout rl) {
            super(rl);
            this.rl=rl;
            typeOfLeave = (TextView) rl.findViewById(R.id.subject);
            imageView = (ImageView) rl.findViewById(R.id.image);
            sub = (TextView) rl.findViewById(R.id.sender);
            smallDetail = (TextView) rl.findViewById(R.id.shortDetail);
            time = (TextView) rl.findViewById(R.id.time);
            status = (TextView) rl.findViewById(R.id.tvStatus);
            status.setVisibility(View.VISIBLE);
        }
    }
}
