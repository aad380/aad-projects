package com.example.odudarenko.test1;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final int SECOND_ACTIVITY = 1;
    private TextView mTextView;
    private EditText mEditText_firstname;
    private EditText mEditText_lastname;
    private int count = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEditText_firstname = (EditText)findViewById(R.id.editText_firstname);
        mEditText_lastname = (EditText)findViewById(R.id.editText_lastname);
        mTextView = (TextView)findViewById(R.id.textView);
        Button mButton = (Button)findViewById(R.id.button);
        assert mButton != null : "Can't find button.";
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count++;
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });
    }

    public void onClick(View view) {
        view.setAlpha(view.getAlpha() == 1.0f ? 0.5f : 1.0f);
        mTextView.setText("Name: " + mEditText_firstname.getText() + "; count: " + count);
    }

    public void changeColor(View view) {
        switch (view.getId()) {
        case R.id.buttonRed:
            //mTextView.setBackgroundColor(getResources().getColor(R.color.redColor));
            //mRelativeLayout.setBackgroundColor(getResources().getColor(R.color.redColor, null));
            mTextView.setBackgroundColor(ContextCompat.getColor(this, R.color.redColor));
            break;
        case R.id.buttonGreen:
            mTextView.setBackgroundColor(getResources().getColor(R.color.greenColor));
            break;
        case R.id.buttonBlue:
            mTextView.setBackgroundColor(getResources().getColor(R.color.blueColor));
            break;
        case R.id.buttonYellow:
            mTextView.setBackgroundColor(getResources().getColor(R.color.yellowColor));
            break;
        }
    }

    public void sendMessage(View view) {
        Intent intent = new Intent(MainActivity.this, SecondActivity.class);
        intent.putExtra("message", "<b>" + mEditText_firstname.getText() + " " + mEditText_lastname.getText() + "</b> приветствует вас");
        //startActivity(intent);
        startActivityForResult(intent, SECOND_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SECOND_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                String answer = data.getStringExtra("answer");
                mEditText_lastname.setText(answer);
            }else {
                mEditText_lastname.setText("?"); // стираем текст
            }
        }
    }
}
