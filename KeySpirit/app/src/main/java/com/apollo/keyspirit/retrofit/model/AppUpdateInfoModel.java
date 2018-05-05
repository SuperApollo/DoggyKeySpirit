package com.apollo.keyspirit.retrofit.model;

/**
 * APP更新信息
 * Created by apollo on 17-3-15.
 */

public class AppUpdateInfoModel {
    String appVersion;
    String appSize;
    String appDescription;
    String appUrl;

    public AppUpdateInfoModel(String appVersion, String appSize, String appDescription, String appUrl) {
        this.appVersion = appVersion;
        this.appSize = appSize;
        this.appDescription = appDescription;
        this.appUrl = appUrl;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getAppSize() {
        return appSize;
    }

    public void setAppSize(String appSize) {
        this.appSize = appSize;
    }

    public String getAppDescription() {
        return appDescription;
    }

    public void setAppDescription(String appDescription) {
        this.appDescription = appDescription;
    }

    public String getAppUrl() {
        return appUrl;
    }

    public void setAppUrl(String appUrl) {
        this.appUrl = appUrl;
    }

    @Override
    public String toString() {
        return "AppUpdateInfoModel{" +
                "appVersion='" + appVersion + '\'' +
                ", appSize='" + appSize + '\'' +
                ", appDescription='" + appDescription + '\'' +
                ", appUrl='" + appUrl + '\'' +
                '}';
    }

}
