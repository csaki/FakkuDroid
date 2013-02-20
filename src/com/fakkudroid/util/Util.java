package com.fakkudroid.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.fakkudroid.R;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;

public class Util {

	public static Bitmap loadFromUrl(String link, int reqWidth)
			throws IOException, URISyntaxException {
		link = escapeURL(link);

		HttpGet httpRequest = null;

		httpRequest = new HttpGet(link);

		HttpClient httpclient = new DefaultHttpClient();

		HttpResponse response = (HttpResponse) httpclient.execute(httpRequest);

		HttpEntity entity = response.getEntity();

		BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);

		InputStream instream = bufHttpEntity.getContent();
		return decodeFile(instream, reqWidth);
	}

	public static String escapeURL(String link) {
		link = link.replaceAll("\\[", "%5B");
		link = link.replaceAll("\\]", "%5D");
		link = link.replaceAll("\\s", "%20");
		return link;
	}

	public static Bitmap decodeFile(InputStream instream, int reqWidth)
			throws IOException {
		// Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(instream, null, o);

		final int width = o.outWidth;
		// Find the correct scale value. It should be the power of 2.
		int inSampleSize = 1;

		if (width > reqWidth) {
			inSampleSize = Math.round((float) width / (float) reqWidth);
		}

		// Decode with inSampleSize
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = inSampleSize;

		instream.reset();
		return BitmapFactory.decodeStream(instream, null, o2);
	}

	public static String limitString(String s, int maxSize, String fill) {
		int sizeFill = fill.length();
		if (s.length() > maxSize) {
			return s.substring(0, maxSize - sizeFill) + fill;
		}
		return s;
	}

	public static String limitString(String s, int maxSize) {
		return limitString(s, maxSize, "");
	}

	public static String getHTML(String url) throws IOException {
		url = escapeURL(url);

		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		HttpResponse response = client.execute(request);

		String html = "";
		InputStream in = response.getEntity().getContent();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder str = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			str.append(line);
		}
		in.close();
		html = str.toString();
		return html;
	}

	public static String getHTML(String url, CookieStore cs) throws IOException {
		url = escapeURL(url);

		// Create local HTTP context
		HttpContext localContext = new BasicHttpContext();
		// Bind custom cookie store to the local context
		if (cs != null)
			localContext.setAttribute(ClientContext.COOKIE_STORE, cs);

		HttpGet get = new HttpGet(url);
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(get, localContext);
		HttpEntity ent = response.getEntity();
		InputStream is = ent.getContent();
		BufferedInputStream bis = new BufferedInputStream(is);
		byte[] tmp = new byte[2048];
		String ret = "";
		while (bis.read(tmp) != -1) {
			ret += new String(tmp);
		}
		return ret;
	}

	public static String createHTMLImage(String url, float width, float height, boolean japaneseMode, Resources res) {
		url = Util.escapeURL(url);
		String html = res.getString(R.string.image_html);
		html = html.replace("@width", width + "");
		html = html.replace("@height", height + "");
		html = html.replace("@japaneseMode", japaneseMode + "");
		html = html.replace("@url", url);		
		
		return html;
	}

	public static String createHTMLImagePercentage(String url, int pct, Resources res) {
		url = Util.escapeURL(url);
		String html = res.getString(R.string.image_html_percent);
		html = html.replace("@percentage", pct + "");		
		html = html.replace("@url", url);		
		return html;
	}
	
}
