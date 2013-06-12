package com.fakkudroid;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.ActionProvider;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.SubMenu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.fakkudroid.bean.VersionBean;
import com.fakkudroid.core.FakkuConnection;
import com.fakkudroid.fragment.DoujinFragment;
import com.fakkudroid.fragment.DoujinListFragment;
import com.fakkudroid.fragment.DownloadListFragment;
import com.fakkudroid.fragment.DownloadQueueListFragment;
import com.fakkudroid.fragment.FavoriteFragment;
import com.fakkudroid.fragment.MenuListFragment;
import com.fakkudroid.util.Constants;
import com.fakkudroid.util.Helper;

public class MainActivity extends SherlockFragmentActivity implements
		SearchView.OnQueryTextListener {

	public final static String INTENT_VAR_URL = "intentVarUrl";
	public final static String INTENT_VAR_TITLE = "intentVarTitle";
	public final static String INTENT_VAR_USER = "intentVarUser";
	public final static String INTENT_VAR_CURRENT_CONTENT = "intentVarCurrentContent";

	private DrawerLayout mDrawerLayout;
	private MenuListFragment frmMenu;
	private FavoriteFragment frmFavorite;
	private ActionBarDrawerToggle mDrawerToggle;
	private DoujinListFragment frmDoujinList;
	private DownloadListFragment frmDownloadListFragment;
	private DownloadQueueListFragment frmDownloadQueueListFragment;
	private DoujinFragment frmDoujinFragment;
	public static final int DOUJIN_LIST = 1;
	public static final int DOWNLOADS = 2;
	public static final int FAVORITES = 3;
	public static final int DOWNLOADS_QUEUE = 4;
	public static final int DOUJIN = 5;
	private static int currentContent = DOUJIN_LIST;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		// set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);

		// enable ActionBar app icon to behave as action to toggle nav drawer
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		Fragment frmCurrent = null;
		
		int tempCurrentContent = getIntent().getIntExtra(INTENT_VAR_CURRENT_CONTENT, -1);
		if(tempCurrentContent!=-1)
			currentContent = tempCurrentContent;
		
		if (currentContent == DOUJIN_LIST) {
			if (frmDoujinList == null){
				frmDoujinList = new DoujinListFragment();
				frmDoujinList.setMainActivity(this);
			}
			frmCurrent = frmDoujinList;
		} else if (currentContent == DOWNLOADS) {
			if (frmDownloadListFragment == null)
				frmDownloadListFragment = new DownloadListFragment();
			frmCurrent = frmDownloadListFragment;
		} else if (currentContent == FAVORITES) {
			if (frmFavorite == null)
				frmFavorite = new FavoriteFragment();
			frmCurrent = frmFavorite;
		}  else if (currentContent == DOWNLOADS_QUEUE) {
			if (frmDownloadQueueListFragment == null)
				frmDownloadQueueListFragment = new DownloadQueueListFragment();
			frmCurrent = frmDownloadQueueListFragment;
		}

		frmMenu = new MenuListFragment();
		frmMenu.setMainActivity(this);

		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.menu_frame, frmMenu)
				.commit();

		fragmentManager.beginTransaction()
				.replace(R.id.content_frame, frmCurrent).commit();

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description for accessibility */
		R.string.drawer_close /* "close drawer" description for accessibility */
		) {
			public void onDrawerClosed(View view) {
				supportInvalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				supportInvalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		Helper.executeAsyncTask(new CheckerVersion());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (currentContent == DOUJIN_LIST || currentContent == DOWNLOADS) {
			// Create the search view
			SearchView searchView = new SearchView(getSupportActionBar()
					.getThemedContext());
			searchView.setQueryHint(getResources().getText(R.string.search));
			searchView.setOnQueryTextListener(this);
			String hint = "";
			if (currentContent == DOUJIN_LIST)
				getResources().getText(R.string.search);
			else
				getResources().getText(R.string.search_by);
			menu.add(hint)
					.setIcon(R.drawable.action_search)
					.setActionView(searchView)
					.setShowAsAction(
							MenuItem.SHOW_AS_ACTION_IF_ROOM
									| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

		}
		if (currentContent == DOWNLOADS_QUEUE || currentContent == DOWNLOADS) {
			menu.add(Menu.NONE, R.string.go_other_list, Menu.NONE,R.string.go_other_list)
            .setIcon(R.drawable.content_import_export)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(getMenuItem(item))) {
			return true;
		}else if(item.getItemId()==R.string.go_other_list){
			if (currentContent == DOWNLOADS_QUEUE) {
				goToDownloads();
			}else if(currentContent == DOWNLOADS){
				goToDownloadsQueue();
			}
			supportInvalidateOptionsMenu();
		}
		return true;
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onQueryTextChange(String arg0) {
		String query = arg0.trim();
		if (currentContent != DOUJIN_LIST) {
			frmDownloadListFragment.search(query);
		}
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String arg0) {
		if (currentContent == DOUJIN_LIST) {
			String query = arg0.trim();
			String url, title;
			if (!query.equals("")) {
				String strSearch = getResources().getString(R.string.search);
				url = Constants.SITESEARCH + Helper.escapeURL(query.trim());
				title = strSearch + ": " + query.trim();
			} else {
				title = getResources().getString(R.string.app_name);
				url = Constants.SITEROOT;
			}
			loadPageDoujinList(title, url);
		}
		InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		im.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		return true;
	}

	public void nextPage(View view) {
		if (currentContent == DOUJIN_LIST)
			frmDoujinList.nextPage(view);
		else if (currentContent == DOWNLOADS)
			frmDownloadListFragment.nextPage(view);
		else
			frmFavorite.nextPage(view);
	}

	public void previousPage(View view) {
		if (currentContent == DOUJIN_LIST)
			frmDoujinList.previousPage(view);
		else if (currentContent == DOWNLOADS)
			frmDownloadListFragment.previousPage(view);
		else
			frmFavorite.previousPage(view);
	}

	public void viewInBrowser(View view) {
		if (currentContent == DOUJIN_LIST)
			frmDoujinList.viewInBrowser(view);
		else if(currentContent == DOUJIN)
			frmDoujinFragment.viewInBrowser(view);
		else
			frmFavorite.viewInBrowser(view);
	}
	
	public void read(View view) {
		frmDoujinFragment.read(view);
	}
	
	public void addOrRemoveFavorite(View view) {
		frmDoujinFragment.addOrRemoveFavorite(view);
	}

	public void refresh(View view) {
		if (currentContent == DOUJIN_LIST)
			frmDoujinList.refresh(view);
		else
			frmFavorite.refresh(view);
	}

	public void goToFavorites(String user) {
		currentContent = FAVORITES;
		mDrawerLayout.closeDrawers();
		if (frmFavorite == null)
			frmFavorite = new FavoriteFragment();
		frmFavorite.setUser(user);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, frmFavorite).commit();
	}

	public void goToDownloads() {
		currentContent = DOWNLOADS;
		mDrawerLayout.closeDrawers();
		setTitle(R.string.download);
		if (frmDownloadListFragment == null)
			frmDownloadListFragment = new DownloadListFragment();
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, frmDownloadListFragment).commit();
	}
	
	public void goToDownloadsQueue() {
		currentContent = DOWNLOADS_QUEUE;
		mDrawerLayout.closeDrawers();
		setTitle(R.string.download);
		if (frmDownloadQueueListFragment == null)
			frmDownloadQueueListFragment = new DownloadQueueListFragment();
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, frmDownloadQueueListFragment).commit();
	}
	
	public void goToDoujin() {
		currentContent = DOUJIN;
		mDrawerLayout.closeDrawers();
		if (frmDoujinFragment == null)
			frmDoujinFragment = new DoujinFragment();
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, frmDoujinFragment).commit();
	}

	public void loadPageDoujinList(String title, String url) {
		mDrawerLayout.closeDrawers();
		this.setTitle(title);

		if (frmDoujinList == null){
			frmDoujinList = new DoujinListFragment();
			frmDoujinList.setMainActivity(this);
		}
		frmDoujinList.setUrl(url);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, frmDoujinList).commit();
		if (currentContent == DOUJIN_LIST) {
			frmDoujinList.loadPage();
		}
		currentContent = DOUJIN_LIST;
	}

	private void relatedContent() {
		setTitle(R.string.related_content);
		if (frmDoujinList == null){
			frmDoujinList = new DoujinListFragment();
			frmDoujinList.setMainActivity(this);
		}
		frmDoujinList.setRelated(true);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, frmDoujinList).commit();
		if (currentContent == DOUJIN_LIST) {
			frmDoujinList.loadPage();
		}
		currentContent = DOUJIN_LIST;
	}

	class CheckerVersion extends AsyncTask<String, Float, VersionBean> {

		@Override
		protected VersionBean doInBackground(String... arg0) {
			VersionBean versionBean = null;
			try {
				versionBean = FakkuConnection.getLastversion();
			} catch (Exception e) {
				Helper.logError(this, "error in verifing if exists new version.", e);
			}
			return versionBean;
		}

		protected void onPostExecute(final VersionBean result) {
			if (result != null) {
				PackageInfo pInfo = null;
				try {
					pInfo = MainActivity.this.getPackageManager()
							.getPackageInfo(MainActivity.this.getPackageName(),
									0);
					String currentVersion = pInfo.versionName;
					if (currentVersion.compareToIgnoreCase(result
							.getVersion_code()) < 0) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								MainActivity.this);
						builder.setMessage(R.string.msg_new_version)
								.setPositiveButton(R.string.visit_page,
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface arg0,
													int arg1) {
												Intent itVersion = new Intent(
														Intent.ACTION_VIEW);
												itVersion.setData(Uri.parse(result
														.getVersion_url()));
												MainActivity.this
														.startActivity(itVersion);
											}
										})
								.setNegativeButton(R.string.remind_later, null)
								.show();
					}
				} catch (NameNotFoundException e) {
					Helper.logError(this, "error getting current version", e);
				}
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		frmMenu.createMainMenu();
		if (resultCode == 1) {
			relatedContent();
		} else if (resultCode == 2) {
			loadPageDoujinList(data.getStringExtra(INTENT_VAR_TITLE),
					data.getStringExtra(INTENT_VAR_URL));
		} else if (resultCode == 3) {
			goToFavorites(data.getStringExtra(INTENT_VAR_USER));
		}
	}
	
	public void relatedContent(View view) {
		Toast.makeText(this,
				getResources().getString(R.string.soon), Toast.LENGTH_SHORT)
				.show();
	}
	
	public void comments(View view) {
		Toast.makeText(this,
				getResources().getString(R.string.soon), Toast.LENGTH_SHORT)
				.show();
	}
	
	public void download(View view) {
		frmDoujinFragment.download(view);
	}

	private android.view.MenuItem getMenuItem(final MenuItem item) {
		return new android.view.MenuItem() {
			@Override
			public int getItemId() {
				return item.getItemId();
			}

			public boolean isEnabled() {
				return true;
			}

			@Override
			public boolean collapseActionView() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean expandActionView() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public ActionProvider getActionProvider() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public View getActionView() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public char getAlphabeticShortcut() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int getGroupId() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public Drawable getIcon() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Intent getIntent() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public ContextMenuInfo getMenuInfo() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public char getNumericShortcut() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int getOrder() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public SubMenu getSubMenu() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public CharSequence getTitle() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public CharSequence getTitleCondensed() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean hasSubMenu() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isActionViewExpanded() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isCheckable() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isChecked() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isVisible() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public android.view.MenuItem setActionProvider(
					ActionProvider actionProvider) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setActionView(View view) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setActionView(int resId) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setAlphabeticShortcut(char alphaChar) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setCheckable(boolean checkable) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setChecked(boolean checked) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setEnabled(boolean enabled) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setIcon(Drawable icon) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setIcon(int iconRes) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setIntent(Intent intent) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setNumericShortcut(char numericChar) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setOnActionExpandListener(
					OnActionExpandListener listener) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setOnMenuItemClickListener(
					OnMenuItemClickListener menuItemClickListener) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setShortcut(char numericChar,
					char alphaChar) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void setShowAsAction(int actionEnum) {
				// TODO Auto-generated method stub

			}

			@Override
			public android.view.MenuItem setShowAsActionFlags(int actionEnum) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setTitle(CharSequence title) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setTitle(int title) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setTitleCondensed(CharSequence title) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setVisible(boolean visible) {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}
}
