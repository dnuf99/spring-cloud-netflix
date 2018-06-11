package org.springframework.cloud.netflix.ribbon.canary;

import com.netflix.loadbalancer.AbstractServerPredicate;
import com.netflix.loadbalancer.PredicateKey;
import com.netflix.loadbalancer.Server;

public class CanaryServerPredicate extends AbstractServerPredicate {

    @Override
    public boolean apply(PredicateKey input) {
        //always true
        if (input.getLoadBalancerKey() == null) {
            return true;
        }
        Boolean loadBalancerKey = Boolean.valueOf(input.getLoadBalancerKey().toString());
        Server server = input.getServer();
        String serverGroup = server.getMetaInfo().getServerGroup();

        if (CanaryConst.INSTANCE_SERVER_GROUP_CANARY.equalsIgnoreCase(serverGroup)) {
            //eureka.instance.a-s-g-name=canary
            if (loadBalancerKey) {
                //choose canary server
                return true;
            } else {
                //not choose canary server
                return false;
            }
        } else {
            //server group is null or not canary
            if (loadBalancerKey) {
                //not choose canary server
                return false;
            } else {
                //choose server
                return true;
            }
        }
    }

}
