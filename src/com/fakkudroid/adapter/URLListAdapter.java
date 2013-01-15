package com.fakkudroid.adapter;

import java.util.LinkedList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.fakkudroid.bean.URLBean;
import com.fakkudroid.R;

public class URLListAdapter extends ArrayAdapter<URLBean> {

	LayoutInflater inf;
	LinkedList<URLBean> objects;

	public URLListAdapter(Context context, int resource, int textViewResourceId,
			LinkedList<URLBean> objects) {
		super(context, resource, textViewResourceId, objects);
		this.inf = LayoutInflater.from(context);
		this.objects = objects;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		URLBean s = objects.get(position);
		if (convertView == null) {
			convertView = inf.inflate(R.layout.row_url, null);
			holder = new ViewHolder();
			holder.tvURL = (TextView) convertView.findViewById(R.id.tvURL);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.tvURL.setText(s.getDescription());
		return convertView;
	}
	
	static class ViewHolder {
		TextView tvURL;
	}
}
