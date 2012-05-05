package com.travelport.tutorial.support;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.ws.BindingProvider;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;

import com.travelport.schema.vehicle_v17_0.VehicleSearchAvailabilityReq;
import com.travelport.service.air_v18_0.*;
import com.travelport.service.hotel_v17_0.HotelMediaLinksServicePortType;
import com.travelport.service.hotel_v17_0.HotelSearchServicePortType;
import com.travelport.service.hotel_v17_0.HotelService;
import com.travelport.service.system_v8_0.*;
import com.travelport.service.vehicle_v17_0.VehicleCancelServicePortType;
import com.travelport.service.vehicle_v17_0.VehicleSearchServicePortType;
import com.travelport.service.vehicle_v17_0.VehicleService;

/**
 * Convenience class for getting access to the WSDL services without needing
 * to mess with the parameters and such.  This hides some CXF specific things
 * that most people don't care about.
 */
public class WSDLService {
	static protected AirLowFareSearchPortType lowFareSearch;
	static protected AirLowFareSearchAsynchPortType lowFareSearchAsynch;
	static protected AirAvailabilitySearchPortType availabilitySearch;
	static protected AirRetrieveLowFareSearchPortType retrieve;
	static protected AirPricePortType price;
	static protected AirCreateReservationPortType createResv;
	
	static protected SystemPingPortType ping;
	static protected SystemInfoPortType info;
	static protected SystemTimePortType time;

    static protected HotelSearchServicePortType hotelSearch;
    static protected HotelMediaLinksServicePortType mediaLinks;
    
    static protected VehicleSearchServicePortType vehicleSearch;
	
	static protected SystemService systemService ;
    static protected AirService airService ;
    static protected HotelService hotelService ;
    static protected VehicleService vehicleService ;

	static protected String URLPREFIX = "file:///Users/iansmith/tport-workspace/uapijava/";
	static protected String SYSTEM_WSDL = "wsdl/system_v8_0/System.wsdl";
    static protected String AIR_WSDL = "wsdl/air_v18_0/Air.wsdl";
    static protected String HOTEL_WSDL = "wsdl/hotel_v17_0/Hotel.wsdl";
    static protected String VEHICLE_WSDL = "wsdl/vehicle_v17_0/Vehicle.wsdl";

	static protected String USERNAME_PROP = "travelport.username";
	static protected String PASSWORD_PROP = "travelport.password";
	static protected String GDS_PROP = "travelport.gds";
	static protected String TARGET_BRANCH = "travelport.targetBranch";

	// these endpoint parameters vary based on which region you are
	// in...check your travelport sign up to see which url you should use...
	static protected String SYSTEM_ENDPOINT = "https://emea.universal-api.travelport.com/B2BGateway/connect/uAPI/SystemService";
    static protected String AIR_ENDPOINT = "https://emea.universal-api.travelport.com/B2BGateway/connect/uAPI/AirService";
    static protected String HOTEL_ENDPOINT = "https://emea.universal-api.travelport.com/B2BGateway/connect/uAPI/HotelService";
    static protected String VEHICLE_ENDPOINT = "https://emea.universal-api.travelport.com/B2BGateway/connect/uAPI/VehicleService";

	/**
	 * Get access to the low fare object -- synchonous version.
	 * 
	 * @return the port for low fare search
	 */
	public static AirLowFareSearchPortType getLowFareSearch(boolean showXML) {
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
		if (showXML) {
			showXML(lowFareSearch);
		}
		return lowFareSearch;
	}
	/**
	 * Get access to the low fare object -- asynchonous version.
	 * 
	 * @return the port for low fare search
	 */
	public static AirLowFareSearchAsynchPortType getLowFareSearchAsynch(boolean showXML) {
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
		if (showXML) {
			showXML(lowFareSearchAsynch);
		}
		return lowFareSearchAsynch;
	}
	/**
	 * Get access to the availability
	 * 
	 * @return the port for low fare search
	 */

	public static AirAvailabilitySearchPortType getAvailabilitySearch(boolean showXML) {
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
		if (showXML) {
			showXML(availabilitySearch);
		}
		return availabilitySearch;
	}
	/**
	 * Get access to the pricing port.
	 * 
	 * @param showXML true to turn on display of the XML traffic
	 * @return the port pricing
	 */

	public static AirPricePortType getPrice(boolean showXML) {
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
		if (showXML) {
			showXML(price);
		}
		return price;
	}

