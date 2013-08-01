package com.fakkudroid.adapter;

import java.util.LinkedList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fakkudroid.R;
import com.fakkudroid.bean.URLBean;

public class MenuListAdapter extends ArrayAdapter<URLBean> {

	LayoutInflater inf;
	LinkedList<URLBean> objects;
	boolean main;

	public MenuListAdapter(Context context, int resource, int textViewResourceId,
			LinkedList<URLBean> objects, boolean main) {
		super(context, resource, textViewResourceId, objects);
		this.inf = LayoutInflater.from(context);
		this.objects = objects;
		this.main =  main;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		URLBean s = objects.get(position);
		if (convertView == null) {
			convertView = inf.inflate(R.layout.row_menu, null);
			holder = new ViewHolder();
			holder.tvMenu = (TextView) convertView.findViewById(R.id.tvMenu);
			holder.iv = (ImageView) convertView.findViewById(R.id.iv);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if(s.getIcon()==-1){
			holder.iv.setVisibility(View.GONE);
		}
        if(s.getIcon()!=-2&&s.getIcon()!=-1){
            holder.iv.setImageResource(s.getIcon());
        }
		if(main){
			if(s.getIcon()==-1){
				holder.tvMenu.setTextAppearance(getContext(), android.R.style.TextAppearance_Small);
                holder.tvMenu.setTextColor(Color.WHITE);
			}
			if(s.getIcon()==-2){
				holder.iv.setPadding(40, holder.iv.getPaddingRight(), holder.iv.getPaddingTop(), holder.iv.getPaddingBottom());
			}
		}
		holder.tvMenu.setText(s.getDescription());
		return convertView;
	}
	
	static class ViewHolder {
		TextView tvMenu;
		ImageView iv;
	}
}
