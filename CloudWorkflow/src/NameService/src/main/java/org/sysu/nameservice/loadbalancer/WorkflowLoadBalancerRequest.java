package org.sysu.nameservice.loadbalancer;

import okhttp3.Request;
import okhttp3.Response;
import org.sysu.nameservice.GlobalContext;
import org.sysu.nameservice.interactionRouter.IIteractionRouter;
import org.sysu.nameservice.interactionRouter.OkHttpCallback;
import org.sysu.nameservice.interactionRouter.OkHttpClientRouter;

import java.util.HashMap;
import java.util.Map;

/** 类似于okhttp，通过构建一个httpRequest，来发起请求；在apply的时候使用ServiceInstance instance来实例信息*/
/** 基于okhttp来实现的Request */
public class WorkflowLoadBalancerRequest implements LoadBalancerRequest {
    private boolean isAsync;

    private String method;

    private OkHttpClientRouter okHttpClientRouter = OkHttpClientRouter.getInstance();

    private Request request;

    /** 回调函数 */
    private OkHttpCallback callback;

    /** 请求头信息 */
    private Map<String, String> headers;

    /** 请求参数 */
    private Map<String, Object> params;

    /** 保持一些附带的执行信息，比如processInstanceRoutingId，processModelKey, url信息等， 用于组装起一个URL*/
    private Map<String, String> info; //用map而不直接用string来表示url是为了方便在最终发送请求之前可以方便修改url的信息

    public WorkflowLoadBalancerRequest(boolean isAsync, String method, Map<String, String> info, Map<String, String> headers, Map<String, Object> params, OkHttpCallback callback) {
        this.isAsync = isAsync;
        this.callback = callback;
        this.method = method;
        this.headers = headers;
        this.params = params;
        this.info = info;
    }


    /** 设置一次额外的信息，比如processModelKey等*/
    public void setKeyValue(String key, String value) {
        info.put(key, value);
    }

    public String getValue(String key) {
        return info.get(key);
    }


    @Override
    public String apply(ServiceInstance instance) throws Exception {
        String url = "" + instance.getUri();

        //组装url
        String action = info.get("action");
        if(action.equals(GlobalContext.ACTION_ACTIVITISERVICE_STARTPROCESS)) {
            url += "/" + info.get("url") + "/" + info.get("processModelKey");
        } else if(action.equals(GlobalContext.ACTION_ACTIVITISERVICE_GETCURRENTSINGLETASK)
                || action.equals(GlobalContext.ACTION_ACTIVITISERVICE_GETCURRENTTASKS)
                || action.equals(GlobalContext.ACTION_ACTIVITISERVICE_GETCURRENTTASKSOFASSIGNEE)) {
            url += "/" + info.get("url") + "/" + info.get("processInstanceId");
        } else if(action.equals(GlobalContext.ACTION_ACTIVITISERVICE_COMPLETETASK)
                || action.equals(GlobalContext.ACTION_ACTIVITISERVICE_CLAIMTASK)) {
            url += "/" + info.get("url") + "/" + info.get("processInstanceId") + "/" + info.get("taskId");
        } else {
            url += "/" + info.get("url");
        }

        if(!isAsync && method.equals("GET")) {
            Response response = okHttpClientRouter.syncGet(url, headers, params);
            return response.body().string();
        }
        if(!isAsync && method.equals("POST")) {
            Response response =  okHttpClientRouter.syncPost(url, headers, params);
            return response.body().string();
        }
        /** 异步 */
        if(isAsync && method.equals("GET")) {
            okHttpClientRouter.asyncGet(url, headers, params, callback);
            return null;
        }
        if(isAsync && method.equals("POST")) {
            okHttpClientRouter.asyncPost(url, headers, params, callback);
        }
        return null;
    }

    /** *
     * 获取请求的操作类型，类型主要有GlobalContext中定义的，是startProcess, claimProcess, complteTask等
     * @return
     */
    public String getAction() {
        return info.get("action");
    }

}
