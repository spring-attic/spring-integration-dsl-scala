package org.springframework.integration.dsl;

public class SimpleService {

	public String echo(String payload){
		return payload.toUpperCase();
	}

}
