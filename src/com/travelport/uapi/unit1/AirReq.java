package com.travelport.uapi.unit1;

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
		PointOfSale pos = new PointOfSale();
		pos.setProviderCode(System.getProperty("travelport.gds"));
		pos.setSourceLocation(pos.sourceLocation());
		req.setBillingPointOfSaleInfo(posInfo);
	}
	// create a leg based on simple origin and destination between two airports
	// sets the search to prefer economy
	public static SearchAirLeg createLeg(String originAirportCode,
			String destAirportCode) {
		SearchAirLeg leg = new SearchAirLeg();
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

		// add the origin and dest to the leg
		leg.getSearchDestination().add(destLoc);
		leg.getSearchOrigin().add(originLoc);

		return leg;
	}
	
	// modify a search leg to use economy class of service as preferred
	public static void addEconomyPreferred(SearchAirLeg leg) {
		AirLegModifiers modifiers = new AirLegModifiers();
		PreferredCabins cabins = new PreferredCabins();
		CabinClass econ = new CabinClass();
		econ.setType(TypeCabinClass.ECONOMY);

		cabins.setCabinClass(econ);
		modifiers.setPreferredCabins(cabins);
		leg.setAirLegModifiers(modifiers);
	}

	// modify a search leg based on a departure date
	public static void addDepartureDate(SearchAirLeg leg, String departureDate) {
		// flexible time spec is flexible in that it allows you to say
		// days before or days after
		TypeFlexibleTimeSpec noFlex = new TypeFlexibleTimeSpec();
		noFlex.setPreferredTime(departureDate);
		leg.getSearchDepTime().add(noFlex);
	}

	// search modifiers
	public static AirSearchModifiers gdsAsModifier(String gdsCode) {
		AirSearchModifiers modifiers = new AirSearchModifiers();
		PreferredProviders providers = new PreferredProviders();
		Provider myGDS = new Provider();
		// set the code for the provider
		myGDS.setCode(gdsCode);
		// can be many providers, but we just use one
		providers.getProvider().add(myGDS);
		modifiers.setPreferredProviders(providers);
		return modifiers;
	}
	
}
