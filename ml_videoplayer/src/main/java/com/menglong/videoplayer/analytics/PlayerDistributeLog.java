package com.menglong.videoplayer.analytics;

import android.content.Context;
import android.text.TextUtils;

import com.menglong.videoplayer.model.PlayFlowLogInfo;
import com.menglong.videoplayer.model.TcpRWTimeoutLogInfo;
import com.star.util.Logger;
import com.star.util.analytics.DataAnalysisUtil;
import com.star.util.json.JSONUtil;
import com.star.util.monitor.NETSpeedTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import tv.danmaku.ijk.media.player.CacheInfo;

/**
 * Created by guorq on 2017/11/20.
 */


public class PlayerDistributeLog {

    public static final int PLAY_LOG_START_POINT = 0;
    public static final int PLAY_LOG_EXIT_POINT = 7;
    public static final int PLAY_LOG_SEEK_POINT = 5;
    public static final int PLAY_LOG_PAUSE_POINT = 6;
    public static final int PLAY_LOG_LOAD_POINT = 9;
    public static final int PLAY_LOG_PLAY_POINT = 10;

    public static final int PLAY_LOG_FIRST_LOADING = 1;
    public static final int PLAY_LOG_SEEK_LOADING = 2;
    public static final int PLAY_LOG_LOADING = 3;
    public static final int PLAY_LOG_PLAYING = 4;
    public static final int PLAY_LOG_PAUSING = 8;

    public static final int PLAY_LOG_LANDSCAPE = 11;
    public static final int PLAY_LOG_PORTRAIT = 12;

    private Context mContext;
    private NETSpeedTest mNet;
    private long epoch = System.currentTimeMillis() / 1000;
    private boolean canPutData = true;
    private HashMap<String, Object> commonMessageMap = new HashMap<String, Object>();
    private List<HashMap<String, Object>> logMap = new ArrayList<HashMap<String, Object>>();
    private List<HashMap<String, Object>> playCacheLogMap = new ArrayList<HashMap<String, Object>>();
    private boolean postStartTimeMain = true;
    private boolean postStartTimeTcp = true;
    private HashMap<String, Object> startTimeMap = new HashMap<String, Object>();
    private boolean postStartErrorMain = true;
    private HashMap<String, Object> startErrorMap = new HashMap<String, Object>();
    private ArrayList<TcpRWTimeoutLogInfo> tcpRWTimeoutArray = new ArrayList<TcpRWTimeoutLogInfo>();
    private ArrayList<PlayFlowLogInfo> mPlayFlowLogArray = new ArrayList<PlayFlowLogInfo>();
    private int currentLogStatus = PLAY_LOG_START_POINT;
    private int currentPlayCacheLogStatus = PLAY_LOG_START_POINT;
    private long currentLogTime = System.currentTimeMillis();
    private boolean userExit = true;
    private boolean netReachable = true;
    private int loadRate = 0;
    private PlayerBaseInfo playerBaseInfo;
    private boolean bPlayLog = false;

    private  int mPacketEndStatus=0; //2==结束包，1==播放结束 0==非结束包
    private String mEventID="";
    private  static String mSessionID="";
    private  static String mPageID="";
    private  static String mPlayID="";
    private int mDTLogIndex=0;
    private Object mCommonlock = new Object();
    private Object mLoglock = new Object();
    private Object mPlayCacheLoglock = new Object();
    private Object mStartTimeLock = new Object();
    private Object mStartErrorColock = new Object();
    private Object mDTLogIndexLock=new Object();
    public enum Distribute_Log_Type{
        COMMON("common",0),LOG("log",1),START_TIME_LOG("start_time_log",2),START_ERROR_LOG("start_error_log",3),
        PLAY_FLOW_LOG("play_flow_log",4),PLAY_CACHE_LOG("play_cache_log",5),PLAY_STATISTIC_LOG("play_statistic_log",6),PLAY_TCPTIMEOUT_LOG("play_tcp_rwtimeout_log",7),
        ALL_TYPE_LOG("all_type_log",8);
        private String name;
        private int index;
        private Distribute_Log_Type(String name, int index) {
            this.name = name;
            this.index = index;
        }
        @Override
        public String toString() {
            return this.name;
        }
    };

    public  static  void setmSessionID(String sessionID){
        mSessionID=sessionID;
    }

    public static void setmPageID(String pageID){
        mPageID=pageID;
    }

