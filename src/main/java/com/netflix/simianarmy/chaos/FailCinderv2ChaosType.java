package com.netflix.simianarmy.chaos;

import com.netflix.simianarmy.MonkeyConfiguration;

public class FailCinderv2ChaosType extends FailOpenstackEndpointChaosType {
	public FailCinderv2ChaosType(MonkeyConfiguration config)
	{
		super(config, "cinderv2");
	}
}
