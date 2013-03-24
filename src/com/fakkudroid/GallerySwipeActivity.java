package com.fakkudroid;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.fakkudroid.bean.SettingBean;
import com.fakkudroid.core.DataBaseHandler;
import com.fakkudroid.core.FakkuDroidApplication;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class GallerySwipeActivity extends Activity {

	FakkuDroidApplication app;
	ViewPager mViewPager;
	Toast toast;
	GalleryPagerAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gallery_swipe);

		app = (FakkuDroidApplication) getApplication();
		adapter = new GalleryPagerAdapter(this);
		mViewPager = (ViewPager) findViewById(R.id.viewPager);
		mViewPager.setAdapter(adapter);

		SettingBean sb = app.getSettingBean();
		if (sb.getReading_mode() == SettingBean.JAPANESE_MODE)
			mViewPager.setCurrentItem(app.getCurrent().getImages().size() - 1);
		setTitle(app.getCurrent().getTitle());
		mViewPager.setOffscreenPageLimit(app.getCurrent().getQtyPages());
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int page) {
				SettingBean sb = app.getSettingBean();
				if (sb.getReading_mode() == SettingBean.JAPANESE_MODE)
					showToast("Page "
							+ (app.getCurrent().getImages().size() - page)
							+ "/" + app.getCurrent().getImages().size(), false);
				else
					showToast("Page " + (page + 1) + "/"
							+ app.getCurrent().getImages().size(), false);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
			registerForContextMenu(mViewPager);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_gallery, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int selectOption = -1;
		SettingBean sb = app.getSettingBean();
		switch (item.getItemId()) {
		case R.id.menu_reading_japanese:
			selectOption = SettingBean.JAPANESE_MODE;
			break;
		case R.id.menu_reading_occidental:
			selectOption = SettingBean.OCCIDENTAL_MODE;
			break;
		case R.id.go_to:
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Go to...");
			alert.setMessage("Page");

			// Set an EditText view to get user input
			final EditText input = new EditText(this);
			input.setInputType(InputType.TYPE_CLASS_NUMBER);
			alert.setView(input);

			alert.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							String value = input.getText().toString();
							if (!value.equals("")) {
								int page = Integer.parseInt(value) - 1;
								if (app.getSettingBean().getReading_mode() == SettingBean.JAPANESE_MODE) {
									page = app.getCurrent().getImages().size()
											- page - 1;
								}
								if (page >= 0
										&& page <= app.getCurrent()
												.getQtyPages() - 1) {
									mViewPager.setCurrentItem(page);
								} else {
									showToast(
											getResources()
													.getString(
															com.fakkudroid.R.string.error_page_out),
											false);
								}
							}
						}
					});

			alert.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// Canceled.
						}
					});

			alert.show();

			return true;
		case R.id.go_to_first:
			if (sb.getReading_mode() == SettingBean.JAPANESE_MODE) {
				mViewPager.setCurrentItem(app.getCurrent().getQtyPages() - 1);
			} else {
				mViewPager.setCurrentItem(0);
			}
			;
			return true;
		case R.id.go_to_last:
			if (sb.getReading_mode() == SettingBean.OCCIDENTAL_MODE) {
				mViewPager.setCurrentItem(app.getCurrent().getQtyPages() - 1);
			} else {
				mViewPager.setCurrentItem(0);
			}
			;
			return true;
		}
		if (sb.getReading_mode() == selectOption) {
			showToast("You are already in this mode.", false);
		} else {
			int currentItem = Math.abs(app.getCurrent().getQtyPages() - 1
					- mViewPager.getCurrentItem());
			sb.setReading_mode(selectOption);
			adapter.inverseOrder();
			mViewPager.setAdapter(adapter);
			mViewPager.setCurrentItem(currentItem);
			new DataBaseHandler(this).updateSetting(sb);
			app.setSettingBean(null);
		}
		return true;
	}

	void showToast(String txt, boolean isLongPress) {
		if (toast != null)
			toast.cancel();

		if (isLongPress)
			toast = Toast.makeText(this, txt, Toast.LENGTH_LONG);
		else
			toast = Toast.makeText(this, txt, Toast.LENGTH_SHORT);
		toast.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_gallery, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int selectOption = -1;
		SettingBean sb = app.getSettingBean();
		switch (item.getItemId()) {
		case R.id.menu_reading_japanese:
			selectOption = SettingBean.JAPANESE_MODE;
			break;
		case R.id.menu_reading_occidental:
			selectOption = SettingBean.OCCIDENTAL_MODE;
			break;
		case R.id.go_to:
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Go to...");
			alert.setMessage("Page");

			// Set an EditText view to get user input
			final EditText input = new EditText(this);
			input.setInputType(InputType.TYPE_CLASS_NUMBER);
			alert.setView(input);

			alert.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							String value = input.getText().toString();
							if (!value.equals("")) {
								int page = Integer.parseInt(value) - 1;
								if (app.getSettingBean().getReading_mode() == SettingBean.JAPANESE_MODE) {
									page = app.getCurrent().getImages().size()
											- page - 1;
								}
								if (page >= 0
										&& page <= app.getCurrent()
												.getQtyPages() - 1) {
									mViewPager.setCurrentItem(page);
								} else {
									showToast(
											getResources()
													.getString(
															com.fakkudroid.R.string.error_page_out),
											false);
								}
							}
						}
					});

			alert.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// Canceled.
						}
					});

			alert.show();

			return true;
		case R.id.go_to_first:
			if (sb.getReading_mode() == SettingBean.JAPANESE_MODE) {
				mViewPager.setCurrentItem(app.getCurrent().getQtyPages() - 1);
			} else {
				mViewPager.setCurrentItem(0);
			}
			;
			return true;
		case R.id.go_to_last:
			if (sb.getReading_mode() == SettingBean.OCCIDENTAL_MODE) {
				mViewPager.setCurrentItem(app.getCurrent().getQtyPages() - 1);
			} else {
				mViewPager.setCurrentItem(0);
			}
			;
			return true;
		}
		if (sb.getReading_mode() == selectOption) {
			showToast("You are already in this mode.", false);
		} else if (SettingBean.JAPANESE_MODE == selectOption
				|| SettingBean.OCCIDENTAL_MODE == selectOption) {
			int currentItem = Math.abs(app.getCurrent().getQtyPages() - 1
					- mViewPager.getCurrentItem());
			sb.setReading_mode(selectOption);
			adapter.inverseOrder();
			mViewPager.setAdapter(adapter);
			mViewPager.setCurrentItem(currentItem);
			new DataBaseHandler(this).updateSetting(sb);
			app.setSettingBean(null);
		}
		return true;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		adapter.resizeImage();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			adapter.load();
		}
	}

	class GalleryPagerAdapter extends PagerAdapter {

		private ArrayList<WebViewImageLayout> views;
		Context context;

		public GalleryPagerAdapter(Context context) {
			views = new ArrayList<WebViewImageLayout>();

			this.context = context;
			SettingBean sb = app.getSettingBean();

			File dir = getDir(app.getCurrent().getId(), Context.MODE_PRIVATE);
			List<String> lstFiles = app.getCurrent().getImagesFiles();

			if (sb.getReading_mode() == SettingBean.JAPANESE_MODE) {
				List<String> lstImages = app.getCurrent().getImages();
				for (int i = lstImages.size() - 1; i >= 0; i--) {
					String strImageFile = lstImages.get(i);
					File myFile = new File(dir, lstFiles.get(i));
					WebViewImageLayout wv = null;
					if (!myFile.exists())
						wv = new WebViewImageLayout(strImageFile,
								GallerySwipeActivity.this);
					else
						wv = new WebViewImageLayout("file://" + myFile.getAbsolutePath(),
								GallerySwipeActivity.this);
					views.add(wv);
				}
			} else
				for (String strImageFile : app.getCurrent().getImages()) {
					WebViewImageLayout wv = new WebViewImageLayout(
							strImageFile, GallerySwipeActivity.this);
					views.add(wv);
				}
		}

		@Override
		public void destroyItem(View view, int arg1, Object object) {
			((ViewPager) view).removeView((WebViewImageLayout) object);
		}

		@Override
		public void finishUpdate(View arg0) {

		}

		@Override
		public int getCount() {
			return views.size();
		}

		@Override
		public Object instantiateItem(View view, int position) {
			View myView = views.get(position);
			((ViewPager) view).addView(myView);
			return myView;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {

		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {

		}

		@SuppressWarnings("deprecation")
		public void load() {
			WindowManager wm = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			int width = display.getWidth();
			int height = display.getHeight();

			WebViewImageLayout wv = null;

			SettingBean sb = app.getSettingBean();

			if (sb.getReading_mode() == SettingBean.JAPANESE_MODE)
				for (int i = views.size() - 1; i >= 0; i--) {
					wv = views.get(i);
					wv.startLoader(width, height, true);
				}
			else
				for (int i = 0; i < views.size(); i++) {
					wv = views.get(i);
					wv.startLoader(width, height, false);
				}
		}

		@SuppressWarnings("deprecation")
		public void resizeImage() {
			WebViewImageLayout wv = null;

			WindowManager wm = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			int width = display.getWidth();
			int height = display.getHeight();

			SettingBean sb = app.getSettingBean();
			if (sb.getReading_mode() == SettingBean.JAPANESE_MODE)
				for (int i = views.size() - 1; i >= 0; i--) {
					wv = views.get(i);
					wv.resizeImage(width, height);
				}
			else
				for (int i = 0; i < views.size(); i++) {
					wv = views.get(i);
					wv.resizeImage(width, height);
				}
		}

		public void inverseOrder() {
			ArrayList<WebViewImageLayout> result = new ArrayList<WebViewImageLayout>();

			SettingBean sb = app.getSettingBean();
			for (int i = views.size() - 1; i >= 0; i--) {
				if (sb.getReading_mode() == SettingBean.JAPANESE_MODE)
					views.get(i).changeJapaneseMode(true);
				else
					views.get(i).changeJapaneseMode(false);
				result.add(views.get(i));
			}
			views = result;

		}
	}
}
