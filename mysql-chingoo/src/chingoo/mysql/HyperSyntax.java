package chingoo.mysql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.StringTokenizer;

public class HyperSyntax {

	static String delim = " \t(),\n;%|=><*+-";
	
	static String syntaxString1[] = {
		"ACCESSIBLE","ADD","ALL","ALTER","ANALYZE","AND","AS","ASC","ASENSITIVE","BEFORE","BETWEEN",
		"BIGINT","BINARY","BLOB","BOTH","BY","CALL","CASCADE","CASE","CHANGE","CHAR","CHARACTER","CHECK",
		"COLLATE","COLUMN","CONDITION","CONSTRAINT","CONTINUE","CONVERT","CREATE","CROSS","CURRENT_DATE",
		"CURRENT_TIME","CURRENT_TIMESTAMP","CURRENT_USER","CURSOR","DATABASE","DATABASES","DAY_HOUR",
		"DAY_MICROSECOND","DAY_MINUTE","DAY_SECOND","DEC","DECIMAL","DECLARE","DEFAULT","DELAYED","DELETE",
		"DESC","DESCRIBE","DETERMINISTIC","DISTINCT","DISTINCTROW","DIV","DOUBLE","DROP","DUAL","EACH",
		"ELSE","ELSEIF","ENCLOSED","ESCAPED","EXISTS","EXIT","EXPLAIN","FALSE","FETCH","FLOAT","FLOAT4",
		"FLOAT8","FOR","FORCE","FOREIGN","FROM","FULLTEXT","GRANT","GROUP","HAVING","HIGH_PRIORITY",
		"HOUR_MICROSECOND","HOUR_MINUTE","HOUR_SECOND","IF","IGNORE","IN","INDEX","INFILE","INNER","INOUT",
		"INSENSITIVE","INSERT","INT","INT1","INT2","INT3","INT4","INT8","INTEGER","INTERVAL","INTO","IS",
		"ITERATE","JOIN","KEY","KEYS","KILL","LEADING","LEAVE","LEFT","LIKE","LIMIT","LINEAR","LINES","LOAD",
		"LOCALTIME","LOCALTIMESTAMP","LOCK","LONG","LONGBLOB","LONGTEXT","LOOP","LOW_PRIORITY",
		"MASTER_SSL_VERIFY_SERVER_CERT","MATCH	MAXVALUE","MEDIUMBLOB","MEDIUMINT","MEDIUMTEXT","MIDDLEINT",
		"MINUTE_MICROSECOND","MINUTE_SECOND","MOD","MODIFIES","NATURAL","NOT","NO_WRITE_TO_BINLOG","NULL",
		"NUMERIC","ON","OPTIMIZE","OPTION","OPTIONALLY","OR","ORDER","OUT","OUTER","OUTFILE","PRECISION",
		"PRIMARY","PROCEDURE","PURGE","RANGE","READ","READS","READ_WRITE","REAL","REFERENCES","REGEXP","RELEASE",
		"RENAME","REPEAT","REPLACE","REQUIRE","RESIGNAL","RESTRICT","RETURN","REVOKE","RIGHT","RLIKE","SCHEMA",
		"SCHEMAS","SECOND_MICROSECOND","SELECT","SENSITIVE","SEPARATOR","SET","SHOW","SIGNAL","SMALLINT",
		"SPATIAL","SPECIFIC","SQL","SQLEXCEPTION","SQLSTATE	SQLWARNING","SQL_BIG_RESULT","SQL_CALC_FOUND_ROWS",
		"SQL_SMALL_RESULT","SSL","STARTING	STRAIGHT_JOIN","TABLE","TERMINATED","THEN","TINYBLOB","TINYINT",
		"TINYTEXT","TO","TRAILING","TRIGGER","TRUE","UNDO","UNION","UNIQUE","UNLOCK","UNSIGNED","UPDATE",
		"USAGE	USE","USING","UTC_DATE","UTC_TIME","UTC_TIMESTAMP","VALUES","VARBINARY","VARCHAR","VARCHARACTER",
		"VARYING","WHEN","WHERE","WHILE","WITH","WRITE","XOR","YEAR_MONTH","ZEROFILL"

		};

