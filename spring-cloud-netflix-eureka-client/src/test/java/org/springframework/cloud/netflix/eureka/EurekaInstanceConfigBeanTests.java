/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.springframework.cloud.netflix.eureka;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.StringUtils;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Dave Syer
 * @author Spencer Gibb
 * @author Ryan Baxter
 * @author Tim Ysewyn
 */
public class EurekaInstanceConfigBeanTests {

	private AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
	private String hostName;
	private String ipAddress;

	@Before
	public void init() throws Exception {
		try (InetUtils utils = new InetUtils(new InetUtilsProperties())) {
			InetUtils.HostInfo hostInfo = utils.findFirstNonLoopbackHostInfo();
			this.hostName = hostInfo.getHostname();
			this.ipAddress = hostInfo.getIpAddress();
		}
	}

	@After
	public void clear() {
		if (this.context != null) {
			this.context.close();
		}
	}

	@Test
	public void basicBinding() {
		TestPropertyValues.of("eureka.instance.appGroupName=mygroup").applyTo(this.context);
		setupContext();
		assertEquals("mygroup", getInstanceConfig().getAppGroupName());
	}

	@Test
	public void nonSecurePort() {
		TestPropertyValues.of("eureka.instance.nonSecurePort:8888").applyTo(this.context);
		setupContext();
		assertEquals(8888, getInstanceConfig().getNonSecurePort());
	}

	@Test
	public void instanceId() {
		TestPropertyValues.of("eureka.instance.instanceId:special").applyTo(this.context);
		setupContext();
		EurekaInstanceConfigBean instance = getInstanceConfig();
		assertEquals("special", instance.getInstanceId());
	}

	@Test
	public void initialHostName() {
		TestPropertyValues.of("eureka.instance.appGroupName=mygroup").applyTo(this.context);
		setupContext();
		if (this.hostName != null) {
			assertEquals(this.hostName, getInstanceConfig().getHostname());
		}
	}

	@Test
	public void refreshHostName() {
		TestPropertyValues.of("eureka.instance.appGroupName=mygroup").applyTo(this.context);
		setupContext();
		ReflectionTestUtils.setField(getInstanceConfig(), "hostname", "marvin");
		assertEquals("marvin", getInstanceConfig().getHostname());
		getInstanceConfig().getHostName(true);
		if (this.hostName != null) {
			assertEquals(this.hostName, getInstanceConfig().getHostname());
		}
	}

	@Test
	public void refreshHostNameWhenSetByUser() {
		TestPropertyValues.of("eureka.instance.appGroupName=mygroup").applyTo(this.context);
		setupContext();
		getInstanceConfig().setHostname("marvin");
		assertEquals("marvin", getInstanceConfig().getHostname());
		getInstanceConfig().getHostName(true);
		assertEquals("marvin", getInstanceConfig().getHostname());
	}

	@Test
	public void initialIpAddress() {
		TestPropertyValues.of("eureka.instance.appGroupName=mygroup").applyTo(this.context);
		setupContext();
		if (this.ipAddress != null) {
			assertEquals(this.ipAddress, getInstanceConfig().getIpAddress());
		}
	}

	@Test
	public void refreshIpAddress() {
		TestPropertyValues.of("eureka.instance.appGroupName=mygroup").applyTo(this.context);
		setupContext();
		ReflectionTestUtils.setField(getInstanceConfig(), "ipAddress", "10.0.0.1");
		assertEquals("10.0.0.1", getInstanceConfig().getIpAddress());
		getInstanceConfig().getHostName(true);
		if (this.ipAddress != null) {
			assertEquals(this.ipAddress, getInstanceConfig().getIpAddress());
		}
	}

	@Test
	public void refreshIpAddressWhenSetByUser() {
		TestPropertyValues.of("eureka.instance.appGroupName=mygroup").applyTo(this.context);
		setupContext();
		getInstanceConfig().setIpAddress("10.0.0.1");
		assertEquals("10.0.0.1", getInstanceConfig().getIpAddress());
		getInstanceConfig().getHostName(true);
		assertEquals("10.0.0.1", getInstanceConfig().getIpAddress());
	}

