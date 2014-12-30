package info.doufm.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import info.doufm.android.R;
import info.doufm.android.user.UserHistoryInfo;
import io.realm.RealmResults;

/**
 * Created with Android Studio.
 * Time: 20:06
 * Info:
 */
public class UserHistoryListAdapter extends BaseAdapter {

    private RealmResults<UserHistoryInfo> userHistoryInfos;
    private Context context;
    private LayoutInflater layoutInflater;

    public UserHistoryListAdapter(Context context, RealmResults<UserHistoryInfo> userHistoryInfos) {
        this.context = context;
        this.userHistoryInfos = userHistoryInfos;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return userHistoryInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return userHistoryInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        UserHistoryInfo userHistoryInfo = userHistoryInfos.get(position);
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.history_list_item, parent, false);
            viewHolder.historyIcon = (ImageView) convertView.findViewById(R.id.ivHistoryIcon);
            viewHolder.musicTitle = (TextView) convertView.findViewById(R.id.tvHistoryMusicTitle);
            viewHolder.singerName = (TextView) convertView.findViewById(R.id.tvHistorySingleName);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.musicTitle.setText(userHistoryInfo.getTitle());
        viewHolder.singerName.setText(userHistoryInfo.getSinger());
        return convertView;
    }

    private class ViewHolder {
        ImageView historyIcon;
        TextView musicTitle;
        TextView singerName;
    }
}
