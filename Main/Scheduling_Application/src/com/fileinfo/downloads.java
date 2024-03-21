package com.fileinfo;


import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.algo.AESencrp;
import com.connection.Dbconn;
import com.csp.DecryptedFile;

public class downloads {
	public static String datafinal=null;
	public static String filedownload(String username,String filename,String servername)
	{
		try {
			int i=0,t=0;
			Connection conn = Dbconn.conn1(servername);
			Statement StInsert = conn.createStatement();
			for(int k=1;k<5;k++){
        	ResultSet rs=StInsert.executeQuery("select * from csp"+k+" where user='"+username+"' and filename='"+filename+"' ");
        	if(!rs.isBeforeFirst())
        	{
        		t++;
        		System.out.println("No data csp"+k); 	
        	}
        	else{
        	while(rs.next()){
        		t++;
        		String str=rs.getString("part");
        		filename=rs.getString("filename");
        		 String Decplain = AESencrp.decrypt(str);
        		FileWriter fw = new FileWriter(Dbconn.filechunk+filename+"_"+i+".txt");
        		fw.write(Decplain);    
                fw.close();  
                if(t==1)
                {
                }
                else if(t==2)
                {
                }
                else if(t==3)
                {
                }
                else if(t==4)
                {
                }
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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//String times=ttime+","+ttime1+","+ttime2+","+ttime3+","+ttime4;
		return datafinal;
	}
	public static void main(String[] args) {
		
	}

}
