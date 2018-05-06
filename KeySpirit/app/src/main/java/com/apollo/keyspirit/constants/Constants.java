package com.apollo.keyspirit.constants;

/**
 * Created by Apollo on 2017/1/13.
 */

public interface Constants {
    /**
     * 服务器根地址
     */
    String BASE_URL = "http://www.zhekoulieshou.com/";
    /**
     * 检查更新地址
     */
    String CHECK_UPDATE_URL = "http://app.qq.com/";
//          +"#id=detail&appid=1105879293";
    /**
     * 放在github上的app
     */
    String CHECK_UPDATE_GITHUB_URL = "https://raw.githubusercontent.com/SuperApollo/app-update/master/description_keyspirit";
    /**
     * 首页列表数据地址
     */
    String HOME_LIST = BASE_URL + "?c=API&a=app_items&offset=0&limit=10&eid=0";
    /**
     * 点击商品条目跳转携带的商品信息
     */
    String GOODS_INFO = "goods_info";
    /**
     * bundle传值标志
     */
    String BUNDLE_TAG = "bundle_tag";
    /**
     * github地址
     */
    String GITHUB_BASE_URL = "https://github.com/";
    /**
     * 测试下载
     */
    String TEST_DOWNLOAD_PIC = "http://e.hiphotos.baidu.com/";
    /**
     * 折扣猎手在腾讯应用宝的地址
     */
    String zkls_tencent_url = "http://sj.qq.com/myapp/detail.htm?apkName=com.apollo.discounthunter";
    /**
     * 折扣猎手在应用宝缩略图地址
     */
    String zkls_tencent_pic = "http://pp.myapp.com/ma_icon/0/icon_52415166_1492676694/96";
    /**
     * 广播 action
     */
    String MY_BROADCUST_RECEIVER_ACTION = "my_broadcust_receiver_action";
    /**
     *  MainActivity 给 SwipeService 发的命令广播
     */
    String BROADCUST_COMMAND = "broadcust_command";
    /**
     * 开始命令
     */
    String COMMAND_START = "command_start";
    /**
     * 停止命令
     */
    String COMMAND_STOP = "command_stop";
}
