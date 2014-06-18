package com.fakkudroid.asynctask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.fakkudroid.R;
import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.core.FakkuConnection;
import com.fakkudroid.core.FakkuDroidApplication;
import com.fakkudroid.service.DownloadManagerService;

import java.io.IOException;

/**
 * Created by neko on 18/06/2014.
 */

public class DownloadAsyncTask extends AsyncTask<DoujinBean, Integer, DoujinBean> {

    ProgressDialog dialog;

    Activity activity;

    public DownloadAsyncTask(Activity activity){
        this.activity = activity;
    }

    @Override
    protected void onPreExecute()
    {
        dialog = new ProgressDialog(activity);
        dialog.setTitle(R.string.app_name);
        dialog.setMessage(activity.getString(R.string.loading));
        dialog.setIcon(R.drawable.ic_launcher);
        dialog.setCancelable(false);
        dialog.setIndeterminate(true);
        dialog.show();
    }

    @Override
    protected DoujinBean doInBackground(DoujinBean... beans) {
        DoujinBean bean = beans[0];
        try {
            if(bean.getImageServer()==null){
                String urlServer = FakkuConnection.imageServerUrl(bean.getUrl());
                bean.setImageServer(urlServer);
            }
            if(bean.getQtyPages()<=0){
                FakkuConnection.parseHTMLDoujin(bean);
            }
        } catch (IOException e) {
            return null;
        }
        return bean;
    }

    @Override
    protected void onPostExecute(DoujinBean result) {
        dialog.dismiss();
        if(result==null){
            Toast.makeText(activity, "Error download", Toast.LENGTH_SHORT).show();
            return;
        }
        ((FakkuDroidApplication)activity.getApplication()).setCurrent(result);
        if (DownloadManagerService.started) {
            if (!DownloadManagerService.DoujinMap.exists(result)) {
                Toast.makeText(activity,
                        activity.getString(R.string.in_queue),
                        Toast.LENGTH_SHORT).show();
                DownloadManagerService.DoujinMap.add(result);
            } else {
                Toast.makeText(activity,
                        activity.getString(R.string.already_queue),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            activity.startService(new Intent(activity, DownloadManagerService.class));
        }
    }
}