package com.menglong.http;

import android.content.Intent;

import com.menglong.common.LogUtil;
import com.menglong.json.MlJson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by User on 2017/3/24.
 * 对OKHttp请求的封装
 */
public class StarHttpsUtil {
    private static OkHttpClient okHttpClient;
    private static long mConnectTimeout = 20;
    private static InputStream[] mCertificates;

    /**
     * 获取oKHttpClient
     * interceptor 拦截器 可在里面添加头文件信息
     * timeout  超时时间
     * certificates 证书信息 没有就传null
     *
     * @return
     */
    public static OkHttpClient getOkHttpClient(Interceptor interceptor, long timeout, InputStream[] certificates) {
        mConnectTimeout = timeout;
        mCertificates = certificates;
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addNetworkInterceptor(new NetInterceptor())
                .connectTimeout(mConnectTimeout, TimeUnit.SECONDS)
                .writeTimeout(mConnectTimeout, TimeUnit.SECONDS)
                .readTimeout(mConnectTimeout, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false);
        if (mCertificates != null) {
            builder.sslSocketFactory(getSSLSocketFactory(mCertificates));
        }
        if (interceptor != null) {
            builder.addInterceptor(interceptor);
        }
        okHttpClient = builder.build();
        return okHttpClient;
    }

    /**
     * 更新oKHttpClient
     *
     * @return
     */
    public static void setInterceptor(Interceptor interceptor) {
        okHttpClient = null;
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(mConnectTimeout, TimeUnit.SECONDS)
                .writeTimeout(mConnectTimeout, TimeUnit.SECONDS)
                .readTimeout(mConnectTimeout, TimeUnit.SECONDS);
        if (interceptor != null) {
            builder.addInterceptor(interceptor);
        }
        if (mCertificates != null) {
            builder.sslSocketFactory(getSSLSocketFactory(mCertificates));
        }
        okHttpClient = builder.build();
    }

    /**
     * 同步get请求的封装
     *
     * @param url
     * @return json
     * @throws Exception
     */
    public static String getSyncToInStream(String url) throws Exception {
        LogUtil.i("OKHttp get url: " + url);
        Request request = buildGetRequest(url);
        String result = syncExecuteToJson(request);
        return result;
    }

    //======================================================== 同步 get 请求 =================================================================================

    /**
     * 同步get请求的封装
     *
     * @param url
     * @param headers
     * @return json
     * @throws Exception
     */
    public static String getSyncToInStream(String url, Headers headers) throws Exception {
        LogUtil.i("OKHttp get url: " + url);
        Request request = buildGetRequest(url, headers);
        String result = syncExecuteToJson(request);
        return result;
    }

    /**
     * 同步get请求的封装
     *
     * @param url
     * @param headers
     * @return obj
     * @throws Exception
     */
    public static ResponseObj getSyncToResponseObj(String url, Headers headers) throws Exception {
        LogUtil.i("OKHttp get url: " + url);
        Request request = buildGetRequest(url, headers);
        ResponseObj responseObj = syncExecuteToResponseObj(request);
        return responseObj;
    }

    /**
     * 同步get请求的封装
     *
     * @param url
     * @return obj
     * @throws Exception
     */
    public static ResponseObj getSyncToResponseObj(String url) throws Exception {
        LogUtil.i("OKHttp get url: " + url);
        Request request = buildGetRequest(url);
        ResponseObj responseObj = syncExecuteToResponseObj(request);
        return responseObj;
    }

    //======================================================== 异步 get 请求 =================================================================================

    /**
     * 异步get请求的封装
     *
     * @param url
     * @return
     * @throws Exception
     */
    public static void getAsyncToInStream(String url, Callback callback) throws Exception {
        LogUtil.i("OKHttp get url: " + url);
        Request request = buildGetRequest(url);
        getOkHttpClient(null, mConnectTimeout, null)
                .newCall(request)
                .enqueue(callback);
    }

    /**
     * 异步get请求的封装
     *
     * @param url
     * @param headers
     * @return
     * @throws Exception
     */
    public static void getAsyncToInStream(String url, Callback callback, Headers headers) throws Exception {
        LogUtil.i("OKHttp get url: " + url);
        Request request = buildGetRequest(url, headers);
        getOkHttpClient(null, mConnectTimeout, null)
                .newCall(request)
                .enqueue(callback);
    }

    //======================================================== 同步 post 请求 =================================================================================

