package com.fakkudroid.fragment;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fakkudroid.DoujinActivity;
import com.fakkudroid.DoujinListActivity;
import com.fakkudroid.FavoriteActivity;
import com.fakkudroid.R;
import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.bean.URLBean;
import com.fakkudroid.core.FakkuConnection;
import com.fakkudroid.core.FakkuDroidApplication;
import com.fakkudroid.util.Util;

@SuppressLint("ValidFragment")
public class DoujinDetailFragment extends Fragment {

	private FakkuDroidApplication app;
	DoujinActivity doujinActivity;
	boolean alreadyDownloaded = false;
	
	@SuppressLint("ValidFragment")
	public DoujinDetailFragment(DoujinActivity doujinActivity){
		this.doujinActivity = doujinActivity;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (FakkuDroidApplication) getActivity().getApplication();
		new CompleteDoujin().execute(app.getCurrent());

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_doujin_detail, container, false);
		return view;
	}
	
	public void refresh(){
		new CompleteDoujin().execute(app.getCurrent());
	}

	public void setComponents() {
		RelativeLayout rl = (RelativeLayout) getView().findViewById(R.id.doujinDetail);
		rl.setVisibility(View.VISIBLE);
		
		TextView tvDescription = (TextView) getView().findViewById(
				R.id.tvDescription);
		TextView tvDoujin = (TextView) getView().findViewById(R.id.tvDoujin);
		TextView tvArtist = (TextView) getView().findViewById(R.id.tvArtist);
		WebView wvTitle = (WebView) getView().findViewById(R.id.wvTitle);
		WebView wvPage = (WebView) getView().findViewById(R.id.wvPage);
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

		s = s.replace("rpc1", "" + app.getCurrent().getQtyPages());
		s = s.replace("rpc2", "" + app.getCurrent().getQtyFavorites());

		tvQtyPages.setText(s);

		s = getResources().getString(R.string.content_uploader);

		s = s.replace("rpc1", app.getCurrent().getUploader().getDescription());
		s = s.replace("rpc2", app.getCurrent().getFecha());

		SpannableString content = new SpannableString(s);
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		tvUploader.setText(content);

		tvDescription.setText(Html.fromHtml(app.getCurrent().getDescription()
				.replace("<br>", "<br/>")));
		tvDescription.setMovementMethod(LinkMovementMethod.getInstance());

		content = new SpannableString(app.getCurrent().getTitle());
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		tvDoujin.setText(content);

		content = new SpannableString(app.getCurrent().getArtist()
				.getDescription());
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		tvArtist.setText(content);

		content = new SpannableString(app.getCurrent().getSerie()
				.getDescription());
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		tvSerie.setText(content);

		content = new SpannableString(app.getCurrent().getLanguage()
				.getDescription());
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		tvLanguage.setText(content);

		content = new SpannableString(app.getCurrent().getTranslator()
				.getDescription());
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		tvTranslator.setText(content);

		wvTitle.loadDataWithBaseURL(null, Util.createHTMLImagePercentage(app
				.getCurrent().getUrlImageTitle(), 100, this.getResources()),
				"text/html", "utf-8", null);
		wvPage.loadDataWithBaseURL(null, Util.createHTMLImagePercentage(app
				.getCurrent().getUrlImagePage(), 100, this.getResources()),
				"text/html", "utf-8", null);

		tvUploader.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent itFavorites = new Intent(getActivity(),
						FavoriteActivity.class);
				itFavorites.putExtra(FavoriteActivity.INTENT_VAR_USER, app
						.getCurrent().getUploader().getDescription());
				getActivity().startActivity(itFavorites);
			}
		});
		tvArtist.setOnClickListener(new URLListener(app.getCurrent()
				.getArtist(), R.string.tile_artist));
		tvLanguage.setOnClickListener(new URLListener(app.getCurrent()
				.getLanguage(), R.string.tile_language));
		tvSerie.setOnClickListener(new URLListener(app.getCurrent().getSerie(),
				R.string.tile_serie));
		tvTranslator.setOnClickListener(new URLListener(app.getCurrent()
				.getTranslator(), R.string.tile_translator));

		for (URLBean urlBean : app.getCurrent().getLstTags()) {
			TextView tv = (TextView) getActivity().getLayoutInflater().inflate(
					R.layout.textview_custom, null);
			content = new SpannableString(urlBean.getDescription());
			content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
			tv.setText(content);

			tv.setOnClickListener(new URLListener(urlBean, R.string.tile_tag));
			llTags.addView(tv);
		}

		ImageButton btnAddToFavorite = (ImageButton) doujinActivity.findViewById(
				R.id.btnAddToFavorite);
		alreadyDownloaded = verifyAlreadyDownloaded();

		if (app.getCurrent() != null){
			if (app.getCurrent().isAddedInFavorite()) {
				btnAddToFavorite.setImageResource(R.drawable.rating_important);
				btnAddToFavorite.setContentDescription(getResources()
						.getString(R.string.remove_favorite));
			} else {
				btnAddToFavorite
						.setImageResource(R.drawable.rating_not_important);
				btnAddToFavorite.setContentDescription(getResources()
						.getString(R.string.add_favorite));
			}
			if(alreadyDownloaded){
				ImageButton btnDownload = (ImageButton)doujinActivity.findViewById(R.id.btnDownload);
				btnDownload.setImageResource(R.drawable.content_discard);
				btnDownload.setContentDescription(getResources().getString(R.string.delete));
			}else{
				ImageButton btnDownload = (ImageButton)doujinActivity.findViewById(R.id.btnDownload);
				btnDownload.setImageResource(R.drawable.av_download);
				btnDownload.setContentDescription(getResources().getString(R.string.download));
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
		List<String> lstFiles = app.getCurrent().getImagesFiles();
		String folder = app.getCurrent().getId();
		for (int i = 0; i < lstFiles.size(); i++) {
			File dir = doujinActivity.getDir(folder, Context.MODE_PRIVATE);
			File myFile = new File(dir, lstFiles.get(i));
			if (!myFile.exists()) {
				return false;
			}
		}
		return true;
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
			return bean;
		}

		protected void onPostExecute(DoujinBean bean) {
			if(bean.getTitle()!=null){
				setComponents();
				doujinActivity.showProgress(false);
			}else{
				Toast.makeText(getActivity(), getResources().getString(R.string.no_data), Toast.LENGTH_SHORT).show();
				getActivity().finish();
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

			Intent it = new Intent(getActivity(), DoujinListActivity.class);
			it.putExtra(DoujinListActivity.INTENT_VAR_TITLE,
					urlBean.getDescription());
			it.putExtra(DoujinListActivity.INTENT_VAR_URL, urlBean.getUrl());
			getActivity().startActivity(it);

		}
	}
}
