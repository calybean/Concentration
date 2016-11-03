package com.youravgjoe.apps.concentration;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class GameActivity extends AppCompatActivity {

    LinearLayout mGameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        mGameLayout = (LinearLayout) findViewById(R.id.game_layout);

        // get screen size (minus margins and header)
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels - convertPixelsToDp(64); // 2 margins
        int screenHeight = metrics.heightPixels - convertPixelsToDp(56 + 64); // header plus 2 margins

        for (int i = 0; i < 4; i++) {
            LinearLayout row = new LinearLayout(this);
            for (int j = 0; j < 4; j++) {
                final ImageView image = new ImageView(this);
                image.setMinimumWidth(screenWidth / 4);
                image.setMinimumHeight(screenHeight / 4);
                image.setImageResource(R.drawable.ic_android_black_24dp);
                image.setId((i * 4) + j); // this will number the cards from 0-15.

                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(v.getContext(), "Clicked image " + image.getId(), Toast.LENGTH_SHORT).show();
                    }
                });

                CardView card = new CardView(this);
                card.addView(image);

                row.addView(card);
            }
            mGameLayout.addView(row);
        }
    }

    private int convertPixelsToDp(int pixels) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (pixels * scale + 0.5f);
    }
}
