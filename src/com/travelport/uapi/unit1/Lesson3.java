package com.travelport.uapi.unit1;

import java.util.Iterator;
import java.util.List;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.ClientImpl;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import com.travelport.schema.air_v18_0.*;
import com.travelport.schema.rail_v12_0.RailPricingSolution;
import com.travelport.service.air_v18_0.AirFaultMessage;
import com.travelport.service.air_v18_0.AirLowFareSearchAsynchPortType;

public class Lesson3 {
	public static void main(String[] argv) {
		//pick a pair of cities that might be better to go via rail and have
		//low cost carrier options
		String origin = "GLA", destination = "LON";
		LowFareSearchAsynchReq req = new LowFareSearchAsynchReq();
		
		
		//a zero timeout means to never time-out... these are in millis
		//setTimeouts(30 * 1000/*30 secs to connect*/,5 * 60 * 1000 /*wait up to five mins fon read*/);

		//this creates the request parameters... and doesn't care if the request
		//is synchronous or asynch
		createLowFareSearchWithRail(req, origin,
				destination, 30, 32);
		
		try {
			System.out.println("can take up to one minute for first results...");
			LowFareSearchAsynchRsp rsp = WSDLService.getLowFareSearchAsynch().service(req);
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
	 * We are going to do some munging with the underlying transport to set
	 * the timeouts since this request may take a long time.
	 */
	public static void setTimeouts(int connectTimeout, int receiveTimeout) {
		//we get the low level object because we need to set some properties
		//on it... note that these objects are singletons inside the 
		//WSDL service object because 1) they might as well be and 
		//2) they are slow to create... so we are modifying the same
		//object that will be returned to the code in the main() above
		AirLowFareSearchAsynchPortType lowFareAsynch = WSDLService.getLowFareSearchAsynch();
		//extract the "client" object inside the communications framework
		Client cl = ClientProxy.getClient(lowFareAsynch);
		//now get the conduit built on HTTP
		HTTPConduit http = (HTTPConduit) cl.getConduit();
		//now create a policy for this HTTP conduit
		HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
		httpClientPolicy.setConnectionTimeout(connectTimeout);
		httpClientPolicy.setAllowChunking(false);
		httpClientPolicy.setReceiveTimeout(receiveTimeout);
		http.setClient(httpClientPolicy);
		
		ClientImpl impl = (ClientImpl)cl;
		impl.setSynchronousTimeout(receiveTimeout);
		
	}
	
	/**
	 * Update the low fare request in preparation for doing an asynchronous
	 * request.
	 * 
	 * @param origin  
	 * @param dest
	 * @param departureDaysInFuture  number of days from "now"
	 * @param returnDaysInFuture number of days from "now"
	 * @return
	 */
	public static void createLowFareSearchWithRail(BaseLowFareSearchReq request, String originAirportcode, String destAirportCode,
			int departureDaysInFuture, int returnDaysInFuture) {
		
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
	}
}
