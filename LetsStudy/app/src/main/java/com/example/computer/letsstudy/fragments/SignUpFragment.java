package com.example.computer.letsstudy.fragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.example.computer.letsstudy.HomeActivity;
import com.example.computer.letsstudy.R;
import com.example.computer.letsstudy.UserInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignUpFragment extends Fragment implements View.OnClickListener{

    private Button mButtonSignup;
    private EditText inputName, inputEmail, inputPassword, inputRetypePassword;
    private TextInputLayout inputLayoutName, inputLayoutEmail, inputLayoutPassword, inputLayoutRetypePassword;

    private String name, email, password;

    private boolean signup;

    public SignUpFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_sign_up, container, false);

        signup = false;

        mButtonSignup = (Button) rootView.findViewById(R.id.btn_signup);
        // reference edittext
        inputName = (EditText) rootView.findViewById(R.id.input_name);
        inputEmail = (EditText) rootView.findViewById(R.id.input_email);
        inputPassword = (EditText) rootView.findViewById(R.id.input_password);
        inputRetypePassword = (EditText) rootView.findViewById(R.id.input_retype_password);
        //reference layout of edittext to display error
        inputLayoutName = (TextInputLayout) rootView.findViewById(R.id.input_layout_name);
        inputLayoutEmail = (TextInputLayout) rootView.findViewById(R.id.input_layout_email);
        inputLayoutPassword = (TextInputLayout) rootView.findViewById(R.id.input_layout_password);
        inputLayoutRetypePassword = (TextInputLayout) rootView.findViewById(R.id.input_layout_retype_password);
        //add listener to edittext to handler event type of user
        inputName.addTextChangedListener(new MyTextWatcher(inputName));
        inputEmail.addTextChangedListener(new MyTextWatcher(inputEmail));
        inputPassword.addTextChangedListener(new MyTextWatcher(inputPassword));
        inputRetypePassword.addTextChangedListener(new MyTextWatcher(inputRetypePassword));

        mButtonSignup.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View view)
    {
        name = inputName.getText().toString();
        email = inputEmail.getText().toString();
        password = inputPassword.getText().toString();

        if(submitForm()) {
            signup = true;
        }
    }

    private boolean submitForm()
    {
        if (!validateName()) {
            return false;
        }

        if (!validateEmail()) {
            return false;
        }

        if (!validatePassword()) {
            return false;
        }

        if(!validateRetypePassword()){
            return false;
        }

        new FetchDataTask().execute();

        return true;
    }

    public class FetchDataTask extends AsyncTask<Void, Void, UserInfo>
    {
        private final String LOG_TAG = FetchDataTask.class.getSimpleName();
        ProgressDialog progressDialog;

        private UserInfo getUsersDataFromJson(String usersDataJsonStr) throws JSONException
        {
            UserInfo userInfo = new UserInfo();

            // There are the names of the JSON objects that need to be extracted
            final String LS_USERS = "user";
            final String LS_FULL_NAME = "fullname";
            final String LS_EMAIL = "email";
            final String LS_PASSWORD = "password";

            JSONObject usersJson = new JSONObject(usersDataJsonStr);
            JSONObject userObject = usersJson.getJSONObject(LS_USERS);

            // Read information of each user
            // the information that we need
            String full_name;
            String email;
            String password;

            full_name = userObject.getString(LS_FULL_NAME);
            email = userObject.getString(LS_EMAIL);
            password = userObject.getString(LS_PASSWORD);

            userInfo.Full_Name = full_name;
            userInfo.Email = email;
            userInfo.Password = password;

            return userInfo;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(SignUpFragment.this.getActivity(), "Processing", "Please wait...");
        }

        @Override
        protected UserInfo doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String infoJsonStr = null;

            try{
                final String INFO_BASE_URL = "https://giapponese.herokuapp.com/users.json?";
                final String EMAIL_PARAM = "user[email]";
                final String FULL_NAME_PARAM = "user[fullname]";
                final String PASSWORD_PARAM = "user[password]";

                Uri buildUri = Uri.parse(INFO_BASE_URL).buildUpon()
                        .appendQueryParameter(FULL_NAME_PARAM,name)
                        .appendQueryParameter(EMAIL_PARAM, email)
                        .appendQueryParameter(PASSWORD_PARAM, password).build();

                URL url = new URL(buildUri.toString());

                // Create the request to server, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
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
                Log.v("Signup", infoJsonStr);
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
                return getUsersDataFromJson(infoJsonStr);
            } catch (JSONException e){
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(UserInfo userInfos) {
            progressDialog.dismiss();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("email", email);
            editor.putString("fullname", name);
            editor.commit();
            if(signup) {
                Intent i = new Intent(getActivity(), HomeActivity.class);
                startActivity(i);
            }
        }
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

    private boolean validateEmail() {
        String email = inputEmail.getText().toString().trim();

        if (email.isEmpty() || !isValidEmail(email)) {
            inputLayoutEmail.setError(getString(R.string.err_msg_email));
            requestFocus(inputEmail);
            return false;
        } else {
            inputLayoutEmail.setErrorEnabled(false);
        }

        return true;
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

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
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
                case R.id.input_email:
                    validateEmail();
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
}
