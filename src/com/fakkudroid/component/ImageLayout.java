package com.fakkudroid.component;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.fakkudroid.R;
import com.fakkudroid.util.Helper;
import com.nikkoaiello.mobile.android.PinchImageView;

public class ImageLayout extends RelativeLayout{

	String imageFile;
    String backgroundColor;
	ProgressBar bar;
	ImageView piv;

	private ImageLayout(Context context){
		super(context);
	}

	public ImageLayout(String imageFile, String backgroundColor, Context context){
		super(context);
		this.imageFile = imageFile;
        this.backgroundColor = backgroundColor;
		init();
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void init() {
		bar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleHorizontal);;
		bar.setId(R.id.view_status);
        bar.setIndeterminate(true);

        piv = new ImageView(getContext());
		
		this.bar.setProgress(0);
		
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, getBarHeight());
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		addView(bar, params);
		
		params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		params.addRule(RelativeLayout.BELOW, bar.getId());
		addView(piv, params);
	}
	
	public int getBarHeight(){
		final float scale = getContext().getResources().getDisplayMetrics().density;
		int pixels = (int) (5 * scale + 0.5f);
		return pixels;
	}
	
	@SuppressWarnings("deprecation")
	public void startLoader(int width, int height, boolean japaneseMode){
		height-= getBarHeight();
        try{
            Bitmap bmp = Helper.decodeSampledBitmapFromFile(imageFile,width,height);
            piv.setImageBitmap(bmp);
        }catch( OutOfMemoryError e ) {
            System.gc();
        }catch (Exception e){

        }
	}
/*
    public void reload(){
        wb.reload();
    }
	
	@SuppressWarnings("deprecation")
	public void resizeImage(int width, int height){
		height-= getBarHeight();
		wb.loadUrl("javascript:resizeAll(" + width/wb.getScale() + "," + height/wb.getScale() + ");");
	}
	
	public void changeJapaneseMode(boolean japaneseMode){
		wb.loadUrl("javascript:japaneseMode=" + japaneseMode);
	}
	*/

	public void zoomIn(){

    }
	
	public void zoomOut(){

	}

    public boolean isLoaded(){
        return piv.getDrawable()!=null;
    }
}
