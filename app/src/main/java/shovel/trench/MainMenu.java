package shovel.trench;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Selection;
import android.view.View;
import android.widget.Button;

public class MainMenu extends AppCompatActivity {

    Button discover; //= (Button) findViewById(R.id.DiscoverButton);
    Button settings;// = (Button) findViewById(R.id.SettingsButton);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        discover = (Button) findViewById(R.id.DiscoverButton);
        settings = (Button) findViewById(R.id.SettingsButton);

        setButtonListeners();
    }

    public void setButtonListeners(){
        discover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent (MainMenu.this, SelectionMenu.class));
            }
        });

    }
}
