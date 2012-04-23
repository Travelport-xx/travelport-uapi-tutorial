package com.travelport.uapi.unit1;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;

import com.travelport.schema.air_v18_0.*;
import com.travelport.schema.air_v18_0.AirLegModifiers.PreferredCabins;
import com.travelport.schema.air_v18_0.AirSearchModifiers.PreferredProviders;
import com.travelport.schema.common_v15_0.*;
import com.travelport.service.air_v18_0.AirFaultMessage;


public class Lesson2 {
	//
	// PROGRAM ENTRY POINT
	//
	public static void main(String[] argv) {
		
		try {
			//make the request... paris to bangkok
			String from ="CDG",to="BKK";
			
			//staying a week ... two months from now.. roundtrip
			AvailabilitySearchRsp rsp = search(from, to, 
					daysInFuture(60), daysInFuture(67));
			
			//make tables that map the "key" (or a reference) to the proper
			//segment and the proper flight details
			Helper.SegmentMap seg = buildSegmentMap(rsp);
			Helper.FlightDetailsMap details = buildFlightDetailsMap(rsp);
			
			//Each "solution" is for a particular part of the journey... on
			//a round trip there will be two of thes
			List<AirItinerarySolution> solutions = rsp.getAirItinerarySolution();
			AirItinerarySolution outboundSolution = solutions.get(0);
			AirItinerarySolution inboundSolution = solutions.get(1);
			
			List<AirItinerary> out = buildRoutings(outboundSolution, seg, details);
			List<AirItinerary> in = buildRoutings(inboundSolution, seg, details);
			
			//merge in and out itins so we can get pricing for whole deal
			mergeOutboundAndInbound(out, in);
			
			//walk the itineraries, displaying them first...
			for (Iterator<AirItinerary> outIter = out.iterator(); outIter.hasNext();) {
				AirItinerary itin = outIter.next();
				List<AirSegment> segments = itin.getAirSegment();
				for (Iterator<AirSegment> iter = segments.iterator(); iter.hasNext();) {
					AirSegment airSegment = (AirSegment) iter.next();
					System.out.print(airSegment.getCarrier()+"#"+airSegment.getFlightNumber());
					System.out.print(" from "+airSegment.getOrigin()+" to "+ 
							airSegment.getDestination());
					System.out.println(" at "+airSegment.getDepartureTime()+
							" (flight time "+airSegment.getFlightTime()+" mins)");
				}
			}
			//display what the response said the response time was in secs
			System.out.println("\n\nResponse time was "+
					rsp.getResponseTime().divide(BigInteger.valueOf(1000))+
					" seconds.");
		} catch (AirFaultMessage e) {
			System.err.println("Error:"+e.getMessage());
		}
	}
	
	/**
	 * Take the inbound and outbound solutions and merge them into full 
	 * itineraries.
	 * @param out
	 * @param in
	 */
	private static void mergeOutboundAndInbound(List<AirItinerary> out,
			List<AirItinerary> in) {
		for (Iterator<AirItinerary> initer = in.iterator(); initer.hasNext();) {
			AirItinerary airItinerary = (AirItinerary) initer.next();
			List<AirSegment> segments = airItinerary.getAirSegment();
			for (Iterator<AirSegment> segIter = segments.iterator(); segIter.hasNext();) {
				AirSegment s = (AirSegment) segIter.next();
				for (Iterator<AirItinerary> outiter = out.iterator(); outiter.hasNext();) {
					AirItinerary itin = (AirItinerary) outiter.next();
					itin.getAirSegment().add(s);
				}
			}
		}
	}
	/**
	 * Walk a solution to build a list of itineraries that can be used in
	 * a pricing request.  These itineraries only are for "half" of a
	 * round trip.
	 */
	public static List<AirItinerary> buildRoutings(AirItinerarySolution soln,
			Helper.SegmentMap segmentMap, Helper.FlightDetailsMap detailMap) {
		ArrayList<AirItinerary> result = new ArrayList<AirItinerary>();
		
		//walk the list of segments in this itinerary... but convert them from
		//references to real segments for use in pricing
		List<AirSegmentRef> legs = soln.getAirSegmentRef();
		ArrayList<AirSegment> segs = new ArrayList<AirSegment>();
		//when this loop is done, we have a list of segments that are good to
		//go for use in a pricing request... 
		for (Iterator<AirSegmentRef> segIter = legs.iterator(); segIter.hasNext();) {
			AirSegmentRef ref = segIter.next();
			segs.add(cloneAndFixFlightDetails(segmentMap.get(ref), detailMap));
		}
		
		//a connection indicates that elements in the list of segs have to
		//be put together to make a routing
		List<Connection> conns = soln.getConnection();
	
		for (Iterator<Connection> connIter = conns.iterator(); connIter.hasNext();) {
			Connection connection = (Connection) connIter.next();
			AirItinerary itin = new AirItinerary();
			int idx = connection.getSegmentIndex();
			itin.getAirSegment().add(segs.get(idx));
			itin.getAirSegment().add(segs.get(idx+1));
			result.add(itin);
			segs.set(idx, null);
			segs.set(idx+1, null);
		}
		
		//those that are left are direct flights (no connections)
		for (int i=0; i<segs.size();++i) {
			AirSegment segment = segs.get(i);
			if (segment!=null) {
				AirItinerary itin = new AirItinerary();
				itin.getAirSegment().add(segment);
				result.add(itin);
			}
		}
		return result;
	}
	
