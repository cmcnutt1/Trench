package shovel.trench;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Metadata;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class PlayerScreen extends AppCompatActivity{

    //private static final String CLIENT_ID = "fe50dae077c74216a5dd25fad3ddc0e3";
    //private static final String REDIRECT_URI = "http://localhost:5505/callback";

    public static Player mPlayer;
    public static boolean finished;
    private static Metadata trackData;

    private ImageButton nextButton;
    private TextView songText;
    private TextView artistText;

    Shovel shovel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Log.d("PlayerScreen","Creating new activity instance");


        setContentView(R.layout.activity_player_screen);

        //Testing... Toast message popup of the Authentication Token string
        //Toast.makeText(this, LandingScreen.getAuthToken(), Toast.LENGTH_LONG).show();

        //Spotify Player object config
        final Config playerConfig = new Config(PlayerScreen.this, LandingScreen.getAuthToken(), AuthorizeUser.CLIENT_ID);

        final int limit = getIntent().getIntExtra("length", 3);
        final String type = getIntent().getStringExtra("type");

        boolean isArtist = (type.equals("artist"));
        String selectedId;

        if(isArtist){ selectedId = getIntent().getStringExtra("selectedArtist"); }
        else { selectedId = getIntent().getStringExtra("selectedTrack"); }

        final String selected = selectedId;

        final Shovel testShovel = new Shovel(type, limit, selected);
        shovel = testShovel;
        shovel.kickOff();



        //SpotifyPlayer (mPlayer) initialized and callback functions
        Spotify.getPlayer(playerConfig, PlayerScreen.this, new SpotifyPlayer.InitializationObserver() {
            @Override
            public void onInitialized(SpotifyPlayer spotifyPlayer) {
                Log.d("Player","Initialized");

                if(!spotifyPlayer.isLoggedIn()){
                    spotifyPlayer.login(LandingScreen.getAuthToken());
                }

                //Assign mPlayer to SpotifyPlayer result
                mPlayer = spotifyPlayer;


                //Add Connection State because if beginPlayback(mPlayer.playURI function specifically) called too soon
                //after player initialization, error will be thrown. Now it will begin playback when onLoggedIn is returned.
                mPlayer.addConnectionStateCallback(new ConnectionStateCallback() {
                    @Override
                    public void onLoggedIn() {
                        finished = false;
                        setupPlayerUI();
                        /*String playUri = "spotify:user:biocoven:playlist:3W2HeQpBb2l3fmeCcv6pye";
                        beginPlayback(playUri);*/
                        shovel.playStack();
                    }

                    @Override
                    public void onLoggedOut() {
                        //Do something
                    }

                    @Override
                    public void onLoginFailed(Error error) {
                        //Do something
                    }

                    @Override
                    public void onTemporaryError() {
                        //Do something
                    }

                    @Override
                    public void onConnectionMessage(String s) {
                        //Log any connection messages to debug
                        Log.d("Player Connection: ", s);
                    }
                });

                mPlayer.addNotificationCallback(new Player.NotificationCallback() {
                    @Override
                    public void onPlaybackEvent(PlayerEvent playerEvent) {
                        Log.v("PLAYEREVENT: ", playerEvent.name());

                        if(playerEvent.name().equals("kSpPlaybackEventAudioFlush") || playerEvent.name().equals("kSpPlaybackNotifyTrackChanged")){
                            try {
                                changeSongDisplay();
                            }
                            catch(Exception e){
                                Log.d("Error", e.toString());
                            }
                        }

                        /*if(playerEvent.name().equals("kSpPlaybackNotifyAudioDeliveryDone")) {
                            if(!finished) {
                                shovel.playStack();
                                resetUI();
                                Log.d("finished?", "no");
                            }
                            else{
                                Log.d("finished?", "yes");
                                endOfStack();
                            }

                        }*/
                    }

                    @Override
                    public void onPlaybackError(Error error) {
                        Log.d("Player","Error: " + error.toString());
                    }
                });



            }

            @Override
            public void onError(Throwable throwable) {
                Log.e("PlayerScreen", "Playback Error: " + throwable.getMessage());
            }
        });
    }


    /*Destroy player on back or exit. Good memory management.
    @Override
    protected void onDestroy() {
        //Spotify.destroyPlayer(this);
        //super.onDestroy();
        Log.d("Player","DESTROYED");
    }*/


    //If user pauses
    public void onPause(Player nPlayer) {
        if (nPlayer != null) {
            nPlayer.pause(new Player.OperationCallback() {
                @Override
                public void onSuccess() {
                    Log.d("Player", "Song Paused");
                    togglePlay(true);
                }

                @Override
                public void onError(Error error) {
                    Log.d("Player", "FAILED ON PAUSE: " + error.toString());
                }
            });
        }
    }


    //If user presses play
    public void onPlay(){
        mPlayer.resume(new Player.OperationCallback() {
            @Override
            public void onSuccess() {
                Log.d("Player","Resuming song");
                togglePlay(false);
            }

            @Override
            public void onError(Error error) {
                Log.d("Player","FAILED ON PLAY: " + error.toString());
            }
        });
    }



    //Get and display album artwork, song name, artist name
    //
    // *NOTE*: For artwork, DownloadImageTask class is used.
    // Look for DownloadImageTask.java in current directory
    // to see more about operation.
    public void changeSongDisplay(){
        trackData = mPlayer.getMetadata();
        new DownloadImageTask((ImageView) findViewById(R.id.albumArt))
                .execute(trackData.currentTrack.albumCoverWebUrl);
        songText = (TextView) findViewById(R.id.SongName);
        assert songText != null;
        songText.setText(trackData.currentTrack.name);
        artistText = (TextView) findViewById(R.id.ArtistName);
        artistText.setText(trackData.currentTrack.artistName);
        //String i = "1";
        //int j = Integer.getInteger(i);
    }


    //Switch pause and play buttons. Make one invisible, the other one visible.
    //If pausing, use togglePlay(true)
    public void togglePlay(boolean playing){
        ImageButton pause = (ImageButton)findViewById(R.id.PauseButton);
        ImageButton play = (ImageButton)findViewById(R.id.PlayButton);


        if(playing) {
            pause.setVisibility(View.INVISIBLE);
            play.setVisibility(View.VISIBLE);
        }
        else{
            play.setVisibility(View.INVISIBLE);
            pause.setVisibility(View.VISIBLE);
        }
    }


    //Set up button listeners
    public void setupPlayerUI(){

        //Skip Forward Button
        nextButton = (ImageButton) findViewById(R.id.PlayerNextButton);
        assert nextButton != null;
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.skipToNext(new Player.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d("PlayerScreen","Going to next track");
                        resetUI();
                        if(!finished) {
                            shovel.playStack();
                            Log.d("finished?", "no");
                        }
                        else{
                            Log.d("finished?", "yes");
                            endOfStack();
                        }


                    }

                    @Override
                    public void onError(Error error) {

                    }
                });
                togglePlay(false);
            }
        });

        /*//Skip Back Button
        backButton = (ImageButton) findViewById(R.id.PlayerBackButton);
        assert backButton != null;
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.skipToPrevious(new Player.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d("PlayerScreen","Going to previous track");
                    }

                    @Override
                    public void onError(Error error) {

                    }
                });
                togglePlay(false);
            }
        });*/

        //Pause Button
        ImageButton pauseButton = (ImageButton) findViewById(R.id.PauseButton);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPause(mPlayer);
            }
        });

        //Play Button
        ImageButton playButton = (ImageButton) findViewById(R.id.PlayButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlay();
            }
        });

        //Like Button
        final ImageButton likeButton = (ImageButton) findViewById(R.id.likeButton);
        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView likedConfirmation = (ImageView) findViewById(R.id.imageView);
                likedConfirmation.setVisibility(View.VISIBLE);
                likeButton.setClickable(false);
                shovel.songLiked(shovel.currentlyPlaying);
            }
        });

        //Hate Button
        final ImageButton hateButton = (ImageButton) findViewById(R.id.hateButton);
        hateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                ImageView hatedConfirmation = (ImageView) findViewById(R.id.imageView2);
                hatedConfirmation.setVisibility(View.VISIBLE);
                hateButton.setClickable(false);
                shovel.songHated();
                resetUI();
            }
        });
    }

    public void resetUI(){
        ImageButton likeButton = (ImageButton) findViewById(R.id.likeButton);
        ImageButton hateButton = (ImageButton) findViewById(R.id.hateButton);
        ImageView likeView = (ImageView) findViewById(R.id.imageView);
        ImageView hateView = (ImageView) findViewById(R.id.imageView2);

        likeButton.setClickable(true);
        hateButton.setClickable(true);
        likeView.setVisibility(View.INVISIBLE);
        hateView.setVisibility(View.INVISIBLE);
    }

    public void endOfStack(){
        AlertDialog.Builder popup = new AlertDialog.Builder(PlayerScreen.this);
        popup.setTitle("End of Trench");

        final EditText input = new EditText(PlayerScreen.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        popup.setView(input);

        popup.setPositiveButton("Create", new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String playName;
                if(input.getText().toString().equals("")){
                    playName = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime());
                }
                else{
                    playName = input.getText().toString();
                }
                shovel.createPlaylist(playName);

                mPlayer.logout();
                finished = false;
                shovel = null;
                System.gc();
                Intent backToMenu = new Intent(PlayerScreen.this, MainMenu.class);
                //backToMenu.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(backToMenu);
                //PlayerScreen.this.finish();
            }
        });

        popup.setNegativeButton("Never Mind", new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                mPlayer.logout();
                finished = false;
                //shovel = null;
                Intent backToMenu = new Intent(PlayerScreen.this, MainMenu.class);
                startActivity(backToMenu);
            }
        });

        popup.show();
    }

    @Override
    public void onBackPressed() {
        mPlayer.destroy();
        Log.d("PlayerScreen", "super onback start");
        super.onBackPressed();
        Log.d("PlayerScreen", "super onback finish. Closing activity");
        this.finish();
    }

    //Start playing
    public void beginPlayback(String uri){
        mPlayer.playUri(null,uri,0,0);
    }
}
