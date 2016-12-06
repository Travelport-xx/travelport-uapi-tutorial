package com.travelport.uapi.unit1;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.ClientImpl;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import com.travelport.schema.air_v18_0.*;
import com.travelport.schema.common_v15_0.PointOfSale;
import com.travelport.schema.common_v15_0.ResponseMessage;
import com.travelport.schema.rail_v12_0.RailPricingSolution;
import com.travelport.service.air_v18_0.AirFaultMessage;
import com.travelport.service.air_v18_0.AirLowFareSearchAsynchPortType;
import com.travelport.tutorial.support.WSDLService;

public class Lesson3 {
	public static final String MYAPP = "tut";

	public static void main(String[] argv) {
		// pick a pair of cities that might be better to go via rail and have
		// low cost carrier options
		String origin = "GLA", destination = "LGW";
		LowFareSearchAsynchReq req = new LowFareSearchAsynchReq();
		// this creates the request parameters... and doesn't care if the
		// request is synchronous or asynch
		createLowFareSearchWithRail(req, origin, destination, 7, 9);

		try {
			System.out.println("waiting for first response from a provider...");
			WSDLService.airShopAsync.showXML(true);
			LowFareSearchAsynchRsp lowCostRsp = WSDLService.airShopAsync.get().service(req);
			HashMap<String, Long> partMap = new HashMap<String, Long>();

			List<AsyncProviderSpecificResponse> specificResponses = lowCostRsp
					.getAsyncProviderSpecificResponse();

			// print out what we got from initial response... this is to print
			// the summary for all providers and set up the partMap for use in
			// our loop below
			for (Iterator<AsyncProviderSpecificResponse> specIter = specificResponses
					.iterator(); specIter.hasNext();) {
				AsyncProviderSpecificResponse asynchRsp = (AsyncProviderSpecificResponse) specIter
						.next();
				partMap.put(asynchRsp.getProviderCode(), asynchRsp
						.getTotalParts().longValue());
				System.out.println("Provider " + asynchRsp.getProviderCode()
						+ " has a total of " + asynchRsp.getTotalParts()
						+ " parts");
			}

			// prepare for the loop... we print first and ask for the response
			// second we have to setup the values before entering the first time
			String searchId = lowCostRsp.getSearchId();
			String currentProvider = lowCostRsp.getProviderCode();
			long currentPart = lowCostRsp.getPartNumber().longValue();
			AirSearchRsp rsp = lowCostRsp; // so we can print it out
			while (partMap.isEmpty() == false) {

				System.out.println("++++++++++++++++++++\n"
						+ "Response is from provider " + currentProvider + ":"
						+ " part " + currentPart + " of "
						+ partMap.get(currentProvider));

				printSomeExampleResults(destination, rsp, 2);

				long total = partMap.get(currentProvider);
				if (total == currentPart) {
					// finished with that one
					partMap.remove(currentProvider);
					// more providers?
					if (partMap.isEmpty()) {
						continue; // just get out of the loop
					}
					// change to next provider from the partMap
					currentProvider = partMap.keySet().iterator().next();
					currentPart = 1;
				} else {
					// more parts left on this provider
					currentPart++;
				}
				// start the retreival of either the next part or the 1st part
				// from the next provider

				// just to show we can do something else while we wait
				try {
					System.out.println("Sleeping 5 secs before trying to "
							+ "request part " + currentPart + " from "
							+ currentProvider);
					Thread.sleep(5 * 1000);
				} catch (InterruptedException ignored) {
					/* wont happen */
				}
				// sleep is finished, run the request for more data...
				RetrieveLowFareSearchReq retrieve = new RetrieveLowFareSearchReq();
				retrieve.setSearchId(searchId);
				retrieve.setProviderCode(currentProvider);
				retrieve.setPartNumber(BigInteger.valueOf(currentPart));
				AirReq.addPointOfSale(retrieve, MYAPP);
				WSDLService.airRetrieve.get();
				rsp = WSDLService.airRetrieve.get().service(retrieve);
				checkForErrorMessage(rsp);
			}
		} catch (AirFaultMessage e) {
			System.err.println("Fault trying send request to travelport:"
					+ e.getMessage());
		}

	}

	/**
	 * Convenience routing for checking if there was an "expected" error from a
	 * provider. If the error is not "expected" puke out with a runtime
	 * exception
	 * 
	 * @param rsp
	 *            the response object with the possible error
	 * @return true if an error response message was found
	 */
	public static void checkForErrorMessage(AirSearchRsp rsp) {
		boolean die = false;

		List<ResponseMessage> msgs = rsp.getResponseMessage();
		if (msgs.size() != 0) {
			for (Iterator<ResponseMessage> iter = msgs.iterator(); iter
					.hasNext();) {

				ResponseMessage msg = (ResponseMessage) iter.next();
				// no data available for a particular date or city pair?
				if ((msg.getType().equals("Error"))
						&& (msg.getCode().equals(BigInteger.ZERO))) {
					if (msg.getProviderCode().equals(Helper.LOW_COST_PROVIDER)) {
						System.out
								.println("No data available low cost provider?");
						continue;
					}
				}

				if (msg.getType().equals("Error")) {
					die = false;
				}
				String supplier = "";
				if (msg.getSupplierCode() != null) {
					supplier = "[" + msg.getSupplierCode() + "] ";
				}
				System.out.print("Response Message From Provider "
						+ msg.getProviderCode() + supplier + " : "
						+ msg.getType() + " : " + msg.getValue() + " -- ");
				System.out.println("Error Code = " + msg.getCode());
			}
		}
		if (die) {
			throw new RuntimeException("Unable to handle error from provider!");
		}
	}

