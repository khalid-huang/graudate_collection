package org.sysu.nameservice.loadbalancer;

import okhttp3.Request;
import okhttp3.Response;
import org.sysu.nameservice.interactionRouter.IIteractionRouter;
import org.sysu.nameservice.interactionRouter.OkHttpCallback;
import org.sysu.nameservice.interactionRouter.OkHttpClientRouter;

import java.util.Map;

/** 类似于okhttp，通过构建一个httpRequest，来发起请求；在apply的时候使用ServiceInstance instance来实例信息*/
/** 基于okhttp来实现的Request */
public class WorkflowLoadBalancerRequest implements LoadBalancerRequest<String> {
    private boolean isAsync;

    private String method;

    private OkHttpClientRouter okHttpClientRouter;

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

    /**针对异步POST */
    public WorkflowLoadBalancerRequest(boolean isAsync, String method, String urlWithoutServerInfo, Map<String, String> headers, Map<String, Object> params, OkHttpCallback callback) {
        this.isAsync = isAsync;
        this.callback = callback;
        this.method = method;
        this.headers = headers;
        this.params = params;
        this.urlWithoutServerInfo = urlWithoutServerInfo;
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


    @Override
    public String apply(ServiceInstance instance) throws Exception {
        String url = instance.getUri().getPath() + urlWithoutServerInfo;
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

}