	/**
	 * Get access to the ping object.
	 * 
	 * @return the port for ping service
	 */
	public static SystemPingPortType getPing(boolean showXML) {
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
		if (showXML) {
			showXML(ping);
		}
		return ping;
	}
	/**
	 * Get access to the time object.
	 * 
	 * @return the port the time 
	 */
	public static SystemTimePortType getTime(boolean showXML) {
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
		if (showXML) {
			showXML(time);
		}
		return time;
	}
	/**
	 * Get access to the info object.
	 * 
	 * @return the port for info service
	 */
	public static SystemInfoPortType getInfo(boolean showXML) {
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
		if (showXML) {
			showXML(info);
		}
		return info;
	}
	/**
	 * Get access to the asynch port further retreival object.
	 * 
	 * @return the port for getting more info from an asynch request
	 */
	public static AirRetrieveLowFareSearchPortType getRetrieve(boolean showXML) {
		if (retrieve != null) {
			return retrieve;
		}
		checkProperties();
		if (airService==null) {
			URL url = getURLForWSDL(AIR_WSDL);
			airService = new AirService(url);
		}
		retrieve = airService.getAirRetrieveLowFareSearchPort();
		addParametersToProvider((BindingProvider) retrieve,
				AIR_ENDPOINT);
		if (showXML) {
			showXML(retrieve);
		}
		return retrieve;
	}

	/**
	 * Get access to the port for creating reservations
	 * 
	 * @return the create reservation port
	 */
	public static AirCreateReservationPortType getCreateResv(boolean showXML) {
		if (createResv != null) {
			return createResv;
		}
		checkProperties();
		if (airService==null) {
			URL url = getURLForWSDL(AIR_WSDL);
			airService = new AirService(url);
		}
		createResv = airService.getAirCreateReservationPort();
		addParametersToProvider((BindingProvider) createResv,
				AIR_ENDPOINT);
		if (showXML) {
			showXML(createResv);
		}
		return createResv;
	}

   /**
     * Get access to the port for searching for hotels.
     * 
     * @return the create reservation port
     */
    public static HotelSearchServicePortType getHotelSearch(boolean showXML) {
        if (hotelSearch != null) {
            return hotelSearch;
        }
        checkProperties();
        if (hotelService==null) {
            URL url = getURLForWSDL(HOTEL_WSDL);
            hotelService = new HotelService(url);
        }
        hotelSearch = hotelService.getHotelSearchServicePort();
        addParametersToProvider((BindingProvider) hotelSearch,
                HOTEL_ENDPOINT);
        if (showXML) {
            showXML(hotelSearch);
        }
        return hotelSearch;
    }
    /**
     * Get access to the service that can provide the media for a hotel.
     * 
     * @return the create reservation port
     */
    public static HotelMediaLinksServicePortType getHotelMedia(boolean showXML) {
        if (mediaLinks != null) {
            return mediaLinks;
        }
        checkProperties();
        if (hotelService==null) {
            URL url = getURLForWSDL(HOTEL_WSDL);
            hotelService = new HotelService(url);
        }
        mediaLinks = hotelService.getHotelMediaLinksServicePort();
        addParametersToProvider((BindingProvider) mediaLinks,
                HOTEL_ENDPOINT);
        if (showXML) {
            showXML(mediaLinks);
        }
        return mediaLinks;
    }

    /**
     * Get access to the service that can look for a vehicle.
     * 
     * @return the create reservation port
     */
    public static VehicleSearchServicePortType getVehicleSearch(boolean showXML) {
        if (vehicleSearch != null) {
            return vehicleSearch;
        }
        checkProperties();
        if (vehicleService==null) {
            URL url = getURLForWSDL(VEHICLE_WSDL);
            vehicleService = new VehicleService(url);
        }
        vehicleSearch = vehicleService.getVehicleSearchServicePort();
        addParametersToProvider((BindingProvider) vehicleSearch,
                VEHICLE_ENDPOINT);
        if (showXML) {
            showXML(vehicleSearch);
        }
        return vehicleSearch;
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
	/**
	 * Turn on the debugging interceptors that display the XML on the console.
	 * @param port must be a WSDL port, despite the lack of typing on this parameter
	 */
	public static void showXML(Object port) {
		Client cl = ClientProxy.getClient(port);
		
		LoggingInInterceptor in = new LoggingInInterceptor();
		in.setPrettyLogging(true);
		LoggingOutInterceptor out = new LoggingOutInterceptor();
		out.setPrettyLogging(true);
		
        cl.getInInterceptors().add(in);
        cl.getOutInterceptors().add(out);
		
	}

}
