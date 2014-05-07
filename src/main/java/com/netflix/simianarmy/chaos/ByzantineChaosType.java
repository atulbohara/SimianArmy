package com.netflix.simianarmy.chaos;

import java.io.IOException;
import java.net.URL;

import org.jclouds.ssh.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.netflix.simianarmy.MonkeyConfiguration;

public class ByzantineChaosType extends ScriptChaosType {
	private static final Logger LOGGER = LoggerFactory.getLogger(ScriptChaosType.class);
	
	public ByzantineChaosType(MonkeyConfiguration config)
	{
		super(config, "Byzantine");
	}
	
	@Override
    public void apply(ChaosInstance instance) {
		LOGGER.info("Copying byzantine binary to instance {}", instance.getInstanceId());

        SshClient ssh = instance.connectSsh();

        String filename = getKey().toLowerCase();
        URL url = Resources.getResource(ScriptChaosType.class, "/scripts/" + filename);
        String script;
        try {
            script = Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Error reading script resource", e);
        }

        ssh.put("/tmp/" + filename, script);
        
        super.apply(instance);
	}
}
