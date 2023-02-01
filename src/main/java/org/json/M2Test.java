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
            "<name>Apple</name>\n"+
            "<root>\n"+
            "   <nick>Crista </nick>\n"+
            "   <nick>Crista1 </nick>\n"+
            "   <nick>Crista2 </nick>\n"+
            "  <address life=\"fd\" life2=\"f22\">\n" +
            "    <street>AveA AAAA</street>\n" +
            "    <zipcode>92614</zipcode>\n" +
            "    <tel>Bibi</tel>\n"+
            "  </address>\n" +
            "  <address>\n" +
            "    <street>AveB BBBBB</street>\n" +
            "    <zipcode>67890</zipcode>\n" +
            "    <tel>Tony</tel>\n"+
            "    <life>ap</life>\n"+
            "  </address>\n" +
            "  <address>\n" +
            "    <street>\n" + 
            "       <tr>22222</tr>\n" +
            "   </street>\n" +
            "    <street>\n" + 
            "       <tr>333333</tr>\n" +
            "   </street>\n" +
            "    <zipcode>12345</zipcode>\n" +
            "  </address>\n" +
            "</root>\n" +
              "</contact>";

              String xmlString2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
              "<contact>\n"+
              "  <nick>Crista </nick>\n"+
              "  <name>Crista Lopes</name>\n" +
              "  <address>\n" +
              "    <street>Ave of Nowhere</street>\n" +
              "    <zipcode>92614</zipcode>\n" +
              "  </address>\n" +
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
            //JSONObject jobj = XML.toJSONObject(new StringReader(xmlString), new JSONPointer("/contact/root/address/2/street"));
            
            //System.out.println(jobj.toString(4)); 

            JSONObject job4 = XML.toJSONObject(xmlString);
            //JSONPointer pointer = new JSONPointer("/contact/root/address/2/street");
            //System.out.println(pointer.queryFrom(job4));
  
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println("-----------------------");

        
        try {
            JSONObject replacement = XML.toJSONObject("<life>Ave of the Arts</life>\n");
            System.out.println("Given replacement: " + replacement);
            JSONObject jobj = XML.toJSONObject(new StringReader(xmlString), new JSONPointer("/contact/root/address/1/life"), replacement);
            System.out.println(jobj.toString(4)); 
            
        } catch (JSONException e) {
            System.out.println(e);
        }

      
        
    }

}