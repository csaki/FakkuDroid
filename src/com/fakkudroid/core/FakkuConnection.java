package com.fakkudroid.core;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import  org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.util.Log;

import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.bean.URLBean;
import com.fakkudroid.util.Constants;
import com.fakkudroid.util.Util;

public class FakkuConnection {

	private static CookieStore cookiesStore = null;

	public static boolean isConnected(){
		return cookiesStore!=null;
	}
	
	public static boolean connect(String user, String password)
			throws ClientProtocolException, IOException {
		if(cookiesStore!=null)
			return true;
		
		boolean result = false;
		DefaultHttpClient httpclient = new DefaultHttpClient();

		HttpGet httpget = new HttpGet(Constants.SITELOGIN);

		HttpResponse response = httpclient.execute(httpget);
		HttpEntity entity = response.getEntity();

		Log.d(new FakkuConnection().getClass().toString(),
				"Login form get: " + response.getStatusLine());
		if (entity != null) {
			entity.consumeContent();
		}
		Log.d(new FakkuConnection().getClass().toString(), "Initial set of cookies:");
		CookieStore cookies = httpclient.getCookieStore();

		HttpPost httpost = new HttpPost(Constants.SITELOGIN);

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("username", user)); // set your own
															// username
		nvps.add(new BasicNameValuePair("password", password)); // set your own
																// password

		httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

		response = httpclient.execute(httpost);
		entity = response.getEntity();

		Log.d(new FakkuConnection().getClass().toString(),
				"Login form get: " + response.getStatusLine());
		if (entity != null) {
			entity.consumeContent();
		}

