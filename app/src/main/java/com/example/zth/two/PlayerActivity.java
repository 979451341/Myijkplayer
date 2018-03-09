package com.example.zth.two;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class PlayerActivity extends AppCompatActivity {

    @BindView(R.id.ijk_player)
    VideoPlayerIJK ijkPlayer;
    @BindView(R.id.btn_back)
    Button btnBack;
    @BindView(R.id.btn_setting)
    Button btnSetting;
    @BindView(R.id.btn_play)
    Button btnPlay;
    @BindView(R.id.seekBar)
    SeekBar seekBar;
    @BindView(R.id.btn_stop)
    Button btnStop;
    @BindView(R.id.tv_time)
    TextView tvTime;
    @BindView(R.id.tv_load_msg)
    TextView tvLoadMsg;
    @BindView(R.id.pb_loading)
    ProgressBar pbLoading;
    @BindView(R.id.rl_loading)
    RelativeLayout rlLoading;
    @BindView(R.id.tv_play_end)
    TextView tvPlayEnd;
    private String path;
    private boolean start = false;
    private Handler handler;

    public static final int MSG_REFRESH = 1001;
    private TimerTask timerTask;
    private Timer timer;
    private long time;

    private RelativeLayout rl_bottom, rl_top;
    private boolean menu_visible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        ButterKnife.bind(this);

        init();
    }

    private void init() {
        ButterKnife.bind(this);


        rl_bottom = (RelativeLayout) findViewById(R.id.include_play_bottom);
        rl_top = (RelativeLayout) findViewById(R.id.include_play_top);
        path = getIntent().getExtras().getString("path", "");

        //加载so文件
        try {
            IjkMediaPlayer.loadLibrariesOnce(null);
            IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        } catch (Exception e) {
            this.finish();
        }
        ijkPlayer = findViewById(R.id.ijk_player);

        ijkPlayer.setListener(new VideoPlayerListener() {
            @Override
            public void onBufferingUpdate(IMediaPlayer mp, int percent) {
            }

            @Override
            public void onCompletion(IMediaPlayer mp) {
                btnPlay.setText("播放");
            }

            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                return false;
            }

            @Override
            public boolean onInfo(IMediaPlayer mp, int what, int extra) {
                return false;
            }

            @Override
            public void onPrepared(IMediaPlayer mp) {
                mp.start();
                rlLoading.setVisibility(View.GONE);
            }

            @Override
            public void onSeekComplete(IMediaPlayer mp) {

            }

            @Override
            public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {

            }
        });


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //进度改变
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //开始拖动

                handler.removeCallbacksAndMessages(null);

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //停止拖动
                ijkPlayer.seekTo(ijkPlayer.getDuration() * seekBar.getProgress() / 100);
                handler.sendEmptyMessageDelayed(MSG_REFRESH, 100);
            }
        });

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_REFRESH:
                        if (ijkPlayer.isPlaying()) {
                            refresh();
                            handler.sendEmptyMessageDelayed(MSG_REFRESH, 1000);
                        }

                        break;
                }

            }
        };


        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                long t = System.currentTimeMillis();
                if (t - time > 3000 && menu_visible) {
                    time = t;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move_bottom);
                            rl_bottom.startAnimation(animation);
                            Animation animation_top = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move_top);
                            rl_top.startAnimation(animation_top);
                            menu_visible = false;


                        }
                    });
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setVisible(View.INVISIBLE);
                        }
                    }, 500);
                }


            }
        };

        timer.schedule(timerTask, 1000, 1000);


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (start == false) {
            start = true;
            if (!path.equals("")) {

                loadVideo(path);
                handler.sendEmptyMessageDelayed(MSG_REFRESH, 1000);

            }

            time = System.currentTimeMillis();
        }
    }

    public void loadVideo(String path) {
        ijkPlayer.setVideoPath(path);
    }


    @Override
    protected void onStop() {
        IjkMediaPlayer.native_profileEnd();
        handler.removeCallbacksAndMessages(null);
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        if (ijkPlayer != null) {
            ijkPlayer.stop();
            ijkPlayer.release();
            ijkPlayer = null;
        }

        super.onDestroy();
    }

    //正真的全屏，隐藏了状态栏、AtionBar、导航栏
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @OnClick({R.id.ijk_player, R.id.btn_back, R.id.btn_setting, R.id.btn_play, R.id.btn_stop})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ijk_player:
                if (menu_visible == false) {
                    setVisible(View.VISIBLE);
                    Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.show_bottom);
                    rl_bottom.startAnimation(animation);
                    Animation animation_top = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.show_top);
                    rl_top.startAnimation(animation_top);
                    menu_visible = true;
                    time = System.currentTimeMillis();
                }

                break;
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_setting:
                break;
            case R.id.btn_play:
                if (btnPlay.getText().toString().equals(getResources().getString(R.string.pause))) {
                    ijkPlayer.pause();
                    btnPlay.setText(getResources().getString(R.string.media_play));
                } else {
                    if(tvPlayEnd.getVisibility() == View.VISIBLE){
                        ijkPlayer.setVideoPath(path);
                    }
                    ijkPlayer.start();

                    tvPlayEnd.setVisibility(View.INVISIBLE);
                    btnPlay.setText(getResources().getString(R.string.pause));
                }

                break;
            case R.id.btn_stop:
                btnPlay.setText(getResources().getString(R.string.media_play));
                ijkPlayer.stop();
                tvPlayEnd.setVisibility(View.VISIBLE);
                break;
        }
    }



    private void refresh() {
        long current = ijkPlayer.getCurrentPosition() / 1000;
        long duration = ijkPlayer.getDuration() / 1000;
        Log.v("zzw", current + " " + duration);
        long current_second = current % 60;
        long current_minute = current / 60;
        long total_second = duration % 60;
        long total_minute = duration / 60;
        String time = current_minute + ":" + current_second + "/" + total_minute + ":" + total_second;
        tvTime.setText(time);
        if (duration != 0) {
            seekBar.setProgress((int) (current * 100 / duration));
        }

    }

    private void setVisible(int state) {
        btnPlay.setVisibility(state);
        btnStop.setVisibility(state);
        btnBack.setVisibility(state);
        btnSetting.setVisibility(state);
        seekBar.setVisibility(state);


    }


}
