package com.menglong.videoplayer.model;

import java.util.List;

/**
 * Created by guorq on 2017/12/6.
 */

public class PlayFlowLogInfo {
    public static class PlayFlowTcpConnectInfo{
        Long tcp_connect_begin;
        Long tcp_connect_finish;
        String remote_ip;
        Long error_code;
    }
    String file;
    Long file_size;
    Long dns_use_pre;
    Long download_begin;
    Long dns_begin;
    Long dns_finish;
    Long tcp_connect_begin;
    Long tcp_connect_finish;
    Long http_response;
    Long download_finish;
    Long error_code;
    Long http_response_code;
    List<PlayFlowTcpConnectInfo> tcp_connect_logs;
}