    /**
     * 同步post请求的封装（接收 object类型的参数）
     *
     * @param url   检测地址
     * @param param object类型的参数
     * @return json格式的字符串
     * @throws Exception
     */
    public static String postSyncObjectToJSON(String url, Object param) throws Exception {
        String params = "";
        if (param instanceof String) {
            params = (String) param;
        } else {
            params = MlJson.parseObjectToLightString(param);
        }
        LogUtil.i("OKHttp post url:" + url + " param: " + params);
        Request request = stringToPostRequst(url, params);
        Headers headers = request.headers();
        LogUtil.i("headers info =========================== " + headers.toString());
        String result = syncExecuteToJson(request);
        return result;
    }

    /**
     * 同步post请求的封装（接收 object类型的参数）
     *
     * @param url     检测地址
     * @param param   object类型的参数
     * @param headers
     * @return json格式的字符串
     * @throws Exception
     */
    public static String postSyncObjectToJSON(String url, Object param, Headers headers) throws Exception {
        String params = "";
        if (param instanceof String) {
            params = (String) param;
        } else {
            params = MlJson.parseObjectToLightString(param);
        }
        LogUtil.i("OKHttp post url:" + url + " param: " + params);
        Request request = stringToPostRequst(url, params, headers);
        String result = syncExecuteToJson(request);
        return result;
    }

    /**
     * 同步post请求的封装（接收 object类型的参数）
     *
     * @param url   检测地址
     * @param param object类型的参数
     * @return ResponseObj
     * @throws Exception
     */
    public static ResponseObj postSyncObjectToResponseObj(String url, Object param) throws Exception {
        String params = "";
        if (param instanceof String) {
            params = (String) param;
        } else {
            params = MlJson.parseObjectToLightString(param);
        }
        LogUtil.i("OKHttp post url:" + url + " param: " + params);
        Request request = stringToPostRequst(url, params);
        Headers headers = request.headers();
        LogUtil.i("headers info =========================== " + headers.toString());
        LogUtil.i("headers size =========================== " + request.headers().size());
        ResponseObj responseObj = syncExecuteToResponseObj(request);
        return responseObj;
    }

    /**
     * 同步post请求的封装（接收 object类型的参数）
     *
     * @param url     检测地址
     * @param param   object类型的参数
     * @param headers
     * @return ResponseObj
     * @throws Exception
     */
    public static ResponseObj postSyncObjectToResponseObj(String url, Object param, Headers headers) throws Exception {
        String params = "";
        if (param instanceof String) {
            params = (String) param;
        } else {
            params = MlJson.parseObjectToLightString(param);
        }
        LogUtil.i("OKHttp post url:" + url + " param: " + params);
        Request request = stringToPostRequst(url, params, headers);
        ResponseObj responseObj = syncExecuteToResponseObj(request);
        return responseObj;
    }

    /**
     * 同步post请求的封装（接收 Map类型的参数）
     *
     * @param url
     * @param params
     * @return json格式的字符串
     * @throws Exception
     */
    public static String postSyncMapToJSON(String url, Map<String, Object> params) throws Exception {
        String param = MlJson.parseObjectToLightString(params);
        LogUtil.i("OKHttp post url:" + url + " param: " + param);
        Request request = stringToPostRequst(url, param);
        String result = syncExecuteToJson(request);
        return result;
    }

    /**
     * 同步post请求的封装（接收 Map类型的参数）
     *
     * @param url
     * @param params
     * @param headers
     * @return json格式的字符串
     * @throws Exception
     */
    public static String postSyncMapToJSON(String url, Map<String, Object> params, Headers headers) throws Exception {
        String param = MlJson.parseObjectToLightString(params);
        LogUtil.i("OKHttp post url:" + url + " param: " + param);
        Request request = stringToPostRequst(url, param, headers);
        String result = syncExecuteToJson(request);
        return result;
    }

    //======================================================== 异步 post 请求 =================================================================================

    /**
     * 同步post请求的封装（接收 Map类型的参数）
     *
     * @param url
     * @param params
     * @return ResponseObj
     * @throws Exception
     */
    public static ResponseObj postSyncMapToResponseObj(String url, Map<String, Object> params) throws Exception {
        String param = MlJson.parseObjectToLightString(params);
        LogUtil.i("OKHttp post url:" + url + " param: " + param);
        Request request = stringToPostRequst(url, param);
        ResponseObj responseObj = syncExecuteToResponseObj(request);
        return responseObj;
    }

