package com.example.computer_networking_1;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Bundle;

public class FindFriendActivity extends AppCompatActivity {

    //private Toolbar mToolbar;
    private androidx.appcompat.widget.Toolbar mToolbar;
    private RecyclerView FindFriendsRecyclerList;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);

        FindFriendsRecyclerList = (RecyclerView) findViewById(R.id.find_friends_recyclerview_list);
        FindFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this)) ;

        mToolbar = (Toolbar) findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

    }
}
