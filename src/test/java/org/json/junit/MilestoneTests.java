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
        String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
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

}