    /**
     * 同步post请求的封装（接收 Map类型的参数）
     *
     * @param url
     * @param params
     * @param headers
     * @return ResponseObj
     * @throws Exception
     */
    public static ResponseObj postSyncMapToResponseObj(String url, Map<String, Object> params, Headers headers) throws Exception {
        String param = MlJson.parseObjectToLightString(params);
        LogUtil.i("OKHttp post url:" + url + " param: " + param);
        Request request = stringToPostRequst(url, param, headers);
        ResponseObj responseObj = syncExecuteToResponseObj(request);
        return responseObj;
    }

    /**
     * 异步post请求的封装
     *
     * @param url
     * @param param
     * @param callback
     * @return
     * @throws Exception
     */
    public static void postAsyncObjectToJSON(String url, Object param, Callback callback) throws Exception {
        String params = "";
        if (param instanceof String) {
            params = (String) param;
        } else {
            params = MlJson.parseObjectToLightString(param);
        }
        LogUtil.i("OKHttp post url:" + url + " param: " + params);
        Request request = stringToPostRequst(url, params);
        Headers headers = request.headers();
        LogUtil.i("headers info =========================== " + headers.toString());
        getOkHttpClient(null, mConnectTimeout, null)
                .newCall(request)
                .enqueue(callback);
    }

    /**
     * 异步post请求的封装
     *
     * @param url
     * @param param
     * @param headers
     * @param callback
     * @return
     * @throws Exception
     */
    public static void postAsyncObjectToJSON(String url, Object param, Callback callback, Headers headers) throws Exception {
        String params = "";
        if (param instanceof String) {
            params = (String) param;
        } else {
            params = MlJson.parseObjectToLightString(param);
        }
        LogUtil.i("OKHttp post url:" + url + " param: " + params);
        Request request = stringToPostRequst(url, params, headers);
        getOkHttpClient(null, mConnectTimeout, null)
                .newCall(request)
                .enqueue(callback);
    }

    /**
     * 异步post请求的封装 接收Map的参数
     *
     * @param url
     * @param callback
     * @throws Exception
     */
    public static void postAsyncMapToJSON(String url, Map<String, Object> params, Callback callback) throws Exception {
        String param = MlJson.parseObjectToLightString(params);
        LogUtil.i("OKHttp post url:" + url + " param: " + param);
        Request request = stringToPostRequst(url, param);
        getOkHttpClient(null, mConnectTimeout, null)
                .newCall(request)
                .enqueue(callback);
    }

    /**
     * 异步post请求的封装 接收Map的参数
     *
     * @param url
     * @param callback
     * @param headers
     * @throws Exception
     */
    public static void postAsyncMapToJSON(String url, Map<String, Object> params, Callback callback, Headers headers) throws Exception {
        String param = MlJson.parseObjectToLightString(params);
        LogUtil.i("OKHttp post url:" + url + " param: " + param);
        Request request = stringToPostRequst(url, param, headers);
        getOkHttpClient(null, mConnectTimeout, null)
                .newCall(request)
                .enqueue(callback);
    }

    //======================================================== 同步 put 请求 =================================================================================

    /**
     * put（接收 object类型的参数）
     *
     * @param url   检测地址
     * @param param object类型的参数
     * @return json格式的字符串
     * @throws Exception
     */
    public static String putSyncObjectToJSON(String url, Object param) throws Exception {
        String params = "";
        if (param instanceof String) {
            params = (String) param;
        } else {
            params = MlJson.parseObjectToLightString(param);
        }
        LogUtil.i("OKHttp put url:" + url + " param: " + params);
        Request request = stringToPutRequst(url, params);
        Headers headers = request.headers();
        LogUtil.i("headers info =========================== " + headers.toString());
        String result = syncExecuteToJson(request);
        return result;
    }

    /**
     * 同步put请求的封装（接收 object类型的参数）
     *
     * @param url     检测地址
     * @param param   object类型的参数
     * @param headers
     * @return json格式的字符串
     * @throws Exception
     */
    public static String putSyncObjectToJSON(String url, Object param, Headers headers) throws Exception {
        String params = "";
        if (param instanceof String) {
            params = (String) param;
        } else {
            params = MlJson.parseObjectToLightString(param);
        }
        LogUtil.i("OKHttp put url:" + url + " param: " + params);
        Request request = stringToPutRequst(url, params, headers);
        String result = syncExecuteToJson(request);
        return result;
    }

