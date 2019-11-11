package com.example.computer_networking_1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import com.squareup.picasso.Picasso;


import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;
import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private Button UpdateAccountSettings,backSettings;
    private EditText userName, userStatus;
    private TextView userid;
    private CircleImageView userProfileImage;

    private String currentUserID;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference reference;

    private static final int GalleryPick = 1;
    private StorageReference UserProfileImagesRef;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference();
        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");


        InitializeFields();
        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });

        backSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToDialogViewActivity();
            }
        });
        // chọn ảnh cho profile
        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);

            }
        });
        RetrieveUserInfo();

    }
    @Override
    protected void onStart() {
        super.onStart();
        updateOnlineStatus("online");
    }
    @Override
    protected void onPause() {
        super.onPause();
        currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            if(ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) updateOnlineStatus("offline");
            else updateOnlineStatus("online");
        }
    }


    private void RetrieveUserInfo() {
        userid.setText(currentUserID);
        reference.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.hasChild("username") && dataSnapshot.hasChild("imageURL")){

                            if(dataSnapshot.child("imageURL").getValue().toString().equals("default"))
                            {
                                String retrieveUserName = dataSnapshot.child("username").getValue().toString();
                                String retrieveUserStatus = dataSnapshot.child("status").getValue().toString();

                                userName.setText(retrieveUserName);
                                userStatus.setText(retrieveUserStatus);
                            }
                            else{
                                String retrieveUserName = dataSnapshot.child("username").getValue().toString();
                                String retrieveUserStatus = dataSnapshot.child("status").getValue().toString();
                                String retrieveProfileImage = dataSnapshot.child("imageURL").getValue().toString();

                                userName.setText(retrieveUserName);
                                userStatus.setText(retrieveUserStatus);

                                // ------- API load profile image ---------
                                Picasso.get().load(retrieveProfileImage).into(userProfileImage);
                            }
                        }
                        else{
                            Toast.makeText(SettingsActivity.this,"Please update your profile information",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void UpdateSettings(){
        final String setUserName = userName.getText().toString();
        String setStatus = userStatus.getText().toString();
        if (TextUtils.isEmpty(setUserName)){
            Toast.makeText(this,"Please write your username...",Toast.LENGTH_SHORT).show();
        }
        else {
            HashMap<String,String> profileMap = new HashMap<>();
            profileMap.put("username", setUserName);
            profileMap.put("status", setStatus);
            reference.child("Users").child(currentUserID).child("status").setValue(setStatus)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                reference.child("Users").child(currentUserID).child("username").setValue(setUserName)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    SendUserToDialogViewActivity();
                                                    Toast.makeText(SettingsActivity.this,"Profile updated successfully!",Toast.LENGTH_SHORT).show();
                                                }
                                                else{
                                                    String message = task.getException().toString();
                                                    Toast.makeText(SettingsActivity.this, "Error: " + message,Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                            else{
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error: " + message,Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void SendUserToDialogViewActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, DialogViewActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


    private void InitializeFields() {
        UpdateAccountSettings = (Button) findViewById(R.id.update_settings_button);
        backSettings = (Button) findViewById(R.id.back_settings_button);
        userid = (TextView) findViewById(R.id.user_id);
        userName = (EditText) findViewById(R.id.set_user_name);
        userStatus = (EditText) findViewById(R.id.set_profile_status);
        userProfileImage = (CircleImageView) findViewById(R.id.profile_image);
        loadingBar = new ProgressDialog(this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        ------------ API Select and crop image --------------
        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null)
        {
            Uri ImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }       // ------ End of API -----------

        // --------------- Upload ảnh lên storage trên Firebase  -----------------
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK){
                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait, your profile image is updating ...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getOriginalUri();

                StorageReference filePath = UserProfileImagesRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        taskSnapshot
                                .getStorage()
                                .getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        final String downloadUrl = uri.toString();

                                        reference.child("Users").child(currentUserID).child("imageURL")
                                                .setValue(downloadUrl)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            Toast.makeText(SettingsActivity.this, " Image save, Successfull ...", Toast.LENGTH_SHORT).show();
                                                            loadingBar.dismiss();
                                                        }
                                                        else{
                                                            String message = task.getException().toString();
                                                            Toast.makeText(SettingsActivity.this, "Error :" + message, Toast.LENGTH_SHORT).show();
                                                            loadingBar.dismiss();
                                                        }
                                                    }
                                                });
                                    }
                                });
                    }
                });
            }
        } // --------  End of upload and storage profile image -----------------

    }
    private void updateOnlineStatus(String online_status){
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("online_status",online_status);
        currentUserID = mAuth.getCurrentUser().getUid();
        reference.child("Users").child(currentUserID).updateChildren(hashMap);
    }
}
