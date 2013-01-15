package com.fakkudroid;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.fakkudroid.bean.SettingBean;
import com.fakkudroid.core.DataBaseHandler;
import com.fakkudroid.core.FakkuDroidApplication;
import com.fakkudroid.R;

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
	MyPagerAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gallery_swipe);

		app = (FakkuDroidApplication) getApplication();
		adapter = new MyPagerAdapter(this);
		mViewPager = (ViewPager) findViewById(R.id.viewPager);
		mViewPager.setAdapter(adapter);

		if (app.getSettingBean().getReading_mode() == SettingBean.JAPANESE_MODE)
			mViewPager.setCurrentItem(app.getCurrent().getImages().size() - 1);

		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int page) {
				if (app.getSettingBean().getReading_mode() == SettingBean.JAPANESE_MODE)
					showToast("Page "
							+ (app.getCurrent().getImages().size() - page)
							+ "/" + app.getCurrent().getImages().size(),false);
				else
					showToast("Page " + (page + 1) + "/"
							+ app.getCurrent().getImages().size(),false);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		
		registerForContextMenu(mViewPager);
				
		showToast(getResources().getString(R.string.tutorial_change_reading_mode),true);		
	}

	void showToast(String txt, boolean isLongPress) {
		if (toast != null)
			toast.cancel();

		if(isLongPress)
			toast = Toast.makeText(this, txt, Toast.LENGTH_LONG);
		else
			toast = Toast.makeText(this, txt, Toast.LENGTH_SHORT);
		toast.show();
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
		switch (item.getItemId()) {
		case R.id.menu_reading_japanese:
			selectOption = SettingBean.JAPANESE_MODE;			
			break;
		case R.id.menu_reading_occidental:
			selectOption = SettingBean.OCCIDENTAL_MODE;
			break;
		}
		if(app.getSettingBean().getReading_mode()==selectOption){
			showToast("You are already in this mode.",false);
		}else{
			int currentItem = Math.abs(app.getCurrent().getQtyPages() - 1 - mViewPager.getCurrentItem());
			app.getSettingBean().setReading_mode(selectOption);
			adapter.inverseOrder();
			mViewPager.setAdapter(adapter);
			mViewPager.setCurrentItem(currentItem);
			new DataBaseHandler(this).updateSetting(app.getSettingBean());
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

	/**
	 * This is a helper class that implements the management of tabs and all
	 * details of connecting a ViewPager with associated TabHost. It relies on a
	 * trick. Normally a tab host has a simple API for supplying a View or
	 * Intent that each tab will show. This is not sufficient for switching
	 * between pages. So instead we make the content part of the tab host 0dp
	 * high (it is not shown) and the TabsAdapter supplies its own dummy view to
	 * show as the tab content. It listens to changes in tabs, and takes care of
	 * switch to the correct paged in the ViewPager whenever the selected tab
	 * changes.
	 */
	public class MyPagerAdapter extends PagerAdapter {

		private ArrayList<WebViewImageLayout> views;
		Context context;

		public MyPagerAdapter(Context context) {
			views = new ArrayList<WebViewImageLayout>();

			this.context = context;
			if (app.getSettingBean().getReading_mode() == SettingBean.JAPANESE_MODE) {
				for (int i = app.getCurrent().getImages().size() - 1; i >= 0; i--) {
					String strImageFile = app.getCurrent().getImages().get(i);
					WebViewImageLayout wv = new WebViewImageLayout(
							strImageFile, GallerySwipeActivity.this);
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
			
			
			if (app.getSettingBean().getReading_mode() == SettingBean.JAPANESE_MODE)
				for (int i = views.size() - 1; i >= 0; i--) {
					wv = views.get(i);
					wv.startLoader(width, height,true);
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
			
			if (app.getSettingBean().getReading_mode() == SettingBean.JAPANESE_MODE)
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
		
		public void inverseOrder(){
			ArrayList<WebViewImageLayout> result = new ArrayList<WebViewImageLayout>();
			for (int i = views.size() - 1; i >= 0; i--) {
				if (app.getSettingBean().getReading_mode() == SettingBean.JAPANESE_MODE)
					views.get(i).changeJapaneseMode(true);
				else
					views.get(i).changeJapaneseMode(false);
				result.add(views.get(i));
			}
			views = result;
			
		}
	}
}
