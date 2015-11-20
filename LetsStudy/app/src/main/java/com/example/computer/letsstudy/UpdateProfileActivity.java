package com.example.computer.letsstudy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateProfileActivity extends AppCompatActivity {

    Toolbar mToolbar;
    EditText inputEmail, inputName, inputOldPassword, inputPassword, inputRetypePassword;
    private TextInputLayout inputLayoutName, inputLayoutOldPassword, inputLayoutPassword, inputLayoutRetypePassword;

    private String mOldPassword;

    private boolean isModifySuccess, mIsUpdate;
    private String mEmail, mFullName, mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //init ui component
        inputEmail = (EditText) findViewById(R.id.input_email);
        inputEmail.setText(getIntent().getExtras().getString("email"));
        inputEmail.setEnabled(false);

        mEmail = inputEmail.getText().toString();
        mIsUpdate = false;

        inputName = (EditText) findViewById(R.id.input_name);
        inputOldPassword = (EditText) findViewById(R.id.input_old_password);
        inputPassword = (EditText) findViewById(R.id.input_password);
        inputRetypePassword = (EditText) findViewById(R.id.input_retype_password);

        //init layout ui component to show error
        inputLayoutName = (TextInputLayout) findViewById(R.id.input_layout_name);
        inputLayoutOldPassword = (TextInputLayout) findViewById(R.id.input_layout_old_password);
        inputLayoutPassword = (TextInputLayout) findViewById(R.id.input_layout_password);
        inputLayoutRetypePassword = (TextInputLayout) findViewById(R.id.input_layout_retype_password);

        //add listener to edittext to handler event type of user
        inputName.addTextChangedListener(new MyTextWatcher(inputName));
        inputOldPassword.addTextChangedListener(new MyTextWatcher(inputOldPassword));
        inputPassword.addTextChangedListener(new MyTextWatcher(inputPassword));
        inputRetypePassword.addTextChangedListener(new MyTextWatcher(inputRetypePassword));

        isModifySuccess = false;

        getOldPassword();
    }

    private void getOldPassword()
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mOldPassword = preferences.getString("password", "");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_update_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.done)
        {
            if(submitForm())
            {
                new UpdateTask().execute();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void UpdateInfo()
    {
        if(mIsUpdate)
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("email", mEmail);
            editor.putString("fullname", mFullName);
            editor.commit();
            Toast.makeText(this, "Update successfully", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, HomeActivity.class);
            startActivity(i);
        }else{
            Toast.makeText(this, "Update unsuccessfully", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, HomeActivity.class);
            startActivity(i);
        }
    }

    private boolean submitForm()
    {
        mFullName = inputName.getText().toString();
        mPassword = inputPassword.getText().toString();

        if (!validateName()) {
            return false;
        }

        if (!validatePassword()) {
            return false;
        }

        if(!validateRetypePassword()){
            return false;
        }

        if(!validateOldPassword()){
            return false;
        }

        return true;
    }

    private boolean validateName() {
        if (inputName.getText().toString().trim().isEmpty()) {
            inputLayoutName.setError(getString(R.string.err_msg_name));
            requestFocus(inputName);
            return false;
        } else if(inputName.getText().toString().length() >= 5 && inputName.getText().toString().length() <= 50){
            inputLayoutName.setErrorEnabled(false);
            return true;
        }else{
            return false;
        }
    }

    private boolean validatePassword() {
        if (inputPassword.getText().toString().trim().isEmpty()) {
            inputLayoutPassword.setError(getString(R.string.err_msg_password));
            requestFocus(inputPassword);
            return false;
        } else if(inputPassword.getText().toString().length() >= 5 && inputPassword.getText().toString().length() <= 50){
            inputLayoutPassword.setErrorEnabled(false);
            return true;
        }else{
            return false;
        }
    }

    private boolean validateRetypePassword()
    {
        if(!inputRetypePassword.getText().toString().trim().equals(inputPassword.getText().toString().trim())){
            inputLayoutRetypePassword.setError(getString(R.string.err_msg_retype_password));
            requestFocus(inputRetypePassword);
            return false;
        } else {
            inputLayoutRetypePassword.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateOldPassword()
    {
        if (inputOldPassword.getText().toString().trim().isEmpty()) {
            inputLayoutOldPassword.setError(getString(R.string.err_msg_password));
            requestFocus(inputOldPassword);
            return false;
        } else if(inputOldPassword.getText().toString().length() >= 5 && inputOldPassword.getText().toString().length() <= 50){
            inputLayoutOldPassword.setErrorEnabled(false);
            if(inputOldPassword.getText().toString().equals(mOldPassword)) {
                return true;
            }else{
                return false;
            }
        }else{
            inputLayoutOldPassword.setError(getString(R.string.err_msg_old_password));
            return false;
        }
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.input_name:
                    validateName();
                    break;
                case R.id.input_old_password:
                    validateOldPassword();
                    break;
                case R.id.input_password:
                    validatePassword();
                    break;
                case R.id.input_retype_password:
                    validateRetypePassword();
                    break;
            }
        }
    }

    public class UpdateTask extends AsyncTask<Void, Void, String>
    {
        private final String LOG_TAG = UpdateTask.class.getSimpleName();
        ProgressDialog progressDialog;

        private String getStatusUpdateFromJson(String updateDataJsonStr) throws JSONException
        {
            String isUpdate;

            // There are the names of the JSON objects that need to be extracted
            final String LS_STATUS = "status";

            JSONObject usersJson = new JSONObject(updateDataJsonStr);
            isUpdate = usersJson.getString(LS_STATUS);

            return isUpdate;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(UpdateProfileActivity.this, "Processing", "Please wait...");
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String infoJsonStr = null;

            try{
                final String INFO_BASE_URL = "https://giapponese.herokuapp.com/users/1.json?";
                final String FULLNAME_PARAM = "user[fullname]";
                final String EMAIL_PARAM = "user[email]";
                final String PASSWORD_PARAM = "user[password]";

                Uri buildUri = Uri.parse(INFO_BASE_URL).buildUpon()
                        .appendQueryParameter(FULLNAME_PARAM, mFullName)
                        .appendQueryParameter(EMAIL_PARAM, mEmail)
                        .appendQueryParameter(PASSWORD_PARAM, mPassword).build();

                URL url = new URL(buildUri.toString());

                // Create the request to server, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("PATCH");
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
                Log.v("Update", infoJsonStr);
            } catch (IOException e){
                Log.e("LetsStudy", "Update", e);
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
                return getStatusUpdateFromJson(infoJsonStr);
            } catch (JSONException e){
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String updateInfos) {
            progressDialog.dismiss();
            if(updateInfos.equals("updated"))
            {
                mIsUpdate = true;
            }else{
                mIsUpdate = false;
            }
            UpdateInfo();
        }
    }
}
