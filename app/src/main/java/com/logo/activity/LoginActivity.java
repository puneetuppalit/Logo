package com.logo.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.logo.R;
import com.logo.application.LogoApplication;
import com.logo.bo.User;
import com.logo.coremanager.CoreManager;
import com.logo.database.manager.UserManager;
import com.logo.services.manager.AlertManager;
import com.logo.services.manager.ApiManager;
import com.logo.services.manager.DeviceManager;
import com.logo.services.manager.InternetManager;

import org.json.JSONObject;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends LogoActivity  {

    LogoApplication logoApplication;
    CoreManager coreManager;
    UserManager userManager;
    AlertManager alertManager;
    InternetManager internetManager;
    DeviceManager deviceManager;
    ApiManager apiManager;

    Context context;
    EditText editTextEmail, editTextMobile;
    Button buttonLogin;
    TextView textViewCreateAccount,textViewForgotPassword;
    ImageView imageViewGoogle,imageViewFaceBook;

    User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
    }

    public void init(){
        logoApplication = getLogoApplication();
        coreManager = logoApplication.getCoreManager();
        userManager = coreManager.getUserManager();
        alertManager = coreManager.getAlertManager();
        internetManager = coreManager.getInternetManager();
        deviceManager = coreManager.getDeviceManager();
        apiManager = coreManager.getApiManager();

        context = this;

        editTextEmail = (EditText) findViewById(R.id.et_email);
        editTextMobile = (EditText) findViewById(R.id.et_mobile);
        buttonLogin = (Button) findViewById(R.id.bt_login);
        textViewCreateAccount = (TextView) findViewById(R.id.tv_create_account);
        textViewForgotPassword = (TextView) findViewById(R.id.tv_forgot_password);
        imageViewFaceBook = (ImageView) findViewById(R.id.iv_fb);
        imageViewGoogle = (ImageView) findViewById(R.id.iv_google);

        buttonLogin.setOnClickListener(onClickListener);
        textViewCreateAccount.setOnClickListener(onClickListener);
        imageViewGoogle.setOnClickListener(onClickListener);
        imageViewFaceBook.setOnClickListener(onClickListener);
        textViewForgotPassword.setOnClickListener(onClickListener);
    }

    OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.bt_login){

                validation();
            }
            if(v.getId() == R.id.tv_create_account){
                startActivity(new Intent(context,SignUpActivity.class));
                finish();
            }
            if(v.getId() == R.id.iv_google){
                alertManager.alert("Comming Soon","Info",context,null);
            }
            if(v.getId() == R.id.iv_fb){
                alertManager.alert("Comming Soon","Info",context,null);
            }
            if(v.getId() == R.id.tv_forgot_password){
                alertManager.alert("Comming Soon","Info",context,null);
            }
        }
    };

    public void validation(){
        String email = editTextEmail.getText().toString();
        String mobile = editTextMobile.getText().toString();

        deviceManager.hideKeypad(editTextEmail,this);
        boolean isValid = true;
        boolean nextStep = true;
        if(email.trim().equals("") && mobile.trim().equals("")){
            isValid = false;
            nextStep = false;
            alertManager.alert("Please enter details", "Info",this,null);
        }
        

        if(nextStep && email.trim().equals("") ){
            isValid = false;
            nextStep = false;
            alertManager.alert("Please enter Email", "Info",this,null);
        }

        if(nextStep && mobile.trim().equals("") ){
            isValid = false;
            nextStep = false;
            alertManager.alert("Please enter Password", "Info",this,null);
        }

        if(nextStep && !email.trim().contains("@") ){
            isValid = false;
            nextStep = false;
            alertManager.alert("Please enter Valid email", "Info",this,null);
        }



        if(isValid){
            if(internetManager.isInternet(this)){
                user = new User();
                user.setEmail(email);
                user.setPassword(mobile);
                new SignInProcess().execute();
            }else{
                alertManager.alert("Please check your Internet","Info",this,null);
            }
        }
    }

    public class SignInProcess extends AsyncTask<Object, Object, Object> {
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Loading");
            progressDialog.show();

        }

        @Override
        protected Object doInBackground(Object... objects) {
            JSONObject jsonObject = null;

            jsonObject = apiManager.signInApi(user);

            return jsonObject;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            try {
                JSONObject jsonObject = new JSONObject(o.toString());
                if(jsonObject != null){
                    progressDialog.dismiss();
                    Log.i("result", jsonObject.toString());
                    if(jsonObject.has("errorCode")){
                        if(jsonObject.getInt("errorCode") != 0 && jsonObject.has("errorDetail")){
                            alertManager.alert(jsonObject.getString("errorDetail"),"Error",context,null);
                        }else{
                            if(jsonObject.has(user.USERID)){
                                user.setUserId(jsonObject.getInt(user.USERID));
                            }else{
                                user.setUserId(0);
                            }

                            if(jsonObject.has(user.USERNAME)){
                                user.setUsername(jsonObject.getString(user.USERNAME));
                            }else{
                                user.setUsername("");
                            }

                            if(jsonObject.has(user.TOKEN)){
                                user.setAuthToken(jsonObject.getString(user.TOKEN));
                            }else{
                                user.setAuthToken("");
                            }

                            userManager.addUser(user);
                            startActivity(new Intent(context,MainActivity.class));
                            finish();
                        }
                    }
                }else {
                    alertManager.alert("Something wrong","Server error",context,null);
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


}

