package arora.kushank.leavereport.verifier;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.ArrayList;

import arora.kushank.leavereport.R;
import arora.kushank.leavereport.ReportClass;
import arora.kushank.leavereport.util;

/**
 * Created by Kushank on 19-Apr-17.
 */
public class CustomListAdapter extends BaseAdapter {

    private final ArrayList data;
    private final LayoutInflater inflater;
    private final Context context;
    private final int tileSize;

    public CustomListAdapter(Context a, ArrayList d) {
        context = a;
        data = d;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        tileSize = context.getResources().getDimensionPixelSize(R.dimen.letter_tile_size);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View vi = convertView;

        if (convertView == null) {
            vi = inflater.inflate(R.layout.list_item, null);

            holder = new ViewHolder();
            holder.subject = (TextView) vi.findViewById(R.id.subject);
            holder.imageView = (ImageView) vi.findViewById(R.id.image);
            holder.senderName = (TextView) vi.findViewById(R.id.sender);
            holder.smallDetail = (TextView) vi.findViewById(R.id.shortDetail);
            holder.time = (TextView) vi.findViewById(R.id.time);

            vi.setTag(holder);
        } else
            holder = (ViewHolder) vi.getTag();

        if (data.size() <= 0)
            holder.subject.setText("No Data");
        else {
            ReportClass tempValues;
            tempValues = (ReportClass) data.get(position);

            holder.senderName.setText(tempValues.getSender().getName());
            holder.subject.setText(tempValues.getSubject());
            String smallDetailString="";
            smallDetailString+= util.getDateString(tempValues.getDetail().getDurationStart());
            smallDetailString+=" to ";
            smallDetailString+=util.getDateString(tempValues.getDetail().getDurationEnd());
            holder.smallDetail.setText(smallDetailString);

            /*
            Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.img2);
            Bitmap croppedBMP;
            if (bmp.getWidth() >= bmp.getHeight())
                croppedBMP = Bitmap.createBitmap(bmp, bmp.getWidth() / 2 - bmp.getHeight() / 2, 0, bmp.getHeight(), bmp.getHeight());
            else
                croppedBMP = Bitmap.createBitmap(bmp, 0, bmp.getHeight() / 2 - bmp.getWidth() / 2, bmp.getWidth(), bmp.getWidth());

            RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(context.getResources(), croppedBMP);
            dr.setCornerRadius(croppedBMP.getWidth() / 2);

            */
            final LetterTileProvider tileProvider = new LetterTileProvider(context);
            final RoundedBitmapDrawable roundedBitmap = tileProvider.getLetterTile(tempValues.getSender().getName(), tempValues.getSender().getEmail_id(), tileSize, tileSize);

            //holder.imageView.setImageResource(R.drawable.img2);
            holder.imageView.setImageDrawable(roundedBitmap);
            holder.time.setText(util.getTimeString(tempValues.getTimeOfArrival(),context));

            if((ListOfReportsActivity.isVC && tempValues.isReadVC()) || (!ListOfReportsActivity.isVC && tempValues.isReadChairman())) {
                holder.subject.setTypeface(null, Typeface.NORMAL);
                holder.senderName.setTypeface(null, Typeface.NORMAL);
                holder.time.setTypeface(null, Typeface.NORMAL);
                holder.subject.setTextColor(Color.rgb(0x66, 0x66, 0x66));
                holder.time.setTextColor(Color.rgb(0x66, 0x66, 0x66));
            }else{
                holder.subject.setTypeface(null, Typeface.BOLD);
                holder.senderName.setTypeface(null, Typeface.BOLD);
                holder.time.setTypeface(null, Typeface.BOLD);
                holder.subject.setTextColor(Color.rgb(0x00, 0x00, 0x00));
                holder.time.setTextColor(Color.rgb(0x44, 0x66, 0xdd));
            }
        }
        return vi;
    }

    public static class ViewHolder {
        public TextView subject;
        public TextView senderName;
        public TextView time;
        public ImageView imageView;
        public TextView smallDetail;
    }
}
