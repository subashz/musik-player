package com.blacpythoz.musik.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.blacpythoz.musik.R;
import com.blacpythoz.musik.adapters.AlbumAdapter;
import com.blacpythoz.musik.adapters.SectionsPageAdapter;
import com.blacpythoz.musik.fragments.AlbumListFragment;
import com.blacpythoz.musik.fragments.ArtistListFragment;
import com.blacpythoz.musik.fragments.PlayListFragment;
import com.blacpythoz.musik.fragments.SongListFragment;
import com.blacpythoz.musik.fragments.SongPlayerFragment;


public class PlayerActivity extends MusicServiceActivity {
    public static final String TAG = PlayerActivity.class.getSimpleName();
    private ViewPager viewPager;
    private TabLayout tabLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
    }

    @Override
    public void onServiceConnected() {
        handleAllView();
    }

    public void handleAllView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        viewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        Fragment songPlayerFragment = getSupportFragmentManager().findFragmentById(R.id.main_content);
        if (songPlayerFragment == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.main_content, new SongPlayerFragment(), "SongPlayer").commit();
            Log.d(TAG, "songPlayerFragment Fragment new created");
        } else {
            Log.d(TAG, "songPlayerFragment Fragment reused ");
        }

    }


    private void setupViewPager(ViewPager viewPager) {
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        SongListFragment songListFragment;
        AlbumListFragment albumListFragment;
        PlayListFragment playListFragment;
        ArtistListFragment artistListFragment;
        songListFragment = new SongListFragment();
        albumListFragment = new AlbumListFragment();
        artistListFragment = new ArtistListFragment();
        playListFragment = new PlayListFragment();
        adapter.addFragment(songListFragment, "All Songs");
        adapter.addFragment(albumListFragment, "Albums");
        adapter.addFragment(artistListFragment, "Artist");
        adapter.addFragment(playListFragment, "PlayList");
        viewPager.setAdapter(adapter);
    }

    public Fragment findWithId(int id) {
        return getSupportFragmentManager().findFragmentById(id);
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
            Intent k = new Intent(this, SettingsActivity.class);
            startActivity(k);
            return true;
        } else if (id == R.id.searchSongItem) {
            Intent search = new Intent(this, SearchActivity.class);
            startActivity(search);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}

