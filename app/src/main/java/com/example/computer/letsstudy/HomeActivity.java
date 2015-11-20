package com.example.computer.letsstudy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    FloatingActionButton mFloatingActionButton;
    RecyclerView list;
    LinearLayoutManager mLinearLayoutManager;
    TextView email_textview;
    TextView name_textview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        name_textview = (TextView) findViewById(R.id.name_textview);
        email_textview = (TextView) findViewById(R.id.email_textview);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final String email = preferences.getString("email", "");
        final String fullname = preferences.getString("fullname", "");

        //set information of the profile
        name_textview.setText(fullname);
        email_textview.setText(email);
        // button which go to activity update profile activity
        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), UpdateProfileActivity.class);
                i.putExtra("email", email);
                startActivity(i);
            }
        });
        //list recyclerview that display study process of user
        list = (RecyclerView) findViewById(R.id.list);
        list.setHasFixedSize(true);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        list.setLayoutManager(mLinearLayoutManager);

        CategoriesInfo array = new CategoriesInfo();
        array.title = "Basic";
        array.info = "learned 12/20";
        array.icon = R.drawable.ic_person_white_24dp;
        ArrayList<CategoriesInfo> mList = new ArrayList<CategoriesInfo>();
        mList.add(array);
        RecyclerView.Adapter mAdapter = new CategoriesAdapter(mList);
        list.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClickWords(View view)
    {
        Intent i = new Intent(getApplicationContext(), WordsListActivity.class);
        startActivity(i);
    }

    public void onClickLession(View view)
    {
        Intent i = new Intent(getApplicationContext(), CategoriesActivity.class);
        startActivity(i);
    }
}
