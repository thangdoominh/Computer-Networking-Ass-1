package com.example.computer_networking_1;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DialogViewActivity extends AppCompatActivity
        implements DialogsListAdapter.OnDialogClickListener<Dialog> {
    private DatabaseReference reference;

    private static final int SHOW_INFO = 100;
    private static final int ENTER_INFO = 200;
    private static final int LOGOUT = 400;
    private static final int SETTINGS = 500;
    private static final int FINDFRIEND = 600;
    private static final int CHAT_ACTIVITY = 300;
    protected ImageLoader imageLoader;
    static DialogsListAdapter<Dialog> dialogsAdapter;
    DialogsList dialogsList;
    FloatingActionButton fab1, fab2, fab3;
    boolean isFABOpen;

    static User me, user;
    private final static String SHARED_PREFERENCES_KEY_USER_SELF = "ME";
    private final static String SHARED_PREFERENCES_KEY_DIALOG = "DIALOG_INFO";
    private static String PREFERENCE_FILE_KEY_SELF = "SELF_INFO";
    private static String PREFERENCE_FILE_KEY_DIALOGS = "DIALOG_LIST";
    SharedPreferences sharedPrefSelf, sharedPrefDialog;
    SharedPreferences.Editor editorUser, editorDialog;
    Gson gson;


    private FloatingActionMenu fam;
    private FloatingActionButton fabShowInfo, fabEnterInfo, fabLogout, fabSettings, fabFindFriend, fabGroup;

    static List<Dialog> dialogArrayList;
    boolean loaded = false, saved = false;
    TextView overlay;

    String image;

    private FirebaseUser currentUser;
    private String currentUserID;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_view);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference();
        //add nek

        image = "https://cdn1.imggmi.com/uploads/2019/10/19/5bf1857add4ee9b72b31257e2adb9030-full.png";

        imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url, Object payload) {
                Picasso.get().load(url).into(imageView);
//                Glide.with(getApplicationContext()).asBitmap().load(R.drawable.dialog).into(imageView);
//                Glide.with(getApplicationContext())
//                        .load(Uri.parse("android.resource://"+R.class.getPackage().getName()+"/" +R.drawable.dialog).toString())
//                        .into(imageView);
            }
        };

        dialogsList = (DialogsList) findViewById(R.id.dialogsList);
        dialogsAdapter = new DialogsListAdapter<>(imageLoader);
        dialogsAdapter.setOnDialogClickListener(this);
        dialogsList.setAdapter(dialogsAdapter);

        gson = new Gson();
        sharedPrefSelf = this.getSharedPreferences(PREFERENCE_FILE_KEY_SELF, Context.MODE_PRIVATE);
        editorUser = sharedPrefSelf.edit();
        String jsonDataStringSelfUser = sharedPrefSelf.getString(SHARED_PREFERENCES_KEY_USER_SELF, "");
        me = gson.fromJson(jsonDataStringSelfUser, User.class);


        overlay = findViewById(R.id.overlay);
        if (dialogsAdapter.isEmpty())
            overlay.setVisibility(View.VISIBLE);

        fabShowInfo = findViewById(R.id.showInfoFab);
        fabEnterInfo = findViewById(R.id.enterInfoFab);
        fabLogout = findViewById(R.id.logoutFab);
        fabSettings = findViewById(R.id.settingsFab);
        fabFindFriend = findViewById(R.id.findFriendFab);
        fabGroup = findViewById(R.id.createGroupFab);
        fam = findViewById(R.id.fab_menu);
        fabShowInfo.setOnClickListener(onButtonClick());
        fabEnterInfo.setOnClickListener(onButtonClick());
        fabLogout.setOnClickListener(onButtonClick());
        fabSettings.setOnClickListener(onButtonClick());
        fabFindFriend.setOnClickListener(onButtonClick());
        fabGroup.setOnClickListener(onButtonClick());

        loaded = false;
        saved = false;
        dialogArrayList = new ArrayList<>();
        updateOnlineStatus("online");
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (loaded == false) {
            sharedPrefDialog = this.getSharedPreferences(PREFERENCE_FILE_KEY_DIALOGS, MODE_PRIVATE);
            editorDialog = sharedPrefDialog.edit();
            String jsonDataStringDialogArray = sharedPrefDialog.getString(SHARED_PREFERENCES_KEY_DIALOG, "");
            Log.e("DialogArrar", jsonDataStringDialogArray);
            if ((jsonDataStringDialogArray != null || jsonDataStringDialogArray != "null") && jsonDataStringDialogArray.length() > 2) {
                Dialog dialodArray[] = gson.fromJson(jsonDataStringDialogArray, Dialog[].class);
                if (dialodArray != null) {
                    for (Dialog d : dialodArray) {
                        dialogArrayList.add(d);
                    }
                    dialogsAdapter.addItems(dialogArrayList);
                    overlay.setVisibility(View.INVISIBLE);
                }
            }
            loaded = true;
        }
    }

    @Override

    protected void onPause() {
        if (saved == false) {
            Log.e("PAUSE", "SAVE");
            String jsonDataString = gson.toJson(dialogArrayList);
            Log.e("PAUSE", jsonDataString);
            editorDialog.putString(SHARED_PREFERENCES_KEY_DIALOG, jsonDataString);
            editorDialog.commit();
            saved = true;
        }
        super.onPause();
        currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            if(ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) updateOnlineStatus("offline");
            else updateOnlineStatus("online");
        }
    }

    @Override
    public void onBackPressed() {
        if (saved == false) {
            Log.e("BACK", "SAVE");
            String jsonDataString = gson.toJson(dialogArrayList);
            Log.e("PAUSE", jsonDataString);
            editorDialog.putString(SHARED_PREFERENCES_KEY_DIALOG, jsonDataString);
            editorDialog.commit();
            saved = true;
        }
        loaded = false;
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (saved == false) {
            Log.e("DESTROY", "SAVE");
            String jsonDataString = gson.toJson(dialogArrayList);
            editorDialog.putString(SHARED_PREFERENCES_KEY_DIALOG, jsonDataString);
            editorDialog.commit();
            saved = true;
        }
    }

    private View.OnClickListener onButtonClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view == fabShowInfo) {
                    Intent intent = new Intent(getApplicationContext(), ShowInfoActivity.class);
                    startActivityForResult(intent, SHOW_INFO);
                } else if (view == fabEnterInfo) {
                    Intent intent = new Intent(getApplicationContext(), ConnectToUserActivity.class);
                    startActivityForResult(intent, ENTER_INFO);
                }
                else if (view == fabLogout) {
                    updateOnlineStatus("offline");
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivityForResult(intent, LOGOUT);
                    finish();
                    //startActivityForResult(intent, ENTER_INFO);
                }
                else if (view == fabSettings) {
                    Intent settingsIntent = new Intent(DialogViewActivity.this, SettingsActivity.class);
                    settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(settingsIntent);
                    finish();

                }
                else if (view == fabSettings) {
                    Intent settingsIntent = new Intent(DialogViewActivity.this, SettingsActivity.class);
                    settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(settingsIntent);
                    finish();

                }
                else if (view == fabFindFriend) {
                    Intent settingsIntent = new Intent(DialogViewActivity.this, FindFriendActivity.class);
                    //settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(settingsIntent);
                    //finish();

                }
                else if (view == fabGroup) {
                    Intent createGroupIntent = new Intent(DialogViewActivity.this, GroupChatFullActivity.class);
                    //settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(createGroupIntent);
                    finish();


                }
                fam.close(true);
            }
        };
    }

    private void RequestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DialogViewActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter Group Name: ");

        final EditText groupNameField = new EditText(DialogViewActivity.this);
        groupNameField.setHint("e.g Thieu nu team");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String groupName = groupNameField.getText().toString();

                if (TextUtils.isEmpty(groupName))
                {
                    Toast.makeText(DialogViewActivity.this, "Please write Group Name ...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(DialogViewActivity.this, "DKM", Toast.LENGTH_SHORT).show();
                    CreateNewGroup(groupName);
                    Intent createGroupIntent = new Intent(DialogViewActivity.this, GroupChatFullActivity.class);
                    //settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(createGroupIntent);
                    finish();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.show();
    }

    private void CreateNewGroup(final String groupName) {
        reference.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(DialogViewActivity.this,groupName+ "is successful", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == SHOW_INFO || requestCode == ENTER_INFO) && data != null) {
            if (resultCode == RESULT_OK) {
                user = (User) data.getSerializableExtra("user");
                Dialog dialog = dialogsAdapter.getItemById(user.getName());
                if (dialog == null) {
                    dialog = new Dialog(user.getName(), user.getName(), image, new ArrayList<User>(Arrays.asList(user)), null, 0);
                    dialogsAdapter.addItem(0, dialog);

                    dialogArrayList = new ArrayList<>();
                    dialogArrayList.add(dialog);

                    String jsonDataStringDialog = gson.toJson(dialogArrayList);
                    editorDialog.putString(SHARED_PREFERENCES_KEY_DIALOG, jsonDataStringDialog);
                    editorDialog.commit();

                    overlay.setVisibility(View.INVISIBLE);
                }
                onDialogClick(dialog);
            }
        }
    }

    @Override
    public void onDialogClick(Dialog dialog) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra("user", dialog.getUsers().get(0));
        intent.putExtra("dialog", dialog);
        startActivity(intent);
    }

    private void updateOnlineStatus(String online_status){
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("online_status",online_status);
        currentUserID = mAuth.getCurrentUser().getUid();
        reference.child("Users").child(currentUserID).updateChildren(hashMap);
    }
}