	static String syntaxString2[] = { 
		"ASCII","BIN","BIT_LENGTH","CHAR_LENGTH","CHAR","CHARACTER_LENGTH","CONCAT_WS","CONCAT","ELT","EXPORT_SET",
		"FIELD","FIND_IN_SET","FORMAT","HEX","INSERT","INSTR","LCASE","LEFT","LENGTH","LIKE","LOAD_FILE","LOCATE",
		"LOWER","LPAD","LTRIM","MAKE_SET","MATCH","MID","OCT","OCTET_LENGTH","ORD","POSITION","QUOTE","REPEAT",
		"REPLACE","REVERSE","RIGHT","RPAD","RTRIM","SOUNDEX","SPACE","STRCMP","SUBSTR","SUBSTRING_INDEX","SUBSTRING",
		"TRIM","UCASE","UNHEX","UPPER","ABS","ACOS","ASIN","ATAN2","ATAN","CEIL","CEILING","CONV","COS","COT",
		"CRC32","DEGREES","EXP","FLOOR","LN","LOG10","LOG2","LOG","MOD","PI","POW","POWER","RADIANS","RAND","ROUND",
		"SIGN","SIN","SQRT","TAN","TRUNCATE","ADDDATE","ADDTIME","CONVERT_TZ","CURDATE","CURRENT_DATE","CURRENT_TIME",
		"CURRENT_TIMESTAMP","CURTIME","DATE_ADD","DATE_FORMAT","DATE_SUB","DATE","DATEDIFF","DAY","DAYNAME",
		"DAYOFMONTH","DAYOFWEEK","DAYOFYEAR","EXTRACT","FROM_DAYS","FROM_UNIXTIME","GET_FORMAT","HOUR","LAST_DAY",
		"LOCALTIME","LOCALTIMESTAMP","MAKEDATE","MAKETIME	MAKETIME","MICROSECOND","MINUTE","MONTH","MONTHNAME","NOW",
		"PERIOD_ADD","PERIOD_DIFF","QUARTER","SEC_TO_TIME","SECOND","STR_TO_DATE","SUBDATE","SUBTIME","SYSDATE",
		"TIME_FORMAT","TIME_TO_SEC","TIME","TIMEDIFF","TIMESTAMP","TIMESTAMPADD","TIMESTAMPDIFF","TO_DAYS",
		"UNIX_TIMESTAMP","UTC_DATE","UTC_TIME","UTC_TIMESTAMP","WEEK","WEEKDAY","WEEKOFYEAR","YEAR","YEARWEEK",
		"DEFAULT","GET_LOCK","INET_ATON","INET_NTOA","IS_FREE_LOCK","IS_USED_LOCK","MASTER_POS_WAIT","NAME_CONST",
		"RAND","RELEASE_LOCK","SLEEP","UUID","VALUES","BENCHMARK","CHARSET","COERCIBILITY","COLLATION","CONNECTION_ID",
		"CURRENT_USER","DATABASE","FOUND_ROWS","LAST_INSERT_ID","ROW_COUNT","SCHEMA","SESSION_USER","SYSTEM_USER",
		"USER","VERSION"
	};
	
	static HashSet<String> syntax1 = new HashSet<String>(Arrays.asList(syntaxString1));
	static HashSet<String> syntax2 = new HashSet<String>(Arrays.asList(syntaxString2));
	
	public static ArrayList<Range> extractComments(String text) {

		ArrayList<Range> list = new ArrayList<Range>();

		// extract multiple line comments - ex: /* .... */
		int last = 0;
		int start = 0;
		while (true) {
			start = text.indexOf("/*", last);
			
			if (start < 0) break;
			
			int end = text.indexOf("*/", start+2);
			if (end < 0) break;
			
			end +=2;
			
			//System.out.println(start + " - " + (end));
			list.add(new Range(start, end, 'C'));
			last = end;
		}
		
		// extract single line comments
		last = 0;
		while (true) {
			start = text.indexOf("#", last);
			
			if (start < 0) break;
			
			int end = text.indexOf("\n", start+1);
			if (end < 0) break;

			// check if start is in between any of comment
			boolean isComment = false;
			for (int i=0;i<list.size();i++) {
				if (list.get(i).start < start && list.get(i).end > start) {
					isComment = true;
					break;
				}
			}
			if (isComment) {
				last = start+1;
				continue;
			}
			
			end += 1;
			
			//System.out.println(start + " : " + end);
			list.add(new Range(start, end, 'C'));
			last = end;
		}
		
		// extract string literals
		// extract single line comments
		last = 0;
		while (true) {
			start = text.indexOf("'", last);
			
			if (start < 0) break;

			// check if start is in between any of comment
			boolean isComment = false;
			for (int i=0;i<list.size();i++) {
				if (list.get(i).start < start && list.get(i).end > start) {
					isComment = true;
					break;
				}
			}
			if (isComment) {
				last = start+1;
				continue;
			}
			
			int end = text.indexOf("'", start+1);
			if (end < 0) break;
			
			end += 1;
			
			// System.out.println(start + " # " + end + ":" + text.substring(start,end));
			list.add(new Range(start, end, 'S'));
			last = end;
		}		
		
		Collections.sort(list, new Comparator<Range>(){
			 
            public int compare(Range o1, Range o2) {
        		return o1.start - o2.start;
            }
 
        });		
		return list;
	}