    /**
     * 同步put请求的封装（接收 object类型的参数）
     *
     * @param url   检测地址
     * @param param object类型的参数
     * @return ResponseObj
     * @throws Exception
     */
    public static ResponseObj putSyncObjectToResponseObj(String url, Object param) throws Exception {
        String params = "";
        if (param instanceof String) {
            params = (String) param;
        } else {
            params = MlJson.parseObjectToLightString(param);
        }
        LogUtil.i("OKHttp put url:" + url + " param: " + params);
        Request request = stringToPutRequst(url, params);
        Headers headers = request.headers();
        LogUtil.i("headers info =========================== " + headers.toString());
        LogUtil.i("headers size =========================== " + request.headers().size());
        ResponseObj responseObj = syncExecuteToResponseObj(request);
        return responseObj;
    }

    /**
     * 同步put请求的封装（接收 object类型的参数）
     *
     * @param url     检测地址
     * @param param   object类型的参数
     * @param headers
     * @return ResponseObj
     * @throws Exception
     */
    public static ResponseObj putSyncObjectToResponseObj(String url, Object param, Headers headers) throws Exception {
        String params = "";
        if (param instanceof String) {
            params = (String) param;
        } else {
            params = MlJson.parseObjectToLightString(param);
        }
        LogUtil.i("OKHttp put url:" + url + " param: " + params);
        Request request = stringToPutRequst(url, params, headers);
        ResponseObj responseObj = syncExecuteToResponseObj(request);
        return responseObj;
    }

    /**
     * 同步put请求的封装（接收 Map类型的参数）
     *
     * @param url
     * @param params
     * @return json格式的字符串
     * @throws Exception
     */
    public static String putSyncMapToJSON(String url, Map<String, Object> params) throws Exception {
        String param = MlJson.parseObjectToLightString(params);
        LogUtil.i("OKHttp put url:" + url + " param: " + param);
        Request request = stringToPutRequst(url, param);
        String result = syncExecuteToJson(request);
        return result;
    }

    /**
     * 同步put请求的封装（接收 Map类型的参数）
     *
     * @param url
     * @param params
     * @param headers
     * @return json格式的字符串
     * @throws Exception
     */
    public static String putSyncMapToJSON(String url, Map<String, Object> params, Headers headers) throws Exception {
        String param = MlJson.parseObjectToLightString(params);
        LogUtil.i("OKHttp put url:" + url + " param: " + param);
        Request request = stringToPutRequst(url, param, headers);
        String result = syncExecuteToJson(request);
        return result;
    }

    //======================================================== 异步 put 请求 =================================================================================

    /**
     * 同步put请求的封装（接收 Map类型的参数）
     *
     * @param url
     * @param params
     * @return ResponseObj
     * @throws Exception
     */
    public static ResponseObj putSyncMapToResponseObj(String url, Map<String, Object> params) throws Exception {
        String param = MlJson.parseObjectToLightString(params);
        LogUtil.i("OKHttp put url:" + url + " param: " + param);
        Request request = stringToPutRequst(url, param);
        ResponseObj responseObj = syncExecuteToResponseObj(request);
        return responseObj;
    }

    /**
     * 同步put请求的封装（接收 Map类型的参数）
     *
     * @param url
     * @param params
     * @param headers
     * @return ResponseObj
     * @throws Exception
     */
    public static ResponseObj putSyncMapToResponseObj(String url, Map<String, Object> params, Headers headers) throws Exception {
        String param = MlJson.parseObjectToLightString(params);
        LogUtil.i("OKHttp put url:" + url + " param: " + param);
        Request request = stringToPutRequst(url, param, headers);
        ResponseObj responseObj = syncExecuteToResponseObj(request);
        return responseObj;
    }

    /**
     * 异步put请求的封装
     *
     * @param url
     * @param param
     * @param callback
     * @return
     * @throws Exception
     */
    public static void putAsyncObjectToJSON(String url, Object param, Callback callback) throws Exception {
        String params = "";
        if (param instanceof String) {
            params = (String) param;
        } else {
            params = MlJson.parseObjectToLightString(param);
        }
        LogUtil.i("OKHttp put url:" + url + " param: " + params);
        Request request = stringToPutRequst(url, params);
        Headers headers = request.headers();
        LogUtil.i("headers info =========================== " + headers.toString());
        getOkHttpClient(null, mConnectTimeout, null)
                .newCall(request)
                .enqueue(callback);
    }

