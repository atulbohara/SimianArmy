package com.netflix.simianarmy.client.openstack;

import java.util.LinkedList;
import java.util.List;
import org.jclouds.openstack.nova.v2_0.domain.Server;

import com.netflix.simianarmy.basic.chaos.BasicInstanceGroup;
import com.netflix.simianarmy.chaos.ChaosCrawler;
import com.netflix.simianarmy.client.aws.AWSClient;
import com.netflix.simianarmy.client.aws.chaos.ASGChaosCrawler;

/**
 * The Class OpenstackChaosCrawler. This will crawl for all available Instances from a zone.
 */
public class OpenstackChaosCrawler extends ASGChaosCrawler implements ChaosCrawler {
    private final OpenstackClient awsClient;

    /**
     * Constructor for the OpenstackChaosCrawler that mimics ASGChaosCrawler
     * @param client An OpenstackClient
     */
    public OpenstackChaosCrawler(OpenstackClient client) {
            super((AWSClient) client);
            awsClient = client;
    }

    /** {@inheritDoc} */
    @Override
    public List<InstanceGroup> groups(String... names) {
        List<InstanceGroup> list = new LinkedList<InstanceGroup>();
        awsClient.connect();
        String zone = awsClient.getServiceConnection().getZone();
        InstanceGroup ig = new BasicInstanceGroup(awsClient.getServiceConnection().getTenantName(), Types.ASG, zone);
        for(Server server: awsClient.getNovaApi().getServerApiForZone(zone).listInDetail().concat()) {
        	ig.addInstance(server.getId());
        }

        list.add(ig);
        awsClient.disconnect();
        return list;
    }
}
