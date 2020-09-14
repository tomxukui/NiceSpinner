package com.ablingbling.app.nicespinner;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.ablingbling.library.nicespinner.NiceSpinner;

import java.util.Arrays;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String[] CITYS = new String[]{"台州市", "椒江区", "黄岩区", "路桥区", "玉环市", "三门县", "天台县", "仙居县", "温岭市", "临海市"};

    private NiceSpinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinner = findViewById(R.id.spinner);
        spinner.attachDataSource(Arrays.asList(CITYS));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });
    }

}
