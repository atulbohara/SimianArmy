package com.netflix.simianarmy.client.openstack;

import java.util.LinkedList;
import java.util.List;

import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.Instance;
import com.netflix.simianarmy.basic.chaos.BasicInstanceGroup;
import com.netflix.simianarmy.chaos.ChaosCrawler;
import com.netflix.simianarmy.chaos.ChaosCrawler.InstanceGroup;
import com.netflix.simianarmy.client.aws.AWSClient;
import com.netflix.simianarmy.client.aws.chaos.ASGChaosCrawler;
import com.netflix.simianarmy.client.aws.chaos.ASGChaosCrawler.Types;
import org.jclouds.openstack.nova.v2_0.domain.Server;

public class OpenstackChaosCrawler extends ASGChaosCrawler implements ChaosCrawler {
	private final OpenstackClient awsClient;
	
	public OpenstackChaosCrawler(OpenstackClient client)
	{
		super((AWSClient)client);
		awsClient = client;
	}
	
	@Override
    public List<InstanceGroup> groups(String... names) {
        List<InstanceGroup> list = new LinkedList<InstanceGroup>();
        awsClient.connect();
        String zone = awsClient.getServiceConnection().getZone();
        InstanceGroup ig = new BasicInstanceGroup(zone, Types.ASG, zone);
        for(Server server: awsClient.getNovaApi().getServerApiForZone(zone).listInDetail().concat())
        {
        	ig.addInstance(server.getId());
        }
        
        list.add(ig);
        
        awsClient.disconnect();
        return list;
    }
}
