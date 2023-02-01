package org.json.junit;

 

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.json.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class MilestoneTests {

    String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
    "<contact>\n"+
    "  <nick>Crista </nick>\n"+
    "  <name>Crista Lopes</name>\n" +
    "  <address>\n" +
    "    <street>Ave of Nowhere</street>\n" +
    "    <zipcode>92614</zipcode>\n" +
    "  </address>\n" +
    "</contact>";

    String nestedXMLString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
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
    "      <life>ap</life>\n"+
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


  
    @Test 
    public void handleEmptyXML(){
        String xmlStr = "";
        try{
          XML.toJSONObject(new StringReader(xmlStr), new JSONPointer(""));
        }
        catch(IllegalArgumentException e){
          assertTrue("Empty path", true);
        }

    }

    @Test
    public void shouldHandleDoesNotStartWithRootSlash(){
     boolean isCorrect = false;
      try{
        XML.toJSONObject(new StringReader(xmlString), new JSONPointer("contact"));
      }
      catch(IllegalArgumentException e){
        isCorrect = true;
      }

      assertTrue("No root slash", isCorrect);
    }


    @Test
    public void shouldHandleDoesNotEndWithRootSlash(){
      boolean isCorrect = false;
      try{
        XML.toJSONObject(new StringReader(xmlString), new JSONPointer("/contact/"));
      }
      catch(IllegalArgumentException e){
        isCorrect = true;
      }

      assertTrue("No end slash", isCorrect);
    }

    @Test
    public void shouldHandleDoubles(){
      try{
        XML.toJSONObject(new StringReader(xmlString), new JSONPointer("/contact/address/0.0"));
        XML.toJSONObject(new StringReader(xmlString), new JSONPointer("/contact/address/-2.5"));
        XML.toJSONObject(new StringReader(xmlString), new JSONPointer("/contact/address/1.25"));
      }
      catch(IllegalArgumentException e){
        assertTrue("No double", true);
      }
    }

    @Test
    public void shouldHandleMoreThanOneDigitInPath(){
      try{
        XML.toJSONObject(new StringReader(xmlString), new JSONPointer("/contact/address/0/2"));
      }
      catch(IllegalArgumentException e){
        assertTrue("too many digits in a row", true);
      }
    }

    @Test
    public void testXMLFileTask2_M2Test_ROOT() throws Exception{
        JSONObject jobj = XML.toJSONObject(new StringReader(xmlString), new JSONPointer("/contact"));      
        JSONObject jobj2 = XML.toJSONObject(xmlString); //query

        assertEquals(jobj2.toString(), jobj.toString());
    }

    @Test
    public void testReadFromXMLFile() throws Exception{
       StringBuilder bc = new StringBuilder(); //store XML file as a stringbuilder

       BufferedReader br = new BufferedReader(new FileReader("src/test/java/org/json/junit/book.xml"));
        String line = br.readLine();
        while(line != null){
            bc.append(line);
            line = br.readLine();
        }

        String xmlStringBC = bc.toString();

        JSONObject jsonObject = XML.toJSONObject(new StringReader(xmlStringBC), new JSONPointer("/catalog"));
        System.out.println(jsonObject);

        JSONObject jsonObject2 = XML.toJSONObject(xmlStringBC);
        

        assertEquals(jsonObject2.toString(), jsonObject.toString());
    } 

  //Testing Milestone 2 Method 1
  @Test
  public void toJSONObjectWithReaderAndJSONPointer() {
    JSONObject jobj = XML.toJSONObject(new StringReader(nestedXMLString), new JSONPointer("/contact/root"));      
    JSONObject jobj2 = XML.toJSONObject(nestedXMLString); //query
    JSONPointer pointer = new JSONPointer("/contact/root");

    Object subObject = pointer.queryFrom(jobj2);

    JSONObject obj = new JSONObject();
    obj.put("root", subObject);

    assertEquals(obj.toString(), jobj.toString());
  }

  @Test
  public void toJSONObjectWithReaderAndJSONPointerOnArray() {
    JSONObject jobj = XML.toJSONObject(new StringReader(nestedXMLString), new JSONPointer("/contact/root/nick"));      
    JSONObject jobj2 = XML.toJSONObject(nestedXMLString); //query
    JSONPointer pointer = new JSONPointer("/contact/root/nick");

    Object subObject = pointer.queryFrom(jobj2);

    JSONObject obj = new JSONObject();
    obj.put("nick", subObject);

    assertEquals(obj.toString(), jobj.toString());
  }

  @Test
  public void toJSONObjectWithReaderAndJSONPointerOnArrayIndex() {
    JSONObject jobj = XML.toJSONObject(new StringReader(nestedXMLString), new JSONPointer("/contact/root/nick/2"));      
    JSONObject jobk = XML.toJSONObject(new StringReader(nestedXMLString), new JSONPointer("/contact/root/nick/1")); 
    
    JSONObject jobj2 = XML.toJSONObject(nestedXMLString); //query
    JSONPointer pointer = new JSONPointer("/contact/root/nick/2");
    JSONPointer pointer2 = new JSONPointer("/contact/root/nick/1");

    Object subObject = pointer.queryFrom(jobj2);
    Object subObject2 = pointer2.queryFrom(jobj2);

    JSONObject obj = new JSONObject();
    obj.put("nick", subObject);
    JSONObject obj2 = new JSONObject();
    obj2.put("nick", subObject2);

    assertEquals(obj.toString(), jobj.toString());
    assertEquals(obj2.toString(), jobk.toString());
  }

  //require indexing of array in middle of path
  @Test
  public void toJSONObjectWithReaderAndJSONPointerNested() {
    JSONObject jobj = XML.toJSONObject(new StringReader(nestedXMLString), new JSONPointer("/contact/root/address/0/street"));      
    
    JSONObject jobj2 = XML.toJSONObject(nestedXMLString); //query
    JSONPointer pointer = new JSONPointer("/contact/root/address/0/street");
    String subObject = (String) pointer.queryFrom(jobj2);

    JSONObject obj = new JSONObject();
    obj.put("street", subObject);

    assertEquals(obj.toString(), jobj.toString());
  }

  @Test
  public void toJSONObjectWithReaderAndJSONPointerNested2() {
    JSONObject jobj = XML.toJSONObject(new StringReader(nestedXMLString), new JSONPointer("/contact/root/address/1/zipcode"));      
    
    JSONObject jobj2 = XML.toJSONObject(nestedXMLString); //query
    JSONPointer pointer = new JSONPointer("/contact/root/address/1/zipcode");
    Object subObject =  pointer.queryFrom(jobj2);

    JSONObject obj = new JSONObject();
    obj.put("zipcode", subObject);

    assertEquals(obj.toString(), jobj.toString());
  }

  @Test
  public void toJSONObjectWithReaderAndJSONPointerInsideTag() {
    JSONObject jobj = XML.toJSONObject(new StringReader(nestedXMLString), new JSONPointer("/contact/root/address/0/life"));      
    
    JSONObject jobj2 = XML.toJSONObject(nestedXMLString); //query
    JSONPointer pointer = new JSONPointer("/contact/root/address/0/life");
    Object subObject =  pointer.queryFrom(jobj2);

    JSONObject obj = new JSONObject();
    obj.put("life", subObject);
    assertEquals(obj.toString(), jobj.toString());
  }

  //Milestone 2 Method 2
  @Test
  public void replacementWithReaderAndJSONPointer() {
    JSONObject replacement = XML.toJSONObject("<life>Ave of the Arts</life>\n");
    JSONObject jobj = XML.toJSONObject(new StringReader(nestedXMLString), new JSONPointer("/contact/root/address/0/life"), replacement);
    
    String replacedStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
    "<contact>\n"+
    "<name>Apple</name>\n"+
    "<root>\n"+
    "   <nick>Crista </nick>\n"+
    "   <nick>Crista1 </nick>\n"+
    "   <nick>Crista2 </nick>\n"+
    "  <address life=\"Ave of the Arts\" life2=\"f22\">\n" +
    "    <street>AveA AAAA</street>\n" +
    "    <zipcode>92614</zipcode>\n" +
    "    <tel>Bibi</tel>\n"+
    "  </address>\n" +
    "  <address>\n" +
    "    <street>AveB BBBBB</street>\n" +
    "    <zipcode>67890</zipcode>\n" +
    "    <tel>Tony</tel>\n"+
    "      <life>ap</life>\n"+
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

    JSONObject replacedObj = XML.toJSONObject(replacedStr);
    JSONPointer pointer = new JSONPointer("/contact");
    Object subObject =  pointer.queryFrom(replacedObj);

    JSONObject obj = new JSONObject();
    obj.put("contact", subObject);

    assertEquals(obj.toString(), jobj.toString());
  }
}
