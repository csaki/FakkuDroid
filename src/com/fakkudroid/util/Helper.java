package com.fakkudroid.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.fakkudroid.R;
import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.bean.URLBean;
import com.google.gson.Gson;

public class Helper {

	public static File getCacheDir(Context context) {
		File file = null;
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		String settingDir = prefs.getString("dir_download", "0");
		if (settingDir.equals(Constants.EXTERNAL_STORAGE + "")) {
			String state = Environment.getExternalStorageState();
			if (Environment.MEDIA_MOUNTED.equals(state)) {
				file = new File(Environment.getExternalStorageDirectory()
						+ Constants.CACHE_DIRECTORY);
				boolean success = true;
				if (!file.exists()) {
					success = file.mkdirs();
				}

				if (!success)
					file = null;
			}
		}
		if (file == null)
			file = new File(Environment.getRootDirectory()
					+ Constants.CACHE_DIRECTORY);

		if (!file.exists()) {
			file.mkdirs();
		}
		return file;
	}

	public static File getDir(String dir, int mode, Context context) {
		File file = null;
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		String settingDir = prefs.getString("dir_download", "0");
		if (settingDir.equals(Constants.EXTERNAL_STORAGE + "")) {
			String state = Environment.getExternalStorageState();
			if (Environment.MEDIA_MOUNTED.equals(state)) {
				file = new File(Environment.getExternalStorageDirectory()
						+ Constants.LOCAL_DIRECTORY + "/" + dir);
				boolean success = true;
				if (!file.exists()) {
					success = file.mkdirs();
				}

				if (!success)
					file = null;
			}
		}
		if (file == null)
			file = new File(Environment.getRootDirectory()
					+ Constants.LOCAL_DIRECTORY + "/" + dir);

		if (!file.exists()) {
			file.mkdirs();
		}
		return file;
	}
	
	public static void saveBitmap(File file, Bitmap bitmap) throws IOException {
		FileOutputStream fOut = new FileOutputStream(file);

		bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
		fOut.flush();
		fOut.close();
	}
	
	public static void logError(Object errorClass, String msg, Exception e){
		Log.e(errorClass.getClass().toString(), msg, e);
	}

	@SuppressLint("NewApi")
	public static <T> void executeAsyncTask(AsyncTask<T, ?, ?> task,
			T... params) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
		} else {
			task.execute(params);
		}
	}

	public static void saveJsonDoujin(DoujinBean doujin, File dir)
			throws IOException {
		File file = new File(dir, "data.json");

		if (!file.exists()) {
			Gson gson = new Gson();
			// convert java object to JSON format,
			// and returned as JSON formatted string
			String json = gson.toJson(doujin);
			FileWriter writer = new FileWriter(file);
			writer.write(json);
			writer.close();
		}
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
		url = Helper.escapeURL(url);
		String html = res.getString(R.string.image_html);
		html = html.replace("@width", width + "");
		html = html.replace("@height", height + "");
		html = html.replace("@japaneseMode", japaneseMode + "");
		html = html.replace("@url", url);

		return html;
	}

	public static String createHTMLImagePercentage(String url, int pct,
			Resources res) {
		url = Helper.escapeURL(url);
		String html = res.getString(R.string.image_html_percent);
		html = html.replace("@percentage", pct + "");
		html = html.replace("@url", url);
		return html;
	}

	public static void saveInStorage(File file, String imageUrl)
			throws Exception {

		imageUrl = Helper.escapeURL(imageUrl);
		String fakkuExtentionFile = file.getAbsolutePath();
		fakkuExtentionFile = fakkuExtentionFile
				.replaceAll("\\.jpg", "\\.fakku");
		File fakkuFile = new File(fakkuExtentionFile);
		if (fakkuFile.exists()) {
			fakkuFile.renameTo(file);
		}

		OutputStream output = null;
		InputStream input = null;

		try {
			if (!file.exists()) {
				URL url = new URL(imageUrl);
				URLConnection connection = url.openConnection();
				connection.connect();

				input = new BufferedInputStream(url.openStream());

				output = new FileOutputStream(file);

				byte data[] = new byte[1024];
				int count;
				while ((count = input.read(data)) != -1) {
					output.write(data, 0, count);
				}
				output.flush();
			}
		} catch (Exception e) {
			if (file.exists()) {
				file.delete();
			}
			throw e;
		} finally {
			if (output != null) {
				output.close();
			}
			if (input != null) {
				input.close();
			}
		}
	}

	public static URLBean castURLBean(String urlBean) {
		URLBean result = new URLBean(urlBean.split("\\|")[1],
				urlBean.split("\\|")[0]);
		return result;
	}

	public static void openPerfectViewer(String firstImage, Activity activity) {
		try {
			Intent intent = activity
					.getPackageManager()
					.getLaunchIntentForPackage("com.rookiestudio.perfectviewer");
			intent.setAction(android.content.Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(new File(firstImage)), "image/*");
			activity.startActivity(intent);
		} catch (Exception e) {
			Toast.makeText(activity, R.string.error_open_perfect_viewer,
					Toast.LENGTH_SHORT).show();
		}
	}
}