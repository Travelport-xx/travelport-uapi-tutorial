# Complex Travel Shopping

## Unit 1, Lesson 3

### Objective Of Lesson 3

After this lesson is completed you should know how to use the "shopping" facility of Travelport's Universal API to find the lowest cost transport between two cities, including using rail and low cost air carriers as a means of travel.   You should also understand after this lesson how get data from the low cost shopping API's asynchronously.

### LowFareSearch vs. Availability/Pricing

In the [previous lesson](lesson2.html) we explained that a typical, perhaps even the archetypal, travel industry workflow was to search for availability of flights--the possible itineraries from origin to destination--and then price one or more itineraries that were interesting to the traveller. Well, there is a little secret that we did not mention in the last lesson: this workflow can be done in one step with the uAPI, not the two part process we explained in [lesson2](lesson2.html).  Previously we explained how to use the availability search port to do a search and then pick itineraries to price; our code for `Lesson2` wasn't particularly clever about choosing what to price--it tried them all--but there are many metrics that a program could use to evaluate itineraries prior to calling the air price port.

The Low Cost Search port, yet another port on the `AirService` object, allows you to combine these two steps by doing a search and having the search results come back already priced.  Further, the Low Cost Search does the work of narrowing down the set of itineraries to those that are the least expensive, since is the common case anyway.  Note that the Low Fare Search algorithm does not guarantee that the price shown is still available and it is advised that you follow-up any "good looking" result returned from the Low Cost Search with an additional `AirPricingRequest` to insure that the itinerary can be priced as returned.  

### Air, Rail, and Low Cost Carriers

The TravelPort uAPI supports three different types of providers.  The first one is the one we have been using previously, the "gds" provider(s) of Travelport such as Galileo("1G"), Apollo("1V") and Worldspan("1P").  These provide you with capabilities that one would expect in online shopping and booking of air travel; lesson 2 was designed to work with this type of provider.  The uAPI also includes the ability search for train travel with several different companies, called "suppliers" in the uAPI language.  The functionality for rail-based travel is accessed in the same general way as we have already seen with air travel, but naturally starting with the `RailService` that is defined in `Rail.wsdl` (in the directory `wsdl/rail_v12_0` in the supplied files with this tutorial.)  

The other type of provider that can be accessed through the uAPI is the unfortunately-named "Low-Cost Carrier" provider; surely all the airlines _think_ they are "low cost."  In fact, a "Low Cost Carrier" in this terminology is a carrier that does not participate in "global distribution" agreements for their inventory of seats--and that is the 'g' and 'd' in the acronym G-D-S.  So, gdses (like Galileo, Apollo, and Worldspan) typically do not know about the flight schedules, fares, or availability of seats on these airlines.  Often these airlines sell exclusively via the internet on their own websites.  We mention this type of provider here for completeness, we will focus primarily on the gds and rail providers as their are numerous special issues that must be addressed when working with the "low cost provider."
  
### The Goal Of Lesson 3