	private static ArrayList<String> getProcedureNames(String text, String type) {
		ArrayList<String> list = new ArrayList<String>();
		
		StringTokenizer st = new StringTokenizer(text, delim, true);
		String s = "";
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			
			String tmp = token.toUpperCase();
			if (tmp.equals("PROCEDURE")|| tmp.equals("FUNCTION")){
				String name="";
				while (true) {
					name = st.nextToken();
					if (!name.trim().equals("")) break;
					if(!st.hasMoreTokens()) break;
				}
				list.add(name.toUpperCase());
				
			}
			
		}
		
		return list;
	}

	public static HashSet<String> getLinkables(String key, String text) {
		HashSet<String> set = new HashSet<String>();
		
		String textU = text.toUpperCase();
		int start=0;
		int end=0;
		while (true) {
			start = textU.indexOf(key, end);
			if (start < 0) break;
			
			char ch = ' ';
			if (start-1 >=0) {
				 ch = textU.charAt(start-1);
				// System.out.println("(" + ch + ")");
				if (delim.indexOf(ch)<0 ) {
					continue;
				}
			}
			
			ch = textU.charAt(start +key.length());
			//System.out.println("(" + ch + ")");
			if (delim.indexOf(ch)<0 ) {
				continue;
			}
			
			end = textU.indexOf(";", start);
			if (end < 0) break;
			
			end++;
			
			String tmp = text.substring(start+key.length(), end).trim();
			StringTokenizer st = new StringTokenizer(tmp, delim);
			if (st.hasMoreTokens()) {
				String token = st.nextToken();
				set.add(token.toUpperCase());
			}
			
			//System.out.println("[" + text.substring(start, end) + "] (" + tmp + ")");
		}
		//System.out.println("SSSS " + set);
		
		return set;
	}
	
	// Get linkable - Global Variables
	public static HashSet<String> getLinkablesGV(String text) {
		HashSet<String> set = new HashSet<String>();
		
		String textU = text.toUpperCase();
		int start=0;
		int end=0;
		while (true) {
			start = textU.indexOf("\n", end);
			if (start < 0) break;
			
			end = textU.indexOf(";", start);
			if (end < 0) break;
			
			end++;
			
			String tmp = text.substring(start, end).trim();
			StringTokenizer st = new StringTokenizer(tmp, delim, false);
			if (st.hasMoreTokens()) {
				String token = st.nextToken();
				if (!syntax1.contains(token)) {
					set.add(token.toUpperCase());
					//System.out.println(" gv=" + token);
				}
			}
			
			//System.out.println("[" + text.substring(start, end) + "] (" + tmp + ")");
		}
		//System.out.println("GV " + set);
		
		return set;
	}

	public static HashSet<String> getProcedures(String text, ArrayList<Range> ranges, String type) {
		
		HashSet<String> set = new HashSet<String>();
		
		int start=0;
		for (Range r:ranges) {
			if (start > r.start) continue;
			String s = text.substring(start, r.start);
			ArrayList<String> list = getProcedureNames(s, type);
			set.addAll(list);
			
			start = r.end;
		}
		String s = text.substring(start);	
		ArrayList<String> list = getProcedureNames(s, type);
		set.addAll(list);
		
		//System.out.println("SET" + set);
		return set;
	}
	
	public static boolean isNumeric(String str)  
	{  
	  try  
	  {  
	    double d = Double.parseDouble(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}
	
	private String hyperSyntax(Connect cn, String text, HashSet<String> procedures, HashSet<String> GV, String type) {
		StringTokenizer st = new StringTokenizer(text, delim, true);
		StringBuffer s = new StringBuffer();
		boolean hyperlink = false;
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			
			if (token.length()==1 && token.indexOf(delim)>=0 ) {
				s.append( Util.escapeHtml(token) );
				continue;
			} 
			
			String tmp = token.toUpperCase();
			if (tmp.equals("PROCEDURE")|| tmp.equals("FUNCTION")){
				//s += "<a name='chapter'></a>"+ "<span class='syntax1'>" + token + "</span>";
				s.append( "<span class='syntax1'>" + token + "</span>" );
				if (!type.equals("PACKAGE"))
					hyperlink = true;
				
			} else if (syntax1.contains(tmp)) {
				s.append( "<span class='syntax1'>" + token + "</span>" );
			} else if (syntax2.contains(tmp)) {
				s.append( "<span class='syntax2'>" + token + "</span>" );
			} else if (isNumeric(tmp))
				s.append( "<span class='syntax3'>" + token + "</span>" );
			else if (cn.isTV(tmp))
				s.append( "<a style='color: darkblue;' href='pop.jsp?key="+tmp+"' target='_blank'>" + token + "</a>" );
			else if (cn.isRoutine(tmp))
				s.append( "<a style='color: darkblue;' target='_blank' href='src.jsp?name=" + tmp + "'>" + token + "</a>" );
			else if (hyperlink && !tmp.trim().equals("")) {
				hyperlink = false;
				s.append( "<a name='" + tmp.toLowerCase() + "'></a>"+ token );
			} else if (GV !=null && GV.contains(tmp)) {
				if (type.equals("PACKAGE"))
					s.append( "<a name='" + tmp.toLowerCase() + "'>" + token + "</a>" );
				else
					s.append( "<a style='color: darkblue;' href='#" + tmp.toLowerCase() + "'>" + token + "</a>" );
			} else if (procedures.contains(tmp))
				s.append( "<a style='color: darkblue;' href='#" + tmp.toLowerCase() + "'>" + token + "</a>" );
			else if (tmp.indexOf('.') > 0) {
				int idx = tmp.indexOf('.');
				String pkg = tmp.substring(0,idx);
				String prc = tmp.substring(idx+1);
				
				if (cn.isRoutine(pkg))
					s.append( "<a style='color: darkblue;' target='_blank' href='src.jsp?name=" + pkg + "#" + prc.toLowerCase() + "'>" + token + "</a>" );
				else
					s.append( token );
			} else {
				s.append( Util.escapeHtml(token) );
			}
			
		}
		
		return s.toString();
	}

	public String getHyperSyntax(Connect cn, String text, String type) {
		long before = System.currentTimeMillis();
		
		StringBuffer s = new StringBuffer();
		//System.out.println("type=" + type + ", size=" + text.length());

		ArrayList<Range> ranges = extractComments(text);
		long after = System.currentTimeMillis();
		//System.out.println("Elapsed Time for extractComment = " + (after - before));
		
		// build string that stripped out comment and string literals
		StringBuffer sb2 = new StringBuffer();
		int start=0;
		for (Range r:ranges) {
			if (start > r.start) continue;
			sb2.append(text.substring(start, r.start));
			start = r.end;
		}
		sb2.append( text.substring(start));
		String s2 = sb2.toString();
		//System.out.println("s2 size=" + s2.length());
		// if (s2.length()<5000) System.out.println(s2);
		
		HashSet<String> set1 = getLinkables("PROCEDURE", s2);
		HashSet<String> set2 = getLinkables("FUNCTION", s2);
		
		HashSet<String> GV = cn.tempSet; 
		if (type.equals("PACKAGE")) {
			GV = getLinkablesGV(s2);
			cn.tempSet = GV;
		}
		
		//System.out.println("GV = " +GV);
		
//		HashSet<String> procedures = getProcedures(text, ranges, type);
		HashSet<String> procedures = new HashSet<String>();
		procedures.addAll(set1);
		procedures.addAll(set2);
		
		after = System.currentTimeMillis();
		//System.out.println("Elapsed Time for getProcedures = " + (after - before));
		
		start=0;
		String className="";
		for (Range r:ranges) {
			if (start > r.start) continue;
			s.append( hyperSyntax(cn, text.substring(start, r.start), procedures, GV, type) );
			if (r.type=='C')
				className = "syn_cmt";
			else if (r.type=='S')
				className ="syn_str";
			s.append( "<span class='"+className+"'>" );
			s.append( Util.escapeHtml( text.substring(r.start, r.end) ) );
			s.append( "</span>" );
			start = r.end;
		}
		s.append( hyperSyntax(cn, text.substring(start), procedures, GV, type) );
		
		after = System.currentTimeMillis();
		if (type.equals("PACKAGE") || type.equals("PACKAGE BODY"))
			System.out.println("Elapsed Time = " + (after - before));
		
		return s.toString();
	}
	
	public ArrayList<String> getTables(Connect cn, String sql) {
		ArrayList<String> tables = new ArrayList<String>();
		
		StringTokenizer st = new StringTokenizer(sql, delim, true);
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			
			if (token.length()==1 && token.indexOf(delim)>=0 ) {
				continue;
			} 
			
			String tmp = token.toUpperCase();
			if (syntax1.contains(tmp) || syntax2.contains(tmp)) {
				continue;
			} else if (cn.isTV(tmp)) {
				if (!tables.contains(tmp))
					tables.add(tmp);
			}
		}

		return tables; 
	}
}
