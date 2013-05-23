package com.fakkudroid.component;

import android.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class ActionImageButton2 extends ImageButton{

	public ActionImageButton2(Context context) {
		super(context);
		this.setOnTouchListener(new OnTouchListenerCahngeBackground());
		this.setOnLongClickListener(new OnLongClickListenerShowContent());
	}
	public ActionImageButton2(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setOnTouchListener(new OnTouchListenerCahngeBackground());
		this.setOnLongClickListener(new OnLongClickListenerShowContent());
	}

	public ActionImageButton2(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.setOnTouchListener(new OnTouchListenerCahngeBackground());
		this.setOnLongClickListener(new OnLongClickListenerShowContent());
	}
	
	class OnLongClickListenerShowContent implements OnLongClickListener{

		@Override
		public boolean onLongClick(View v) {
			Toast.makeText(ActionImageButton2.this.getContext(), ActionImageButton2.this.getContentDescription(), Toast.LENGTH_SHORT).show();
			return true;
		}
		
	}
	
	class OnTouchListenerCahngeBackground implements OnTouchListener{

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction())
	        {
		        case MotionEvent.ACTION_DOWN:
	            {
	            	ActionImageButton2.this.setBackgroundResource(com.fakkudroid.R.color.url_color);
	                return false;
	            }
		        default:
	            {
	            	ActionImageButton2.this.setBackgroundResource(R.color.transparent);
	                return false;
	            }
	        }
		}
		
	}
	
}
