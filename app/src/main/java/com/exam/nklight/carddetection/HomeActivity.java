package com.exam.nklight.carddetection;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class HomeActivity extends AppCompatActivity {
    Button btnScan;
    Button btnChoose;
    private static final int PERMISSION_REQUEST = 235;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        btnScan = findViewById(R.id.btn_scan);
        btnChoose = findViewById(R.id.btn_choose);

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent intent = new Intent(HomeActivity.this, ResultActivity.class);
               startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        requestPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(PERMISSION_REQUEST)
    private void requestPermission() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(HomeActivity.this, perms)) {
            // ok to go
        } else {
            EasyPermissions.requestPermissions(HomeActivity.this, "Need camera and write stogare to continue!", PERMISSION_REQUEST, perms);
        }
    }

}
