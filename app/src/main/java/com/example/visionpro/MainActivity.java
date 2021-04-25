package com.example.visionpro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.app.Notification;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;
    private FirebaseAuth mAuth;
    private static final String API_KEY="0e45995084ea4300b148222edd3da5c2";
    private static final String API_ENDPOINT="https://southeastasia.api.cognitive.microsoft.com/vision/v3.0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth=FirebaseAuth.getInstance();

        init();
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(Color.rgb(250,46,100));
        toolbar.setTitle("");
        Toast.makeText(this, toolbar.getTitle(), Toast.LENGTH_SHORT).show();
        Drawer();
        navigationView();

    }
    void Drawer(){

        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(this,drawer,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

    }
    void navigationView(){
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.Image_Analysis:
                        startActivity(new Intent(MainActivity.this,ImageAnalysisActivity.class));
                        Toast.makeText(MainActivity.this, "Image Analysis", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.logout:
                        mAuth.signOut();
                        Toast.makeText(MainActivity.this, "Logout Successful", Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(MainActivity.this,LoginActivity.class);
                        startActivity(intent);
                        finish();
                    break;
                    default:
                        Toast.makeText(MainActivity.this, "Ehhhhh! Wrong Choice", Toast.LENGTH_SHORT).show();
                }

                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();


    }

    void init(){
        drawer=findViewById(R.id.drawerLayout);
        toolbar=findViewById(R.id.toolBar);
        navigationView=findViewById(R.id.navigationView);
    }
}