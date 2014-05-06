package com.netflix.simianarmy.chaos;

import com.netflix.simianarmy.MonkeyConfiguration;

public class FailKeystoneChaosType extends FailOpenstackEndpointChaosType {
	public FailKeystoneChaosType(MonkeyConfiguration config)
	{
		super(config, "nova");
	}
}
