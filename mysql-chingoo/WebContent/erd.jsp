<%@ page language="java" 
	import="java.util.*" 
	import="java.sql.*" 
	import="chingoo.mysql.*" 
	contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"
%>
<%
	Connect cn = (Connect) session.getAttribute("CN");

	String table = request.getParameter("tname");
	String owner = request.getParameter("owner");
	
	// incase owner is null & table has owner info
	if (owner==null && table!=null && table.indexOf(".")>0) {
		int idx = table.indexOf(".");
		owner = table.substring(0, idx);
		table = table.substring(idx+1);
	}
	
	System.out.println("owner=" + owner);
	
//	String catalog = null;
	String tname = table;
	int idx = table.indexOf(".");
/* 	if (idx>0) {
		catalog = table.substring(0, idx);
		tname = table.substring(idx+1);
	}
	if (catalog==null) catalog = cn.getSchemaName();
 */
	if (owner==null) owner = cn.getSchemaName();
 
 	String pkName = cn.getPrimaryKeyName(tname);
	ArrayList<String> pk = cn.getPrimaryKeys(owner, tname);
	if (pkName == null && owner != null) pkName = cn.getPrimaryKeyName(owner, tname);

	String pkCols = cn.getConstraintCols(owner, pkName);
	if (pkName != null && pkCols.equals(""))
		pkCols = cn.getConstraintCols(owner, pkName);
	
	List<ForeignKey> fks = cn.getForeignKeys(tname);
	if (owner != null) fks = cn.getForeignKeys(owner, tname);
	
	List<String> refTabs = cn.getReferencedTables(tname);
	
	List<TableCol> list = cn.getTableDetail(owner, tname);	
%>

<html>
<head> 
	<title>Chingoo - ERD</title>
    <script src="script/jquery-1.7.2.min.js" type="text/javascript"></script>
    <script src="script/data-methods.js?<%= Util.getBuildNo() %>" type="text/javascript"></script>

    <link rel='stylesheet' type='text/css' href='css/style.css?<%= Util.getBuildNo() %>'>
	<link rel="icon" type="image/png" href="image/chingoo-icon.png">
    
<script type="text/javascript">
	function toggleDiv(id) {
		var img = $("#img-"+ id).attr("src");
		if (img.indexOf("plus")>=0) {
			$("#img-"+ id).attr("src","image/minus.gif");
			$("#sub-"+id).slideDown();
		} else {
			$("#img-"+ id).attr("src","image/plus.gif");
			$("#sub-"+id).slideUp();
		}
	}
	
	function hideDiv(id) {
		$("#div-"+id).slideUp();
	}
	
	function openAll() {
		$("div ").each(function() {
			var divName = $(this).attr('id');
			if (divName != null && divName.indexOf("sub-")>=0) {
				$("#"+divName).slideDown();
				$("#img-"+divName.substring(4)).attr("src", "image/minus.gif");
			}
		});
	}
	
	function closeAll() {
		$("div ").each(function() {
			var divName = $(this).attr('id');
			if (divName != null && divName.indexOf("sub-")>=0) {
				$("#"+divName).slideUp();
				$("#img-"+divName.substring(4)).attr("src", "image/plus.gif");
			}
		});
	}
	
	function hideEmpty() {
		$("span ").each(function() {
			var spanName = $(this).attr('id');
			if (spanName != undefined && spanName.substring(0,7) == "rowcnt-") {
				var id = spanName.substring(7);
				var rowcnt = $("#"+spanName+".rowcountstyle").html();
				//alert('hide ' + id + " " + rowcnt);
				if (rowcnt == "0") hideDiv(id);
			}
		});
	}
	
	
	function runQuery(tab) {
		var sList = "";
		var form = "DIV_" + tab; 

		var query = "SELECT * FROM " + tab + " A";
		
		$("#sql").val(query);
		$("#FORM_query").submit();
	}
	
</script>
</head> 

<body>

<img src="image/data-link.png" align="middle"/>
<%= cn.getUrlString() %>

<br/>

<h3>ERD</h3>

<a href="Javascript:openAll()">Open All</a>&nbsp;
<a href="Javascript:closeAll()">Close All</a>&nbsp;
<a href="Javascript:hideEmpty()">Hide Empty Table</a>
<br/><br/>

<form id="FORM_query" name="FORM_query" action="query.jsp" target="_blank" method="post">
<input id="sql" name="sql" type="hidden"/>
<input name="norun" type="hidden" value="YES"/>
</form>

<div id="parentDiv" style="width: 100%; overflow:auto;">
&nbsp;

