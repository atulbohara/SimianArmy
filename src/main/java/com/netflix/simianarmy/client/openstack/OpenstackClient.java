package com.netflix.simianarmy.client.openstack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.io.IOException;

import org.apache.commons.lang.Validate;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.http.HttpRequest;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.openstack.keystone.v2_0.domain.Access;
import org.jclouds.openstack.keystone.v2_0.domain.Endpoint;
import org.jclouds.openstack.keystone.v2_0.domain.Service;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.extensions.VolumeAttachmentApi;
import org.jclouds.openstack.nova.v2_0.domain.SecurityGroup;
import org.jclouds.openstack.nova.v2_0.domain.ServerWithSecurityGroups;
import org.jclouds.openstack.nova.v2_0.domain.VolumeAttachment;
import org.jclouds.openstack.cinder.v1.CinderApi;
import org.jclouds.openstack.cinder.v1.features.VolumeApi;
import org.jclouds.openstack.nova.v2_0.extensions.SecurityGroupApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;
import com.google.common.io.Closeables;
import com.google.inject.Module;
import com.netflix.simianarmy.CloudClient;
import com.netflix.simianarmy.NotFoundException;
import com.netflix.simianarmy.client.aws.AWSClient;

public class OpenstackClient extends AWSClient implements CloudClient {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OpenstackClient.class);
	private final OpenstackServiceConnection connection;
	
	private ComputeService compute = null;
	private NovaApi nova = null;
	
	private ComputeServiceContext context = null;
	private Access access;
	private CinderApi cinder = null;

	/**
	 * Create the specific Client from the given connection information.
	 * @param the connection parameters
	 */
	public OpenstackClient(OpenstackServiceConnection conn) {
		super(conn.getZone());
		this.connection = conn;
	}
	
    /**
     * Connect to the Openstack services
     * @throws AmazonServiceException
     */
    protected void connect() throws AmazonServiceException {
        try {
            if(compute == null) {
                Iterable<Module> modules = ImmutableSet.<Module> of(new SLF4JLoggingModule());
                String identity = connection.getTenantName() + ":" + connection.getUserName(); // tenantName:userName
                ContextBuilder cb = ContextBuilder.newBuilder(connection.getProvider())
                                .endpoint(connection.getUrl()) //"http://141.142.237.5:5000/v2.0/"
                                .credentials(identity, connection.getPassword())
                                .modules(modules);
                context = cb.buildView(ComputeServiceContext.class);
                compute = context.getComputeService();
                nova = cb.buildApi(NovaApi.class);
                cinder = cb.buildApi(CinderApi.class);
            }
        } catch(NoSuchElementException e) {
            throw new AmazonServiceException("Cannot connect to OpenStack", e);
        }
    }

    /**
     * Disconnect from the Openstack services
     */
    protected void disconnect() {
        if(nova != null) {
        	try {
	            Closeables.close(nova, true);
	            nova = null;
        	}
        	catch(IOException e) {
        		LOGGER.error("Error disconnecting nova: " + e.getMessage());
        	}
        }
        if(cinder != null) {
        	try {
        		Closeables.close(cinder, true);
        	}
        	catch(IOException e) {
        		LOGGER.error("Error disconnecting cinder: " + e.getMessage());
        	}
        }
	}

	/** {@inheritDoc} */
	@Override
	public void terminateInstance(String instanceId) {
		Validate.notEmpty(instanceId);
        connect();
        try {
        	nova.getServerApiForZone(connection.getZone()).stop(instanceId);
        } catch (UnsupportedOperationException e) {
            throw new NotFoundException("Instance " + instanceId + " not found", e);
        }
        disconnect();
	}

    /** {@inheritDoc} */
	@Override
	public void deleteAutoScalingGroup(String asgName) {
		Validate.notEmpty(asgName);
		LOGGER.error("No AutoScalingGroups in OpenStack... Better wait for Heat to be released!");
	}

    /** {@inheritDoc} */
	@Override
	public void deleteLaunchConfiguration(String launchConfigName) {
		Validate.notEmpty(launchConfigName);
        LOGGER.error("No AutoScalingGroups in OpenStack... Better wait for Heat to be released!");
	}

    /** {@inheritDoc} */
	@Override
	public void deleteVolume(String volumeId) {
		Validate.notEmpty(volumeId);
        connect();
        VolumeApi v = (VolumeApi)nova.getVolumeExtensionForZone(connection.getZone());
        v.delete(volumeId);
        disconnect();
	}

    /** {@inheritDoc} */
	@Override
	public void deleteSnapshot(String snapshotId) {
		Validate.notEmpty(snapshotId);
		connect();
		cinder.getSnapshotApiForZone(connection.getZone()).delete(snapshotId);
		disconnect();
	}

    /** {@inheritDoc} */
	@Override
	public void deleteImage(String imageId) {
		Validate.notEmpty(imageId);
        connect();
        nova.getImageApiForZone(connection.getZone()).delete(imageId);
        disconnect();
	}

    /** {@inheritDoc} */
	@Override
	public void createTagsForResources(Map<String, String> keyValueMap, String... resourceIds) {
		LOGGER.error("No tagging in OpenStack yet...");		
	}

    /** {@inheritDoc} */
	@Override
	public List<String> listAttachedVolumes(String instanceId, boolean includeRoot) {
		// Returns list of volume IDs that are attached to server instanceId.
		// includeRoot doesn't do anything right now because I'm not sure how Openstack handles root volumes on attached storage
		Validate.notEmpty(instanceId);
		List<String> out = new ArrayList<String>();
		VolumeAttachmentApi volumeAttachmentApi = nova.getVolumeAttachmentExtensionForZone(connection.getZone()).get();

		for(VolumeAttachment volumeAttachment: volumeAttachmentApi.listAttachmentsOnServer(instanceId))	{
			out.add(volumeAttachment.getVolumeId());
		}
		
		return out;
	}

    /** {@inheritDoc} */
	@Override
	public void detachVolume(String instanceId, String volumeId, boolean force) {
		//Detaches the volume. Openstack doesn't seem to have a force option for detaching, so the force parameter will be unused.
		Validate.notEmpty(instanceId);
		Validate.notEmpty(volumeId);
		connect();
		VolumeAttachmentApi volumeAttachmentApi = nova.getVolumeAttachmentExtensionForZone(connection.getZone()).get();
		boolean result = volumeAttachmentApi.detachVolumeFromServer(volumeId, instanceId);
		if(!result) {
			LOGGER.error("Error detaching volume " + volumeId + " from " + instanceId);
		}
	}

	/** {@inheritDoc} */
	@Override
	public ComputeService getJcloudsComputeService() {
		return compute;
	}

    /** {@inheritDoc} */
	@Override
	public String getJcloudsId(String instanceId) {
		Validate.notEmpty(instanceId);
		return connection.getZone() + "/" + instanceId;
	}

    /** {@inheritDoc} */
	@Override
	public String findSecurityGroup(String instanceId, String groupName) {
		Validate.notEmpty(instanceId);
		Validate.notEmpty(groupName);
		String id = null;
		connect();
		SecurityGroupApi v = nova.getSecurityGroupExtensionForZone(connection.getZone()).get();
		for(SecurityGroup group: v.list())
		{
			if(group.getName() == groupName)
			{
				id = group.getId();
				break;
			}
		}
		disconnect();
		return id;
	}

    /** {@inheritDoc} */
	@Override
	public String createSecurityGroup(String instanceId, String groupName, String description) {
		Validate.notEmpty(instanceId);
		Validate.notEmpty(groupName);
		Validate.notEmpty(description);
		connect();
		SecurityGroupApi v = nova.getSecurityGroupExtensionForZone(connection.getZone()).get();
        LOGGER.info(String.format("Creating OpenStack security group %s.", groupName));

        SecurityGroup result = v.createWithDescription(groupName, description);
        //Add security group to the instance
        addSecurityGroupToInstanceByName(instanceId, groupName);
        
        disconnect();
        return result.getId();
	}

    /** {@inheritDoc} */
	@Override
	public void setInstanceSecurityGroups(String instanceId, List<String> groupIds) {
		Validate.notEmpty(instanceId);
		Validate.notEmpty(groupIds);
		connect();
		
		//Get all security groups for instance
		ServerWithSecurityGroups serverWithSG = nova.getServerWithSecurityGroupsExtensionForZone(connection.getZone()).get().get(instanceId);
		//Remove all security groups from the instance
		for(String secGroup: serverWithSG.getSecurityGroupNames()) {
			removeSecurityGroupFromInstanceByName(instanceId, secGroup);
		}
		//Add specified groups to the instance
		
		for(String groupId: groupIds) {
			addSecurityGroupToInstanceById(instanceId, groupId);
		}
	}
	
	//This assumes you have already done a call to connect()
	private void removeSecurityGroupFromInstanceByName(String instanceId, String groupName)
	{
		String endpoint = "";
		for (Service service: access) {
	    	  //System.out.println(" Service = " + service.getName());
	    	  if(service.getName().startsWith("nova")) {
	    		  endpoint = ((Endpoint)service.toArray()[0]).getPublicURL().toString();
	    		  break;
	    	  }
		}
		HashMultimap<String, String> headers = HashMultimap.create();
		headers.put("Accept", "application/json");
		headers.put("Content-Type", "application/json");
		headers.put("X-Auth-Token", access.getToken().getId());
		HttpRequest request = HttpRequest.builder().method("POST").endpoint(endpoint + "/servers/" + instanceId + "/action").headers(headers).payload(ByteSource.wrap(("{\"removeSecurityGroup\": {\"name\": \"" + groupName + "\"}}").getBytes())).build();
		context.utils().http().invoke(request);
	}
	
	//This assumes you have already done a call to connect()
	private void addSecurityGroupToInstanceById(String instanceId, String groupId)
	{
		SecurityGroupApi v = nova.getSecurityGroupExtensionForZone(connection.getZone()).get();
		String groupName = v.get(groupId).getName();
		addSecurityGroupToInstanceByName(instanceId, groupName);
	}
	
	//This assumes you have already done a call to connect()
	private void addSecurityGroupToInstanceByName(String instanceId, String groupName)
	{
		String endpoint = "";
		for (Service service: access) {
	    	  //System.out.println(" Service = " + service.getName());
	    	  if(service.getName().startsWith("nova")) {
	    		  endpoint = ((Endpoint)service.toArray()[0]).getPublicURL().toString();
	    		  break;
	    	  }
		}
		HashMultimap<String, String> headers = HashMultimap.create();
		headers.put("Accept", "application/json");
		headers.put("Content-Type", "application/json");
		headers.put("X-Auth-Token", access.getToken().getId());
		HttpRequest request = HttpRequest.builder().method("POST").endpoint(endpoint + "/servers/" + instanceId + "/action").headers(headers).payload(ByteSource.wrap(("{\"addSecurityGroup\": {\"name\": \"" + groupName + "\"}}").getBytes())).build();
		context.utils().http().invoke(request);
	}
	
    /** {@inheritDoc} */
    @Override
    public boolean canChangeInstanceSecurityGroups(String instanceId) {
    	LOGGER.info("This feature requires Heat to fail. Returning true.");
        return true;
    }
    
    public NovaApi getNovaApi()
    {
    	return nova;
    }
    
    public OpenstackServiceConnection getServiceConnection()
    {
    	return connection;
    }
    
	/** {@inheritDoc} */
    /*@Override
    public SshClient connectSsh(String instanceId, LoginCredentials credentials) {
        ComputeService computeService = getJcloudsComputeService();

        String jcloudsId = getJcloudsId(instanceId);
        NodeMetadata node = getJcloudsNode(computeService, jcloudsId);

        node = NodeMetadataBuilder.fromNodeMetadata(node).credentials(credentials).build();

        Utils utils = computeService.getContext().getUtils();
        SshClient ssh = utils.sshForNode().apply(node);

        ssh.connect();

        return ssh;
    }
    
    private NodeMetadata getJcloudsNode(ComputeService computeService, String jcloudsId) {
        // Work around a jclouds bug / documentation issue...
        // TODO: Figure out what's broken, and eliminate this function

        // This should work (?):
        // Set<NodeMetadata> nodes = computeService.listNodesByIds(Collections.singletonList(jcloudsId));

        Set<NodeMetadata> nodes = Sets.newHashSet();
        for (ComputeMetadata n : computeService.listNodes()) {
            if (jcloudsId.equals(n.getId())) {
                nodes.add((NodeMetadata) n);
            }
        }

        if (nodes.isEmpty()) {
            LOGGER.warn("Unable to find jclouds node: {}", jcloudsId);
            for (ComputeMetadata n : computeService.listNodes()) {
                LOGGER.info("Did find node: {}", n);
            }
            throw new IllegalStateException("Unable to find node using jclouds: " + jcloudsId);
        }
        NodeMetadata node = Iterables.getOnlyElement(nodes);
        return node;
    }*/

}
