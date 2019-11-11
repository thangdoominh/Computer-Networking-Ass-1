package com.example.computer_networking_1;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Button CreateAccountButton;
    private EditText UserEmail, UserPassword;
    private TextView AlreadyHaveAccountLink;

    private FirebaseAuth mAuth;
    private DatabaseReference reference;
    private ProgressDialog loadingBar;


    Button showInfoBtn, enterInfoBtn, continueBtn;
    EditText nameInput;
    User me; // Assign Self Username

    private final static String SHARED_PREFERENCES_KEY_USER_SELF = "ME";
    private static String PREFERENCE_FILE_KEY = "SELF_INFO";
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    Gson gson;
    private static final int selfPort = 8080;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //add
        mAuth = FirebaseAuth.getInstance();

        InitializeFields();
        //add nek
        AlreadyHaveAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLoginActivity();

            }
        });

//        showInfoBtn = findViewById(R.id.showInfo);
//        enterInfoBtn = findViewById(R.id.enterInfo);
        nameInput = findViewById(R.id.nameInput);


        gson = new Gson();
        sharedPref = this.getSharedPreferences(
                PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        String jsonDataString = sharedPref.getString(SHARED_PREFERENCES_KEY_USER_SELF, "");
        me = gson.fromJson(jsonDataString, User.class);

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(nameInput.getText().toString().length() < 1){
                    Snackbar snackbar = Snackbar
                            .make(nameInput, "Please Enter Username", Snackbar.LENGTH_LONG);
                    snackbar.show();
                    return;
                }
                me = new User("1", nameInput.getText().toString());
                String jsonDataString = gson.toJson(me);
                editor.putString(SHARED_PREFERENCES_KEY_USER_SELF, jsonDataString);
                editor.commit();

                CreateNewAccount();

            }
        });

//        showInfoBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(nameInput.getText().toString().length() < 1){
//                    Snackbar snackbar = Snackbar
//                            .make(nameInput, "Please Enter Username", Snackbar.LENGTH_LONG);
//                    snackbar.show();
//                    return;
//                }
//                me = new User("1", nameInput.getText().toString());
//                Intent intent = new Intent(getApplicationContext(), ShowInfoActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        enterInfoBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(nameInput.getText().toString().length() < 1){
//                    Snackbar snackbar = Snackbar
//                            .make(nameInput, "Please Enter Username", Snackbar.LENGTH_LONG);
//                    snackbar.show();
//                    return;
//                }
//                me = new User("1", nameInput.getText().toString());
//                Intent intent = new Intent(getApplicationContext(), ConnectToUserActivity.class);
//                startActivity(intent);
//            }
//        });
    }

    private void CreateNewAccount() {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();
        final String username = nameInput.getText().toString();
        final String ip_address = getSelfIpAddress();
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please enter your email!"+username,Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please enter your password!",Toast.LENGTH_SHORT).show();
        }
        if(password.length() < 6){
            Toast.makeText(this,"Password must be at least 6 characters",Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                assert firebaseUser != null;
                                String userid = firebaseUser.getUid();
                                reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
                                HashMap<String,String> hashMap = new HashMap<>();
                                hashMap.put("id",userid);
                                hashMap.put("imageURL","default");
                                hashMap.put("ip",ip_address);
                                hashMap.put("port",Integer.toString(selfPort));
                                if(TextUtils.isEmpty(username)) hashMap.put("username", userid);
                                else hashMap.put("username", username);
                                hashMap.put("status", "I'm online");

                                reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(MainActivity.this, "Account created"+username,Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(getApplicationContext(), DialogViewActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                });
                                //Toast.makeText(MainActivity.this, "Account created"+username,Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else{
                                String message = task.getException().toString();
                                Toast.makeText(MainActivity.this, "Error: " + message,Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private String getSelfIpAddress() {
        String self_ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress
                            .nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        self_ip = inetAddress.getHostAddress();
                    }
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
            Log.e("GET_IP", "IP NOT FOUND");
        }
        return self_ip;
    }
    public static int getSelfPort() {
        return selfPort;
    }

    private void SendUserToDialogView() {

        Intent intent = new Intent(getApplicationContext(), DialogViewActivity.class);
        //settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void InitializeFields() {
        CreateAccountButton = (Button) findViewById(R.id.continue_btn);
        UserEmail = (EditText) findViewById(R.id.register_email);
        UserPassword = (EditText) findViewById(R.id.register_password);
        nameInput = (EditText) findViewById(R.id.nameInput);
        AlreadyHaveAccountLink = (TextView) findViewById(R.id.already_have_account_link);
        loadingBar = new ProgressDialog(this);
        continueBtn = findViewById(R.id.continue_btn);
    }
    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }
}
