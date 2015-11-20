package com.example.computer.letsstudy;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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

public class WordsListActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Spinner categorySpinner, statusSpinner;
    private ArrayList<String> categories;

    private RecyclerView mWordList;
    private LinearLayoutManager mLinearLayoutManager;
    private ArrayList<Word> mWords;
    private WordAdapter mWordAdapter;

    private String mEmail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.words_list_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        categories = new ArrayList<String>();
        mWords = new ArrayList<Word>();
        new CategoriesDataTask().execute();

        //init Recyclerview
        mWordList = (RecyclerView) findViewById(R.id.words_list);
        mWordList.setHasFixedSize(true);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mWordList.setLayoutManager(mLinearLayoutManager);

        //init Spinner
        categorySpinner = (Spinner) findViewById(R.id.category);
        statusSpinner = (Spinner) findViewById(R.id.status);

        //get email of user
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mEmail = preferences.getString("email", "");
    }

    private void addAdapterForSpinner()
    {
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(this, R.array.status, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);

        categorySpinner.setOnItemSelectedListener(this);
        statusSpinner.setOnItemSelectedListener(this);
    }

    private void addAdapterForRecyclerView()
    {
        mWordAdapter = new WordAdapter(mWords);
        mWordList.setAdapter(mWordAdapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        new FetchWordDataTask().execute(String.valueOf(categorySpinner.getSelectedItem()), String.valueOf(statusSpinner.getSelectedItem()));
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public class CategoriesDataTask extends AsyncTask<Void, Void, ArrayList<String>>
    {
        private final String LOG_TAG = CategoriesDataTask.class.getSimpleName();

        private ArrayList<String> getCategoriesDataFromJson(String categoriesDataJsonStr) throws JSONException
        {
            ArrayList<String> categoriesList = new ArrayList<String>();

            // There are the names of the JSON objects that need to be extracted
            final String LS_CATEGORIES = "categories";
            final String LS_NAME = "name";

            JSONObject categoriesJson = new JSONObject(categoriesDataJsonStr);
            JSONArray categoriesArray = categoriesJson.getJSONArray(LS_CATEGORIES);

            // Read information of each category
            for(int i=0; i < categoriesArray.length(); i++)
            {
                // the information that we need
                String name;

                JSONObject categoryObject = categoriesArray.getJSONObject(i);
                name = categoryObject.getString(LS_NAME);

                categoriesList.add(name);
            }

            return categoriesList;
        }

        @Override
        protected ArrayList<String> doInBackground(Void... params) {
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
        protected void onPostExecute(ArrayList<String> categoriesInfos) {
            if(null != categoriesInfos)
            {
                categories.clear();
                categories.add("All");
                for(int i=0; i<categoriesInfos.size(); i++)
                {
                    categories.add(categoriesInfos.get(i));
                }
                addAdapterForSpinner();
            }
        }
    }

    public class FetchWordDataTask extends AsyncTask<String, Void, ArrayList<Word>>
    {
        private final String LOG_TAG = FetchWordDataTask.class.getSimpleName();
        ProgressDialog progressDialog;

        private ArrayList<Word> getWordDataFromJson(String wordDataJsonStr) throws JSONException
        {
            ArrayList<Word> wordList = new ArrayList<Word>();

            // There are the names of the JSON objects that need to be extracted
            final String LS_WORDS = "words";
            final String LS_MEAN = "answers";
            final String LS_CONTENT = "content";
            final String LS_CORRECT = "correct";

            JSONObject wordsJson = new JSONObject(wordDataJsonStr);
            JSONArray wordArray = wordsJson.getJSONArray(LS_WORDS);

            for(int j=0; j<wordArray.length(); j++)
            {
                JSONObject wordObject = wordArray.getJSONObject(j);
                String content = wordObject.getString(LS_CONTENT);
                JSONArray answersArr = wordObject.getJSONArray(LS_MEAN);

                // Read information of each user
                for(int i=0; i < answersArr.length(); i++)
                {

                    JSONObject answer = answersArr.getJSONObject(i);
                    if (answer.getInt(LS_CORRECT) == 1) {
                        Word wordInfo = new Word();
                        wordInfo.word = content;
                        wordInfo.mean = answer.getString(LS_CONTENT);

                        wordList.add(wordInfo);
                        break;
                    }
                }
            }


            return wordList;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(WordsListActivity.this, "Processing", "Please wait...");
        }

        @Override
        protected ArrayList<Word> doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String infoJsonStr = null;

            try{
                final String INFO_BASE_URL = "http://giapponese.heroku.com/words.json?";
                final String CATEGORY_NAME = "category_name";
                final String LEARNED = "learned";
                final String EMAIL = "email";
                int learn;

                if(params[1].toString() == "all")
                {
                    learn = 3;
                }else if(params[1].toString() == "learned"){
                    learn = 1;
                }else {
                    learn = 2;
                }

                Uri buildUri = Uri.parse(INFO_BASE_URL).buildUpon()
                        .appendQueryParameter(EMAIL, mEmail)
                        .appendQueryParameter(CATEGORY_NAME, params[0])
                        .appendQueryParameter(LEARNED, Integer.toString(learn)).build();

                URL url = new URL(buildUri.toString());

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
                Log.v("WordsListActivity", infoJsonStr);
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
                return getWordDataFromJson(infoJsonStr);
            } catch (JSONException e){
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Word> wordInfos) {
            progressDialog.dismiss();
            if(null != wordInfos)
            {
                mWords.clear();
                for(int i=0; i<wordInfos.size(); i++)
                {
                    Word word = new Word();
                    word.word = wordInfos.get(i).word;
                    word.mean = wordInfos.get(i).mean;
                    mWords.add(word);
                }
                addAdapterForRecyclerView();
            }
        }
    }
}
