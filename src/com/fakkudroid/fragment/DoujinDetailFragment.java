package com.fakkudroid.fragment;

import java.io.File;
import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fakkudroid.DoujinActivity;
import com.fakkudroid.MainActivity;
import com.fakkudroid.R;
import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.bean.URLBean;
import com.fakkudroid.core.DataBaseHandler;
import com.fakkudroid.core.FakkuConnection;
import com.fakkudroid.core.FakkuDroidApplication;
import com.fakkudroid.util.Util;

@SuppressLint("ValidFragment")
public class DoujinDetailFragment extends Fragment {

	private FakkuDroidApplication app;
	DoujinActivity doujinActivity;
	boolean alreadyDownloaded = false;
	DoujinBean currentBean;
	
	public DoujinDetailFragment() {}

	@SuppressLint("ValidFragment")
	public DoujinDetailFragment(DoujinActivity doujinActivity) {
		this.doujinActivity = doujinActivity;
		this.currentBean = doujinActivity.getCurrentBean();
	}

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (FakkuDroidApplication) getActivity().getApplication();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			new CompleteDoujin()
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, currentBean);
		} else {
			new CompleteDoujin().execute(currentBean);
		}

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_doujin_detail,
				container, false);
		return view;
	}

	@SuppressLint("NewApi")
	public void refresh() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			new CompleteDoujin()
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, currentBean);
		} else {
			new CompleteDoujin().execute(currentBean);
		}
	}

	public void setComponents() {
		RelativeLayout rl = (RelativeLayout) getView().findViewById(
				R.id.doujinDetail);
		rl.setVisibility(View.VISIBLE);

		TextView tvDescription = (TextView) getView().findViewById(
				R.id.tvDescription);
		TextView tvDoujin = (TextView) getView().findViewById(R.id.tvDoujin);
		TextView tvArtist = (TextView) getView().findViewById(R.id.tvArtist);
		ImageView ivTitle = (ImageView) getView().findViewById(R.id.ivTitle);
		ImageView ivPage = (ImageView) getView().findViewById(R.id.ivPage);
		TextView tvSerie = (TextView) getView().findViewById(R.id.tvSerie);
		TextView tvQtyPages = (TextView) getView()
				.findViewById(R.id.tvQtyPages);
		TextView tvUploader = (TextView) getView()
				.findViewById(R.id.tvUploader);
		TextView tvLanguage = (TextView) getView()
				.findViewById(R.id.tvLanguage);
		TextView tvTranslator = (TextView) getView().findViewById(
				R.id.tvTranslator);
		LinearLayout llTags = (LinearLayout) getView()
				.findViewById(R.id.llTags);

		String s = getResources().getString(R.string.content_pages);

		s = s.replace("rpc1", "" + currentBean.getQtyPages());
		s = s.replace("rpc2", "" + currentBean.getQtyFavorites());

		tvQtyPages.setText(s);

		s = getResources().getString(R.string.content_uploader);

		s = s.replace("rpc1", currentBean.getUploader().getDescription());
		s = s.replace("rpc2", currentBean.getFecha());

		SpannableString content = new SpannableString(s);
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		tvUploader.setText(content);

		tvDescription.setText(Html.fromHtml(currentBean.getDescription()
				.replace("<br>", "<br/>")));
		tvDescription.setMovementMethod(LinkMovementMethod.getInstance());

		content = new SpannableString(currentBean.getTitle());
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		tvDoujin.setText(content);

		content = new SpannableString(currentBean.getArtist()
				.getDescription());
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		tvArtist.setText(content);

		content = new SpannableString(currentBean.getSerie()
				.getDescription());
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		tvSerie.setText(content);

		content = new SpannableString(currentBean.getLanguage()
				.getDescription());
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		tvLanguage.setText(content);

		content = new SpannableString(currentBean.getTranslator()
				.getDescription());
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		tvTranslator.setText(content);

		ivTitle.setImageBitmap(currentBean.getBitmapImageTitle(
				getActivity().getCacheDir()));
		ivPage.setImageBitmap(currentBean.getBitmapImagePage(
				getActivity().getCacheDir()));

		tvUploader.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent itFavorites = new Intent();
				itFavorites.putExtra(MainActivity.INTENT_VAR_USER, app
						.getCurrent().getUploader().getDescription());
				doujinActivity.goToFavorite(itFavorites);
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

		for (URLBean urlBean : currentBean.getLstTags()) {
			TextView tv = (TextView) getActivity().getLayoutInflater().inflate(
					R.layout.textview_custom, null);
			content = new SpannableString(urlBean.getDescription());
			content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
			tv.setText(content);

			tv.setOnClickListener(new URLListener(urlBean, R.string.tile_tag));
			llTags.addView(tv);
		}

		ImageButton btnAddToFavorite = (ImageButton) doujinActivity
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
				ImageButton btnDownload = (ImageButton) doujinActivity
						.findViewById(R.id.btnDownload);
				btnDownload.setImageResource(R.drawable.content_discard);
				btnDownload.setContentDescription(getResources().getString(
						R.string.delete));
			} else {
				ImageButton btnDownload = (ImageButton) doujinActivity
						.findViewById(R.id.btnDownload);
				btnDownload.setImageResource(R.drawable.av_download);
				btnDownload.setContentDescription(getResources().getString(
						R.string.download));
			}
		}
	}

	public void setAlreadyDownloaded(boolean alreadyDownloaded) {
		this.alreadyDownloaded = alreadyDownloaded;
	}

	public boolean isAlreadyDownloaded() {
		return alreadyDownloaded;
	}

	public boolean verifyAlreadyDownloaded() {
		try {
			DataBaseHandler db = new DataBaseHandler(this.getActivity());
			return db.getDoujinBean(currentBean.getId()) != null;
		} catch (Exception e) {
			Log.e(DoujinDetailFragment.class.getName(),
					"Error verifing if exists doujin in the db.", e);
		}

		return false;
	}

	class CompleteDoujin extends AsyncTask<DoujinBean, Float, DoujinBean> {

		protected void onPreExecute() {
			doujinActivity.showProgress(true);
		}

		protected DoujinBean doInBackground(DoujinBean... beans) {

			if (app.getSettingBean().isChecked())
				try {
					FakkuConnection.connect(app.getSettingBean().getUser(), app
							.getSettingBean().getPassword());
				} catch (ClientProtocolException e) {
					Log.e(this.getClass().toString(), e.getLocalizedMessage(),
							e);
				} catch (IOException e) {
					Log.e(this.getClass().toString(), e.getLocalizedMessage(),
							e);
				}
			DoujinBean bean = beans[0];

			try {
				FakkuConnection.parseHTMLDoujin(bean);
			} catch (ClientProtocolException e) {
				Log.e(CompleteDoujin.class.toString(), "Exception", e);
			} catch (IOException e) {
				Log.e(CompleteDoujin.class.toString(), "Exception", e);
			}

			try {
				File dir = getActivity().getCacheDir();

				File myFile = new File(dir, bean.getFileImageTitle());
				Util.saveInStorage(myFile, bean.getUrlImageTitle());

				myFile = new File(dir, bean.getFileImagePage());
				Util.saveInStorage(myFile, bean.getUrlImagePage());
			} catch (Exception e) {
				Log.e(CompleteDoujin.class.toString(), "Exception", e);
			}
			return bean;
		}

		protected void onPostExecute(DoujinBean bean) {
			try {
				if (bean.getTitle() != null) {
					setComponents();
					doujinActivity.showProgress(false);
				} else {
					Toast.makeText(getActivity(),
							getResources().getString(R.string.no_data),
							Toast.LENGTH_SHORT).show();
					getActivity().finish();
				}
			} catch (Exception e) {
				Log.e(CompleteDoujin.class.toString(), "Exception", e);
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

			Intent it = new Intent();
			it.putExtra(MainActivity.INTENT_VAR_TITLE, urlBean.getDescription());
			it.putExtra(MainActivity.INTENT_VAR_URL, urlBean.getUrl());
			doujinActivity.goToList(it);

		}
	}
}