    /**
     * 异步put请求的封装
     *
     * @param url
     * @param param
     * @param headers
     * @param callback
     * @return
     * @throws Exception
     */
    public static void putAsyncObjectToJSON(String url, Object param, Callback callback, Headers headers) throws Exception {
        String params = "";
        if (param instanceof String) {
            params = (String) param;
        } else {
            params = MlJson.parseObjectToLightString(param);
        }
        LogUtil.i("OKHttp put url:" + url + " param: " + params);
        Request request = stringToPutRequst(url, params, headers);
        getOkHttpClient(null, mConnectTimeout, null)
                .newCall(request)
                .enqueue(callback);
    }

    /**
     * 异步put请求的封装 接收Map的参数
     *
     * @param url
     * @param callback
     * @throws Exception
     */
    public static void putAsyncMapToJSON(String url, Map<String, Object> params, Callback callback) throws Exception {
        String param = MlJson.parseObjectToLightString(params);
        LogUtil.i("OKHttp put url:" + url + " param: " + param);
        Request request = stringToPutRequst(url, param);
        getOkHttpClient(null, mConnectTimeout, null)
                .newCall(request)
                .enqueue(callback);
    }

    /**
     * 异步put请求的封装 接收Map的参数
     *
     * @param url
     * @param callback
     * @param headers
     * @throws Exception
     */
    public static void putAsyncMapToJSON(String url, Map<String, Object> params, Callback callback, Headers headers) throws Exception {
        String param = MlJson.parseObjectToLightString(params);
        LogUtil.i("OKHttp put url:" + url + " param: " + param);
        Request request = stringToPutRequst(url, param, headers);
        getOkHttpClient(null, mConnectTimeout, null)
                .newCall(request)
                .enqueue(callback);
    }

    //======================================================== 同步 delete 请求 =================================================================================

    /**
     * delete（接收 object类型的参数）
     *
     * @param url   检测地址
     * @param param object类型的参数
     * @return json格式的字符串
     * @throws Exception
     */
    public static String deleteSyncObjectToJSON(String url, Object param) throws Exception {
        String params = "";
        if (param instanceof String) {
            params = (String) param;
        } else {
            params = MlJson.parseObjectToLightString(param);
        }
        LogUtil.i("OKHttp delete url:" + url + " param: " + params);
        Request request = stringToDeleteRequst(url, params);
        Headers headers = request.headers();
        LogUtil.i("headers info =========================== " + headers.toString());
        String result = syncExecuteToJson(request);
        return result;
    }

    /**
     * 同步delete请求的封装（接收 object类型的参数）
     *
     * @param url     检测地址
     * @param param   object类型的参数
     * @param headers
     * @return json格式的字符串
     * @throws Exception
     */
    public static String deleteSyncObjectToJSON(String url, Object param, Headers headers) throws Exception {
        String params = "";
        if (param instanceof String) {
            params = (String) param;
        } else {
            params = MlJson.parseObjectToLightString(param);
        }
        LogUtil.i("OKHttp delete url:" + url + " param: " + params);
        Request request = stringToDeleteRequst(url, params, headers);
        String result = syncExecuteToJson(request);
        return result;
    }

    /**
     * 同步delete请求的封装（接收 object类型的参数）
     *
     * @param url   检测地址
     * @param param object类型的参数
     * @return ResponseObj
     * @throws Exception
     */
    public static ResponseObj deleteSyncObjectToResponseObj(String url, Object param) throws Exception {
        String params = "";
        if (param instanceof String) {
            params = (String) param;
        } else {
            params = MlJson.parseObjectToLightString(param);
        }
        LogUtil.i("OKHttp delete url:" + url + " param: " + params);
        Request request = stringToDeleteRequst(url, params);
        Headers headers = request.headers();
        LogUtil.i("headers info =========================== " + headers.toString());
        LogUtil.i("headers size =========================== " + request.headers().size());
        ResponseObj responseObj = syncExecuteToResponseObj(request);
        return responseObj;
    }

    /**
     * 同步delete请求的封装（接收 object类型的参数）
     *
     * @param url     检测地址
     * @param param   object类型的参数
     * @param headers
     * @return ResponseObj
     * @throws Exception
     */
    public static ResponseObj deleteSyncObjectToResponseObj(String url, Object param, Headers headers) throws Exception {
        String params = "";
        if (param instanceof String) {
            params = (String) param;
        } else {
            params = MlJson.parseObjectToLightString(param);
        }
        LogUtil.i("OKHttp delete url:" + url + " param: " + params);
        Request request = stringToDeleteRequst(url, params, headers);
        ResponseObj responseObj = syncExecuteToResponseObj(request);
        return responseObj;
    }

