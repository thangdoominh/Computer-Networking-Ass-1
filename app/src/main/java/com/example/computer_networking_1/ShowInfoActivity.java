package com.example.computer_networking_1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

import net.glxn.qrgen.android.QRCode;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;

public class ShowInfoActivity extends AppCompatActivity {

    private static final int selfPort = 8080;
    private Server myServer;
    private FirebaseUser currentUser;
    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference reference;
    public void setConnected(User user) {
        Intent data = new Intent();
        data.putExtra("user",user);
        setResult(RESULT_OK,data);
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_info);

        TextView ipView = findViewById(R.id.ipDisplay);
        TextView portView = findViewById(R.id.portDisplay);

        String ip_address = getSelfIpAddress();
        ipView.setText(ip_address);
        portView.setText(Integer.toString(selfPort));

        Bitmap myBitmap = QRCode.from(ip_address+":"+selfPort).bitmap();
        ImageView myImage = (ImageView) findViewById(R.id.qr_view);
        myImage.setImageBitmap(myBitmap);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onResume() {
        super.onResume();
        myServer = new Server(this, getSelfIpAddress(), getSelfPort());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (myServer != null)
            myServer.onDestroy();
        currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            if(ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) updateOnlineStatus("offline");
            else updateOnlineStatus("online");
        }
    }

    public static int getSelfPort() {
        return selfPort;
    }

    // Returns device IP Address
    public static String getSelfIpAddress() {
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
    private void updateOnlineStatus(String online_status){
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("online_status",online_status);
        currentUserID = mAuth.getCurrentUser().getUid();
        reference.child("Users").child(currentUserID).updateChildren(hashMap);
    }
}
