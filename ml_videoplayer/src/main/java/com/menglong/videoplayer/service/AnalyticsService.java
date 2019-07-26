package com.menglong.videoplayer.service;

import android.content.Context;

import com.star.util.service.AbstractService;

/**
 * Created by dujr on 2017/8/9.
 */

public class AnalyticsService extends AbstractService {

    public AnalyticsService(Context context) {
        super(context);
    }

    @Override
    protected boolean needInterceptResponse() {
        return false;
    }

    @Override
    protected boolean needInterceptFailedResponse(int errorCode, String msg, String loadUrl) {
        return false;
    }

    @Override
    protected boolean needInterceptRequest(String url) {
        return false;
    }

    public void sendfinishPlayerDelay(String url, String playlog) {
        doPostBody(url, String.class, playlog, null);
    }
}
