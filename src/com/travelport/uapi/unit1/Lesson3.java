package com.travelport.uapi.unit1;

import java.util.Iterator;
import java.util.List;

import com.travelport.schema.air_v18_0.*;
import com.travelport.schema.rail_v12_0.RailPricingSolution;
import com.travelport.service.air_v18_0.AirFaultMessage;

public class Lesson3 {
	public static void main(String[] argv) {
		//pick a pair of cityies that might be better to go via rail
		String origin = "GLA", destination = "LON";
		LowFareSearchReq req = createLowFareSearchWithRail(origin,
				destination, 30, 32);
		
		try {
			LowFareSearchRsp rsp = Helper.WSDLService.getLowFareSearch().service(req);
			Helper.AirSegmentMap allAirSegments = null;
			Helper.RailJourneyMap allRailJourneys = null;
			Helper.RailSegmentMap allRailSegments = null;
			
			if (rsp.getAirSegmentList()!=null) {
				allAirSegments =
					Helper.createAirSegmentMap(rsp.getAirSegmentList().getAirSegment());
			}
			if (rsp.getRailSegmentList()!=null) {
				allRailJourneys =
						Helper.createRailJourneyMap(rsp.getRailJourneyList().getRailJourney());
				allRailSegments =
						Helper.createRailSegmentMap(rsp.getRailSegmentList().getRailSegment());
			}
			
			//walk all the solutions and create a printable itinerary for each one
			//then print them out
			List<AirPricingSolution> airs = rsp.getAirPricingSolution();
			for (Iterator<AirPricingSolution> iterator = airs.iterator(); iterator.hasNext();) {
				AirPricingSolution soln = (AirPricingSolution) iterator.next();
				PrintableItinerary i = new PrintableItinerary(soln, allAirSegments, destination);
				System.out.println(i+"\n-------------------");
			}
			//print rail solutions
			List<RailPricingSolution> rails = rsp.getRailPricingSolution();
			for (Iterator<RailPricingSolution> iterator = rails.iterator(); iterator.hasNext();) {
				RailPricingSolution soln = (RailPricingSolution) iterator.next();
				PrintableItinerary i = new PrintableItinerary(soln, allRailJourneys, allRailSegments);
				System.out.println(i+"\n-------------------");
			}
		} catch (AirFaultMessage e) {
			System.err.println("Fault trying send request to travelport:" +
					e.getMessage());
		}
		
	}
	
	/**
	 * Create the low fare request in preparation for doing an asynchronous
	 * request.
	 * 
	 * @param origin  
	 * @param dest
	 * @param departureDaysInFuture  number of days from "now"
	 * @param returnDaysInFuture number of days from "now"
	 * @return
	 */
	public static LowFareSearchReq createLowFareSearchWithRail(String originAirportcode, String destAirportCode,
			int departureDaysInFuture, int returnDaysInFuture) {
		LowFareSearchReq request = new LowFareSearchReq();
		
		//add in the tport branch code
		request.setTargetBranch(System.getProperty("travelport.targetBranch"));
		
		//set the providers (including real and low cost) via a search modifier
		AirSearchModifiers modifiers = AirReq.createModifiersWithProviders(
				System.getProperty("travelport.gds"), Helper.LOW_COST_PROVIDER,
				Helper.RAIL_PROVIDER);
		request.setAirSearchModifiers(modifiers);

		//need a POS as of v18_0
		AirReq.addPointOfSale(request, "tutorial-unit1-lesson3");

		//we need to create a search leg but we do with some slack plus we use
		//the city code for london
		SearchAirLeg outbound = AirReq.createLeg(
				AirReq.createLocationNear("GLA"), 
				AirReq.createLocationNear("LON"));
		
		AirReq.addDepartureDate(outbound, Helper.daysInFuture(departureDaysInFuture));
		AirReq.addEconomyPreferred(outbound);

		//coming back, again something near these...
		SearchAirLeg ret = AirReq.createLeg(
				AirReq.createLocationNear("LON"), 
				AirReq.createLocationNear("GLA"));
		AirReq.addDepartureDate(ret, Helper.daysInFuture(returnDaysInFuture));
		AirReq.addEconomyPreferred(ret);

		//put them in the request
		List<SearchAirLeg> legs = request.getSearchAirLeg();
		legs.add(outbound);
		legs.add(ret);
		
		//one adult passenger
		AirReq.addAdultPassengers(request, 1);
		
		return request;
	}
}
