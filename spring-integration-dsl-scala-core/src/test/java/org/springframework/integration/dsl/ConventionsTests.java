/**
 *
 */
package org.springframework.integration.dsl;

import org.junit.Test;

import org.springframework.integration.dsl.utils.Conventions;

/**
 * @author ozhurakousky
 *
 */
public class ConventionsTests {

	@Test
	public void testPropertyToAttribute(){
		System.out.println(Conventions.propertyNameToAttributeName("fooBarBaz"));
	}
}
