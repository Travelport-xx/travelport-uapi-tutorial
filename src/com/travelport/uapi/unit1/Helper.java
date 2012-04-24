package com.travelport.uapi.unit1;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.xml.ws.BindingProvider;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;

import com.travelport.schema.air_v18_0.AirSegment;
import com.travelport.schema.air_v18_0.FlightDetails;
import com.travelport.schema.rail_v12_0.RailJourney;
import com.travelport.schema.rail_v12_0.RailSegment;
import com.travelport.service.air_v18_0.*;
import com.travelport.service.system_v8_0.*;


public class Helper {
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
