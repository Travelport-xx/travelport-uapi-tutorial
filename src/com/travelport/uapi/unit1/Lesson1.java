package com.travelport.uapi.unit1;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.ws.BindingProvider;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transport.http.auth.DefaultBasicAuthSupplier;
import org.apache.cxf.transport.http.auth.HttpAuthSupplier;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import com.travelport.schema.system_v8_0.PingReq;
import com.travelport.schema.system_v8_0.PingRsp;
import com.travelport.service.system_v8_0.SystemFaultMessage;
import com.travelport.service.system_v8_0.SystemPingPortType;
import com.travelport.service.system_v8_0.SystemService;

public class Lesson1 {

	public static void main(String[] argv) {		
		//
		// PING REQUEST
		//
		String payload= "this my payload; there are many like it but this one is mine";
		String someTraceId = "doesntmatter-8176";
		
		//set up the request parameters into a PingReq object
		PingReq req = new PingReq();
		req.setPayload(payload);
		req.setTraceId(someTraceId);
		
		try {
			//run the ping request
			PingRsp rsp = Helper.WSDLService.getPing().service(req);
			//print results.. payload and trace ID are echoed back in response
			System.out.println(rsp.getPayload());
			System.out.println(rsp.getTraceId());
			System.out.println(rsp.getTransactionId());
		} catch (SystemFaultMessage e) {
			//usually only the error message is useful, not the full stack
			//trace, since the stack trace in is your address space...
			System.err.println("Error making ping request: "+e.getMessage());
		}
	}
}
