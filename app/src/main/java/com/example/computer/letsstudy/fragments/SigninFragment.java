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
import android.widget.TextView;

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
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class SigninFragment extends Fragment implements View.OnClickListener{

    private Button mButtonLogin;
    private EditText inputEmail, inputPassword;
    private TextInputLayout inputLayoutEmail, inputLayoutPassword;
    private String mEmail, mPassword;
    private boolean login;

    private TextView error_text_message;

    public static ArrayList<UserInfo> usersList = new ArrayList<UserInfo>();
    //private static

    public SigninFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_signin, container, false);

        mButtonLogin = (Button) rootView.findViewById(R.id.btn_signin);

        inputEmail = (EditText) rootView.findViewById(R.id.input_email);
        inputPassword = (EditText) rootView.findViewById(R.id.input_password);
        error_text_message = (TextView) rootView.findViewById(R.id.error_text_message);
        error_text_message.setText("");

        inputLayoutEmail = (TextInputLayout) rootView.findViewById(R.id.input_layout_email);
        inputLayoutPassword = (TextInputLayout) rootView.findViewById(R.id.input_layout_password);

        inputEmail.addTextChangedListener(new MyTextWatcher(inputEmail));
        inputPassword.addTextChangedListener(new MyTextWatcher(inputPassword));

        mButtonLogin.setOnClickListener(this);

        login = false;

        return rootView;
    }

    @Override
    public void onClick(View view)
    {
        mEmail = inputEmail.getText().toString();
        mPassword = inputPassword.getText().toString();

        if(submitForm()) {
            getDataFromWeb();
        }
    }

    //login to app
    private void LoginToApp(String fullname)
    {
        error_text_message.setText("");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("email", inputEmail.getText().toString());
        editor.putString("fullname", fullname);
        editor.putString("password", inputPassword.getText().toString());
        editor.commit();
        Intent i = new Intent(getActivity(), HomeActivity.class);
        startActivity(i);
    }

    //error login
    private void failToLogin()
    {
        error_text_message.setText("The email or password is incorrect");
    }

    private boolean submitForm()
    {
        if (!validateEmail()) {
            return false;
        }

        if (!validatePassword()) {
            return false;
        }

        return true;
    }

    public class FetchDataTask extends AsyncTask<Void, Void, UserInfo>
    {
        private final String LOG_TAG = FetchDataTask.class.getSimpleName();
        ProgressDialog progressDialog;

        private UserInfo getUsersDataFromJson(String usersDataJsonStr) throws JSONException
        {
            UserInfo User = new UserInfo();

            // There are the names of the JSON objects that need to be extracted
            final String LS_USER = "user";
            final String LS_FULL_NAME = "fullname";
            final String LS_EMAIL = "email";
            final String LS_PASSWORD = "password";
            final String LS_SUCCESS = "status";

            JSONObject usersJson = new JSONObject(usersDataJsonStr);
            String isSuccess = usersJson.getString(LS_SUCCESS);

            if (isSuccess.equals("success"))
            {
                JSONObject userObject = usersJson.getJSONObject(LS_USER);
                login = true;
                // Read information of each user
                // the information that we need
                String full_name;
                String email;
                String password;

                full_name = userObject.getString(LS_FULL_NAME);
                email = userObject.getString(LS_EMAIL);
                password = userObject.getString(LS_PASSWORD);

                User.Full_Name = full_name;
                User.Email = email;
                User.Password = password;
            }else{
                login = false;
            }

            return User;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(SigninFragment.this.getActivity(), "Processing", "Please wait...");
        }

        @Override
        protected UserInfo doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String infoJsonStr = null;

            try{
                final String INFO_BASE_URL = "https://giapponese.herokuapp.com/signin.json?";
                final String EMAIL_PARAM = "session[email]";
                final String PASSWORD_PARAM = "session[password]";

                Uri buildUri = Uri.parse(INFO_BASE_URL).buildUpon()
                        .appendQueryParameter(EMAIL_PARAM, mEmail)
                        .appendQueryParameter(PASSWORD_PARAM, mPassword).build();

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
                Log.v("Signin", infoJsonStr);
            } catch (IOException e){
                Log.e("LetsStudy", "Signin", e);
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
            if(login)
            {
                LoginToApp(userInfos.Full_Name);
            }else{
                failToLogin();
            }
        }
    }

    private void getDataFromWeb()
    {
        FetchDataTask fetchDataTask = new FetchDataTask();
        fetchDataTask.execute();
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
        } else {
            inputLayoutPassword.setErrorEnabled(false);
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
                case R.id.input_email:
                    validateEmail();
                    break;
                case R.id.input_password:
                    validatePassword();
                    break;
            }
        }
    }
}
