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

    /** 请求url，没有带http://localhost:10089，也就是没有协议头，没有host，没有port*/
    /** 要求以/开头*/
    private String urlWithoutServerInfo;

    /** 保持一些附带的执行信息，比如processInstanceRoutingId，processModelKey等*/
    private Map<String, String> info;

    private String processInstanceRoutingId;

    /**针对异步POST */
    public WorkflowLoadBalancerRequest(boolean isAsync, String method, String urlWithoutServerInfo, Map<String, String> headers, Map<String, Object> params, OkHttpCallback callback) {
        this.isAsync = isAsync;
        this.callback = callback;
        this.method = method;
        this.headers = headers;
        this.params = params;
        this.urlWithoutServerInfo = urlWithoutServerInfo;
        info = new HashMap<>();
    }

    /** 针对同步GET */
    public WorkflowLoadBalancerRequest(boolean isAsync, String method, String urlWithoutServerInfo, Map<String, String> headers) {
        this(isAsync, method, urlWithoutServerInfo, headers, null, null);
    }

    /** 针对异步GET*/
    public WorkflowLoadBalancerRequest(boolean isAsync, String method, String urlWithoutServerInfo, Map<String, String> headers, OkHttpCallback callback) {
        this(isAsync, method, urlWithoutServerInfo,headers, null ,callback);
    }

    /**针对同步POST*/
    public WorkflowLoadBalancerRequest(boolean isAsync, String method, String urlWithoutServerInfo, Map<String, String> headers, Map<String, Object> params) {
        this(isAsync, method, urlWithoutServerInfo, headers, params, null);
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
        String url = instance.getUri() +"/"+ urlWithoutServerInfo;
        if(!isAsync && method.equals("GET")) {
            Response response = okHttpClientRouter.syncGet(url, headers);
            return response.body().string();
        }
        if(!isAsync && method.equals("POST")) {
            Response response =  okHttpClientRouter.syncPost(url, headers, params);
            return response.body().string();
        }
        /** 异步 */
        if(isAsync && method.equals("GET")) {
            okHttpClientRouter.asyncGet(url, headers, callback);
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
        if(urlWithoutServerInfo.startsWith(GlobalContext.URL_ACTIVITISERVICE_STARTPROCESS)) {
            return GlobalContext.ACTION_ACTIVITISERVICE_STARTPROCESS;
        }
        if(urlWithoutServerInfo.startsWith(GlobalContext.URL_ACTIVITISERVICE_CLAIMTASK)) {
            return GlobalContext.ACTION_ACTIVITISERVICE_CLAIMTASK;
        }
        if(urlWithoutServerInfo.startsWith(GlobalContext.URL_ACTIVITISERVICE_COMPLETETASK)) {
            return GlobalContext.ACTION_ACTIVITISERVICE_COMPLETETASK;
        }
        return "TEST";
    }

    public String getProcessInstanceRoutingId() {
        return processInstanceRoutingId;
    }

    public void setProcessInstanceRoutingId(String processInstanceRoutingId) {
        this.processInstanceRoutingId = processInstanceRoutingId;
    }
}
