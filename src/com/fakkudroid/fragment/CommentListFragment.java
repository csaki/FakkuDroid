package com.fakkudroid.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.fakkudroid.MainActivity;
import com.fakkudroid.R;
import com.fakkudroid.adapter.CommentListAdapter;
import com.fakkudroid.adapter.DoujinListAdapter;
import com.fakkudroid.bean.CommentBean;
import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.bean.UserBean;
import com.fakkudroid.core.DataBaseHandler;
import com.fakkudroid.core.FakkuConnection;
import com.fakkudroid.core.FakkuDroidApplication;
import com.fakkudroid.exception.ExceptionNotLoggedIn;
import com.fakkudroid.util.Constants;
import com.fakkudroid.util.Helper;

import org.apache.http.client.ClientProtocolException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Neko on 15/01/14.
 */
public class CommentListFragment extends SherlockListFragment {

    private MainActivity mMainActivity;
    FakkuDroidApplication app;
    private View mFormView;
    private View mStatusView;
    private View view;
    int numPage;
    int index = -1;
    String url, urlDoujin;
    private boolean listCharged;
    private CommentListAdapter da;
    private DoujinBean currentBean;
    private LinkedList<CommentBean> llComments;

    public void setMainActivity(MainActivity mainActivity) {
        mMainActivity = mainActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (FakkuDroidApplication) getActivity().getApplication();

        if (url == null) {
            urlDoujin = getActivity().getIntent().getStringExtra(MainActivity.INTENT_VAR_URL);
            url = urlDoujin.replaceAll(Constants.SITEROOT, Constants.SITEROOT + "/comments");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater
                .inflate(R.layout.fragment_comment_list, container, false);

        mFormView = view.findViewById(R.id.view_form);
        mStatusView = view.findViewById(R.id.view_status);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void replyComment(CommentBean b) {
        Toast.makeText(this.getActivity(), getResources().getString(R.string.soon), Toast.LENGTH_SHORT).show();
    }

    public void nextPage(View view) {
        index = -1;
        numPage++;
        loadComments();
        String txt = "Top Comments";
        if (numPage > 0) {
            txt = "Page " + numPage;
        }
        CharSequence text = txt;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(this.getActivity(), text, duration);
        toast.show();
    }

    public void previousPage(View view) {
        index = -1;
        if (numPage - 1 == -1) {
            CharSequence text = "There aren't more pages.";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(this.getActivity(), text, duration);
            toast.show();
        } else {
            numPage--;
            loadComments();
            String txt = "Top Comments";
            if (numPage > 0) {
                txt = "Page " + numPage;
            }
            CharSequence text = txt;
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(this.getActivity(), text, duration);
            toast.show();
        }
    }

    public void changePage(int page) {
        index = -1;
        numPage = page;
        loadComments();
        CharSequence text = "Page " + numPage;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(this.getActivity(), text, duration);
        toast.show();
    }

    public void loadComments() {
        currentBean = mMainActivity.getCurrentBean();
        if(currentBean!=null){
            TextView tvPage = (TextView) view.findViewById(R.id.tvPage);
            String txt = "Top Comments";
            if (numPage > 0) {
                txt = "Page " + numPage;
            }
            tvPage.setText(txt);
            if (numPage < 2) {
                if (numPage == 0) {
                    llComments = currentBean.getLstTopComments();
                } else {
                    llComments = currentBean.getLstRecentComments();
                }
                setData();
            } else {
                String urlComments = url + "/" + ((numPage - 1) * 30);
                Helper.executeAsyncTask(new DownloadComments(), urlComments);
            }
        }

        listCharged = true;
    }

    public void goToFavorites(String user, String urlUser) {
        Intent itMain = new Intent(mMainActivity, MainActivity.class);
        itMain.putExtra(MainActivity.INTENT_VAR_CURRENT_CONTENT, MainActivity.FAVORITES);
        itMain.putExtra(MainActivity.INTENT_VAR_USER, user);
        itMain.putExtra(MainActivity.INTENT_VAR_URL, urlUser);
        getActivity().startActivityForResult(itMain, 1);
    }

    public void likeOrDislike(CommentBean b, boolean like) {
        if (!app.getSettingBean().isChecked()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    this.getActivity());
            builder.setMessage(R.string.login_please)
                    .setPositiveButton(R.string.login,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent itMain = new Intent(mMainActivity, MainActivity.class);
                                    itMain.putExtra(MainActivity.INTENT_VAR_CURRENT_CONTENT, MainActivity.LOGIN);
                                    getActivity().startActivityForResult(itMain, 1);
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    return;
                                }
                            }).create().show();
        }
        app.setSettingBean(null);
        if (app.getSettingBean().isChecked()) {
            Helper.executeAsyncTask(new TransactionLike(), b, like);
        }
    }

    private void setData() {
        showProgress(false);
        if(llComments==null)
            llComments=new LinkedList<CommentBean>();
        da = new CommentListAdapter(this.getActivity(), R.layout.row_comment, 0, llComments, this);
        this.setListAdapter(da);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            int shortAnimTime = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);

            mStatusView.setVisibility(View.VISIBLE);
            mStatusView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mStatusView.setVisibility(show ? View.VISIBLE
                                    : View.GONE);
                        }
                    });

            mFormView.setVisibility(View.VISIBLE);
            mFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mFormView.setVisibility(show ? View.GONE
                                    : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    class DownloadComments extends
            AsyncTask<String, Float, LinkedList<CommentBean>> {

        protected void onPreExecute() {
            showProgress(true);
        }

        protected LinkedList<CommentBean> doInBackground(String... urls) {
            LinkedList<CommentBean> llComments = new LinkedList<CommentBean>();
            try {
                Log.i(DownloadComments.class.toString(), "URL Comments: "
                        + urls[0]);
                llComments = FakkuConnection.parseComments(urls[0]);
            } catch (ClientProtocolException e) {
                Helper.logError(this, e.getMessage(), e);
            } catch (IOException e) {
                Helper.logError(this, e.getMessage(), e);
            } catch (URISyntaxException e) {
                Helper.logError(this, e.getMessage(), e);
            } catch (Exception e) {
                Helper.logError(this, e.getMessage(), e);
            }
            return llComments;
        }

        protected void onPostExecute(LinkedList<CommentBean> result) {
            llComments = result;
            setData();
        }
    }

    class TransactionLike extends AsyncTask<Object, Float, Boolean> {

        protected void onPreExecute() {
            showProgress(true);
        }

        protected Boolean doInBackground(Object... obj) {
            boolean isConnected = false;
            if (app.getSettingBean().isChecked())
                try {
                    FakkuConnection.connect(app.getSettingBean());
                    isConnected = app.getSettingBean().isChecked();
                } catch (ClientProtocolException e) {
                    Helper.logError(this, e.getMessage(), e);
                } catch (IOException e) {
                    Helper.logError(this, e.getMessage(), e);
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
                            cb.setRank(cb.getRank() - 1);
                        } else {
                            cb.setSelectLike(1);
                            cb.setRank(cb.getRank() + 1);
                        }
                    } else {
                        if (cb.getSelectLike() < 0) {
                            cb.setSelectLike(0);
                            cb.setRank(cb.getRank() + 1);
                        } else {
                            cb.setSelectLike(1);
                            cb.setRank(cb.getRank() - 1);
                        }
                    }

                } catch (ExceptionNotLoggedIn e) {
                    Helper.logError(this, e.getMessage(), e);
                } catch (IOException e) {
                    Helper.logError(this, e.getMessage(), e);
                }
            }
            return isConnected;
        }

        protected void onPostExecute(Boolean isConnected) {
            showProgress(false);
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
                                        Intent itMain = new Intent(mMainActivity, MainActivity.class);
                                        itMain.putExtra(MainActivity.INTENT_VAR_CURRENT_CONTENT, MainActivity.LOGIN);
                                        getActivity().startActivityForResult(itMain, 1);
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

    public boolean isListCharged() {
        return listCharged;
    }
}
