package com.fakkudroid.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.fakkudroid.GallerySwipeActivity;
import com.fakkudroid.MainActivity;
import com.fakkudroid.R;
import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.bean.URLBean;
import com.fakkudroid.bean.UserBean;
import com.fakkudroid.core.DataBaseHandler;
import com.fakkudroid.core.FakkuConnection;
import com.fakkudroid.core.FakkuDroidApplication;
import com.fakkudroid.exception.ExceptionNotLoggedIn;
import com.fakkudroid.service.DownloadManagerService;
import com.fakkudroid.util.Constants;
import com.fakkudroid.util.Helper;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.ClientProtocolException;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DoujinFragment extends SherlockFragment {

    private MainActivity mMainActivity;
    FakkuDroidApplication app;
    private View mFormView;
    private View mStatusView;
    private View view;
    private DoujinBean currentBean;
    boolean alreadyDownloaded = false;
    boolean quickDownload = false;
    private ProgressBar progressBar;

    public void setMainActivity(MainActivity mainActivity) {
        mMainActivity = mainActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (FakkuDroidApplication) getActivity().getApplication();
        if(currentBean==null){
            currentBean = new DoujinBean();
            currentBean.setUrl(getActivity().getIntent().getStringExtra(MainActivity.INTENT_VAR_URL));
        }

        quickDownload = getActivity().getIntent().getBooleanExtra(MainActivity.INTENT_VAR_QUICK_DOWNLOAD, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        alreadyDownloaded = verifyAlreadyDownloaded();
        if(!currentBean.isCompleted()){
            Helper.executeAsyncTask(new CompleteDoujin(), currentBean);
        }else{
            setComponents();
            showProgress(false);
        }
        if (DownloadManagerService.DoujinMap.exists(currentBean)) {
            Helper.executeAsyncTask(new UpdateStatus());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater
                .inflate(R.layout.fragment_doujin, container, false);

        mFormView = view.findViewById(R.id.view_form);
        mStatusView = view.findViewById(R.id.view_status);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        return view;
    }

    public void viewInBrowser(View view) {
        Intent viewBrowser = new Intent(Intent.ACTION_VIEW);
        viewBrowser.setData(Uri.parse(currentBean.getUrl()));
        this.startActivity(viewBrowser);
    }

    public void refresh() {
        currentBean = app.getCurrent();
        Helper.executeAsyncTask(new CompleteDoujin(), currentBean);
    }

    public void addOrRemoveFavorite(View view) {
        if (!app.getSettingBean().isChecked()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.login_please)
                    .setPositiveButton(R.string.login,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    Intent itMain = new Intent(mMainActivity, MainActivity.class);
                                    itMain.putExtra(MainActivity.INTENT_VAR_CURRENT_CONTENT, MainActivity.LOGIN);
                                    getActivity().startActivityForResult(itMain, 1);
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    return;
                                }
                            }).create().show();
        }
        app.setSettingBean(null);
        if (app.getSettingBean().isChecked())
            if (!currentBean.isAddedInFavorite()) {
                Helper.executeAsyncTask(new FavoriteDoujin(), true);
            } else {
                Helper.executeAsyncTask(new FavoriteDoujin(), false);
            }
    }

    public void read(View view) {
        app.setCurrent(currentBean);
        SharedPreferences preferenceManager = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (preferenceManager.getBoolean("perfect_viewer_checkbox", false) && alreadyDownloaded) {
            List<String> lstFiles = app.getCurrent().getImagesFiles();
            File dir = Helper.getDir(app.getCurrent().getId(), getActivity());
            File myFile = new File(dir, lstFiles.get(0));
            Helper.openPerfectViewer(myFile.getAbsolutePath(), getActivity());
        } else {
            Intent it = new Intent(getActivity(), GallerySwipeActivity.class);
            this.startActivity(it);
        }
    }

    public void download(boolean quick) {
        app.setCurrent(currentBean);
        if (!alreadyDownloaded) {
            if (DownloadManagerService.started) {
                if (!DownloadManagerService.DoujinMap.exists(app.getCurrent())) {
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.in_queue),
                            Toast.LENGTH_SHORT).show();
                    DownloadManagerService.DoujinMap.add(app.getCurrent());
                } else {
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.already_queue),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                getActivity().startService(new Intent(getActivity(), DownloadManagerService.class));
                Helper.executeAsyncTask(new UpdateStatus());
            }
        } else if (!quick){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.ask_delete)
                    .setPositiveButton(android.R.string.yes,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    String folder = currentBean.getId();
                                    File dir = Helper.getDir(folder, getActivity());
                                    try {
                                        FileUtils.deleteDirectory(dir);
                                    } catch (IOException e) {
                                        Helper.logError(this, e.getMessage(), e);
                                    }
                                    DataBaseHandler db = new DataBaseHandler(
                                            getActivity());
                                    db.deleteDoujin(currentBean.getId());

                                    ImageButton btnDownload = (ImageButton) DoujinFragment.this.view.findViewById(R.id.btnDownload);
                                    btnDownload
                                            .setImageResource(R.drawable.av_download);
                                    btnDownload
                                            .setContentDescription(getResources()
                                                    .getString(
                                                            R.string.download));
                                    Toast.makeText(
                                            getActivity(),
                                            getResources().getString(
                                                    R.string.deleted),
                                            Toast.LENGTH_SHORT).show();
                                    alreadyDownloaded = false;
                                }
                            }).setNegativeButton(android.R.string.no, null)
                    .create().show();
        }

        if (quick) {
            mMainActivity.quickDownloadDone();

            if (alreadyDownloaded)
            {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.quick_download_already),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setComponents() {
        RelativeLayout rl = (RelativeLayout) view.findViewById(
                R.id.doujinDetail);
        rl.setVisibility(View.VISIBLE);

        TextView tvDescription = (TextView) view.findViewById(
                R.id.tvDescription);
        TextView tvDoujin = (TextView) view.findViewById(R.id.tvDoujin);
        TextView tvArtist = (TextView) view.findViewById(R.id.tvArtist);
        ImageView ivTitle = (ImageView) view.findViewById(R.id.ivTitle);
        ImageView ivPage = (ImageView) view.findViewById(R.id.ivPage);
        TextView tvSerie = (TextView) view.findViewById(R.id.tvSerie);
        TextView tvQtyPages = (TextView) view
                .findViewById(R.id.tvQtyPages);
        TextView tvUploader = (TextView) view
                .findViewById(R.id.tvUploader);
        TextView tvLanguage = (TextView) view
                .findViewById(R.id.tvLanguage);
        TextView tvTranslator = (TextView) view.findViewById(
                R.id.tvTranslator);
        LinearLayout llTags = (LinearLayout) view
                .findViewById(R.id.llTags);

        String s = getResources().getString(R.string.content_pages);

        s = s.replace("rpc1", "" + currentBean.getQtyPages());
        s = s.replace("rpc2", "" + currentBean.getQtyFavorites());

        tvQtyPages.setText(s);

        s = getResources().getString(R.string.content_uploader);

        s = s.replace("rpc1", currentBean.getUploader().getUser());
        s = s.replace("rpc2", currentBean.getDate());

        SpannableString content = new SpannableString(s);
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        tvUploader.setText(content);

        s = getResources().getString(R.string.label_description);
        s = s.replace("?", currentBean.getDescription().replace("<br>", "<br/>"));
        tvDescription.setText(Html.fromHtml(s));
        tvDescription.setMovementMethod(LinkMovementMethod.getInstance());

        content = new SpannableString(currentBean.getTitle());
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        tvDoujin.setText(content);

        s = getResources().getString(R.string.label_artist);
        s = s.replace("?", currentBean.getArtist().getDescription());
        tvArtist.setText(Html.fromHtml(s));

        s = getResources().getString(R.string.label_serie);
        s = s.replace("?", currentBean.getSerie().getDescription());
        tvSerie.setText(Html.fromHtml(s));

        s = getResources().getString(R.string.label_language);
        s = s.replace("?", currentBean.getLanguage().getDescription());
        tvLanguage.setText(Html.fromHtml(s));

        s = getResources().getString(R.string.label_translator);
        s = s.replace("?", currentBean.getTranslator().getDescription());
        tvTranslator.setText(Html.fromHtml(s));

        Bitmap bmpTitle = currentBean.getBitmapImageTitle(Helper.getCacheDir(getActivity()));
        Bitmap bmpPage = currentBean.getBitmapImagePage(Helper.getCacheDir(getActivity()));

        if(bmpTitle!=null)
            ivTitle.setImageBitmap(bmpTitle);

        if(bmpPage!=null)
            ivPage.setImageBitmap(bmpPage);

        tvUploader.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent itMain = new Intent(mMainActivity, MainActivity.class);
                itMain.putExtra(MainActivity.INTENT_VAR_CURRENT_CONTENT, MainActivity.FAVORITES);
                itMain.putExtra(MainActivity.INTENT_VAR_USER,currentBean.getUploader().getUser());
                itMain.putExtra(MainActivity.INTENT_VAR_URL,currentBean.getUploader().getUrlUser());
                getActivity().startActivityForResult(itMain, 1);
            }
        });
        tvArtist.setOnClickListener(new URLListener(currentBean
                .getArtist(), R.string.tile_artist));
        tvLanguage.setOnClickListener(new URLListener(currentBean
                .getLanguage(), R.string.tile_language));
        tvSerie.setOnClickListener(new URLListener(currentBean.getSerie(),
                R.string.tile_serie));
        tvTranslator.setOnClickListener(new URLListener(currentBean
                .getTranslator(), R.string.tile_translator));

        llTags.removeAllViews();
        for (URLBean urlBean : currentBean.getLstTags()) {
            TextView tv = (TextView) getActivity().getLayoutInflater().inflate(
                    R.layout.textview_custom, null);
            content = new SpannableString(urlBean.getDescription());
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            tv.setText(content);

            tv.setOnClickListener(new URLListener(urlBean, R.string.tile_tag));
            llTags.addView(tv);
        }

        ImageButton btnAddToFavorite = (ImageButton) view
                .findViewById(R.id.btnAddToFavorite);
        alreadyDownloaded = verifyAlreadyDownloaded();

        if (currentBean != null) {
            if (currentBean.isAddedInFavorite()) {
                btnAddToFavorite.setImageResource(R.drawable.rating_important);
                btnAddToFavorite.setContentDescription(getResources()
                        .getString(R.string.remove_favorite));
            } else {
                btnAddToFavorite
                        .setImageResource(R.drawable.rating_not_important);
                btnAddToFavorite.setContentDescription(getResources()
                        .getString(R.string.add_favorite));
            }
            if (alreadyDownloaded) {
                ImageButton btnDownload = (ImageButton) view
                        .findViewById(R.id.btnDownload);
                btnDownload.setImageResource(R.drawable.content_discard);
                btnDownload.setContentDescription(getResources().getString(
                        R.string.delete));
            } else {
                ImageButton btnDownload = (ImageButton) view
                        .findViewById(R.id.btnDownload);
                btnDownload.setImageResource(R.drawable.av_download);
                btnDownload.setContentDescription(getResources().getString(
                        R.string.download));
            }
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            int shortAnimTime = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);

            mStatusView.setVisibility(View.VISIBLE);
            mStatusView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mStatusView.setVisibility(show ? View.VISIBLE
                                    : View.GONE);
                        }
                    });

            mFormView.setVisibility(View.VISIBLE);
            mFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mFormView.setVisibility(show ? View.GONE
                                    : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    class FavoriteDoujin extends AsyncTask<Boolean, Float, Boolean> {

        protected void onPreExecute() {
            showProgress(true);
        }

        protected Boolean doInBackground(Boolean... bool) {
            boolean isConnected = false;
            if (app.getSettingBean().isChecked())
                try {
                    isConnected = FakkuConnection.connect(app.getSettingBean()
                            .getUser(), app.getSettingBean().getPassword());
                } catch (ClientProtocolException e) {
                    Helper.logError(this, e.getMessage(), e);
                } catch (IOException e) {
                    Helper.logError(this, e.getMessage(), e);
                }

            if (!isConnected) {
                UserBean s = app.getSettingBean();
                s.setChecked(false);
                new DataBaseHandler(getActivity()).updateSetting(s);
                app.setSettingBean(null);
            } else {
                Boolean b = bool[0];
                try {
                    if (b)
                        FakkuConnection.transaction(currentBean
                                .urlFavorite(Constants.SITEADDFAVORITE));
                    else
                        FakkuConnection.transaction(currentBean
                                .urlFavorite(Constants.SITEREMOVEFAVORITE));
                } catch (ExceptionNotLoggedIn e) {
                    Helper.logError(this, e.getMessage(), e);
                } catch (IOException e) {
                    Helper.logError(this, e.getMessage(), e);
                }
                currentBean.setAddedInFavorite(b);
            }
            return isConnected;
        }

        protected void onPostExecute(Boolean bytes) {
            showProgress(false);
            if (bytes) {
                setComponents();
                String text = null;

                if (currentBean.isAddedInFavorite())
                    text = getResources().getString(R.string.added_favorite);
                else
                    text = getResources().getString(R.string.removed_favorite);
                Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT)
                        .show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        getActivity());
                builder.setMessage(R.string.login_please)
                        .setPositiveButton(R.string.login,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        Intent itMain = new Intent(mMainActivity, MainActivity.class);
                                        itMain.putExtra(MainActivity.INTENT_VAR_CURRENT_CONTENT, MainActivity.LOGIN);
                                        getActivity().startActivityForResult(itMain, 1);
                                    }
                                })
                        .setNegativeButton(android.R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        return;
                                    }
                                }).create().show();
            }
        }
    }

    class CompleteDoujin extends AsyncTask<DoujinBean, Float, DoujinBean> {

        protected void onPreExecute() {
            showProgress(true);
        }

        protected DoujinBean doInBackground(DoujinBean... beans) {

            if (app.getSettingBean().isChecked())
                try {
                    FakkuConnection.connect(app.getSettingBean().getUser(), app
                            .getSettingBean().getPassword());
                } catch (ClientProtocolException e) {
                    Helper.logError(this, e.getMessage(), e);
                } catch (IOException e) {
                    Helper.logError(this, e.getMessage(), e);
                }
            DoujinBean bean = beans[0];

            try {
                //if(bean.getUrl().toLowerCase().endsWith("random"))
                    FakkuConnection.parseHTMLDoujin(bean);
               /* else{
                    if(!bean.getUrl().contains(Constants.SITEAPI))
                        bean.setUrl(bean.getUrl().replace(Constants.SITEROOT, Constants.SITEAPI));
                    FakkuConnection.parseJsonDoujin(bean);
                }*/
            } catch (Exception e) {
                bean = null;
                Helper.logError(this, e.getMessage(), e);
            }

            try{
                if(bean!=null){
                    File dir = Helper.getCacheDir(getActivity());

                    File myFile = new File(dir, bean.getFileImageTitle());
                    Helper.saveInStorage(myFile, bean.getUrlImageTitle());

                    myFile = new File(dir, bean.getFileImagePage());
                    Helper.saveInStorage(myFile, bean.getUrlImagePage());
                }
            }catch (Exception e){
                Helper.logError(this, e.getMessage(), e);
            }

            return bean;
        }

        protected void onPostExecute(DoujinBean bean) {
            try {
                if (bean != null && bean.getTitle() != null) {
                    setComponents();
                    showProgress(false);

                    if (quickDownload)
                        download(true);

                } else {
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.no_data),
                            Toast.LENGTH_SHORT).show();
                    mMainActivity.onBackPressed();
                }
            } catch (Exception e) {
                Helper.logError(this, e.getMessage(), e);
            }
        }
    }

    class UpdateStatus extends AsyncTask<Boolean, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Boolean... arg0) {
            try {
                Thread.sleep(1000);
                while (DownloadManagerService.DoujinMap.exists(currentBean)) {
                    if (isRemoving()) {
                        break;
                    }
                    if (DownloadManagerService.currentBean != null
                            && DownloadManagerService.currentBean.getId()
                            .hashCode() == currentBean.getId()
                            .hashCode())
                        publishProgress(DownloadManagerService.percent);
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            progressBar.setProgress(DownloadManagerService.percent);
        }

        protected void onPostExecute(Boolean bytes) {
            if (isRemoving()) {
                return;
            }
            progressBar.setProgress(100);
            alreadyDownloaded = verifyAlreadyDownloaded();
            if(alreadyDownloaded){
                ImageButton btnDownload = (ImageButton) view.findViewById(R.id.btnDownload);
                btnDownload.setImageResource(R.drawable.content_discard);
                btnDownload.setContentDescription(getResources().getString(
                        R.string.delete));
            }

        }

    }

    class URLListener implements OnClickListener {

        URLBean urlBean;
        int rID;

        public URLListener(URLBean urlBean, int rID) {
            this.urlBean = urlBean;
            this.rID = rID;
        }

        @Override
        public void onClick(View v) {
            if(urlBean!=null&&urlBean.getDescription()!=null&&!urlBean.getDescription().equals("")){
                Intent itMain = new Intent(mMainActivity, MainActivity.class);
                itMain.putExtra(MainActivity.INTENT_VAR_CURRENT_CONTENT, MainActivity.DOUJIN_LIST);
                itMain.putExtra(MainActivity.INTENT_VAR_URL, urlBean.getUrl());
                itMain.putExtra(MainActivity.INTENT_VAR_TITLE, urlBean.getDescription());
                getActivity().startActivityForResult(itMain, 1);
            }
        }
    }

    public boolean verifyAlreadyDownloaded() {
        try {
            DataBaseHandler db = new DataBaseHandler(this.getActivity());
            return db.getDoujinBean(currentBean.getId()) != null;
        } catch (Exception e) {
            Helper.logError(this, "Error verifing if exists doujin in the db.", e);
        }

        return false;
    }

    public DoujinBean getCurrentBean() {
        return currentBean;
    }
}
