package com.fileinfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.algo.AESencrp;
import com.algo.cpuload;
import com.algo.saveserversize;
import com.connection.Dbconn;
import com.csp.EncryptFile;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.mysql.jdbc.PreparedStatement;

/**
 * Servlet implementation class multifileuplaod
 */
@WebServlet("/multifileuplaod")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, // 2MB
maxFileSize = 1024 * 1024 * 10, // 10MB
maxRequestSize = 1024 * 1024 * 50)
public class multifileuplaod extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static File finalpath;
	public static int s1 = 0, s2 = 0, s3 = 0, s4;
	public static String Filename = null, contenttype = null;
	public static Connection con;
	public static Map<Integer, Long> loadfilesize = new HashMap<Integer, Long>();
	public static long startingtime;
	public static long endingtime;
	public static long totaltime;
	static Map<Integer, String> filenamemaster = new HashMap<Integer, String>();

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public multifileuplaod() {
		super();
		//
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		//
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		PrintWriter pw = response.getWriter();
		HttpSession session = request.getSession(true);
		cpuload.chunksize.clear();
		cpuload.filenamelist.clear();
		cpuload.memoryloadlist.clear();
		cpuload.filesizelist.clear();
		filenamemaster.clear();
		// String s_id=request.getParameter("sid");
		Random randomGenerator = new Random();
		int randomInt = randomGenerator.nextInt(5) + 1;
		int sid = randomInt;// Integer.valueOf(s_id);
		cpuload.memoryload();// call cpu load
		// process only if its multipart content
		String Username = (String) session.getAttribute("name");
		String Emailid = (String) session.getAttribute("emailid");
		if (isMultipart) {

			// Create a factory for disk-based file items
			FileItemFactory factory = new DiskFileItemFactory();
			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload(factory);

			try {
				// Parse the request
				int filechunkid = 1;
				@SuppressWarnings("unchecked")
				List<FileItem> multiparts = upload.parseRequest(request);
				System.out.println(multiparts);
				if (multiparts.size() == 5)// check file select only 4
				{

					finalpath = new File(Dbconn.filepath, Username);
					finalpath.mkdir();
					for (FileItem item : multiparts) {
						if (!item.isFormField()) {
							try {
								int filesize = (int) item.getSize();
								Filename = new File(item.getName()).getName();
								contenttype = item.getContentType();
								// System.out.println(contenttype);
								loadfilesize.put(filechunkid, (long) filesize);
								if (contenttype.equals("application/pdf")) {
									String fileName = new File(item.getName())
											.getName();
									String filePath = finalpath
											+ File.separator + fileName;
									File uploadedFile = new File(filePath);
									// System.out.println(filePath);
									// saves the file to upload directory
									item.write(uploadedFile);

									String[] FileNames = Filename.split("\\.");
									String filename = FileNames[0].toString()
											+ "_" + filechunkid + ".txt";
									String filepathnew = finalpath
											+ File.separator + filename;
									pdfwriter(filename, filePath, filepathnew);
									cpuload.chunksize.add(filesize);// one file
																	// size
									cpuload.filenamelist.add(Filename);// file
																		// name
									cpuload.filesizelist.put(filechunkid,
											filesize);// file chunk id and file
														// size
									cpuload.listfilename.put(filechunkid,
											filename);// file chunk id and file
														// name

									filenamemaster.put(filechunkid, fileName);
									System.out.println("\nFirst File-ID :"
											+ filechunkid + "\tFile Size :"
											+ filesize + "\tFile Name=>"
											+ filename);
									filechunkid++;
									@SuppressWarnings("resource")
									BufferedReader br = new BufferedReader(
											new FileReader(
													new File(finalpath
															+ File.separator
															+ filename)));// file
																			// read
																			// and
																			// line
																			// by
																			// line
									StringBuilder sb1 = new StringBuilder();
									String data1;
									while ((data1 = br.readLine()) != null) {
										sb1.append(data1);
									}
									con = Dbconn.conn();
									String Encplain = AESencrp.encrypt(sb1
											.toString());
									String sql = "insert into fileinfo values(?,?,?,?,?)";// all
																							// file
																							// data
																							// and
																							// file
																							// name
									PreparedStatement prest;
									prest = (PreparedStatement) con
											.prepareStatement(sql);
									prest.setString(1, Username);
									prest.setString(2, fileName);
									prest.setString(3, Encplain);
									prest.setString(4, String.valueOf(filesize));
									prest.setString(5, contenttype);
									prest.executeUpdate();

								} // if end pdf
								else if (contenttype.equals("text/plain")) {

									String[] FileNames = Filename.split("\\.");
									String filename = FileNames[0].toString()
											+ "_" + filechunkid + ".txt";
									item.write(new File(finalpath
											+ File.separator + filename));
									cpuload.chunksize.add(filesize);// one file
																	// size
									cpuload.filenamelist.add(Filename);// file
																		// name
									cpuload.filesizelist.put(filechunkid,
											filesize);// file chunk id and file
														// size
									cpuload.listfilename.put(filechunkid,
											filename);// file chunk id and file
														// name
									System.out.println("\nFirst File-ID :"
											+ filechunkid + "\tFile Size :"
											+ filesize + "\tFile Name=>"
											+ filename);
									filechunkid++;
									@SuppressWarnings("resource")
									BufferedReader br = new BufferedReader(
											new FileReader(
													new File(finalpath
															+ File.separator
															+ filename)));// file
																			// read
																			// and
																			// line
																			// by
																			// line
									StringBuilder sb1 = new StringBuilder();
									String data1;
									while ((data1 = br.readLine()) != null) {
										sb1.append(data1);
									}
									con = Dbconn.conn();
									String Encplain = AESencrp.encrypt(sb1
											.toString());
									String sql = "insert into fileinfo values(?,?,?,?,?)";// all
																							// file
																							// data
																							// and
																							// file
																							// name
									PreparedStatement prest;
									prest = (PreparedStatement) con
											.prepareStatement(sql);
									prest.setString(1, Username);
									prest.setString(2, filename);
									prest.setString(3, Encplain);
									prest.setString(4, String.valueOf(filesize));
									prest.setString(5, contenttype);
									prest.executeUpdate();
									System.out.println("text");
								}// else if end text
								else {
									String fileName = new File(item.getName())
											.getName();
									String filePath = finalpath
											+ File.separator + fileName;
									File uploadedFile = new File(filePath);
									// System.out.println(filePath);
									// saves the file to upload directory
									item.write(uploadedFile);

									String[] FileNames = Filename.split("\\.");
									String filename = FileNames[0].toString()
											+ "_" + filechunkid + ".txt";
									String filepathnew = finalpath
											+ File.separator + filename;
									pdfwriter(filename, filePath, filepathnew);
									cpuload.chunksize.add(filesize);// one file
																	// size
									cpuload.filenamelist.add(Filename);// file
																		// name
									cpuload.filesizelist.put(filechunkid,
											filesize);// file chunk id and file
														// size
									cpuload.listfilename.put(filechunkid,
											filename);// file chunk id and file
														// name

									filenamemaster.put(filechunkid, fileName);
									System.out.println("\nFirst File-ID :"
											+ filechunkid + "\tFile Size :"
											+ filesize + "\tFile Name=>"
											+ filename);
									filechunkid++;
									@SuppressWarnings("resource")
									BufferedReader br = new BufferedReader(
											new FileReader(
													new File(finalpath
															+ File.separator
															+ filename)));// file
																			// read
																			// and
																			// line
																			// by
																			// line
									StringBuilder sb1 = new StringBuilder();
									String data1;
									while ((data1 = br.readLine()) != null) {
										sb1.append(data1);
									}
									con = Dbconn.conn();
									String Encplain = AESencrp.encrypt(sb1
											.toString());
									String sql = "insert into fileinfo values(?,?,?,?,?)";// all
																							// file
																							// data
																							// and
																							// file
																							// name
									PreparedStatement prest;
									prest = (PreparedStatement) con
											.prepareStatement(sql);
									prest.setString(1, Username);
									prest.setString(2, fileName);
									prest.setString(3, Encplain);
									prest.setString(4, String.valueOf(filesize));
									prest.setString(5, contenttype);
									prest.executeUpdate();

								}
							} catch (Exception ex) {
								System.out.println(ex);
							}
						}// if end loop
					}// for loop end
					System.out.println("Serve");
					// loadfilesize.put(filechunkid, (long) filesize);
					long filesizen = loadfilesize.get(sid);
					System.out.println(loadfilesize+"FileSize=>"+filesizen);
					long sload = cpuload.memoryloadlistnew.get(sid);
					int sizefile = (int) filesizen / 1024;
					// int loadm=(int)sload;
					int flag = 0;
					System.out.println("Server ID=>" + sid + "\tKB=>"
							+ sizefile + "\tLoad=>" + sload);
					String serverN = "";
					if (sizefile > sload) {
						ql_Penalty_Data(String.valueOf(sid));

						serverN = ql_trust_Data();
						flag = 1;
						pw.println("<html><script>alert('Based Server="
								+ serverN + "');</script><body>");
						pw.println("");
						pw.println("</body></html>");
					} else {
						ql_Reward_Data(String.valueOf(sid));
					}
					Collections.sort(cpuload.memoryloadlist);// desc cpu load
					Collections.sort(cpuload.chunksize);// desc file size

					int no = 1, k = 0;
					try {
						for (int str : cpuload.memoryloadlist) {
							System.out.println("****************");
							startingtime = System.currentTimeMillis();
							// first file chunk id return
							int chunkid = (int) getKeyFromValue(
									cpuload.filesizelist,
									cpuload.chunksize.get(k));
							String filename = cpuload.listfilename.get(chunkid);// file
																				// chunk
																				// id
																				// return
																				// file
																				// name

							System.out.println("FileName=>" + filename
									+ "\tFile-ID :" + chunkid
									+ "\tMemoryLoad :" + str + "\tFile Size :"
									+ cpuload.chunksize.get(k));
							File file1 = new File(finalpath + File.separator
									+ filename);
							@SuppressWarnings("resource")
							BufferedReader br1 = new BufferedReader(
									new FileReader(file1));// file read and line
															// by line
							StringBuilder sb1 = new StringBuilder();
							String data1;
							while ((data1 = br1.readLine()) != null) {
								sb1.append(data1);
							}

							String sql1 = "insert into vm" + no
									+ " values(?,?,?,?)";// server tables add
															// one by one file
															// name
							PreparedStatement p;
							p = (PreparedStatement) con.prepareStatement(sql1);
							p.setString(1, String.valueOf(chunkid));
							p.setString(2, filename);
							p.setString(3, sb1.toString());
							p.setString(4, cpuload.chunksize.get(k).toString());
							p.executeUpdate();
							// String vmName="VM"+no;
							String s = "Server" + no;
							long fsize = cpuload.chunksize.get(k);
							saveserversize.serversize(s, fsize);
							String servername = "server" + no;
							double LoadOfServer = 0.0;
							Statement stLoad = con.createStatement();
							ResultSet rsLoad = stLoad
									.executeQuery("select * from serverinfo where ServerName='"
											+ s + "'");
							if (rsLoad.next()) {
								LoadOfServer = rsLoad
										.getDouble("VMavailableuse");
								System.out.println("Selected Server " + s);
							}
							if (LoadOfServer < 30.0) {
								System.out
										.println("---------Selected server Migrade the file *********************");
								if (s.equals("Server1")) {
									s2++;
									save(no, filename, Username, servername,
											flag, sid, "server2", Emailid,
											chunkid);
									int s11 = 0;
									Statement sts1 = con.createStatement();
									ResultSet rsLogin;
									rsLogin = sts1
											.executeQuery("select * from serverinfo where ServerName='Server2'");
									if (rsLogin.next()) {
										s11 = Integer.valueOf(rsLogin
												.getString("assgin_jobcount"));
									}
									int sum = s2 + s11;
									Statement sts = con.createStatement();
									String str1 = "UPDATE serverinfo SET assgin_jobcount='"
											+ sum
											+ "' WHERE ServerName='Server2'";
									sts.executeUpdate(str1);
								} else if (s.equals("Server2")) {
									s3++;
									save(no, filename, Username, servername,
											flag, sid, "server3", Emailid,
											chunkid);
									int s11 = 0;
									Statement sts1 = con.createStatement();
									ResultSet rsLogin;
									rsLogin = sts1
											.executeQuery("select * from serverinfo where ServerName='Server3'");
									if (rsLogin.next()) {
										s11 = Integer.valueOf(rsLogin
												.getString("assgin_jobcount"));
									}
									int sum = s3 + s11;
									Statement sts = con.createStatement();
									String str1 = "UPDATE serverinfo SET assgin_jobcount='"
											+ sum
											+ "' WHERE ServerName='Server3'";
									sts.executeUpdate(str1);
								} else if (s.equals("Server3")) {
									s4++;
									save(no, filename, Username, servername,
											flag, sid, "server4", Emailid,
											chunkid);
									int s11 = 0;
									Statement sts1 = con.createStatement();
									ResultSet rsLogin;
									rsLogin = sts1
											.executeQuery("select * from serverinfo where ServerName='Server4'");
									if (rsLogin.next()) {
										s11 = Integer.valueOf(rsLogin
												.getString("assgin_jobcount"));
									}
									int sum = s4 + s11;
									Statement sts = con.createStatement();
									String str1 = "UPDATE serverinfo SET assgin_jobcount='"
											+ sum
											+ "' WHERE ServerName='Server4'";
									sts.executeUpdate(str1);
								} else if (s.equals("Server4")) {

									s1++;
									save(no, filename, Username, servername,
											flag, sid, "server1", Emailid,
											chunkid);
									int s11 = 0;
									Statement sts1 = con.createStatement();
									ResultSet rsLogin;
									rsLogin = sts1
											.executeQuery("select * from serverinfo where ServerName='Server1'");
									if (rsLogin.next()) {
										s11 = Integer.valueOf(rsLogin
												.getString("assgin_jobcount"));
									}
									int sum = s1 + s11;
									Statement sts = con.createStatement();
									String str1 = "UPDATE serverinfo SET assgin_jobcount='"
											+ sum
											+ "' WHERE ServerName='Server1'";
									sts.executeUpdate(str1);
								}
							}

							else {
								if (s.equals("Server1")) {
									s1++;
									int s11 = 0;
									Statement sts1 = con.createStatement();
									ResultSet rsLogin;
									rsLogin = sts1
											.executeQuery("select * from serverinfo where ServerName='"
													+ s + "'");
									if (rsLogin.next()) {
										s11 = Integer.valueOf(rsLogin
												.getString("assgin_jobcount"));
									}
									int sum = s1 + s11;
									Statement sts = con.createStatement();
									String str1 = "UPDATE serverinfo SET assgin_jobcount='"
											+ sum
											+ "' WHERE ServerName='"
											+ s
											+ "'";
									sts.executeUpdate(str1);
								} else if (s.equals("Server2")) {
									s2++;
									int s11 = 0;
									Statement sts1 = con.createStatement();
									ResultSet rsLogin;
									rsLogin = sts1
											.executeQuery("select * from serverinfo where ServerName='"
													+ s + "'");
									if (rsLogin.next()) {
										s11 = Integer.valueOf(rsLogin
												.getString("assgin_jobcount"));
									}
									int sum = s2 + s11;
									Statement sts = con.createStatement();
									String str1 = "UPDATE serverinfo SET assgin_jobcount='"
											+ sum
											+ "' WHERE ServerName='"
											+ s
											+ "'";
									sts.executeUpdate(str1);

								} else if (s.equals("Server3")) {
									s3++;
									int s11 = 0;
									Statement sts1 = con.createStatement();
									ResultSet rsLogin;
									rsLogin = sts1
											.executeQuery("select * from serverinfo where ServerName='"
													+ s + "'");
									if (rsLogin.next()) {
										s11 = Integer.valueOf(rsLogin
												.getString("assgin_jobcount"));
									}
									int sum = s3 + s11;
									Statement sts = con.createStatement();
									String str1 = "UPDATE serverinfo SET assgin_jobcount='"
											+ sum
											+ "' WHERE ServerName='"
											+ s
											+ "'";
									sts.executeUpdate(str1);

								} else if (s.equals("Server4")) {

									s4++;
									int s11 = 0;
									Statement sts1 = con.createStatement();
									ResultSet rsLogin;
									rsLogin = sts1
											.executeQuery("select * from serverinfo where ServerName='"
													+ s + "'");
									if (rsLogin.next()) {
										s11 = Integer.valueOf(rsLogin
												.getString("assgin_jobcount"));
									}
									int sum = s4 + s11;
									Statement sts = con.createStatement();
									String str1 = "UPDATE serverinfo SET assgin_jobcount='"
											+ sum
											+ "' WHERE ServerName='"
											+ s
											+ "'";
									sts.executeUpdate(str1);

								}

								String inputFile = finalpath + File.separator
										+ filename;
								EncryptFile.Chunk(inputFile, filename,
										Dbconn.filechunk, Username, servername);
								String vmname = "vm" + no;
								String Servername = null;// ="server"+no;
								if (flag == 1) {
									if (sid == no) {
										Servername = serverN;
									} else {
										Servername = "server" + no;
									}
									if (sid == no) {
										Servername = serverN;
									} else {
										Servername = "server" + no;
									}
									if (sid == no) {
										Servername = serverN;
									} else {
										Servername = "server" + no;
									}
									if (sid == no) {
										Servername = serverN;
									} else {
										Servername = "server" + no;
									}
								} else {
									Servername = "server" + no;
								}
								System.out.println("Job Number=>" + no
										+ "\tVM Name=>vm" + no
										+ "\tServer Name=>server" + no
										+ "\tFile Name=>" + filename);

								endingtime = System.currentTimeMillis();
								totaltime = endingtime - startingtime;

								System.out.println("MasterList=>"
										+ filenamemaster);
								if (contenttype.equals("application/pdf")) {
									String pdffiname = filenamemaster
											.get(chunkid);
									String sql2 = "insert into masterinfo(jobid,user,filename,servername,vmname,Time,downloadtime,u_emailid) values(?,?,?,?,?,?,?,?)";// server
									PreparedStatement p1;
									String download = "0";
									p1 = (PreparedStatement) con
											.prepareStatement(sql2);
									p1.setString(1, String.valueOf(no));
									p1.setString(2, Username);
									p1.setString(3, pdffiname);
									p1.setString(4, Servername);
									p1.setString(5, vmname);
									p1.setString(6, String.valueOf(totaltime));
									p1.setString(7, download);
									p1.setString(8, Emailid);
									p1.executeUpdate();

								} else {
									String sql2 = "insert into masterinfo(jobid,user,filename,servername,vmname,Time,downloadtime,u_emailid) values(?,?,?,?,?,?,?,?)";// server
									PreparedStatement p1;
									String download = "0";
									p1 = (PreparedStatement) con
											.prepareStatement(sql2);
									p1.setString(1, String.valueOf(no));
									p1.setString(2, Username);
									p1.setString(3, filename);
									p1.setString(4, Servername);
									p1.setString(5, vmname);
									p1.setString(6, String.valueOf(totaltime));
									p1.setString(7, download);
									p1.setString(8, Emailid);
									p1.executeUpdate();

								}
								System.out.println("Job Number=>" + no
										+ "\tStarting Time=>" + startingtime
										+ "\tEnding Time=>" + endingtime
										+ "\tTotal Time=>" + totaltime);

								if (no == 1) {
									String nos = "0";
									String sql02 = "insert into tbluploadtime(FileName,Chunk_No_One,Chunk_No_Two,Chunk_No_Three,Chunk_No_Four,EmailName) values(?,?,?,?,?,?)";// server
								PreparedStatement p01;
									p01 = (PreparedStatement) con
											.prepareStatement(sql02);
									p01.setString(1, filename);
									p01.setString(2, String.valueOf(totaltime));
									p01.setString(3, nos);
									p01.setString(4, nos);
									p01.setString(5, nos);
									p01.setString(6, Emailid);
									p01.executeUpdate();
								} else if (no == 2) {
									Statement st = con.createStatement();
									st.executeUpdate("update tbluploadtime set Chunk_No_Two='"
											+ totaltime
											+ "' where EmailName='"
											+ Emailid + "'");

								} else if (no == 3) {
									Statement st = con.createStatement();
									st.executeUpdate("update tbluploadtime set Chunk_No_Three='"
											+ totaltime
											+ "' where EmailName='"
											+ Emailid + "'");

								} else if (no == 4) {
									Statement st = con.createStatement();
									st.executeUpdate("update tbluploadtime set Chunk_No_Four='"
											+ totaltime
											+ "' where EmailName='"
											+ Emailid + "'");

								}

							}
							no++;
							k++;
						}// for loop end
					} catch (Exception e) {
						System.out.println(e);
					}

					pw.println("<html><script>alert('File Upload Successfully...');</script><body>");
					pw.println("");
					pw.println("</body></html>");
				} else {
					pw.println("<html><script>alert('Please Select Four File...');</script><body>");
					pw.println("");
					pw.println("</body></html>");
					System.out.println("File Not Select Four");
				}
			} catch (Exception e) {
				System.out.println(e);
			}

		}// if end ismultipart

		RequestDispatcher rd = request
				.getRequestDispatcher("/MultipleFileUpload.jsp");
		rd.include(request, response);
	}// post method end

	public static Object getKeyFromValue(Map<Integer, Integer> hm, Object value) {
		for (Object o : hm.keySet()) {
			if (hm.get(o).equals(value)) {
				return o;
			}
		}
		return null;
	}

	public static void pdfwriter(String filename, String filepath,
			String filepathnew) {
		try {

			String data = null;
			PdfReader pdfReader = new PdfReader(filepath);
			// Get the number of pages in pdf.
			int pages = pdfReader.getNumberOfPages();
			// Iterate the pdf through pages.
			StringBuilder sb = new StringBuilder();
			for (int i = 1; i <= pages; i++) {
				// Extract the page content using PdfTextExtractor.
				data = PdfTextExtractor.getTextFromPage(pdfReader, i);
				// Print the page content on console.
				sb.append(data);
			}
			// System.out.println(sb.toString());

			pdfReader.close();
			try {
				FileWriter fw = new FileWriter(filepathnew);
				fw.write(sb.toString());
				fw.close();
			} catch (Exception e) {
				System.out.println(e);
			}
			// System.out.println("Success...");

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void ql_Penalty_Data(String Server_Id) {
		String Reward_Data = "0";
		String Penalty_Data = "1";
		String sql02 = "insert into tblqlearning(Server_Id,Reward_Data,Penalty_Data) values(?,?,?)";// server
																									// tables
																									// add
																									// one
																									// by
																									// one
																									// file
																									// name
		PreparedStatement p01;
		try {
			con = Dbconn.conn();
			p01 = (PreparedStatement) con.prepareStatement(sql02);
			p01.setString(1, Server_Id);
			p01.setString(2, Reward_Data);
			p01.setString(3, Penalty_Data);
			p01.executeUpdate();
		} catch (SQLException e) {
			//
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			//
			e.printStackTrace();
		}
	}

	public static void ql_Reward_Data(String Server_Id) {
		String Reward_Data = "1";
		String Penalty_Data = "0";

		String sql02 = "insert into tblqlearning(Server_Id,Reward_Data,Penalty_Data) values(?,?,?)";// server
																									// tables
																									// add
																									// one
																									// by
																									// one
																									// file
																									// name
		PreparedStatement p01;
		try {
			con = Dbconn.conn();
			p01 = (PreparedStatement) con.prepareStatement(sql02);
			p01.setString(1, Server_Id);
			p01.setString(2, Reward_Data);
			p01.setString(3, Penalty_Data);
			p01.executeUpdate();
		} catch (SQLException e) {
			//
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			//
			e.printStackTrace();
		}
	}

	public static void save(int no, String filename, String Username,
			String servername, int flag, int sid, String serverN,
			String Emailid, int chunkid) {
		try {
			String inputFile = finalpath + File.separator + filename;
			EncryptFile.Chunk(inputFile, filename, Dbconn.filechunk, Username,
					servername);
			String vmname = "vm" + no;
			String Servername = null;// ="server"+no;
			if (flag == 1) {
				if (sid == no) {
					Servername = serverN;
				} else {
					Servername = "server" + no;
				}
				if (sid == no) {
					Servername = serverN;
				} else {
					Servername = "server" + no;
				}
				if (sid == no) {
					Servername = serverN;
				} else {
					Servername = "server" + no;
				}
				if (sid == no) {
					Servername = serverN;
				} else {
					Servername = "server" + no;
				}
			} else {
				Servername = "server" + no;
			}
			System.out
					.println("Job Number=>" + no + "\tVM Name=>vm" + no
							+ "\tServer Name=>server" + no + "\tFile Name=>"
							+ filename);

			endingtime = System.currentTimeMillis();
			totaltime = endingtime - startingtime;

			System.out.println("MasterList=>" + filenamemaster);
			if (contenttype.equals("application/pdf")) {
				String pdffiname = filenamemaster.get(chunkid);
				String sql2 = "insert into masterinfo(jobid,user,filename,servername,vmname,Time,downloadtime,u_emailid) values(?,?,?,?,?,?,?,?)";// server
				PreparedStatement p1;
				String download = "0";
				p1 = (PreparedStatement) con.prepareStatement(sql2);
				p1.setString(1, String.valueOf(no));
				p1.setString(2, Username);
				p1.setString(3, pdffiname);
				p1.setString(4, Servername);
				p1.setString(5, vmname);
				p1.setString(6, String.valueOf(totaltime));
				p1.setString(7, download);
				p1.setString(8, Emailid);
				p1.executeUpdate();

			} else {
				String sql2 = "insert into masterinfo(jobid,user,filename,servername,vmname,Time,downloadtime,u_emailid) values(?,?,?,?,?,?,?,?)";// server
				PreparedStatement p1;
				String download = "0";
				p1 = (PreparedStatement) con.prepareStatement(sql2);
				p1.setString(1, String.valueOf(no));
				p1.setString(2, Username);
				p1.setString(3, filename);
				p1.setString(4, Servername);
				p1.setString(5, vmname);
				p1.setString(6, String.valueOf(totaltime));
				p1.setString(7, download);

				p1.setString(8, Emailid);

				p1.executeUpdate();

			}
			System.out.println("Job Number=>" + no + "\tStarting Time=>"
					+ startingtime + "\tEnding Time=>" + endingtime
					+ "\tTotal Time=>" + totaltime);

			if (no == 1) {
				String nos = "0";
				String sql02 = "insert into tbluploadtime(FileName,Chunk_No_One,Chunk_No_Two,Chunk_No_Three,Chunk_No_Four,EmailName) values(?,?,?,?,?,?)";// server
				PreparedStatement p01;
				p01 = (PreparedStatement) con.prepareStatement(sql02);
				p01.setString(1, filename);
				p01.setString(2, String.valueOf(totaltime));
				p01.setString(3, nos);
				p01.setString(4, nos);
				p01.setString(5, nos);
				p01.setString(6, Emailid);
				p01.executeUpdate();
			} else if (no == 2) {
				Statement st = con.createStatement();
				st.executeUpdate("update tbluploadtime set Chunk_No_Two='"
						+ totaltime + "' where EmailName='" + Emailid + "'");

			} else if (no == 3) {
				Statement st = con.createStatement();
				st.executeUpdate("update tbluploadtime set Chunk_No_Three='"
						+ totaltime + "' where EmailName='" + Emailid + "'");

			} else if (no == 4) {
				Statement st = con.createStatement();
				st.executeUpdate("update tbluploadtime set Chunk_No_Four='"
						+ totaltime + "' where EmailName='" + Emailid + "'");

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String ql_trust_Data() {
		String serverN = "";
		int server1 = 0, server2 = 0, server3 = 0, server4 = 0, totalserver = 0;
		try {
			con = Dbconn.conn();

			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery("select * from tblqlearning");
			while (rs.next()) {
				String sid = rs.getString("Server_Id");
				String Reward_Data = rs.getString("Reward_Data");
				if (sid.equals("1") && Reward_Data.equals("1")) {
					server1++;
				} else if (sid.equals("2") && Reward_Data.equals("1")) {
					server2++;
				} else if (sid.equals("3") && Reward_Data.equals("1")) {
					server3++;
				} else if (sid.equals("4") && Reward_Data.equals("1")) {
					server4++;
				}
				totalserver++;

			}
			double s1, s2, s3, s4;
			ArrayList<Double> serverlist = new ArrayList<Double>();
			s1 = (double) server1 / totalserver;
			s2 = (double) server2 / totalserver;
			s3 = (double) server3 / totalserver;
			s4 = (double) server4 / totalserver;
			serverlist.add(s1);
			serverlist.add(s2);
			serverlist.add(s3);
			serverlist.add(s4);
			System.out.println("S1=>" + server1 + "\tS2=>" + server2 + "\tS3=>"
					+ server3 + "\tS4=>" + server4 + "\tT=>" + totalserver);
			System.out.println("S1=>" + s1 + "\tS2=>" + s2 + "\tS3=>" + s3
					+ "\tS4=>" + s4);
			double smax = Collections.max(serverlist);
			int servername = serverlist.indexOf(smax) + 1;

			if (servername == 1) {
				serverN = "server" + 1;
				System.out.println(smax + "=>" + servername);
			} else if (servername == 2) {
				serverN = "server" + 2;
				System.out.println(smax + "=>" + servername);
			} else if (servername == 3) {
				serverN = "server" + 3;
				System.out.println(smax + "=>" + servername);
			} else if (servername == 4) {
				serverN = "server" + 4;
				System.out.println(smax + "=>" + servername);
			}

		} catch (ClassNotFoundException e) {
			//
			e.printStackTrace();
		} catch (SQLException e) {
			//
			e.printStackTrace();
		}
		return serverN;
	}

}
