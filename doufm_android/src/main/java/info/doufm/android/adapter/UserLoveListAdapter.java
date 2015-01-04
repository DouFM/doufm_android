package info.doufm.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import info.doufm.android.R;
import info.doufm.android.user.UserLoveMusicInfo;
import io.realm.RealmResults;

/**
 * Created with Android Studio.
 * Time: 10:13
 * Info:
 */
public class UserLoveListAdapter extends BaseAdapter {

    private ArrayList<UserLoveMusicInfo> userLoveMusicInfos;
    private Context context;
    private LayoutInflater layoutInflater;

    public UserLoveListAdapter(Context context, ArrayList<UserLoveMusicInfo> userLoveMusicInfos) {
        this.context = context;
        this.userLoveMusicInfos = userLoveMusicInfos;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return userLoveMusicInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return userLoveMusicInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        UserLoveMusicInfo userHistoryInfo = userLoveMusicInfos.get(position);
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.love_list_item, parent, false);
            viewHolder.historyIcon = (ImageView) convertView.findViewById(R.id.ivLoveIcon);
            viewHolder.musicTitle = (TextView) convertView.findViewById(R.id.tvLoveMusicTitle);
            viewHolder.singerName = (TextView) convertView.findViewById(R.id.tvLoveSingerName);
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
