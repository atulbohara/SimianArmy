package com.netflix.simianarmy.client.openstack;

import com.netflix.simianarmy.MonkeyConfiguration;
import com.netflix.simianarmy.basic.BasicChaosMonkeyContext;
import com.netflix.simianarmy.client.openstack.OpenstackChaosCrawler;

public class OpenstackContext extends BasicChaosMonkeyContext{
    private OpenstackClient client;
    public OpenstackContext() {
            client = null;
            createClient();
    }

	@Override
    protected void createClient() {
        MonkeyConfiguration config = configuration();
        final OpenstackServiceConnection conn = new OpenstackServiceConnection(config);
        client = new OpenstackClient(conn);
        setCloudClient(client);
        setChaosCrawler(new OpenstackChaosCrawler(client));
    }
}
