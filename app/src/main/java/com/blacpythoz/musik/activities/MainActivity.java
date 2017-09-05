package com.blacpythoz.musik.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.blacpythoz.musik.R;
import com.blacpythoz.musik.adapters.SectionsPageAdapter;
import com.blacpythoz.musik.fragments.AlbumListFragment;
import com.blacpythoz.musik.fragments.ArtistListFragment;
import com.blacpythoz.musik.fragments.PlayListFragment;
import com.blacpythoz.musik.fragments.SongListFragment;
import com.blacpythoz.musik.services.MusicService;

public class MainActivity extends AppCompatActivity {
    private SectionsPageAdapter sectionsPageAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    MusicService musicService;
    Intent playIntent;
    boolean boundService =false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (checkPermission()) {
            handleAllView();
        }
    }

    // Service is also created in MainActivity so that
    // it can create the notification before unbinding to the service
    // When main activiy gets stopped, it create the notifcation if service is active,
    // so that service can play the music
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected (ComponentName componentName, IBinder iBinder){
            MusicService.MusicBinder binder = (MusicService.MusicBinder) iBinder;
            musicService = binder.getService();
            // dont show the notification
            musicService.toBackground();
            boundService = true;
        }
        @Override
        public void onServiceDisconnected (ComponentName componentName){ }
    };

    // initialize all the things for services
    @Override
    protected void onStart() {
        super.onStart();
        playIntent = new Intent(this,MusicService.class);
        playIntent.setAction("");
        bindService(playIntent,serviceConnection,Context.BIND_AUTO_CREATE);
        startService(playIntent);
    }

    public void handleAllView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sectionsPageAdapter=new SectionsPageAdapter(getSupportFragmentManager());
        viewPager=(ViewPager)findViewById(R.id.container);
        setupViewPager(viewPager);
        tabLayout=(TabLayout)findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsPageAdapter adapter=new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new SongListFragment(),"All Songs");
        adapter.addFragment(new AlbumListFragment(),"Albums");
        adapter.addFragment(new ArtistListFragment(),"Artist");
        adapter.addFragment(new PlayListFragment(),"PlayList");
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent k=new Intent(this, SettingsActivity.class);
            startActivity(k);
            return true;
        } else if(id==R.id.searchSongItem) {
            Intent search=new Intent(this,SearchActivity.class);
            startActivity(search);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean checkPermission() {
        if(Build.VERSION.SDK_INT >= 23) {
            if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},123);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==123) {
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                handleAllView();
            } else {
                this.finish();
            }
        }else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // this is the main for switching the
    // service to the background process
    @Override
    protected void onStop() {
        super.onStop();
        if(boundService) {
            if(musicService.isPlaying()) {
                musicService.toForeground();
            } else {
                stopService(playIntent);
            }
            unbindService(serviceConnection);
            boundService = false;
        }
    }
}

