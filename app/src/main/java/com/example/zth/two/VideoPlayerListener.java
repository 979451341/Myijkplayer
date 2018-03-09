package com.example.zth.two;

/**
 * Created by ZTH on 2018/3/8.
 */

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * 提供回调的接口
 * Created by GuoShaoHong on 2017/3/15.
 */

public abstract class VideoPlayerListener implements IMediaPlayer.OnBufferingUpdateListener, IMediaPlayer.OnCompletionListener, IMediaPlayer.OnPreparedListener, IMediaPlayer.OnInfoListener, IMediaPlayer.OnVideoSizeChangedListener, IMediaPlayer.OnErrorListener, IMediaPlayer.OnSeekCompleteListener {
}