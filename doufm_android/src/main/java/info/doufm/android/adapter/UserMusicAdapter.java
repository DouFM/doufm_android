package info.doufm.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import info.doufm.android.R;
import info.doufm.android.info.MusicInfo;
import info.doufm.android.info.UserHistoryInfoFromAPI;

/**
 * Created by lsc on 2014/12/28.
 */
public class UserMusicAdapter extends BaseAdapter {
    private JSONArray userHistoryInfos;
    private Context context;
    private LayoutInflater layoutInflater;

    public UserMusicAdapter(Context context, JSONArray jsonArray){
        layoutInflater = LayoutInflater.from(context);
        this.userHistoryInfos = jsonArray;
        this.context = context;
    }
    @Override
    public int getCount() {
        return userHistoryInfos.length();
    }

    @Override
    public Object getItem(int position) {
        JSONObject userHistoryInfo = null;
        try {
           userHistoryInfo = userHistoryInfos.getJSONObject(position);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return userHistoryInfo;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        JSONObject userHistoryInfo = null;
        try {
            userHistoryInfo = userHistoryInfos.getJSONObject(position);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.history_list_item, parent, false);
            viewHolder.historyIcon = (ImageView) convertView.findViewById(R.id.ivHistoryIcon);
            viewHolder.musicTitle = (TextView) convertView.findViewById(R.id.tvHistoryMusicTitle);
            viewHolder.date = (TextView) convertView.findViewById(R.id.tvHistorySingleName);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        /*if (userHistoryInfo != null) {
            try {
                viewHolder.historyIcon.setImageBitmap(userHistoryInfo.getString("cover"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }*/
        if (userHistoryInfo != null) {
            try {
                viewHolder.musicTitle.setText(userHistoryInfo.getString("title"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (userHistoryInfo != null) {
            try {
                viewHolder.date.setText(userHistoryInfo.getString("artist"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return convertView;
    }
    private class ViewHolder {
        ImageView historyIcon;
        TextView musicTitle;
        TextView date;
    }
}
