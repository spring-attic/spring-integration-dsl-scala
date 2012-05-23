package org.springframework.integration.dsl.utils;

import org.springframework.util.Assert;
/**
 * Temporary class. Will be removed once method is migrated to {@link Conventions.springframework.core.Conventions}
 *
 * @author Oleg Zhurakousky
 *
 */
public class Conventions {

	public static String propertyNameToAttributeName(String propertyName) {
		Assert.hasText(propertyName, "'propertyName' must not be null");
		StringBuffer attributeBuffer = new StringBuffer();
		char[] propertyNameCharacterArray = propertyName.toCharArray();
		for (char ch : propertyNameCharacterArray) {
			if (Character.isUpperCase(ch)){
				if (attributeBuffer.length() == 0){
					attributeBuffer.append(Character.toLowerCase(ch));
				}
				else {
					attributeBuffer.append(Character.toLowerCase('-'));
					attributeBuffer.append(Character.toLowerCase(ch));
				}
			}
			else {
				attributeBuffer.append(Character.toLowerCase(ch));
			}
		}

		return attributeBuffer.toString();
	}
}
