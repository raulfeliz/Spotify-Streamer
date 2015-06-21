package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;
import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.database.DatabaseHandler;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SearchArtistActivity extends AppCompatActivity {

    private SearchBox search;
    private Toolbar toolbar;
    SpotifyService spotify;
    SearchArtistActivityFragment searchArtistActivityFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_artist);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(null != toolbar) {
            setSupportActionBar(toolbar);

            if(getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }

        FragmentManager fm = getFragmentManager();
        searchArtistActivityFragment = (SearchArtistActivityFragment) fm.findFragmentByTag("search_fragment");

        // create the fragment and data the first time
        if (searchArtistActivityFragment == null) {
            // add the fragment
            searchArtistActivityFragment = new SearchArtistActivityFragment();
            fm.beginTransaction().add(R.id.search_artist_activity_container, searchArtistActivityFragment, "search_fragment").commit();
            // load the data from the spotify web
            //searchArtistActivityFragment.setData(loadMyData());
            fm.executePendingTransactions();
        }

        search = (SearchBox)findViewById(R.id.searchbox);
        search.enableVoiceRecognition(this);

        SpotifyApi api = new SpotifyApi();
        spotify = api.getService();

    }

    @Override
    public void onPostResume(){
        super.onPostResume();
        openSearch();
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_artist, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){
            case R.id.action_settings:
                Utilities.showToast(this, getResources().getString(R.string.comming_soon));
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_search:
                if(search.getVisibility() != View.VISIBLE)
                    openSearch();
                else
                    search.toggleSearch();
                return true;
            case R.id.action_delete_recent_searches:
                deleteRecentSearches();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed(){
        switch(search.getVisibility()) {
            case View.VISIBLE:
                search.toggleSearch();
                return;
            default:
                super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SearchBox.VOICE_RECOGNITION_CODE && resultCode == RESULT_OK) {
            ArrayList<String> matches =  data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if(matches.size()>0){
                ArrayList<String> mainMatch = new ArrayList<>();
                mainMatch.add(matches.get(0));
                search.populateEditText(mainMatch);
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void openSearch() {
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        search.setLogoText(getResources().getString(R.string.search_hint));
        search.setSearchString("");
        readStoredSearches();
        search.revealFromMenuItem(R.id.action_search, this);

        search.setMenuListener(new SearchBox.MenuListener() {

            @Override
            public void onMenuClick() {
                // Hamburger has been clicked
                search.toggleSearch();
            }

        });
        search.setSearchListener(new SearchBox.SearchListener() {


            @Override
            public void onSearchOpened() {
                // Use this to tint the screen

            }

            @Override
            public void onSearchClosed() {
                // Use this to un-tint the screen
                closeSearch();
            }

            @Override
            public void onSearchTermChanged() {
                // React to the search term changing
                // Called after it has updated results
            }

            @Override
            public void onSearch(String searchTerm) {
                TextView result = ((TextView) toolbar.findViewById(R.id.toolbar_subtitle));
                result.setText(searchTerm);
                //setSupportActionBar(toolbar);
                result.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                insertSerchable(searchTerm);
                String parsedSearch = searchTerm.replace(" ", "+");
                //parsedSearch = "*" + parsedSearch + "*";
                spotify.searchArtists(parsedSearch, new Callback<ArtistsPager>() {


                    @Override
                    public void success(ArtistsPager artistsPager, Response response) {
                        final List<ArtistItem> artists = new ArrayList<>();
                        for(int i=0; i<artistsPager.artists.items.size(); i++){
                            ArtistItem item = new ArtistItem();
                            item.setId(artistsPager.artists.items.get(i).id);
                            item.setName(artistsPager.artists.items.get(i).name);
                            for(int j=0; j<artistsPager.artists.items.get(i).images.size(); j++){
                                if(j == 0)
                                    item.setPicture(artistsPager.artists.items.get(i).images.get(j).url);
                                else
                                    if(artistsPager.artists.items.get(i).images.get(j).width == 200){
                                        item.setPicture(artistsPager.artists.items.get(i).images.get(j).url);
                                    }
                            }
                            artists.add(item);
                        }
                        Handler mainHandler = new Handler(getApplicationContext().getMainLooper());
                        mainHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                searchArtistActivityFragment.setArtists(artists);
                            }
                        });

                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("Album failure", error.toString());
                    }
                });
            }

            @Override
            public void onSearchCleared() {

            }

        });


    }

    protected void closeSearch() {
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        search.hideCircularly(this);
    }

    private void readStoredSearches(){
        DatabaseHandler dbHandler = new DatabaseHandler(this);
        List<String> recentSearches = dbHandler.getStoredSearches();
        search.clearSearchable();
        for(int i = 0; i < recentSearches.size(); i++){
            SearchResult option = new SearchResult(recentSearches.get(i), ContextCompat.getDrawable(this, R.drawable.ic_history));
            search.addSearchable(option);
        }

    }

    private void insertSerchable(String search){
        DatabaseHandler dbHandler = new DatabaseHandler(this);
        dbHandler.storeRecentSearch(search);
        readStoredSearches();
    }

    private void deleteRecentSearches(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        DatabaseHandler dbHandler = new DatabaseHandler(SearchArtistActivity.this);
                        dbHandler.deleteStoredSearches();
                        search.clearSearchable();
                        search.toggleSearch();
                        readStoredSearches();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:

                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.delete_recent_searches_question)).setPositiveButton(
                getResources().getString(R.string.Delete), dialogClickListener)
                .setNegativeButton(getResources().getString(R.string.cancel), dialogClickListener).show();

    }
}
