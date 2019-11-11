package com.example.computer_networking_1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.io.Console;

public class LoginActivity extends AppCompatActivity {
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference reference;

    private Button LoginButton;
    private EditText UserEmail,UserPassword;
    private TextView RegisterLink, ForgetPasswordLink;
    User me;
    Gson gson;
    private final static String SHARED_PREFERENCES_KEY_USER_SELF = "ME";
    private static String PREFERENCE_FILE_KEY = "SELF_INFO";
    //add
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    //add nek
    //FrameLayout progressOverlay;
    private User user;
    private Client myClient;

    public void setUser(User user) {
        this.user = user;
        Intent data = new Intent();
        data.putExtra("user",user);
        setResult(RESULT_OK,data);
        //progressOverlay.setVisibility(View.INVISIBLE);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        //add
        //progressOverlay = findViewById(R.id.progress_overlay);
        gson = new Gson();
        sharedPref = this.getSharedPreferences(
                PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        String jsonDataString = sharedPref.getString(SHARED_PREFERENCES_KEY_USER_SELF, "");
        me = gson.fromJson(jsonDataString, User.class);


        InitializeFields();
        RegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToRegisterActivity();
            }
        });
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowUserToLogin();

            }
        });
    }
//    @Override
//    protected void onResume() {
//        super.onResume();
//        progressOverlay.setVisibility(View.INVISIBLE);
//        if (myClient != null && !myClient.isCancelled())
//            myClient.cancel(true);
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (myClient != null && !myClient.isCancelled())
//            myClient.cancel(true);
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (myClient != null && !myClient.isCancelled())
//            myClient.cancel(true);
//    }

    private void AllowUserToLogin() {
        //add


        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please enter your email!",Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please enter your password!",Toast.LENGTH_SHORT).show();
        }
        if(password.length() < 6){
            Toast.makeText(this,"Password must be at least 6 characters",Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Loading");
            loadingBar.setMessage("Please wait...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                //
                                //me = new User("1", nameInput.getText().toString());
                                // userRef;

                                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                String userid = currentUser.getUid();
                                reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
                                String username= reference.child("username").toString();
                                //Toast.makeText("lan2nek",Toast.LENGTH_SHORT).show();
                                String ip_address=reference.child("ip").toString();
                                String port=reference.child
                                ("port").toString();
                                me = new User("1", "lan2nek");
                                String jsonDataString = gson.toJson(me);
                                editor.putString(SHARED_PREFERENCES_KEY_USER_SELF, jsonDataString);
                                editor.commit();
                                SendUserToMainActivity();

                                //add nek
//                                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
//                                imm.hideSoftInputFromWindow(portInput.getWindowToken(), 0);
//                                progressOverlay.setVisibility(View.VISIBLE);
////                                myClient = new Client(ip_address, Integer.parseInt(port), this );
////                                myClient.execute();
                                Toast.makeText(LoginActivity.this,"Login successful.",Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                Intent intent = new Intent(getApplicationContext(), DialogViewActivity.class);
                                startActivity(intent);
                                finish();

                            }
                            else{
                                String message = task.getException().toString();
                                Toast.makeText(LoginActivity.this, "Error: " + message,Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void InitializeFields() {
        LoginButton = (Button) findViewById(R.id.login_button);
        UserEmail = (EditText) findViewById(R.id.login_email);
        UserPassword = (EditText) findViewById(R.id.login_password);
        RegisterLink = (TextView) findViewById(R.id.register_link);
        ForgetPasswordLink = (TextView) findViewById(R.id.forget_password_link);
        loadingBar = new ProgressDialog(this);
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        if(currentUser != null){
//            SendUserToMainActivity();
//        }
//    }

    private void SendUserToMainActivity() {
        Intent intent = new Intent(getApplicationContext(), DialogViewActivity.class);
        startActivity(intent);
        finish();
    }
    private void SendUserToRegisterActivity() {
        Intent registerIntent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(registerIntent);
    }


}