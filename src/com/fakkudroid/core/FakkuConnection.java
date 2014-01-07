package com.fakkudroid.core;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.util.Log;

import com.fakkudroid.bean.CommentBean;
import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.bean.URLBean;
import com.fakkudroid.bean.UserBean;
import com.fakkudroid.bean.VersionBean;
import com.fakkudroid.exception.ExceptionNotLoggedIn;
import com.fakkudroid.json.FakkuContent;
import com.fakkudroid.util.Constants;
import com.fakkudroid.util.Helper;
import com.google.gson.Gson;

public class FakkuConnection {

	private static CookieStore cookiesStore = null;

	public static boolean isConnected() {
		return cookiesStore != null;
	}

	public static void disconnect() {
		cookiesStore = null;
	}

	public static boolean connect(String user, String password)
			throws ClientProtocolException, IOException {
		if (cookiesStore != null)
			return true;

		boolean result = false;
		DefaultHttpClient httpclient = new DefaultHttpClient();

		HttpGet httpget = new HttpGet(Constants.SITELOGIN);

		HttpResponse response = httpclient.execute(httpget);
		HttpEntity entity = response.getEntity();

		Log.d(new FakkuConnection().getClass().toString(), "Login form get: "
				+ response.getStatusLine());
		if (entity != null) {
			entity.consumeContent();
		}
		Log.d(new FakkuConnection().getClass().toString(),
				"Initial set of cookies:");
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

		Log.d(new FakkuConnection().getClass().toString(), "Login form get: "
				+ response.getStatusLine());
		if (entity != null) {
			entity.consumeContent();
		}

		cookies = httpclient.getCookieStore();
		if (cookies.getCookies().isEmpty()) {
			Log.d(new FakkuConnection().getClass().toString(),
					"Post logon cookies: None");
		} else {
			for (int i = 0; i < cookies.getCookies().size(); i++) {
				if (cookies.getCookies().get(i).getName()
						.equalsIgnoreCase("fakku_sid")) {
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

	public static void transaction(String url) throws ExceptionNotLoggedIn,
			IOException {
		if (cookiesStore == null)
			throw new ExceptionNotLoggedIn();

		String html = Helper.getHTML(url, cookiesStore);

		if (html.contains("Please enter your username and password to login"))
			throw new ExceptionNotLoggedIn();
	}

	public static LinkedList<CommentBean> parseComments(String url,
			Object[] moreComments) throws ClientProtocolException, IOException,
			URISyntaxException {
		LinkedList<CommentBean> result = new LinkedList<CommentBean>();

		String html = Helper.getHTML(url, cookiesStore);

		String token = "<div class=\"manga_ comment-row\">|<div class=\"reply_ comment-row\">|<div class=\"tree_ comment-row\">";
		html = html.replaceAll("<div class=\"manga_ comment-row\">",
				"<div class=\"manga_ comment-row\">manga");
		html = html.replaceAll("<div class=\"reply_ comment-row\">",
				"<div class=\"reply_ comment-row\">reply");
		html = html.replaceAll("<div class=\"tree_ comment-row\">",
				"<div class=\"tree_ comment-row\">tree");
		String[] sections = html.split(token);
		moreComments[0] = html.contains(">View More<");

		for (int i = 1; i < sections.length; i++) {
			String section = sections[i];
			int level = 0;
			if (section.startsWith("manga")) {
				level = 0;
			} else if (section.startsWith("reply")) {
				level = 1;
			} else if (section.startsWith("tree")) {
				level = 2;
			} else {
				continue;
			}
			CommentBean c = new CommentBean();
			c.setLevel(level);

			token = "<a id=\"";
			String value = "";
			int idxStart = section.indexOf(token) + token.length();
			int idxEnd = section.indexOf("\"", idxStart);
			value = section.substring(idxStart, idxEnd);

			c.setId(value);

			URLBean user = new URLBean();

			token = "<a href=\"";
			idxStart = section.indexOf(token) + token.length();
			idxEnd = section.indexOf("\"", idxStart);
			value = section.substring(idxStart, idxEnd);

			user.setUrl(Constants.SITEROOT + value);

			token = ">";
			idxStart = section.indexOf(token, idxStart) + token.length();
			idxEnd = section.indexOf("<", idxStart);
			value = section.substring(idxStart, idxEnd);

			user.setDescription(value);

			c.setUser(user);

			token = "<span itemprop=\"commentTime\">";
			idxStart = section.indexOf(token) + token.length();
			idxEnd = section.indexOf("<", idxStart);
			value = section.substring(idxStart, idxEnd);

			c.setDate(value);

			idxStart = section.indexOf("class=\"rank\"");

			token = "href=\"";
			idxStart = section.indexOf(token, idxStart) + token.length();
			idxEnd = section.indexOf("\"", idxStart);
			value = section.substring(idxStart, idxEnd);

			c.setUrlLike(value);

			token = "href=\"";
			idxStart = section.indexOf(token, idxStart) + token.length();
			idxEnd = section.indexOf("\"", idxStart);
			value = section.substring(idxStart, idxEnd);

			c.setUrlDislike(value);

			if (section.contains("class=\"arrow selected like\"")) {
				c.setSelectLike(1);
			} else if (section.contains("class=\"arrow selected dislike\"")) {
				c.setSelectLike(-1);
			}

			token = "<i class=\"plus\">";
			if (!section.contains(token)) {
				token = "<i class=\"minus\">";
			}
			if (section.contains(token)) {
				idxStart = section.indexOf(token) + token.length();
				idxEnd = section.indexOf(" ", idxStart);
				value = section.substring(idxStart, idxEnd);

				if (value.startsWith("+"))
					c.setRank(Integer.parseInt(value.substring(1)));
				else
					c.setRank(Integer.parseInt(value));
			}

			token = "comment_text\" itemprop=\"commentText\">";
			idxStart = section.indexOf(token) + token.length();
			idxEnd = section.indexOf("</div>", idxStart);
			value = section.substring(idxStart, idxEnd);

			c.setComment(value);

			result.add(c);
		}

		return result;
	}

	public static VersionBean getLastversion() throws IOException {
		VersionBean result = null;
		String html = Helper.getHTML(Constants.UPDATE_SERVICE);
		if (!html.equals("null")) {
			Gson gson = new Gson();
			result = gson.fromJson(html, VersionBean.class);
		}
		return result;
	}

	public static LinkedList<DoujinBean> parseHTMLCatalog(String url)
			throws ClientProtocolException, IOException, URISyntaxException {
		LinkedList<DoujinBean> result = new LinkedList<DoujinBean>();

		String html = Helper.getHTML(url);

        Helper.logInfo("parseHTMLCatalog : " + url, html);

		String token = "content-row manga|content-row doujinshi";
		String[] sections = html.split(token);
		for (int i = 1; i < sections.length; i++) {
			String section = sections[i];

			DoujinBean bean = new DoujinBean();

			int idxStart = -1;
			int idxEnd = -1;
			String s = "";

			// url
			token = "<a href=\"";
			idxStart = section.indexOf(token, idxStart) + token.length();
			idxEnd = section.indexOf("\"", idxStart);
			s = section.substring(idxStart, idxEnd);
			bean.setUrl(Constants.SITEROOT + s);

			// Images
			token = "src=\"";
			idxStart = section.indexOf(token, idxStart) + token.length();
			idxEnd = section.indexOf("\"", idxStart);
			s = section.substring(idxStart, idxEnd);
			bean.setUrlImageTitle(s);

			// Look for the next image tag
            token = "<img class=\"sample\"";
			idxStart = section.indexOf(token, idxStart) + token.length();
			token = "src=\"";
			idxStart = section.indexOf(token, idxStart) + token.length();
			idxEnd = section.indexOf("\"", idxStart);
			s = section.substring(idxStart, idxEnd);
			bean.setUrlImagePage(s);

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
			idxEnd = section.indexOf(token, idxStart) + token.length();
			s = section.substring(idxStart, idxEnd);
			bean.setSerie(parseURLBean(s.trim()));

			// artist
			token = "<h3>Artist:</h3>";
			idxStart = section.indexOf(token, idxStart) + token.length();
			idxStart = section.indexOf("<a", idxStart);
			token = "</a>";
			idxEnd = section.indexOf(token, idxStart) + token.length();
			s = section.substring(idxStart, idxEnd);
			bean.setArtist(parseURLBean(s.trim()));

			// description
			token = "<h3>Description:</h3>";
			idxStart = section.indexOf(token, idxStart) + token.length();
			idxEnd = section.indexOf("</div>", idxStart);
			s = section.substring(idxStart, idxEnd);
			bean.setDescription(s);

			// tags
			List<URLBean> lstTags = new ArrayList<URLBean>();

            try {
                token = "Tags:";
                idxStart = section.indexOf(token) + token.length();
                token = "</div>";
                idxEnd = section.indexOf(token, idxStart);
                s = section.substring(idxStart, idxEnd);
                token = ",";

                for (String str : s.split(",")) {
                    idxStart = str.indexOf("<a");
                    token = "</a>";
                    idxEnd = str.indexOf(token, idxStart) + token.length();
                    String tag = str.substring(idxStart, idxEnd);

                    lstTags.add(parseURLBean(tag));
                }
            }catch (Exception e){}

			bean.setLstTags(lstTags);

			result.add(bean);
		}

		return result;
	}

	public static LinkedList<DoujinBean> parseHTMLFavorite(String url)
			throws ClientProtocolException, IOException, URISyntaxException {
		LinkedList<DoujinBean> result = new LinkedList<DoujinBean>();

		String html = Helper.getHTML(url);

        Helper.logInfo("parseHTMLFavorite : " + url, html);

		String token = "<div class=\"favorite ";
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
			token = "alt=\"";
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
/*
    public static DoujinBean parseJsonDoujin(DoujinBean result) throws IOException {
        FakkuContent fakkuContent = null;
        String url = result.getUrl();
        String html = Helper.getHTML(url);

        Gson gson = new Gson();
        fakkuContent = gson.fromJson(html, FakkuContent.class);

        if(fakkuContent!=null){
            result.setUrl(url);
            result.setQtyPages(fakkuContent.getPages());
            result.setQtyFavorites(fakkuContent.getFavorites());
            result.setUrlImageTitle(Constants.SITEROOT + fakkuContent.getCover());
            result.setUrlImagePage(Constants.SITEROOT + fakkuContent.getSample());
            result.setTitle(fakkuContent.getName());
            result.setDescription(fakkuContent.getDescription());
            if(cookiesStore!=null){
                String htmlComplete = Helper.getHTML(url, cookiesStore);
                result.setAddedInFavorite(!html.contains("Add To Favorites"));
            }
            result.setDate(Helper.formatterDate(new Date(fakkuContent.getDate())));
            result.setSerie(fakkuContent.getSeries().parseURLBean());
            result.setArtist(fakkuContent.getArtists().parseURLBean());
            result.setTranslator(fakkuContent.getTranslators().parseURLBean());
            result.setLstTags(fakkuContent.getTags().parseListURLBean());
            result.setUploader(fakkuContent.parseUploader());
            result.setLanguage(fakkuContent.parseLanguage());
            result.setImageServer(fakkuContent.imageServer());
        }

        return result;
    }*/

	public static void parseHTMLDoujin(DoujinBean bean)
			throws ClientProtocolException, IOException {
		String url = bean.getUrl();

		String html = Helper.getHTML(url, cookiesStore);

        Helper.logInfo("parseHTMLDoujin : " + url, html);

		bean.setAddedInFavorite(!html.contains("Add To Favorites"));

		// Qty Pages
		String token = "</b> pages";
		int idxStart = html.indexOf(token);
		token = "<b>";
		idxStart = html.substring(0, idxStart).lastIndexOf(token)
				+ token.length();
		int idxEnd = html.indexOf("<", idxStart);
		String s = html.substring(idxStart, idxEnd);

		int c = 0;
		try {
			c = Integer.parseInt(s.replace(",", "").trim());
		} catch (Exception e) {

		}

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

		// URL
		token = "<div class=\"wrap\">";
		idxStart = html.indexOf(token) + token.length();
		token = "<a href=\"";
		idxStart = html.indexOf(token, idxStart) + token.length();
		idxEnd = html.indexOf("\"", idxStart);

		s = html.substring(idxStart, idxEnd);
		s = Constants.SITEROOT + s;
		idxEnd = s.lastIndexOf("/");
		s = s.substring(0, idxEnd);
		bean.setUrl(s);

		// Images
		token = "<img ";
		idxStart = html.indexOf(token) + token.length();
		token = "src=\"";
		idxStart = html.indexOf(token, idxStart) + token.length();
		idxEnd = html.indexOf("\"", idxStart);
		s = html.substring(idxStart, idxEnd);

		bean.setUrlImageTitle(s);

		token = "<img class=\"sample\"";
		idxStart = html.indexOf(token, idxStart) + token.length();
		token = "src=\"";
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
		idxEnd = html.indexOf("</div>", idxStart);
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
		bean.setUploader(parseUserBean(s.trim()));

		// date
		token = "<b>";
		idxStart = html.indexOf(token, idxStart) + token.length();
		token = "</b>";
		idxEnd = html.indexOf(token, idxStart);
		s = html.substring(idxStart, idxEnd);
		bean.setDate(s);

		// tags
        List<URLBean> lstTags = new ArrayList<URLBean>();

        try{
            token = "Tags:";
            idxStart = html.indexOf(token) + token.length();
            token = "</div>";
            idxEnd = html.indexOf(token, idxStart);
            s = html.substring(idxStart, idxEnd);
            token = ",";

            for (String str : s.split(",")) {
                idxStart = str.indexOf("<a");
                token = "</a>";
                idxEnd = str.indexOf(token, idxStart) + token.length();
                String tag = str.substring(idxStart, idxEnd);

                lstTags.add(parseURLBean(tag));
            }
        }catch (Exception e){}

		bean.setLstTags(lstTags);

		//Get imageServer link
		html = Helper.getHTML(bean.getUrl() + "/read#page=1");

        Helper.logInfo("parseHTMLDoujin / imageServer : " + bean.getUrl() + "/read#page=1", html);

		token = "function imgpath(x)";
		idxStart = html.indexOf(token, idxStart) + token.length();
		token = "return";
		idxStart = html.indexOf(token, idxStart) + token.length();
        token = "'";
        idxStart = html.indexOf(token, idxStart) + token.length();
		idxEnd = html.indexOf(token, idxStart) + token.length();
		s = html.substring(idxStart, idxEnd-1);
		bean.setImageServer(s);
		
		//Get download link
        /*
		html = Helper.getHTML(bean.getUrl() + "/download");
		token = "<div class=\"download-row\"><span>fu</span> ";
		idxStart = html.indexOf(token, idxStart) + token.length();
		
		if(idxStart!=-1){
			token = "href=\"";
			idxStart = html.indexOf(token, idxStart) + token.length();
			token = "\"";
			idxEnd = html.indexOf(token, idxStart) + token.length();
			s = html.substring(idxStart, idxEnd-1);
			
			bean.setUrlDownload(s);
		}*/

        bean.setCompleted(true);
	}

	public static LinkedList<URLBean> parseHTMLTagsList(String url)
			throws IOException {
		LinkedList<URLBean> result = new LinkedList<URLBean>();
		String html = Helper.getHTML(url);

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
			b.setUrl("http://" + section.substring(idxStart, idxEnd));

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
		String html = Helper.getHTML(url);

		String token = "attribute-row";
		String[] sections = html.split(token);

		for (int i = 1; i < sections.length; i++) {
			String section = sections[i];
			token = "href=\"";

			int idxStart = section.indexOf(token) + token.length();
			int idxEnd = section.indexOf("\"", idxStart);

			URLBean b = new URLBean();
			b.setUrl(Constants.SITEROOT + section.substring(idxStart, idxEnd));

			token = ">";

			idxStart = section.indexOf(token) + token.length();
			idxEnd = section.indexOf("<", idxStart);

			b.setDescription(section.substring(idxStart, idxEnd));

			Log.i("HTMLParser", b.toString());

			result.add(b);
		}

		return result;
	}

	private static URLBean parseURLBean(String a) {
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

		b.setUrl(Constants.SITEROOT + s.trim());

		return b;
	}

    private static UserBean parseUserBean(String a) {
        UserBean b = new UserBean();
        String token = ">";
        int idxStart = a.indexOf(token) + token.length();
        token = "<";
        int idxEnd = a.indexOf(token, idxStart);
        String s = a.substring(idxStart, idxEnd);

        b.setUser(s.trim());

        token = "href=\"";
        idxStart = a.indexOf(token) + token.length();
        token = "/users/";
        idxStart = a.indexOf(token, idxStart) + token.length();
        token = "\"";
        idxEnd = a.indexOf(token, idxStart);
        s = a.substring(idxStart, idxEnd);

        b.setUrlUser(s.trim());

        return b;
    }
}