<%
HashSet <String> hsTable = new HashSet<String>();
for (ForeignKey rec: fks) {
	if (hsTable.contains(rec.rTableName)) 
		continue;
	else
		hsTable.add(rec.rTableName);
/*
for (ForeignKey rec: fks) { 
*/
	List<TableCol> list1 = cn.getTableDetail(owner, rec.rTableName);
	ArrayList<String> pk1 = cn.getPrimaryKeys(owner, rec.rTableName);
	String id = Util.getId();
%>
<div id="div-<%=id%>" style="margin-left: 20px; background-color: #ffffcc; width:220px; border: 1px solid #cccccc; float: left;">
<a href="erd.jsp?tname=<%= rec.rTableName %>"><%= rec.rTableName %></a> <span id="rowcnt-<%=id%>" class="rowcountstyle"><%= cn.getTableRowCount(rec.rTableName) %></span>
<a href="javascript:toggleDiv('<%= id %>')"><img id="img-<%=id%>" align=top src="image/plus.gif"></a>
<a href="javascript:runQuery('<%= rec.rTableName %>')"><img src="image/view.png"></a>
<a href="javascript:hideDiv('<%= id %>')">x</a>

<div id="sub-<%=id%>" style="display: none;">
<table>
<%
for (TableCol t: list1) {
	String colDisp = t.getName().toLowerCase();
	if (pk1.contains(t.getName())) colDisp = "<b>" + colDisp + "</b>";
%>
<tr>
<td width="10">&nbsp;</td>
<td>
<%= colDisp %>
</td>
<td>
<%= t.getTypeName() %>
</td>
</tr>
<% } %>
</table>
</div>
</div>
<% } %>

</div>

<% if (fks.size() >0 ) { %>
<img style="margin-left:170px;" src="image/arrow_up.jpg">
<% } %>

<%
	String id = Util.getId();
%>

<div id="mainDiv" style="margin-left: 80px; background-color: #99FFFF; width:220px; border: 1px solid #cccccc;">
<b><%= tname %></b> <span class="rowcountstyle"><%= cn.getTableRowCount(tname) %></span>
<a href="javascript:toggleDiv('<%= id %>')"><img id="img-<%=id%>" align=top src="image/minus.gif"></a>
<a href="javascript:runQuery('<%= tname %>')"><img src="image/view.png"></a>
<div id="sub-<%=id%>" style="display: block;">
<table>
<%
for (TableCol t: list) {
	String colDisp = t.getName().toLowerCase();
	if (pk.contains(t.getName())) colDisp = "<b>" + colDisp + "</b>";
%>
<tr>
<td width="10">&nbsp;</td>
<td>
<%= colDisp %>
</td>
<td>
<%= t.getTypeName() %>
</td>
</tr>
<% } %>
</table>
</div>
</div>

<% if (refTabs.size() >0 ) { %>
<img style="margin-left:170px;" src="image/arrow_up.jpg">
<% } %>

<div id="childDiv">

<% for (String tbl: refTabs) { 
	List<TableCol> list1 = cn.getTableDetail(owner, tbl);
	ArrayList<String> pk1 = cn.getPrimaryKeys(owner, tbl);
	id = Util.getId();
%>
<div id="div-<%=id%>" style="margin-left: 20px; background-color: #ffffcc; width:220px; border: 1px solid #cccccc; float: left;">
<a href="erd.jsp?tname=<%= tbl %>"><%= tbl %></a> <span id="rowcnt-<%=id%>" class="rowcountstyle"><%= cn.getTableRowCount(tbl) %></span>
<a href="javascript:toggleDiv('<%= id %>')"><img id="img-<%=id%>" align=top src="image/plus.gif"></a>
<a href="javascript:runQuery('<%= tbl %>')"><img src="image/view.png"></a>
<a href="javascript:hideDiv('<%= id %>')">x</a>

<div id="sub-<%=id%>" style="display: none;">
<table>
<%
for (TableCol t: list1) {
	String colDisp = t.getName().toLowerCase();
	if (pk1.contains(t.getName())) colDisp = "<b>" + colDisp + "</b>";
%>
<tr>
<td width="10">&nbsp;</td>
<td>
<%= colDisp %>
</td>
<td>
<%= t.getTypeName() %>
</td>
</tr>
<% } %>
</table>
</div>
</div>
<% } %>
</div>

    <script type="text/javascript">
    $(document).ready(function(){
    	openAll();
      });
    </script>

<script type="text/javascript">

  var _gaq = _gaq || [];
  _gaq.push(['_setAccount', '<%= Util.trackingId() %>']);
  _gaq.push(['_trackPageview']);
  _gaq.push(['_setDomainName', 'none']);
  
  _gaq.push(['_trackEvent', 'Erd', '<%= table %>']);

  (function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  })();

</script>


</body>
</html>