    public static void setmPlayID(String playID){
        mPlayID=playID;
    }

    public PlayerDistributeLog(Context context) {
        this.mContext = context.getApplicationContext();
        mNet = new NETSpeedTest(mContext);
        mDTLogIndex=0;
    }

    public void addCommon(String mame, int data) {
        synchronized (mCommonlock) {
            commonMessageMap.put(mame, data);
        }
    }

    public void addCommon(String mame, String data) {
        synchronized (mCommonlock) {
            commonMessageMap.put(mame, data);
        }
    }

    public void addCommon(String mame, long data) {
        synchronized (mCommonlock) {
            commonMessageMap.put(mame, data);
        }
    }

    public void addStartLog(String name, long data) {
        synchronized (mStartTimeLock) {
            startTimeMap.put(name, data);
        }
    }

    public void addStartErrorLog(String name, long data){
        synchronized (mStartErrorColock){
            startErrorMap.put(name,data);
        }
    }

    public void addLogMap(HashMap<String, Object> data){
        synchronized (mLoglock){
            logMap.add(data);
        }
    }

    public void addPlayCacheLogMap(HashMap<String, Object> data){
        synchronized (mPlayCacheLoglock){
            playCacheLogMap.add(data);
        }
    }

    public void clearCommonDT(){
        commonMessageMap.clear();
    }

    public void clearStartTimeLogDT(){
        startTimeMap.clear();
    }

    public void clearStartErrorLogDT(){
        startErrorMap.clear();
    }

    public void clearLogMapDT(){
        logMap.clear();
    }

    public void clearPlayCacheLogMapDT(){
        playCacheLogMap.clear();
    }


    public int getDTLogNewIndex(){
        synchronized (mDTLogIndexLock) {
            return mDTLogIndex++;
        }
    }

    public void postBasicInfo(PlayerBaseInfo baseInfo, String videoURI, boolean isLive, String eventID) {
        if (baseInfo == null) {
            return;
        }
        mEventID=eventID;
        this.playerBaseInfo = baseInfo;
        String sessionID = getSessionID();
        if (sessionID == null) {
            return;
        }
        if (!canPutData)
            return;
        addCommon("uri", videoURI);
        addCommon("eventID", eventID);
        addCommon("sessionID", getSessionID());
        addCommon("equipID", getEquipID());
        addCommon("network", getNetwork());

        String ipAreaName = TextUtils.isEmpty(playerBaseInfo.getIpAreaName()) ? "UNKNOWN" : playerBaseInfo.getIpAreaName();
        addCommon("ipAreaName", ipAreaName);
        String areaName = TextUtils.isEmpty(playerBaseInfo.getAreaName()) ? "UNKNOWN" : playerBaseInfo.getAreaName();
        addCommon("area", areaName);
        addCommon("isLive", isLive ? 1 : 0);
        Long userID = playerBaseInfo.getUserId();
        addCommon("user_id", userID==null?-1:userID);
        addCommon("operator", getOperator());
        addCommon("version", playerBaseInfo.getAppVersion());
        if (videoURI == null)
            return;
        if (videoURI.length() >= 4) {
            String str = videoURI.substring(0, 4);
            if (str.equals("http")) {
                addCommon("usePreM3U8", 0);
            } else {
                addCommon("usePreM3U8", 1);
            }
        }
    }

    /**
     * 一次播放
     *
     * @return
     */
    String getSessionID() {
        if (playerBaseInfo != null && playerBaseInfo.getUserId() != null) {
            return playerBaseInfo.getUserId() +/*"-"+StarApplication.getUser().getUserName()+"-"+*/"" + epoch;
        } else {
            return "NO";
        }
    }

    /**
     * 一次上传记录
     *
     * @return
     */
   // String getEventID() {
   //    return playerBaseInfo == null ? "" : playerBaseInfo.getToken() + System.currentTimeMillis();
   //  }

    String getEquipID() {
//		String id = SharedPreferencesUtil.getDeviceId(mPlayer);
//		if(id == null) return SharedPreferencesUtil.getUserName(mPlayer);
        return playerBaseInfo == null ? "" : playerBaseInfo.getDeviceId();
    }

    Integer getNetwork() {
        return (Integer) mNet.networkTypePlayer(mContext)[0];
    }

