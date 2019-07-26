/*
 * Copyright (C) 2015 Zhang Rui <bbcallen@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.menglong.videoplayer.ijkplayer;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.TableLayout;
import com.menglong.videoplayer.R;
import com.menglong.videoplayer.analytics.PlayerBaseInfo;
import com.menglong.videoplayer.util.LanguageInfoTable;
import com.menglong.videoplayer.view.IRenderView;
import com.menglong.videoplayer.view.SurfaceRenderView;
import com.menglong.videoplayer.view.TextureRenderView;
import com.star.util.Logger;
import com.star.util.PostHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.TextureMediaPlayer;
import tv.danmaku.ijk.media.player.misc.IMediaDataSource;
import tv.danmaku.ijk.media.player.misc.ITrackInfo;


public class IjkVideoView extends FrameLayout implements MediaController.MediaPlayerControl {
    //-------------------------
    // Extend: Render
    //-------------------------
    public static final int RENDER_NONE = 0;
    public static final int RENDER_SURFACE_VIEW = 1;
    public static final int RENDER_TEXTURE_VIEW = 2;
    // all possible internal states
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;

    public static final int MSG_URL_ERROR = 14;
    public static final int MSG_LOAD_TIMEOUT = 15;

    public static final int PAUSE_AUTO = 0;
    public static final int PAUSE_MANUAL = 1;

    private static final int[] s_allAspectRatio = {
            IRenderView.AR_ASPECT_FIT_PARENT,
            IRenderView.AR_ASPECT_FILL_PARENT,
            IRenderView.AR_ASPECT_WRAP_CONTENT,
            // IRenderView.AR_MATCH_PARENT,
            IRenderView.AR_16_9_FIT_PARENT,
            IRenderView.AR_4_3_FIT_PARENT};
    private boolean bPlayerLog = false;
    // private int         mAudioSession;
    final int DEFAULT_VIDEO_SIZE = 0;
    // settable by the client
    private Uri mUri;
    private Map<String, String> mHeaders;
    // mCurrentState is a VideoView object's current state.
    // mTargetState is the state that a method caller intends to reach.
    // For instance, regardless the VideoView object's current state,
    // calling pause() intends to bring the object to a target state
    // of STATE_PAUSED.
    private int mCurrentState = STATE_IDLE;
    private int mTargetState = STATE_IDLE;
    // All the stuff we need for playing and showing a video
    private IRenderView.ISurfaceHolder mSurfaceHolder = null;
    private IMediaPlayer mMediaPlayer = null;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private int mVideoRotationDegree;
    private IMediaController mMediaController;
    private IMediaPlayer.OnCompletionListener mOnCompletionListener;
    private IMediaPlayer.OnPreparedListener mOnPreparedListener;
    private int mCurrentBufferPercentage;
    private IMediaPlayer.OnErrorListener mOnErrorListener;
    private IMediaPlayer.OnInfoListener mOnInfoListener;
    private PauseListener mPauseListener;
    private FinishListener mFinishListener;
    private sendErrorListener mSendErrorListener;
    private PostHandler mStartTimeOut;
    private int mSeekWhenPrepared;  // recording the seek position while preparing
    private boolean mCanPause = true;
    private boolean mCanSeekBack = true;
    private boolean mCanSeekForward = true;

    /**
     * Subtitle rendering widget overlaid on top of the video.
     */
    // private RenderingWidget mSubtitleWidget;
    //zy add
    private boolean mIsLoading = true;
    private boolean mIspaused = true;
    private boolean mIsStarted = false;
    private PlayerOptions mPlayerOptions = null;
    private boolean mCanResizeRender = true;
    private PlayerBaseInfo mPlayerBaseInfo;

    //private InfoHudViewHolder mHudViewHolder;
    /**
     * Listener for changes to subtitle data, used to redraw when needed.
     */
    // private RenderingWidget.OnChangedListener mSubtitlesChangedListener;

    private Context mAppContext;
    private Settings mSettings;
    private IRenderView mRenderView;
    private int mVideoSarNum;
    private int mVideoSarDen;
    IMediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
            new IMediaPlayer.OnVideoSizeChangedListener() {
                public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sarNum, int sarDen) {
                    mVideoWidth = mp.getVideoWidth();
                    mVideoHeight = mp.getVideoHeight();
                    mVideoSarNum = mp.getVideoSarNum();
                    mVideoSarDen = mp.getVideoSarDen();
                    if (mVideoWidth != 0 && mVideoHeight != 0) {
                        //if (mRenderView != null) {
                        //mRenderView.setVideoSize(mVideoWidth, mVideoHeight);
                        //mRenderView.setVideoSampleAspectRatio(mVideoSarNum, mVideoSarDen);
                        //}
                        // REMOVED: getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                        requestLayout();
                    }
                }
            };
    private long mPrepareStartTime = 0;
    private long mPrepareEndTime = 0;
    private long mSeekStartTime = 0;
    private long mSeekEndTime = 0;

    // REMOVED: onMeasure
    // REMOVED: onInitializeAccessibilityEvent
    // REMOVED: onInitializeAccessibilityNodeInfo
    // REMOVED: onInitializeAccessibilityNodeInfo
    // REMOVED: resolveAdjustedSize
    private boolean isLive = false;
    private boolean isDTLog = true;
    IMediaPlayer.OnPreparedListener mPreparedListener = new IMediaPlayer.OnPreparedListener() {
        public void onPrepared(IMediaPlayer mp) {
            mPrepareEndTime = System.currentTimeMillis();
//            mHudViewHolder.updateLoadCost(mPrepareEndTime - mPrepareStartTime);
            mCurrentState = STATE_PREPARED;

            // Get the capabilities of the player for this stream
            // REMOVED: Metadata

            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared(mMediaPlayer);
            }
            if (mMediaController != null) {
                mMediaController.setEnabled(true);
            }
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();

            int seekToPosition = mSeekWhenPrepared;  // mSeekWhenPrepared may be changed after seekTo() call
            if (seekToPosition != 0) {
                seekTo(seekToPosition);
            }
            if (mVideoWidth != 0 && mVideoHeight != 0 && mCanResizeRender) {
                //Log.i("@@@@", "video size: " + mVideoWidth +"/"+ mVideoHeight);
                // REMOVED: getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                if (mRenderView != null) {
                    mRenderView.setVideoSize(mVideoWidth, mVideoHeight);
                    mRenderView.setVideoSampleAspectRatio(mVideoSarNum, mVideoSarDen);
                    if (!mRenderView.shouldWaitForResize() || mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
                        // We didn't actually change the size (it was already at the size
                        // we need), so we won't get a "surface changed" callback, so
                        // start the video here instead of in the callback.
                        if (mTargetState == STATE_PLAYING) {
                            start();
                            if (mMediaController != null) {
                                mMediaController.show();
                            }
                        } else if (!isPlaying() &&
                                (seekToPosition != 0 || getCurrentPosition() > 0)) {
                            if (mMediaController != null) {
                                // Show the media controls when we're paused into a video and make 'em stick.
                                mMediaController.show(0);
                            }
                        }
                    }
                }
            } else {
                // We don't know the video size yet, but should start anyway.
                // The video size might be reported to us later.
                if (mTargetState == STATE_PLAYING) {
                    start();
                }
            }
            //强制刷新可以加快首帧显示
            if (mMediaPlayer != null)
                mMediaPlayer.imageRefresh();
        }
    };
    private IMediaPlayer.OnCompletionListener mCompletionListener =
            new IMediaPlayer.OnCompletionListener() {
                public void onCompletion(IMediaPlayer mp) {

                    mCurrentState = STATE_PLAYBACK_COMPLETED;
                    mTargetState = STATE_PLAYBACK_COMPLETED;
                    if (mMediaController != null) {
                        mMediaController.hide();
                    }
                    {
                        if (mFinishListener != null)
                            mFinishListener.show();

                    }
                    if (mOnCompletionListener != null) {
                        mOnCompletionListener.onCompletion(mMediaPlayer);
                    }
                }
            };
    private IMediaPlayer.OnStartLogListener mOnStartLogListener =
            new IMediaPlayer.OnStartLogListener() {
                public boolean onInfo(IMediaPlayer mp, String log) {
                    //Logger.d("CountlyGOLOG: " + log);

                    String[] returnKey = new String[1];
                    returnKey[0] = "";
                    Logger.i("startLog = " + log);
                    return true;
                }
            };
    private IMediaPlayer.OnInfoListener mInfoListener =
            new IMediaPlayer.OnInfoListener() {
                public boolean onInfo(IMediaPlayer mp, int arg1, int arg2) {
                    if (mOnInfoListener != null) {
                        mOnInfoListener.onInfo(mp, arg1, arg2);
                    }
                    switch (arg1) {
                        case IMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                            Logger.d("MEDIA_INFO_VIDEO_TRACK_LAGGING:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                            //首帧有可能是个黑屏帧，所以强制再刷新一帧

                            if (mMediaPlayer != null)
                                mMediaPlayer.imageRefresh();
                            mRenderView.setVideoSize(mVideoWidth, mVideoHeight);
                            mRenderView.setVideoSampleAspectRatio(mVideoSarNum, mVideoSarDen);
                            playbackStartProcess();
                            Logger.d("MEDIA_INFO_VIDEO_RENDERING_START: timestamp=" + mMediaPlayer.getPlayStartTimestamp());
                            break;
                        case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                            mIsLoading = true;
                            Logger.d("zy MEDIA_INFO_BUFFERING_START:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                            mIsLoading = false;
                            Logger.d("zy MEDIA_INFO_BUFFERING_END:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH:
                            Logger.d("MEDIA_INFO_NETWORK_BANDWIDTH: " + arg2);
                            break;
                        case IMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                            Logger.d("MEDIA_INFO_BAD_INTERLEAVING:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                            Logger.d("MEDIA_INFO_NOT_SEEKABLE:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                            Logger.d("MEDIA_INFO_METADATA_UPDATE:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
                            Logger.d("MEDIA_INFO_UNSUPPORTED_SUBTITLE:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:
                            Logger.d("MEDIA_INFO_SUBTITLE_TIMED_OUT:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                            mVideoRotationDegree = arg2;
                            Logger.d("MEDIA_INFO_VIDEO_ROTATION_CHANGED: " + arg2);
                            if (mRenderView != null)
                                mRenderView.setVideoRotation(arg2);
                            break;
                        case IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START:
                            playbackStartProcess();
                            Logger.d("MEDIA_INFO_AUDIO_RENDERING_START: timestamp=" + mMediaPlayer.getPlayStartTimestamp());
                            break;
                    }
                    return true;
                }
            };
    private IMediaPlayer.OnErrorListener mErrorListener =
            new IMediaPlayer.OnErrorListener() {
                public boolean onError(IMediaPlayer mp, int framework_err, int impl_err) {
                    Logger.w("Error: " + framework_err + "," + impl_err);
                    mCurrentState = STATE_ERROR;
                    mTargetState = STATE_ERROR;
                    if (mMediaController != null) {
                        mMediaController.hide();
                    }

                    /* If an error handler has been supplied, use it and finish. */
                    if (mOnErrorListener != null) {
                        if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
                            return true;
                        }
                    }

                    stopStartTimeOut();
                    mSendErrorListener.sendError(MSG_URL_ERROR, impl_err);


                    /* Otherwise, pop up an error dialog so the user knows that
                     * something bad has happened. Only try and pop up the dialog
                     * if we're attached to a window. When we're going away and no
                     * longer have a window, don't bother showing the user an error.
                     */
//                    if (getWindowToken() != null) {
//                        Resources r = mAppContext.getResources();
//                        int messageId;
//
//                        if (framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
//                            messageId = R.string.VideoView_error_text_invalid_progressive_playback;
//                        } else {
//                            messageId = R.string.VideoView_error_text_unknown;
//                        }
//
//                        new AlertDialog.Builder(getContext())
//                                .setMessage(messageId)
//                                .setPositiveButton(R.string.VideoView_error_button,
//                                        new DialogInterface.OnClickListener() {
//                                            public void onClick(DialogInterface dialog, int whichButton) {
//                                            /* If we get here, there is no onError listener, so
//                                             * at least inform them that the video is over.
//                                             */
//                                                if (mOnCompletionListener != null) {
//                                                    mOnCompletionListener.onCompletion(mMediaPlayer);
//                                                }
//                                            }
//                                        })
//                                .setCancelable(false)
//                                .show();
//                    }
                    return true;
                }
            };
    private IMediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
            new IMediaPlayer.OnBufferingUpdateListener() {
                public void onBufferingUpdate(IMediaPlayer mp, int percent) {
                    mCurrentBufferPercentage = percent;
                }
            };
    private IMediaPlayer.OnSeekCompleteListener mSeekCompleteListener = new IMediaPlayer.OnSeekCompleteListener() {

        @Override
        public void onSeekComplete(IMediaPlayer mp) {
            mSeekEndTime = System.currentTimeMillis();
//            mHudViewHolder.updateSeekCost(mSeekEndTime - mSeekStartTime);
        }
    };

    // REMOVED: addSubtitleSource
    // REMOVED: mPendingSubtitleTracks
    IRenderView.IRenderCallback mSHCallback = new IRenderView.IRenderCallback() {
        @Override
        public void onSurfaceChanged(/*@NonNull*/ IRenderView.ISurfaceHolder holder, int format, int w, int h) {
            if (holder.getRenderView() != mRenderView) {
                Logger.i("onSurfaceChanged: unmatched render callback\n");
                return;
            }

            Logger.i("onSurfaceChanged: w=" + w + "h=" + h);

            mSurfaceWidth = w;
            mSurfaceHeight = h;
            boolean isValidState = (mTargetState == STATE_PLAYING);
            boolean hasValidSize = !mRenderView.shouldWaitForResize() || (mVideoWidth == w && mVideoHeight == h);
            if (mMediaPlayer != null && isValidState && hasValidSize) {
                if (mSeekWhenPrepared != 0) {
                    seekTo(mSeekWhenPrepared);
                }
                start();
            }

            try {
                if (mMediaPlayer != null)
                    mMediaPlayer.imageRefresh();
            } catch (IllegalStateException ex) {
                Logger.i("onSurfaceChanged refresh image exception");
            }
        }

        @Override
        public void onSurfaceCreated(/*@NonNull*/ IRenderView.ISurfaceHolder holder, int width, int height) {
            if (holder.getRenderView() != mRenderView) {
                Logger.i("onSurfaceCreated: unmatched render callback\n");
                return;
            }

            Logger.i("onSurfaceCreated: w=" + width + "h=" + height);
            mSurfaceHolder = holder;
            if (mMediaPlayer != null)
                bindSurfaceHolder(mMediaPlayer, holder);
            else
                openVideo();
        }

        @Override
        public void onSurfaceDestroyed(/*@NonNull*/ IRenderView.ISurfaceHolder holder) {
            if (holder.getRenderView() != mRenderView) {
                Logger.i("onSurfaceDestroyed: unmatched render callback\n");
                return;
            }

            Logger.i("onSurfaceDestroyed");
            // after we return from this we can't use the surface any more
            mSurfaceHolder = null;
            // REMOVED: if (mMediaController != null) mMediaController.hide();
            // REMOVED: release(true);
            releaseWithoutStop();
        }
    };
    private int mCurrentAspectRatioIndex = 0;
    private int mCurrentAspectRatio = s_allAspectRatio[0];
    private List<Integer> mAllRenders = new ArrayList<Integer>();
    private int mCurrentRenderIndex = 0;
    private int mCurrentRender = RENDER_NONE;

    public IjkVideoView(Context context) {
        super(context);
        initVideoView(context);
    }

    public IjkVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initVideoView(context);
    }

    public IjkVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVideoView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public IjkVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initVideoView(context);
    }

    private boolean isPlayerLog() {
        return bPlayerLog;
    }

    private void initVideoView(Context context) {
        mAppContext = context.getApplicationContext();
        mSettings = new Settings(mAppContext);
        setBackgroundColor(context.getResources().getColor(R.color.md_dim_gray));
        initRenders();
        mVideoWidth = 0;
        mVideoHeight = 0;
        // REMOVED: getHolder().addCallback(mSHCallback);
        // REMOVED: getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        // REMOVED: mPendingSubtitleTracks = new Vector<Pair<InputStream, MediaFormat>>();
        mCurrentState = STATE_IDLE;
        mTargetState = STATE_IDLE;
        mIsStarted = false;
    }

    public void setPlayerBaseInfo(PlayerBaseInfo baseInfo) {
        this.mPlayerBaseInfo = baseInfo;
    }

    public PlayerOptions getPlayerOptions() {
        return mPlayerOptions;
    }

    public void setPlayerOptions(PlayerOptions playerOptions) {
        mPlayerOptions = playerOptions;
    }

    public void setRenderView(IRenderView renderView) {
        if (mRenderView != null) {
            if (mMediaPlayer != null)
                mMediaPlayer.setDisplay(null);

            View renderUIView = mRenderView.getView();
            mRenderView.removeRenderCallback(mSHCallback);
            mRenderView = null;
            removeView(renderUIView);
        }

        if (renderView == null)
            return;

        mRenderView = renderView;
        renderView.setAspectRatio(mCurrentAspectRatio);
        if (mVideoWidth > 0 && mVideoHeight > 0)
            renderView.setVideoSize(mVideoWidth, mVideoHeight);
        if (mVideoSarNum > 0 && mVideoSarDen > 0)
            renderView.setVideoSampleAspectRatio(mVideoSarNum, mVideoSarDen);

        View renderUIView = mRenderView.getView();
        LayoutParams lp = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);
        renderUIView.setLayoutParams(lp);
        addView(renderUIView);

        mRenderView.addRenderCallback(mSHCallback);
        mRenderView.setVideoRotation(mVideoRotationDegree);
    }

    public void setRender(int render) {
        switch (render) {
            case RENDER_NONE:
                setRenderView(null);
                break;
            case RENDER_TEXTURE_VIEW: {
                TextureRenderView renderView = new TextureRenderView(getContext());
                if (mMediaPlayer != null) {
                    renderView.getSurfaceHolder().bindToMediaPlayer(mMediaPlayer);
                    renderView.setVideoSize(mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());
                    renderView.setVideoSampleAspectRatio(mMediaPlayer.getVideoSarNum(), mMediaPlayer.getVideoSarDen());
                    renderView.setAspectRatio(mCurrentAspectRatio);
                }
                setRenderView(renderView);
                break;
            }
            case RENDER_SURFACE_VIEW: {
                SurfaceRenderView renderView = new SurfaceRenderView(getContext());
                setRenderView(renderView);
                break;
            }
            default:
                Logger.w(String.format(Locale.getDefault(), "invalid render %d\n", render));
                break;
        }
    }

    public void setHudView(TableLayout tableLayout) {
//        mHudViewHolder = new InfoHudViewHolder(getContext(), tableLayout);
    }

    /**
     * Sets video path.
     *
     * @param path the path of the video.
     */
    public void setVideoPath(String path) {
        setVideoURI(Uri.parse(path));
    }

    /**
     * Sets video URI.
     *
     * @param uri the URI of the video.
     */
    public void setVideoURI(Uri uri) {
        setVideoURI(uri, null);
    }

    /**
     * Sets video URI using specific headers.
     *
     * @param uri     the URI of the video.
     * @param headers the headers for the URI request.
     *                Note that the cross domain redirection is allowed by default, but that can be
     *                changed with key/value pairs through the headers parameter with
     *                "android-allow-cross-domain-redirect" as the key and "0" or "1" as the value
     *                to disallow or allow cross domain redirection.
     */
    private void setVideoURI(Uri uri, Map<String, String> headers) {
        mUri = uri;
        mHeaders = headers;
        mSeekWhenPrepared = 0;
        openVideo();
        requestLayout();
        invalidate();
    }


    public void stopPlayback(int reason) {
        stopStartTimeOut();
        if (mMediaPlayer != null) {
            try {
                mCanResizeRender = false;
                mMediaPlayer.stop();
                mMediaPlayer.release();
            } catch (IllegalStateException e) {
                Logger.e("stopPlayback error: " + e.toString());
            }
            mUri = null;
            mMediaPlayer = null;
//            if (mHudViewHolder != null)
//                mHudViewHolder.setMediaPlayer(null);
            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;
            mIsStarted = false;
            AudioManager am = (AudioManager) mAppContext.getSystemService(Context.AUDIO_SERVICE);
            am.abandonAudioFocus(null);
        }
    }

    public void setLive(boolean live) {
        isLive = live;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private void openVideo() {
        if (mUri == null || mSurfaceHolder == null) {
            // not ready for playback just yet, will try again later
            return;
        }
        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        release(false);
        AudioManager am = (AudioManager) mAppContext.getSystemService(Context.AUDIO_SERVICE);
        am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        try {
            mMediaPlayer = createPlayer(mSettings.getPlayer());
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnInfoListener(mInfoListener);
            mMediaPlayer.setOnStartLogListener(mOnStartLogListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
            mCurrentBufferPercentage = 0;
            String scheme = mUri.getScheme();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 &&
                    mSettings.getUsingMediaDataSource() &&
                    (TextUtils.isEmpty(scheme) || scheme.equalsIgnoreCase("file"))) {
                IMediaDataSource dataSource = new FileMediaDataSource(new File(mUri.toString()));
                mMediaPlayer.setDataSource(dataSource);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mMediaPlayer.setDataSource(mAppContext, mUri, mHeaders);
                /*if(mUri.getScheme().equals(ContentResolver.SCHEME_ANDROID_RESOURCE)){
                    RawDataSourceProvider rawDataSourceProvider = RawDataSourceProvider.create(mAppContext, mUri);
                    mMediaPlayer.setDataSource(rawDataSourceProvider);
                }*/
            } else {
                mMediaPlayer.setDataSource(mUri.toString());
            }
            bindSurfaceHolder(mMediaPlayer, mSurfaceHolder);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mPrepareStartTime = System.currentTimeMillis();
            setStartTimeOut();//设置超时计时器

            mIsLoading = true;
            mIspaused = false;

            mMediaPlayer.prepareAsync();

            mCurrentState = STATE_PREPARING;
            attachMediaController();
        } catch (IOException | IllegalArgumentException ex) {
            Logger.e("Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
        }
//        finally {
//            // REMOVED: mPendingSubtitleTracks.clear();
//        }
    }


    public void setMediaController(IMediaController controller) {
        if (mMediaController != null) {
            mMediaController.hide();
        }
        mMediaController = controller;
        attachMediaController();
    }

    private void attachMediaController() {
        if (mMediaPlayer != null && mMediaController != null) {
            mMediaController.setMediaPlayer(this);
            View anchorView = this.getParent() instanceof View ?
                    (View) this.getParent() : this;
            mMediaController.setAnchorView(anchorView);
            mMediaController.setEnabled(isInPlaybackState());
        }
    }

    public long getPlayStartTimestamp() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getPlayStartTimestamp();
        }
        return 0;
    }


    /////////////interfaces for notify uplayer player events///////////////////
//
//    /**
//     * Register a callback to be invoked when the media file
//     * is loaded and ready to go.
//     *
//     * @param l The callback that will be run
//     */
    public void setOnPreparedListener(IMediaPlayer.OnPreparedListener l) {
        mOnPreparedListener = l;
    }
//
//    /**
//     * Register a callback to be invoked when the end of a media file
//     * has been reached during playback.
//     *
//     * @param l The callback that will be run
//     */
//    public void setOnCompletionListener(IMediaPlayer.OnCompletionListener l) {
//        mOnCompletionListener = l;
//    }
//
//    /**
//     * Register a callback to be invoked when an error occurs
//     * during playback or setup.  If no listener is specified,
//     * or if the listener returned false, VideoView will inform
//     * the user of any errors.
//     *
//     * @param l The callback that will be run
//     */
////    public void setOnErrorListener(IMediaPlayer.OnErrorListener l) {
////        mOnErrorListener = l;
////    }
//
//    /**
//     * Register a callback to be invoked when an informational event
//     * occurs during playback or setup.
//     *
//     * @param l The callback that will be run
//     */
//    public void setOnInfoListener(IMediaPlayer.OnInfoListener l) {
//        mOnInfoListener = l;
//    }

    // REMOVED: mSHCallback
    private void bindSurfaceHolder(IMediaPlayer mp, IRenderView.ISurfaceHolder holder) {
        if (mp == null)
            return;

        if (holder == null) {
            mp.setDisplay(null);
            return;
        }

        holder.bindToMediaPlayer(mp);
    }

    public void releaseWithoutStop() {
        if (mMediaPlayer != null)
            mMediaPlayer.setDisplay(null);
    }

    /*
     * release the media player in any state
     */
    public void release(boolean cleartargetstate) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            // REMOVED: mPendingSubtitleTracks.clear();
            mCurrentState = STATE_IDLE;
            mIsStarted = false;
            if (cleartargetstate) {
                mTargetState = STATE_IDLE;
            }
            AudioManager am = (AudioManager) mAppContext.getSystemService(Context.AUDIO_SERVICE);
            am.abandonAudioFocus(null);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK &&
                keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
                keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
                keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
                keyCode != KeyEvent.KEYCODE_MENU &&
                keyCode != KeyEvent.KEYCODE_CALL &&
                keyCode != KeyEvent.KEYCODE_ENDCALL;
        if (isInPlaybackState() && isKeyCodeSupported && mMediaController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK ||
                    keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                } else {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!mMediaPlayer.isPlaying()) {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                }
                return true;
            } else {
                toggleMediaControlsVisiblity();
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void toggleMediaControlsVisiblity() {
        if (mMediaController.isShowing()) {
            mMediaController.hide();
        } else {
            mMediaController.show();
        }
    }

    @Override
    public void start() {
        try {
            if (isInPlaybackState()) {
                mMediaPlayer.start();
                mCurrentState = STATE_PLAYING;
            }
            mIspaused = false;
            mTargetState = STATE_PLAYING;
        } catch (IllegalStateException ex) {
            Logger.e("play start exception: " + mUri);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
        }
    }

    @Override
    public void pause() {
        pause(PAUSE_AUTO);
    }

    public void pause(int reason) {
        Logger.d("Player onPause");
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState = STATE_PAUSED;
            }
        }
        mIspaused = true;
        mTargetState = STATE_PAUSED;
    }


//    public void suspend() {
//        release(false);
//    }
//
//    // REMOVED: getAudioSessionId();
//    // REMOVED: onAttachedToWindow();
//    // REMOVED: onDetachedFromWindow();
//    // REMOVED: onLayout();
//    // REMOVED: draw();
//    // REMOVED: measureAndLayoutSubtitleWidget();
//    // REMOVED: setSubtitleWidget();
//    // REMOVED: getSubtitleLooper();
//
//    //-------------------------
//    // Extend: Aspect Ratio
//    //-------------------------
//
//    public void resume() {
//        openVideo();
//    }


    @Override
    public int getDuration() {
        if (isInPlaybackState()) {
            clearDefaultDurationIfPossible();
            return (int) mMediaPlayer.getDuration();
        }
        if (hasDefaultDurationValue()) {
            return mPlayerOptions.getDefaultDuration();
        }

        return -1;
    }

    @Override
    public int getCurrentPosition() {
        if (mCurrentState == STATE_PLAYBACK_COMPLETED)
            return getDuration();
        if (isInPlaybackState()) {
            clearDefaultDurationIfPossible();
            return (int) mMediaPlayer.getCurrentPosition();
        }
        if (hasDefaultDurationValue()) {
            return mPlayerOptions.getDefaultCurrentPosition();
        }
        return 0;
    }

    public int getPlayedDuration() {
        if (isInPlaybackState()) {
            return (int) mMediaPlayer.getPlayedDuration();
        }
        return 0;
    }

    @Override
    public void seekTo(int msec) {
        if (isInPlaybackState()) {
            mSeekStartTime = System.currentTimeMillis();
            mMediaPlayer.seekTo(msec);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
        }
    }

    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying() && mCurrentState != STATE_PAUSED;
    }

    @Override
    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    private boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
                mCurrentState != STATE_ERROR &&
                mCurrentState != STATE_IDLE &&
                mCurrentState != STATE_PREPARING);
    }

    @Override
    public boolean canPause() {
        return mCanPause;
    }

    @Override
    public boolean canSeekBackward() {
        return mCanSeekBack;
    }

    @Override
    public boolean canSeekForward() {
        return mCanSeekForward;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    public int toggleAspectRatio() {
        mCurrentAspectRatioIndex++;
        mCurrentAspectRatioIndex %= s_allAspectRatio.length;

        mCurrentAspectRatio = s_allAspectRatio[mCurrentAspectRatioIndex];
        if (mRenderView != null)
            mRenderView.setAspectRatio(mCurrentAspectRatio);
        return mCurrentAspectRatio;
    }

    private void initRenders() {
        mAllRenders.clear();

        if (mSettings.getEnableSurfaceView())
            mAllRenders.add(RENDER_SURFACE_VIEW);
        if (mSettings.getEnableTextureView() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            mAllRenders.add(RENDER_TEXTURE_VIEW);
        if (mSettings.getEnableNoView())
            mAllRenders.add(RENDER_NONE);

        if (mAllRenders.isEmpty())
            mAllRenders.add(RENDER_SURFACE_VIEW);
        mCurrentRender = mAllRenders.get(mCurrentRenderIndex);
        setRender(mCurrentRender);
    }

    //chenwq, set player options dynamically, such as:  cookies
    public void setPlayerOptions(String name, String value) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setPlayerOption(name, value);
        }
    }

    public IMediaPlayer createPlayer(int playerType) {
        IMediaPlayer mediaPlayer = null;

        switch (playerType) {
            case Settings.PV_PLAYER__IjkExoMediaPlayer: {
                //IjkExoMediaPlayer IjkExoMediaPlayer = new IjkExoMediaPlayer(mAppContext);
                //mediaPlayer = IjkExoMediaPlayer;
            }
            break;
            case Settings.PV_PLAYER__AndroidMediaPlayer: {
                //AndroidMediaPlayer androidMediaPlayer = new AndroidMediaPlayer();
                //mediaPlayer = androidMediaPlayer;
            }
            break;
            case Settings.PV_PLAYER__IjkMediaPlayer:
            default: {
                IjkMediaPlayer ijkMediaPlayer = null;
                if (mUri != null) {
                    ijkMediaPlayer = new IjkMediaPlayer();
                    ijkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_INFO);

                    if (mSettings.getUsingMediaCodec()) {
                        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
                        if (mSettings.getUsingMediaCodecAutoRotate()) {
                            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
                        } else {
                            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 0);
                        }
                        if (mSettings.getMediaCodecHandleResolutionChange()) {
                            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);
                        } else {
                            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 0);
                        }
                    } else {
                        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0);
                    }

                    if (mSettings.getUsingOpenSLES()) {
                        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 1);
                    } else {
                        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0);
                    }

                    String pixelFormat = mSettings.getPixelFormat();
                    if (TextUtils.isEmpty(pixelFormat)) {
                        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
                    } else {
                        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", pixelFormat);
                    }
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);

                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);

                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);

                    //zy add
                    String DnsString = getDnsString();
                    if (!DnsString.equals("")) {
                        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "star_DNS_set", DnsString);
                    } else {
                    }


                    String langKey = mPlayerOptions.getAudioLanguage();
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "audio_track", langKey);
                    String langKeyList = LanguageInfoTable.getInstance(mAppContext).getLanguageKeyList();
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "audio_track_priority_list", langKeyList);
                    Logger.d("start player with default audio track: " + langKey);

                    //chenwq: decrease decode thread to fix crash/anr bug
                    ijkMediaPlayer.setOption(ijkMediaPlayer.OPT_CATEGORY_CODEC, "threads", 2);

                    //loadrate depends on start_log so all should collect these logs
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "post_start_log", 1);

                    Random randomBufferMaxSize = new Random();
                    boolean bBufferMaxSize = mPlayerBaseInfo.getBufferMaxSizeRate() >= randomBufferMaxSize.nextInt(100) ? true : false;
                    Logger.d("buffer max size rate: " + mPlayerBaseInfo.getBufferMaxSizeRate());
                    if (bBufferMaxSize) {
                        int playerBufferMaxSize = mPlayerBaseInfo.getPlayerBufferMaxSize();
                        if (playerBufferMaxSize > 0) {
                            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-buffer-size", playerBufferMaxSize);
                            Logger.d("buffer max size: " + playerBufferMaxSize);
                        } else {
                            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-buffer-size", 1 * 1024 * 1024);
                        }
                    } else {
                        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-buffer-size", 1 * 1024 * 1024);
                    }

                    if (needStartSeek()) {
                        //不等于0，说明需要直接跳转到某个进度
                        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "seek-at-start", mPlayerOptions.getStartSeekPosition());
                        //min_start_seek_remaining_time设置播放到什么程度了就不进行开始时刻的seek，比如设置的是5000就意味这当播放到还剩5s的时候切换清晰度将从头开始播放
                        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min_start_seek_remaining_time", 5000);
                        mPlayerOptions.clearStartSeekPosition();
                    }
                    if (mPlayerOptions != null && !TextUtils.isEmpty(mPlayerOptions.getCookies())) {
                        ijkMediaPlayer.setPlayerOption("cookies", mPlayerOptions.getCookies());
                    }
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "protocol_whitelist", "file,crypto,hls,mpegts,http,tcp");//支持本地m3u8文件
                    /////////////////////////

                    Random random = new Random();
                    boolean bTcpRWTiemout = mPlayerBaseInfo.getTcpRWTimeoutRate() >= random.nextInt(100) ? true : false;
                    Logger.d("tcp rwtimeout rate: " + mPlayerBaseInfo.getTcpRWTimeoutRate());
                    if (bTcpRWTiemout) {
                        int startTcpRWTimeout = 30000000;
                        int playTcpRWTimeout = mPlayerBaseInfo.getTcpRWTimeout() * 1000000;
                        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "timeout", startTcpRWTimeout);
                        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "tcp_rwtimeout", playTcpRWTimeout);
                    }

                    Random randomPlayerlog = new Random();
                    bPlayerLog = mPlayerBaseInfo.getPlayerLogRate() >= randomPlayerlog.nextInt(100) ? true : false;
                    Logger.d("player log rate: " + mPlayerBaseInfo.getPlayerLogRate());
                    if (bPlayerLog) {
                        ijkMediaPlayer.initBufferLog(mPlayerBaseInfo.getPlayerBufferLogTime());
                        Logger.d("buffer log time: " + mPlayerBaseInfo.getPlayerBufferLogTime());
                    }

                    int liveStartIndex = mPlayerBaseInfo.getLiveStartIndex();
                    if (liveStartIndex < 0 && isLive) {
                        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "live_start_index", liveStartIndex);
                    }
                }
                mediaPlayer = ijkMediaPlayer;
            }
            break;
        }

        if (mSettings.getEnableDetachedSurfaceTextureView()) {
            mediaPlayer = new TextureMediaPlayer(mediaPlayer);
        }

        return mediaPlayer;
    }

    public boolean isPlayerCreated() {
        return mMediaPlayer == null ? false : true;
    }

    public ITrackInfo[] getTrackInfo() {
        if (mMediaPlayer == null)
            return null;

        return mMediaPlayer.getTrackInfo();
    }

    public void selectTrack(int stream) {
        MediaPlayerCompat.selectTrack(mMediaPlayer, stream);
    }

    public void deselectTrack(int stream) {
        MediaPlayerCompat.deselectTrack(mMediaPlayer, stream);
    }

    public int getSelectedTrack(int trackType) {
        return MediaPlayerCompat.getSelectedTrack(mMediaPlayer, trackType);
    }

    public String formatTime(int millis) {
        String time;
        long second = (millis / 1000) % 60;
        long minute = (millis / (1000 * 60)) % 60;
        long hour = (millis / (1000 * 60 * 60)) % 24;
        if (hour != 0)
            time = String.format("%02d:%02d:%02d", hour, minute, second);
        else
            time = String.format("%02d:%02d", minute, second);
        return time;
    }

    public void setmPauseListener(PauseListener pauseListener) {
        this.mPauseListener = pauseListener;
    }

    public void setmFinishListener(FinishListener FinishListener) {
        this.mFinishListener = FinishListener;
    }

    public void setmSendErrorListener(sendErrorListener l) {
        this.mSendErrorListener = l;
    }

    /**
     * 三十秒如果没有看到视频，就认为超时
     */
    private void setStartTimeOut() {
        if (mStartTimeOut == null) {
            mStartTimeOut = new IJKPostHandler(getContext(), this);
        }
        mStartTimeOut.postDelayed(30000);
    }

    /**
     * 停止计时器
     */
    private void stopStartTimeOut() {
        if (mStartTimeOut != null) {
            mStartTimeOut.stop();
        }
    }

    private String getOneDnsString(String[] addArgs) throws DnsException {
        if (addArgs == null)
            return "";
        if (addArgs.length < 2) {
            throw new DnsException("Dns must have a domain and an IP");
        }

        String dnsString = "";
        for (int i = 0; i < addArgs.length; i++) {
            dnsString += addArgs[i] + ",";
        }
        dnsString = dnsString.substring(0, dnsString.length() - 1);
        Logger.i("DNS param=" + dnsString);
        return dnsString;
    }

    private String getDnsString() {
        String finalArg = "";
        if (mPlayerBaseInfo != null) {
            List<String[]> argsList = mPlayerBaseInfo.getDnsArgs();
            if (argsList != null && argsList.size() > 0) {
                for (int i = 0; i < argsList.size(); i++) {
                    String[] tempStrting = argsList.get(i);
                    try {
                        finalArg += getOneDnsString(tempStrting) + ";";
                    } catch (DnsException e) {
                        e.printStackTrace();
                    }
                    for (int j = 0; j < tempStrting.length; j++)
                        Logger.d("zy string  i=" + i + "j=" + j + tempStrting[j]);
                }
            }
            if (finalArg.length() > 0)
                finalArg = finalArg.substring(0, finalArg.length() - 1);
            if (finalArg.length() > 1000)
                finalArg = finalArg.substring(0, 1000);
        }
        return finalArg;
    }

    private boolean needStartSeek() {
        if (mPlayerOptions != null) {
            if (mPlayerOptions.needStartSeek())
                return true;
        }
        return false;
    }

    private boolean shouldPauseAtStart() {
        if (mPlayerOptions != null) {
            if (mPlayerOptions.isStartPause())
                return true;
        }
        return false;
    }

    private boolean hasDefaultDurationValue() {
        if (mPlayerOptions != null) {
            if (mPlayerOptions.hasDefaultDurationAndCurrentDuration())
                return true;
        }
        return false;
    }

    private void clearDefaultDurationIfPossible() {
        if (hasDefaultDurationValue()) {
            mPlayerOptions.clearDefaultDurationAndCurrentDuration();
        }
    }

    private void playbackStartProcess() {
        Logger.d("zy playbackStartProcess started");
        if (!mIsStarted) {
            Logger.d("zy playbackStartProcess started !!!!!");
            if (shouldPauseAtStart()) {
                playerCallPause();
            }
            mIsLoading = false;
            stopStartTimeOut();

            mIsStarted = true;
        }
    }


    private void playerCallPause() {
        mPauseListener.doPause();
        mMediaPlayer.imageRefresh();
    }

    public interface LoadingIconListener {
        void loadingStart();//开始loading

        void loadingFinish();//结束loading

        void videoRendering();//有画面视频

        void videoPlaying();//视频成功播放
    }

    public interface PauseListener {
        void doPause();

        void doResume();
    }

    public interface FinishListener {
        void show();
    }

    public interface sendErrorListener {
        void sendError(final int errorType, int errorcode);
    }

    private static class IJKPostHandler extends PostHandler<IjkVideoView> {

        public IJKPostHandler(Context context, IjkVideoView ijkVideoView) {
            super(context, ijkVideoView);
        }

        @Override
        public void execute(IjkVideoView ijkVideoView) {
            ijkVideoView.mSendErrorListener.sendError(MSG_LOAD_TIMEOUT, -101);
        }
    }
}
