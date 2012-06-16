---
layout: page
title: "Making an Air or Hotel booking"
description: "Understanding how to create a booking for air, hotel, or both with the Universal API."
---
{% include JB/setup %}

## Unit 2, Lesson 5

### Objective of Lesson 5

In this lesson we are going to focus on the mechanics of actually making a booking, and having that reservation handled by Travelport.

We will discuss Hotel bookings in some depth, and refer to the similarities with making Air reservations.  

### Shopping and reality

For air travel, [Lesson 3](lesson_1-3.html) discussed how to perform a _low fare search_, usually just called _shopping_. [Lesson 4](lesson_2_4.html) did something similar for hotel rooms shopping.

Shopping differs from [availability and pricing](lesson_1-2.html) not only because it combines the two processes in one step, but also because the technology underlying it is quite different.

In particular, almost all providers of search services --- from internet search engines like [Google](http://google.com) to comparison shopping tools for consumer goods like [PriceGrabber](http://www.pricegrabber.com) --- take some shortcuts in an effort to produce the lowest-priced result as quickly as possible.  Although the techniques are different by both industry and shopping provider, typically there is a need to use caching techniques so the shopping provider does not have to do live queries for all of the items that it might propose as results. 

In the particular case of buying travel-related _live_ inventory, a particular result from a shopping request (`LowFareSearchReq` producing a `LowFareSearchRsp`) may be "out of date".  For this reason, you were cautioned in the previous lesson that it is good practice with the uAPI to follow up a result gained from a shopping request with an additional `AirPricingReq` to insure that the inventory is still available and that the price has not changed.

A deeper reason than caching, for this need to "verify" that a particular price is available or has not changed, is because the uAPI is a truly real-time system with thousands of concurrent users.  It is more than likely that during the time your program spends processing results from a shopping requests, many other pieces of software are connected to Travelport and actively changing the inventory that is available.

Further, the owners of the inventory can, and do, change their prices frequently, and this also can occur concurrently with your program's operation.

The endgame of managing live inventory is the booking step.

When doing a booking request, all possible checks are done to the requested item to be reserved: this is to ensure that the object is still available, has been priced correctly with the provided fares, and any taxes are added correctly.

Further, the system must also know _who_ is making the booking, and how the traveler will pay for the booking.  The uAPI will validate all of the values provided very carefully, to ensure that a final booking is only made when all the data are correct.


### Hotel details

The sequence of calls to the uAPI for reserving a hotel is:
1. shop
2. details
3. reserve

Generally, the values returned from the Air service are more complex to parse and display to the user, because of the broader range of products (including rail) and the much larger set of possible, applicable fares.  For a hotel room, there are fewer variables so the responses returned are somewhat simpler to process and display.

Although hotel inventory is simpler, the results from a shopping request, such as those shown in lesson 4, are not sufficient to create a booking.  Once a particular hotel of interest is found with a shopping request, it should be followed up to get the detailed room and pricing information, as well as more details about the property that are likely of interest to any traveler, like the full address, phone numbers, etc.

When the `HotelDetailsServicePortType` is invoked with a `HotelDetailsReq`, the parameters that are needed are quite similar to a shopping request but the result includes many different options in terms of pricing and, in some cases, other marketing information from the hotel.  

Let's look "under the hood" to see part of the XML result that is passed back to our client program when we ask for details about a particular property:

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

This is far from all the `hotel:HotelRateDetail` entities encoded in this single XML message!

The early part of this snippet shows some of the detailed data about the property, and the marketing message sent from the hotel's owner.  Further, there are many possible _rates_ that can be identified by the `RatePlanType` attribute.  The prices are shown with descriptive text about each option.  As we shall see, the `Name="Guarantee"` attribute of a `RoomRateDescription` element (child of a `HotelRateDetail` object), will be critical at a later stage in booking, as such a rate typically requires a credit card to hold the reservation.

The output of the code for `lesson5` is to show the user the selected hotel, some details about it, and the information about room rates.  The search result above and the result below are done based on the point of interest "Staples Center" (in the center of Los Angeles, California).  We have selected the cheapest hotel from our shopping search, then asked for details, and chosen the lowest rate found in the details (2 nights in LA for about 170 USD!), for a non-smoking room, with a queen-sized bed.

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

For the third step in the hotel booking sequence, we must construct a new request with the key hotel parameters such as the number of rooms, number of adults, check-in and out dates, and choices that might affect price or availability such as `HotelBedding`. 

The two objects for making bookings are called `AirCreateReservationPortType` and `HotelReservationServicePortType`, with the naturally named requests `AirCreateReservationReq` and `HotelCreateReservationReq` and the matching response (`Rsp`) types for getting results.

Since we are now making the reservation, we need to put in details about who the traveler is in the create reservation request:

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

In a real application, rather than a tutorial, one would almost certainly want to add many more details about the traveler.  The `BookingTraveler` object has numerous fields that can accept multiple addresses, phone numbers, email addresses, the gender, the age of the traveler, and more.

If there is a family traveling and booking this room in Los Angeles, the hotel reception may want to have the name and details of other family members in case they should arrive separately at the hotel from Mr Capet.

We can take some values found in the "hotel details" response, and place them into the request that we are going to use for booking the room:
* the `lowestRate` which is of type `HotelRateDetail`
* the `property` which is of type `HotelProperty`

This is convenient, since we can be sure that we are sending all the details we know about the rate and property.

{% highlight java %}

req.setHotelRateDetail(lowestRate);
req.setHotelProperty(property);

{% endhighlight %}

### Guarantee

A credit card number is needed to reserve a room in most cases.

Generally speaking, the card simply holds the reservation, and is not charged immediately. Later on, the property might charge penalties to the card in case of late cancellations or no-show.

Because the card is not being charged, many providers do not validate the address of the cardholder against the credit card account --- and the uAPI might generate error if you provide this detail.

In `lesson4` we have a helper routine called `getFakeCreditCard()` that will return a bogus credit card object and, optionally, attach a billing address as well.

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

If you are implementing this tutorial using "testing" credentials issued by Travelport, the actual values in the credit card fields will not be validated.

In a real application, many checks will be made to verify with the credit card provider if this card number is associated with this owner, and to check if the CCV code (the code on the back of the card) is correct.

----------------------

[< Return to Unit 2, Lesson 4](lesson_2-4.html) |
[Proceed to Unit 3, Lesson 6 >](lesson_3-6.html) 

[Table of Contents](index.html)
<hr>

{% include JB/comments %}
{% include JB/analytics %}
