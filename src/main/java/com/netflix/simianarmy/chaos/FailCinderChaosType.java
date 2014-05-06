package com.netflix.simianarmy.chaos;

import com.netflix.simianarmy.MonkeyConfiguration;

public class FailCinderChaosType extends FailOpenstackEndpointChaosType {
	public FailCinderChaosType(MonkeyConfiguration config)
	{
		super(config, "cinder");
	}
}
