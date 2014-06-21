package com.fakkudroid.adapter;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.component.ActionImageButton2;
import com.fakkudroid.fragment.DownloadListFragment;
import com.fakkudroid.util.Constants;
import com.fakkudroid.util.Helper;
import com.fakkudroid.R;
import com.fakkudroid.util.ImageQuality;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DownloadListAdapter extends ArrayAdapter<DoujinBean> {

	LayoutInflater inf;
	LinkedList<DoujinBean> objects;
	DownloadListFragment downloadListFragment;

	public DownloadListAdapter(Context context, int resource,
			int textViewResourceId, LinkedList<DoujinBean> objects,
			DownloadListFragment downloadListFragment) {
		super(context, resource, textViewResourceId, objects);
		this.inf = LayoutInflater.from(context);
		this.objects = objects;
		this.downloadListFragment = downloadListFragment;
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
			holder.ivTitle = (ImageView) convertView.findViewById(R.id.ivTitle);
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
		holder.tvArtist.setText(Helper.limitString(s.getArtist()
				.getDescription(), 36, "..."));
		holder.tvSerie.setText(s.getSerie().getDescription());
		holder.tvDescription.setText(Html.fromHtml(s.getDescription().replace(
				"<br>", "<br/>")));
		;
		holder.btnDelete.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				downloadListFragment.delete(s);
			}
		});
		holder.btnDetails.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				downloadListFragment.showDetails(s);
			}
		});
		holder.btnRead.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				downloadListFragment.read(s);
			}
		});

		File dir = Helper.getDir(s.getId(), getContext());
		File oldTitleFile = new File(dir, "001.fakku");
		File titleFile = new File(dir, "001.jpg");
		// Save data.json
		try {
			Helper.saveJsonDoujin(s, dir);
		} catch (IOException e) {
			Helper.logError(this, e.getMessage(), e);
		}
		if (oldTitleFile.exists()) {
			oldTitleFile.renameTo(titleFile);
		}
		File thumbFile = new File(dir, "thumb.jpg");

		if (!thumbFile.exists() && titleFile.exists()) {
			Bitmap titleBitmap = Helper.decodeSampledBitmapFromFile(
					titleFile.getAbsolutePath(), ImageQuality.MEDIUM.getWidth(),
					ImageQuality.MEDIUM.getHeight());
			try {
				Helper.saveBitmap(thumbFile, titleBitmap);
			} catch (IOException e) {
				Helper.logError(this, "Error saving bitmap thumb", e);
			}
		}

            holder.image = Helper.decodeSampledBitmapFromFile(thumbFile.getAbsolutePath(), ImageQuality.MEDIUM.getWidth(),
                    ImageQuality.MEDIUM.getHeight());
        holder.ivTitle.setImageBitmap(holder.image);
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

		ImageView ivTitle;
        Bitmap image;
	}
}
