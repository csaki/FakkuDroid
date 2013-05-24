package com.fakkudroid.adapter;

import java.io.File;
import java.util.LinkedList;

import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.util.Constants;
import com.fakkudroid.util.Util;
import com.fakkudroid.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DoujinListAdapter extends ArrayAdapter<DoujinBean> {

	LayoutInflater inf;
	LinkedList<DoujinBean> objects;

	public DoujinListAdapter(Context context, int resource,
			int textViewResourceId, LinkedList<DoujinBean> objects) {
		super(context, resource, textViewResourceId, objects);
		this.inf = LayoutInflater.from(context);
		this.objects = objects;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		DoujinBean s = objects.get(position);
		if (convertView == null) {
			convertView = inf.inflate(R.layout.row_doujin, null);
			holder = new ViewHolder();
			holder.tvDoujin = (TextView) convertView
					.findViewById(R.id.tvDoujin);
			holder.tvArtist = (TextView) convertView
					.findViewById(R.id.tvArtist);
			holder.tvSerie = (TextView) convertView.findViewById(R.id.tvSerie);
			holder.tvDescription = (TextView) convertView
					.findViewById(R.id.tvDescription);
			holder.tvTags = (TextView) convertView.findViewById(R.id.tvTags);
			holder.ivTitle = (ImageView) convertView.findViewById(R.id.ivTitle);
			holder.ivPage = (ImageView) convertView.findViewById(R.id.ivPage);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.tvDoujin.setText(Util.limitString(s.getTitle(), 36, "..."));
		holder.tvArtist.setText(Util.limitString(
				s.getArtist().getDescription(), 36, "..."));
		holder.tvSerie.setText(s.getSerie().getDescription());
		holder.tvDescription.setText(Html.fromHtml(s.getDescription().replace(
				"<br>", "<br/>")));
		;
		holder.tvTags.setText(s.getTags());
		
		holder.ivTitle.setImageBitmap(s.getBitmapImageTitle(getContext().getCacheDir()));
		holder.ivPage.setImageBitmap(s.getBitmapImagePage(getContext().getCacheDir()));
		return convertView;
	}

	static class ViewHolder {
		TextView tvDoujin;
		TextView tvSerie;
		TextView tvArtist;
		TextView tvTags;
		TextView tvDescription;

		ImageView ivTitle;
		ImageView ivPage;
	}
}
