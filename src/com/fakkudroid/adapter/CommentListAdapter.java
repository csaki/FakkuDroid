package com.fakkudroid.adapter;

import java.util.LinkedList;

import com.fakkudroid.bean.CommentBean;
import com.fakkudroid.component.ActionImageButton2;
import com.fakkudroid.fragment.CommentListFragment;
import com.fakkudroid.FavoriteActivity;
import com.fakkudroid.R;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CommentListAdapter extends ArrayAdapter<CommentBean> {
	
	private LayoutInflater inf;
	private LinkedList<CommentBean> objects;
	private CommentListFragment fragment;

	public CommentListAdapter(Context context, int resource, int textViewResourceId,
			LinkedList<CommentBean> objects, CommentListFragment fragment) {
		super(context, resource, textViewResourceId, objects);
		this.inf = LayoutInflater.from(context);
		this.objects = objects;
		this.fragment = fragment;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		final CommentBean s = objects.get(position);
		if (convertView == null) {
			convertView = inf.inflate(R.layout.row_comment, null);
			holder = new ViewHolder();
			holder.tvUser = (TextView) convertView.findViewById(R.id.tvUser);
			holder.tvDate = (TextView) convertView.findViewById(R.id.tvDate);
			holder.tvRank = (TextView) convertView.findViewById(R.id.tvRank);
			holder.tvComment = (TextView) convertView.findViewById(R.id.tvComment);
			holder.rlComment = (RelativeLayout) convertView.findViewById(R.id.rlComment);
			holder.btnReply = (ActionImageButton2) convertView.findViewById(R.id.btnReply);
			holder.btnLike = (ActionImageButton2) convertView.findViewById(R.id.btnLike);
			holder.btnDislike = (ActionImageButton2) convertView.findViewById(R.id.btnDisLike);
			holder.btnLoadMore = (Button) convertView.findViewById(R.id.btnLoadMore);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.tvDate.setText(s.getDate());
		holder.tvComment.setText(Html.fromHtml(s.getComment().replace("<br>", "<br/>")));
		holder.tvComment.setMovementMethod(LinkMovementMethod.getInstance());
		holder.rlComment.setPadding(40*s.getLevel(), 0, 0, 0);
		SpannableString content = new SpannableString(s.getUser().getDescription());
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		holder.tvUser.setText(content);
		holder.tvUser.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent itFavorites = new Intent(fragment.getActivity(),
						FavoriteActivity.class);
				itFavorites.putExtra(FavoriteActivity.INTENT_VAR_USER,s.getUser().getDescription());
				fragment.getActivity().startActivity(itFavorites);		
			}
		});
		if(s.getLevel()==2){
			holder.btnReply.setVisibility(View.INVISIBLE);
		}else{
			holder.btnReply.setVisibility(View.VISIBLE);
		}
		if(position == objects.size()-1&&!fragment.isLastPage()){
			holder.btnLoadMore.setVisibility(View.VISIBLE);
		}else{
			holder.btnLoadMore.setVisibility(View.GONE);
		}
		
		holder.btnLoadMore.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				fragment.loadMoreComments();		
			}
		});
		
		holder.btnReply.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				fragment.replyComment(s);		
			}
		});
		
		holder.btnLike.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				fragment.likeOrDislike(s,true);		
			}
		});
		
		holder.btnDislike.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				fragment.likeOrDislike(s,false);		
			}
		});
		
		if(s.getRank()<0){
			holder.tvRank.setTextColor(fragment.getResources().getColor(R.color.main_color));
		}
		holder.tvRank.setText(s.getStrRank());
		if(s.getSelectLike()>0){
			holder.btnLike.setImageResource(R.drawable.navigation_collapse_select);
		}else if(s.getSelectLike()<0){
			holder.btnDislike.setImageResource(R.drawable.navigation_expand_select);
		}else{
			holder.btnDislike.setImageResource(R.drawable.navigation_expand);
			holder.btnLike.setImageResource(R.drawable.navigation_collapse);
		}
		return convertView;
	}
	
	public void addAll(LinkedList<CommentBean> lstComments){
		for (CommentBean commentBean : lstComments) {
			add(commentBean);
		}
	}
	
	static class ViewHolder {
		TextView tvUser;
		TextView tvComment;
		TextView tvDate;
		TextView tvRank;
		RelativeLayout rlComment;
		ActionImageButton2 btnReply;
		ActionImageButton2 btnLike;
		ActionImageButton2 btnDislike;
		Button btnLoadMore;
	}
}
