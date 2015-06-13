package org.wso2.carbon.esb.jms.inbound.transport.test.utills;

import org.wso2.carbon.automation.extensions.servers.jmsserver.controller.config.JMSBrokerConfiguration;

public class HornetQBrokerConfigurationProvider{
	private static JMSBrokerConfiguration configuration = null;
	private static HornetQBrokerConfigurationProvider instance = new HornetQBrokerConfigurationProvider();

	private HornetQBrokerConfigurationProvider() {
		configuration = this.getHornetQBrokerConfiguration();
	}

	public static HornetQBrokerConfigurationProvider getInstance() {
		return instance;
	}

	public JMSBrokerConfiguration getBrokerConfiguration() {
		return configuration;
	}

	private JMSBrokerConfiguration getHornetQBrokerConfiguration() {
		JMSBrokerConfiguration jmsBrokerConfiguration = new JMSBrokerConfiguration();
		jmsBrokerConfiguration.setInitialNamingFactory("org.jnp.interfaces.NamingContextFactory");
		jmsBrokerConfiguration.setProviderURL("jnp://localhost:1099");
		return jmsBrokerConfiguration;
	}
}

