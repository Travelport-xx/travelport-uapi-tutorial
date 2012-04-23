package com.travelport.uapi.unit1;

import java.util.ArrayList;
import java.util.List;

import com.travelport.schema.air_v18_0.LowFareSearchReq;
import com.travelport.schema.common_v15_0.SearchPassenger;

public class Lesson3 {
	// add some number of adult passengers to a search request
	public static void addAdultPassengers(LowFareSearchReq request, int n) {
		for (int i = 0; i < n; ++i) {
			SearchPassenger adult = new SearchPassenger();
			adult.setCode("ADT");
			request.getSearchPassenger().add(adult);
		}
	}


	public static void main(String[] argv) {
		//walk all the solutions and create an itinerary for that
		List<Helper.PrintableItinerary> result = new ArrayList<Helper.PrintableItinerary>();

		/*
		List<AirPricingSolution> solutions = rsp.getAirPricingSolution();
		for (Iterator<AirPricingSolution> iterator = solutions.iterator(); iterator.hasNext();) {
			AirPricingSolution soln = (AirPricingSolution) iterator.next();
			result.add(new Helper.PrintableItinerary(soln, segmentMap, destinationAirport));
		}
		
		*/
	}
}
