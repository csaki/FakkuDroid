package com.fakkudroid.adapter;

import java.io.File;
import java.util.LinkedList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fakkudroid.R;
import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.util.Util;

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
		holder.tvDoujin.setText(Util.limitString(s.getTitle(), 36, "..."));

		File myFile = new File(getContext().getCacheDir(), s.getFileImageTitle());
		
		Bitmap myBitmap = BitmapFactory.decodeFile(myFile.getAbsolutePath());
		
		holder.ivTitle.setImageBitmap(myBitmap);
		return convertView;
	}

	static class ViewHolder {
		TextView tvDoujin;

		ImageView ivTitle;
	}
}
