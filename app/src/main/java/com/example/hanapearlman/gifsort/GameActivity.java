package com.example.hanapearlman.gifsort;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.TreeSet;

import cz.msebera.android.httpclient.Header;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class GameActivity extends AppCompatActivity {

    private GestureDetectorCompat mDetector;
    private static final String DEBUG_TAG = "Gestures";
    CardView cvGif;
    GiphyClient client;
    private HashMap<String, String[]> categories = new HashMap<String, String[]>();
    List<String> fourCats;
    private ArrayList<Gif> gifSet = new ArrayList<>();
    ImageView ivGif;
    TextView tvCat1;
    TextView tvCat2;
    TextView tvCat3;
    TextView tvCat4;
    TextView tvScore;
    long score;
    Context context;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        context = this;
        mDetector = new GestureDetectorCompat(this, new MyGestureListener());
        client = new GiphyClient();
        fourCats = new ArrayList<>();
        //TODO: change this later
        fillCategories();
        populateGifList("Animals");

        cvGif = (CardView) findViewById(R.id.cvGif);
        ivGif = (ImageView) findViewById(R.id.ivGif);
        score = 0;

//        Glide.with(this)
//                .load("https://media.giphy.com/media/RTvv8ST7rYUec/giphy.gif")
//                .asGif()
//                .into(ivGif);

        cvGif.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mDetector.onTouchEvent(motionEvent);
                return true;
            }
        });
        tvScore = (TextView) findViewById(R.id.tvScore);
        tvScore.setText("Score: " + score);

        prefs = this.getSharedPreferences("GifSort", Context.MODE_PRIVATE);
        editor = prefs.edit();
        editor.commit();
    }

    private void fillCategories() {
        categories.put("Animals", new String[]{"cats", "dogs", "walrus",
                "birds", "fish", "panda", "bunnies", "penguin", "horse", "pig",
                "owl", "duck", "butterfly", "fox", "sloth", "giraffe"});
        categories.put("Motion", new String[]{"bounce", "jump", "run",
                        "sleep", "dance", "swim", "drink"});
        categories.put("Entertainment", new String[]{"disney", "glee",
                        "simpsons", "sponge bob", "hamilton", "anime"});
        categories.put("Emotions", new String[]{"cry", "smile", "happy",
                        "sad", "angry", "no", "yas", "heart"});
        categories.put("Nature", new String[]{"rain", "snow", "sun", "wind", "water",
                        "tornado", "fire", "flower", "sea", "space", "globe"});
        categories.put("Cute", new String[]{"babies", "dogs", "cats",
                        "bunnies", "boop", "love"});
        categories.put("Misc", new String[]{"hand", "kids", "math", "school",
                        "money", "clock", "beach", "workout", "ballet", "memes", "fireworks"});
        categories.put("Food", new String[]{"fries", "burgers", "ice cream",
                        "cake", "pizza", "cookie", "chocolate", "candy"});
        categories.put("Sports", new String[]{"soccer", "basketball",
                        "football", "frisbee", "golf", "baseball"});
        categories.put("People", new String[]{"obama", "trump", "hillary",
                        "beyonce", "bieber", "nicki", "lorde", "tswift", "kkw", "vader", "bart",
                         "homer", "patrick", "sponge bob", "jlaw", "gaga", "rihanna", "zayn",
                         "bernie"});
        categories.put("KPop", new String[]{"bts", "snsd", "blackpink",
                        "twice", "bigbang", "got7", "iu"});
        categories.put("Colors", new String[]{"red", "blue", "green",
                        "yellow", "orange"});
        categories.put("Trippy", new String[]{"fractal", "psychedelic",
                        "spiral", "recursion"});
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            Log.d(DEBUG_TAG,"onDown: " + event.toString());
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            Log.d(DEBUG_TAG, "onFling: " + event1.toString() + event2.toString());
            float diffY = event2.getY() - event1.getY();
            float diffX = event2.getX() - event1.getX();
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (diffX > 0) {
                    onSwipeRight();
                } else {
                    onSwipeLeft();
                }
            }
            else {
                if (diffY > 0) {
                    onSwipeBottom();
                } else {
                    onSwipeTop();
                }
            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            Log.d(DEBUG_TAG, "onDoubleTap: " + event.toString());
            return true;
        }
    }

    public void onSwipeRight() {
        //TODO: accelerate card right
        Animation animation = new TranslateAnimation(0, 900, 0, 0);
        animation.setDuration(200);
        animation.setFillAfter(false);
        //llTransportOptions.startAnimation(animation);
        cvGif.startAnimation(animation);
        if (gifSet.get(0).tags.get(0).equals(tvCat2.getText())) {
            score++;
            tvScore.setText("Score: " + score);
        }
        Log.d(DEBUG_TAG, "onSwipeRight: ");
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //do nothing
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                showNextGif();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                //do nothing
            }
        });
    }

    public void onSwipeLeft() {
        //TODO: accelerate card left
        Animation animation = new TranslateAnimation(0, -900, 0, 0);
        animation.setDuration(200);
        animation.setFillAfter(false);
        //llTransportOptions.startAnimation(animation);
        cvGif.startAnimation(animation);
        if (gifSet.get(0).tags.get(0).equals(tvCat3.getText())) {
            score++;
            tvScore.setText("Score: " + score);
        }
        Log.d(DEBUG_TAG, "onSwipeLeft: ");
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //do nothing
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                showNextGif();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                //do nothing
            }
        });
    }

    public void onSwipeTop() {
        //TODO: accelerate card top
        Animation animation = new TranslateAnimation(0, 0, 0, -1500);
        animation.setDuration(350);
        animation.setFillAfter(false);
        //llTransportOptions.startAnimation(animation);
        cvGif.startAnimation(animation);
        Log.d(DEBUG_TAG, "onSwipeTop: ");
        if (gifSet.get(0).tags.get(0).equals(tvCat1.getText())) {
            score++;
            tvScore.setText("Score: " + score);
        }
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //do nothing
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                showNextGif();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                //do nothing
            }
        });
    }

    public void onSwipeBottom() {
        //TODO: accelerate card bottom
        Animation animation = new TranslateAnimation(0, 0, 0, 1500);
        animation.setDuration(350);
        animation.setFillAfter(false);
        //llTransportOptions.startAnimation(animation);
        cvGif.startAnimation(animation);
        if (gifSet.get(0).tags.get(0).equals(tvCat4.getText())) {
            score++;
            tvScore.setText("Score: " + score);
        }
        Log.d(DEBUG_TAG, "onSwipeDown: ");
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //do nothing
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                showNextGif();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                //do nothing
            }
        });
    }

    public void populateGifList(final String category) {
        TreeSet<Integer> intSet = new TreeSet<Integer>();
        int categorySize = categories.get(category).length;
        for (int i = 0; i < 4; i++) {
            int random = (int) (Math.random() * categorySize);
            if (!intSet.contains(random)) {
                getGiphysFromCategory(categories.get(category)[random]);
                fourCats.add(categories.get(category)[random]);
                intSet.add(random);
            } else {
                i--;
            }
        }
        populateCategoryNames();
    }

    public void getGiphysFromCategory(final String category) {
        for (int i = 0; i < 7; i++) {
            client.getRandomGiphyWithTag(category, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            super.onSuccess(statusCode, headers, response);
                            //parse the JSON
                            try {
                                JSONObject data = response.getJSONObject("data");
                                gifSet.add(new Gif(data.getString("image_original_url"),
                                        Arrays.asList(category), data.getInt("image_width"),
                                        data.getInt("image_height")));
                                if (gifSet.size() == 1) {
                                    Glide.with(context)
                                            .load(gifSet.get(0).getUrl())
                                            .asGif()
                                            .override(gifSet.get(0).width, gifSet.get(0).height)
                                            .into(ivGif);
                                    Log.i("NEWGIF", gifSet.get(0).getUrl());
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            super.onFailure(statusCode, headers, responseString, throwable);
                            Log.e("GameActivity", "Failure");
                        }
                    }
            );
        }
    }

    public void populateCategoryNames() {
        tvCat1 = (TextView) findViewById(R.id.tvCat1);
        tvCat1.setText(fourCats.get(0));
        tvCat2 = (TextView) findViewById(R.id.tvCat2);
        tvCat2.setText(fourCats.get(1));
        tvCat3 = (TextView) findViewById(R.id.tvCat3);
        tvCat3.setText(fourCats.get(2));
        tvCat4 = (TextView) findViewById(R.id.tvCat4);
        tvCat4.setText(fourCats.get(3));
    }

    public void showNextGif() {
        gifSet.remove(0);
        Glide.with(this)
                .load(gifSet.get(0).getUrl())
                .asGif()
                .override(gifSet.get(0).width, gifSet.get(0).height)
                .into(ivGif);
        Log.i("NEWGIF", gifSet.get(0).getUrl());
    }

    public void gameOver() {
        //whether high score changes
        if (score <= prefs.getLong("High Score", (long) -1.0)) {
            editor.putLong("High Score", score);
            editor.commit();
        }

        //get high score
        long highscore = prefs.getLong("High Score", (long) -1.0);
    }
}
