package arora.kushank.leavereport.verifier;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import arora.kushank.leavereport.R;

/**
 * Created by Kushank-Arora on 21-Apr-17.
 */
public class CustomListAdapterDetail extends BaseAdapter{
    private final ArrayList data;
    private final LayoutInflater inflater;
    private final Context context;

    public CustomListAdapterDetail(Context a, ArrayList d) {
        context = a;
        data = d;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
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
            vi = inflater.inflate(R.layout.list_item_detail, null);

            holder = new ViewHolder();
            holder.item = (TextView) vi.findViewById(R.id.tvitem);

            vi.setTag(holder);
        } else
            holder = (ViewHolder) vi.getTag();

        if (data.size() <= 0)
            holder.item.setText("No Data");
        else {
            String tempValue = (String) data.get(position);
            holder.item.setText(tempValue);
        }
        return vi;
    }

    public static class ViewHolder {
        public TextView item;
    }
}
