package com.travelport.uapi.unit1;

import java.math.BigInteger;

import com.travelport.schema.air_v18_0.*;
import com.travelport.schema.air_v18_0.AirLegModifiers.PreferredCabins;
import com.travelport.schema.air_v18_0.AirSearchModifiers.PreferredProviders;
import com.travelport.schema.common_v12_0.PointOfSale;
import com.travelport.schema.common_v15_0.*;

/**
 * Namespace for static functions that can manipulate an air request in
 * useful ways.
 *
 */
public class AirReq {

	/**
	 * As of v18 of air wsdl, the point of sale is required.  This sets
	 * the point of sale just contain the GDS and the application name.
	 * @param req the request to modify
	 * @param appName the name to send as application name
	 */
	public static void addPointOfSale(BaseReq req, String appName ) {
		BillingPointOfSaleInfo posInfo = new BillingPointOfSaleInfo();
		posInfo.setOriginApplication(appName);
		req.setBillingPointOfSaleInfo(posInfo);
	}

	/**
	 * Add the search passengers to the request.  We only add ADT (adult)
	 * passengers and this only works for LowFareSearchReq objects.
	 * @param request the req to add the passenger parameter to
	 * @param n number of adults to put in the requset
	 */
	public static void addAdultPassengers(BaseLowFareSearchReq request, int n) {
		for (int i = 0; i < n; ++i) {
			SearchPassenger adult = new SearchPassenger();
			adult.setCode("ADT");
			request.getSearchPassenger().add(adult);
		}
	}



	/**
	 * Create a leg for a search based on simple origin and destination 
	 * between two airports.
	 * 
	 * @param originAirportCode
	 * @param destAirportCode
	 * @return  
	 */
	public static SearchAirLeg createLeg(String originAirportCode,
			String destAirportCode) {
		TypeSearchLocation originLoc = new TypeSearchLocation();
		TypeSearchLocation destLoc = new TypeSearchLocation();

		// airport objects are just wrappers for their codes
		Airport origin = new Airport(), dest = new Airport();
		origin.setCode(originAirportCode);
		dest.setCode(destAirportCode);
		
		// search locations can be things other than airports but we are using
		// the airport version...
		originLoc.setAirport(origin);
		destLoc.setAirport(dest);

		return createLeg(originLoc, destLoc);
	}
	/**
	 * Create a leg based on the (more general) TypeSearchLocation which can
	 * be a variety of different things (such as non-iata named rail stations).
	 * 
	 * @param originLoc starting point of leg
	 * @param destLoc endpoint of leg
	 * @return
	 */
	public static SearchAirLeg createLeg(TypeSearchLocation originLoc,
			TypeSearchLocation destLoc) {
		SearchAirLeg leg = new SearchAirLeg();

		// add the origin and dest to the leg
		leg.getSearchDestination().add(destLoc);
		leg.getSearchOrigin().add(originLoc);

		
		return leg;
	}
	
	/**
	 * Make a search location based on a city or airport code (city is 
	 * preferred to airport in a conflict) and set the search radius to
	 * 50mi.
	 */
	public static TypeSearchLocation createLocationNear(String cityOrAirportCode) {
		TypeSearchLocation result = new TypeSearchLocation();
		
		//city
		CityOrAirport place = new CityOrAirport();
		place.setCode(cityOrAirportCode);
		place.setPreferCity(false);
		result.setCityOrAirport(place);

		//distance
		Distance dist = new Distance();
		dist.setUnits("mi");
		dist.setValue(BigInteger.valueOf(50));
		result.setDistance(dist);
		
		return result;
	}
	 
	
	/**
	 * Mmodify a search leg to use economy class of service as preferred.
	 * 
	 * @param leg the leg to modify
	 */
	public static void addEconomyPreferred(SearchAirLeg leg) {
		AirLegModifiers modifiers = new AirLegModifiers();
		PreferredCabins cabins = new PreferredCabins();
		CabinClass econ = new CabinClass();
		econ.setType(TypeCabinClass.ECONOMY);

		cabins.setCabinClass(econ);
		modifiers.setPreferredCabins(cabins);
		leg.setAirLegModifiers(modifiers);
	}

	/**
	 * Modify a search leg based on a departure date
	 * 
	 * @param leg the leg to modify
	 * @param departureDate the departure date in YYYY-MM-dd
	 */
	public static void addDepartureDate(SearchAirLeg leg, String departureDate) {
		// flexible time spec is flexible in that it allows you to say
		// days before or days after
		TypeFlexibleTimeSpec noFlex = new TypeFlexibleTimeSpec();
		noFlex.setPreferredTime(departureDate);
		leg.getSearchDepTime().add(noFlex);
	}

	/**
	 * Search modifiers to create, usually a GDS code plus optionally 
	 * RCH (Helper.RAIL_PROVIDER) or ACH (Helper.LOW_COST_PROVIDER).
	 * 
	 * @param providerCode  one or more provider codes (zero will not work!)
	 * @return the modifiers object
	 */
	public static AirSearchModifiers createModifiersWithProviders(String ... providerCode) {
		AirSearchModifiers modifiers = new AirSearchModifiers();
		PreferredProviders providers = new PreferredProviders();
		for (int i=0; i<providerCode.length;++i) {
			Provider p = new Provider();
			// set the code for the provider
			p.setCode(providerCode[i]);
			// can be many providers, but we just use one
			providers.getProvider().add(p);
		}
		modifiers.setPreferredProviders(providers);
		return modifiers;
	}
	
}
