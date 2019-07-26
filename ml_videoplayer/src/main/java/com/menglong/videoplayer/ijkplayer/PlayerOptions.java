package com.menglong.videoplayer.ijkplayer;

import java.util.UUID;

/**
 * Created by zy on 16-12-26.
 */
public class PlayerOptions {
    final int DEFAULT_NUM = -1;

    private int mStartSeekPosition = DEFAULT_NUM; //视频启动时要seek到的位置
    private int mDefaultCurrentPosition = DEFAULT_NUM;  ////视频启动时要seek到的位置
    private int mDefaultDuration = DEFAULT_NUM;
    private int mPlayIndex = 0;

    private boolean mStartPause = false;

    private String mStream = "Default";

    private String mPlayID = " ";
    private String mEventID = " ";

    private String mCookies = null;

    private int mAutoPlay = 1;

    private String mAudioLanguage="";

    public PlayerOptions() {
        mStartSeekPosition = DEFAULT_NUM;
        mDefaultCurrentPosition = DEFAULT_NUM;
        mDefaultDuration = DEFAULT_NUM;
        mStartPause = false;
        mStream = "Default";
        UUID uuid = UUID.randomUUID();
        mPlayID = uuid.toString();
        mPlayIndex = 0;
    }

    public int getStartSeekPosition() {
        return mStartSeekPosition;
    }

    public void setStartSeekPosition(int startSeekPosition) {
        this.mStartSeekPosition = startSeekPosition;
    }

    public boolean needStartSeek() {
        return mStartSeekPosition != DEFAULT_NUM;
    }

    public int getDefaultCurrentPosition() {
        return mDefaultCurrentPosition;
    }

    public void setDefaultCurrentPosition(int defaultCurrentPosition) {
        this.mDefaultCurrentPosition = defaultCurrentPosition;
    }

    public int getDefaultDuration() {
        return mDefaultDuration;
    }

    public void setDefaultDuration(int defaultDuration) {
        this.mDefaultDuration = defaultDuration;
    }

    public boolean hasDefaultDurationAndCurrentDuration() {
        return mDefaultDuration != DEFAULT_NUM && mDefaultCurrentPosition != DEFAULT_NUM;
    }

    public void clearStartSeekPosition() {
        mStartSeekPosition = DEFAULT_NUM;
    }

    public void clearDefaultDurationAndCurrentDuration() {
        mDefaultDuration = DEFAULT_NUM;
        mDefaultCurrentPosition = DEFAULT_NUM;
    }

    public boolean isStartPause() {
        return mStartPause;
    }

    public void setStartPause(boolean mStartPause) {
        this.mStartPause = mStartPause;
    }

    public String getStream() {
        return mStream;
    }

    public void setStream(String mStream) {
        this.mStream = mStream;
    }

    public String getPlayID() {
        return mPlayID;
    }

    public void setPlayID(String mUUID) {
        this.mPlayID = mUUID;
    }

    public int getPlayIndex() {
        return mPlayIndex;
    }

    public void setPlayIndex(int mPlayIndex) {
        this.mPlayIndex = mPlayIndex;
    }

    public String getCookies() {
        return mCookies;
    }

    public void setCookies(String cookies) {
        this.mCookies = cookies;
    }

    public int getAutoPlay() {
        return mAutoPlay;
    }

    public void setAutoPlay( int autoPlay) {
        this.mAutoPlay = autoPlay;
    }

    public String getEventID() { return mEventID; }

    public void setEventID(String eventID) { this.mEventID = eventID; }

    public String getAudioLanguage() {
        return mAudioLanguage;
    }

    public void setAudioLanguage(String audioLanguage) {
        this.mAudioLanguage = audioLanguage;
    }
}
