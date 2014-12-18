package info.doufm.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import info.doufm.android.R;

/**
 * Created with Android Studio.
 * Time: 20:06
 * Info:
 */
public class ChannelListAdapter extends BaseAdapter {

    private List<String> channelList;
    private Context context;
    private LayoutInflater layoutInflater;

    public ChannelListAdapter(Context context, List<String> channelList) {
        this.context = context;
        this.channelList = channelList;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return channelList.size();
    }

    @Override
    public Object getItem(int position) {
        return channelList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String channelName = channelList.get(position);
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.music_list_item, parent, false);
            viewHolder.channelIcon = (ImageView) convertView.findViewById(R.id.ivChannelIcon);
            viewHolder.channelName = (TextView) convertView.findViewById(R.id.tvChannelName);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.channelName.setText(channelList.get(position));
        return convertView;
    }

    private class ViewHolder {
        ImageView channelIcon;
        TextView channelName;
    }
}
