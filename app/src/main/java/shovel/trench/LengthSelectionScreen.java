package shovel.trench;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import kaaes.spotify.webapi.android.models.Track;

public class LengthSelectionScreen extends AppCompatActivity {


    Button small;
    Button med;
    Button large;

    int smallDig = 3;
    int mediumDig = 5;
    int largeDig = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_length_selection_screen);

        small = (Button) findViewById(R.id.shortSelect);
        med = (Button) findViewById(R.id.mediumSelect);
        large = (Button) findViewById(R.id.largeSelect);

        setButtonListeners();
    }

    public void setButtonListeners() {

        final boolean isArtist = (getIntent().getStringExtra("type").equals("artist"));

        small.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isArtist){ artistDig(smallDig); }
                else { trackDig(smallDig); }
            }
        });

        med.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isArtist){ artistDig(mediumDig); }
                else { trackDig(mediumDig); }
            }
        });

        large.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isArtist){ artistDig(largeDig); }
                else { trackDig(largeDig); }

            }
        });
    }

    public void artistDig(int length){
        Intent startDig = new Intent(LengthSelectionScreen.this, PlayerScreen.class);
        String selected = getIntent().getStringExtra("selectedArtist");
        startDig.putExtra("selectedArtist", selected);
        startDig.putExtra("length", length);
        startDig.putExtra("type", "artist");
        startActivity(startDig);
    }

    public void trackDig(int length){
        Intent startDig = new Intent(LengthSelectionScreen.this, PlayerScreen.class);
        String selected = getIntent().getStringExtra("selectedTrack");
        startDig.putExtra("selectedTrack", selected);
        startDig.putExtra("length", length);
        startDig.putExtra("type", "song");
        startActivity(startDig);
    }
}
