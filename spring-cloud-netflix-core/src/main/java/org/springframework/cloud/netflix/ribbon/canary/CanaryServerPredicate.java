package org.springframework.cloud.netflix.ribbon.canary;

import com.netflix.loadbalancer.AbstractServerPredicate;
import com.netflix.loadbalancer.PredicateKey;
import com.netflix.loadbalancer.Server;

public class CanaryServerPredicate extends AbstractServerPredicate {

    @Override
    public boolean apply(PredicateKey input) {
        if(input.getLoadBalancerKey() == null || !Boolean.valueOf(input.getLoadBalancerKey().toString())){
            return true;
        }
        Server server = input.getServer();
        String serverGroup = server.getMetaInfo().getServerGroup();
        if(serverGroup != null && serverGroup.equalsIgnoreCase(CanaryConst.INSTANCE_SERVER_GROUP_CANARY)){
            return true;
        }else{
            return false;
        }
    }

}
