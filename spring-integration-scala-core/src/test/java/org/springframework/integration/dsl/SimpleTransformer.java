package org.springframework.integration.dsl;

public class SimpleTransformer {
	
	public String transform(String payload){
		return payload.toUpperCase();
	}

}
