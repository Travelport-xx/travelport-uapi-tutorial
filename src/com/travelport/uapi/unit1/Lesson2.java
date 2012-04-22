package com.travelport.uapi.unit1;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.annotation.processing.RoundEnvironment;
import javax.xml.ws.BindingProvider;

import com.travelport.schema.air_v15_0.*;
import com.travelport.schema.air_v15_0.AirLegModifiers.PreferredCabins;
import com.travelport.schema.air_v15_0.AirSearchModifiers.PreferredProviders;
import com.travelport.schema.common_v12_0.*;
import com.travelport.service.air_v15_0.AirFaultMessage;
import com.travelport.service.air_v15_0.AirLowFareSearchPortType;
import com.travelport.service.air_v15_0.AirLowFareSearchService;

public class Lesson2 {
	//
	// PROGRAM ENTRY POINT
	//
	public static void main(String[] argv) {
		
		try {
			//make the request... paris to bangkok
			String from ="CDG",to="BKK";
			
			//staying a week two months from now.. roundtrip
			LowFareSearchRsp rsp = lowcostSearch(from, to, 
					daysInFuture(60), daysInFuture(67));
			
			//decode all the itin data
			List<Helper.PrintableItinerary> itins = decodeItineraries(rsp, to);
			
			//print the results
			for (Iterator<Helper.PrintableItinerary> iterator = itins.iterator(); iterator.hasNext();) {
				Helper.PrintableItinerary itin = (Helper.PrintableItinerary) iterator.next();
				System.out.println("----------------------------");
				System.out.println(itin.toString());
			}
			//display what the response said the response time was in secs
			System.out.println("\n\nResponse time was "+
					rsp.getResponseTime().divide(BigInteger.valueOf(1000))+
					" seconds.");
		} catch (AirFaultMessage e) {
			System.err.println("Error making low cost search request:"+e.getMessage());
		}
	}

	/**
	 * Take a result object and return a set of printable itineraries. The
	 * caller should have already checked that the result contains data
	 * suitable for display and not errors.
	 */
	public static List<Helper.PrintableItinerary> decodeItineraries(LowFareSearchRsp rsp,
			String destinationAirport) {
		//construct a map with all the segments and their keys
		Helper.SegmentMap segmentMap = new Helper.SegmentMap();
		
		List<AirSegment> segments = rsp.getAirSegmentList().getAirSegment();
		for (Iterator<AirSegment> iterator = segments.iterator(); iterator.hasNext();) {
			AirSegment airSegment = (AirSegment) iterator.next();
			segmentMap.add(airSegment);
		}
		//walk all the solutions and create an itinerary for that
		List<Helper.PrintableItinerary> result = new ArrayList<Helper.PrintableItinerary>();
	
		List<AirPricingSolution> solutions = rsp.getAirPricingSolution();
		for (Iterator<AirPricingSolution> iterator = solutions.iterator(); iterator.hasNext();) {
			AirPricingSolution soln = (AirPricingSolution) iterator.next();
			result.add(new Helper.PrintableItinerary(soln, segmentMap, destinationAirport));
		}
		
		return result;
	}
	/**
	 * Do a round trip search for one adult traveller.  This returns the raw
	 * result object to the caller.
	 * 
	 * @param origin airport code
	 * @param dest airport code
	 * @param dateOut date of departure in yyyy-MM-dd format
	 * @param dateBack date of return in yyyy-MM-dd format
	 * @return raw response object
	 */
	public static LowFareSearchRsp  lowcostSearch(String origin,
			String dest, String dateOut, String dateBack) throws AirFaultMessage{

		LowFareSearchReq request = new LowFareSearchReq();
		LowFareSearchRsp response;
		request.setTargetBranch(System.getProperty("travelport.targetBranch"));
		
		//set the GDS via a search modifier
		AirSearchModifiers modifiers = Lesson2.gdsAsModifier(System.getProperty("travelport.gds"));
		request.setAirSearchModifiers(modifiers);

		//R/T journey
		SearchAirLeg outbound = Lesson2.createLeg(origin, dest);
		Lesson2.addDepartureDate(outbound, dateOut);
		Lesson2.addEconomyPreferred(outbound);

		//coming back
		SearchAirLeg ret = Lesson2.createLeg(dest, origin);
		Lesson2.addDepartureDate(ret, dateBack);
		//put traveller in econ
		Lesson2.addEconomyPreferred(ret);

		//put the legs in the request
		List<SearchAirLeg> legs = request.getSearchAirLeg();
		legs.add(outbound);
		legs.add(ret);
		
		//1 adult travelling
		Lesson2.addAdultPassengers(request, 1);
		response = Helper.WSDLService.getLowFareSearch().service(request);

		//print out any messages that the GDS sends back
		for (Iterator<ResponseMessage> iterator = response.getResponseMessage().iterator(); iterator.hasNext();) {
			ResponseMessage message = (ResponseMessage) iterator.next();
			System.out.println("MESSAGE:"+message.getProviderCode()+" ["+message.getType() 
					+"] "+message.getValue());
		}
		
		return response;
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

	// add some number of adult passengers to a search request
	public static void addAdultPassengers(LowFareSearchReq request, int n) {
		for (int i = 0; i < n; ++i) {
			SearchPassenger adult = new SearchPassenger();
			adult.setCode("ADT");
			request.getSearchPassenger().add(adult);
		}
	}


}
