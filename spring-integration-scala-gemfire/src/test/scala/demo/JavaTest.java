package demo;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class JavaTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ClassPathXmlApplicationContext("config.xml", JavaTest.class);
		
		boolean b = "".equals("") ? true : false;
	}

}
