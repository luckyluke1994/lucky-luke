package com.example.computer.letsstudy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class CategoriesActivity extends AppCompatActivity {

    RecyclerView Categories_List;
    LinearLayoutManager mLinearLayoutManager;
    private ArrayList<CategoriesInfo> mCategoriesList;
    private CategoriesAdapter mCategoriesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        Toolbar toolbar = (Toolbar) findViewById(R.id.categories_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Categories_List = (RecyclerView) findViewById(R.id.categories_recycler_view);
        Categories_List.setHasFixedSize(true);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        Categories_List.setLayoutManager(mLinearLayoutManager);

        new FetchCategoriesDataTask().execute();

        mCategoriesList = new ArrayList<CategoriesInfo>();
    }

    private void addAdapterForRecyclerView()
    {
        mCategoriesAdapter = new CategoriesAdapter(mCategoriesList);
        Categories_List.setAdapter(mCategoriesAdapter);
        CategoriesAdapter.OnItemClickListener onItemClickListener = new CategoriesAdapter.OnItemClickListener()
        {
            @Override
            public void onItemClick(View view, int position) {
                TextView title_category = (TextView) view.findViewById(R.id.title_categories);
                Intent i = new Intent(CategoriesActivity.this, TestLession.class);
                i.putExtra("category", title_category.getText().toString());
                startActivity(i);
            }
        };
        mCategoriesAdapter.setOnItemClickListener(onItemClickListener);
    }

    public class FetchCategoriesDataTask extends AsyncTask<Void, Void, ArrayList<CategoriesInfo>>
    {
        private final String LOG_TAG = FetchCategoriesDataTask.class.getSimpleName();
        ProgressDialog progressDialog;

        private ArrayList<CategoriesInfo> getCategoriesDataFromJson(String categoriesDataJsonStr) throws JSONException
        {
            ArrayList<CategoriesInfo> categoriesList = new ArrayList<CategoriesInfo>();

            // There are the names of the JSON objects that need to be extracted
            final String LS_CATEGORIES = "categories";
            final String LS_NAME = "name";
            final String LS_DESCRIPTION = "description";

            JSONObject categoriesJson = new JSONObject(categoriesDataJsonStr);
            JSONArray categoriesArray = categoriesJson.getJSONArray(LS_CATEGORIES);

            // Read information of each category
            for(int i=0; i < categoriesArray.length(); i++)
            {
                // the information that we need
                String name;
                String description;

                JSONObject categoryObject = categoriesArray.getJSONObject(i);
                name = categoryObject.getString(LS_NAME);
                description = categoryObject.getString(LS_DESCRIPTION);

                CategoriesInfo categoryInfo = new CategoriesInfo();
                categoryInfo.title = name;
                categoryInfo.info = description;
                categoryInfo.icon = R.drawable.ic_person_white_24dp;

                categoriesList.add(categoryInfo);
            }

            return categoriesList;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(CategoriesActivity.this, "Processing", "Please wait...");
        }

        @Override
        protected ArrayList<CategoriesInfo> doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String infoJsonStr = null;

            try{
                final String INFO_BASE_URL = "https://giapponese.herokuapp.com/categories.json";
                URL url = new URL(INFO_BASE_URL);

                // Create the request to server, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a string
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if(null == inputStream)
                {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while((line = reader.readLine()) != null)
                {
                    buffer.append(line + "\n");
                }

                if(buffer.length() == 0){
                    return null;
                }

                infoJsonStr = buffer.toString();
                Log.v("Categories", infoJsonStr);
            } catch (IOException e){
                Log.e("LetsStudy", "Error", e);
                return null;
            }finally {
                if(null != urlConnection)
                {
                    urlConnection.disconnect();
                }
                if(null != reader)
                {
                    try{
                        reader.close();
                    }catch (final IOException e){
                        Log.e("LesStudy", "Error closing stream", e);
                    }
                }
            }

            try{
                return getCategoriesDataFromJson(infoJsonStr);
            } catch (JSONException e){
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<CategoriesInfo> categoriesInfos) {
            progressDialog.dismiss();
            if(null != categoriesInfos)
            {
                mCategoriesList.clear();
                for(int i=0; i<categoriesInfos.size(); i++)
                {
                    CategoriesInfo categoryInfo = new CategoriesInfo();
                    categoryInfo.title = categoriesInfos.get(i).title;
                    categoryInfo.info = categoriesInfos.get(i).info;
                    categoryInfo.icon = categoriesInfos.get(i).icon;

                    mCategoriesList.add(categoryInfo);
                }
                addAdapterForRecyclerView();
            }
        }
    }
}
