package com.netflix.simianarmy.client.openstack;

import com.netflix.simianarmy.MonkeyConfiguration;
import com.netflix.simianarmy.basic.BasicChaosMonkeyContext;

public class OpenstackContext extends BasicChaosMonkeyContext{
	@Override
    protected void createClient() {
        MonkeyConfiguration config = configuration();
        final OpenstackClient client = new OpenstackClient(config);
        setCloudClient(client);
    }
	
}