    String getOperator() {
        Object oName = mNet.networkTypePlayer(mContext)[1];
        if (oName == null || oName.toString().isEmpty()) {
            return "UNKNOWN";
        } else {
            return oName.toString();
        }
    }
//
//    //start logs
//    public boolean shouldSendStartLog() {
//        return sendStartErrorLog || sendStartTimeLog;
//    }
//
//    public boolean setStartLogConfig() {
//        if (playerBaseInfo != null) {
//            Random random = new Random();
//
//            sendStartTimeLog = (random.nextInt(100) < playerBaseInfo.getStartTimeLogRate()) ? true : false;
//        }
//        clearStartLogs();
//        return shouldSendStartLog();
//    }

    protected void processStartTimeLog(String rawStr, String[] returnKey) {
        String[] elems = rawStr.split(" = ");
        if (elems.length < 2)
            return;
        String key = elems[0];
        Long timestamp = Long.parseLong(elems[1]);
        startTimeMap.put(key, timestamp);
        returnKey[0]=key;
        if (timestamp > 0) {
            int curLoadRate = 0;
            Random random = new Random();
            int offset = random.nextInt(10);
            if (key.equals("start")) {
                curLoadRate = 0;
            } else if (key.equals("m3u8_DNS_finish")) {
                curLoadRate = 2;
            } else if (key.equals("m3u8_tcp_connect_finish")) {
                curLoadRate = 20 + offset;
            } else if (key.equals("ts_DNS_finish")) {
                curLoadRate = 40 + offset;
            } else if (key.equals("ts_tcp_connect_finish")) {
                curLoadRate = 50 + offset;
            } else if (key.equals("ts_http_response")) {
                curLoadRate = 70 + offset;
            } else if (key.equals("download_ts_data")) {
                curLoadRate = 80 + offset;
            } else if (key.equals("video_decoder1_finish") || key.equals("audio_decoder1_finish")) {
                curLoadRate = 90 + offset;
            } else if (key.equals("show")) {
                curLoadRate = 100;
            }
            if (curLoadRate > loadRate) {
                loadRate = curLoadRate;
                Logger.d("player start load rate=" + loadRate + "%");
            }
        }
    }

    protected void processPlayCacheLog(String rawStr, String[] returnKey) {
        //Logger.d(rawStr);
        HashMap<String, Object> playCacheLogItemMap = new HashMap<String, Object>();
        String[] rawElems = rawStr.split(" # ");
        for(int i = 0; i < rawElems.length; i++) {
            String[] elems = rawElems[i].split(" = ");
            if (elems.length < 2)
                return;
            String key = elems[0];
            Long lValue = Long.parseLong(elems[1]);
            playCacheLogItemMap.put(key, lValue);
        }

        if( playCacheLogItemMap.size() > 4 )
        {
            processLastPlayCacheLog(playCacheLogItemMap);
        }
    }

    private void processStartErrorLog(String rawStr, String[] returnKey) {
        String[] elems = rawStr.split(" = ");
        if (elems.length < 2)
            return;
        String key = elems[0];
        returnKey[0]=key;
        Long code = Long.parseLong(elems[1]);
        addStartErrorLog(key, code);
    }

    protected void findStartTimeLog(String rawStr, String[] returnKey) {
        String[] elems = rawStr.split(":");
        if (elems.length < 4)
            return;
        int logLayer;
        try {
            logLayer = Integer.parseInt(elems[1]);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e("player analytic parseInt failed1!");
            return;
        }
        switch (logLayer) {
            case 0:
                if (!postStartTimeMain)
                    return;
                break;
            case 1:
                if (!postStartTimeTcp)
                    return;
                break;
            default:
                return;
        }
        processStartTimeLog(elems[3],returnKey);
    }

    protected void findStartPlayCacheLog(String rawStr, String[] returnKey) {
        if( !bPlayLog )
            return;

        String[] elems = rawStr.split(":");

        if (elems.length < 4)
            return;
        int logLayer;
        try {
            logLayer = Integer.parseInt(elems[1]);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e("player distribute parseInt failed1!");
            return;
        }

        returnKey[0]=elems[0];
        processPlayCacheLog(elems[3],returnKey);
    }

    protected void findPlayFlowLog(String rawStr, String[] returnKey) {
        if( !bPlayLog )
            return;

        String[] elems = rawStr.split("=");

        if (elems.length < 2)
            return;

        try {
            PlayFlowLogInfo log = JSONUtil.getFromJSON("{" + elems[1] + "}", PlayFlowLogInfo.class);
            mPlayFlowLogArray.add(log);
            returnKey[0] = elems[0];
            //Logger.d("CountlyIF: " + returnKey[0] + "   " + mPlayFlowLogArray);
        }
        catch (Exception e) {
            e.printStackTrace();
            Logger.e("findPlayFlowLog: catch error");
            return;
        }
    }