		cookies = httpclient.getCookieStore();
		if (cookies.getCookies().isEmpty()) {
			Log.d(new FakkuConnection().getClass().toString(), "Post logon cookies: None");
		} else {
			for (int i = 0; i < cookies.getCookies().size(); i++) {
				if (cookies.getCookies().get(i).getName().equalsIgnoreCase("NEWFAKKU_sid")
						|| cookies.getCookies().get(i).getName()
								.equalsIgnoreCase("NEWFAKKU_data")) {
					result = true;
				}
			}
		}
		if (!result)
			cookiesStore = null;
		else
			cookiesStore = cookies;
		return result;
	}

	public static void removeFromFavorites(DoujinBean doujinBean) throws ExceptionNotLoggedIn, IOException{
		if(cookiesStore==null)
			throw new ExceptionNotLoggedIn();
		
		String html = Util.getHTML(doujinBean.urlFavorite(Constants.SITEREMOVEFAVORITE), cookiesStore);
		
		if(html.contains("Please enter your username and password to login"))
			throw new ExceptionNotLoggedIn();
	}

	public static void addToFavorites(DoujinBean doujinBean) throws ExceptionNotLoggedIn, IOException{
		if(cookiesStore==null)
			throw new ExceptionNotLoggedIn();
		
		String html = Util.getHTML(doujinBean.urlFavorite(Constants.SITEADDFAVORITE), cookiesStore);
		
		if(html.contains("Please enter your username and password to login"))
			throw new ExceptionNotLoggedIn();
	}
	
	public static LinkedList<DoujinBean> parseHTMLCatalog(String url)
			throws ClientProtocolException, IOException, URISyntaxException {
		LinkedList<DoujinBean> result = new LinkedList<DoujinBean>();

		String html = Util.getHTML(url);

		String token = "content-row manga|content-row doujinshi";
		String[] sections = html.split(token);
		for (int i = 1; i < sections.length; i++) {
			String section = sections[i];

			DoujinBean bean = new DoujinBean();

			int idxStart = -1;
			int idxEnd = -1;

			// Images
			String s = "";
			token = "src=\"";
			idxStart = section.indexOf(token) + token.length();
			idxEnd = section.indexOf("\"", idxStart);
			s = section.substring(idxStart, idxEnd);

			bean.setUrlImageTitle(s);

			idxStart = section.indexOf(token, idxStart) + token.length();
			idxEnd = section.indexOf("\"", idxStart);
			s = section.substring(idxStart, idxEnd);
			bean.setUrlImagePage(s);

			// url
			token = "<a href=\"";
			idxStart = section.indexOf(token, idxStart) + token.length();
			idxEnd = section.indexOf("\"", idxStart);
			s = section.substring(idxStart, idxEnd);
			bean.setUrl(Constants.SITEROOT + s);

			// title
			token = "title=\"";
			idxStart = section.indexOf(token, idxStart) + token.length();
			idxEnd = section.indexOf("\"", idxStart);
			s = section.substring(idxStart, idxEnd);
			bean.setTitle(s);

			// serie
			token = "<h3>Series:</h3>";
			idxStart = section.indexOf(token, idxStart) + token.length();
			idxStart = section.indexOf("<a", idxStart);
			token = "</a>";
			idxEnd = section.indexOf(token, idxStart) +token.length();
			s = section.substring(idxStart, idxEnd);
			bean.setSerie(parseURLBean(s.trim()));

			// artist
			token = "<h3>Artist:</h3>";
			idxStart = section.indexOf(token, idxStart) + token.length();
			idxStart = section.indexOf("<a", idxStart);
			token = "</a>";
			idxEnd = section.indexOf(token, idxStart) +token.length();
			s = section.substring(idxStart, idxEnd);
			bean.setArtist(parseURLBean(s.trim()));

			// description
			token = "<h3>Description:</h3>";
			idxStart = section.indexOf(token, idxStart) + token.length();
			idxEnd = section.indexOf("</div>", idxStart);
			s = section.substring(idxStart, idxEnd);
			bean.setDescription(s);

			// tags
			token = "Tags:";
			idxStart = section.indexOf(token) + token.length();
			token = "</div>";
			idxEnd = section.indexOf(token, idxStart);
			s = section.substring(idxStart, idxEnd);
			token = ",";
			
			List<URLBean> lstTags = new ArrayList<URLBean>();
			
			for (String str : s.split(",")) {
				idxStart = str.indexOf("<a");
				token = "</a>";
				idxEnd = str.indexOf(token, idxStart) + token.length();
				String tag = str.substring(idxStart, idxEnd);
				
				lstTags.add(parseURLBean(tag));
			}
			bean.setLstTags(lstTags);
			
			Log.i("HTMLParser", "Read data of: " + bean);

			result.add(bean);
		}

		return result;
	}
	
	public static LinkedList<DoujinBean> parseHTMLFavorite(String url)
			throws ClientProtocolException, IOException, URISyntaxException {
		LinkedList<DoujinBean> result = new LinkedList<DoujinBean>();

		String html = Util.getHTML(url);

		String token = "<div class=\"favorite\">";
		String[] sections = html.split(token);
		for (int i = 1; i < sections.length; i++) {
			String section = sections[i];

			DoujinBean bean = new DoujinBean();

			int idxStart = -1;
			int idxEnd = -1;

			// Images
			String s = "";
			token = "src=\"";
			idxStart = section.indexOf(token) + token.length();
			idxEnd = section.indexOf("\"", idxStart);
			s = section.substring(idxStart, idxEnd);

			bean.setUrlImageTitle(s);

			s = "";
			token = "title=\"";
			idxStart = section.indexOf(token) + token.length();
			idxEnd = section.indexOf("\"", idxStart);
			s = section.substring(idxStart, idxEnd);

			bean.setTitle(s);
			
			s = "";
			token = "href=\"";
			idxStart = section.indexOf(token) + token.length();
			idxEnd = section.indexOf("\"", idxStart);
			s = Constants.SITEROOT + section.substring(idxStart, idxEnd);

			bean.setUrl(s);
			
			result.add(bean);
		}

		return result;
	}

	public static void parseHTMLDoujin(DoujinBean bean)
			throws ClientProtocolException, IOException {
		String url = bean.getUrl();

		String html = Util.getHTML(url, cookiesStore);

		bean.setAddedInFavorite(!html.contains("Add To Favorites"));
		
		html = Util.getHTML(url);
		// Qty Pages
		String token = " pages ";
		int idxStart = html.indexOf(token);
		token = "<b>";
		idxStart = html.lastIndexOf(token, idxStart) + token.length();
		int idxEnd = html.indexOf("<", idxStart);
		String s = html.substring(idxStart, idxEnd);

		int c = Integer.parseInt(s.replace(",", "").trim());

		bean.setQtyPages(c);

		// Qty favorites
		token = " favorites,";
		idxStart = html.indexOf(token);
		token = ">";
		idxStart = html.lastIndexOf(token, idxStart) + token.length();
		token = " favorites,";
		idxEnd = html.indexOf(token, idxStart);
		s = html.substring(idxStart, idxEnd);

		c = Integer.parseInt(s.replace(",", "").trim());

		bean.setQtyFavorites(c);
		
		// Images
		token = "<img class=\"cover\"";
		idxStart = html.indexOf(token) + token.length();
		token = "src=\"";
		idxStart = html.indexOf(token, idxStart) + token.length();
		idxEnd = html.indexOf("\"", idxStart);
		s = html.substring(idxStart, idxEnd);

		bean.setUrlImageTitle(s);

		idxStart = html.indexOf(token, idxStart) + token.length();
		idxEnd = html.indexOf("\"", idxStart);
		s = html.substring(idxStart, idxEnd);
		bean.setUrlImagePage(s);

		// title
		token = "<h1 itemprop=\"name\">";
		idxStart = html.indexOf(token) + token.length();
		idxEnd = html.indexOf("<", idxStart);
		s = html.substring(idxStart, idxEnd);
		bean.setTitle(s);

		// serie
		token = "<div class=\"left\">Series:";
		idxStart = html.indexOf(token, idxStart) + token.length();
		idxStart = html.indexOf("<a", idxStart);
		token = "</a>";
		idxEnd = html.indexOf(token, idxStart) + token.length();
		s = html.substring(idxStart, idxEnd);
		bean.setSerie(parseURLBean(s.trim()));

		// language
		token = "Language:";
		idxStart = html.indexOf(token) + token.length();
		idxStart = html.indexOf("<a", idxStart);
		token = "</a>";
		idxEnd = html.indexOf(token, idxStart) + token.length();
		s = html.substring(idxStart, idxEnd);
		bean.setLanguage(parseURLBean(s.trim()));
		
		// artist
		token = "<div class=\"left\">Artist:";
		idxStart = html.indexOf(token) + token.length();
		idxStart = html.indexOf("<a", idxStart);
		token = "</a>";
		idxEnd = html.indexOf(token, idxStart) + token.length();
		s = html.substring(idxStart, idxEnd);
		bean.setArtist(parseURLBean(s.trim()));

		// description
		token = "<b>Description:</b>";
		idxStart = html.indexOf(token) + token.length();
		idxEnd = html.indexOf("<", idxStart);
		s = html.substring(idxStart, idxEnd);
		bean.setDescription(s.trim());
		
		// translator
		token = "Translator: ";
		idxStart = html.indexOf(token) + token.length();
		idxStart = html.indexOf("<a", idxStart);
		token = "</a>";
		idxEnd = html.indexOf(token, idxStart) + token.length();
		s = html.substring(idxStart, idxEnd);
		bean.setTranslator(parseURLBean(s.trim()));
		
		// Uploaded by
		token = "uploaded by";
		idxStart = html.indexOf(token) + token.length();
		idxStart = html.indexOf("<a", idxStart);
		token = "</a>";
		idxEnd = html.indexOf(token, idxStart) + token.length();
		s = html.substring(idxStart, idxEnd);
		bean.setUploader(parseURLBean(s.trim()));
		
		// fecha
		token = "<b>";
		idxStart = html.indexOf(token, idxStart) + token.length();
		token = "</b>";
		idxEnd = html.indexOf(token, idxStart);
		s = html.substring(idxStart, idxEnd);
		bean.setFecha(s);
		
		// tags
		token = "Tags:";
		idxStart = html.indexOf(token) + token.length();
		token = "</div>";
		idxEnd = html.indexOf(token, idxStart);
		s = html.substring(idxStart, idxEnd);
		token = ",";
		
		List<URLBean> lstTags = new ArrayList<URLBean>();
		
		for (String str : s.split(",")) {
			idxStart = str.indexOf("<a");
			token = "</a>";
			idxEnd = str.indexOf(token, idxStart) + token.length();
			String tag = str.substring(idxStart, idxEnd);
			
			lstTags.add(parseURLBean(tag));
		}
		bean.setLstTags(lstTags);
		
		idxStart = html.indexOf(token, idxStart) + token.length();
	}

	public static LinkedList<URLBean> parseHTMLTagsList(String url)
			throws IOException {
		LinkedList<URLBean> result = new LinkedList<URLBean>();
		String html = Util.getHTML(url);

		String token = "<strong>Tags:</strong>";
		int idxStart = html.indexOf(token);
		token = "<ul id=\"tag-list\">";
		int idxEnd = html.indexOf(token);

		String section = html.substring(idxStart, idxEnd);

		token = "<a href=";
		String[] sections = section.split(token);

		for (int i = 1; i < sections.length; i++) {
			section = sections[i];
			token = "<a href=\"";

			idxStart = section.indexOf(token) + token.length();
			idxEnd = section.indexOf("\"", idxStart);

			URLBean b = new URLBean();
			b.setUrl("http://"+section.substring(idxStart, idxEnd));

			token = ">";

			idxStart = section.indexOf(token) + token.length();
			idxEnd = section.indexOf("<", idxStart);

			b.setDescription(section.substring(idxStart, idxEnd).replace("\\",
					""));

			Log.i("HTMLParser", b.toString());

			result.add(b);
		}

		return result;
	}

	public static LinkedList<URLBean> parseHTMLSeriesList(String url)
			throws IOException {
		LinkedList<URLBean> result = new LinkedList<URLBean>();
		String html = Util.getHTML(url);

		String token = "series_wrap";
		String[] sections = html.split(token);

		for (int i = 1; i < sections.length; i++) {
			String section = sections[i];
			token = "<a href=\"";

			int idxStart = section.indexOf(token) + token.length();
			int idxEnd = section.indexOf("\"", idxStart);

			URLBean b = new URLBean();
			b.setUrl(Constants.SITEROOT + section.substring(idxStart, idxEnd));

			token = "title=\"";

			idxStart = section.indexOf(token) + token.length();
			idxEnd = section.indexOf("\"", idxStart);

			b.setDescription(section.substring(idxStart, idxEnd));

			Log.i("HTMLParser", b.toString());

			result.add(b);
		}

		return result;
	}
	
	private static URLBean parseURLBean(String a){		
		URLBean b = new URLBean();
		String token = ">";
		int idxStart = a.indexOf(token) + token.length();
		token = "<";
		int idxEnd = a.indexOf(token, idxStart);
		String s = a.substring(idxStart, idxEnd);
		
		b.setDescription(s.trim());
		
		token = "href=\"";
		idxStart = a.indexOf(token) + token.length();
		token = "\"";
		idxEnd = a.indexOf(token, idxStart);
		s = a.substring(idxStart, idxEnd);
		
		b.setUrl(Constants.SITEROOT+s.trim());
		
		return b;
	}

}
