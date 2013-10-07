package com.fakkudroid.fragment;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.fakkudroid.MainActivity;
import com.fakkudroid.adapter.FavoriteListAdapter;
import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.core.FakkuConnection;
import com.fakkudroid.core.FakkuDroidApplication;
import com.fakkudroid.util.Helper;
import com.fakkudroid.R;

public class FavoriteFragment extends SherlockFragment implements
		AdapterView.OnItemClickListener {

	FakkuDroidApplication app;
    private LinkedList<DoujinBean> llDoujin;
	FavoriteListAdapter da;
	GridView gvFavorites;
    private String user;
    private String url_user;
    private String title;
    private int numPage = 1;
	private View mFormView;
	private View mStatusView;
	private View view;
    private MainActivity mMainActivity;
    private boolean refresh;

    public void setMainActivity(MainActivity mainActivity) {
        mMainActivity = mainActivity;
    }

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
	public void onStart(){
		super.onStart();
        if(llDoujin==null||refresh)
            loadPage();
        else{
            showProgress(false);
            setData();
        }
	}
	
	private View findViewById(int resource){
		return view.findViewById(resource);
	}
	
	public void setUser(String user, String url_user){
        refresh = true;
        numPage = 1;
		this.user = user;
        if(url_user==null)
            this.url_user = user;
        else
            this.url_user = url_user;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_favorite, container,
				false);
		gvFavorites = (GridView) findViewById(R.id.gvFavorites);
		gvFavorites.setOnItemClickListener(this);

		mFormView = findViewById(R.id.view_form);
		mStatusView = findViewById(R.id.view_status);
		return view;
	}

	@SuppressLint("NewApi")
	private void loadPage() {
		title = getResources().getString(R.string.favorite);
		title = title.replace("usr", user);

		TextView tvPage = (TextView) findViewById(R.id.tvPage);
		tvPage.setText("Page " + numPage);
		Helper.executeAsyncTask(new DownloadCatalog(),app.getUrlFavorite(numPage, url_user));
	}

	public void nextPage(View view) {
        numPage++;
		loadPage();
		CharSequence text = "Page " + numPage;
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(getActivity(), text, duration);
		toast.show();
	}

	public void previousPage(View view) {
		if (numPage - 1 == 0) {
			CharSequence text = "There aren't more pages.";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(getActivity(), text, duration);
			toast.show();
		} else {
            numPage--;
			loadPage();
			CharSequence text = "Page " + numPage;
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(getActivity(), text, duration);
			toast.show();
		}
	}

    public void changePage(int page){
        numPage = page;
        loadPage();
        CharSequence text = "Page " + numPage;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(this.getActivity(), text, duration);
        toast.show();
    }

	public void viewInBrowser(View view) {
		Intent viewBrowser = new Intent(Intent.ACTION_VIEW);
		viewBrowser.setData(Uri.parse(app.getUrlFavorite(numPage, url_user)));
		this.startActivity(viewBrowser);
	}

	public void refresh(View view) {
		loadPage();
	}

    public void refreshChanginUrlUser(String url_user){
        this.url_user = url_user;
        loadPage();
    }

	/**
	 * Funci�n auxiliar que recibe una lista de mapas, y utilizando esta data
	 * crea un adaptador para poblar al ListView del dise�o
	 * */
	private void setData() {
		da = new FavoriteListAdapter(this.getActivity(), R.layout.row_doujin, 0, llDoujin);
		gvFavorites.setAdapter(da);
        refresh = false;
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
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

    class DownloadImage extends AsyncTask<List<DoujinBean>, String, Boolean> {

        private ProgressBar progressBar;

        protected void onPreExecute() {
            progressBar = (ProgressBar) FavoriteFragment.this.getActivity().findViewById(R.id.progressBarImages);
        }

        @Override
        protected Boolean doInBackground(List<DoujinBean>... params) {
            boolean success = false;
            List<DoujinBean> list = params[0];
            for(DoujinBean bean:list){
                if(FavoriteFragment.this.getActivity()!=null){
                    try {
                        File dir = Helper.getCacheDir(getActivity());

                        File myFile = new File(dir, bean.getFileImageTitle());
                        Helper.saveInStorage(myFile, bean.getUrlImageTitle());

                        bean.loadImages(dir);
                    } catch (Exception e) {
                        Helper.logError(this, e.getMessage(), e);
                    }
                }
                publishProgress();
            }

            return success;
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            progressBar.setProgress(progressBar.getProgress() + 1);
            da.notifyDataSetChanged();
        }
    }

	class DownloadCatalog extends AsyncTask<String, String, Integer> {

        private ProgressBar progressBar;

		protected void onPreExecute() {
            progressBar = (ProgressBar) FavoriteFragment.this.getActivity().findViewById(R.id.progressBarImages);
            progressBar.setProgress(0);
			showProgress(true);
		}

		protected Integer doInBackground(String... urls) {

            publishProgress(getResources().getString(R.string.downloading_data));
			try {
				Log.i(DownloadCatalog.class.toString(), "URL Catalog: "
						+ urls[0]);
				llDoujin = FakkuConnection.parseHTMLFavorite(urls[0]);
			} catch (ClientProtocolException e) {
				Helper.logError(this, e.getMessage(), e);
			} catch (IOException e) {
				Helper.logError(this, e.getMessage(), e);
			} catch (URISyntaxException e) {
				Helper.logError(this, e.getMessage(), e);
			} catch (Exception e) {
				Helper.logError(this, e.getMessage(), e);
			}
			if (llDoujin == null)
				llDoujin = new LinkedList<DoujinBean>();
			return llDoujin.size();
		}

        protected void onPostExecute(Integer size) {
            if(FavoriteFragment.this.getActivity()!=null){
                if(llDoujin.size()==0){
                    progressBar.setMax(100);
                    progressBar.setProgress(100);
                }else{
                    progressBar.setMax(llDoujin.size());
                    List<List<DoujinBean>> list = Helper.splitArrayList(llDoujin, 3);
                    for(List<DoujinBean> l : list){
                        Helper.executeAsyncTask(new DownloadImage(), l);
                    }
                }
                setData();
                showProgress(false);
            }
        }
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		DoujinBean data = llDoujin.get(arg2);
		app.setCurrent(data);
        Intent itMain = new Intent(mMainActivity, MainActivity.class);
        itMain.putExtra(MainActivity.INTENT_VAR_CURRENT_CONTENT, MainActivity.DOUJIN);
        itMain.putExtra(MainActivity.INTENT_VAR_URL, data.getUrl());
        itMain.putExtra(MainActivity.INTENT_VAR_TITLE, data.getTitle());
        getActivity().startActivityForResult(itMain, 1);
	}

}