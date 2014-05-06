package com.netflix.simianarmy.chaos;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;

import org.jclouds.ssh.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.netflix.simianarmy.CloudClient;
import com.netflix.simianarmy.MonkeyConfiguration;
import com.netflix.simianarmy.client.openstack.OpenstackClient;

public class FailOpenstackEndpointChaosType extends ChaosType {
    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(BlockAllNetworkTrafficChaosType.class);
    private final String endpointType;
    /**
     * Constructor.
     *
     * @param config
     *            Configuration to use
     */
    public FailOpenstackEndpointChaosType(MonkeyConfiguration config, String endpointType) {
    	super(config, "Fail" + endpointType);
    	this.endpointType = endpointType;
    }

    /**
     * We can apply the strategy iff the endpoint type exists.
     */
    @Override
    public boolean canApply(ChaosInstance instance) {
        OpenstackClient cloudClient = (OpenstackClient) instance.getCloudClient();

        return cloudClient.getEndpoints().containsKey(endpointType);
    }

    /**
     * Null-routes the openstack endpoint.
     */
    @Override
    public void apply(ChaosInstance instance) {
    	OpenstackClient cloudClient = (OpenstackClient) instance.getCloudClient();
        String instanceId = instance.getInstanceId();
        SshClient ssh = instance.connectSsh();
        for(String endpoint: cloudClient.getEndpoints().get(endpointType))
        {
        	try {
        		URL endpointURL = new URL(endpoint);
        		InetAddress address = InetAddress.getByName(endpointURL.getHost());
        		ssh.exec("sudo iptables -A OUTPUT -d " + address.getHostAddress() + " -p tcp -m tcp --dport " + endpointURL.getPort() + " -j DROP");
        		ssh.exec("sudo iptables -A OUTPUT -d " + address.getHostAddress() + " -p udp -m udp --dport " + endpointURL.getPort() + " -j DROP");

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
        }
    }

}
