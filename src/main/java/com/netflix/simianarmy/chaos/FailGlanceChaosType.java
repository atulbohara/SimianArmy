package com.netflix.simianarmy.chaos;

import com.netflix.simianarmy.MonkeyConfiguration;

public class FailGlanceChaosType extends FailOpenstackEndpointChaosType {
	public FailGlanceChaosType(MonkeyConfiguration config)
	{
		super(config, "glance");
	}
}
