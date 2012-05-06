---
layout: page
title: "Making A Booking"
description: "Understanding how to create a booking for air, hotel, or both with the Universal API."
---
{% include JB/setup %}

## Unit 2, Lesson 5

### The Objective of Lesson 5

In this lesson we are going to focus on the mechanics of actually making a booking and having that reservation handled by TravelPort.  We will discuss Hotel bookings in some depth and refer to the similarities with making Air reservations.  

### Shopping and reality

For air travel, [lesson 3](lesson_1-3.html) discussed how to perform a "low fare search", usually just called "shopping".  We did something similar in [lesson 4](lesson_2_4.html) for shopping for hotel rooms.  Shopping differs from [availability and pricing](lesson_1-2.html) because it not only combines the two processes but also because the technology underlying it is quite different.  In particular, almost all providers of search services--from internet search engines like [Google](http://google.com) to comparison shopping tools for consumer goods like [PriceGrabber](http://www.pricegrabber.com)--take some shortcuts in an effort to produce the lowest-priced result quickly.  Although the techniques are different by both industry and shopping provider, typically there is caching that is being used so the shopping provider does not have to do "live queries" of all the items that it might propose as results. 

In the particular case of buying travel-related inventory, a particular result from a shopping request (`LowFareSearchReq` producing a `LowFareSearchRsp`) may be "out of date."  This is the reason that you were cautioned in the previous lesson that it is good practice with the uAPI to follow up a result gained from a shopping request with an additional `AirPricingReq` to insure that the inventory is still available and that the price has not changed.  

A deeper reason than caching for this need to "verify" that a particular price is available or has not changed is because the uAPI is a truly real-time system with thousands of concurrent users.  It is more than likely than in the time that your program spends processing results from a shopping requests that many other pieces of software are connected to TravelPort and actively changing the inventory that is available.  Further, the owners of the inventory can, and do, change their prices from time to time and this also can occur concurrently with your program's operation.

The final "endgame" of this issue is the booking step.  When doing a booking request, all possible checks are done to the requested item to be reserved to insure that the object is still available, has been priced correctly with the provided fares, and the taxes added correctly.  Further, the issue now arises about _who_ is making the booking and how the traveler will pay for the booking.  Again, the uAPI will validate all of the values provided very carefully to insure that a final booking is only made when all the values are correct.

### Hotel Details

The two objects for making bookings are called `AirCreateReservationPortType` and `HotelReservationServicePortType` who have the naturally named `AirCreateReservationReq` and `HotelCreateReservationReq` plus the matching response (\"Rsp\") types for getting results. Generally, the values returned from the Air service are more complex to parse and display to the user because of the broader range of products (including rail) and the much larger set of possible, applicable fares.  For a hotel room, there are fewer variables so the responses returned are somewhat simpler to process and display.

Although they are simpler, they are not so simple that the results from a shopping request, such as those shown in lesson 4, are sufficient to create a booking.  Once a particular hotel of interest is found with a shopping request, it should be followed up to get the detailed pricing information as well as more details about the property that are likely of interest to any traveler, like the full address, phone numbers, etc.  When the `HotelDetailsServicePortType` is invoked with a `HotelDetailsReq` the parameters that are needed are quite similar to a shopping request but the result includes many different options in terms of pricing and, in some cases, other marketing information from the hotel.  

Let's look "under the hood" to see part of the XML result that is passed over the network back to our client program when we ask for details about a particular property that we are interested in:

{% highlight xml %}
<hotel:RequestedHotelDetails>
  <hotel:HotelProperty HotelChain="HJ" HotelCode="29993" HotelLocation="LAX" Name="HOWARD JOHNSON LOS ANGELES">
    <hotel:PropertyAddress>
      <hotel:Address>603 S. New Hampshire Ave. </hotel:Address>
      <hotel:Address>Los Angeles CA 90005 US </hotel:Address>
    </hotel:PropertyAddress>
    <common_v15_0:PhoneNumber Type="Business" Number="1 213-3854444"/>
    <common_v15_0:PhoneNumber Type="Fax" Number="1 213-3805413"/>
    <common_v15_0:Distance Value="3" Direction="SW"/>
  </hotel:HotelProperty>
  <hotel:HotelDetailItem Name="Marketing Message">
    <hotel:Text>THANK YOU FOR CHOOSING HOWARD JOHNSO</hotel:Text>
  </hotel:HotelDetailItem>
  <hotel:HotelRateDetail RatePlanType="NDD1SDI" Base="USD148.84" Tax="USD23.07" Total="USD171.90">
    <hotel:RoomRateDescription Name="Total Includes">
      <hotel:Text>The Total includes taxes, surcharges, fees.</hotel:Text>
    </hotel:RoomRateDescription>
    <hotel:RoomRateDescription Name="Description">
      <hotel:Text>STAY 2  SAVE 15 PERCENT -.2 DOUBLE BEDS NON SMOKING ROOM WITH.FREE CONTINENTAL BREAKFAST  FREE.</hotel:Text>
    </hotel:RoomRateDescription>
    <hotel:RoomRateDescription Name="Commission">
      <hotel:Text>Yes</hotel:Text>
    </hotel:RoomRateDescription>
    <hotel:RoomRateDescription Name="Guarantee">
      <hotel:Text>Guarantee Required</hotel:Text>
    </hotel:RoomRateDescription>
    <hotel:HotelRateByDate EffectiveDate="2012-06-20" ExpireDate="2012-06-22" Base="USD74.42"/>
  </hotel:HotelRateDetail>
  <hotel:HotelRateDetail RatePlanType="DD1SDI" Base="USD148.84" Tax="USD23.07" Total="USD171.90">
    <hotel:RoomRateDescription Name="Total Includes">
      <hotel:Text>The Total includes taxes, surcharges, fees.</hotel:Text>
    </hotel:RoomRateDescription>
    <hotel:RoomRateDescription Name="Description">
      <hotel:Text>STAY 2  SAVE 15 PERCENT -.2 DOUBLE BEDS SMOKING ROOM WITH FREE.CONTINENTAL BREAKFAST  FREE WI-FI.</hotel:Text>
    </hotel:RoomRateDescription>
    <hotel:RoomRateDescription Name="Commission">
      <hotel:Text>Yes</hotel:Text>
    </hotel:RoomRateDescription>
    <hotel:RoomRateDescription Name="Guarantee">
      <hotel:Text>Guarantee Required</hotel:Text>
    </hotel:RoomRateDescription>
    <hotel:HotelRateByDate EffectiveDate="2012-06-20" ExpireDate="2012-06-22" Base="USD74.42"/>
  </hotel:HotelRateDetail>

{% endhighlight %}

This is far from all the `hotel:HotelRateDetail` entities encoded in this single XML message! The early part of this snippet shows some of the detailed data about the property that is provided and the marketing message sent from the hotel's owner.  Further, there are many possible "rates" that can be identified by the `RatePlanType` attribute.  The prices are shown plus some descriptive text about each offer.  As we shall see, the "Name=Guarantee" attribute of a `RoomRateDescription` element that is a child of some `HotelRateDetail` objects will be critical at a later stage in booking, as such a rate typically requires a credit card to hold the reservation.

The output of the code for lesson 5 is to show the user the hotel selected, some details about it, and the information about the costs.  The search result above and the result below mece done based on a point of interest of "Staples Center" (in the center of Los Angeles, California).  We have selected the cheapest hotel from our shopping search, then asked for details, and chosen the lowest rate found in the details (2 nights in LA for about 170 USD!) for a non-smoking room with a queen-sized bed.

{% highlight console %}
HOWARD JOHNSON LOS ANGELES
              603 S. New Hampshire Ave. 
              Los Angeles CA 90005 US 
              Business 1 213-3854444
              Fax 1 213-3805413


[The Total includes taxes, surcharges, fees.]
[STAY 2  SAVE 15 PERCENT -.2 DOUBLE BEDS NON SMOKING ROOM WITH.FREE CONTINENTAL BREAKFAST  FREE.]

Total Price: USD171.90
{% endhighlight %}

### Reserving the room

The sequence of calls on the uAPI for reserving a hotel "shop, details, and reserve". For the third time in this sequence, we must construct and add into this 3rd request the key hotel parameters such as the number of rooms, number of adults, check-in and out dates, and choices that might affect price or availability such as `HotelBedding`.  

Since we are now making the reservation, we need to put in details about who the traveler is in the create reservation request ("req"):

{% highlight java %}

BookingTraveler traveler = new BookingTraveler();
BookingTravelerName name = new BookingTravelerName();
name.setFirst("Hugh");
name.setLast("Capet");
PhoneNumber number = new PhoneNumber();
number.setLocation("home");
number.setCountryCode("1");
number.setAreaCode("212");
number.setNumber("555-1212");
traveler.setTravelerType("ADT");
traveler.setBookingTravelerName(name);
traveler.getPhoneNumber().add(number);
req.getBookingTraveler().add(traveler);

{% endhighlight %}

In a real application, rather than a tutorial, one would almost certainly want to add in a great many more details about the traveler.  The `BookingTraveler` object has numerous fields that can accept various addresses, phone numbers, email addresses, the gender, the age of the traveler, and more.  Naturally, if there is a family traveling and booking this room in Los Angeles, one may want to have the name and details of other family members in case they should arrive separately at the hotel from Mr Capet.

We take some values we have found in the hotel "details" response, the `lowestRate` which is of type `HotelRateDetail` and the `property` which is of type `HotelProperty`, and place them into the request that we are going to use for booking the room.  This is convenient since we can be sure that we are sending all the details we know about the rate and property!

{% highlight java %}

req.setHotelRateDetail(lowestRate);
req.setHotelProperty(property);

{% endhighlight %}

### Guarantees 

A credit card number is needed to reserve a room in many cases.  Generally speaking, the card simply holds the reservation, the card is not charged immediately.  Because the card is not being charged, many providers do not validate the address of the cardholder against the credit card account--and the uAPI will generate error if you provide this detail! In `Lesson4` we have a helper routine called `getFakeCreditCard()` that will return a bogus credit card object and, optionally, attach a billing address as well.

{% highlight java %}

public static CreditCard getFakeCreditCard(boolean withAddress) {
    CreditCard cc = new CreditCard();
    TypeStructuredAddress addr = new TypeStructuredAddress();
    
    cc.setType("VI");
    cc.setNumber("4012888888881881");
    //dec 2014
    cc.setExpDate(getFactory().newXMLGregorianCalendarDate(2014, 12,
            DatatypeConstants.FIELD_UNDEFINED, 
            DatatypeConstants.FIELD_UNDEFINED));
    cc.setName("JOAN TEST");
    cc.setCVV("111");
    addr.setAddressName("Hugh Capet");
    addr.setCity("Montpellier");
    State vt = new State();
    vt.setValue("VT");
    addr.setState(vt);
    addr.setCountry("US");
    addr.setPostalCode("05602");
    addr.setAddressName("1 louvre street");
    
    if (withAddress) {
        cc.setBillingAddress(addr);
    }
    return cc;
}
{% endhighlight %}

Because the TravelPort developer program has issued credentials to you for this tutorial that are marked as "testing", the actual values in the credit card fields will not be validated.  In a real application, many checks may be made to verify with the credit card provider if this card number is associated with this owner and to check if the CCV (on the back of the card) is correct.


[< Return to Unit 2, Lesson 4](lesson_2-4.html) |
Proceed to Unit 2, Lesson 6 

[Table of Contents](index.html)
{% include JB/comments %}
{% include JB/analytics %}