The goal of this lesson is produce output that allows the user to compare not only price but compare means of transport between two locations.  This is a snippet from the [Lesson3](https://github.com/iansmith/travelport-uapi-tutorial/blob/master/src/com/travelport/uapi/unit1/Lesson3.java) program show two different itineraries from Glasgow, Scotland, to London's Gatwick Airport:

```
AIR Departing from GLA to LGW on TUE MAY 01 11:00:00 CEST 2012
         Flight [BA]#2957  Flight time: 85 minutes
                           Arrive TUE MAY 01 12:25:00 CEST 2012


AIR Departing from LGW to GLA on THU MAY 03 12:25:00 CEST 2012
         Flight [BA]#2960  Flight time: 80 minutes
                           Arrive THU MAY 03 13:45:00 CEST 2012
Base Price: GBP80.00   Total Price GBP160.10


RAIL (OUTWARD) From Paisley Gilmour Str. to Gatwick Airport on TUE MAY 01 07:04:00 CEST 2012
         SCOTRAIL[SR] Train #SR7989   --- From Paisley Gilmour Str. to Glasgow
         VIRGIN TRAINS[VT] Train #VT6560   --- From Glasgow to London Euston
         METRO --- From London Euston to London Victoria
         SOUTHERN[SN] Train #SN1992   --- From London Victoria to Gatwick Airport
                           Arrive TUE MAY 01 13:22:00 CEST 2012
RAIL (RETURN) From Gatwick Airport to Paisley Gilmour Str. on THU MAY 03 05:10:00 CEST 2012
         SOUTHERN[SN] Train #SN4071   --- From Gatwick Airport to Clapham Junction
         SOUTHERN[SN] Train #SN3000   --- From Clapham Junction to Watford Junction
         VIRGIN TRAINS[VT] Train #VT6000   --- From Watford Junction to Glasgow
         SCOTRAIL[SR] Train #SR5718   --- From Glasgow to Paisley Gilmour Str.
                           Arrive THU MAY 03 12:10:00 CEST 2012
Total Price GBP356.00
```

In addition, this data was retrieved asynchronously allowing the application to do other things while waiting for the results to be returned.  In the case of this tutorial, the `Lesson3` application just "sleeps" but there is no reason it could not calculate the cube root of pi, the price of tea in China, the net worth of Sergei Brin, etc.

### Low Cost Searching, The Hard Way

Since you have already finished [lesson1](index.html) and [lesson2](lesson2.html), we'll omit a lot of the details that are present in the `Lesson3` class' source code.  To search using the `AirLowFareSearchPortType` one simply combines the start of `Lesson2`, creating a search request, and the end of `Lesson2`, displaying the resulting pricing solutions.  The intermediate manipulation of various data structures that was a bit complex in the case of `Lesson2` is now avoided.  (Again, it is best to use the air price port to check that results returned from low cost searching are still valid, but we'll ignore this for now.)  This seems easy, so let's make low cost searching *more* difficult by _not_ using the `AirLowFareSearchPortType` and instead using its brother, the `AirLowFareSearchAsyncPortType`!  

Because some of the results from providers can take some time to produce, the uAPI offers you the ability to send a search request and then retrieve the results at your convenience.  So, the flow of such an application looks like this:

* Send `LowCostSearchAsyncReq` via the low cost search async port's `service()` method
* Consume `LowCostSearchAsyncRsp' response object to determine what providers have what data
* Looping over all the providers that have results
	** Send a `RetrieveLowFareSearchReq` to retrieve results from the above search from but from a specific provider
	** Consume the `RetrieveLowFareSearchRsp` object to get results

As we have seen in `Lesson1` and `Lesson2` a _particular_ request/response pair should be handled synchronously with the uAPI.  However, because of the structure above, it is possible to proceed with other actions in between requesting, say, the air results and the rail results of a particular search.

### Java Typing, uAPI, And Low Fare Search Responses

There are two basic Java types that are used in the above approach to handling responses:  `LowFareSearchAsyncRsp` and `RetrieveLowFareSearchRsp` as these are the appropriate types returned by the search and "get me more data" ports, respectively.  Thoughtfully, the designers of the uAPI planned ahead for this and made these two types share a common base type (a Java superclass), `AirSearchRsp`.  This means that your code can be written, of course with some care, to consume results from the `AirSearchRsp` class and then it can handle either immediate or later-retrieved results.

With this in mind, the result of the availability requests in [Lesson2](lesson2.html) can _also_ be treated as an `AirSearchRsp` object as the class `AvailabilitySearchReq` also inherits from this base class.  These types of relationships are present in many places in the uAPI and it is often very useful to use Eclipse's "Go To Definition" feature (typically bound to the F3 key) to investigate the parent classes in the class hierarchy generated by the uAPI's WSDL.  Looking further up the heirarchy, for example, reveals that all search requests also share a common base class (`AirSearchReq` and its parent `BaseSearchReq`).  All requests, without regard to their type, share the base class `BaseReq` (with the notable exception of ping).  In `BaseReq` you find those fields that are common to any request, such as "TraceId".

### A Small Amount Of Nomenclature

One has to be a bit "flexible" with naming and use of some of the methods that are part of the `AirSearchRsp` response object.  It may seem strange at first that such a such response contains the method get *rail* journeys! This strange bit of nomenclature is needed to account for the ability to put different providers as your preferred (or not preferred!) in an `AirSearchReq`. If you include the rail provider as a preferred provider for a journey from Berlin to Montpellier, the low cost air search can, and usually, will return a result that includes Deutsche Bahn trains.  

### PrintableItinerary

The class [PrintableItinerary](https://github.com/iansmith/travelport-uapi-tutorial/blob/master/src/com/travelport/uapi/unit1/PrintableItinerary.java) is included with the code for this unit.  It is intended to be used as part of this lesson.  Instances of this class can be constructed in one of two ways:

```java
public PrintableItinerary(AirPricingSolution solution, Helper.AirSegmentMap seg,
		String roundTripTurnaround)

public PrintableItinerary(RailPricingSolution solution, Helper.RailJourneyMap jour,
				Helper.RailSegmentMap seg)

```

These two constructors are the air and rail versions of this class, so that once constructed it is possible to simply call "toString()" on the `PrintableItinerary` object and have something reasonably understandable to a human being get printed out. 

A couple of things to note about `PrintableItinerary` constructors:

* The air version of the constructor requires the caller to supply the `roundTripTurnaround` as an airport code.  This is because air pricing solutions do not have the `direction` notion that is present in `RailJourney` and thus the code in `PrintableItinerary` must determine which parts of a trip (`AirSegments`) are out-bound and which are in-bound.

* Both of these constructors need the "maps" that were discussed in lesson 2 to have already been built, so these will be needed in lesson 3 as well.  This is not surprising since the PrintableItinerary prints out many details that are present only in the full definition of classes like `AirSegment` not in the `AirSegmentRef` in the solution.

`PrintableItinerary` is far from a great looking output, but it is functional.   The example output at beginning of this lesson is from `PrintableItinerary`.  You can change the code to improve the quality of the output if you want to, but be careful to protect your code from unexpected `null` values; often the results of a rail or air journey have `null` values in many places and you don't want your code to crash with a `NullPointerException` (NPE).

### the main() event

The code for `main` in `Lesson3` is reproduced, edited for size and clarity, below:

```java
public static void main(String[] argv) {
	String origin = "GLA", destination = "LGW";
	LowFareSearchAsynchReq req = new LowFareSearchAsynchReq();
	//7 days out for departure and 9 days out for return
	createLowFareSearchWithRail(req, origin, destination, 7, 9);

	try {
		//the actual search request
		LowFareSearchAsynchRsp lowCostRsp = WSDLService
				.getLowFareSearchAsynch(false).service(req);

		[create a HashMap that knows about all the providers and how many 'parts' they have]

		//these three parameters are needed as we loop to get each result... they are the
		//context of our first search above
		String searchId = lowCostRsp.getSearchId();
		String currentProvider = lowCostRsp.getProviderCode();
		long currentPart = lowCostRsp.getPartNumber().longValue();
		
		AirSearchRsp rsp = lowCostRsp; // so we can print it out
		while (partMap.isEmpty() == false) {

			printSomeExampleResults(destination, rsp, 2); //2 sample results

			[update currentPart and currentProvider for next "retrieve request"]

			[sleep a few seconds]
			
			//run the request for more data...
			RetrieveLowFareSearchReq retrieve = new RetrieveLowFareSearchReq();
			//note that these are the "context" parameters that need to be given in
			//the retreive so we get back "more data" from our search
			retrieve.setSearchId(searchId);
			retrieve.setProviderCode(currentProvider);
			retrieve.setPartNumber(BigInteger.valueOf(currentPart));

			//request more data based on the currentPart and currentProvider
			rsp = WSDLService.getRetrieve(false).service(retrieve);
			
			checkForErrorMessage(rsp);
		}
	} catch (AirFaultMessage e) {
		System.err.println("Fault trying send request to travelport:"
				+ e.getMessage());
	}

}
```

None of the code above should be shocking if you have been following us through the previous two lessons.  The key portion of this `main()` function is the loop that goes through the set of providers and each of their parts.  A more clever program than the one we have written here would do some useful computation where we have just done "[sleep a few seconds]"!

### Output

When you run `Lesson3` you will seem some itineraries such as the ones presented at the first of the lesson.  With those elided for clarity, the output of the program will look like much like this:

```
waiting for first response from a provider...
Provider ACH has a total of 1 parts
Provider 1G has a total of 1 parts
Provider RCH has a total of 3 parts
++++++++++++++++++++
Response is from provider 1G: part 1 of 1
Total number solutions: 66 air and 0 rail
[Example AIR Solution 1 of 66]
[Example AIR Solution 2 of 66]
Sleeping 5 secs before trying to request part 1 from ACH
No data available from low cost provider?
++++++++++++++++++++
Response is from provider ACH: part 1 of 1
Total number solutions: 0 air and 0 rail
Sleeping 5 secs before trying to request part 1 from RCH
Response Message From Provider RCH[BN]  : Warning : BeNe only support international journeys. -- Error Code = 14
++++++++++++++++++++
Response is from provider RCH: part 1 of 3
Total number solutions: 0 air and 0 rail
Sleeping 5 secs before trying to request part 2 from RCH
Response Message From Provider RCH[TL]  : Warning : TL assumes adult for PassengerType if no Age or Date of Birth specified. -- Error Code = 14
++++++++++++++++++++
Response is from provider RCH: part 2 of 3
Total number solutions: 0 air and 30 rail
[Example RAIL solution 1 of 30]
[Example RAIL solution 2 of 30]
Sleeping 5 secs before trying to request part 3 from RCH
Response Message From Provider RCH[DB]  : Warning : No available rail suppliers for the requested origin location country -- Error Code = 14
++++++++++++++++++++
Response is from provider RCH: part 3 of 3
Total number solutions: 0 air and 0 rail

```

This is a bit more "real" than the idealized output shown at the beginning of this lesson that included just the itineraries.   A couple of things that the reader be interested in:  The `RCH` provider is the rail provider and two of its suppliers (Benelux and Deutsche Bahn) have no train service in the United Kingdom, so we get the warnings from "RCH[BN]" and "RCH[DB]" when doing a Glasgow to London search.  These two providers are also shown in the output as having 0 air and 0 rail results.

### End Of Unit 1

Congratulations! You've managed to get through (or "suffered through"?)  all three of the lessons in this unit.  With these three lessons under your belt, you should be feeling fairly confident of using nearly any feature of the uAPI that involves search for content and displaying it to the user.  In the upcoming unit, we will focus on some other types of common workflows such as booking tickets or searching hotels by their distance from a landmark.  Enjoy using the uAPI!


### Exercises For The Reader

* Using the low cost search (synchronous or asynchronous) build the necessary tables to keep track of the lowest priced way to travel from origin to destination and print out the lowest five itineraries, whether by rail or air.

* Above we discussed Java's type system, `AirSearchRsp` objects and the fact that these may include rail journey information.  By studying the WSDL and XSD files, determine if the same "crossover" applies to searching for availability in train travel.

* Use the `FlightDetails` class to display to the user if any meals are expected on air journeys as well as the particular type of aircraft that will be used.  It may be helpful to build a table to make the set of aircraft easier to understand for those not familiar with the model numbers of aircraft, such as changing "737" into "Boeing 737" or even "Boeing Single-Aisle Jet".

* Add a `toJson()` method for `PrintableItinerary` so it can output data in a different format, suitable for use by another program.
