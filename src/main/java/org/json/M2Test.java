package org.json;
import java.io.StringReader;

public class M2Test {
    public static void main(String[] args) {
      String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
            "<contact>\n"+
            "  <nick>Crista </nick>\n"+
            "  <name>Crista Lopes</name>\n" +
            "  <address>\n" +
            "    <street>Ave of Nowhere</street>\n" +
            "    <zipcode>92614</zipcode>\n" +
            "  </address>\n" +
            "</contact>";
        
      
      xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
            "<contact>\n"+
            "  <nick>Crista </nick>\n"+
            "  <name>Crista Lopes</name>\n" +
            "  <address>\n" +
            "    <street>Ave of Nowhere</street>\n" +
            "    <zipcode>92614</zipcode>\n" +
            "  </address>\n" +
            "  <address>\n" +
            "    <street>Somewhere</street>\n" +
            "    <zipcode>92444444</zipcode>\n" +
            "  </address>\n" +
            "</contact>";
        

            //Idea 1: modify String by extracting only the XML part needed to convert to JSON

            //parse function seems to loop through the entire string to the end (with exception of <? <!...)

            JSONObject test = new JSONObject();
            test.put("try", "this");
            //System.out.println(test);
            
        try {
            JSONObject jobj = XML.toJSONObject(new StringReader(xmlString), new JSONPointer("/contact/address/street"));
            System.out.println(jobj.toString(4)); 
        } catch (JSONException e) {
            //System.out.println(e);
        }
/*
        System.out.println("-----------------------");


        //thought: try to traverse and modify the string first???
        //or use helper function once the entire JSON object has been parsed
        try {
            JSONObject replacement = XML.toJSONObject("<street>Ave of the Arts</street>\n");
            System.out.println("Given replacement: " + replacement);
            JSONObject jobj = XML.toJSONObject(new StringReader(xmlString), new JSONPointer("/contact/address/street/"), replacement);
            System.out.println(jobj); 
        } catch (JSONException e) {
            System.out.println(e);
        }
        */
    }

}