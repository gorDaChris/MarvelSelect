package com.example.marvelselect;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class NameTeam extends AppCompatActivity {

    //UI
    EditText teamName;
    Button save;

    //constant keys
    public static final String EXTRA_NAME = "com.example.application.example.EXTRA_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_team);

        //find views
        save = findViewById(R.id.id_nameteam_button);

        //OnClickListener for save button
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //goes back to Locker class
                openLocker();
            }
        });
    }//onCreate

    //method to send user to Locker
    private void openLocker() {
        //find view for EditText
        teamName = findViewById(R.id.id_nameteam_edittext);
        //gets String data from EditText
        String text = teamName.getText().toString();

        //intent declaration
        Intent intent = new Intent();
        //bundle of intent
        Bundle bundle = getIntent().getExtras();
        //position of button clicked on ListView
        int position = bundle.getInt("position", 0);
        //name of team
        intent.putExtra(EXTRA_NAME, text);
        //position of button clicked on ListView
        intent.putExtra("position", position);
        intent.putExtra("fromMain", false);
        setResult(RESULT_OK, intent);
        finish();
    }//openLocker
}