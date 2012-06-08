package org.springframework.integration.dsl;

import org.springframework.integration.Message;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: 43674823
 * Date: 07/06/12
 * Time: 09:28
 * To change this template use File | Settings | File Templates.
 */
public class Utils {
    public static Object get(Message<?> mm, String key) {
        Message<ArrayList<Map<String,String>>> message = (Message<ArrayList<Map<String,String>>>) mm;
        ArrayList<Map<String,String>> payload = message.getPayload();
        Map<String,String> map = payload.get(0);
        return map.get(key);
    }

    public static void print(Message<?> mm) {
        Message<ArrayList<Map<String,String>>> message = (Message<ArrayList<Map<String,String>>>) mm;
        ArrayList<Map<String,String>> payload = message.getPayload();
        System.out.println("Received message: " + payload.get(0).toString());
    }
}
