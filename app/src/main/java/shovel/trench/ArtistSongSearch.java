package shovel.trench;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.client.Response;

public class ArtistSongSearch extends AppCompatActivity {

    ImageView backgroundCircle;
    ImageView icon;
    TextView iconSub;
    ListView resultPanel;
    TextView error;
    SpotifyService webService;
    SearchView searchBar;
    List<Track> listOfTracks = new ArrayList<>();
    List<Artist> listOfArtists = new ArrayList<>();
    boolean isArtist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_song_search);

        backgroundCircle = (ImageView) findViewById(R.id.backgroundCircle);
        icon = (ImageView) findViewById(R.id.songArtistLogo);
        iconSub = (TextView) findViewById(R.id.iconLabel);
        webService = LandingScreen.getWebService();
        searchBar = (SearchView) findViewById(R.id.songArtistSearchBar);
        resultPanel = (ListView) findViewById(R.id.resultView);
        error = (TextView)findViewById(R.id.searchError);

        isArtist = (getIntent().getStringExtra("type")).equals("artist");

        if (isArtist) { searchArtist(); }
        else { searchSong(); }

        setSearchListener();
        setListListener();
    }

    public void setBackgroundCircle() {
        backgroundCircle.setMaxHeight(icon.getMaxHeight() + 10 );
        backgroundCircle.setMaxWidth(icon.getMaxWidth() + 10);
        backgroundCircle.setX(icon.getX());
        backgroundCircle.setY(icon.getY());
    }

    public void setSearchListener() {
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                String query = searchBar.getQuery().toString();
                if(isArtist) {
                    querySpotifyArtists(query);
                }
                else {
                    querySpotifyTracks(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }

        });
    }

    public void setListListener() {
        resultPanel.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent searchIntent = new Intent(ArtistSongSearch.this, LengthSelectionScreen.class);
                if(isArtist) {
                    searchIntent.putExtra("selectedArtist", listOfArtists.get(i).id);
                    Log.d("ArtistInfo", listOfArtists.get(i).id);
                    searchIntent.putExtra("type", "artist");
                    startActivity(searchIntent);
                }
                else{
                    searchIntent.putExtra("selectedTrack", listOfTracks.get(i).id);
                    searchIntent.putExtra("type", "song");
                    startActivity(searchIntent);
                }

            }
        });
    }

    public void searchSong() {
        icon.setImageResource(R.drawable.songicon);
        iconSub.setText("Song");
    }

    public void searchArtist(){
        icon.setImageResource((R.drawable.artisticon));
        iconSub.setText("Artist");
    }

    public void querySpotifyArtists(String query) {
        resultPanel.setAdapter(null);
        webService.searchArtists(query, new SpotifyCallback<ArtistsPager>() {
            @Override
            public void failure(SpotifyError spotifyError) {
                Log.d("Error", spotifyError.toString());
            }

            @Override
            public void success(ArtistsPager artistsPager, Response response) {
                if(artistsPager.artists.total > 0) {
                    // Get rid of error, if visible
                    if(error.getVisibility()==View.VISIBLE) { error.setVisibility(View.INVISIBLE); }

                    listOfArtists.clear();

                    ArrayList<String> artists = new ArrayList<>();

                    for (Artist t : artistsPager.artists.items) {
                        artists.add(t.name);
                        listOfArtists.add(t);
                    }
                    populateArtistList(artists);
                }
                else {
                    error.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void querySpotifyTracks(String query) {
        resultPanel.setAdapter(null);
        webService.searchTracks(query, new SpotifyCallback<TracksPager>() {
            @Override
            public void failure(SpotifyError spotifyError) {
                Log.d("Error", spotifyError.toString());
            }

            @Override
            public void success(TracksPager tracksPager, Response response) {
                List<Map<String, String>> tracks = new ArrayList<>();
                if(tracksPager.tracks.total > 0) {
                    // Get rid of error, if visible
                    if(error.getVisibility()==View.VISIBLE) { error.setVisibility(View.INVISIBLE); }

                    listOfTracks.clear();

                    for (Track t : tracksPager.tracks.items) {
                        Map<String, String> trackData = new HashMap<>(2);
                        trackData.put("name", t.name);
                        trackData.put("artist", t.artists.get(0).name);
                        tracks.add(trackData);
                        listOfTracks.add(t);
                    }
                    populateTrackList(tracks);
                }
                else {
                    error.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void populateArtistList(ArrayList<String> items) {
        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        resultPanel.setAdapter(listAdapter);
    }

    public void populateTrackList(List<Map<String, String>> tracks) {
        SimpleAdapter trackAdapter = new SimpleAdapter(this, tracks, android.R.layout.simple_list_item_2, new String[] {"name", "artist"}, new int[] {android.R.id.text1, android.R.id.text2});
        resultPanel.setAdapter(trackAdapter);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
