package com.larswerkman.colorpicker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockActivity;
import com.fakkudroid.*;
import com.fakkudroid.R;
import com.fakkudroid.util.Constants;

/**
 * Created by cesar on 30/06/13.
 */
public class ColorPickerActivity extends SherlockActivity{

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        setContentView(R.layout.activity_color_picker);

        final ColorPicker picker = (ColorPicker) findViewById(R.id.picker);
        final SVBar svBar = (SVBar) findViewById(R.id.svbar);

        picker.addSVBar(svBar);

        String currentColor = prefs.getString("default_color", Constants.DEFAULT_COLOR);

        picker.setColor(Integer.valueOf(currentColor, 16).intValue());
        //To set the old selected color u can do it like this
        picker.setOldCenterColor(picker.getColor());


        Button btnDefault = (Button)findViewById(R.id.btnDefault);
        btnDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefs.edit().putString("default_color", Constants.DEFAULT_COLOR).commit();
                finish();
            }
        });

        Button btnSelectColor = (Button)findViewById(R.id.btnSelectColor);
        btnSelectColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String hexColor = Integer.toHexString(picker.getColor()) + "";
                prefs.edit().putString("default_color", hexColor.substring(2)).commit();
                finish();
            }
        });
    }

}
