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
import org.jclouds.compute.Utils;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.NodeMetadataBuilder;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.NovaAsyncApi;
import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.jclouds.openstack.nova.v2_0.features.ImageApi;
import org.jclouds.openstack.nova.v2_0.extensions.VolumeApi;
import org.jclouds.rest.RestContext;
import org.jclouds.ssh.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.ModifyInstanceAttributeRequest;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
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
                            context = ContextBuilder.newBuilder(connection.getProvider())
                                            .endpoint(connection.getUrl()) //"http://141.142.237.5:5000/v2.0/"
                                            .credentials(identity, connection.getPassword())
                                            .modules(modules)
                                            .buildView(ComputeServiceContext.class);
                            compute = context.getComputeService();
                            nova = context.unwrap();
                            zones = nova.getApi().getConfiguredZones();
                    }
            } catch(NoSuchElementException e) {
                    throw new AmazonServiceException("Cannot connect to OpenStack", e);
            }
    }

    /**
     * Disconnect from the Openstack services
     */
    protected void disconnect() {
        if(compute != null) {
            closeQuietly(compute.getContext());
            compute = null;
        }
	}

	/** {@inheritDoc} */
	@Override
	public void terminateInstance(String instanceId) {
		Validate.notEmpty(instanceId);
        connect();
        try {
        	nova.getApi().getServerApiForZone(connection.getZone()).stop(instanceId);
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
        VolumeApi v = (VolumeApi)nova.getApi().getVolumeExtensionForZone(connection.getZone());
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
        ImageApi v = (ImageApi)nova.getApi().getImageApiForZone(connection.getZone());
        v.delete(imageId);
        disconnect();
	}

    /** {@inheritDoc} */
	@Override
	public void createTagsForResources(Map<String, String> keyValueMap,
			String... resourceIds) {
		// TODO Auto-generated method stub
		
	}

    /** {@inheritDoc} */
	@Override
	public List<String> listAttachedVolumes(String instanceId,
			boolean includeRoot) {
		// TODO Auto-generated method stub
		return null;
	}

    /** {@inheritDoc} */
	@Override
	public void detachVolume(String instanceId, String volumeId, boolean force) {
		// TODO Auto-generated method stub
		
	}

	/** {@inheritDoc} */
	@Override
	public ComputeService getJcloudsComputeService() {
		return compute;
	}

    /** {@inheritDoc} */
	@Override
	public String getJcloudsId(String instanceId) {
		return connection.getZone() + "/" + instanceId;
	}

    /** {@inheritDoc} */
	@Override
	public String findSecurityGroup(String instanceId, String groupName) {
		// TODO Auto-generated method stub
		return null;
	}

    /** {@inheritDoc} */
	@Override
	public String createSecurityGroup(String instanceId, String groupName,
			String description) {
		connect();
		

        AmazonEC2 ec2Client = ec2Client();
        CreateSecurityGroupRequest request = new CreateSecurityGroupRequest();
        request.setGroupName(name);
        request.setDescription(description);
        request.setVpcId(vpcId);

        LOGGER.info(String.format("Creating OpenStack security group %s.", groupName));

        CreateSecurityGroupResult result = ec2Client.createSecurityGroup(request);
        return result.getGroupId();
	}

    /** {@inheritDoc} */
	@Override
	public void setInstanceSecurityGroups(String instanceId,
			List<String> groupIds) {
		Validate.notEmpty(instanceId);
		connect();
        LOGGER.info(String.format("Removing all security groups from instance %s in zone %s.", instanceId, connection.getZone()));
        try {
        	
            
        } catch (AmazonServiceException e) {
            if (e.getErrorCode().equals("InvalidInstanceID.NotFound")) {
                throw new NotFoundException("AWS instance " + instanceId + " not found", e);
            }
            throw e;
        }
		
	}

    /** {@inheritDoc} */
    @Override
    public boolean canChangeInstanceSecurityGroups(String instanceId) {
        // TODO Auto-generated method stub
        return false;
    }

	/** {@inheritDoc} */
    @Override
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
    }

}
