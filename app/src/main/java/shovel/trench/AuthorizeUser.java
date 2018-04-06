package shovel.trench;

import android.app.Activity;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import static com.spotify.sdk.android.authentication.LoginActivity.REQUEST_CODE;


public class AuthorizeUser {

    static String CLIENT_ID = "b9fc6b05e55443bcb3e32eecbd01c08b";
    String REDIRECT_URI = "http://localhost:5505/callback";

    //Build authentication request, open the login activity in parameter 'callingScreen'
    //callingScreen: Activity that is calling the authentication. For most cases, LandingScreen
    public void auth(Activity callingScreen) {
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);

        //SCOPES
        builder.setScopes(new String[]{"user-read-private", "streaming", "playlist-read-private", "playlist-modify-private", "playlist-modify-public", "user-follow-read"});

        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(callingScreen, REQUEST_CODE, request);
    }
}
