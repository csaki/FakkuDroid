package com.fakkudroid.core;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import static org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

	public static LinkedList<CommentBean> parseComments(String url) throws IOException,
			URISyntaxException {

		String html = Helper.getHTMLCORS(url);

        Document doc = Jsoup.parse(html);

		return parseHTMLtoComments(doc.select("div.comment-row"));
	}

    public static LinkedList<CommentBean> parseHTMLtoComments(Elements comments){
        LinkedList<CommentBean> result = new LinkedList<CommentBean>();

        for (Element comment:comments){
            int level = 0;
            if(comment.hasClass("comment-tree")){
                level = 2;
            }else if(comment.hasClass("comment-reply")){
                level = 1;
            }else if(comment.hasClass("comment-")){
                level = 0;
            }else{
                continue;
            }

            CommentBean c = new CommentBean();
            c.setLevel(level);

            c.setId(comment.select("a").first().id());
            URLBean user = new URLBean();
            Element userCreator = comment.select("[itemprop=creator]").first();
            user.setUrl(Constants.SITEROOT + userCreator.attr("href"));
            user.setDescription(userCreator.text());

            c.setUser(user);


            c.setDate(comment.select("[itemprop=commentTime]").first().text());

            Element likeA = comment.select("a.arrow").select(".like").first();
            Element disLikeA = comment.select("a.arrow").select(".dislike").first();

            c.setUrlLike(likeA.attr("href"));
            c.setUrlDislike(disLikeA.attr("href"));

            if(likeA.hasClass("selected")) {
                c.setSelectLike(1);
            }else if(disLikeA.hasClass("selected")) {
                c.setSelectLike(-1);
            }

            Element rank = comment.select("i").first();
            if(rank!=null)
                c.setRank(Integer.parseInt(rank.text().replace("+","").replace(" points","")));

            c.setComment(comment.select("div.comment_text").first().text());
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
			throws IOException, URISyntaxException {
		LinkedList<DoujinBean> result = new LinkedList<DoujinBean>();

		String html = Helper.getHTMLCORS(url);

        Helper.logInfo("parseHTMLCatalog : " + url, html);

        Document doc = Jsoup.parse(html);

        Elements elements = doc.select(".content-row");
		for (Element e : elements) {
            if(e.hasClass("manga")||e.hasClass("doujinshi")){
                DoujinBean bean = new DoujinBean();

                // url
                bean.setUrl(Constants.SITEROOT + e.select("a").first().attr("href"));

                // Images
                Elements elementsAux = e.select("img");
                bean.setUrlImageTitle(elementsAux.select(".cover").first().attr("src"));

                // Look for the next image tag
                bean.setUrlImagePage(elementsAux.select(".sample").first().attr("src"));

                // title
                bean.setTitle(e.select(".content-title").first().text());

                elementsAux = e.select(".row").select("a");
                // serie
                bean.setSerie(parseURLBean(elementsAux.get(0)));
                // language
                bean.setLanguage(parseURLBean(elementsAux.get(1)));
                // artist
                bean.setArtist(parseURLBean(elementsAux.get(2)));
                // translator
                bean.setTranslator(parseURLBean(elementsAux.get(3)));

                // description
                bean.setDescription(e.select(".short").first().text().substring(14));

                // tags
                List<URLBean> lstTags = new ArrayList<URLBean>();

                try {
                    elementsAux = e.select(".short").select("a");

                    for (Element tag : elementsAux) {
                        lstTags.add(parseURLBean(tag));
                    }
                }catch (Exception ex){}

                bean.setLstTags(lstTags);

                result.add(bean);
            }
		}

		return result;
	}

	public static LinkedList<DoujinBean> parseHTMLFavorite(String url)
			throws IOException, URISyntaxException {
		LinkedList<DoujinBean> result = new LinkedList<DoujinBean>();

		String html = Helper.getHTMLCORS(url);

        Document doc = Jsoup.parse(html);

        Helper.logInfo("parseHTMLFavorite : " + url, html);

        Elements favorites = doc.select(".favorite");
		for (Element favorite : favorites) {
			DoujinBean bean = new DoujinBean();

			// Images
            Element img = favorite.select("img").first();
			bean.setUrlImageTitle(img.attr("src"));

			bean.setTitle(img.attr("alt"));

			bean.setUrl(Constants.SITEROOT + favorite.select(".cover").first().attr("href"));

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
			throws IOException {
		String url = bean.getUrl();

		String html = Helper.getHTML(url, cookiesStore);

        Helper.logInfo("parseHTMLDoujin : " + url, html);

        Document doc = Jsoup.parse(html);

        bean.setAddedInFavorite(!html.contains("Add To Favorites"));

		// Qty Pages

        Element el = doc.select("div.small").first().select("b").first();
		int c = 0;
		try {
			c = Integer.parseInt(el.text());
		} catch (Exception e) {}

		bean.setQtyPages(c);

		// Qty favorites
        el = doc.select("div.small").first().select("div.left").first();

        String aux = el.text();
        aux = aux.replace("\n","");
        c = aux.indexOf(' ');
        if(c!=-1)
            aux = aux.substring(0, c).trim();
        try {
            c = Integer.parseInt(aux);
        }catch (Exception e){
            c = 0;
        }
		bean.setQtyFavorites(c);

		// URL
        el = doc.select("ul.content-navigation").first().select("a").first();
		aux = Constants.SITEROOT + el.select("a").first().attr("href");
		bean.setUrl(aux);

        el = doc.select("div.images").first();
		// Images
		aux = el.select("img.cover").first().attr("src");
		bean.setUrlImageTitle(aux);

        aux = el.select("img.sample").first().attr("src");
		bean.setUrlImagePage(aux);

		// title
        el = doc.select("h1").first();
        aux = el.text();
		bean.setTitle(aux);

		// serie
        el = doc.select("div#right").first();

        Elements elements = el.select("a");
		bean.setSerie(parseURLBean(elements.get(0)));

		// language
		bean.setLanguage(parseURLBean(elements.get(1)));

		// artist
		bean.setArtist(parseURLBean(elements.get(2)));

        // translator
        bean.setTranslator(parseURLBean(elements.get(3)));

        // Uploaded by
        bean.setUploader(parseUserBean(elements.get(4)));

        // date
        bean.setDate(el.select("div.small").first().select("div.right").first().select("b").first().text());

		// description
        el = el.select("[itemprop=description]").first();
		bean.setDescription(el.text().substring(14));

		// tags
        List<URLBean> lstTags = new ArrayList<URLBean>();

        try{
            elements = doc.select("[itemprop=keywords]").first().select("a");

            for (Element a : elements) {
                lstTags.add(parseURLBean(a));
            }
        }catch (Exception e){}

		bean.setLstTags(lstTags);

        try{
            Elements comments = doc.select("div.comment-row");
            Elements topComments = new Elements();
            Elements recentComments = new Elements();
            for (Element comment : comments){
                if(comment.parent().hasClass("ajax-container")){
                    recentComments.add(comment);
                }else
                    topComments.add(comment);
            }
            bean.setLstTopComments(parseHTMLtoComments(topComments));
            bean.setLstRecentComments(parseHTMLtoComments(recentComments));
        }catch(Exception e){}

		//Get imageServer link
		html = Helper.getHTMLCORS(bean.getUrl() + "/read#page=1");

        Helper.logInfo("parseHTMLDoujin / imageServer : " + bean.getUrl() + "/read#page=1", html);

		String token = "function imgpath(x)";
		int idxStart = html.indexOf(token, 0) + token.length();
		token = "return";
		idxStart = html.indexOf(token, idxStart) + token.length();
        token = "'";
        idxStart = html.indexOf(token, idxStart) + token.length();
		int idxEnd = html.indexOf(token, idxStart) + token.length();
		String s = html.substring(idxStart, idxEnd-1);
		bean.setImageServer(s);
		
        bean.setCompleted(true);
	}

	public static LinkedList<URLBean> parseHTMLSeriesList(String url)
			throws IOException {
		LinkedList<URLBean> result = new LinkedList<URLBean>();
		String html = Helper.getHTMLCORS(url);

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

	private static URLBean parseURLBean(Element a) {
		URLBean b = new URLBean();

		b.setDescription(a.text());
		b.setUrl(Constants.SITEROOT + a.attr("href"));

		return b;
	}

    private static UserBean parseUserBean(Element a) {
        UserBean b = new UserBean();
        b.setUser(a.text());

        String href = a.attr("href");
        String token = "/users/";
        int idxStart = href.indexOf(token) + token.length();
        String s = href.substring(idxStart);

        b.setUrlUser(s.trim());

        return b;
    }
}
