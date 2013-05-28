package com.fakkudroid.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.fakkudroid.R;
import com.fakkudroid.bean.URLBean;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.Toast;

public class Util {

	public static void saveBitmap(File file, Bitmap bitmap) throws IOException{
		FileOutputStream fOut = new FileOutputStream(file);

	    bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
	    fOut.flush();
	    fOut.close();
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and
			// width
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will
			// guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}

	public static Bitmap decodeSampledBitmapFromResource(Resources res,
			int resId, int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

	public static Bitmap decodeSampledBitmapFromFile(String file, int reqWidth,
			int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(file, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(file, options);
	}

	public static String escapeURL(String link) {
		link = link.replaceAll("\\[", "%5B");
		link = link.replaceAll("\\]", "%5D");
		link = link.replaceAll("\\s", "%20");
		return link;
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

		String html = "";
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder str = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			str.append(line);
		}
		is.close();
		html = str.toString();
		return html;
	}

	public static String createHTMLImage(String url, float width, float height,
			boolean japaneseMode, Resources res) {
		url = Util.escapeURL(url);
		String html = res.getString(R.string.image_html);
		html = html.replace("@width", width + "");
		html = html.replace("@height", height + "");
		html = html.replace("@japaneseMode", japaneseMode + "");
		html = html.replace("@url", url);

		return html;
	}

	public static String createHTMLImagePercentage(String url, int pct,
			Resources res) {
		url = Util.escapeURL(url);
		String html = res.getString(R.string.image_html_percent);
		html = html.replace("@percentage", pct + "");
		html = html.replace("@url", url);
		return html;
	}

	public static void saveInStorage(File file, String imageUrl) throws Exception {
		imageUrl = Util.escapeURL(imageUrl);
		String fakkuExtentionFile = file.getAbsolutePath();
		fakkuExtentionFile = fakkuExtentionFile.replaceAll("\\.jpg", "\\.fakku");
		File fakkuFile = new File(fakkuExtentionFile);
		if(fakkuFile.exists()){
			fakkuFile.renameTo(file);
		}
		if (!file.exists()) {
			URL url = new URL(imageUrl);
			URLConnection connection = url.openConnection();
			connection.connect();

			InputStream input = new BufferedInputStream(url.openStream());

			OutputStream output = new FileOutputStream(file);

			byte data[] = new byte[1024];
			int count;
			while ((count = input.read(data)) != -1) {
				output.write(data, 0, count);
			}

			output.flush();
			output.close();
			input.close();
		}
	}
	
	public static URLBean castURLBean(String urlBean){
		URLBean result = new URLBean(urlBean.split("\\|")[1],urlBean.split("\\|")[0]);		
		return result;
	}
	
	public static void openPerfectViewer(String firstImage, Activity activity){
		try {
			Intent intent = activity.getPackageManager().getLaunchIntentForPackage("com.rookiestudio.perfectviewer");
			intent.setAction(android.content.Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(new File(firstImage)), "image/*");
			activity.startActivity(intent);
		} catch (Exception e) {
			Toast.makeText(activity, R.string.error_open_perfect_viewer, Toast.LENGTH_SHORT).show();
		}
	}
}
