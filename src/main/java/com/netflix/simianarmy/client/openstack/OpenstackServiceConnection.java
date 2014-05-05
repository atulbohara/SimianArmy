package com.netflix.simianarmy.client.openstack;

import com.netflix.simianarmy.MonkeyConfiguration;

/**
 * Contains metadata for the OpenstackClient
 */
public class OpenstackServiceConnection {
   private String userName = null;
   private String tenantName = null;
   private String password = null;
   private String provider = null;
   private String url = null;
   private String zone = null;
   
   public OpenstackServiceConnection(MonkeyConfiguration config) {
	   userName = config.getStr("simianarmy.client.openstack.userName");
	   url = config.getStr("simianarmy.client.openstack.url");
	   provider = config.getStrOrElse("simianarmy.client.openstack.provider", "openstack-nova");
	   tenantName = config.getStr("simianarmy.client.openstack.tenantName");
	   password = config.getStr("simianarmy.client.openstack.password");
	   zone = config.getStrOrElse("simianarmy.client.openstack.zone", "regionOne");
   }
   
   public String getZone() {
	   return zone;
   }
   
   public void setZone(String zone) {
	   this.zone = zone;
   }
   
   public String getUserName() {
	   return userName;
   }
   
   public void setUserName(String userName) {
	   this.userName = userName;
   }
   
   public String getPassword() {
	   return password;
   }
   
   public void setPassword(String password) {
	   this.password = password;
   }
   
   public String getTenantName() {
	   return tenantName;
   }
   
   public void setTenantName(String tenantName) {
	   this.tenantName = tenantName;
   }
   
   public String getUrl() {
	   return url;
   }
   
   public void setUrl(String url) {
	   this.url = url;
   }
   
   public String getProvider() {
	   return provider;
   }
   
   public void setProvider(String provider) {
	   this.provider = provider;
   }
}
