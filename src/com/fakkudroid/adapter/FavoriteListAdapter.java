package com.fakkudroid.adapter;

import java.util.LinkedList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fakkudroid.R;
import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.util.Helper;

public class FavoriteListAdapter extends ArrayAdapter<DoujinBean> {

	LayoutInflater inf;
	LinkedList<DoujinBean> objects;

	public FavoriteListAdapter(Context context, int resource,
			int textViewResourceId, LinkedList<DoujinBean> objects) {
		super(context, resource, textViewResourceId, objects);
		this.inf = LayoutInflater.from(context);
		this.objects = objects;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		DoujinBean s = objects.get(position);
		if (convertView == null) {
			convertView = inf.inflate(R.layout.cell_favorite, null);
			holder = new ViewHolder();
			holder.tvDoujin = (TextView) convertView
					.findViewById(R.id.tvDoujin);
			holder.ivTitle = (ImageView) convertView.findViewById(R.id.ivTitle);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.tvDoujin.setText(Helper.limitString(s.getTitle(), 36, "..."));

        if(s.isTitleLoaded()){
            holder.ivTitle.setImageBitmap(s.getBitmapImageTitle(Helper.getCacheDir(getContext())));
        }else{
            holder.ivTitle.setImageResource(R.drawable.ic_launcher);
        }
		return convertView;
	}

	static class ViewHolder {
		TextView tvDoujin;

		ImageView ivTitle;
	}
}
