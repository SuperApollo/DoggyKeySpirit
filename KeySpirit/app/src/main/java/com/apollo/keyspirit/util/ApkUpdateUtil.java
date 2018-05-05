package com.apollo.keyspirit.util;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.widget.TextView;
import android.widget.Toast;


import com.apollo.keyspirit.BuildConfig;
import com.apollo.keyspirit.R;
import com.apollo.keyspirit.app.AppConfig;
import com.apollo.keyspirit.retrofit.ServiceGenerator;
import com.apollo.keyspirit.retrofit.requestinterface.DownloadService;
import com.apollo.keyspirit.widget.HorizontalProgressBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * APP升级工具
 * Created by apollo on 17-3-15.
 */

public class ApkUpdateUtil {
    private static final String apkName = "discounthunter.apk";
    private String apkUrl;
    private Context mContext;
    private HorizontalProgressBar bar;
    private Dialog dialog;
    private TextView textSize;
    private final String TAG = ApkUpdateUtil.class.getSimpleName();
    private final int UPDATE_PROGRESS = 1;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_PROGRESS:
                    long[] progress = (long[]) msg.obj;
                    updatePress(progress[1], progress[0]);
                    break;
            }
        }
    };

    public ApkUpdateUtil(Context context, String apkUrl) {
        this.apkUrl = apkUrl;
        this.mContext = context;
        initDialog();
    }

    private void initDialog() {
        dialog = new Dialog(mContext, R.style.Dialog);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.view_progressbar_horizontal);
        bar = (HorizontalProgressBar) dialog.findViewById(R.id.my_progress);
        textSize = (TextView) dialog.findViewById(R.id.text_update_size);
    }

    private void updatePress(long total, long current) {
        if (total <= 0) {
            return;
        }
        double press = ((current * 0.1d) / (total * 0.1d)) * 100;
        bar.setProgress((int) press);
        textSize.setText("下载进度" + (int) press + "%");
    }

    /**
     * 开始下载
     */
    public void startDown() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {// 判断是否存在SD卡
            ToastUtil.show("SD卡不可用");
            return;
        }

        DownloadService downloadService = ServiceGenerator.createService(DownloadService.class);
        String[] strings = apkUrl.split("https://github.com/");
        Call<ResponseBody> call = downloadService.downloadRepo(strings[1]);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    LogUtil.i(TAG, "连接服务成功,开始下载存储文件===");
                    new SaveTask(response.body()).execute();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                call.cancel();
                Toast.makeText(mContext.getApplicationContext(), "连接服务失败", Toast.LENGTH_SHORT).show();
                LogUtil.e(TAG, t.getMessage());
            }
        });

    }


    private void setupAPk(File file) {
        Intent install = new Intent(Intent.ACTION_VIEW);
        install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 24) {//判读版本是否在7.0以上
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//添加这一句表示对目标应用临时授权该Uri所代表的文件
            install.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Uri apkUri = FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".fileProvider", file);
            install.setDataAndType(apkUri, "application/vnd.android.package-archive");

        } else {

            install.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }

        mContext.startActivity(install);
    }

    /**
     * 将文件保存到本地
     *
     * @param body
     * @param filePath
     * @return
     */
    private boolean writeResponseBodyToDisk(ResponseBody body, String filePath) {
        try {
            // todo change the file location/name according to your needs
            File downloadApkFile = new File(filePath);
            if (!downloadApkFile.getParentFile().exists()) {
                downloadApkFile.getParentFile().mkdirs();
            }

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(downloadApkFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    Message message = new Message();
                    message.what = UPDATE_PROGRESS;
                    long[] progress = {fileSizeDownloaded, fileSize};
                    message.obj = progress;
                    mHandler.sendMessage(message);
                    LogUtil.w("saveFile", "file download: " + fileSizeDownloaded + " of " + fileSize);
                }

                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }

                mHandler.removeCallbacksAndMessages(null);

            }
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 下载任务
     */
    private class SaveTask extends AsyncTask {
        ResponseBody body;

        public SaveTask(ResponseBody body) {
            this.body = body;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            String filePath = AppConfig.FILE_DOWNLOAD + "apk" + File.separator + apkName;
            boolean success = writeResponseBodyToDisk(body, filePath);
            LogUtil.i(TAG, "保存成功：" + success);
            return null;
        }


        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            String filePath = AppConfig.FILE_DOWNLOAD + "apk" + File.separator + apkName;
            File file = new File(filePath);
            if (file.exists()) {
                setupAPk(file);
            }
        }
    }
}