    /**
     * 同步delete请求的封装（接收 Map类型的参数）
     *
     * @param url
     * @param params
     * @return json格式的字符串
     * @throws Exception
     */
    public static String deleteSyncMapToJSON(String url, Map<String, Object> params) throws Exception {
        String param = MlJson.parseObjectToLightString(params);
        LogUtil.i("OKHttp delete url:" + url + " param: " + param);
        Request request = stringToDeleteRequst(url, param);
        String result = syncExecuteToJson(request);
        return result;
    }

    /**
     * 同步delete请求的封装（接收 Map类型的参数）
     *
     * @param url
     * @param params
     * @param headers
     * @return json格式的字符串
     * @throws Exception
     */
    public static String deleteSyncMapToJSON(String url, Map<String, Object> params, Headers headers) throws Exception {
        String param = MlJson.parseObjectToLightString(params);
        LogUtil.i("OKHttp delete url:" + url + " param: " + param);
        Request request = stringToDeleteRequst(url, param, headers);
        String result = syncExecuteToJson(request);
        return result;
    }

    //======================================================== 异步 delete 请求 =================================================================================

    /**
     * 同步delete请求的封装（接收 Map类型的参数）
     *
     * @param url
     * @param params
     * @return ResponseObj
     * @throws Exception
     */
    public static ResponseObj deleteSyncMapToResponseObj(String url, Map<String, Object> params) throws Exception {
        String param = MlJson.parseObjectToLightString(params);
        LogUtil.i("OKHttp delete url:" + url + " param: " + param);
        Request request = stringToDeleteRequst(url, param);
        ResponseObj responseObj = syncExecuteToResponseObj(request);
        return responseObj;
    }

    /**
     * 同步delete请求的封装（接收 Map类型的参数）
     *
     * @param url
     * @param params
     * @param headers
     * @return ResponseObj
     * @throws Exception
     */
    public static ResponseObj deleteSyncMapToResponseObj(String url, Map<String, Object> params, Headers headers) throws Exception {
        String param = MlJson.parseObjectToLightString(params);
        LogUtil.i("OKHttp delete url:" + url + " param: " + param);
        Request request = stringToDeleteRequst(url, param, headers);
        ResponseObj responseObj = syncExecuteToResponseObj(request);
        return responseObj;
    }

    /**
     * 异步delete请求的封装
     *
     * @param url
     * @param param
     * @param callback
     * @return
     * @throws Exception
     */
    public static void deleteAsyncObjectToJSON(String url, Object param, Callback callback) throws Exception {
        String params = "";
        if (param instanceof String) {
            params = (String) param;
        } else {
            params = MlJson.parseObjectToLightString(param);
        }
        LogUtil.i("OKHttp delete url:" + url + " param: " + params);
        Request request = stringToDeleteRequst(url, params);
        Headers headers = request.headers();
        LogUtil.i("headers info =========================== " + headers.toString());
        getOkHttpClient(null, mConnectTimeout, null)
                .newCall(request)
                .enqueue(callback);
    }

    /**
     * 异步delete请求的封装
     *
     * @param url
     * @param param
     * @param headers
     * @param callback
     * @return
     * @throws Exception
     */
    public static void deleteAsyncObjectToJSON(String url, Object param, Callback callback, Headers headers) throws Exception {
        String params = "";
        if (param instanceof String) {
            params = (String) param;
        } else {
            params = MlJson.parseObjectToLightString(param);
        }
        LogUtil.i("OKHttp delete url:" + url + " param: " + params);
        Request request = stringToDeleteRequst(url, params, headers);
        getOkHttpClient(null, mConnectTimeout, null)
                .newCall(request)
                .enqueue(callback);
    }

    /**
     * 异步delete请求的封装 接收Map的参数
     *
     * @param url
     * @param callback
     * @throws Exception
     */
    public static void deleteAsyncMapToJSON(String url, Map<String, Object> params, Callback callback) throws Exception {
        String param = MlJson.parseObjectToLightString(params);
        LogUtil.i("OKHttp delete url:" + url + " param: " + param);
        Request request = stringToDeleteRequst(url, param);
        getOkHttpClient(null, mConnectTimeout, null)
                .newCall(request)
                .enqueue(callback);
    }

