package org.sysu.nameservice.interactionRouter;

import okhttp3.Call;
import okhttp3.Response;

/** 用于传入回调函数，这个函数在事件函数中最先执行*/
public interface OkHttpCallback {
    void call(Call call, Response response);
}
