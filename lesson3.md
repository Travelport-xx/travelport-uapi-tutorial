# Complex Travel Shopping

## Unit 1, Lesson 3

### Objective Of Lesson 3

After this lesson is completed you should know how to use the "shopping" facility of Travelport's Universal API to find the lowest cost transport between two cities, including using rail and low cost air carriers as a means of travel.   You should also understand after this lesson how get data from the low cost shopping API's asynchronously.

### LowFareSearch vs. Availability/Pricing

In the [previous lesson](lesson2.html) we explained that a typical, perhaps even the archetypal, travel industry workflow was to search for availability of flights--the possible itineraries from origin to destination--and then price one or more itineraries that were interesting to the traveller. Well, there is a little secret that we did not mention in the last lesson: this workflow can be done in one step with the uAPI, not the two part process we explained in [lesson2](lesson2.html).  Previously we explained how to use the availability search port to do a search and then pick itineraries to price; our code for `Lesson2` wasn't particularly clever about choosing what to price--it tried them all--but there are many metrics that a program could use to evaluate itineraries prior to calling the air price port.

The Low Cost Search port, yet another port on the `AirService` object, allows you to combine these two steps by doing a search and having the search results come back already priced.  Further, the Low Cost Search does the work of narrowing down the set of itineraries to those that are the least expensive, since is the common case anyway.  Note that the Low Fare Search algorithm does not guarantee that the price shown is still available and it is advised that you follow-up any "good looking" result returned from the Low Cost Search with an additional `AirPricingRequest` to insure that the itinerary can be priced as returned.  

### Air, Rail, and Low Cost Carriers

The TravelPort uAPI supports three different types of providers.  The first one is the one we have been using previously, the "gds" provider(s) of Travelport such as Galileo("1G"), Apollo("1V") and Worldspan("1P").  These provide you with capabilities that one would expect in online shopping and booking of air travel; Lesson 2 was designed to work with this type of provider.  The uAPI also includes the ability search for Rail travel with several different companies, called "suppliers" in the uAPI language.  The functionality for rail-based travel is accessed in the same general way as we have already seen with air travel, but naturally starting with the `RailService` that is defined in `Rail.wsdl` (in the directory `wsdl/rail_v12_0` in the supplied files with this tutorial.)  

The other type of provider that can be accessed through the uAPI is the unfortunately-named "Low-Cost Carrier" provider; surely all the airlines _think_ they are "low cost."  In fact, a "Low Cost Carrier" in this terminology is a carrier that does not participate in "global distribution" agreements for their inventory of seats--and that is the 'g' and 'd' in the acronym G-D-S.  So, gdses (like Galileo, Apollo, and Worldspan) typically do not know about the flight schedules, fares, or availability of seats on these airlines.  Often these airlines sell exclusively via the internet on their own websites.  We mention this type of provider here for completeness, we will focus primarily on the gds and rail providers as their are numerous special issues that must be addressed when working with the "low cost provider."
  
### The Goal Of Lesson 3

The goal of this lesson is produce output that allows the user to compare not only price but compare means of transport between two locations.  This is a snippet from the `Lesson3` program show two different itineraries from Glasgow, Scotland, to London's Gatwick Airport:

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



### Exercises For The Reader

* Using the low cost search (synchronous or asynchronous) build the necessary tables to keep track of the lowest priced way to travel from origin to destination and print out the lowest five itineraries, whether by rail or air.