    /**
     * 异步delete请求的封装 接收Map的参数
     *
     * @param url
     * @param callback
     * @param headers
     * @throws Exception
     */
    public static void deleteAsyncMapToJSON(String url, Map<String, Object> params, Callback callback, Headers headers) throws Exception {
        String param = MlJson.parseObjectToLightString(params);
        LogUtil.i("OKHttp delete url:" + url + " param: " + param);
        Request request = stringToDeleteRequst(url, param, headers);
        getOkHttpClient(null, mConnectTimeout, null)
                .newCall(request)
                .enqueue(callback);
    }

    //======================================================== 私有方法 =================================================================================

    /**
     * 产出get请求的Request
     *
     * @param url
     * @param headers 头信息
     * @return
     */
    private static Request buildGetRequest(String url, Headers headers) {
        //不使用缓存
        CacheControl.Builder builder = new CacheControl.Builder();
        builder.noCache().noStore();
        Request.Builder requestBuilder = new Request.Builder();
        if (headers != null && headers.size() > 0) {
            requestBuilder.headers(headers);
        }
        return requestBuilder.cacheControl(builder.build()).url(url).build();
    }

    /**
     * 产出get请求的Request
     *
     * @param url
     * @return
     */
    private static Request buildGetRequest(String url) {
        //不使用缓存
        CacheControl.Builder builder = new CacheControl.Builder();
        builder.noCache().noStore();
        return new Request.Builder().cacheControl(builder.build()).url(url).build();
    }

