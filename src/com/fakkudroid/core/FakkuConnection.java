package com.fakkudroid.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;

import com.fakkudroid.bean.CommentBean;
import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.bean.DoujinListBean;
import com.fakkudroid.bean.URLBean;
import com.fakkudroid.bean.UserBean;
import com.fakkudroid.bean.VersionBean;
import com.fakkudroid.exception.ExceptionNotLoggedIn;
import com.fakkudroid.json.Attribute;
import com.fakkudroid.json.Content;
import com.fakkudroid.json.JSONListResult;
import com.fakkudroid.json.JSONContentResult;
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

    public static void connect(UserBean user)
            throws ClientProtocolException, IOException {
        if (cookiesStore != null) {
            user.setChecked(true);
            return;
        }
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

        HttpPost httpost = new HttpPost(Constants.SITELOGIN);

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("username", user.getUser())); // set your own
        // username
        nvps.add(new BasicNameValuePair("password", user.getPassword())); // set your own
        // password

        httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

        response = httpclient.execute(httpost);
        entity = response.getEntity();

        Log.d(new FakkuConnection().getClass().toString(), "Login form get: "
                + response.getStatusLine());

        CookieStore cookies = httpclient.getCookieStore();
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
        else {
            cookiesStore = cookies;

            InputStream is = null;
            try {
                is = entity.getContent();
                String html = "";
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder str = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    str.append(line);
                }
                html = str.toString();

                Document doc = Jsoup.parse(html);
                String url = doc.select("a#user-menu-favorites").first().select("a").first().attr("href").substring(7);
                url = url.substring(0, url.indexOf("/"));
                user.setUrlUser(url);
            } catch (Exception ex) {
            } finally {
                if (is != null)
                    is.close();
            }
        }
        user.setChecked(result);
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

        String html = Helper.getHTML(url);

        Document doc = Jsoup.parse(html);

        return parseHTMLtoComments(doc.select("div.comment-row"));
    }

    public static LinkedList<CommentBean> parseHTMLtoComments(Elements comments) {
        LinkedList<CommentBean> result = new LinkedList<CommentBean>();

        for (Element comment : comments) {
            int level = 0;
            if (comment.hasClass("comment-tree")) {
                level = 2;
            } else if (comment.hasClass("comment-reply")) {
                level = 1;
            } else if (comment.hasClass("comment-")) {
                level = 0;
            } else {
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

            c.setUrlLike(Constants.SITEROOT + likeA.attr("href"));
            c.setUrlDislike(Constants.SITEROOT + disLikeA.attr("href"));

            if (likeA.hasClass("selected")) {
                c.setSelectLike(1);
            } else if (disLikeA.hasClass("selected")) {
                c.setSelectLike(-1);
            }

            Element rank = comment.select("i").first();
            if (rank != null)
                c.setRank(Integer.parseInt(rank.text().replace("+", "").replace(" points", "")));

            c.setComment(comment.select("div.comment_text").first().html());
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

    public static DoujinListBean parseJSONCatalog(String url)
            throws IOException, URISyntaxException {
        DoujinListBean result = new DoujinListBean();
        LinkedList<DoujinBean> list = new LinkedList<DoujinBean>();

        result.setLstDoujin(list);

        String html = Helper.getHTML(url);

        JSONListResult jsonResult = new Gson().fromJson(html, JSONListResult.class);

        Helper.logInfo("parseJSONCatalog : " + url, html);

        List<Content> contents = jsonResult.getContent() == null ? (jsonResult.getIndex() == null ? jsonResult.getRelated() : jsonResult.getIndex()) : jsonResult.getContent();
        for (Content content : contents) {
            if (content.isContent()) {
                DoujinBean bean = new DoujinBean();
                bean.setUrl(content.getContent_url().replace("www.fakku.net/", "api.fakku.net/"));
                bean.setUrlImageTitle(content.getContent_images().getCover());
                bean.setUrlImagePage(content.getContent_images().getSample());
                bean.setTitle(content.getContent_name());
                bean.setSerie(parseURLBean(content.getContent_series()));
                bean.setArtist(parseURLBean(content.getContent_artists()));

                URLBean urlBean = new URLBean();
                urlBean.setDescription(content.getContent_language());
                urlBean.setUrl(Constants.SITEROOT + "/" + content.getContent_category() + "/" + content.getContent_language());
                bean.setLanguage(urlBean);

                UserBean userBean = new UserBean();
                userBean.setUser(content.getContent_poster());
                userBean.setUrlUser(content.getContent_poster_url());
                bean.setUploader(userBean);

                bean.setTranslator(parseURLBean(content.getContent_translators()));
                bean.setDescription(content.getContent_description());

                bean.setLstTags(parseURLBeans(content.getContent_tags(), content.getContent_category()));
                bean.setQtyPages(content.getContent_pages());
                bean.setImageServer(content.getContent_images().getCover().replaceAll("/thumbs/.*", "").replaceAll("https", "http") + "/images/");
                list.add(bean);
            }
        }
        result.setPages(jsonResult.getPages());
        return result;
    }

    public static LinkedList<DoujinBean> parseHTMLFavorite(String url)
            throws IOException, URISyntaxException {
        LinkedList<DoujinBean> result = new LinkedList<DoujinBean>();

        String html = Helper.getHTML(url);

        Document doc = Jsoup.parse(html);

        Helper.logInfo("parseHTMLFavorite : " + url, html);

        Elements favorites = doc.select(".book");
        for (Element favorite : favorites) {
            DoujinBean bean = new DoujinBean();

            // Images
            Element img = favorite.select("img").first();
            bean.setUrlImageTitle(img.attr("src"));

            bean.setTitle(img.attr("alt"));

            String aux = favorite.select(".cover").first().attr("href");
            bean.setUrl(Constants.SITEROOT + aux);
            bean.setImageServer(aux.split("/thumbs/")[0] + "/images/");

            result.add(bean);
        }

        return result;
    }

    public static void parseJSONDoujin(DoujinBean bean)
            throws IOException {
        String url = bean.getUrl();

        String html = Helper.getHTML(url);

        Content content = new Gson().fromJson(html, JSONContentResult.class).getContent();


        bean.setSerie(parseURLBean(content.getContent_series()));
        bean.setArtist(parseURLBean(content.getContent_artists()));
        bean.setTranslator(parseURLBean(content.getContent_translators()));
        UserBean userBean = new UserBean();
        userBean.setUser(content.getContent_poster());
        userBean.setUrlUser(content.getContent_poster_url());
        bean.setUploader(userBean);

        Date date = new Date(content.getContent_date() * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy");
        bean.setDate(sdf.format(date));
        //Language
        URLBean urlBean = new URLBean();
        urlBean.setDescription(content.getContent_language());
        urlBean.setUrl(Constants.SITEROOT + "/" + content.getContent_category() + "/" + content.getContent_language());
        bean.setLanguage(urlBean);
        // Qty Pages

        bean.setQtyPages(content.getContent_pages());
        bean.setDescription(content.getContent_description());

        bean.setLstTags(parseURLBeans(content.getContent_tags(), content.getContent_category()));

        // URL
        bean.setUrl(content.getContent_url().replace("www.fakku", "api.fakku"));
        bean.setTitle(content.getContent_name());

        // Image
        bean.setUrlImageTitle(content.getContent_images().getCover());
        bean.setImageServer(content.getContent_images().getCover().replaceAll("/thumbs/.*", "") + "/images/");

        bean.setCompleted(true);
    }

    public static String imageServerUrl(String url) throws IOException {
        String result = null;

        String html = Helper.getHTMLCORS(url + "/read#page=1");

        Helper.logInfo("parseHTMLDoujin / imageServer : " + url + "/read#page=1", html);

        String token = "function imgpath(x)";
        int idxStart = html.indexOf(token, 0) + token.length();
        token = "return";
        idxStart = html.indexOf(token, idxStart) + token.length();
        token = "'";
        idxStart = html.indexOf(token, idxStart) + token.length();
        int idxEnd = html.indexOf(token, idxStart) + token.length();
        result = html.substring(idxStart, idxEnd - 1);
        return result;
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

    private static List<URLBean> parseURLBeans(List<Attribute> attributes, String category) {
        List<URLBean> urlBeans = new ArrayList<URLBean>();
        if (attributes != null)
            for (Attribute attribute : attributes) {
                URLBean b = new URLBean();
                b.setDescription(attribute.getAttribute());
                b.setUrl(Constants.SITEROOT + "/" + category + attribute.getAttribute_link());
                urlBeans.add(b);
            }
        return urlBeans;
    }

    private static URLBean parseURLBean(List<Attribute> attributes) {
        URLBean b = new URLBean();
        if (attributes != null && attributes.size() > 0 && attributes.get(0) != null) {
            b.setDescription(attributes.get(0).getAttribute());
            b.setUrl(Constants.SITEROOT + attributes.get(0).getAttribute_link());
        } else {
            b.setDescription("");
            b.setUrl("");
        }
        return b;
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
