package com.fakkudroid.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
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


    public static String LOG_FILE = "logFile.txt";
    public  static File logFile;
    public static boolean writeLogFile;

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
            try {
                file = new File(Environment.getRootDirectory()
                        + Constants.CACHE_DIRECTORY);
            }catch (Exception e){
                file = context.getDir("", Context.MODE_WORLD_WRITEABLE);
                file = new File(file, Constants.CACHE_DIRECTORY);
            }

		if (!file.exists()) {
			if(!file.mkdirs()){
                file = context.getDir("", Context.MODE_WORLD_WRITEABLE);
                file = new File(file, Constants.CACHE_DIRECTORY);
                if (!file.exists())
                    file.mkdirs();
            }
		}
		return file;
	}

	public static File getDir(String dir, Context context) {
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
            try {
                file = new File(Environment.getRootDirectory()
                        + Constants.LOCAL_DIRECTORY + "/" + dir);
            }catch (Exception e){
                file = context.getDir("", Context.MODE_WORLD_WRITEABLE);
                file = new File(file, Constants.LOCAL_DIRECTORY);
            }

        if (!file.exists()) {
            if(!file.mkdirs()){
                file = context.getDir("", Context.MODE_WORLD_WRITEABLE);
                file = new File(file, Constants.LOCAL_DIRECTORY + "/" + dir);
                if (!file.exists()) {
                    file.mkdirs();
                }
            }
        }
		return file;
	}

	public static void saveBitmap(File file, Bitmap bitmap) throws IOException {
		FileOutputStream fOut = new FileOutputStream(file);

		bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
		fOut.flush();
		fOut.close();
	}

	public static void logError(Object errorClass, String msg, Exception e) {
		logError(errorClass.getClass().toString(), msg, e);
	}
	
	public static void logError(String errorClass, String msg, Exception e) {
		Log.e(errorClass, msg, e);

        if(writeLogFile&&logFile!=null){
            String completeError = "\n"+"\n"+errorClass + " // " + msg + " // " + e.getMessage() + "\n";
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            completeError += errors.toString() + "\n";

            writeLog(completeError);
        }
	}

    public static void logInfo(String tag, String msg) {
        Log.i(tag, msg);

        if(writeLogFile&&logFile!=null){
            String message = "\n\n" + tag + "\n" + msg;
            writeLog(message);
        }
    }

    public static void writeLog(String msg){
        try {
            // if file doesnt exists, then create it
            if (!logFile.exists()) {
                logFile.createNewFile();
            }

            FileWriter fw = new FileWriter(logFile.getAbsoluteFile(),true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(msg);
            bw.close();
        } catch (Exception e) {}
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

    public static DoujinBean readJsonDoujin(File f) throws IOException {
        BufferedReader br = null;
        DoujinBean doujinBean = null;
        String json = "";
        try {

            String sCurrentLine;
            br = new BufferedReader(new FileReader(f));

            while ((sCurrentLine = br.readLine()) != null) {
                json+=sCurrentLine;
            }

        } finally {
            try {
                if (br != null)br.close();
            } catch (IOException ex) {}
        }

        Gson gson = new Gson();
        doujinBean = gson.fromJson(json, DoujinBean.class);

        return doujinBean;
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
        try{
            String path = link;
            path = java.net.URLEncoder.encode(path, "utf8");
            path = path.replace("%3A",":");
            path = path.replace("%2F","/");
            path = path.replace("+","%20");
            path = path.replace("%23","#");
            path = path.replace("%3D","=");
            return path;
        }catch(Exception e){
            link = link.replaceAll("\\[", "%5B");
            link = link.replaceAll("\\]", "%5D");
            link = link.replaceAll("\\s", "%20");
        }
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
        int code = response.getStatusLine().getStatusCode();

        if(code!=200)
            System.out.print(code);

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

	public static String getHTMLCORS(String url) throws IOException {
		url = escapeURL(url);

		HttpClient client = new DefaultHttpClient();
        ((AbstractHttpClient) client)
            .addRequestInterceptor(new HttpRequestInterceptor() {
                public void process(final HttpRequest request,
                                    final HttpContext context) throws HttpException,
                        IOException {
                    request.setHeader(HTTP.TARGET_HOST, "www.fakku.net");
                }
        });
		HttpGet request = new HttpGet(url);
        request.setHeader("Referer", "http://www.fakku.net");
		HttpResponse response = client.execute(request);
        int code = response.getStatusLine().getStatusCode();

        if(code!=200)
            System.out.print(code);

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
        int code = response.getStatusLine().getStatusCode();

        if(code!=200)
            System.out.print(code);

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
			boolean japaneseMode, Resources res, String backgroundColor) {
		url = Helper.escapeURL(url);
		String html = res.getString(R.string.image_html);
		html = html.replace("@width", width + "");
		html = html.replace("@height", height + "");
		html = html.replace("@japaneseMode", japaneseMode + "");
		html = html.replace("@url", url);
        html = html.replace("@color", backgroundColor);

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

	public static void zipDecompress(String zipFile, String outputFolder) {
		byte[] buffer = new byte[1024];

		try {

			// create output directory is not exists
			File folder = new File(outputFolder);
			if (!folder.exists()) {
				folder.mkdir();
			}
			File zip = new File(zipFile);
			// get the zip file content
			ZipInputStream zis = new ZipInputStream(
					new FileInputStream(zipFile));
			// get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();

			boolean first = true;
			int count = 1;
			while (ze != null) {
				Formatter fmt = new Formatter();
				File newFile = new File(outputFolder + File.separator
						+ fmt.format("%03d", count++) + ".jpg");
				fmt.close();

				System.out.println("file unzip : " + newFile.getAbsoluteFile());

				// create all non exists folders
				// else you will hit FileNotFoundException for compressed folder
				if (ze.isDirectory()) {
					new File(newFile.getAbsolutePath()).mkdirs();
				} else {
					if (first) {
						File newfolder = new File(zip.getName().substring(0,
								zip.getName().indexOf(".")),
								folder.getAbsolutePath());
						newfolder.mkdirs();
						folder = newfolder;
					}

					FileOutputStream fos = null;

					new File(newFile.getParent()).mkdirs();
					if (!newFile.exists())
						newFile.createNewFile();
					fos = new FileOutputStream(newFile);

					int len;
					while ((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}

					fos.close();
				}
				first = false;
				ze = zis.getNextEntry();
			}

			zis.closeEntry();
			zis.close();

			System.out.println("Done");

		} catch (IOException ex) {
			logError(Helper.class.toString(), "zipDecompress", ex);
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

    public static String formatterDate(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("MMMMM dd, yyyy");
        return formatter.format(date);
    }

    public static <T> List<List<T>> splitArrayList(List<T> array, int size){
        List<List<T>> result = new ArrayList<List<T>>();

        for (int i=0;i<size;i++){
            List<T> list = new ArrayList<T>();
            result.add(list);
        }
        int c = 0;

        for(T t:array){
           result.get(c++).add(t);
            if(c>=size)
                c = 0;
        }

        return result;
    }
}
