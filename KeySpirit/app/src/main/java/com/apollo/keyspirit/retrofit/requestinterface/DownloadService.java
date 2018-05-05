package com.apollo.keyspirit.retrofit.requestinterface;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by apollo on 17-3-16.
 */

public interface DownloadService {
    @Streaming//加上这个注解以后就不会讲文件内容加载到内存中,而是在通过ResponseBody 去读取文件的时候才从网络文件去下载文件.
    @GET
    Call<ResponseBody> downloadRepo(@Url String fileUrl);
}
