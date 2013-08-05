package com.fakkudroid.adapter;

import java.io.File;
import java.util.LinkedList;

import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.component.ActionImageButton2;
import com.fakkudroid.fragment.DownloadQueueListFragment;
import com.fakkudroid.service.DownloadManagerService;
import com.fakkudroid.util.Constants;
import com.fakkudroid.util.Helper;
import com.fakkudroid.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DownloadQueueListAdapter extends ArrayAdapter<DoujinBean> {

	LayoutInflater inf;
	LinkedList<DoujinBean> objects;
	DownloadQueueListFragment downloadQueueListFragment;

	public DownloadQueueListAdapter(Context context, int resource,
			int textViewResourceId, LinkedList<DoujinBean> objects,
			DownloadQueueListFragment downloadQueueListFragment) {
		super(context, resource, textViewResourceId, objects);
		this.inf = LayoutInflater.from(context);
		this.objects = objects;
		this.downloadQueueListFragment = downloadQueueListFragment;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		final DoujinBean s = objects.get(position);
		if (convertView == null) {
			convertView = inf.inflate(R.layout.row_download_queue, null);
			holder = new ViewHolder();
			holder.tvDoujin = (TextView) convertView
					.findViewById(R.id.tvDoujin);
			holder.tvArtist = (TextView) convertView
					.findViewById(R.id.tvArtist);
			holder.tvSerie = (TextView) convertView.findViewById(R.id.tvSerie);
			holder.tvDescription = (TextView) convertView
					.findViewById(R.id.tvDescription);
			holder.ivTitle = (ImageView) convertView.findViewById(R.id.ivTitle);
			holder.btnCancel = (ActionImageButton2) convertView
					.findViewById(R.id.btnCancel);
			holder.btnDetails = (ActionImageButton2) convertView
					.findViewById(R.id.btnDetails);
			holder.progressBar = (ProgressBar) convertView
					.findViewById(R.id.progressBar);
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
		holder.btnCancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				downloadQueueListFragment.cancel(s);
			}
		});
		holder.btnDetails.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				downloadQueueListFragment.showDetails(s);
			}
		});

		File dir = Helper.getCacheDir(getContext());
		File thumbFile = new File(dir, s.getFileImageTitle());

            holder.image = Helper.decodeSampledBitmapFromFile(thumbFile.getAbsolutePath(), Constants.WIDTH_STANDARD,
                    Constants.HEIGHT_STANDARD);
        holder.ivTitle.setImageBitmap(holder.image);
		if(s==DownloadManagerService.DoujinMap.next()){
			holder.progressBar.setIndeterminate(false);
			holder.progressBar.setProgress(DownloadManagerService.percent);
		}else{
			holder.progressBar.setIndeterminate(true);
		}
		return convertView;
	}

	static class ViewHolder {
		TextView tvDoujin;
		TextView tvSerie;
		TextView tvArtist;
		TextView tvDescription;
		ActionImageButton2 btnCancel;
		ActionImageButton2 btnDetails;
		ProgressBar progressBar;

        ImageView ivTitle;
        Bitmap image;
	}
}
