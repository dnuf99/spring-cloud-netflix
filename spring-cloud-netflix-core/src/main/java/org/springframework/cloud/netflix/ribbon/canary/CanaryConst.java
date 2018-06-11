package org.springframework.cloud.netflix.ribbon.canary;

public class CanaryConst {

    /**
     * http request header
     * X-Canary:true means current request must be routed to canary environment
     */
    public static final String REQUEST_HEADER_X_CANARY = "X-Canary";

    /**
     * eureka.instance.a-s-g-name=canary
     */
    public static final String INSTANCE_SERVER_GROUP_CANARY = "canary";
}
