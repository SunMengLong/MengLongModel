package com.menglong.update;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by sml on 2019/7/25.
 * Apk下载与安装
 */

public class MlAppUpdate {

    private TextView progressTv1, progressTv2;
    private Context context;
    private ProgressBar progressBar;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                showDialog();
            } else {
                int progress = msg.arg1;
                progressBar.setProgress(progress);
                progressTv1.setText(progress + "%");
                progressTv2.setText(progress + "/100");
            }
        }
    };

    private volatile static MlAppUpdate instance;
    private AlertDialog alertDialog;

    public static MlAppUpdate getInstence() {
        if (instance == null) {
            synchronized (MlAppUpdate.class) {
                if (instance == null) {
                    instance = new MlAppUpdate();
                }
            }
        }
        return instance;
    }

    /**
     * 下载apk文件
     */
    public void downloadAndInstall(final String apkUrl, Context context) {
        this.context = context;
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    URL url = new URL(apkUrl);
                    //打开连接
                    URLConnection conn = url.openConnection();
                    //打开输入流
                    InputStream is = conn.getInputStream();
                    //获得长度
                    int contentLength = conn.getContentLength();
                    //创建文件夹 MyDownLoad，在存储卡下
                    String dirName = Environment.getExternalStorageDirectory() + "/";
                    File file = new File(dirName);
                    //不存在创建
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    //下载后的文件名
                    String fileName = dirName + "aabbccddee11234.apk";
                    File file1 = new File(fileName);
                    if (file1.exists()) {
                        file1.delete();
                    }
                    //创建字节流
                    byte[] bs = new byte[1024];
                    int len;
                    OutputStream os = new FileOutputStream(fileName);
                    //写数据
                    int bytes = contentLength / 100;
                    int i = 0;
                    handler.sendEmptyMessage(0);
                    while ((len = is.read(bs)) != -1) {
                        os.write(bs, 0, len);
                        i = i + len;
                        Message message = Message.obtain();
                        message.arg1 = i / bytes;
                        message.what = 1;
                        handler.sendMessage(message);
                    }
                    Message message = Message.obtain();
                    message.arg1 = 100;
                    message.what = 1;
                    handler.sendMessage(message);
                    alertDialog.dismiss();
                    os.close();
                    is.close();
                    openAPK(fileName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 文件下载进度弹框
     */
    private void showDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        View view = View.inflate(context, R.layout.layout_update_dialog, null);
        progressBar = view.findViewById(R.id.update_dialog_progressbar);
        progressTv1 = view.findViewById(R.id.progress_tv1);
        progressTv2 = view.findViewById(R.id.progress_tv2);
        alertDialogBuilder.setView(view).setCancelable(false).create();
        alertDialog = alertDialogBuilder.show();
    }

    /**
     * 安装apk
     *
     * @param fileSavePath
     */
    private void openAPK(String fileSavePath) {
        try {
            Intent installIntent = new Intent();
            installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            installIntent.setAction(Intent.ACTION_VIEW);
            Uri apkFileUri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                apkFileUri = FileProvider.getUriForFile(context, "com.menglong.modeltest.provider", new File(fileSavePath));
            } else {
                apkFileUri = Uri.fromFile(new File(fileSavePath));
            }
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            installIntent.setDataAndType(apkFileUri, "application/vnd.android.package-archive");
            context.startActivity(installIntent);
        } catch (ActivityNotFoundException e) {

        }
    }
}
