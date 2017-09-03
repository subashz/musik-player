package com.blacpythoz.musik.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;

import com.blacpythoz.musik.R;

/**
 * Created by deadsec on 9/3/17.
 */

public class SearchActivity  extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=this.getMenuInflater();
        menuInflater.inflate(R.menu.search_menu,menu);

        MenuItem menuItem=menu.findItem(R.id.searchSongItem);
        SearchView searchView = (SearchView)menuItem.getActionView();
        searchView.setIconified(false);

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //adapter.filter(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
}
