package com.fakkudroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.InputType;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.fakkudroid.component.ImageLayout;
import com.fakkudroid.core.FakkuDroidApplication;
import com.fakkudroid.util.Constants;
import com.fakkudroid.util.Helper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 */
public class ViewerMangaActivity extends SherlockActivity {

	FakkuDroidApplication app;
	ViewPager mViewPager;
	Toast toast;
	GalleryPagerAdapter adapter;
	boolean showPageNumber;
	int readingMode, volumeButtons;
    String backgroundColor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_gallery_swipe);

		configSettings();

		app = (FakkuDroidApplication) getApplication();
		adapter = new GalleryPagerAdapter(this);
		mViewPager = (ViewPager) findViewById(R.id.viewPager);
		mViewPager.setAdapter(adapter);

		if (readingMode == Constants.RIGHT_LEFT_MODE)
			mViewPager.setCurrentItem(app.getCurrent().getImages().size() - 1);
		setTitle(app.getCurrent().getTitle());
		mViewPager.setOffscreenPageLimit(app.getCurrent().getQtyPages());
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int page) {
				if (showPageNumber) {
					if (readingMode == Constants.RIGHT_LEFT_MODE)
						showToast("Page "
								+ (app.getCurrent().getImages().size() - page)
								+ "/" + app.getCurrent().getImages().size(),
								false);
					else
						showToast("Page " + (page + 1) + "/"
								+ app.getCurrent().getImages().size(), false);
                    adapter.load(page);
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
        mViewPager.setOffscreenPageLimit(5);
	}

	public void openOptionsMenu(View view) {
		openOptionsMenu();
	}

	private void configSettings() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		switch (Integer.parseInt(prefs
				.getString("screen_orientation_list", "0"))) {
		case Constants.SCREEN_ORIENTATION_LANDSCAPE:
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			break;
		case Constants.SCREEN_ORIENTATION_PORTRAIT:
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			break;
		}
		showPageNumber = prefs.getBoolean("page_number_checkbox", true);
        volumeButtons = Integer.parseInt(prefs.getString("volume_button_list", "" + Constants.VOLUME_BUTTONS_NONE));
		readingMode = Integer.parseInt(prefs
				.getString("reading_mode_list", "0"));
        backgroundColor = prefs.getString("default_color", Constants.DEFAULT_COLOR);
	}

	private void setReadingMode(int readingMode) {
		this.readingMode = readingMode;
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		prefs.edit().putString("reading_mode_list", readingMode + "").commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getSupportMenuInflater().inflate(R.menu.activity_gallery, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int selectOption = -1;
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		switch (item.getItemId()) {
		case R.id.menu_reading_right_left:
			selectOption = Constants.RIGHT_LEFT_MODE;
			break;
		case R.id.menu_reading_left_right:
			selectOption = Constants.LEFT_RIGHT_MODE;
			break;
		case R.id.go_to:
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Go to...");
			alert.setMessage("Page");

			// Set an EditText view to get user input
			final EditText input = new EditText(this);
			input.setInputType(InputType.TYPE_CLASS_NUMBER);
			alert.setView(input);

			alert.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							String value = input.getText().toString();
							if (!value.equals("")) {
								int page = Integer.parseInt(value) - 1;
								if (readingMode == Constants.RIGHT_LEFT_MODE) {
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
															R.string.error_page_out),
											false);
								}
							}
						}
					});

			alert.setNegativeButton(android.R.string.cancel, null);

			alert.show();

			return true;
		case R.id.go_to_first:
			if (readingMode == Constants.RIGHT_LEFT_MODE) {
				mViewPager.setCurrentItem(app.getCurrent().getQtyPages() - 1);
			} else {
				mViewPager.setCurrentItem(0);
			}
			return true;
		case R.id.go_to_last:
			if (readingMode == Constants.LEFT_RIGHT_MODE) {
				mViewPager.setCurrentItem(app.getCurrent().getQtyPages() - 1);
			} else {
				mViewPager.setCurrentItem(0);
			}
			return true;
		case R.id.menu_none_screen_orientation:
			prefs.edit().putString("screen_orientation_list", "0").commit();
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			return true;
		case R.id.menu_portrait_screen_orientation:
			prefs.edit().putString("screen_orientation_list", "1").commit();
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			return true;
		case R.id.menu_landscape_screen_orientation:
			prefs.edit().putString("screen_orientation_list", "2").commit();
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			return true;
        case R.id.menu_refresh:
            //adapter.getCurrent().reload();
            return true;
		default:
			return true;
		}
		if (readingMode == selectOption) {
			showToast("You are already in this mode.", false);
		} else {
			int currentItem = Math.abs(app.getCurrent().getQtyPages() - 1
					- mViewPager.getCurrentItem());

			setReadingMode(selectOption);

			adapter.inverseOrder();
			mViewPager.setAdapter(adapter);
			mViewPager.setCurrentItem(currentItem);

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
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		adapter.resizeImage();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
		    adapter.load(mViewPager.getCurrentItem());
		}
	}

	class GalleryPagerAdapter extends PagerAdapter {

		private ArrayList<ImageLayout> views;
		Context context;

		public GalleryPagerAdapter(Context context) {
			views = new ArrayList<ImageLayout>();

			this.context = context;
			File dir = Helper.getDir("", getApplicationContext());
            dir = new File(dir, app.getCurrent().getId());
			List<String> lstFiles = app.getCurrent().getImagesFiles();
			List<String> lstImages = app.getCurrent().getImages();

			if (readingMode == Constants.RIGHT_LEFT_MODE) {
				for (int i = lstFiles.size() - 1; i >= 0; i--) {
					String strImageFile = lstImages.get(i);
					File myFile = new File(dir, lstFiles.get(i));
                    ImageLayout wv = null;
					if (!myFile.exists())
						wv = new ImageLayout(strImageFile,backgroundColor,
								ViewerMangaActivity.this);
					else
						wv = new ImageLayout(myFile.getAbsolutePath(),backgroundColor,
								ViewerMangaActivity.this);
					views.add(wv);
				}
			} else
				for (int i = 0; i < lstFiles.size(); i++) {
					String strImageFile = lstImages.get(i);
					File myFile = new File(dir, lstFiles.get(i));
                    ImageLayout wv = null;
					if (!myFile.exists())
						wv = new ImageLayout(strImageFile,backgroundColor,
								ViewerMangaActivity.this);
					else
						wv = new ImageLayout(myFile.getAbsolutePath(),backgroundColor,
								ViewerMangaActivity.this);
					views.add(wv);
				}
		}

		@Override
		public void destroyItem(View view, int arg1, Object object) {
			((ViewPager) view).removeView((ImageLayout) object);
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

		@SuppressWarnings("deprecation")
		public void load(int i) {
			WindowManager wm = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			int width = display.getWidth();
			int height = display.getHeight();

            ImageLayout wv = null;


            if(!adapter.getCurrent().isLoaded()){
                wv = views.get(i);
                wv.startLoader(width, height, readingMode == Constants.RIGHT_LEFT_MODE);
            }
		}

		@SuppressWarnings("deprecation")
		public void resizeImage() {
			/*WebViewImageLayout wv = null;

			WindowManager wm = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			int width = display.getWidth();
			int height = display.getHeight();

			if (readingMode == Constants.RIGHT_LEFT_MODE)
				for (int i = views.size() - 1; i >= 0; i--) {
					wv = views.get(i);
					wv.resizeImage(width, height);
				}
			else
				for (int i = 0; i < views.size(); i++) {
					wv = views.get(i);
					wv.resizeImage(width, height);
				}*/
		}

		public void inverseOrder() {
			ArrayList<ImageLayout> result = new ArrayList<ImageLayout>();

			for (int i = views.size() - 1; i >= 0; i--) {
				/*if (readingMode == Constants.RIGHT_LEFT_MODE)
					views.get(i).changeJapaneseMode(true);
				else
					views.get(i).changeJapaneseMode(false);*/
				result.add(views.get(i));
			}
			views = result;

		}

		public ImageLayout getCurrent() {
			return views.get(mViewPager.getCurrentItem());
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {

		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {

		}

		@Override
		public void finishUpdate(View arg0) {

		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int action = event.getAction();
		int keyCode = event.getKeyCode();
		if (volumeButtons==Constants.VOLUME_BUTTONS_ZOOM){
			if ((action == KeyEvent.ACTION_DOWN)
					&& (keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
				adapter.getCurrent().zoomIn();
				return true;
			} else if ((action == KeyEvent.ACTION_DOWN)
					&& (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
				adapter.getCurrent().zoomOut();
				return true;
			}
        }else if(volumeButtons==Constants.VOLUME_BUTTONS_CHANGE_PAGE)
                if ((action == KeyEvent.ACTION_DOWN)
                        && (keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
                    if (readingMode == Constants.RIGHT_LEFT_MODE){
                        if(mViewPager.getCurrentItem()!=mViewPager.getChildCount()-1){
                            mViewPager.setCurrentItem(mViewPager.getCurrentItem()+1);
                        }
                    }else{
                        if(mViewPager.getCurrentItem()!=0){
                            mViewPager.setCurrentItem(mViewPager.getCurrentItem()-1);
                        }
                    }
                    return true;
                } else if ((action == KeyEvent.ACTION_DOWN)
                        && (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
                    if (readingMode == Constants.RIGHT_LEFT_MODE){
                        if(mViewPager.getCurrentItem()!=0){
                            mViewPager.setCurrentItem(mViewPager.getCurrentItem()-1);
                        }
                    }else{
                        if(mViewPager.getCurrentItem()!=mViewPager.getChildCount()-1){
                            mViewPager.setCurrentItem(mViewPager.getCurrentItem()+1);
                        }
                    }
                    return true;
                }
		return super.dispatchKeyEvent(event);
	}
}