    /**
     * 接收string对象  产出post请求的Request对象
     *
     * @param url     请求地址
     * @param params  参数
     * @param headers 头文件
     * @return
     */
    private static Request stringToPostRequst(String url, String params, Headers headers) {
        Request request = null;
        if (params != null) {
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), params);
            Request.Builder requestBuilder = new Request.Builder();
            if (headers != null && headers.size() > 0) {
                requestBuilder.headers(headers);
            }
            request = requestBuilder.url(url).post(requestBody).build();
        }
        return request;
    }

    /**
     * 接收string对象  产出post请求的Request对象
     *
     * @param url    请求地址
     * @param params 参数
     * @return
     */
    private static Request stringToPostRequst(String url, String params) {
        Request request = null;
        if (params != null) {
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), params);
            request = new Request.Builder().url(url).post(requestBody).build();
        }
        return request;
    }

    /**
     * 接收string对象  产出put请求的Request对象
     *
     * @param url     请求地址
     * @param params  参数
     * @param headers 头文件
     * @return
     */
    private static Request stringToPutRequst(String url, String params, Headers headers) {
        Request request = null;
        if (params != null) {
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), params);
            Request.Builder requestBuilder = new Request.Builder();
            if (headers != null && headers.size() > 0) {
                requestBuilder.headers(headers);
            }
            request = requestBuilder.url(url).put(requestBody).build();
        }
        return request;
    }

    /**
     * 接收string对象  产出put请求的Request对象
     *
     * @param url    请求地址
     * @param params 参数
     * @return
     */
    private static Request stringToPutRequst(String url, String params) {
        Request request = null;
        if (params != null) {
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), params);
            request = new Request.Builder().url(url).put(requestBody).build();
        }
        return request;
    }

    /**
     * 接收string对象  产出delete请求的Request对象
     *
     * @param url    请求地址
     * @param params 参数
     * @return
     */
    private static Request stringToDeleteRequst(String url, String params) {
        Request request = null;
        if (params != null) {
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), params);
            request = new Request.Builder().url(url).delete(requestBody).build();
        }
        return request;
    }

    /**
     * 接收string对象  产出delete请求的Request对象
     *
     * @param url     请求地址
     * @param params  参数
     * @param headers 头文件
     * @return
     */
    private static Request stringToDeleteRequst(String url, String params, Headers headers) {
        Request request = null;
        if (params != null) {
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), params);
            Request.Builder requestBuilder = new Request.Builder();
            if (headers != null && headers.size() > 0) {
                requestBuilder.headers(headers);
            }
            request = requestBuilder.url(url).delete(requestBody).build();
        }
        return request;
    }

    /**
     * 同步执行请求（post + get）
     *
     * @param request
     * @return 返回json串
     * @throws Exception
     */
    private static String syncExecuteToJson(Request request) throws Exception {
        try {
//            LogUtil.e(StarConstants.TAG, "request rul = " + request.url());
            LogUtil.i("request header = " + request.headers().toString());
            Response response = getOkHttpClient(null, mConnectTimeout, null)
                    .newCall(request)
                    .execute();
            if (response.isSuccessful()) {
                String result = response.body().string();
                LogUtil.i("OKHttp result: " + result);
                return result;
            } else {
                LogUtil.i("OKHttp response failed message = " + response.message());
                LogUtil.i("OKHttp response failed body = " + response.body().string());
                LogUtil.i("OKHttp response code = " + response.code());
            }
        } catch (Exception e) {
            LogUtil.i("OKHttp syncExecuteToJson excepition : "+e.toString());
            throw e;
        }
        return "";
    }

    /**
     * 同步执行请求（post + get）
     *
     * @param request
     * @return 返回一个自定义对象
     * @throws Exception
     */
    private static ResponseObj syncExecuteToResponseObj(Request request) throws Exception {
        ResponseObj responseObj = new ResponseObj();
        try {
            LogUtil.i("request header = " + request.headers().toString());
            Response response = getOkHttpClient(null, mConnectTimeout, null)
                    .newCall(request)
                    .execute();
            responseObj.setCode(response.code());
            if (response.isSuccessful()) {
                String result = response.body().string();
                responseObj.setJsonStr(result);
                LogUtil.i("OKHttp result: " + result);
                return responseObj;
            } else {
                LogUtil.i("OKHttp response failed message = " + response.message());
                LogUtil.i("OKHttp response failed body = " + response.body().string());
                LogUtil.i("OKHttp response code = " + response.code());
            }
        } catch (Exception e) {
            LogUtil.i("OKHttp syncExecuteToJson excepition : "+e.toString());
            throw e;
        }
        return responseObj;
    }

    /**
     * 获取SSLSocketFactory
     *
     * @param certificates 证书流文件
     * @return
     */
    private static SSLSocketFactory getSSLSocketFactory(InputStream[] certificates) {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            int index = 0;
            for (InputStream certificate : certificates) {
                String certificateAlias = Integer.toString(index++);
                keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));

                try {
                    if (certificate != null) {
                        certificate.close();
                    }
                } catch (IOException e) {
                }
            }
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //======================================================== 其他扩展方法 =================================================================================

    /**
     * 异步下载文件
     *
     * @param url        下载地址
     * @param targetFile 目标文件
     * @param callback   回调 如果为空则用默认的下载回调
     */
    public void asyncDownloadFile(String url, final File targetFile, Callback callback) {
        Request request = buildGetRequest(url);
        Call call = getOkHttpClient(null, mConnectTimeout, null).newCall(request);
        if (callback == null) {
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) {
                    long Size = response.body().contentLength();
                    InputStream inputStream = response.body().byteStream();
                    FileOutputStream fileOutputStream = null;
                    try {
                        fileOutputStream = new FileOutputStream(targetFile);
                        byte[] buffer = new byte[2048];
                        int len = 0;
                        while ((len = inputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, len);
                        }
                        fileOutputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            call.enqueue(callback);
        }
    }

    /**
     * 上传文件
     *
     * @param upLoadUrl 接口地址
     * @param filePath  本地文件地址
     */
    public static void upLoadVideoFile(String upLoadUrl, String filePath, Callback callback) {
        LogUtil.i("OKHttp upLoadVideoFile request: " + upLoadUrl);
        File file = new File(filePath);
        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(fileBody)
                .addFormDataPart("video", "test.mp4", fileBody)
                .build();

        Request request = new Request.Builder()
                .url(upLoadUrl)
                .post(requestBody)
                .build();

        Call call = getOkHttpClient(null, mConnectTimeout, null).newCall(request);
        call.enqueue(callback);
    }

    /**
     * 上传多张图片
     *
     * @param url 接口地址
     */
    public static void upLoadMultiPic(String url, List<String> targetPics, Callback callback) {
        LogUtil.i("OKHttp upLoadMultiPic request: " + url);
        if (targetPics != null && targetPics.size() > 0) {
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            for (int i = 0; i < targetPics.size(); i++) {
                File file = new File(targetPics.get(i));
                RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
                builder.addFormDataPart("pic", "test.png", fileBody);
            }
            Request request = new Request.Builder()
                    .url(url)
                    .post(builder.build())
                    .build();

            Call call = getOkHttpClient(null, mConnectTimeout, null).newCall(request);
            call.enqueue(callback);
        }

    }
}
