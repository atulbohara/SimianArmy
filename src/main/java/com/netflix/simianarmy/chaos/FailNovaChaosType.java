package com.netflix.simianarmy.chaos;

import com.netflix.simianarmy.MonkeyConfiguration;

public class FailNovaChaosType extends FailOpenstackEndpointChaosType {
	public FailNovaChaosType(MonkeyConfiguration config)
	{
		super(config, "nova");
	}
}
