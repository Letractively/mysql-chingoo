<%@ page language="java" 
	import="java.util.*" 
	import="java.sql.*" 
	import="chingoo.mysql.*" 
	contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"
%>
<%
Connect cn = (Connect) session.getAttribute("CN");
if (cn==null) return;
%>
<%= cn.getConnectMessage() %>
