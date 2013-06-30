package com.androidexplorer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.fakkudroid.R;

public class AndroidExplorerActivity extends SherlockListActivity {

    private List<String> item = null;
    private List<String> path = null;
    private String root = "/";
    private TextView myPath;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_explorer);
        myPath = (TextView) findViewById(R.id.path);
        getDir(root);
    }

    private void getDir(String dirPath)
    {
        myPath.setText("Location: " + dirPath);
        item = new ArrayList<String>();
        path = new ArrayList<String>();

        File f = new File(dirPath);
        File[] files = f.listFiles();
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return Integer.valueOf(f1.getAbsoluteFile().compareTo(f2.getAbsoluteFile()));
            }
        });
        if (!dirPath.equals(root))
        {
            item.add(root);
            path.add(root);
            item.add("../");
            path.add(f.getParent());
        }

        for (int i = 0; i < files.length; i++)
        {
            File file = files[i];
            if (file.isDirectory()){
                path.add(file.getPath());
                item.add(file.getName() + "/");
            }
        }

        ArrayAdapter<String> fileList =
                new ArrayAdapter<String>(this, R.layout.row_android_explorer, item);
        setListAdapter(fileList);
    }


    @Override

    protected void onListItemClick(ListView l, View v, int position, long id) {

        File file = new File(path.get(position));
        if (file.isDirectory())
        {
            if (file.canRead())
                getDir(path.get(position));
            else
            {
                new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_launcher)
                        .setTitle(getResources().getString(R.string.location_android_explorer).replace("@location", file.getName()))
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // TODO Auto-generated method stub
                                    }
                                }).show();
            }
        }
    }
}