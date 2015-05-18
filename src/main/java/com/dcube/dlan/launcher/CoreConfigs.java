package com.dcube.dlan.launcher;

import java.io.InputStream;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CoreConfigs be used to access the configuration of core instance.
 *  
 **/
public class CoreConfigs {

	public static final String VERSION_INFO = "0.1";
	/** default configuration path */
	public static final String DEFAULT_CONFIG = "core-config.properties";
	
	private static PropertiesConfiguration _coreconfig = null;
	
	private static Logger LOGGER = LoggerFactory.getLogger(CoreConfigs.class);
	
	static{
		try {
			InputStream is = CoreConfigs.class.getClassLoader().getResourceAsStream(DEFAULT_CONFIG);
			if(is != null)
				_coreconfig = new PropertiesConfiguration(DEFAULT_CONFIG);
		} catch (ConfigurationException e) {
			if(LOGGER.isDebugEnabled())
				LOGGER.debug("Fail to load configuration properties file",e);
			else
				LOGGER.warn("Fail to load configuration properties file");
		}	
	}
	
	/**
	 * Get the network port of remote command listener
	 **/
	public static int getNetcmdPort(){
		
		if( _coreconfig != null){
			return _coreconfig.getInt("netcmd.port", 13721);
		}
		return 13721;
	}
}
