package com.example.computer_networking_1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID, senderUserID, current_State;
    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus, userId;
    private Button SendMessageRequestButton, DeclineMessageRequestButton;

    private DatabaseReference UserRef, ChatRequestRef, ContactsRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String currentUserID;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        senderUserID = mAuth.getCurrentUser().getUid();

        GetInformationProfile();

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

    private void GetInformationProfile() {

        userProfileImage = findViewById(R.id.visit_profile_image);
        userProfileName = findViewById(R.id.visit_profile_name);
        userId = findViewById(R.id.visit_user_id);
        userProfileStatus = findViewById(R.id.visit_profile_status);
        SendMessageRequestButton = findViewById(R.id.visit_profile_send_message_request);
        DeclineMessageRequestButton = findViewById(R.id.visit_profile_decline_message_request);
        current_State = "new";

    }

    private void RetrieveUserInfo() {
        UserRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.exists()) && (dataSnapshot.hasChild("imageURL"))) {
                    String userImage = dataSnapshot.child("imageURL").getValue().toString();
                    String userName = dataSnapshot.child("username").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    String userGetId = dataSnapshot.child("id").getValue().toString();
                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                    userId.setText(userGetId);

                }
                else{
                    String userName = dataSnapshot.child("username").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    String userGetId = dataSnapshot.child("id").getValue().toString();
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                    userId.setText(userGetId);
                }

                //gửi request gửi tin nhắn
                ManageChatRequests();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ManageChatRequests() {

        ChatRequestRef.child(senderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(receiverUserID)){

                            String request_type = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();

                            if (request_type.equals("sent")){
                                current_State = "request_sent";
                                SendMessageRequestButton.setText("Cancel Chat Request");
                            }
                            else if(request_type.equals("received")){

                                current_State = "request_received";
                                SendMessageRequestButton.setText("Accept Request");

                                DeclineMessageRequestButton.setVisibility(View.VISIBLE);
                                DeclineMessageRequestButton.setEnabled(true);

                                DeclineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CancelChatRequest();
                                    }
                                });
                            }
                        }
                        // Khi không còn receiverUserID tức là đã thành bạn bè, nên không còn phân biệt người gửi và người nhận
                        else{
                            ContactsRef.child(senderUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(receiverUserID))
                                            {
                                                current_State = "friends";
                                                SendMessageRequestButton.setText("Remove This Contact");
                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

        if (!senderUserID.equals(receiverUserID)){
            SendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SendMessageRequestButton.setEnabled(false);
                    if (current_State.equals("new")){
                        SendChatRequest();
                    }
                    if (current_State.equals("request_sent")){
                        CancelChatRequest();
                    }
                    if (current_State.equals("request_received"))
                    {
                        AcceptChatRequest();
                    }
                    if (current_State.equals("friends"))
                    {
                        RemoveSpecificContact();
                    }
                }
            });
        }
        else{
            SendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void RemoveSpecificContact() {
        ContactsRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            ContactsRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                SendMessageRequestButton.setEnabled(true);
                                                current_State = "new";
                                                SendMessageRequestButton.setText("Send Message");

                                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptChatRequest() {
        ContactsRef.child(senderUserID).child(receiverUserID)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){

                            ContactsRef.child(receiverUserID).child(senderUserID)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){

                                                ChatRequestRef.child(senderUserID).child(receiverUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                ChatRequestRef.child(receiverUserID).child(senderUserID)
                                                                        .removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                SendMessageRequestButton.setEnabled(true);
                                                                                current_State = "friends";
                                                                                SendMessageRequestButton.setText("Remove This Contact");

                                                                                DeclineMessageRequestButton.setEnabled(false);
                                                                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE );
                                                                            }
                                                                        });
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void CancelChatRequest() {
        ChatRequestRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            ChatRequestRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                SendMessageRequestButton.setEnabled(true);
                                                current_State = "new";
                                                SendMessageRequestButton.setText("Send Message");

                                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void SendChatRequest() {
        ChatRequestRef.child(senderUserID).child(receiverUserID)
                .child("request_type")
                .setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            ChatRequestRef.child(receiverUserID).child(senderUserID)
                                    .child("request_type")
                                    .setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                SendMessageRequestButton.setEnabled(true);
                                                current_State = "request_sent";
                                                SendMessageRequestButton.setText("Cancel Chat Request");
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void updateOnlineStatus(String online_status){
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("online_status",online_status);
        currentUserID = mAuth.getCurrentUser().getUid();
        reference.child("Users").child(currentUserID).updateChildren(hashMap);
    }
}
