package org.json;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class M2Test2 {
  
  public static void main(String[] args){
    StringBuilder bc = new StringBuilder(); //store XML file as a stringbuilder

    try{
      BufferedReader br = new BufferedReader(new FileReader("src/test/java/org/json/junit/book.xml"));
        String line = br.readLine();
        while(line != null){
            bc.append(line);
            line = br.readLine();
        }

        String xmlStringBC = bc.toString();
        br.close();
    }
    catch (FileNotFoundException e){
      e.printStackTrace();
    } 
    catch (IOException e) {
      e.printStackTrace();
    }
       
  }
}
