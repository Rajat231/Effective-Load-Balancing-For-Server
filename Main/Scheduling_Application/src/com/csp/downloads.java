package com.csp;


import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.connection.Dbconn;

public class downloads {
	public static String datafinal=null;
	public static String filedownload(String username,String filename,String servername)
	{
		try {
			int i=0;
			Connection conn = Dbconn.conn1(servername);
			Statement StInsert = conn.createStatement();
			for(int k=1;k<5;k++){
        	ResultSet rs=StInsert.executeQuery("select * from csp"+k+" where user='"+username+"' and filename='"+filename+"' ");
        	if(!rs.isBeforeFirst())
        	{
        		System.out.println("No data csp"+k); 	
        	}
        	else{
        	while(rs.next()){
        		String str=rs.getString("part");
        		filename=rs.getString("filename");
        		FileWriter fw = new FileWriter(Dbconn.filechunk+filename+"_"+i+".txt");
        		fw.write(str);    
                fw.close();  
             System.out.println("Data csp"+k);
         	}
        	// fw.close();
        	}
        	 i++;
			}
			datafinal=DecryptedFile.download(filename);
		} catch (ClassNotFoundException e) {
			// 
			e.printStackTrace();
		} catch (SQLException e) {
			// 
			e.printStackTrace();
		} catch (IOException e) {
			// 
			e.printStackTrace();
		}
		return datafinal;
	}
	public static void main(String[] args) {
		// 

		try {
			int i=0;
			String filename = null;
			Connection conn = Dbconn.conn();
			Statement StInsert = conn.createStatement();
			for(int k=1;k<5;k++){
        	ResultSet rs=StInsert.executeQuery("select * from csp"+k+" where user='jitu' and filename='title.txt' ");
        	if(!rs.isBeforeFirst())
        	{
        		System.out.println("No data csp"+k); 	
        	}
        	else{
        	while(rs.next()){
        		String str=rs.getString("part");
        		filename=rs.getString("filename");
        		FileWriter fw = new FileWriter("C:/Data/split/"+filename+"_"+i+".txt");
        		fw.write(str);    
                fw.close();  
            System.out.println(i+str);
          fw.close();
         
        	}
        	}
        	 i++;
			}
        	DecryptedFile.download(filename);
		} catch (ClassNotFoundException e) {
			// 
			e.printStackTrace();
		} catch (SQLException e) {
			// 
			e.printStackTrace();
		} catch (IOException e) {
			// 
			e.printStackTrace();
		}
	}

}
