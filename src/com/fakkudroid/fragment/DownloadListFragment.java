package com.fakkudroid.fragment;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import org.apache.commons.io.FileUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.fakkudroid.DoujinActivity;
import com.fakkudroid.GallerySwipeActivity;
import com.fakkudroid.R;
import com.fakkudroid.adapter.DownloadListAdapter;
import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.core.DataBaseHandler;
import com.fakkudroid.core.FakkuDroidApplication;

public class DownloadListFragment extends SherlockListFragment{
	private FakkuDroidApplication app;
	DownloadListAdapter da;
	int numPage = 1;
	private View view;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (FakkuDroidApplication) getActivity().getApplication();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_download_list, container,
				false);
		setData();
		return view;
	}

	public void nextPage(View view) {
		numPage++;
		TextView tvPage = (TextView) view.findViewById(R.id.tvPage);
		tvPage.setText("Page " + numPage);
		setData();
		CharSequence text = "Page " + numPage;
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(this.getActivity(), text, duration);
		toast.show();
	}

	public void previousPage(View view) {
		if (numPage - 1 == 0) {

			CharSequence text = "There aren't more pages.";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(this.getActivity(), text, duration);
			toast.show();
		} else {
			numPage--;
			TextView tvPage = (TextView) view.findViewById(R.id.tvPage);
			tvPage.setText("Page " + numPage);
			setData();
			CharSequence text = "Page " + numPage;
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(this.getActivity(), text, duration);
			toast.show();
		}
	}

	private void setData() {
		DataBaseHandler db = new DataBaseHandler(this.getActivity());
		LinkedList<DoujinBean> llDoujin = db.getDoujinList(numPage);

		da = new DownloadListAdapter(this.getActivity(), R.layout.row_download, 0, llDoujin,
				this);
		this.setListAdapter(da);
	}

	public void delete(final DoujinBean bean) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
		builder.setMessage(R.string.ask_delete)
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								String folder = bean.getId();
								File dir = DownloadListFragment.this.getActivity().getDir(folder, Context.MODE_PRIVATE);
								try {
									FileUtils.deleteDirectory(dir);
								} catch (IOException e) {
									Log.e(DownloadListFragment.class.toString(),
											"Exception", e);
								}
								DataBaseHandler db = new DataBaseHandler(
										DownloadListFragment.this.getActivity());
								db.deleteDoujin(bean.getId());

								Toast.makeText(
										DownloadListFragment.this.getActivity(),
										getResources().getString(
												R.string.deleted),
										Toast.LENGTH_SHORT).show();
								setData();
							}
						}).setNegativeButton(android.R.string.no, null)
				.create().show();
	}

	public void showDetails(DoujinBean bean) {
		app.setCurrent(bean);
		Intent it = new Intent(this.getActivity(), DoujinActivity.class);
		this.startActivity(it);
	}

	public void read(DoujinBean bean) {
		app.setCurrent(bean);
		Intent it = new Intent(this.getActivity(), GallerySwipeActivity.class);
		this.startActivity(it);
	}
}