	/**
	 * Just a way to display a few of the results returned in a response. There
	 * are usually many solutions so we just display up to numberSamplesToShow.
	 * Note the type here of the response is AirSearchRsp so it can be passed
	 * either RetreiveLowFareSearchRsp or AsynchLowFareSearchRsp
	 * 
	 * @param destination
	 *            the destination airport is needed for air solutions
	 * @param rsp
	 *            the response object
	 * @param numberSamplesToShow
	 *            the number of samples to show in this display
	 */
	public static void printSomeExampleResults(String destination,
			AirSearchRsp rsp, int numberSamplesToShow) {
		Helper.AirSegmentMap allAirSegments = null;
		Helper.RailJourneyMap allRailJourneys = null;
		Helper.RailSegmentMap allRailSegments = null;

		// saves time if you don't care about seeing the examples
		if (numberSamplesToShow == 0) {
			return;
		}

		if (rsp.getAirSegmentList() != null) {
			allAirSegments = Helper.createAirSegmentMap(rsp.getAirSegmentList()
					.getAirSegment());
		}
		if (rsp.getRailSegmentList() != null) {
			allRailJourneys = Helper.createRailJourneyMap(rsp
					.getRailJourneyList().getRailJourney());
			allRailSegments = Helper.createRailSegmentMap(rsp
					.getRailSegmentList().getRailSegment());
		}

		int airTotal = rsp.getAirPricingSolution().size();
		int railTotal = rsp.getRailPricingSolution().size();
		int count = 0;

		System.out.println("Total number solutions: " + airTotal + " air and "
				+ railTotal + " rail");

		// walk all the solutions and create a printable itinerary for each one
		// then print them out
		List<AirPricingSolution> airs = rsp.getAirPricingSolution();
		for (Iterator<AirPricingSolution> iterator = airs.iterator(); iterator
				.hasNext();) {
			System.out.println("Example AIR Solution " + (count + 1) + " of "
					+ airTotal);
			AirPricingSolution soln = (AirPricingSolution) iterator.next();
			PrintableItinerary i = new PrintableItinerary(soln, allAirSegments,
					destination);
			System.out.println(i + "\n-------------------");
			++count;
			if (count == numberSamplesToShow) {
				break;
			}
		}
		// print rail solutions
		List<RailPricingSolution> rails = rsp.getRailPricingSolution();
		for (Iterator<RailPricingSolution> iterator = rails.iterator(); iterator
				.hasNext();) {
			System.out.println("Example RAIL solution " + (count + 1) + " of "
					+ railTotal);
			RailPricingSolution soln = (RailPricingSolution) iterator.next();
			PrintableItinerary i = new PrintableItinerary(soln,
					allRailJourneys, allRailSegments);
			System.out.println(i + "\n-------------------");
			++count;
			if (count == numberSamplesToShow) {
				break;
			}
		}
	}
	/**
	 * Update the low fare request in preparation for doing an asynchronous
	 * request for low cost search.
	 * 
	 * @param origin
	 * @param dest
	 * @param departureDaysInFuture
	 *            number of days from "now"
	 * @param returnDaysInFuture
	 *            number of days from "now"
	 * @return
	 */
	public static void createLowFareSearchWithRail(
			BaseLowFareSearchReq request, String originAirportcode,
			String destAirportCode, int departureDaysInFuture,
			int returnDaysInFuture) {

		// add in the tport branch code
		request.setTargetBranch(System.getProperty("travelport.targetBranch"));

		// set the providers (including real and low cost) via a search modifier
		AirSearchModifiers modifiers = AirReq.createModifiersWithProviders(
				System.getProperty("travelport.gds"), 
				Helper.RAIL_PROVIDER,
				Helper.LOW_COST_PROVIDER);
		request.setAirSearchModifiers(modifiers);

		// need a POS as of v18_0
		AirReq.addPointOfSale(request, MYAPP);

		// we need to create a search leg but we do with some slack plus we use
		// the city code for london
		SearchAirLeg outbound = AirReq.createLeg(originAirportcode, destAirportCode);

		AirReq.addDepartureDate(outbound,
				Helper.daysInFuture(departureDaysInFuture));
		AirReq.addEconomyPreferred(outbound);

		// coming back, again something near these...
		SearchAirLeg ret = AirReq.createLeg(destAirportCode, originAirportcode);
		AirReq.addDepartureDate(ret, Helper.daysInFuture(returnDaysInFuture));
		AirReq.addEconomyPreferred(ret);

		// put them in the request
		List<SearchAirLeg> legs = request.getSearchAirLeg();
		legs.add(outbound);
		legs.add(ret);

		// one adult passenger
		AirReq.addAdultPassengers(request, 1);

		// get the point of sale right
		PointOfSale gdsPOS = new PointOfSale();
		gdsPOS.setProviderCode(System.getProperty("travelport.gds"));
		gdsPOS.setPseudoCityCode(originAirportcode);

		PointOfSale railPOS = new PointOfSale();
		railPOS.setProviderCode(Helper.RAIL_PROVIDER);
		railPOS.setPseudoCityCode(originAirportcode);
		
		PointOfSale lccPOS = new PointOfSale();
		lccPOS.setProviderCode(Helper.LOW_COST_PROVIDER);
		lccPOS.setPseudoCityCode(originAirportcode);
		
		request.getPointOfSale().add(gdsPOS);
		request.getPointOfSale().add(railPOS);
		request.getPointOfSale().add(lccPOS);
	}
}
