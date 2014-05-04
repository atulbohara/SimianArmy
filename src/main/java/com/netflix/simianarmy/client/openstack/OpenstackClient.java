package com.netflix.simianarmy.client.openstack;

import static com.google.common.io.Closeables.closeQuietly;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.NovaAsyncApi;
import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.jclouds.rest.RestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import com.netflix.simianarmy.CloudClient;
import com.netflix.simianarmy.NotFoundException;

public class OpenstackClient implements CloudClient {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OpenstackClient.class);
	private final OpenstackServiceConnection connection;
	
	private ComputeService compute = null;
	private RestContext<NovaApi, NovaAsyncApi> nova = null;
	private Set<String> zones = null;
	private ComputeServiceContext context = null;

	/**
	 * Create the specific Client from the given connection information.
	 * @param the connection parameters
	 */
	public OpenstackClient(OpenstackServiceConnection conn) {
		this.connection = conn;
		LOGGER.info("Instantiating OpenStack Client");
	}
	
	protected void connect() throws AmazonServiceException
	   {
		   try
		   {
			   if(compute == null)
			   {
				   Iterable<Module> modules = ImmutableSet.<Module> of(new SLF4JLoggingModule());
				   String identity = connection.getTenantName() + ":" + connection.getUserName(); // tenantName:userName
				   context = ContextBuilder.newBuilder(connection.getProvider())
						   .endpoint(connection.getUrl()) //"http://141.142.237.5:5000/v2.0/"
						   .credentials(identity, connection.getPassword())
						   .modules(modules)
						   .buildView(ComputeServiceContext.class);
				   compute = context.getComputeService();
				   nova = context.unwrap();
				   zones = nova.getApi().getConfiguredZones();
			   }
		   }
		   catch(NoSuchElementException e)
		   {
			   throw new AmazonServiceException("Cannot connect to OpenStack", e);
		   }
	   }
	   
	protected void disconnect()
	   {
		   if(compute != null)
		   {
			   closeQuietly(compute.getContext());
			   compute = null;
		   }
	   }
	   
	   protected Set<String> getZones() {
		   return zones;
	   }
	   
	   protected FluentIterable<? extends Server> getServersForZone(String zone)
	   {
	     ServerApi serverApi = nova.getApi().getServerApiForZone(zone);
	     return serverApi.listInDetail().concat();
	   }
	
	/** {@inheritDoc} */
	@Override
	public void terminateInstance(String instanceId) {
		Validate.notEmpty(instanceId);
        LOGGER.info(String.format("Terminating instance %s in region %s.", instanceId, region));
        try {
        	openstackClient
            ec2Client().terminateInstances(new TerminateInstancesRequest(Arrays.asList(instanceId)));
        } catch (AmazonServiceException e) {
            if (e.getErrorCode().equals("InvalidInstanceID.NotFound")) {
                throw new NotFoundException("AWS instance " + instanceId + " not found", e);
            }
            throw e;
        }		
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
