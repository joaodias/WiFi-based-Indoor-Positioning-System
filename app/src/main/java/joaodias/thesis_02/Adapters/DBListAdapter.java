package joaodias.thesis_02.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import joaodias.thesis_02.R;

public class DBListAdapter extends BaseAdapter {
    Context context;
    ArrayList<String> data;
    private static LayoutInflater inflater = null;


    public DBListAdapter(Context context, ArrayList<String> data) {    
        this.context = context;
        this.data = data;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.row, null);
        TextView element = (TextView) vi.findViewById(R.id.textView);
        element.setText(data.get(position));

        return vi;
    }
}