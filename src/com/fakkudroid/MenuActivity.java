package com.fakkudroid;

import java.util.LinkedList;

import com.fakkudroid.adapter.URLListAdapter;
import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.bean.URLBean;
import com.fakkudroid.core.FakkuDroidApplication;
import com.fakkudroid.util.Constants;
import com.fakkudroid.R;

import android.os.Bundle;
import android.app.ListActivity;
import android.content.Intent;
import android.view.View;
import android.widget.ListView;

public class MenuActivity extends ListActivity {

	public static String INTENT_VAR_URL = "itentVarUrl";

	LinkedList<URLBean> lstMenu;
	private FakkuDroidApplication app;
	String site;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);

		app = (FakkuDroidApplication) getApplication();

		Intent it = getIntent();
		site = it.getStringExtra(INTENT_VAR_URL);

		String title = "";
		if (site.equals(Constants.SITEDOUJINSHI)) {
			title = getResources().getString(R.string.doujinshis);
		}else{
			title = getResources().getString(R.string.mangas);
		}
		setTitle(title);
		createListUrl();
		
		this.setListAdapter(new URLListAdapter(this, R.layout.row_url, 0,lstMenu));
	}
	

	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		URLBean urlBean = lstMenu.get(position);
		
		String url = urlBean.getUrl();
		String title = urlBean.getDescription();
		
		
		Intent it = null;
		
		if(url.equals(site + Constants.SORTSERIES)||url.equals(site + Constants.SORTARTIST)){
			it = new Intent(MenuActivity.this,
					ListOptionActivity.class);
			it.putExtra(ListOptionActivity.INTENT_VAR_TITLE, title);
			it.putExtra(ListOptionActivity.INTENT_VAR_URL, url);
			it.putExtra(ListOptionActivity.INTENT_VAR_TYPE_LIST, ListOptionActivity.INTENT_VAR_TYPE_LIST_SERIES);
		}else if(url.equals(site + Constants.SORTTAGS)){
			it = new Intent(MenuActivity.this,
					ListOptionActivity.class);
			it.putExtra(ListOptionActivity.INTENT_VAR_TITLE, title);
			it.putExtra(ListOptionActivity.INTENT_VAR_URL, url);
			it.putExtra(ListOptionActivity.INTENT_VAR_TYPE_LIST, ListOptionActivity.INTENT_VAR_TYPE_LIST_TAGS);
		}else if(url.equals(site + Constants.RANDOM)){
			it = new Intent(MenuActivity.this,
					DoujinActivity.class);
			DoujinBean bean = new DoujinBean();
			bean.setUrl(url);
			app.setCurrent(bean);			
		}else{
			it = new Intent(MenuActivity.this,
					DoujinListActivity.class);	
			it.putExtra(DoujinListActivity.INTENT_VAR_TITLE, title);
			it.putExtra(DoujinListActivity.INTENT_VAR_URL, url);		
		}
		MenuActivity.this.startActivity(it);
	}

	public void createListUrl() {
		lstMenu = new LinkedList<URLBean>();

		String url = site + Constants.SORTNEWEST;
		String description = getResources().getString(R.string.sortByNewest);
		URLBean b = new URLBean(url, description);
		lstMenu.add(b);

		url = site + Constants.SORTENGLISH;
		description = getResources().getString(R.string.sortByEnglish);
		b = new URLBean(url, description);
		lstMenu.add(b);
		
		url = site + Constants.SORTTAGS;
		description = getResources().getString(R.string.sortByTags);
		b = new URLBean(url, description);
		lstMenu.add(b);

		if (site.equals(Constants.SITEDOUJINSHI)) {
			url = site + Constants.SORTSERIES;
			description = getResources().getString(R.string.sortBySeries);
			b = new URLBean(url, description);
			lstMenu.add(b);
		}
		
		url = site + Constants.SORTARTIST;
		description = getResources().getString(R.string.sortByArtist);
		b = new URLBean(url, description);
		lstMenu.add(b);

		url = site + Constants.MOSTCONTROVERSIAL;
		description = getResources().getString(R.string.mostControversial);
		b = new URLBean(url, description);
		lstMenu.add(b);

		url = site + Constants.MOSTFAVORITES;
		description = getResources().getString(R.string.mostFavorites);
		b = new URLBean(url, description);
		lstMenu.add(b);

		url = site + Constants.MOSTPOPULAR;
		description = getResources().getString(R.string.mostPopular);
		b = new URLBean(url, description);
		lstMenu.add(b);

		url = site + Constants.RANDOM;
		description = getResources().getString(R.string.random);
		b = new URLBean(url, description);
		lstMenu.add(b);
	}
}
