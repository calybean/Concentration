package com.youravgjoe.apps.concentration;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    LinearLayout mGameLayout;
    List<Integer> mCardList; // list of image resource ids to each card image

    List<Integer> mMatches = new ArrayList<>();

    ImageView mImageOne;
    ImageView mImageTwo;

    boolean mKillRunnable = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        mGameLayout = (LinearLayout) findViewById(R.id.game_layout);

        setupGame();
    }

    private void setupGame() {
        displayCards();
        randomizeCards();
    }

    private void displayCards() {
        // get screen size (minus margins and header)
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels - convertPixelsToDp(80); // 2 8dp margins + 8 8dp margins: 2(8) + 8(8) = 16 + 64 = 80
        int screenHeight = metrics.heightPixels - convertPixelsToDp(152); // 5dp header plus 2 8dp margins + 8 8dp margins: 1(56) + 2(16) + 8(8) = 56 + 32 + 64 = 152

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

                        // if we've already matched this one, skip it
                        if (mMatches.contains(image.getId()) || (mImageOne != null && mImageOne.getId() == image.getId())) {
                            return;
                        }
                        if (mImageOne == null) {
                            mImageOne = (ImageView) findViewById(image.getId());
                            mImageOne.setImageDrawable(getResources().getDrawable(mCardList.get(image.getId())));
                        } else if (mImageTwo == null) {
                            mImageTwo =  (ImageView) findViewById(image.getId());
                            mImageTwo.setImageDrawable(getResources().getDrawable(mCardList.get(image.getId())));

                            // we found a match!
                            if (mImageOne.getDrawable().getConstantState().equals(mImageTwo.getDrawable().getConstantState())) {

                                mImageOne.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                mImageTwo.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                mMatches.add(mImageOne.getId());
                                mMatches.add(mImageTwo.getId());

                                mImageOne = null;
                                mImageTwo = null;

                                if (mMatches.size() == 16) {
                                    gameWon();
                                }
                            } else {
                                // do nothing, just wait for them to click again?
                            }

                        } else {

                            mImageOne.setImageDrawable(getResources().getDrawable(R.drawable.ic_android_black_24dp));
                            mImageTwo.setImageDrawable(getResources().getDrawable(R.drawable.ic_android_black_24dp));

                            mImageOne = null;
                            mImageTwo = null;
                        }
                    }
                });

                CardView card = new CardView(this);

                // set card margins
                int margin = 16;
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(margin, margin, margin, margin);
                card.setLayoutParams(params);

                card.addView(image);

                row.addView(card);
            }
            mGameLayout.addView(row);
        }
    }

    private void gameWon() {
        new AlertDialog.Builder(this)
            .setMessage("You won! Congratulations, you're a pro!")
            .setPositiveButton("Yes, yes I am", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            })
            .show();
    }

    private void randomizeCards() {
        // make list of available image resources
        // give two ids to each resource (0-15)
        // edit the onclick listener to query the list for the image to display

        mCardList = new ArrayList<>();

        // add each card twice
        for (int i = 0; i < 2; i++) {
            mCardList.add(R.drawable.ic_brightness_5_black_24dp);
            mCardList.add(R.drawable.ic_insert_emoticon_black_24dp);
            mCardList.add(R.drawable.ic_local_airport_black_24dp);
            mCardList.add(R.drawable.ic_local_florist_black_24dp);
            mCardList.add(R.drawable.ic_local_shipping_black_24dp);
            mCardList.add(R.drawable.ic_phone_black_24dp);
            mCardList.add(R.drawable.ic_star_black_24dp);
            mCardList.add(R.drawable.ic_wifi_black_24dp);
        }

        // shuffle the cards
        long seed = System.nanoTime();
        Collections.shuffle(mCardList, new Random(seed));
    }

    private int convertPixelsToDp(int pixels) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (pixels * scale + 0.5f);
    }
}
