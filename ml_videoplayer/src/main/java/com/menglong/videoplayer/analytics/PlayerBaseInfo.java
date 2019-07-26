package com.menglong.videoplayer.analytics;

import java.io.Serializable;
import java.util.List;

/**
 * Created by dujr on 2017/8/9.
 */

public class PlayerBaseInfo implements Serializable {

    private String appVersion;

    private Long userId;

    private String ipAreaName;

    private String areaName;

    private String token;

    private String deviceId;

    private int tcpRWTimeout;

    private int tcpRWTimeoutRate;

    private int playerBufferLogTime;
    private int playerLogRate;
    private int playerBufferMaxSize;
    private int bufferMaxSizeRate;
    private int startTimeLogRate;
    private int liveStartIndex;

    private List<String[]> dnsArgs;

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getIpAreaName() {
        return ipAreaName;
    }

    public void setIpAreaName(String ipAreaName) {
        this.ipAreaName = ipAreaName;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getStartTimeLogRate() {
        return startTimeLogRate;
    }

    public void setStartTimeLogRate(int startTimeLogRate) {
        this.startTimeLogRate = startTimeLogRate;
    }

    public List<String[]> getDnsArgs() {
        return dnsArgs;
    }

    public void setDnsArgs(List<String[]> dnsArgs) {
        this.dnsArgs = dnsArgs;
    }

    public void setTcpRWTimeout(int tcpRWTimeout) {
        this.tcpRWTimeout = tcpRWTimeout;
    }
    public int getTcpRWTimeout() {
        return tcpRWTimeout;
    }
    public int getTcpRWTimeoutRate() {
        return tcpRWTimeoutRate;
    }
    public void setTcpRWTimeoutRate(int tcpRWTimeoutRate) {
        this.tcpRWTimeoutRate = tcpRWTimeoutRate;
    }

    public void setPlayerBufferLogTime(int playerBufferLogTime) {
        this.playerBufferLogTime = playerBufferLogTime;
    }
    public int getPlayerBufferLogTime() {
        return playerBufferLogTime;
    }
    public int getPlayerLogRate() {
        return playerLogRate;
    }
    public void setPlayerLogRate(int playerLogRate) {
        this.playerLogRate = playerLogRate;
    }

    public void setPlayerBufferMaxSize(int playerBufferMaxSize) {
        this.playerBufferMaxSize = playerBufferMaxSize;
    }
    public int getPlayerBufferMaxSize() {
        return playerBufferMaxSize;
    }
    public int getBufferMaxSizeRate() {
        return bufferMaxSizeRate;
    }
    public void setBufferMaxSizeRate(int bufferMaxSizeRate) {
        this.bufferMaxSizeRate = bufferMaxSizeRate;
    }

    public int getLiveStartIndex() { return liveStartIndex; }
    public void setLiveStartIndex(int liveStartIndex) {this.liveStartIndex = liveStartIndex;}
}
