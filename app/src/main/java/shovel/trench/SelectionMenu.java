package shovel.trench;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Selection;
import android.view.View;
import android.widget.ImageButton;

import com.spotify.sdk.android.player.Spotify;

import kaaes.spotify.webapi.android.SpotifyService;

public class SelectionMenu extends AppCompatActivity {


    ImageButton artistButton;// = (ImageButton) findViewById(R.id.byArtistButton);
    ImageButton songButton;// = (ImageButton) findViewById(R.id.bySongButton);
    ImageButton playlistButton;// = (ImageButton) findViewById(R.id.byPlaylistButton);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection_menu);

        artistButton = (ImageButton) findViewById(R.id.byArtistButton);
        songButton = (ImageButton) findViewById(R.id.bySongButton);
        playlistButton = (ImageButton) findViewById(R.id.byPlaylistButton);
        setListeners();
    }

    public void setListeners() {
        artistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent search = new Intent(SelectionMenu.this, ArtistSongSearch.class);
                //player.putExtra("uri","spotify:user:biocoven:playlist:3zpEy2JrhBVF6yscY41BuG");
                search.putExtra("type", "artist");
                startActivity(search);
            }
        });

        songButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent search = new Intent(SelectionMenu.this, ArtistSongSearch.class);
                search.putExtra("type", "song");
                startActivity(search);
            }
        });

        playlistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SelectionMenu.this, PlaylistSearch.class));
            }
        });
    }
}
