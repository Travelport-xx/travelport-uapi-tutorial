# Complex Travel Shopping

## Unit 1, Lesson 3

### Objective Of Lesson 3

After this lesson is completed you should know how to use the uAPI facility for "shopping" for lowest cost transport between two cities, including using rail and low cost air carriers as a means of travel.   You should also understand how get data from the low cost shopping API's asynchronously.

### The Goal

The goal of this lesson is produce output that allows the user to compare not only price but means of transport between two locations.  This is a snippet from the `Lesson3` program show two different itineraries:

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

In addition, this data was retrieved asynchronously allowing the application to do other things while waiting for the results to be returned.  In our case, the `Lesson3` application just "sleeps" but there is no reason it could not calculate the cube root of pi, the price of tea in China, etc.


### Exercises For The Reader

* Using the low cost search (synchronous or asynchronous) build the necessary tables to keep track of the lowest priced way to travel from origin to destination and print out the lowest five itineraries, whether by rail or air.




