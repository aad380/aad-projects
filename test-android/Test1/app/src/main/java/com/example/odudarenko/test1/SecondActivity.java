package com.example.odudarenko.test1;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // set message
        TextView mTextView = (TextView)findViewById(R.id.textView_second);
        mTextView.setText(getIntent().getStringExtra("message"));
    }

    public void onRadioClick(View v) {
        Intent answerIntent = new Intent();
        switch (v.getId()) {
            case R.id.radioDog:
                answerIntent.putExtra("answer", "Сраный пёсик");
                break;
            case R.id.radioCrow:
                answerIntent.putExtra("answer", "Ворона");
                break;
            case R.id.radioCat:
                answerIntent.putExtra("answer", "Лошадь Пржевальского");
                break;
            default:
                break;
        }
        setResult(RESULT_OK, answerIntent);
        finish();
    }


}
