<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@page import="com.connection.*"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">

<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">
    <title>Dynamic Job Ordering and Data Recovery and Workflow Scheduling Approach in Cloud  </title>
	
	<!-- core CSS -->
    <link href="css/bootstrap.min.css" rel="stylesheet">
    <link href="css/font-awesome.min.css" rel="stylesheet">
    <link href="css/animate.min.css" rel="stylesheet">
    <link href="css/prettyPhoto.css" rel="stylesheet">
    <link href="css/main.css" rel="stylesheet">
    <link href="css/responsive.css" rel="stylesheet">
   <script type="text/javascript" src="jsnew/jquery-1.9.1.min.js"></script>
   
        <script src="jsnew/highcharts.js"></script>

<script>
    <%        

	Connection con=Dbconn.conn();
            ArrayList arr1=new ArrayList();
             ArrayList arr2=new ArrayList();
              ArrayList arr3=new ArrayList();
              double filelength=0;
   String uname="pjitendra201290@gmail.com";//(String)session.getAttribute("name");
         Statement st;
         ResultSet rs;
         String sql="select * from tbluploadtime where EmailName='"+uname+"'";
         st=con.createStatement();
         rs=st.executeQuery(sql);
         String Chunk_No_One=null,Chunk_No_Two=null,Chunk_No_Three=null,Chunk_No_Four=null;
         while(rs.next())
         {
        	 Chunk_No_One=rs.getString("Chunk_No_One");
        	 Chunk_No_Two=rs.getString("Chunk_No_Two");
        	 Chunk_No_Three=rs.getString("Chunk_No_Three");
        	 Chunk_No_Four=rs.getString("Chunk_No_Four");
            
         }
         arr3.add(Chunk_No_One);
         arr3.add(Chunk_No_Two);
         arr3.add(Chunk_No_Three);
         arr3.add(Chunk_No_Four);

         String  arr33=arr3.toString().replace("[", "").replace("]", "")
         	    .replace(", ", ",");//y

    %>
    

$(function () {
    $('#containerg').highcharts({
    	chart: {
            type: 'column'
        },
    	title: {
            text: 'File Upload Time',
            x: -20 
        },
        subtitle: {
        	text: 'Parameters: X-Axies: File Length(Bytes)',
            x: -20
        },
        xAxis: {
        	title: {
                text: 'File Length(Bytes)'
            },
            categories: ['Job_No_One','Job_No_Two','Job_No_Three','Job_No_Four']
        },
        yAxis: {
            title: {
                text: 'Time (ms)'
            },
            plotLines: [{
                value: 0,
                width: 0.5,
                color: '#808080'
            }]
        },
        tooltip: {
            valueSuffix: 'ms'
        },
        legend: {
        	
            layout: 'vertical',
            align: 'right',
            verticalAlign: 'middle',
            borderWidth: 0
        },
        series: [{
            name: 'Chunk Time(sec)',
            data: [<%=arr33%>],
            color:'red'
        }]
    });
});

</script>
</head>

<body class="homepage">

    <header id="header">
        <div class="top-bar">
            <div class="container">
                <div class="row">
                    <div class="col-sm-6 col-xs-4">
                 <nav class="navbar navbar-inverse" role="banner">
            <div class="container">
                <div class="navbar-header center wow fadeInDown">
                    <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                        <span class="sr-only">Toggle navigation</span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                    </button>
						<jsp:include page="titlepage.jsp"></jsp:include>  </div>
		
		</nav><!--/nav-->      
                    </div>
                   
                </div>
            </div><!--/.container-->
        </div><!--/.top-bar-->

        <nav class="navbar navbar-inverse" role="banner">
            <div class="container">
                
                <div class="collapse navbar-collapse ">
                    <ul class="nav navbar-nav">
                        <li class="active"><a href="UserHome.jsp">Home</a></li>
                       	<li><a href="MultipleFileUpload.jsp">FileUpload</a></li>
							<li><a href="FileDownload.jsp">File Download</a></li>
								<li><a href="file_upload_time.jsp">Job Time</a></li>
							<li><a href="Q_Learning_time.jsp">Server Reward</a></li>
							<li><a href="Main_time.jsp">System Evaluation</a></li>
							
                        <li class="dropdown">
                            <a href="#" class="dropdown-toggle" data-toggle="dropdown"><%=session.getAttribute("name")%><i class="fa fa-angle-down"></i></a>
                            <ul class="dropdown-menu">
                                <li><a href="Login">Logout</a></li> 
                            </ul>
                        </li>
                        
                                               
                    </ul>
                </div>
            </div><!--/.container-->
        </nav><!--/nav-->
		
            </div><!--/.container-->
        		
    </header><!--/header-->

 
    <section id="feature" >
        <div class="container">
           <div class="center wow fadeInDown">
                <h2>HOME PAGE</h2>
                <p class="lead">Welcome to Home, "<%=session.getAttribute("name")%>"	</p>
            </div>
<div id="containerg" style="min-width: 310px; height: 500px; max-width: 800px; margin: 0 auto"></div>

            <div class="row">
                <div class="features">
                </div><!--/.services-->
            </div><!--/.row-->    
        </div><!--/.container-->
    </section><!--/#feature-->


    <footer id="footer" class="midnight-blue">

    </footer><!--/#footer-->

    
</body>
</html>