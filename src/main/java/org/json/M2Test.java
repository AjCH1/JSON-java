package org.json;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
      

           // JSONPointer pointer = new JSONPointer("/contact/address");
            //JSONObject testObj = XML.toJSONObject(xmlString);
            //System.out.println(pointer.queryFrom(testObj));

        try {
          
            JSONObject jobj = XML.toJSONObject(new StringReader(xmlString3), new JSONPointer("/contact/address/1/street"));
            System.out.println(jobj.toString(4)); 

            //JSONObject job4 = XML.toJSONObject(xmlString);
            //JSONPointer pointer = new JSONPointer("/contact/root/address/2/street/0");
          
            //System.out.println(pointer.queryFrom(job4));
  
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println("-----------------------");

        
        try {

          StringBuilder bc = new StringBuilder(); //store XML file as a stringbuilder

          BufferedReader br = new BufferedReader(new FileReader("src/test/java/org/json/junit/book.xml"));
           String line = br.readLine();
           while(line != null){
               bc.append(line);
               line = br.readLine();
           }
     
           /* 
           String xmlStringBC = bc.toString();
     
           JSONObject jsonObject = XML.toJSONObject(new StringReader(xmlStringBC), new JSONPointer("/catalog/book"));
           JSONObject jsonObject2 = XML.toJSONObject(xmlStringBC);
           JSONPointer pointer = new JSONPointer("/catalog/book");
     
           Object subObject = pointer.queryFrom(jsonObject2);
     
           JSONObject obj = new JSONObject();
           obj.put("book", subObject);

           //System.out.println(obj.toString(4));
           System.out.println("BLOCK LINE");
           System.out.println(jsonObject.toString(4));
           */
            
          
            //JSONObject replacement = XML.toJSONObject("<newObject>CHANGED OBJECT</newObject>\n");
            //System.out.println("Given replacement: " + replacement);
            //JSONObject jobj = XML.toJSONObject(new StringReader(xmlString), new JSONPointer("/contact/root/address/0/tel"), replacement);
            //System.out.println(jobj.toString(4)); 
            
        } catch (JSONException e) {
            System.out.println(e);
        }
        catch(IOException e){
          System.out.println(e);
        }

      
        
    }

}