package com.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public class Dbconn {
	public static ArrayList<String> ResList = new ArrayList<String>();
	public static ArrayList<String> filetitle = new ArrayList<String>();
	public static ArrayList<String> offline2list = new ArrayList<String>();
	public static int hitCount;
	public static String data = "";

	//public static String filepath = "F:/BE2018-2019/Sachin Mundhe/PreojectCode/Project/FileData/FileWriter/";

	public static String filepath = "F:/BE2021-2022/Aniket sir/project_1(Effective Distributed Dynamic Load Balancing for the Clouds)/Code/Data/";

	public static String filechunk = filepath+"split/";


	public static String Output = filepath+"Output/";
	public Dbconn() throws SQLException {
		super();
	}

	public static Connection conn() throws SQLException, ClassNotFoundException {
		Connection con;

		Class.forName("com.mysql.jdbc.Driver");
		con = DriverManager.getConnection(
				"jdbc:mysql://localhost/scheduling_application_new", "root",
				"admin");
		return (con);
	}

	public static Connection conn1(String DbName) throws SQLException,
			ClassNotFoundException {

		String Db = "dynamicResource";

		if (DbName.equals("server1")) {
			Db = "server1";
		} else if (DbName.equals("server2")) {
			Db = "server2";
		} else if (DbName.equals("server3")) {
			Db = "server3";
		} else if (DbName.equals("server4")) {
			Db = "server4";
		}
		//System.out.println("Server Selected " + Db);
		Connection con;
		Class.forName("com.mysql.jdbc.Driver");
		con = DriverManager.getConnection("jdbc:mysql://localhost/" + Db,
				"root", "admin");

		return (con);

	}

}