	@Test
	public void testDefaultInitialStatus() {
		setupContext();
		assertEquals("initialStatus wrong", InstanceStatus.UP,
				getInstanceConfig().getInitialStatus());
	}

	@Test(expected = BeanCreationException.class)
	public void testBadInitialStatus() {
		TestPropertyValues.of("eureka.instance.initial-status:FOO").applyTo(this.context);
		setupContext();
	}

	@Test
	public void testCustomInitialStatus() {
		TestPropertyValues.of("eureka.instance.initial-status:STARTING").applyTo(this.context);
		setupContext();
		assertEquals("initialStatus wrong", InstanceStatus.STARTING,
				getInstanceConfig().getInitialStatus());
	}

	@Test
	public void testPreferIpAddress() throws Exception {
		TestPropertyValues.of("eureka.instance.preferIpAddress:true").applyTo(this.context);
		setupContext();
		EurekaInstanceConfigBean instance = getInstanceConfig();
		assertTrue("Wrong hostname: " + instance.getHostname(),
				getInstanceConfig().getHostname().equals(instance.getIpAddress()));

	}

	@Test
	public void testDefaultVirtualHostName() throws Exception {
		TestPropertyValues.of("spring.application.name:myapp").applyTo(this.context);
		setupContext();
		assertEquals("virtualHostName wrong", "myapp", getInstanceConfig().getVirtualHostName());
		assertEquals("secureVirtualHostName wrong", "myapp", getInstanceConfig().getSecureVirtualHostName());

	}

	@Test
	public void testCustomVirtualHostName() throws Exception {
		TestPropertyValues.of("spring.application.name:myapp", "eureka.instance.virtualHostName=myvirthost",
				"eureka.instance.secureVirtualHostName=mysecurevirthost").applyTo(this.context);
		setupContext();
		assertEquals("virtualHostName wrong", "myvirthost", getInstanceConfig().getVirtualHostName());
		assertEquals("secureVirtualHostName wrong", "mysecurevirthost", getInstanceConfig().getSecureVirtualHostName());

	}

	@Test
	public void testDefaultAppName() throws Exception {
		setupContext();
		assertEquals("default app name is wrong", "unknown", getInstanceConfig().getAppname());
		assertEquals("default virtual hostname is wrong", "unknown", getInstanceConfig().getVirtualHostName());
		assertEquals("default secure virtual hostname is wrong", "unknown", getInstanceConfig().getSecureVirtualHostName());
	}

	@Test
	public void testCustomInstanceId() throws Exception {
		TestPropertyValues.of("eureka.instance.instanceId=myinstance").applyTo(this.context);
		setupContext();
		assertEquals("instance id is wrong", "myinstance", getInstanceConfig().getInstanceId());
	}

	@Test
	public void testCustomInstanceIdWithMetadata() throws Exception {
		TestPropertyValues.of("eureka.instance.metadataMap.instanceId=myinstance").applyTo(this.context);
		setupContext();
		assertEquals("instance id is wrong", "myinstance", getInstanceConfig().getInstanceId());
	}

	@Test
	public void testDefaultInstanceId() throws Exception {
		setupContext();
		assertEquals("default instance id is wrong", null, getInstanceConfig().getInstanceId());
	}

	private void setupContext() {
		this.context.register(PropertyPlaceholderAutoConfiguration.class,
				TestConfiguration.class);
		this.context.refresh();
	}

	private EurekaInstanceConfigBean getInstanceConfig() {
		return this.context.getBean(EurekaInstanceConfigBean.class);
	}

	@Configuration
	@EnableConfigurationProperties
	protected static class TestConfiguration {
		@Autowired
		ConfigurableEnvironment env;
		@Bean
		public EurekaInstanceConfigBean eurekaInstanceConfigBean() {
			EurekaInstanceConfigBean configBean = new EurekaInstanceConfigBean(new InetUtils(new InetUtilsProperties()));
			String springAppName = this.env.getProperty("spring.application.name", "");
			if(StringUtils.hasText(springAppName)) {
				configBean.setSecureVirtualHostName(springAppName);
				configBean.setVirtualHostName(springAppName);
				configBean.setAppname(springAppName);
			}
			return configBean;
		}

	}

}
