package com.razor.raas.models;

import java.util.Date;
import java.util.UUID;

/**
 * Created by phemmings on 10/30/17.
 * Container for result
 */

public class ResultMessage {

    private Long createTime;
    private RequestMessage originalRequest;
    private String result;
    private String uniqueId;

    public ResultMessage(final RequestMessage original, final String result) {
        this.uniqueId = UUID.randomUUID().toString();
        this.createTime = new Date().getTime();
        this.originalRequest = original;
        this.result = result;
    }

    public String getResult() {
        return result;
    }
    public Long getCreateTime() {
        return createTime;
    }
    public RequestMessage getOriginalRequest() {
        return originalRequest;
    }
    public String getUniqueId() {
        return uniqueId;
    }
}
