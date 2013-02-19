package com.fakkudroid;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.bean.SettingBean;
import com.fakkudroid.bean.URLBean;
import com.fakkudroid.core.DataBaseHandler;
import com.fakkudroid.core.ExceptionNotLoggedIn;
import com.fakkudroid.core.FakkuConnection;
import com.fakkudroid.core.FakkuDroidApplication;
import com.fakkudroid.util.Util;
import com.fakkudroid.R;

public class DoujinActivity extends Activity {

	private FakkuDroidApplication app;
	private View mFormView;
	private View mStatusView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_doujin);

		mFormView = findViewById(R.id.view_form);
		mStatusView = findViewById(R.id.view_status);

		app = (FakkuDroidApplication) getApplication();

		new CompleteDoujin().execute(app.getCurrent());
	}

	public void viewInBrowser(View view) {
		Intent viewBrowser = new Intent(Intent.ACTION_VIEW);
		viewBrowser.setData(Uri.parse(app.getCurrent().getUrl()));
		this.startActivity(viewBrowser);
	}

	public void refresh(View view) {
		showProgress(true);
		new CompleteDoujin().execute(app.getCurrent());
	}

	public void readOnline(View view) {
		Intent it = new Intent(DoujinActivity.this, GallerySwipeActivity.class);
		DoujinActivity.this.startActivity(it);
	}
	
	public void relatedContent(View view) {
		Intent it = new Intent(DoujinActivity.this, RelatedContentListActivity.class);
		DoujinActivity.this.startActivity(it);
	}
	
	public void comments(View view) {
		Toast.makeText(this, getResources().getString(R.string.in_construction), Toast.LENGTH_LONG).show();
	}

	public void addOrRemoveFavorite(View view) {

		if (!app.getSettingBean().isChecked()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.login_please)
					.setPositiveButton(R.string.login,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									Intent it = new Intent(DoujinActivity.this,
											LoginActivity.class);
									DoujinActivity.this.startActivity(it);
								}
							})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									return;
								}
							}).create().show();
		}
		app.setSettingBean(null);
		if (app.getSettingBean().isChecked())
			if (!app.getCurrent().isAddedInFavorite()) {
				new FavoriteDoujin().execute(true);
			} else {
				new FavoriteDoujin().execute(false);
			}
	}

	private void setComponents() {
		TextView tvDescription = (TextView) findViewById(R.id.tvDescription);
		TextView tvDoujin = (TextView) findViewById(R.id.tvDoujin);
		TextView tvArtist = (TextView) findViewById(R.id.tvArtist);
		WebView wvTitle = (WebView) findViewById(R.id.wvTitle);
		WebView wvPage = (WebView) findViewById(R.id.wvPage);
		TextView tvSerie = (TextView) findViewById(R.id.tvSerie);
		TextView tvQtyPages = (TextView) findViewById(R.id.tvQtyPages);
		TextView tvUploader = (TextView) findViewById(R.id.tvUploader);
		TextView tvLanguage = (TextView) findViewById(R.id.tvLanguage);
		TextView tvTranslator = (TextView) findViewById(R.id.tvTranslator);
		LinearLayout llTags = (LinearLayout) findViewById(R.id.llTags);

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

		tvDescription.setText(Html.fromHtml(app.getCurrent().getDescription().replace("<br>", "<br/>")));
		tvDescription.setMovementMethod(LinkMovementMethod.getInstance());
		
		content = new SpannableString(app.getCurrent().getTitle());
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		tvDoujin.setText(content);
		
		content = new SpannableString(app.getCurrent().getArtist().getDescription());
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		tvArtist.setText(content);
		
		content = new SpannableString(app.getCurrent().getSerie().getDescription());
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		tvSerie.setText(content);
		
		content = new SpannableString(app.getCurrent().getLanguage().getDescription());
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		tvLanguage.setText(content);
		
		content = new SpannableString(app.getCurrent().getTranslator().getDescription());
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		tvTranslator.setText(content);

		wvTitle.loadDataWithBaseURL(null, Util.createHTMLImagePercentage(app
				.getCurrent().getUrlImageTitle(), 100), "text/html", "utf-8",
				null);
		wvPage.loadDataWithBaseURL(null, Util.createHTMLImagePercentage(app
				.getCurrent().getUrlImagePage(), 100), "text/html", "utf-8",
				null);

		tvUploader.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent itFavorites = new Intent(DoujinActivity.this,
						FavoriteActivity.class);
				itFavorites.putExtra(FavoriteActivity.INTENT_VAR_USER, app
						.getCurrent().getUploader().getDescription());
				DoujinActivity.this.startActivity(itFavorites);
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
			TextView tv = (TextView) getLayoutInflater().inflate(
					R.layout.textview_custom, null);
			content = new SpannableString(urlBean.getDescription());			
			content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
			tv.setText(content);

			tv.setOnClickListener(new URLListener(urlBean, R.string.tile_tag));
			llTags.addView(tv);
		}

		ImageButton btnAddToFavorite = (ImageButton) findViewById(R.id.btnAddToFavorite);

		if (app.getCurrent() != null)
			if (app.getCurrent().isAddedInFavorite()) {
				btnAddToFavorite.setImageResource(R.drawable.rating_important);
				btnAddToFavorite.setContentDescription(getResources().getString(R.string.remove_favorite));
			} else {
				btnAddToFavorite
				.setImageResource(R.drawable.rating_not_important);
				btnAddToFavorite.setContentDescription(getResources().getString(R.string.add_favorite));
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
					Log.e(this.getClass().toString(), e.getLocalizedMessage(),
							e);
				} catch (IOException e) {
					Log.e(this.getClass().toString(), e.getLocalizedMessage(),
							e);
				}

			if (!isConnected) {
				SettingBean s = app.getSettingBean(); 
				s.setChecked(false);
				new DataBaseHandler(DoujinActivity.this).updateSetting(s);
				app.setSettingBean(null);
			} else {
				Boolean b = bool[0];
				try {
					if (b)
						FakkuConnection.addToFavorites(app.getCurrent());
					else
						FakkuConnection.removeFromFavorites(app.getCurrent());
				} catch (ExceptionNotLoggedIn e) {
					Log.e(this.getClass().toString(), e.getLocalizedMessage(),
							e);
				} catch (IOException e) {
					Log.e(this.getClass().toString(), e.getLocalizedMessage(),
							e);
				}
				app.getCurrent().setAddedInFavorite(b);
			}
			return isConnected;
		}

		protected void onPostExecute(Boolean bytes) {
			showProgress(false);
			if(bytes){
				setComponents();
				String text = null;
				
				if(app.getCurrent().isAddedInFavorite())
					text = getResources().getString(R.string.added_favorite);
				else
					text = getResources().getString(R.string.removed_favorite);
				Toast.makeText(DoujinActivity.this, text, Toast.LENGTH_SHORT).show();
			}else{
				AlertDialog.Builder builder = new AlertDialog.Builder(DoujinActivity.this);
				builder.setMessage(R.string.login_please)
						.setPositiveButton(R.string.login,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										Intent it = new Intent(DoujinActivity.this,
												LoginActivity.class);
										DoujinActivity.this.startActivity(it);
									}
								})
						.setNegativeButton(R.string.cancel,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										return;
									}
								}).create().show();
			}
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
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

	class CompleteDoujin extends AsyncTask<DoujinBean, Float, DoujinBean> {

		protected void onPreExecute() {
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

		protected void onPostExecute(DoujinBean bytes) {
			setComponents();
			showProgress(false);
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

			Intent it = new Intent(DoujinActivity.this,
					DoujinListActivity.class);
			it.putExtra(DoujinListActivity.INTENT_VAR_TITLE,
					urlBean.getDescription());
			it.putExtra(DoujinListActivity.INTENT_VAR_URL, urlBean.getUrl());
			DoujinActivity.this.startActivity(it);

		}
	}
}
