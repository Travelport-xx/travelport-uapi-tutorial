package com.travelport.uapi.unit1;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.xml.ws.BindingProvider;

import com.travelport.schema.air_v18_0.AirSegment;
import com.travelport.schema.air_v18_0.FlightDetails;
import com.travelport.schema.rail_v12_0.RailJourney;
import com.travelport.schema.rail_v12_0.RailSegment;
import com.travelport.service.air_v18_0.*;
import com.travelport.service.system_v8_0.*;


public class Helper {

	/**
	 * Convenience class for getting access to the WSDL services without needing
	 * to mess with the parameters and such.
	 */
	public static class WSDLService {
		static protected AirLowFareSearchPortType lowFareSearch;
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
		static protected String LOW_FARE_ENDPOINT = "https://emea.universal-api.travelport.com/B2BGateway/connect/uAPI/AirLowFareSearchService";
		static protected String AVAILABILITY_ENDPOINT = "https://emea.universal-api.travelport.com/B2BGateway/connect/uAPI/AirAvailabilitySearchService";
		static protected String PRICE_ENDPOINT = "https://emea.universal-api.travelport.com/B2BGateway/connect/uAPI/AirPriceService";
		static protected String SYSTEM_ENDPOINT = "https://emea.universal-api.travelport.com/B2BGateway/connect/uAPI/SystemService";
		static protected String AIR_ENDPOINT = "https://emea.universal-api.travelport.com/B2BGateway/connect/uAPI/AirService";
	
		/**
		 * Get access to the low fare object.
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
			provider.getRequestContext().put(
					BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
			provider.getRequestContext().put(BindingProvider.USERNAME_PROPERTY,
					System.getProperty("travelport.username"));
			provider.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY,
					System.getProperty("travelport.password"));
		}

	}
	
	/**
	 * Utility class for building a map that knows about all the segments in the
	 * response.
	 */
	public static class AirSegmentMap extends HashMap<String, AirSegment> {
		public void add(AirSegment segment) {
			put(segment.getKey(), segment);
		}
	}
	/**
	 * Utility class for building a map that knows all the flight details 
	 * objects and can look them up by their key.
	 */
	public static class FlightDetailsMap extends HashMap<String, FlightDetails> {
		public void add(FlightDetails detail) {
			put(detail.getKey(), detail);
		}
	}

	//this is the format we SEND to travelport
	public static SimpleDateFormat searchFormat = new SimpleDateFormat(
			"yyyy-MM-dd");

	// return a date that is n days in future
	public static String daysInFuture(int n) {
		Date now = new Date(), future;
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(now);
		calendar.add(Calendar.DATE, n);
		future = calendar.getTime();
		return searchFormat.format(future);
	}

	/**
	 * Build the map from references to flight details to to real flight details.
	 */
	public static FlightDetailsMap createFlightDetailsMap(
			List<FlightDetails> details) {
		FlightDetailsMap result = new FlightDetailsMap();
		for (Iterator<FlightDetails> iterator = details.iterator(); iterator.hasNext();) {
			FlightDetails deet = (FlightDetails) iterator.next();
			result.add(deet);
		}
		return result;
	}

	/**
	 * Take a air segment list and construct a map of all the segments into
	 * a segment map.  This makes other parts of the work easier.
	 */
	public static AirSegmentMap createAirSegmentMap(
			List<AirSegment> segments) {
		//construct a map with all the segments and their keys
		AirSegmentMap segmentMap = new AirSegmentMap();
		
		for (Iterator<AirSegment> iterator = segments.iterator(); iterator.hasNext();) {
			AirSegment airSegment = (AirSegment) iterator.next();
			segmentMap.add(airSegment);
		}

		return segmentMap;
	}

	public static final String RAIL_PROVIDER = "RCH";
	public static final String LOW_COST_PROVIDER = "ACH";

	/**
	 * Convenience class for keeping a mapping from id to rail journey.
	 * @author iansmith
	 *
	 */
	public static class RailJourneyMap extends HashMap<String, RailJourney>{
		public void add(RailJourney j) {
			put(j.getKey(), j);
		}
	}
	/**
	 * Take a list of rail journeys and put them all into a map.
	 * @param r  list of rail journeys
	 * @return the built map from keys to rail segments
	 */
	public static RailJourneyMap createRailJourneyMap(
			List<RailJourney> r) {
		
		RailJourneyMap result = new Helper.RailJourneyMap();
		for (Iterator<RailJourney> iterator = r.iterator(); iterator.hasNext();) {
			RailJourney seg = (RailJourney) iterator.next();
			result.add(seg);
		}
		return result;
	}

	/**
	 * Convenience class for keeping a mapping from id to rail segment.
	 *
	 */
	public static class RailSegmentMap extends HashMap<String, RailSegment>{
		public void add(RailSegment j) {
			put(j.getKey(), j);
		}
	}
	/**
	 * Take a list of rail segments and put them all into a map.
	 * @param r  list of rail segments
	 * @return the built map from keys to rail segments
	 */
	public static RailSegmentMap createRailSegmentMap(
			List<RailSegment> r) {
		
		RailSegmentMap result = new Helper.RailSegmentMap();
		for (Iterator<RailSegment> iterator = r.iterator(); iterator.hasNext();) {
			RailSegment seg = (RailSegment) iterator.next();
			result.add(seg);
		}
		return result;
	}

	//this is not *quite* a travel port date because tport puts a colon in
	//the timezone which is not ok with RFC822 timezones
	public static SimpleDateFormat tportResultFormat =
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	//turn a travel port date from a response back into a java object
	//not as easy to do because java gets confused by the iso 8601 timezone
	public static Date dateFromISO8601(String iso) {
		try {
			String noColon = iso.substring(0,26) +iso.substring(27);
			return tportResultFormat.parse(noColon);
		} catch (ParseException e) {
			throw new RuntimeException("Really unlikely, but it looks like "+
			"travelport is not using ISO dates anymore! "+e.getMessage());
		}
	}

}
