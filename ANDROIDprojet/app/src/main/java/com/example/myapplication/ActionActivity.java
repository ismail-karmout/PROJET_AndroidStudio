package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.myapplication.adapter.AdapterActivity;
import com.example.myapplication.db.MyDataBase;
import com.example.myapplication.model.Activity;

import java.util.List;

public class ActionActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    MyDataBase mydb = new MyDataBase(ActionActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action);

        recyclerView = findViewById(R.id.idRecHistory);
        List<Activity> activities = mydb.getActivities();
        AdapterActivity adapter = new AdapterActivity(this, activities);
        recyclerView.setAdapter(adapter);
    }
    //Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    //clicked menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch ((item.getItemId())){
            case R.id.idEditProfile:
                startActivity(new Intent(ActionActivity.this, EditProfileActivity.class));
                return true;
            case R.id.idMaps:
                startActivity(new Intent(ActionActivity.this, LocationActivity.class));
                return true;
            case R.id.idLogout:
                startActivity(new Intent(ActionActivity.this, MainActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}