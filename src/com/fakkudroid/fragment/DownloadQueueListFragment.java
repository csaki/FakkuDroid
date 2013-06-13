package com.fakkudroid.fragment;

import java.util.LinkedList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockListFragment;
import com.fakkudroid.MainActivity;
import com.fakkudroid.R;
import com.fakkudroid.adapter.DownloadQueueListAdapter;
import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.core.FakkuDroidApplication;
import com.fakkudroid.service.DownloadManagerService;
import com.fakkudroid.util.Helper;

public class DownloadQueueListFragment extends SherlockListFragment{

    private MainActivity mMainActivity;
	DownloadQueueListAdapter da;
	private View view;
	NotificationManager mNotificationManager;

    public void setMainActivity(MainActivity mainActivity) {
        mMainActivity = mainActivity;
    }

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mNotificationManager = (NotificationManager)getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	private void showNotification(DoujinBean bean) {
		Intent resultIntent = new Intent(getActivity(),
                MainActivity.class);
        resultIntent.putExtra(MainActivity.INTENT_VAR_CURRENT_CONTENT, MainActivity.DOUJIN);
        resultIntent.putExtra(MainActivity.INTENT_VAR_URL, bean.getUrl());

		// Gets a PendingIntent containing the entire back stack
		PendingIntent resultPendingIntent = PendingIntent.getActivity(getActivity(),
				0, resultIntent,PendingIntent.FLAG_ONE_SHOT);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				getActivity()).setSmallIcon(
				R.drawable.ic_launcher).setContentTitle(bean.getTitle());

		mBuilder.setContentText(getResources().getString(R.string.download_cancelled));
		Notification notif = mBuilder.build();
		notif.contentIntent = resultPendingIntent;

		notif.flags = notif.flags | Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
		mNotificationManager.notify(bean.getId().hashCode(), notif);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_download_queue_list, container,
				false);
		setData();
		return view;
	}
	
	private void setData() {		
		LinkedList<DoujinBean> llDoujin = DownloadManagerService.DoujinMap.getList();

		da = new DownloadQueueListAdapter(this.getActivity(), R.layout.row_download, 0, llDoujin,
				this);
		this.setListAdapter(da);
	}
	
	public void onStart(){
		super.onStart();
		Helper.executeAsyncTask(new UpdateStatus());
	}
	
	public void cancel(DoujinBean bean){
		if(DownloadManagerService.DoujinMap.next()==bean){
			showNotification(bean);
			DownloadManagerService.DoujinMap.remove(bean);
			DownloadManagerService.cancel = true;
		}else{
			DownloadManagerService.DoujinMap.remove(bean);
		}
		setData();
	}

	public void showDetails(DoujinBean bean) {
        mMainActivity.loadDoujin(bean.getUrl());
	}
	
	class UpdateStatus extends AsyncTask<Boolean, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(Boolean... arg0) {
			try {
				Thread.sleep(1000);
				while (DownloadManagerService.DoujinMap.getList().size()>0) {
					if (getActivity()!=null&&getActivity().isFinishing()) {
						break;
					}
					publishProgress(DownloadManagerService.percent);
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			da.notifyDataSetChanged();
		}

		protected void onPostExecute(Boolean bytes) {
			if (getActivity()!=null&&getActivity().isFinishing()) {
				return;
			}
			da.notifyDataSetChanged();
		}

	}
}
