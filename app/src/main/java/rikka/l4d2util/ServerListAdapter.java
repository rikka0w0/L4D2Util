package rikka.l4d2util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import rikka.l4d2util.R;
import rikka.l4d2util.common.ServerObject;

/**
 * Created by Rikka0w0 on 2/13/2018.
 */

public class ServerListAdapter extends BaseAdapter {
    protected Context context;

    protected List<ServerObject> serverList;
    protected LayoutInflater inflater;

    public ServerListAdapter(Context context, List<ServerObject> serverList) {
        this.serverList= serverList;
        this.inflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public int getCount() {
        return serverList.size();
    }

    @Override
    public Object getItem(int i) {
        return serverList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = this.inflater.inflate(R.layout.listview_item_serverlist, viewGroup, false);

            holder.text = (TextView) view.findViewById(R.id.serverobject_text);
            holder.subtext = (TextView) view.findViewById(R.id.serverobject_subtext);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        ServerObject server = serverList.get(i);
        holder.text.setText(server.text);
        holder.subtext.setText(server.subText);

        return view;
    }

    private static class ViewHolder {
        TextView text;
        TextView subtext;
    }
}
