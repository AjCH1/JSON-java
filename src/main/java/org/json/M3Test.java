package org.json;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import org.json.XML.addSWE262_String;
import org.json.XML.doNothing;
import org.json.XML.reverseString;


public class M3Test {

  public static void main(String args[]){
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
    "  <address life=\"fd\" life2=\"f22\">\n" +
    "    <street>AveA AAAA</street>\n" +
    "    <zipcode>92614</zipcode>\n" +
    "    <tel>Abby</tel>\n"+
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
    "       <tr>333333</tr>\n" +
    "   </street>\n" +
    "    <zipcode>12345</zipcode>\n" +
    "  </address>\n" +
    "  <nick>Crista </nick>\n"+
    "  <nick>Crist1 </nick>\n"+
    "  <nick>Crista2 </nick>\n"+
    "  <line life=\"fd\" life2=\"f22\">\n" +
    "    <street>AveA AAAA</street>\n" +
    "    <zipcode>77777</zipcode>\n" +
    "    <tel>Bibi</tel>\n"+
    "  </line>\n" +
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


String xmlString3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
"<contact>\n"+
"  <address>\n" +
"    <street>Ave of 1</street>\n" +
"    <zipcode>1111</zipcode>\n" +
"  </address>\n" +
"  <address>\n" +
"    <street>Ave of 2</street>\n" +
"    <zipcode>2222</zipcode>\n" +
"  </address>\n" +
"  <address>\n" +
"    <street>Ave of 3</street>\n" +
"    <zipcode>3333</zipcode>\n" +
"  </address>\n" +
"  <address>\n" +
"    <street>Ave of 4</street>\n" +
"    <zipcode>4444</zipcode>\n" +
"  </address>\n" +
"  <address>\n" +
"    <street>Ave of 5</street>\n" +
"    <zipcode>5555</zipcode>\n" +
"  </address>\n" +
"  <address>\n" +
"    <street>Ave of 6</street>\n" +
"    <zipcode>6666</zipcode>\n" +
"  </address>\n" +
"</contact>";

    try {
          
      //JSONObject jobj = XML.toJSONObject(new StringReader(xmlString), new doNothing());
      JSONObject jobj = XML.toJSONObject(new StringReader(xmlString), new addSWE262_String());
      //JSONObject jobj = XML.toJSONObject(new StringReader(xmlString), new reverseString());
      System.out.println(jobj.toString(4)); 


    } catch (JSONException e) {
        e.printStackTrace();
    }


  }
}
