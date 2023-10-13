package com.example.myaudioplayer;

import static com.example.myaudioplayer.MainActivity.musicFiles;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class PlayerActivity extends AppCompatActivity  {

    TextView song_name, song_artist, durationPlayedStart, durationPlayedEnd;
    ImageView cover_art, id_next, id_shuffle_off, id_repeat;
    FloatingActionButton play_pause;
    SeekBar seekBar;
    int position = -1;
    static ArrayList<MusicFiles> listSongs = new ArrayList<>();
    static Uri uri;
    static MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    boolean isShuffle = false;
    boolean isRepeat = false;
    private Random random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        initViews();
        getIntentMethod();
        song_name.setText(listSongs.get(position).getTitle());
        song_artist.setText(listSongs.get(position).getArtist());
        ImageView id_prev = findViewById(R.id.id_prev);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlayPause();
            }
        });


        id_shuffle_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleShuffle();
            }
        });


        id_repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleRepeat();
            }
        });
        id_repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleRepeat();
            }
        });
        id_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        });

        id_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });




        updateUIAndSeekbar();


        random = new Random();


    }



    private void togglePlayPause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                play_pause.setImageResource(R.drawable.baseline_play_arrow);
            } else {
                mediaPlayer.start();
                play_pause.setImageResource(R.drawable.baseline_pause);
            }
        }
    }


    private void toggleShuffle() {
        isShuffle = !isShuffle;
        if (isShuffle) {

            Collections.shuffle(listSongs);
            Toast.makeText(this, "Shuffle On", Toast.LENGTH_SHORT).show();
        } else {

            Collections.sort(listSongs, (song1, song2) -> song1.getPosition() - song2.getPosition());
            Toast.makeText(this, "Shuffle Off", Toast.LENGTH_SHORT).show();
        }
    }
    private void playNext() {
        if (position < listSongs.size() - 1) {
            position++;
            Uri uri = Uri.parse(listSongs.get(position).getPath());
            initializeMediaPlayer(uri);
            song_name.setText(listSongs.get(position).getTitle());
            song_artist.setText(listSongs.get(position).getArtist());
        } else {
            position = 0; // Loop to the first song if at the end of the playlist
            Uri uri = Uri.parse(listSongs.get(position).getPath());
            initializeMediaPlayer(uri);
            song_name.setText(listSongs.get(position).getTitle());
            song_artist.setText(listSongs.get(position).getArtist());
        }
    }

    private void playPrev() {
        if (position > 0) {
            position--;
            Uri uri = Uri.parse(listSongs.get(position).getPath());
            initializeMediaPlayer(uri);
            song_name.setText(listSongs.get(position).getTitle());
            song_artist.setText(listSongs.get(position).getArtist());
        } else {
            position = listSongs.size() - 1; // Loop to the last song if at the beginning of the playlist
            Uri uri = Uri.parse(listSongs.get(position).getPath());
            initializeMediaPlayer(uri);
            song_name.setText(listSongs.get(position).getTitle());
            song_artist.setText(listSongs.get(position).getArtist());
        }
    }




    // Function to toggle repeat
    private static final int REPEAT_OFF = 0;
    private static final int REPEAT_ONE = 1;
    private static final int REPEAT_ALL = 2;
    private int repeatMode = REPEAT_OFF;

    private void toggleRepeat() {
        switch (repeatMode) {
            case REPEAT_OFF:
                // Switch to Repeat One
                mediaPlayer.setLooping(true);
                Toast.makeText(this, "Repeat One", Toast.LENGTH_SHORT).show();
                id_repeat.setVisibility(View.GONE);
                id_repeat.setVisibility(View.VISIBLE);
                repeatMode = REPEAT_ONE;
                break;
            case REPEAT_ONE:
                // Switch to Repeat All
                mediaPlayer.setLooping(false);
                Toast.makeText(this, "Repeat All", Toast.LENGTH_SHORT).show();
                id_repeat.setVisibility(View.GONE);
                id_repeat.setVisibility(View.VISIBLE); // You need to have a repeat all button
                repeatMode = REPEAT_ALL;
                break;
            case REPEAT_ALL:
                // Switch to Repeat Off
                mediaPlayer.setLooping(false);
                Toast.makeText(this, "Repeat Off", Toast.LENGTH_SHORT).show();
                id_repeat.setVisibility(View.GONE);
                id_repeat.setVisibility(View.VISIBLE);
                repeatMode = REPEAT_OFF;
                break;
        }
    }



    private void updateUIAndSeekbar() {
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                    durationPlayedStart.setText(formattedTime(mCurrentPosition));

                    // Set the total duration at the beginning of playback
                    int totalDuration = mediaPlayer.getDuration() / 1000;
                    durationPlayedEnd.setText(formattedTime(totalDuration));
                }
                handler.postDelayed(this, 1000);
            }
        });
    }



    private String formattedTime(int mCurrentPosition) {
        String seconds = String.valueOf(mCurrentPosition % 60);
        String minutes = String.valueOf(mCurrentPosition / 60);


        if (seconds.length() == 1) {
            seconds = "0" + seconds;
        }

        return minutes + ":" + seconds;
    }


    private void initViews() {
        song_name = findViewById(R.id.song_name);
        song_artist = findViewById(R.id.song_artist);
        durationPlayedStart = findViewById(R.id.durationPlayedStart);
        durationPlayedEnd = findViewById(R.id.durationPlayedEnd);
        cover_art = findViewById(R.id.cover_art);
        id_next = findViewById(R.id.id_next);
        id_shuffle_off = findViewById(R.id.id_shuffle_off);
        id_repeat = findViewById(R.id.id_repeat);
        play_pause = findViewById(R.id.play_pause);
        seekBar = findViewById(R.id.seekBar);
    }


    private void getIntentMethod() {
        position = getIntent().getIntExtra("position", -1);
        listSongs = musicFiles;
        if (listSongs != null && position >= 0 && position < listSongs.size()) {
            play_pause.setImageResource(R.drawable.baseline_pause);
            uri = Uri.parse(listSongs.get(position).getPath());
            initializeMediaPlayer(uri);
        }
    }

    // Initialize the media player
    private void initializeMediaPlayer(Uri uri) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);

        // Set the completion listener to play the next track
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // Check if repeatMode is REPEAT_ONE
                if (repeatMode == REPEAT_ONE) {
                    // If repeatMode is REPEAT_ONE, replay the current track
                    mp.start();
                } else {
                    // If not, play the next track
                    playNext();
                }
            }
        });

        mediaPlayer.start();

        // Set the maximum value of the seekBar based on the duration of the media player.
        seekBar.setMax(mediaPlayer.getDuration() / 1000);
        metaData(uri);
    }


    private void metaData(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int durationTotal = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        durationPlayedEnd.setText(formattedTime(durationTotal));
        byte[] art = retriever.getEmbeddedPicture();
        Bitmap bitmap;
        if (art != null) {
            bitmap = BitmapFactory.decodeByteArray(art,0,art.length);
            ImageAnimation(this,cover_art,bitmap);
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(@Nullable Palette palette) {
                    Palette.Swatch swatch = palette.getDominantSwatch();
                    if (swatch != null)
                    {
                        ImageView grediend = findViewById(R.id.imageViewGredied);
                        RelativeLayout material_child_content_container = findViewById(R.id.material_child_content_container);
                        grediend.setBackgroundResource(R.drawable.grediend_bg);
                        material_child_content_container.setBackgroundResource(R.drawable.main_bg);
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb() ,0x00000000});
                        grediend.setBackground(gradientDrawable);
                        GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb() , swatch.getRgb()});
                        material_child_content_container.setBackground(gradientDrawableBg);
                        song_name.setTextColor(swatch.getTitleTextColor() );
                        song_artist.setTextColor(swatch.getBodyTextColor());
                    }
                    else
                    {
                        ImageView grediend = findViewById(R.id.imageViewGredied);
                        RelativeLayout material_child_content_container = findViewById(R.id.material_child_content_container);
                        grediend.setBackgroundResource(R.drawable.grediend_bg);
                        material_child_content_container.setBackgroundResource(R.drawable.main_bg);
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{0xff000000,0x00000000});
                        grediend.setBackground(gradientDrawable);
                        GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{0xff000000 , 0xff000000});
                        material_child_content_container.setBackground(gradientDrawableBg);
                        song_name.setTextColor(Color.WHITE);
                        song_artist.setTextColor(Color.DKGRAY);
                    }

                }
            });

        } else {
            Glide.with(this)
                    .asBitmap()
                    .load(R.drawable.bewdoc)
                    .into(cover_art);
        }
    }
    public void ImageAnimation (Context context,ImageView imageView,Bitmap bitmap)
    {
        Animation animOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        Animation animIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);
                animIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                imageView.startAnimation(animIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        imageView.startAnimation(animOut);
    }
}

