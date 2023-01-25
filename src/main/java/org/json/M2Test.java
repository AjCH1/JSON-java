package org.json;
import java.io.StringReader;

public class M2Test {
    public static void main(String[] args) {
      String xmlString4 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
            "<contact>\n"+
            "  <nick>Crista </nick>\n"+
            "  <address>\n" +
            "    <street>Ave of Nowhere</street>\n" +
            "    <zipcode>92614</zipcode>\n" +
            "  </address>\n" +
            "  <address>\n" +
            "    <street>Ave of Nowhere22</street>\n" +
            "    <zipcode>92614</zipcode>\n" +
            "  </address>\n" +
            "  <name>Crista Lopes</name>\n" +
            "</contact>";
        
       
      String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
            "<contact>\n"+
            "<root>\n"+
            "  <nick>Crista </nick>\n"+
            "  <nick>Crista2 </nick>\n"+
            "  <nick>Crista3 </nick>\n"+
            "  <name>Crista Lopes</name>\n" +
            "  <address life=\"fd\">\n" +
            "    <street>Ave of Nowhere</street>\n" +
            "    <zipcode>92614</zipcode>\n" +
            "  </address>\n" +
            "  <address>\n" +
            "    <street>\n" + 
            "       <tr>12345</tr>\n" +
            "   </street>\n" +
            "    <zipcode>12345</zipcode>\n" +
            "  </address>\n" +
            "</root>\n" +
              "</contact>";

              String xmlString2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
              "<contact>\n"+
              "<root>\n" +
              "  <nick>Crista </nick>\n"+
              "  <name>Crista Lopes</name>\n" +
              "  <address>\n" +
              "    <street>Ave of Nowhere</street>\n" +
              "    <zipcode>92614</zipcode>\n" +
              "  </address>\n" +
              " </root>\n"+
              "</contact>";
        

            //Idea 1: modify String by extracting only the XML part needed to convert to JSON
            //Problems: Difficulty identifying if path leads to JSONArray vs JSONObject in string

            //Idea 2: skip parsing until reach end of path (set false conditions in overloaded parse())
            //Diffculty: Dealing with potential JSONArray index case 


            
            //NOTES
            //index-single
            //no index
            //parse <address> ... </address> As an array...?
            // /contact/0 implies theres more than 1 <contact> tag

           // JSONPointer pointer = new JSONPointer("/contact/address");
            //JSONObject testObj = XML.toJSONObject(xmlString);
            //System.out.println(pointer.queryFrom(testObj));

        try {
            JSONObject jobj = XML.toJSONObject(new StringReader(xmlString), new JSONPointer("/contact/root/nick"));
            System.out.println(jobj.toString(4)); 
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println("-----------------------");

        /* 
        //thought: try to traverse and modify the string first???
        //or use helper function once the entire JSON object has been parsed
        try {
            JSONObject replacement = XML.toJSONObject("<street>Ave of the Arts</street>\n");
            System.out.println("Given replacement: " + replacement);
            JSONObject jobj = XML.toJSONObject(new StringReader(xmlString), new JSONPointer("/contact/address/street"), replacement);
            System.out.println(jobj.toString(4)); 
        } catch (JSONException e) {
            System.out.println(e);
        }

        */
        
    }

}