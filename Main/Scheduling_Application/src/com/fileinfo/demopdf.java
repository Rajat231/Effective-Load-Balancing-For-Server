package com.fileinfo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import com.connection.Dbconn;

public class demopdf {

	public static void main(String[] args) {
		String name="TITLE15.txt";
		if(name.indexOf(".pdf")!=-1)
		{
			System.out.println("Yes");
		}
		else{
			System.out.println("NO");
		}
		
		
		int server1=0,server2=0,server3=0,server4=0,totalserver=0;
		try {
			Connection con=Dbconn.conn();
		
		Statement st= con.createStatement();
		ResultSet rs=st.executeQuery("select * from tblqlearning");
		while(rs.next())
		{
			String sid=rs.getString("Server_Id");
			String Reward_Data=rs.getString("Reward_Data");
			if(sid.equals("1") && Reward_Data.equals("1"))
			{
				server1++;
			}
			else if(sid.equals("2") && Reward_Data.equals("1"))
			{
				server2++;
			}
			else if(sid.equals("3") && Reward_Data.equals("1"))
			{
				server3++;
			}
			else if(sid.equals("4") && Reward_Data.equals("1"))
			{
				server4++;
			}
			totalserver++;
			
		}
		double s1,s2,s3,s4;
		ArrayList<Double> serverlist=new ArrayList<Double>();
		s1=(double)server1/totalserver;
		s2=(double)server2/totalserver;
		s3=(double)server3/totalserver;
		s4=(double)server4/totalserver;
		serverlist.add(s1);
		serverlist.add(s2);
		serverlist.add(s3);
		serverlist.add(s4);
		System.out.println("S1=>"+server1+"\tS2=>"+server2+"\tS3=>"+server3+"\tS4=>"+server4+"\tT=>"+totalserver);
		System.out.println("S1=>"+s1+"\tS2=>"+s2+"\tS3=>"+s3+"\tS4=>"+s4);
		double smax=Collections.max(serverlist);
		int servername=serverlist.indexOf(smax)+1;
		System.out.println(smax+"=>"+servername);
		} catch (ClassNotFoundException e) {
			// 
			e.printStackTrace();
		} catch (SQLException e) {
			// 
			e.printStackTrace();
		}
	}

}
