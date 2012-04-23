package com.travelport.uapi.unit1;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.ws.BindingProvider;

import com.travelport.service.air_v18_0.*;
import com.travelport.service.system_v8_0.*;

/**
 * Convenience class for getting access to the WSDL services without needing
 * to mess with the parameters and such.  This hides some CXF specific things
 * that most people don't care about.
 */
public class WSDLService {
	static protected AirLowFareSearchPortType lowFareSearch;
	static protected AirLowFareSearchAsynchPortType lowFareSearchAsynch;
	static protected AirAvailabilitySearchPortType availabilitySearch;
	static protected AirPricePortType price;
	static protected SystemPingPortType ping;
	static protected SystemInfoPortType info;
	static protected SystemTimePortType time;

	static protected SystemService systemService ;
	static protected AirService airService ;

	static protected String URLPREFIX = "file:///Users/iansmith/tport-workspace/uapijava/";
	static protected String SYSTEM_WSDL = "wsdl/system_v8_0/System.wsdl";
	static protected String AIR_WSDL = "wsdl/air_v18_0/Air.wsdl";

	static protected String USERNAME_PROP = "travelport.username";
	static protected String PASSWORD_PROP = "travelport.password";
	static protected String GDS_PROP = "travelport.gds";
	static protected String TARGET_BRANCH = "travelport.targetBranch";

	// these endpoint parameters vary based on which region you are
	// in...check your travelport sign up to see which url you should use...
	static protected String SYSTEM_ENDPOINT = "https://emea.universal-api.travelport.com/B2BGateway/connect/uAPI/SystemService";
	static protected String AIR_ENDPOINT = "https://emea.universal-api.travelport.com/B2BGateway/connect/uAPI/AirService";

	/**
	 * Get access to the low fare object -- synchonous version.
	 * 
	 * @return the port for low fare search
	 */
	public static AirLowFareSearchPortType getLowFareSearch() {
		if (lowFareSearch != null) {
			return lowFareSearch;
		}
		URL url = getURLForWSDL(AIR_WSDL);
		checkProperties();
		if (airService==null) {
			airService = new AirService(url);
		}
		lowFareSearch = airService.getAirLowFareSearchPort();
		addParametersToProvider((BindingProvider) lowFareSearch,
				AIR_ENDPOINT);
		return lowFareSearch;
	}
	/**
	 * Get access to the low fare object -- asynchonous version.
	 * 
	 * @return the port for low fare search
	 */
	public static AirLowFareSearchAsynchPortType getLowFareSearchAsynch() {
		if (lowFareSearchAsynch != null) {
			return lowFareSearchAsynch;
		}
		URL url = getURLForWSDL(AIR_WSDL);
		checkProperties();
		if (airService==null) {
			airService = new AirService(url);
		}
		lowFareSearchAsynch = airService.getAirLowFareSearchAsynchPort();
		addParametersToProvider((BindingProvider) lowFareSearchAsynch,
				AIR_ENDPOINT);
		return lowFareSearchAsynch;
	}
	/**
	 * Get access to the availability
	 * 
	 * @return the port for low fare search
	 */

	public static AirAvailabilitySearchPortType getAvailabilitySearch() {
		if (availabilitySearch != null) {
			return availabilitySearch;
		}
		URL url = getURLForWSDL(AIR_WSDL);
		checkProperties();
		if (airService==null) {
			airService = new AirService(url);
		}
		availabilitySearch = airService.getAirAvailabilitySearchPort();
		addParametersToProvider((BindingProvider) availabilitySearch,
				AIR_ENDPOINT);
		return availabilitySearch;
	}
	/**
	 * Get access to the availability
	 * 
	 * @return the port for low fare search
	 */

	public static AirPricePortType getPrice() {
		if (price != null) {
			return price;
		}
		URL url = getURLForWSDL(AIR_WSDL);
		checkProperties();
		if (airService==null) {
			airService = new AirService(url);
		}
		price = airService.getAirPricePort();
		addParametersToProvider((BindingProvider) price,
				AIR_ENDPOINT);
		return price;
	}

	/**
	 * Get access to the ping object.
	 * 
	 * @return the port for low fare search
	 */
	public static SystemPingPortType getPing() {
		if (ping != null) {
			return ping;
		}
		checkProperties();
		if (systemService==null) {
			URL url = getURLForWSDL(SYSTEM_WSDL);
			systemService = new SystemService(url);
		}
		ping = systemService.getSystemPingPort();
		addParametersToProvider((BindingProvider) ping,
				SYSTEM_ENDPOINT);

		return ping;
	}
	/**
	 * Get access to the time object.
	 * 
	 * @return the port for low fare search
	 */
	public static SystemTimePortType getTime() {
		if (time != null) {
			return time;
		}
		checkProperties();
		if (systemService==null) {
			URL url = getURLForWSDL(SYSTEM_WSDL);
			systemService = new SystemService(url);
		}
		time = systemService.getSystemtimePort();
		addParametersToProvider((BindingProvider) time,
				SYSTEM_ENDPOINT);

		return time;
	}
	/**
	 * Get access to the info object.
	 * 
	 * @return the port for low fare search
	 */
	public static SystemInfoPortType getInfo() {
		if (info != null) {
			return info;
		}
		checkProperties();
		if (systemService==null) {
			URL url = getURLForWSDL(SYSTEM_WSDL);
			systemService = new SystemService(url);
		}
		info = systemService.getSystemInfoPort();
		addParametersToProvider((BindingProvider) info,
				SYSTEM_ENDPOINT);

		return info;
	}


	/**
	 * Check that all the properties we get through the environment are at
	 * least present.
	 */
	public static void checkProperties() {
		if ((System.getProperty(USERNAME_PROP) == null)
				|| (System.getProperty(PASSWORD_PROP) == null)
				|| (System.getProperty(GDS_PROP) == null)
				|| (System.getProperty(TARGET_BRANCH) == null)) {
			throw new RuntimeException(
					"One or more of your properties "
							+ "has not been set properly for you to access the travelport "
							+ "uAPI.  Check your command line arguments or eclipse "
							+ "run configuration for these properties:"
							+ USERNAME_PROP + "," + PASSWORD_PROP + ","
							+ GDS_PROP + "," + TARGET_BRANCH);
		}
	}

	/**
	 * This checks that the path given by a URL is well formed as URL
	 * despite the fact that this must be a pointer to a file.
	 * 
	 * @param wsdlFileInThisProject
	 *            name of the wsdl file, with dir prefix
	 * @return the URL that points to the wsdl
	 */
	public static URL getURLForWSDL(String wsdlFileInThisProject) {
		try {
			URL url = new URL(URLPREFIX + wsdlFileInThisProject);
			return url;
		} catch (MalformedURLException e) {
			throw new RuntimeException(
					"The URL to access the WSDL was not "
							+ "well-formed! Check the URLPREFIX value in the class "
							+ "WSDLService in the file Helper.java.  We tried to "
							+ "to use this url:\n" + URLPREFIX + AIR_WSDL);

		}
	}

	/**
	 * Add the necessary gunk to the BindingProvider to make it work right
	 * with an authenticated SOAP service.
	 * 
	 * @param provider
	 *            the provider (usually this a port object also)
	 * @param endpoint
	 *            the string that is the internet-accessible place to access
	 *            the service
	 */

	public static void addParametersToProvider(BindingProvider provider,
			String endpoint) {
		provider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
				endpoint);
		provider.getRequestContext().put(BindingProvider.USERNAME_PROPERTY,
				System.getProperty("travelport.username"));
		provider.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY,
				System.getProperty("travelport.password"));
	}

}
