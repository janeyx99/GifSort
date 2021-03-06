package com.example.hanapearlman.gifsort;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class GameActivity extends AppCompatActivity {

    private GestureDetectorCompat mDetector;
    private static final String DEBUG_TAG = "Gestures";
    CardView cvGif;
    CardView cvHiddenGif;
    ImageView ivHiddenGif;
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
    TextView tvHighScore;
    TextView tvHSNumber;
    long numRight;
    Context context;
    Vibrator v;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    boolean instructions = true;

    TextView timerTextView;
    long startTime = 0;

    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            timerTextView.setText(String.format("%d:%02d", minutes, seconds));
            double score = numRight * 17 / (Math.log(seconds/4));
            if (Double.isNaN(score) || score <= 0) {
                score = 0;
            } else if (Double.isInfinite(score)) {
                score = 999999999.999999999;
            }
            tvScore.setText("Score: " + score);

            timerHandler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        context = this;
        mDetector = new GestureDetectorCompat(this, new MyGestureListener());
        client = new GiphyClient();
        fourCats = new ArrayList<>();
        //TODO: change this later
        fillCategories();

        int catNumber = (int) (Math.random()*13) + 1;
        String category = getCategoryFromRandomNumber(catNumber);
        populateGifList(category);

        ImageView ivPlayAgain = (ImageView) findViewById(R.id.ivPlayAgain);
        ivPlayAgain.setVisibility(View.INVISIBLE);
        cvGif = (CardView) findViewById(R.id.cvGif);
        ivGif = (ImageView) findViewById(R.id.ivGif);
        tvHighScore = (TextView) findViewById(R.id.tvHighScore);
        tvHSNumber = (TextView) findViewById(R.id.tvHSNumber);
        cvHiddenGif = (CardView) findViewById(R.id.cvHiddenLoadingCard);
        ivHiddenGif = (ImageView) findViewById(R.id.ivHiddenGif);
        numRight = 0;

        cvGif.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mDetector.onTouchEvent(motionEvent);
                return true;
            }
        });
        timerTextView = (TextView) findViewById(R.id.tvTime);
        tvScore = (TextView) findViewById(R.id.tvScore);

        prefs = this.getSharedPreferences("GifSort", Context.MODE_PRIVATE);
        editor = prefs.edit();
        editor.commit();
        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);
    }

    private void fillCategories() {
        categories.put("Animals", new String[]{"cats", "dogs", "walrus",
                "birds", "fish", "panda", "bunnies", "penguin", "horse", "pig",
                "owl", "duck", "butterfly", "fox", "sloth", "giraffe"});
        categories.put("Motion", new String[]{"jump", "run",
                        "sleep", "dance", "swim", "drink"});
        categories.put("Entertainment", new String[]{"disney", "glee",
                        "simpsons", "sponge bob", "hamilton", "anime"});
        categories.put("Emotions", new String[]{"sad", "angry", "yas", "heart"});
        categories.put("Nature", new String[]{"rain", "snow", "sun", "wind",
                        "tornado", "fire", "flower", "sea", "space", "globe"});
        categories.put("Cute", new String[]{"babies", "dogs", "cats",
                        "bunnies", "love"});
        categories.put("Misc", new String[]{"hand", "kids", "math", "school",
                        "money", "clock", "beach", "workout", "ballet", "memes", "fireworks"});
        categories.put("Food", new String[]{"fries", "burgers", "ice cream",
                        "cake", "pizza", "cookie", "chocolate"});
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
            //TODO: shrink and spin card till it disappear

            AnimationSet animationSet = new AnimationSet(true);

            Animation shrinkAnim = new ScaleAnimation(1, 0, 1, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            shrinkAnim.setDuration(400);

            Animation rotateAnim = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnim.setDuration(400);


            animationSet.addAnimation(shrinkAnim);
            animationSet.addAnimation(rotateAnim);
            animationSet.setFillAfter(false);
            cvGif.startAnimation(animationSet);

            if (!instructions) {
                String category = gifSet.get(0).tags.get(0);
                if (category.equals(tvCat2.getText()) || category.equals(tvCat1.getText())
                        || category.equals(tvCat3.getText()) || category.equals(tvCat4.getText())) {
                    if (numRight > 0) {
                        numRight--;
                    }
                    v.vibrate(150);
                } else {
                    numRight++;
                }
            } else {
                instructions = false;
            }
            animationSet.setAnimationListener(new Animation.AnimationListener() {
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

            return true;
        }
    }

    public void onSwipeRight() {
        //TODO: accelerate card right
        Animation animation = new TranslateAnimation(0, 1500, 0, 0);
        animation.setDuration(350);
        animation.setFillAfter(false);
        //llTransportOptions.startAnimation(animation);
        cvGif.startAnimation(animation);

        if (!instructions) {
            if (gifSet.get(0).tags.get(0).equals(tvCat2.getText())) {
                numRight++;
            } else {
                v.vibrate(150);
            }
        } else {
            instructions = false;
        }
        Log.d(DEBUG_TAG, "onSwipeRight: ");
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                loadNext();
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
        Animation animation = new TranslateAnimation(0, -1500, 0, 0);
        animation.setDuration(350);
        animation.setFillAfter(false);
        //llTransportOptions.startAnimation(animation);
        cvGif.startAnimation(animation);

        if (!instructions) {
            if (gifSet.get(0).tags.get(0).equals(tvCat3.getText())) {
                numRight++;
            } else {
                v.vibrate(150);
            }

        } else {
            instructions = false;
        }
        Log.d(DEBUG_TAG, "onSwipeLeft: ");
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                loadNext();
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
        Animation animation = new TranslateAnimation(0, 0, 0, -900);
        animation.setDuration(320);
        animation.setFillAfter(false);
        //llTransportOptions.startAnimation(animation);
        cvGif.startAnimation(animation);
        Log.d(DEBUG_TAG, "onSwipeTop: ");

        if (!instructions) {
            if (gifSet.get(0).tags.get(0).equals(tvCat1.getText())) {
                numRight++;
            } else {
                v.vibrate(150);
            }
        } else {
            instructions = false;
        }
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //do nothing
                loadNext();
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
        Animation animation = new TranslateAnimation(0, 0, 0, 900);
        animation.setDuration(320);
        animation.setFillAfter(false);
        //llTransportOptions.startAnimation(animation);
        cvGif.startAnimation(animation);

        if (!instructions) {
            if (gifSet.get(0).tags.get(0).equals(tvCat4.getText())) {
                numRight++;
            } else {
                v.vibrate(150);
            }
        } else {
            instructions = false;
        }
        Log.d(DEBUG_TAG, "onSwipeDown: ");
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                loadNext();
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
        client.getSearchGiphyWithTag(category, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        //super.onSuccess(statusCode, headers, response);
                        //parse the JSON
                        try {
                            JSONArray data = response.getJSONArray("data");
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject obj = data.getJSONObject(i);
                                JSONObject imgSpecs = obj.getJSONObject("images").getJSONObject("original");
                                gifSet.add(new Gif(imgSpecs.getString("url"),
                                        Arrays.asList(category), imgSpecs.getInt("width"),
                                        imgSpecs.getInt("height")));
                                /*if (gifSet.size() == 1) {
                                    Glide.with(context)
                                            .load(gifSet.get(0).getUrl())
                                            .asGif()
                                            .override(200, 400)
                                            .centerCrop()
                                            .into(ivGif);
                                    Log.i("NEWGIF", gifSet.get(0).getUrl());
                                }*/


                                if (gifSet.size() == 20) {
                                    addRandomGiphys();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Log.e("GameActivity", "Failure");
            }
        });

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
        if (gifSet.size() == 0) {
            timerHandler.removeCallbacks(timerRunnable);
            long tEnd = System.currentTimeMillis();
            long tDelta = tEnd - startTime;
            double elapsedSeconds = tDelta / 1000.0;
            //TODO: handle game over somehow

            long score = (long) Double.parseDouble(tvScore.getText().toString().substring(7));

            if (score > prefs.getLong("High Score", (long) 0)) {
                editor.putLong("High Score", (long) score);
                editor.commit();
            }
            long highscore = prefs.getLong("High Score", (long) 0);
            tvHSNumber.setText("" + highscore);

            ivGif.setVisibility(View.INVISIBLE);
            cvGif.setVisibility(View.INVISIBLE);
            tvHighScore.setVisibility(View.VISIBLE);
            tvHSNumber.setVisibility(View.VISIBLE);
            ImageView ivPlayAgain = (ImageView) findViewById(R.id.ivPlayAgain);
            ivPlayAgain.setVisibility(View.VISIBLE);
        } else {
            Glide.with(this)
                    .load(gifSet.get(0).getUrl())
                    .asGif()
                    .override(gifSet.get(0).width, gifSet.get(0).height)
                    .into(ivGif);
            Log.i("NEWGIF", gifSet.get(0).getUrl());
        }
    }

    public void loadNext() {
        if (gifSet.size() > 1) {
            Glide.with(this)
                    .load(gifSet.get(1).getUrl())
                    .asGif()
                    .override(gifSet.get(1).width, gifSet.get(1).height)
                    .into(ivHiddenGif);
        } else {
            //do nothing, no more cards
        }
    }

    public String getCategoryFromRandomNumber(int categoryNumber) {
        switch (categoryNumber){
            case 1 : return "Animals";
            case 2: return "Motion";
            case 3: return "Entertainment";
            case 4: return "Emotions";
            case 5: return "Nature";
            case 6: return "Cute";
            case 7: return "Misc";
            case 8: return "Food";
            case 9: return "Sport";
            case 10: return "People";
            case 11: return "KPop";
            case 12: return "Colors";
            default: return "Trippy";
        }
    }

    public void playAgain(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }

    public void addRandomGiphys() {
        for (int i = 0; i < 3; i++) {
            client.getRandomGiphy(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    //super.onSuccess(statusCode, headers, response);
                    //parse the JSON
                    try {
                        JSONObject data = response.getJSONObject("data");
                        Log.i("JSON DATA", data.toString());
                        gifSet.add(new Gif(data.getString("image_original_url"),
                                Arrays.asList("Random"), data.getInt("image_width"),
                                data.getInt("image_height")));

                        if (gifSet.size() > 22) {
                            Collections.shuffle(gifSet);
                            loadNext();
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
            });
        }
    }
}
