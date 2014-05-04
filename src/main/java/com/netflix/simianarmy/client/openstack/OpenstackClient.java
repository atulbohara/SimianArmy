package com.netflix.simianarmy.client.openstack;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.simianarmy.client.aws.AWSClient;
import com.netflix.simianarmy.CloudClient;
import com.netflix.simianarmy.client.aws.AWSClient;

public class OpenstackClient extends AWSClient implements CloudClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(AWSClient.class);
	private final OpenstackServiceConnection connection;

	/**
	 * Create the specific Client from the given connection ifnormation.
	 * @param the connection parameters
	 */
	public OpenstackClient(OpenstackServiceConnection conn) {
		super("region-" + conn.getUrl());
		this.connection = conn;
		LOGGER.info("Instantiating OpenStack Client");
	}
	
	@Override
	public void terminateInstance(String instanceId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteAutoScalingGroup(String asgName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteLaunchConfiguration(String launchConfigName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteVolume(String volumeId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteSnapshot(String snapshotId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteImage(String imageId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createTagsForResources(Map<String, String> keyValueMap,
			String... resourceIds) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<String> listAttachedVolumes(String instanceId,
			boolean includeRoot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void detachVolume(String instanceId, String volumeId, boolean force) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ComputeService getJcloudsComputeService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getJcloudsId(String instanceId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String findSecurityGroup(String instanceId, String groupName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createSecurityGroup(String instanceId, String groupName,
			String description) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setInstanceSecurityGroups(String instanceId,
			List<String> groupIds) {
		// TODO Auto-generated method stub
		
	}

}
