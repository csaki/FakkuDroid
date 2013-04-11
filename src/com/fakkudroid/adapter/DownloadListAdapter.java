package com.fakkudroid.adapter;

import java.io.File;
import java.util.LinkedList;

import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.util.ActionImageButton2;
import com.fakkudroid.util.Util;
import com.fakkudroid.DownloadListActivity;
import com.fakkudroid.R;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DownloadListAdapter extends ArrayAdapter<DoujinBean> {

	LayoutInflater inf;
	LinkedList<DoujinBean> objects;
	DownloadListActivity downloadListActivity;

	public DownloadListAdapter(Context context, int resource,
			int textViewResourceId, LinkedList<DoujinBean> objects,
			DownloadListActivity downloadListActivity) {
		super(context, resource, textViewResourceId, objects);
		this.inf = LayoutInflater.from(context);
		this.objects = objects;
		this.downloadListActivity = downloadListActivity;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		final DoujinBean s = objects.get(position);
		if (convertView == null) {
			convertView = inf.inflate(R.layout.row_download, null);
			holder = new ViewHolder();
			holder.tvDoujin = (TextView) convertView
					.findViewById(R.id.tvDoujin);
			holder.tvArtist = (TextView) convertView
					.findViewById(R.id.tvArtist);
			holder.tvSerie = (TextView) convertView.findViewById(R.id.tvSerie);
			holder.tvDescription = (TextView) convertView
					.findViewById(R.id.tvDescription);
			holder.wvTitle = (WebView) convertView.findViewById(R.id.wvTitle);
			holder.btnDelete = (ActionImageButton2) convertView
					.findViewById(R.id.btnDelete);
			holder.btnDetails = (ActionImageButton2) convertView
					.findViewById(R.id.btnDetails);
			holder.btnRead = (ActionImageButton2) convertView
					.findViewById(R.id.btnReadOnline);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.tvDoujin.setText(s.getTitle());
		holder.tvArtist.setText(Util.limitString(
				s.getArtist().getDescription(), 36, "..."));
		holder.tvSerie.setText(s.getSerie().getDescription());
		holder.tvDescription.setText(Html.fromHtml(s.getDescription().replace(
				"<br>", "<br/>")));
		;
		holder.wvTitle.setFocusable(false);
		holder.wvTitle.setLongClickable(false);
		holder.wvTitle.setClickable(false);
		holder.wvTitle.setFocusableInTouchMode(false);

		holder.btnDelete.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				downloadListActivity.delete(s);
			}
		});
		holder.btnDetails.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				downloadListActivity.showDetails(s);
			}
		});
		holder.btnRead.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				downloadListActivity.read(s);
			}
		});

		File dir = this.getContext().getDir(s.getId(), Context.MODE_PRIVATE);
		File titleFile = new File(dir, "001.fakku");
		holder.wvTitle.loadDataWithBaseURL(
				null,
				Util.createHTMLImagePercentage(
						"file://" + titleFile.getAbsolutePath(), 100,
						parent.getResources()), "text/html", "utf-8", null);
		return convertView;
	}

	static class ViewHolder {
		TextView tvDoujin;
		TextView tvSerie;
		TextView tvArtist;
		TextView tvDescription;
		ActionImageButton2 btnDelete;
		ActionImageButton2 btnRead;
		ActionImageButton2 btnDetails;

		WebView wvTitle;
	}
}
