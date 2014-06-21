package com.fakkudroid.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fakkudroid.R;
import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.fragment.DoujinListFragment;
import com.fakkudroid.util.Helper;
import com.fakkudroid.util.ImageQuality;

import java.util.LinkedList;

public class DoujinListAdapter extends ArrayAdapter<DoujinBean> {

    LayoutInflater inf;
    LinkedList<DoujinBean> objects;
    boolean related = false;
    public DoujinListFragment doujinListFragment = null;

    public DoujinListAdapter(Context context, int resource,
                             int textViewResourceId, LinkedList<DoujinBean> objects, boolean related) {
        super(context, resource, textViewResourceId, objects);
        this.inf = LayoutInflater.from(context);
        this.objects = objects;
        this.related = related;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        DoujinBean s = objects.get(position);
        if (convertView == null) {
            convertView = inf.inflate(R.layout.row_doujin, null);
            holder = new ViewHolder();
            holder.tvDoujin = (TextView) convertView
                    .findViewById(R.id.tvDoujin);
            holder.tvArtist = (TextView) convertView
                    .findViewById(R.id.tvArtist);
            holder.tvSerie = (TextView) convertView.findViewById(R.id.tvSerie);
            holder.tvDescription = (TextView) convertView
                    .findViewById(R.id.tvDescription);
            holder.tvTags = (TextView) convertView.findViewById(R.id.tvTags);
            holder.ivTitle = (ImageView) convertView.findViewById(R.id.ivTitle);
            holder.ivPage = (ImageView) convertView.findViewById(R.id.ivPage);
            holder.ll = (LinearLayout) convertView.findViewById(R.id.ll);
            holder.btnQuickDownload = (Button)convertView.findViewById(R.id.btnQuickDownload);
            holder.btnQuickRead = (Button)convertView.findViewById(R.id.btnQuickRead);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if(related&&position==0)
            holder.ll.setBackgroundColor(convertView.getResources().getColor(R.color.abs__background_holo_light));
        else
            holder.ll.setBackgroundColor(convertView.getResources().getColor(android.R.color.white));

        holder.tvDoujin.setText(Helper.limitString(s.getTitle(), 36, "..."));
        holder.tvArtist.setText(Helper.limitString(
                s.getArtist().getDescription(), 36, "..."));
        holder.tvSerie.setText(s.getSerie().getDescription());
        holder.tvDescription.setText(Html.fromHtml(s.getDescription().replace(
                "<br>", "<br/>")));

        holder.tvTags.setText(s.getTags());

        if(s.isTitleLoaded()){
            holder.ivTitle.setImageBitmap(s.getBitmapImageTitle(Helper.getCacheDir(getContext()), ImageQuality.MEDIUM));
        }
        if(s.isPageLoaded()){
            holder.ivPage.setImageBitmap(s.getBitmapImagePage(Helper.getCacheDir(getContext()),ImageQuality.MEDIUM));
        }

        if (null != doujinListFragment) {
            holder.btnQuickDownload.setVisibility(View.VISIBLE);
            holder.btnQuickDownload.setTag(position);
            holder.btnQuickDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    quickDownload((Integer) holder.btnQuickDownload.getTag());
                }
            });
            holder.btnQuickRead.setVisibility(View.VISIBLE);
            holder.btnQuickRead.setTag(position);
            holder.btnQuickRead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    quickRead((Integer) holder.btnQuickRead.getTag());
                }
            });
        }
        else {
            holder.btnQuickDownload.setVisibility(View.GONE);
            holder.btnQuickRead.setVisibility(View.GONE);
        }
        return convertView;
    }

    private void quickDownload(Integer tag) {
        doujinListFragment.quickDownload(tag);
    }

    private void quickRead(Integer tag) {
        doujinListFragment.quickRead(tag);
    }

    static class ViewHolder {
        TextView tvDoujin;
        TextView tvSerie;
        TextView tvArtist;
        TextView tvTags;
        TextView tvDescription;
        Button btnQuickDownload;
        Button btnQuickRead;
        LinearLayout ll;

        ImageView ivTitle;
        ImageView ivPage;
    }
}
