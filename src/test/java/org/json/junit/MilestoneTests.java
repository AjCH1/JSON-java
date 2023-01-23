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

    @Test
    public void handleEmptyXML() {

        String xmlStr = "";
        JSONObject jsonObject = XML.toJSONObject(new StringReader(xmlStr), new JSONPointer("/df"));
        assertTrue("jsonObject should be empty", jsonObject.isEmpty());
    }

    @Test
    public void testXMLFileTask2_M2Test() throws Exception{
        String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
            "<contact>\n"+
            "  <nick>Crista </nick>\n"+
            "  <name>Crista Lopes</name>\n" +
            "  <address>\n" +
            "    <street>Ave of Nowhere</street>\n" +
            "    <zipcode>92614</zipcode>\n" +
            "  </address>\n" +
            "</contact>";

        JSONObject jobj = XML.toJSONObject(new StringReader(xmlString), new JSONPointer("/contact/address/street"));      
        JSONObject jobj2 = XML.toJSONObject(xmlString); //query

        assertEquals(jobj2.toString(), jobj.toString());
    }

    @Test
    public void testXMLFile() throws Exception{
       StringBuilder sb = new StringBuilder(); //store XML file as a stringbuilder

       BufferedReader br = new BufferedReader(new FileReader("src/test/java/org/json/junit/book.xml"));
        String line = br.readLine();
        while(line != null){
            sb.append(line);
            line = br.readLine();
        }

        String xmlString = sb.toString();

        JSONObject jsonObject = XML.toJSONObject(new StringReader(xmlString), new JSONPointer("/"));
        System.out.println(jsonObject);

        JSONObject jsonObject2 = XML.toJSONObject(xmlString);

        assertEquals(jsonObject2.toString(), jsonObject.toString());
    } 

}
