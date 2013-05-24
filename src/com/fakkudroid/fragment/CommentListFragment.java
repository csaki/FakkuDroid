package com.fakkudroid.fragment;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;

import org.apache.http.client.ClientProtocolException;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.fakkudroid.DoujinActivity;
import com.fakkudroid.LoginActivity;
import com.fakkudroid.R;
import com.fakkudroid.adapter.CommentListAdapter;
import com.fakkudroid.bean.CommentBean;
import com.fakkudroid.bean.UserBean;
import com.fakkudroid.core.DataBaseHandler;
import com.fakkudroid.core.ExceptionNotLoggedIn;
import com.fakkudroid.core.FakkuConnection;
import com.fakkudroid.core.FakkuDroidApplication;

@SuppressLint("ValidFragment")
public class CommentListFragment extends SherlockListFragment {

	private FakkuDroidApplication app;
	private DoujinActivity doujinActivity;
	private CommentListAdapter da;
	private boolean listCharged;
	private boolean lastPage = false;
	private int currentPage = 1;

	public CommentListFragment(){}
	
	@SuppressLint("ValidFragment")
	public CommentListFragment(DoujinActivity doujinActivity) {
		this.doujinActivity = doujinActivity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (FakkuDroidApplication) getActivity().getApplication();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_comment_list, container,
				false);
		setData();
		return view;
	}

	public void refresh() {
		listCharged = true;
		currentPage = 1;
		da.clear();
		new DownloadComments().execute(app.getCurrent().urlComments(0));
	}

	public void loadMoreComments() {
		listCharged = true;
		new DownloadComments().execute(app.getCurrent().urlComments(
				currentPage++));
	}

	private void setData() {
		da = new CommentListAdapter(this.getActivity(), R.layout.row_comment,
				0, new LinkedList<CommentBean>(), this);
		this.setListAdapter(da);
	}

	public boolean isListCharged() {
		return listCharged;
	}

	public boolean isLastPage() {
		return lastPage;
	}

	public void replyComment(CommentBean b) {
		Toast.makeText(this.getActivity(),
				getResources().getString(R.string.soon), Toast.LENGTH_SHORT)
				.show();
	}
	
	public void goToFavorite(Intent data) {
		doujinActivity.goToFavorite(data);
	}

	public void likeOrDislike(CommentBean b, boolean like) {
		if (!app.getSettingBean().isChecked()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					this.getActivity());
			builder.setMessage(R.string.login_please)
					.setPositiveButton(R.string.login,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									Intent it = new Intent(
											CommentListFragment.this
													.getActivity(),
											LoginActivity.class);
									CommentListFragment.this.getActivity()
											.startActivity(it);
								}
							})
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									return;
								}
							}).create().show();
		}
		app.setSettingBean(null);
		if (app.getSettingBean().isChecked()) {
			new TransactionLike().execute(b, like);
		}
	}

	class DownloadComments extends
			AsyncTask<String, Float, LinkedList<CommentBean>> {

		protected void onPreExecute() {
			doujinActivity.showProgress(true);
		}

		protected LinkedList<CommentBean> doInBackground(String... urls) {
			LinkedList<CommentBean> llComments = new LinkedList<CommentBean>();
			Object[] moreComments = new Object[] { true };
			try {
				Log.i(DownloadComments.class.toString(), "URL Comments: "
						+ urls[0]);
				llComments = FakkuConnection.parseComments(urls[0],
						moreComments);
			} catch (ClientProtocolException e1) {
				Log.e(DownloadComments.class.toString(), "Exception", e1);
			} catch (IOException e1) {
				Log.e(DownloadComments.class.toString(), "Exception", e1);
			} catch (URISyntaxException e1) {
				Log.e(DownloadComments.class.toString(), "Exception", e1);
			} catch (Exception e1) {
				Log.e(DownloadComments.class.toString(), "Exception", e1);
			}
			lastPage = !(Boolean) moreComments[0];
			return llComments;
		}

		protected void onPostExecute(LinkedList<CommentBean> result) {
			da.addAll(result);
			da.notifyDataSetChanged();
			doujinActivity.showProgress(false);
		}
	}

	class TransactionLike extends AsyncTask<Object, Float, Boolean> {

		protected void onPreExecute() {
			doujinActivity.showProgress(true);
		}

		protected Boolean doInBackground(Object... obj) {
			boolean isConnected = false;
			if (app.getSettingBean().isChecked())
				try {
					isConnected = FakkuConnection.connect(app.getSettingBean()
							.getUser(), app.getSettingBean().getPassword());
				} catch (ClientProtocolException e) {
					Log.e(this.getClass().toString(), e.getLocalizedMessage(),
							e);
				} catch (IOException e) {
					Log.e(this.getClass().toString(), e.getLocalizedMessage(),
							e);
				}

			if (!isConnected) {
				UserBean s = app.getSettingBean();
				s.setChecked(false);
				new DataBaseHandler(getActivity()).updateSetting(s);
				app.setSettingBean(null);
			} else {
				try {
					CommentBean cb = (CommentBean) obj[0];
					String url = "";
					boolean like = (Boolean) obj[1];
					url = like ? cb.getUrlLike() : cb.getUrlDislike();

					if (url.equals("like"))
						FakkuConnection.transaction(url);
					else
						FakkuConnection.transaction(url);
					if (like) {
						if (cb.getSelectLike() > 0) {
							cb.setSelectLike(0);
							cb.setRank(cb.getRank()-1);
						} else {
							cb.setSelectLike(1);
							cb.setRank(cb.getRank()+1);
						}
					} else {
						if (cb.getSelectLike() < 0) {
							cb.setSelectLike(0);
							cb.setRank(cb.getRank()+1);
						} else {
							cb.setSelectLike(-1);
							cb.setRank(cb.getRank()-1);
						}
					}

				} catch (ExceptionNotLoggedIn e) {
					Log.e(this.getClass().toString(), e.getLocalizedMessage(),
							e);
				} catch (IOException e) {
					Log.e(this.getClass().toString(), e.getLocalizedMessage(),
							e);
				}
			}
			return isConnected;
		}

		protected void onPostExecute(Boolean isConnected) {
			doujinActivity.showProgress(false);
			if (isConnected) {
				da.notifyDataSetChanged();
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						getActivity());
				builder.setMessage(R.string.login_please)
						.setPositiveButton(R.string.login,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										Intent it = new Intent(getActivity(),
												LoginActivity.class);
										getActivity().startActivity(it);
									}
								})
						.setNegativeButton(android.R.string.cancel,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										return;
									}
								}).create().show();
			}
		}
	}
}
