package com.netflix.simianarmy.client.openstack;

import java.util.List;
import java.util.Map;

import org.jclouds.compute.ComputeService;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.ssh.SshClient;

import com.netflix.simianarmy.CloudClient;

public class OpenstackClient implements CloudClient {

	public OpenstackClient(OpenstackServiceConnection conn)
	{
		
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
	public SshClient connectSsh(String instanceId, LoginCredentials credentials) {
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
	public boolean canChangeInstanceSecurityGroups(String instanceId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setInstanceSecurityGroups(String instanceId,
			List<String> groupIds) {
		// TODO Auto-generated method stub
		
	}

}