    protected void findStartErrorLog(String rawStr, String[] returnKey) {
        String[] elems = rawStr.split(":");

        if (elems.length < 4)
            return;
        int logLayer;
        try {
            logLayer = Integer.parseInt(elems[1]);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e("player analytic parseInt failed1!");
            return;
        }
        switch (logLayer) {
            case 0:
                if (!postStartErrorMain)
                    return;
                break;
            default:
                return;
        }
        processStartErrorLog(elems[3],returnKey);
    }


    protected void clearStartLogs() {
        loadRate = 0;
        clearStartTimeLogDT();
        clearStartErrorLogDT();
    }

    public void refreshStartLog(String rawLog, String[] returnKey) {
        try {
            int index = rawLog.indexOf("STAR_START_TIME_LOG:");
            if (index >= 0) {
                findStartTimeLog(rawLog.substring(index),returnKey);
            }
            index = rawLog.indexOf("STAR_START_ERROR_LOG:");
            if (index >= 0) {
                findStartErrorLog(rawLog.substring(index),returnKey);
            }
            index = rawLog.indexOf("STAR_PLAY_CACHE_LOG:");
            if (index >= 0) {
                findStartPlayCacheLog(rawLog.substring(index),returnKey);
            }
            index = rawLog.indexOf("PLAY_FLOW_LOG");
            if (index >= 0) {
                findPlayFlowLog(rawLog, returnKey);
            }

            index = rawLog.indexOf("STAR_TCP_RWTIMEOUT_LOG");
            if (index >= 0) {
                findTcpRWTimeoutLog(rawLog.substring(index));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void findTcpRWTimeoutLog(String rawStr) {
        Logger.d("recv tcp rwtimeout log: "+rawStr );

        if( !bPlayLog )
            return;

        try
        {
            String[] elems = rawStr.split(":");
            if (elems.length < 4)
                return;
            int index = -1;
            int count = 0;
            do{
                index = rawStr.indexOf(":");
                if ( index!=-1 ){
                    rawStr = rawStr.substring(index+1);
                    count++;
                }
                if (count>=2)
                    break;
            }while(index!=-1);
            TcpRWTimeoutLogInfo log = JSONUtil.getFromJSON("{"+rawStr+"}",TcpRWTimeoutLogInfo.class);
            tcpRWTimeoutArray.add(log);
            distributePlayerLog(Distribute_Log_Type.PLAY_TCPTIMEOUT_LOG);
        }
        catch(Exception ex){
            Logger.d("parse tcp rwtimeout log exception: " +rawStr );
        }
    }

    public void processLastLog(int messageType, long currentPos, long targetPos) {
        if (messageType == currentLogStatus)
            return;
        HashMap<String, Object> zyTempMap = new HashMap<String, Object>();
        zyTempMap.put("duration", (System.currentTimeMillis() - currentLogTime));
        zyTempMap.put("video_pos", currentPos);
        zyTempMap.put("timestamp", System.currentTimeMillis());
        switch (currentLogStatus) {
            case PLAY_LOG_START_POINT:
                zyTempMap.put("type", PLAY_LOG_FIRST_LOADING);
                break;
            case PLAY_LOG_SEEK_POINT:
                zyTempMap.put("type", PLAY_LOG_SEEK_LOADING);
                break;
            case PLAY_LOG_LOAD_POINT:
                zyTempMap.put("type", PLAY_LOG_LOADING);
                break;
            case PLAY_LOG_PLAY_POINT:
                zyTempMap.put("type", PLAY_LOG_PLAYING);
                break;
            case PLAY_LOG_PAUSE_POINT:
                zyTempMap.put("type", PLAY_LOG_PAUSING);
                break;
        }
        addLogMap(zyTempMap);

    }


    public void processLastPlayCacheLog(int messageType, CacheInfo cacheInfo) {
        if (messageType == currentPlayCacheLogStatus)
            return;

        HashMap<String, Object> zyTempMap = new HashMap<String, Object>();
        zyTempMap.put("timestamp", System.currentTimeMillis());
        zyTempMap.put("video_cache_byte", cacheInfo.mVideoCachedBytes);
        zyTempMap.put("audio_cache_byte", cacheInfo.mAudioCachedBytes);
        zyTempMap.put("video_cached_duration", cacheInfo.mVideoCachedDuration);
        zyTempMap.put("audio_cached_duration", cacheInfo.mAudioCachedDuration);
        zyTempMap.put("buffer_size_max", cacheInfo.mMaxBufferSize);
        zyTempMap.put("type", messageType);
        
        addPlayCacheLogMap(zyTempMap);
    }

    public void processLastPlayCacheLog(HashMap<String, Object> zyTempMap) {
        zyTempMap.put("timestamp", System.currentTimeMillis());
        switch (currentPlayCacheLogStatus) {
            case PLAY_LOG_START_POINT:
                zyTempMap.put("type", PLAY_LOG_FIRST_LOADING);
                break;
            case PLAY_LOG_SEEK_POINT:
                zyTempMap.put("type", PLAY_LOG_SEEK_LOADING);
                break;
            case PLAY_LOG_LOAD_POINT:
                zyTempMap.put("type", PLAY_LOG_LOADING);
                break;
            case PLAY_LOG_PLAY_POINT:
                zyTempMap.put("type", PLAY_LOG_PLAYING);
                break;
            case PLAY_LOG_PAUSE_POINT:
                zyTempMap.put("type", PLAY_LOG_PAUSING);
                break;
            case PLAY_LOG_EXIT_POINT:
                zyTempMap.put("type", 7);
                break;
        }
        addPlayCacheLogMap(zyTempMap);
    }

    public int getCurrentLogStatus() {
        return currentLogStatus;
    }

    public void updatePlayerMessage(int messageType, long currentPos, long targetPos ){
        updatePlayerMessage(messageType, currentPos, targetPos, 0);
    }

    public void updatePlayerMessage(int messageType, long currentPos, long targetPos, int reason) {
        Logger.d("zy receive message type = " + messageType + ", currentPos = " + currentPos + ", targetPos = " + targetPos);
        if (messageType == currentLogStatus && messageType != PLAY_LOG_START_POINT)
            return;
        processLastLog(messageType, currentPos, targetPos);
        HashMap<String, Object> zyTempMap = new HashMap<String, Object>();
        currentLogStatus = messageType;
        currentLogTime = System.currentTimeMillis();
        switch (messageType) {
            case PLAY_LOG_START_POINT:
                zyTempMap.put("timestamp", currentLogTime);
                zyTempMap.put("type", messageType);
                break;
            case PLAY_LOG_SEEK_POINT:
                zyTempMap.put("timestamp", currentLogTime);
                zyTempMap.put("video_start", currentPos);
                zyTempMap.put("video_end", targetPos);
                zyTempMap.put("type", messageType);
                break;
            case PLAY_LOG_PAUSE_POINT:
                zyTempMap.put("timestamp", currentLogTime);
                zyTempMap.put("video_pos", currentPos);
                zyTempMap.put("type", messageType);
                zyTempMap.put("reason", reason);
                break;
            case PLAY_LOG_LOAD_POINT:
                zyTempMap.put("timestamp", currentLogTime);
                zyTempMap.put("video_pos", currentPos);
                zyTempMap.put("type", messageType);
                break;
            case PLAY_LOG_PLAY_POINT:
                zyTempMap.put("timestamp", currentLogTime);
                zyTempMap.put("video_pos", currentPos);
                zyTempMap.put("type", messageType);
                break;
            case PLAY_LOG_EXIT_POINT:
                zyTempMap.put("timestamp", currentLogTime);
                zyTempMap.put("type", 7);
                zyTempMap.put("reason", reason);
                break;
        }
        addLogMap(zyTempMap);
    }

    public void updatePlayerCacheMessage(int messageType, CacheInfo cacheInfo ){
        Logger.d("player cache message receive message type = " + messageType);
        if (messageType == currentPlayCacheLogStatus && messageType != PLAY_LOG_START_POINT)
            return;

        processLastPlayCacheLog(messageType, cacheInfo);
        currentPlayCacheLogStatus = messageType;
    }

    public void updatePlayerCacheMessage(int messageType ){
        Logger.d(" player cache message receive message type = " + messageType);
        if (messageType == currentPlayCacheLogStatus && messageType != PLAY_LOG_START_POINT)
            return;

        currentPlayCacheLogStatus = messageType;
    }

    protected void putFinalData() {
        this.addCommon("userExit", userExit == true ? 1 : 0);
        this.addCommon("errorNetwork", netReachable == true ? 0 : 1);
    }

    public void setTotalDuration(long videoTotalTime) {
        addCommon("video_totaltime", videoTotalTime);
    }

    public void sendfinishPlayerDelay() {
        try {
            putFinalData();
            canPutData = false;
        } catch (Exception e) {
            canPutData = false;
            Logger.d("make json play log exception=" + e.toString());
        }
    }

    private   void getDTLogHeader(HashMap<String, Object> logHeader, int logIndex){
        logHeader.put("play_packet_id",mEventID);
        logHeader.put("play_packet_index",logIndex);
        logHeader.put("play_packet_end",mPacketEndStatus);
        logHeader.put("timestap", System.currentTimeMillis());
    }

    public void distributePlayerLog(Distribute_Log_Type logType) {

        try {
            String playLog="";
            int logIndex = 0;
            HashMap<String, Object> totalMessage = new HashMap<String, Object>();
            HashMap<String, Object> logHeader = new HashMap<String, Object>();
            switch (logType){
                case COMMON:
                    if (commonMessageMap.size() <= 0)
                        return;
                    logIndex=getDTLogNewIndex();
                    synchronized (mCommonlock) {
                        totalMessage.put(logType.toString(),commonMessageMap);
                        getDTLogHeader(logHeader,logIndex);
                        totalMessage.put("play_packet_header",logHeader);
                        playLog= JSONUtil.getJSON(totalMessage);
                        clearCommonDT();
                    }
                    break;
                case START_TIME_LOG:
                    if (!bPlayLog){
                        return;
                    }

                    if (startTimeMap.size() <= 0)
                        return;
                    logIndex=getDTLogNewIndex();
                    synchronized (mStartTimeLock) {
                        totalMessage.put(logType.toString(),startTimeMap);
                        getDTLogHeader(logHeader,logIndex);
                        totalMessage.put("play_packet_header",logHeader);
                        playLog= JSONUtil.getJSON(totalMessage);
                        clearStartTimeLogDT();
                    }
                    break;
                case START_ERROR_LOG:
                    if (startErrorMap.size() <= 0)
                        return;

                    logIndex=getDTLogNewIndex();
                    synchronized (mStartErrorColock) {
                        totalMessage.put(logType.toString(),startErrorMap);
                        getDTLogHeader(logHeader,logIndex);
                        totalMessage.put("play_packet_header",logHeader);
                        playLog= JSONUtil.getJSON(totalMessage);
                        clearStartErrorLogDT();
                    }
                    break;
                case LOG:
                    if (logMap.size() <= 0)
                        return;

                    logIndex=getDTLogNewIndex();
                    synchronized (mLoglock) {
                        totalMessage.put(logType.toString(),logMap);
                        getDTLogHeader(logHeader,logIndex);
                        totalMessage.put("play_packet_header",logHeader);
                        playLog= JSONUtil.getJSON(totalMessage);
                        clearLogMapDT();
                    }
                    break;
                case PLAY_CACHE_LOG:
                    if (playCacheLogMap.size() <= 0)
                        return;

                    logIndex=getDTLogNewIndex();
                    synchronized (mPlayCacheLoglock) {
                        totalMessage.put(logType.toString(),playCacheLogMap);
                        getDTLogHeader(logHeader,logIndex);
                        totalMessage.put("play_packet_header",logHeader);
                        playLog= JSONUtil.getJSON(totalMessage);
                        //Logger.d(playLog);
                        clearPlayCacheLogMapDT();
                    }
                    break;

                case PLAY_TCPTIMEOUT_LOG:
                    if (tcpRWTimeoutArray.size() <= 0)
                        return;
                    logIndex=getDTLogNewIndex();
                    totalMessage.put(logType.toString(),tcpRWTimeoutArray);
                    getDTLogHeader(logHeader,logIndex);
                    totalMessage.put("play_packet_header",logHeader);
                    playLog= JSONUtil.getJSON(totalMessage);
                    tcpRWTimeoutArray.clear();
                    //HashMap<String, ArrayList<TcpRWTimeoutLogInfo>> tmp = new HashMap<String, ArrayList<TcpRWTimeoutLogInfo>>();
                    //tmp.put(Distribute_Log_Type.PLAY_TCPTIMEOUT_LOG.name(), tcpRWTimeoutArray);
                    //playLog = JSONUtil.getJSON(tmp);
                    //tcpRWTimeoutArray.clear();
                    //ThreadPoolManager.getInstance().addTask(new TaskDistributePlayLog(postUrl, playLog, mContext,logType,getDTLogNewIndex()));
                    break;
                case PLAY_FLOW_LOG:
                    if (mPlayFlowLogArray.size() <= 0)
                        return;

                    logIndex=getDTLogNewIndex();
                    totalMessage.put(logType.toString(),mPlayFlowLogArray);
                    getDTLogHeader(logHeader,logIndex);
                    totalMessage.put("play_packet_header",logHeader);
                    playLog= JSONUtil.getJSON(totalMessage);
                    mPlayFlowLogArray.clear();
                    break;
				case ALL_TYPE_LOG:  //player exit can call ALL_TYPE_LOG
                    if (commonMessageMap.size() > 0){
                        totalMessage.put(Distribute_Log_Type.COMMON.toString(),commonMessageMap);
                    }

                    if (bPlayLog && startTimeMap.size() > 0){
                        totalMessage.put(Distribute_Log_Type.START_TIME_LOG.toString(),startTimeMap);
                    }

                    if (startErrorMap.size() > 0){
                        totalMessage.put(Distribute_Log_Type.START_ERROR_LOG.toString(),startErrorMap);
                    }

                    if (logMap.size() > 0){
                        totalMessage.put(Distribute_Log_Type.LOG.toString(),logMap);
                    }

                    if( playCacheLogMap.size() > 0 ){
                        totalMessage.put(Distribute_Log_Type.PLAY_CACHE_LOG.toString(),playCacheLogMap);
                    }

                    logIndex=getDTLogNewIndex();
                    getDTLogHeader(logHeader,logIndex);
                    totalMessage.put("play_packet_header",logHeader);
                    playLog= JSONUtil.getJSON(totalMessage);
                    clearCommonDT();
                    clearStartTimeLogDT();
                    clearLogMapDT();
                    clearStartErrorLogDT();
                    clearPlayCacheLogMapDT();
                    break;

                default:
                    return;
            }

            DataAnalysisUtil.sendPlayLogEventCountly(logType.toString(),mSessionID,mPageID,mPlayID,mEventID,playLog,logIndex);
            //ThreadPoolManager.getInstance().addTask(new TaskDistributePlayLog(playLog, mContext,logType,logIndex));
        } catch (Exception e) {
            Logger.d("CountlyGO distributePlayerLog exception=" + e.toString());
        }
    }


    //record netwokrstate when play error
    public void setNetworkState(boolean netReachable) {
        this.netReachable = netReachable;
    }

    public void setUserExit(boolean userExit) {
        this.userExit = userExit;
    }

    public int getLoadProgress() {
        return loadRate;
    }

    public void setPacketEndStatus(int status){
        this.mPacketEndStatus=status;
    }

    //async distribute play log
    public class TaskDistributePlayLog implements Runnable {
        private String playLog;
        private Context context;
        private Distribute_Log_Type logType;
        private int dtIndex;

        public TaskDistributePlayLog(String log, Context ctx, Distribute_Log_Type logType, int dtIndex) {
            this.playLog = log;
            this.context = ctx;
            this.logType = logType;
            this.dtIndex = dtIndex;
        }

        @Override
        public void run() {
            //Logger.d("CountlyGO run " +" dtIndex: " + dtIndex + " playLog: " + playLog + " logType: " + logType.toString());
            DataAnalysisUtil.sendPlayLogEventCountly(logType.toString(),mSessionID,mPageID,mPlayID,mEventID,playLog,dtIndex);
        }
    }

    public void updatePlayerViewMessage(int messageType, long currentPos) {
        if (messageType == currentLogStatus)
            return;
        HashMap<String, Object> mapData = new HashMap<String, Object>();
        currentLogTime = System.currentTimeMillis();
        switch (messageType) {
            case PLAY_LOG_LANDSCAPE:
            case PLAY_LOG_PORTRAIT:
                mapData.put("timestamp", currentLogTime);
                mapData.put("video_pos", currentPos);
                mapData.put("type", messageType);
                break;
            default:
                Logger.d("invalid player view message, "+messageType);
                break;
        }
        if (!mapData.isEmpty())
            addLogMap(mapData);
    }

    public void initPlayerLog(boolean bPlayerLog) {
       this.bPlayLog = bPlayerLog;
    }
}

