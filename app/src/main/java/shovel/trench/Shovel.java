package shovel.trench;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.Recommendations;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by cmcnutt on 9/28/2017.
 */

public class Shovel {

    int lim;
    String type;
    SpotifyService webService;
    String starting;
    int levelItr;
    Track currentlyPlaying;

    Stack<Map<Object, Track>> trackStack = new Stack<>();
    List<String> likedList = new ArrayList<>();

    public Shovel(String type, int limit, String entityId) {
        this.lim = limit;
        this.type = type;
        this.starting = entityId;
        this.webService = LandingScreen.getWebService();

    }

    public void kickOff(){
        if(type.equals("song")) { getSongShovel(this.starting); }
        else if(type.equals("artist")) { getArtistShovel(this.starting); }
    }

    public void getTrackRec(Track seedTrack, final int currentLevel, final boolean fromLike) {
        Map<String, Object> queryOptions = new HashMap<>();
        queryOptions.put("seed_tracks", seedTrack.id);
        queryOptions.put("seed_artists", seedTrack.artists.get(0).id);
        queryOptions.put("limit", lim);
        webService.getRecommendations(queryOptions, new SpotifyCallback<Recommendations>() {
            @Override
            public void failure(SpotifyError spotifyError) {

            }

            @Override
            public void success(Recommendations recommendations, Response response) {

                for (int i = (lim - 1); i >= 0; i--) {
                    Map<Object, Track> song = new HashMap<>();
                    song.put(currentLevel, recommendations.tracks.get(i));
                    Log.d("stack", "pushing to stack");
                    trackStack.push(song);
                }

                Log.d("StackInfo", String.valueOf(trackStack.size()));

                //playStack();
            }
        });

    }

    public void getTrackRec(String artistId){
        Map<String, Object> queryOptions = new HashMap<>();
        Log.d("artistId debug", artistId);
        queryOptions.put("seed_artists", artistId);
        queryOptions.put("limit", lim);
        webService.getRecommendations(queryOptions, new SpotifyCallback<Recommendations>() {
            @Override
            public void failure(SpotifyError spotifyError) {

            }

            @Override
            public void success(Recommendations recommendations, Response response) {
                for (int i = (lim - 1); i >= 0; i--){
                    Map<Object, Track> song = new HashMap<>();
                    song.put(1, recommendations.tracks.get(i));
                    Log.d("stack", "pushing to stack");
                    trackStack.push(song);
                }
                Log.d("StackInfo", String.valueOf(trackStack.size()));
            }
        });
    }

    public void getSongShovel(String songId) {
        webService.getTrack(songId, new Callback<Track>() {
            @Override
            public void success(Track track, Response response) {
                startTrack(track);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void getArtistShovel(String artistId) {
        getTrackRec(artistId);
    }

    public void startTrack(Track startingTrack) {
        levelItr = 1;
        getTrackRec(startingTrack, levelItr, false);
    }

    public void songLiked(Track likedTrack) {
        levelItr += 1;
        likedList.add(likedTrack.uri);
        PlayerScreen.finished = false;
        getTrackRec(likedTrack, levelItr, true);

    }

    public void playStack() {
        if(trackStack.size() > 0) {
            Map<Object, Track> currentTrack = trackStack.pop();
            // Kind of convoluted. Only one track in map for pop return, so get first value.
            currentlyPlaying = currentTrack.entrySet().iterator().next().getValue();
            Log.d("Popped track. info", String.valueOf(currentTrack.size()));
            Log.d("popped track. info", currentTrack.toString());
            Log.d("StackSize", String.valueOf(trackStack.size()));
            if(trackStack.size() == 0){
                PlayerScreen.finished = true;
            }
            PlayerScreen.mPlayer.playUri(null, currentlyPlaying.uri, 0, 0);
        }
        /*else {
            PlayerScreen.finished=true;
        }*/
    }

    public void songHated() {
        boolean popStackMode = true;
        if (levelItr == 1) { popStackMode = false ; }

        while(popStackMode) {
            Map<Object, Track> peeked = trackStack.peek();
            if(peeked.containsKey(levelItr)) {
                trackStack.pop();
            }
            else {
                popStackMode = false;
            }
        }
        playStack();
    }

    public void createPlaylist(String playName) {
        final String playlistName = playName;
        webService.getMe(new retrofit.Callback<UserPrivate>() {
            @Override
            public void success(UserPrivate userPrivate, Response response) {
                final String myId = userPrivate.id;
                String[] uriArr = new String[likedList.size()];
                int i = 0;
                for (String uri : likedList) {
                    uriArr[i] = uri;
                    i++;
                }
                final Map<String, Object> postBody = new HashMap<>();
                postBody.put("uris", uriArr);
                Map<String, Object> queryOptions = new HashMap<>();
                queryOptions.put("name", playlistName);

                webService.createPlaylist(myId, queryOptions, new retrofit.Callback<Playlist>() {
                    @Override
                    public void success(Playlist playlist, Response response) {
                        webService.addTracksToPlaylist(myId, playlist.id, null, postBody, new Callback<Pager<PlaylistTrack>>() {

                            @Override
                            public void success(Pager<PlaylistTrack> playlistTrackPager, Response response) {

                            }

                            @Override
                            public void failure(RetrofitError error) {

                            }
                        });
                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {

            }

            ;

        });
    }




}