	/**
	 * This is not a true clone because we don't copy all the fields; just the
	 * ones we need to do pricing with this segment.
	 * 
	 * @param orig segment to "clone"
	 * @return a clone of the input segment, with any reference to flight
	 * details adjusted to be the actual details
	 */
	public static AirSegment cloneAndFixFlightDetails(AirSegment orig, Helper.FlightDetailsMap detailMap) {
		AirSegment result = new AirSegment();
		result.setCarrier(orig.getCarrier());
		result.setClassOfService(orig.getClassOfService());
		result.setFlightNumber(orig.getFlightNumber());
		result.setKey(orig.getKey());
		result.setDepartureTime(orig.getDepartureTime());
		result.setArrivalTime(orig.getArrivalTime());
		result.setDestination(orig.getDestination());
		result.setOrigin(orig.getOrigin());
		result.setProviderCode(System.getProperty("travelport.gds"));
		
		//adjust flight detail references to be REAL flight details
		List<FlightDetailsRef> refs = orig.getFlightDetailsRef();
		for (Iterator<FlightDetailsRef> refsIter = refs.iterator(); refsIter.hasNext();) {
			FlightDetailsRef ref = (FlightDetailsRef) refsIter.next();
			FlightDetails deets = detailMap.get(ref);
			result.getFlightDetails().add(deets);
		}
		return result;
	}
	
	/**
	 * Build the map from references to to real flight details
	 */
	public static Helper.FlightDetailsMap buildFlightDetailsMap(
			AvailabilitySearchRsp rsp) {
		Helper.FlightDetailsMap result = new Helper.FlightDetailsMap();
		List<FlightDetails> details = rsp.getFlightDetailsList().getFlightDetails();
		for (Iterator<FlightDetails> iterator = details.iterator(); iterator.hasNext();) {
			FlightDetails deet = (FlightDetails) iterator.next();
			result.add(deet);
		}
		return result;
	}

	public static void priceItin(AirItinerary itin) throws AirFaultMessage {
		//now lets try to price it
		AirPriceReq priceReq = new AirPriceReq();
		AirPriceRsp priceRsp;
		priceReq.setAirItinerary(itin);
		AirPricingCommand command = new AirPricingCommand();
		command.setCabinClass(TypeCabinClass.ECONOMY);
		priceReq.getAirPricingCommand().add(command);
		priceReq.setTargetBranch(System.getProperty("travelport.targetBranch"));
		SearchPassenger adult = new SearchPassenger();
		adult.setCode("ADT");
		priceReq.getSearchPassenger().add(adult);
		priceRsp = Helper.WSDLService.getPrice().service(priceReq);
		List<AirPriceResult> prices = priceRsp.getAirPriceResult();
		for (Iterator<AirPriceResult> i = prices.iterator(); i.hasNext();) {
			AirPriceResult result = (AirPriceResult) i.next();
			TypeResultMessage message = result.getAirPriceError();
			if (message!=null) {
				System.out.println("Pricing Error ["+ message.getCode()+
						"] "+message.getType()+" : "+
						message.getValue());
			}
		}
	}

	/**
	 * Take a result object and construct a list of all the segments into
	 * a segment map.  This makes other parts of the work easier.
	 */
	public static Helper.SegmentMap buildSegmentMap(AvailabilitySearchRsp rsp) {
		//construct a map with all the segments and their keys
		Helper.SegmentMap segmentMap = new Helper.SegmentMap();
		
		List<AirSegment> segments = rsp.getAirSegmentList().getAirSegment();
		for (Iterator<AirSegment> iterator = segments.iterator(); iterator.hasNext();) {
			AirSegment airSegment = (AirSegment) iterator.next();
			segmentMap.add(airSegment);
		}
	
		return segmentMap;
	}
	/**
	 * Do a search for availability for a traveller.
	 * 
	 * @param origin airport code
	 * @param dest airport code
	 * @param dateOut date of departure in yyyy-MM-dd format
	 * @param dateBack date of return in yyyy-MM-dd format
	 * @return raw response object
	 */
	public static AvailabilitySearchRsp  search(String origin,
			String dest, String dateOut, String dateBack) throws AirFaultMessage{

		AvailabilitySearchReq request = new AvailabilitySearchReq();
		AvailabilitySearchRsp response;
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
		
		response = Helper.WSDLService.getAvailabilitySearch().service(request);

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


}
