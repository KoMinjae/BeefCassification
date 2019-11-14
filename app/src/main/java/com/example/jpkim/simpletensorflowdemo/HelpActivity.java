package com.example.jpkim.simpletensorflowdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SlidingView sv = new SlidingView(this);
        View v1 = View.inflate(this, R.layout.help,null);
        View v2 = View.inflate(this, R.layout.help1,null);
        View v3 = View.inflate(this, R.layout.help2,null);
        View v4 = View.inflate(this, R.layout.help3,null);
        View v5 = View.inflate(this, R.layout.help4,null);
        View v6 = View.inflate(this, R.layout.help5,null);
        View v7 = View.inflate(this, R.layout.help6,null);
        View v8 = View.inflate(this, R.layout.help7,null);
        View v9 = View.inflate(this, R.layout.help8,null);
        View v10 = View.inflate(this, R.layout.help9,null);
        sv.addView(v1);
        sv.addView(v2);
        sv.addView(v3);
        sv.addView(v4);
        sv.addView(v5);
        sv.addView(v6);
        sv.addView(v7);
        sv.addView(v8);
        sv.addView(v9);
        sv.addView(v10);
        setContentView(sv);
    }
}
