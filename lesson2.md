# Basic Air Travel Requests

## Unit 1, Lesson 2

### Objective Of Lesson 2

After this lesson is completed you should know how to search for available flights and price itineraries using the Travelport Universal API.

### Workflow

This lesson, building on the [previous one](index.html), will allow you to do what most travel agents did in the past and what many still do today.  Their objective is to sell a trip to a customer, but to do that they need to do two basic tasks:

* Find a set of available flights that get the traveller from origin to destination (and often back)

* Given the set of flights, determine the price of that itinerary

Given the terminology we explained in [lesson 1](index.html), there are two ports that are needed to accomplish this workflow, unsurprisingly called the "Availabilty Search" port and the "Price" port.  Both of these can be accessed from the `AirService` object.

### Generating The Client

As with the `Service.wsdl` in the previous lesson, we will need to generate the Java code to access uAPI services, this time from `Air.wsdl` in the directory `wsdl/air_v18_0`.  After you have generated the code, you will have many more packages in your project (hitting "refresh" or "F5" on your `src` folder is probably a good idea).  The [AirService](https://github.com/iansmith/travelport-uapi-tutorial/blob/master/src/com/travelport/service/air_v18_0/AirService.java) object's (generated) implementation is part of the package [com.travelport.service.air_v18_0]((https://github.com/iansmith/travelport-uapi-tutorial/blob/master/src/com/travelport/service/air_v18_0/).  

### Why All The Packages And Code?

The reason for all the generated code--tens of thousands of lines of it--resulting running "generate client" on `Air.wsdl` is that WSDL files may reference other WSDL files as well as schema files (in XSD files).  The `Air.wsdl` is the top of a large pyramid of different objects and since they all can be referenced in a chain that starts from `Air.wsdl` the CXF framework is obligated to generate code for them. CXF must generate Java code for all _reachable_ types starting at `Air.wsdl` and proceeding through any number of requests and responses; in this specific case, the set of reachable types includes classes describing the amenities available in a particular hotel and the details of the taxes on a particular rail journey!

### Goal

The class [Lesson2](https://github.com/iansmith/travelport-uapi-tutorial/blob/master/src/com/travelport/uapi/unit1/Lesson2.java) can output itineraries for a given city pair, in this case Paris to Chattanooga Tennessee, USA, in a form like this:

```
Price:GBP941.70 [BasePrice EUR760.00, Taxes GBP315.70]
AF#682 from CDG to ATL at 2012-06-22T10:55:00.000+02:00
AF#8468 from ATL to CHA at 2012-06-22T16:05:00.000-04:00
DL#5023 from CHA to ATL at 2012-06-29T16:06:00.000-04:00
DL#8517 from ATL to CDG at 2012-06-29T17:55:00.000-04:00
-----------
Price:GBP3594.70 [BasePrice EUR3998.00, Taxes GBP301.70]
UA#2331 from CDG to CLT at 2012-06-22T11:10:00.000+02:00
US#3568 from CLT to CHA at 2012-06-22T16:25:00.000-04:00
DL#5023 from CHA to ATL at 2012-06-29T16:06:00.000-04:00
DL#8517 from ATL to CDG at 2012-06-29T17:55:00.000-04:00
```

Itineraries are separated by the dashed lines.  This output, one must admit, is a bit terse and quite specific to the airline industry.  The first itinerary has a price of 941.70 Great British Pounds (GBP) and involves two Air France (AF) flights on June 22nd and two Delta (DL) flights on the way back to Paris on June 29th.  The next itinerary is (shocking!) about four times as expensive, 3594.70 GBP, and involves three carriers this time, UA (United Airlines), US (US Airways), and again Delta on the return.  Note that this outbound air journey has a connection in Charlotte, North Carolina, USA (CLT) instead of Atlanta, Georgia (ATL).

When run, the code `Lesson2` will produce a number of these itineraries plus prices for them--typically about 20.  There will be a pause at the time the program starts while it waits for the proposed, available itineraries to be returned from a TravelPort GDS.  At the time each itinerary is displayed there will also be a pause as that particular itinerary is priced.

### Outline 

At a high level, the class [Lesson2](https://github.com/iansmith/travelport-uapi-tutorial/blob/master/src/com/travelport/uapi/unit1/Lesson2.java) must perform these logical operations:

* Construct the necessary parameters for an availability search, such the origin and destination city as well as the travel dates
* Make the availability search request
* Decode the results of the search into proper itineraries
* Looping over each itinerary
  * Request the price of this itinerary
  * Display the resulting price
  * Display the segments of the journey


As we shall see, the first and third items will prove to be more complex than most people would expect.

### Preparing the search

At the beginning of `main()` in the `Lesson2` class are these lines:

```java

	//make the request... paris to chattanooga TN USA
	String from ="CDG",to="CHA";
	//staying a week ... two months from now.. roundtrip
	AvailabilitySearchRsp rsp = search(from, to, 
		Helper.daysInFuture(60), Helper.daysInFuture(67));
```

The two calls to the helper method `Helper.daysInFuture()` should be fairly self explanatory.  So, we've setup all we need for a search now, right? We have the origin, destination, and dates of travel, so we are ready, right?  Not by a long shot!  The method `search` is implemented in `Lesson2` and is dozens of lines of code plus uses a number of helper routines.  "Why all this extra code?", one may wonder.  The reason is that there are hundreds of parameters that can possibly be set on an air search, for far too many reasons than can be explained here.  However, some of these parameters are required to be set in _any_ air travel search done with uAPI such as the (obvious) origin and destination but also which class of service should be considered (Economy is our default choice) and what type of passenger is traveling (Adult is our default choice, but there are more than 100 types of passengers such as Military Veteran, Member of the Clergy, etc).

Here is a snippet from the implementation of the `search()` method:

```java

	//R/T journey
	SearchAirLeg outbound = AirReq.createLeg(origin, dest);
	AirReq.addDepartureDate(outbound, dateOut);
	AirReq.addEconomyPreferred(outbound);

	//coming back
	SearchAirLeg ret = AirReq.createLeg(dest, origin);
	AirReq.addDepartureDate(ret, dateBack);
	//put traveller in econ
	AirReq.addEconomyPreferred(ret);

```

The code above creates two "legs" for the search to consider: one outbound from `origin` to `dest` and one for the reverse (`ret`) one week later.  Each leg also has a departure date and what type of seat should be searched for.  Each line of this snippet with code on it uses a method from the [AirReq](https://github.com/iansmith/travelport-uapi-tutorial/blob/master/src/com/travelport/uapi/unit1/AirReq.java) helper object.  These helper methods have been provided to try to make it easier to understand the examples or write new code that does similar things.  

The `AirReq` class has no "magic," of course.  This class is building various structures from the classes that were generated as part of work with `Air.wsdl`.  For example:

```java

	public static SearchAirLeg createLeg(String originAirportCode,
		String destAirportCode) {
		
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

		return createLeg(originLoc, destLoc);
	}
```

This is the code that creates a single `SearchAirLeg` object that is part of our request to the TravelPort uAPI for an availability search.  You can see from the code above that locations are more complicated objects than one might expect... they _can_ be an airport code as in this example, or they can be more complex entities such as "all locations near a given a city" as we will see in Lesson 3.

It is worth the time look at the implementation of `AirReq` so that you can see, even for the relatively simple searches we are doing here the number of different options, and thus different classes and structures, that are used.

### Decoding The Result

To understand the decoding taking place in client, it may be useful to examine the XML that is actually returned from the uAPI server to our client.  This is example is slighted edited for space:

```xml
 <?xml version="1.0" encoding="UTF-8"?><SOAP:Envelope xmlns:SOAP="http://schemas.xmlsoap.org/soap/envelope/">
  <SOAP:Body>
    <air:AvailabilitySearchRsp xmlns:air="http://www.travelport.com/schema/air_v18_0" xmlns:common_v15_0="http://www.travelport.com/schema/common_v15_0" TransactionId="E32DF18C0A07581501D13BB0BE67E3C8" ResponseTime="965" DistanceUnits="MI">
      <common_v15_0:NextResultReference>H4sIAAAAAAAAAJ1UTUzUQBR+W1z5cQ0BlBiMP0SiXmzLwi674kVBEVPxZ4kxEA5DO5TBblums0sxxkQT9aJHYzTRi5HExKgHDxoTE056NMabejBcvHjXE84MrViJbnQPs/Nmvve97/1MH32FdEChbQZVkVphxFEPUormDRKw8NK7bbdfo7t1kBqGdQE5j0MfAJS5dXxt4k77TK+s2tlgjjBzWrWtQEUW8hmmgcpvyp6rHg4ZRSYb9c5h17j8YQdZbllQoGEcNiBKR0kZizgMuscN7qDFVBqnKnsWdrQVGk0gSz42yRQxESOe288ZLOyvMhRrMBxxcEgmHbyWyRBMASOuNBm0GqIUmoNcWysxSlybQ9I29So+g7ZfLoddhm1M+W1j2aNY5jgLFyFtwHqPEpvEVsanXpVYmA5wRdFZmv2Eh36FRhVIFFPqjwu5VveDnWfevrrVd0MBkG0BzlKswfLnKrx/ObF/67WliZgtxdub/z8uZ/zF4uOsflWBlAHNAUbUnJZzMIjmAwbq3xtVSuL7Qwrqv1Xmm3F90fyy9Z4CdQZs9CmewpRiS+AY7EpGj4KKudcGEZPJ8IZmgohQ2KJJzbzJjO9P88Zzlr21x1Ui+0Of17EzoT+SLZ9aHBJWfikF0mOQsfjpiMeGSBW7Y9BM3CpyiNQ/5rmYA4SSGGBAw4xnIcHEYIvhUVsTtiYwiZRY5D+CVlJKiz6LNrdzH1X4qALyU9PzA6E1M/7pu5gICjuSoEkUYPUQX2L08vL3zycm1PsKKMegnhwnjkMC/rTIwDT1XM/x7HkGHb/JW73rl2IK7ffPAle0JxnMlDB1uHRi1aGrxCqTD9+0TM32ndxTJ4Y2WCMy1iaylvgFLXXnQrZjSeLnGqH+MDO1oeOjYegL990150w29emzU0dvdnVekfOV4ePqEP79+GVSmhweOHGSnEJ5yEP6YrOJf1XqBo4e5H/be/QevU/vLup9Ob27kOvWi3m9N6fnsr3FXD4vcINDXKlwUypibZDvXuya5JsVu4xcW2Nzs6jr/icfQRgdkfe2ldi7/YhTxs6vjd1byBaKxWxPPtLohz8AIHKIBC8GAAA=</common_v15_0:NextResultReference>
      <air:FlightDetailsList>
        <air:FlightDetails Key="0T" Origin="CDG" Destination="ATL" DepartureTime="2012-06-23T10:55:00.000+02:00" ArrivalTime="2012-06-23T14:20:00.000-04:00" FlightTime="565" TravelTime="722" Equipment="77W" OriginTerminal="2E" DestinationTerminal="S"/>
        <air:FlightDetails Key="1T" Origin="ATL" Destination="CHA" DepartureTime="2012-06-23T16:05:00.000-04:00" ArrivalTime="2012-06-23T16:57:00.000-04:00" FlightTime="52" TravelTime="722" Equipment="CRJ" OriginTerminal="S"/>
        <air:FlightDetails Key="2T" Origin="CDG" Destination="ATL" DepartureTime="2012-06-23T10:55:00.000+02:00" ArrivalTime="2012-06-23T14:20:00.000-04:00" FlightTime="565" TravelTime="722" Equipment="77W" OriginTerminal="2E" DestinationTerminal="S"/>
        <air:FlightDetails Key="3T" Origin="ATL" Destination="CHA" DepartureTime="2012-06-23T16:05:00.000-04:00" ArrivalTime="2012-06-23T16:57:00.000-04:00" FlightTime="52" TravelTime="722" Equipment="CRJ" OnTimePerformance="70" OriginTerminal="S"/>
        <air:FlightDetails Key="4T" Origin="CDG" Destination="CLT" DepartureTime="2012-06-23T11:10:00.000+02:00" ArrivalTime="2012-06-23T14:35:00.000-04:00" FlightTime="565" TravelTime="743" Equipment="767" OriginTerminal="1"/>
        <air:FlightDetails Key="5T" Origin="CLT" Destination="CHA" DepartureTime="2012-06-23T16:25:00.000-04:00" ArrivalTime="2012-06-23T17:33:00.000-04:00" FlightTime="68" TravelTime="743" Equipment="CRJ" OnTimePerformance="-10"/>
        <air:FlightDetails Key="6T" Origin="CDG" Destination="CLT" DepartureTime="2012-06-23T11:10:00.000+02:00" ArrivalTime="2012-06-23T14:35:00.000-04:00" FlightTime="565" TravelTime="743" Equipment="767" OriginTerminal="1"/>
        <air:FlightDetails Key="7T" Origin="CLT" Destination="CHA" DepartureTime="2012-06-23T16:25:00.000-04:00" ArrivalTime="2012-06-23T17:33:00.000-04:00" FlightTime="68" TravelTime="743" Equipment="CRJ" OnTimePerformance="-10"/>
      </air:FlightDetailsList>
      <air:AirSegmentList>
        <air:AirSegment Key="30T" Group="0" Carrier="AF" FlightNumber="682" Origin="CDG" Destination="ATL" DepartureTime="2012-06-23T10:55:00.000+02:00" ArrivalTime="2012-06-23T14:20:00.000-04:00" FlightTime="565" TravelTime="722" ETicketability="Yes" Equipment="77W" ChangeOfPlane="false" ParticipantLevel="Secure Sell" LinkAvailability="true" PolledAvailabilityOption="Polled avail used" OptionalServicesIndicator="false" AvailabilitySource="Seamless">
          <air:AirAvailInfo ProviderCode="1G">
            <air:BookingCodeInfo BookingCounts="J9|ZC|W9|U9|TC|GR"/>
            <air:BookingCodeInfo CabinClass="Business" BookingCounts="C9|D7|IC"/>
            <air:BookingCodeInfo CabinClass="Economy" BookingCounts="OC|S9|Y9|B9|M9|K9|H9|LC|QC|EC|NC|RC|VC|XC"/>
            <air:BookingCodeInfo CabinClass="PremiumEconomy" BookingCounts="A5"/>
          </air:AirAvailInfo>
          <air:FlightDetailsRef Key="0T"/>
        </air:AirSegment>
        <air:AirSegment Key="31T" Group="0" Carrier="AF" FlightNumber="8468" Origin="ATL" Destination="CHA" DepartureTime="2012-06-23T16:05:00.000-04:00" ArrivalTime="2012-06-23T16:57:00.000-04:00" FlightTime="52" TravelTime="722" ETicketability="Yes" Equipment="CRJ" ChangeOfPlane="false" ParticipantLevel="Secure Sell" LinkAvailability="true" PolledAvailabilityOption="Polled avail used" OptionalServicesIndicator="false" AvailabilitySource="Seamless">
          <air:CodeshareInfo OperatingCarrier="DL"/>
          <air:AirAvailInfo ProviderCode="1G">
            <air:BookingCodeInfo BookingCounts="W9|U9|T9"/>
            <air:BookingCodeInfo CabinClass="Economy" BookingCounts="S9|Y9|B9|M9|K9|H9|V9|L9|Q9|N7|R7"/>
            <air:BookingCodeInfo CabinClass="PremiumEconomy" BookingCounts="A9"/>
          </air:AirAvailInfo>
          <air:FlightDetailsRef Key="1T"/>
        </air:AirSegment>
        <air:AirSegment Key="32T" Group="0" Carrier="DL" FlightNumber="8504" Origin="CDG" Destination="ATL" DepartureTime="2012-06-23T10:55:00.000+02:00" ArrivalTime="2012-06-23T14:20:00.000-04:00" FlightTime="565" TravelTime="722" ETicketability="Yes" Equipment="77W" ChangeOfPlane="false" ParticipantLevel="Secure Sell" LinkAvailability="true" PolledAvailabilityOption="Polled avail used" OptionalServicesIndicator="false" AvailabilitySource="Seamless">
          <air:CodeshareInfo OperatingCarrier="AF"/>
          <air:AirAvailInfo ProviderCode="1G">
            <air:BookingCodeInfo BookingCounts="J9|W5|U0|T0"/>
            <air:BookingCodeInfo CabinClass="Business" BookingCounts="C9|D7|I0"/>
            <air:BookingCodeInfo CabinClass="Economy" BookingCounts="S0|Y9|B9|M9|H9|Q9|K0|L0"/>
          </air:AirAvailInfo>
          <air:FlightDetailsRef Key="2T"/>
        </air:AirSegment>
        <air:AirSegment Key="33T" Group="0" Carrier="DL" FlightNumber="5542" Origin="ATL" Destination="CHA" DepartureTime="2012-06-23T16:05:00.000-04:00" ArrivalTime="2012-06-23T16:57:00.000-04:00" FlightTime="52" TravelTime="722" ETicketability="Yes" Equipment="CRJ" ChangeOfPlane="false" ParticipantLevel="Secure Sell" LinkAvailability="true" PolledAvailabilityOption="Polled avail used" OptionalServicesIndicator="false" AvailabilitySource="Seamless">
          <air:AirAvailInfo ProviderCode="1G">
            <air:BookingCodeInfo CabinClass="Economy" BookingCounts="Y9|B9|M9|H9|Q9|K9|L9|E3"/>
            <air:BookingCodeInfo BookingCounts="U9|T9"/>
          </air:AirAvailInfo>
          <air:FlightDetailsRef Key="3T"/>
        </air:AirSegment>
      </air:AirSegmentList>
      <air:AirItinerarySolution Key="60T">
        <air:AirSegmentRef Key="30T"/>
        <air:AirSegmentRef Key="31T"/>
        <air:AirSegmentRef Key="32T"/>
        <air:AirSegmentRef Key="33T"/>
        <air:AirSegmentRef Key="34T"/>
        <air:AirSegmentRef Key="35T"/>
        <air:AirSegmentRef Key="36T"/>
        <air:AirSegmentRef Key="37T"/>
        <air:AirSegmentRef Key="38T"/>
        <air:AirSegmentRef Key="39T"/>
        <air:AirSegmentRef Key="40T"/>
        <air:AirSegmentRef Key="41T"/>
        <air:AirSegmentRef Key="42T"/>
        <air:AirSegmentRef Key="43T"/>
        <air:AirSegmentRef Key="44T"/>
        <air:AirSegmentRef Key="45T"/>
        <air:Connection SegmentIndex="0"/>
        <air:Connection SegmentIndex="2"/>
        <air:Connection SegmentIndex="4"/>
        <air:Connection SegmentIndex="6"/>
        <air:Connection SegmentIndex="8"/>
        <air:Connection SegmentIndex="10"/>
        <air:Connection SegmentIndex="12"/>
        <air:Connection SegmentIndex="14"/>
      </air:AirItinerarySolution>
      <air:AirItinerarySolution Key="61T">
        <air:AirSegmentRef Key="46T"/>
        <air:AirSegmentRef Key="47T"/>
        <air:AirSegmentRef Key="48T"/>
        <air:AirSegmentRef Key="49T"/>
        <air:AirSegmentRef Key="50T"/>
        <air:AirSegmentRef Key="51T"/>
        <air:AirSegmentRef Key="52T"/>
        <air:AirSegmentRef Key="53T"/>
        <air:AirSegmentRef Key="54T"/>
        <air:AirSegmentRef Key="55T"/>
        <air:AirSegmentRef Key="56T"/>
        <air:AirSegmentRef Key="57T"/>
        <air:AirSegmentRef Key="58T"/>
        <air:AirSegmentRef Key="59T"/>
        <air:Connection SegmentIndex="0"/>
        <air:Connection SegmentIndex="2"/>
        <air:Connection SegmentIndex="4"/>
        <air:Connection SegmentIndex="6"/>
        <air:Connection SegmentIndex="8"/>
        <air:Connection SegmentIndex="9"/>
        <air:Connection SegmentIndex="11"/>
        <air:Connection SegmentIndex="12"/>
      </air:AirItinerarySolution>
    </air:AvailabilitySearchRsp>
  </SOAP:Body>
</SOAP:Envelope>
```

Let's explain the approach the uAPI is using to encode the results.  Each "type" of entity is detailed once, typically in a "list" of that type, for example the `air:FlightDetailsList` has many `air:FlightDetails` entities within in (and many more were clipped out for space reasons!).  Similarly, the `air:AirSegmentList` contains many `air:AirSegment` encodings (again, we removed many `air:AirSegment` for space).  However, it is important to note that _within_ the `air:AirSegment` it does not repeat the `air:FlightDetails` but instead uses a `air:FlightDetailsRef` to refer to the flight details in question.  The `air:FlightDetailsRef` has a `Key` attribute that matches up with the `Key` attribute in the `air:FlightDetails`  object.  Why do it this way? Primarily, this approach avoids repetition which would bloat the already large requests and responses.  If you look at the last XML objects in the example you will see two "solutions" (`air:AirItinerarySolution`) that clearly indicate that it is possible to have a compact representation... after you have all the definitions above!  Interestingly, a single `air:ItinerarySolution` may encode may possible itineraries (despite the name!) because it "connects" segments with the `air:Connection` entries.  We'll explain more about this later when discuss building "routings."

	The large size of these messages and the complexity of encoding and decoding them is one of the more serious complaints about SOAP/XML as a transport in systems such as the uAPI.  We will not debate that point here, but just mention that the requests and responses sent to and from the Travelport system to end up being hundreds of lines of XML.
	





