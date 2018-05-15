package org.springframework.cloud.netflix.ribbon.canary;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.*;

public class CanaryRule extends ZoneAvoidanceRule {

    private CompositePredicate compositePredicate;

    public CanaryRule() {
        super();
        CanaryServerPredicate canaryServerPredicate = new CanaryServerPredicate();
        compositePredicate = createCompositePredicate(canaryServerPredicate);
    }

    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {
        ZoneAvoidancePredicate zonePredicate = new ZoneAvoidancePredicate(this, clientConfig);
        AvailabilityPredicate availabilityPredicate = new AvailabilityPredicate(this, clientConfig);
        CanaryServerPredicate canaryServerPredicate = new CanaryServerPredicate();
        compositePredicate = createCompositePredicate(zonePredicate, availabilityPredicate, canaryServerPredicate);
    }

    @Override
    public AbstractServerPredicate getPredicate() {
        return compositePredicate;
    }

    private CompositePredicate createCompositePredicate(AbstractServerPredicate ...serverPredicates) {
        return CompositePredicate.withPredicates(serverPredicates)
                .addFallbackPredicate(serverPredicates[serverPredicates.length-1])
                .addFallbackPredicate(AbstractServerPredicate.alwaysTrue())
                .build();

    }
}
