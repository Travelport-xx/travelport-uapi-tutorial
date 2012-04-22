package com.travelport.uapi.unit1;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;

import javax.xml.ws.BindingProvider;

import org.junit.BeforeClass;
import org.junit.Test;

import com.travelport.schema.air_v15_0.*;
import com.travelport.schema.common_v12_0.ResponseMessage;
import com.travelport.service.air_v15_0.*;

public class AirSvcTest {

	@Test
	public void availability() throws AirFaultMessage {
		AvailabilitySearchReq request = new AvailabilitySearchReq();
		AvailabilitySearchRsp rsp;
		
		setupRequestForSearch(request);
		rsp=Helper.WSDLService.getAvailabilitySearch().service(request);
		//these checks are just sanity that we can make an availability request
		assertThat(rsp.getAirItinerarySolution().size(), is(not(0)));
		assertThat(rsp.getAirSegmentList().getAirSegment().size(), is(not(0)));
	}
	
	@Test
	public void lowFareSearch() throws ParseException, AirFaultMessage {
		LowFareSearchReq request = new LowFareSearchReq();
		LowFareSearchRsp response;
		String myTraceId = "ltk-007";
		
		request.setTraceId(myTraceId);//sanity
		
		setupRequestForSearch(request);
		//2 adults travelling, needed for a low cost search
		Lesson2.addAdultPassengers(request, 2);

		//do the work
		response = Helper.WSDLService.getLowFareSearch().service(request);
		
		//sanity cechk
		assertThat(myTraceId, is(equalTo(request.getTraceId())));
		
		//hard to say what the response exactly *SHOULD* be but we should
		//not have a bunch of null or empty values!
		assertThat(response.getAirPricingSolution().size(), is(not(0)));
 		assertThat(response.getAirSegmentList().getAirSegment().size(), is(not(0)));
 		//this should not be empty for 1G as the gds because it should be 
 		//giving a warning that MaxSolutions is not supported by the provider
		assertThat(response.getResponseMessage().size(), is(not(0)));
		
	}

	//different search request types use this different ways
	public void setupRequestForSearch(AirSearchReq request) {
		request.setTargetBranch(System.getProperty("travelport.targetBranch"));
		
		//set the GDS via a search modifier
		AirSearchModifiers modifiers = Lesson2.gdsAsModifier(System.getProperty("travelport.gds"));
		
		//try to limit the size of the return... not supported by 1G!
		modifiers.setMaxSolutions(BigInteger.valueOf(25));
		request.setAirSearchModifiers(modifiers);
		
		//travel is for paris to portland 2 months from now, one week trip
		SearchAirLeg outbound = Lesson2.createLeg("CDG", "PDX");
		Lesson2.addDepartureDate(outbound, Lesson2.daysInFuture(60));
		Lesson2.addEconomyPreferred(outbound);

		//coming back
		SearchAirLeg ret = Lesson2.createLeg("PDX", "CDG");
		Lesson2.addDepartureDate(ret, Lesson2.daysInFuture(67));
		Lesson2.addEconomyPreferred(ret);

		//put them in the request
		List<SearchAirLeg> legs = request.getSearchAirLeg();
		legs.add(outbound);
		legs.add(ret);
	}
}
