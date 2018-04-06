package shovel.trench;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import retrofit.client.Response;

public class PlaylistSearch extends AppCompatActivity {

    ListView resultsPanel;
    TextView error;
    SpotifyService webService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_search);

        resultsPanel = (ListView) findViewById(R.id.playlistResults);
        error = (TextView) findViewById(R.id.playlistError);

        webService = LandingScreen.getWebService();

        loadPlaylists();
    }

    public void loadPlaylists() {
        webService.getMyPlaylists(new SpotifyCallback<Pager<PlaylistSimple>>() {
            @Override
            public void failure(SpotifyError spotifyError) {
                Log.d("Error", spotifyError.toString());
            }

            @Override
            public void success(Pager<PlaylistSimple> playlistSimplePager, Response response) {
                List<Map<String, String>> playlists = new ArrayList<>();

                if(playlistSimplePager.total > 0) {
                    for(PlaylistSimple t : playlistSimplePager.items) {
                        Map<String, String> playData = new HashMap<String, String>(2);
                        playData.put("name", t.name);
                        playData.put("length", String.valueOf(t.tracks.total) + " tracks");
                        playlists.add(playData);
                    }
                    populatePlaylists(playlists);
                }
                else { error.setVisibility(View.VISIBLE); }
            }
        });
    }

    public void populatePlaylists(List<Map<String, String>> playlists) {
        SimpleAdapter trackAdapter = new SimpleAdapter(this, playlists, android.R.layout.simple_list_item_2, new String[] {"name", "length"}, new int[] {android.R.id.text1, android.R.id.text2});
        resultsPanel.setAdapter(trackAdapter);
    }
}
