package com.example.wikiaudio.activates.login_acticity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.wikiaudio.R;
import com.example.wikiaudio.wikipedia.WikiServerHolder;
import com.example.wikiaudio.wikipedia.Wikipedia;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {

    String userName;
    String password;
    WikiServerHolder wikiServerHolder;
    private Wikipedia wikipedia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        wikipedia = new Wikipedia(this);
        wikiServerHolder = WikiServerHolder.getInstance();

        // todo for debugging only
        userName = "tomer_ronen";
        password = "xTGHTibZAL3cBws";
        wikipedia.login(userName, password);
    }
}


