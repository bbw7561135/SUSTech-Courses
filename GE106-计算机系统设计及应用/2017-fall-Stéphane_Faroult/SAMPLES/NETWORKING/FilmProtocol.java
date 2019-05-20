import java.net.*;
import java.io.*;
import java.util.Calendar;
import java.sql.*;

public class FilmProtocol {
    private static Connection con = null;

    public FilmProtocol(Connection cnx) {
        con = cnx;
    }

    private static String capitalize(String str) {
        if (str.length() > 1) {
          // Beware of hyphens
          StringBuffer s = new StringBuffer();
          String[]     bits = str.split("-");
          for (int i = 0; i < bits.length; i ++) {
            if (i > 0) {
              s.append('-');
            }
            s.append(bits[i].trim().substring(0,1).toUpperCase()
                + bits[i].trim()
                         .substring(1,bits[i].trim().length())
                         .toLowerCase());
          }
          return s.toString();
        } else {
          return str.toUpperCase();
        }
    }

    private String runQuery(String query) {
        StringBuffer result = new StringBuffer();
        int          rowCount = 0;

        try {
          PreparedStatement stmt = con.prepareStatement(query);
          ResultSet         rs = stmt.executeQuery();
          ResultSetMetaData info = rs.getMetaData();
          int               cols = info.getColumnCount();
          for (int i = 1; i <= cols; i++) {
            if (i > 1) {
              result.append('\t');
            }
            result.append(info.getColumnLabel(i));
          }
          result.append('\n');
          while (rs.next()) {
            for (int i = 1; i <= cols; i++) {
              if (i > 1) {
                result.append('\t');
              }
              if (rs.getString(i) != null) {
                result.append(rs.getString(i));
              }
            }
            result.append('\n');
            rowCount++;
          }
          rs.close();
          if (rowCount == 0) {
            return "*** No film found ***";
          }
        } catch (Exception e) {
           return e.getMessage() + "\n" + query;
        }
        return result.toString();
    }

    public String processInput(String theInput) {
        // We expect a condition as
        // KEYWORD cond[|cond ...][, OTHER_KEYWORD cond[|cond ...] ...]
        String[]     conditions = theInput.split(",");
        String[]     values;
        String[]     tokens;
        String       keyWord;
        boolean      cmdOK = true;  // Be positive
        String       theOutput = "";
        StringBuffer sqlCond = new StringBuffer("select m.movieid,"
                                     +"count(*) rank\n"
                                     +"from movies m\n"
                                     +" join countries c\n"
                                     +"   on c.country_code=m.country\n");
        StringBuffer sqlCondWhere = new StringBuffer("where ");
        boolean      sqlCondStart = true;
        int          id = 0;
        int          firstT; // Index of first token

        try {
          for (String cond: conditions) {
            if (cond.trim().length() > 0) {
              values = cond.trim().split("\\|");
              tokens = values[0].trim().split("\\s");
                     // \\s represents one or several spaces,
              // Check that the first token is a valid keyword
              keyWord = tokens[0].toUpperCase();
              switch (keyWord) {
                case "TITLE":
                   if (tokens.length == 1) {
                     throw new Exception("missing parameter after "
                                             +keyWord); 
                   }
                   if (!sqlCondStart) {
                     sqlCondWhere.append("\nand ");
                   } else {
                     sqlCondStart = false;
                   }
                   sqlCondWhere.append("m.movieid in(select movieid\n"
                             +"from searchable_title\n"
                             +"where title match '");
                   firstT = 1;
                   for (String val: values) {
                     tokens = val.split("\\s");
                     if (firstT == 0) {
                       sqlCondWhere.append("or title match '");
                     }
                     for (int t = firstT; t < tokens.length; t++) {
                       if (t > firstT) {
                         sqlCondWhere.append(' ');
                       }
                       sqlCondWhere.append(tokens[t].trim());
                     }
                     sqlCondWhere.append("'\n");
                     firstT = 0;
                   }
  
                   sqlCondWhere.append(")");
                   break;
                case "ACTOR":
                case "ACTRESS":
                case "DIRECTOR":
                   if (tokens.length == 1) {
                     throw new Exception("missing parameter after "
                                             +keyWord); 
                   }
                   if (!sqlCondStart) {
                     sqlCondWhere.append("\nand (");
                   } else {
                     sqlCondWhere.append('(');
                     sqlCondStart = false;
                   }
                   sqlCond.append(" left join credits c"
                                 +Integer.toString(id)+"\n"
                                 +"   on c"+Integer.toString(id)
                                 +".movieid=m.movieid\n"
                                 +"  and c"+Integer.toString(id)
                                 +".credited_as="
                                 +(keyWord.equals("DIRECTOR") ? "'D'" : "'A'")
                                 +" left join people p"
                                 +Integer.toString(id)+"\n"
                                 +"   on p"+Integer.toString(id)
                                 +".peopleid=c"+Integer.toString(id)
                                 +".peopleid\n");
                   boolean firstValue = true;
                   for (String val: values) {
                     tokens = val.split("\\s");
                     firstT = 1;
                     // Try every possible combination
                     // and DON'T assume anything about
                     // the respective positions of the first name
                     // and of the surname, unless a single name
                     // is provided (and yet, not quite right with
                     // some South Indians)
                     if (!firstValue) {
                       sqlCondWhere.append(" or ");
                     } else {
                       firstValue = false;
                     }
                     if (tokens.length == firstT + 1) {
                       sqlCondWhere.append("(p"+Integer.toString(id)
                                           +".surname='"
                                           +capitalize(tokens[firstT].trim())
                                           +"')");
                     } else {
                       StringBuffer name1 = new StringBuffer();
                       StringBuffer name2 = new StringBuffer();
                       for (int k = firstT; k < tokens.length - 1; k++) {
                         if (k > firstT) {
                           sqlCondWhere.append(" or ");
                         }
                         name1.delete(0, name1.length());
                         name2.delete(0, name2.length());
                         for (int t = firstT; t <= k; t++) {
                           if (t > firstT) {
                             name1.append(' ');
                           }
                           name1.append(capitalize(tokens[t].trim()));
                         }
                         for (int t2 = k+1; t2 < tokens.length; t2++) {
                           if (t2 > k+1) {
                             name2.append(' ');
                           }
                           name2.append(capitalize(tokens[t2].trim()));
                         }
                         sqlCondWhere.append("(p"+Integer.toString(id)
                                             +".first_name='"
                                             +name1+"' and p"
                                             +Integer.toString(id)
                                             +".surname='"
                                             +name2+"' or p"
                                             +Integer.toString(id)
                                             +".first_name='"
                                             +name2+"' and p"
                                             +Integer.toString(id)
                                             +".surname='"
                                             +name1+"')\n");
                       }
                     }
                   }
                   sqlCondWhere.append(')');
                   id++;
                   break;
                case "COUNTRY":
                   if (tokens.length == 1) {
                     throw new Exception("missing parameter after "
                                             +keyWord); 
                   }
                   if (!sqlCondStart) {
                     sqlCondWhere.append("\nand ");
                   } else {
                     sqlCondStart = false;
                   }
                   firstT = 1;
                   for (String val: values) {
                     tokens = val.split("\\s");
                     if (firstT == 0) {
                       sqlCondWhere.append("or ");
                     } else {
                       sqlCondWhere.append("(");
                     }
                     if (tokens.length == 1 + firstT) {
                       sqlCondWhere.append("m.country=lower('"
                                           +tokens[firstT].trim()
                                           +"') or c.country_name='"
                                           +capitalize(tokens[firstT].trim())
                                           +"'\n");
                     } else {
                       sqlCondWhere.append("c.country_name='");
                       for (int t = firstT; t < tokens.length; t++) {
                         if (t > firstT) {
                           sqlCondWhere.append(' ');
                         }
                         sqlCondWhere.append(capitalize(tokens[t].trim()));
                       }
                       sqlCondWhere.append("'");
                     } 
                     firstT = 0;
                   }
                   sqlCondWhere.append(")\n");
                   break;
                case "YEAR":
                   if (tokens.length == 1) {
                     throw new Exception("missing parameter after "
                                             +keyWord); 
                   }
                   if (!sqlCondStart) {
                     sqlCondWhere.append("\nand ");
                   } else {
                     sqlCondStart = false;
                   }
                   sqlCondWhere.append("year_released in(");
                   for (int c = 1; c < tokens.length; c++) {
                     if (c > 1) {
                       sqlCondWhere.append(',');
                     }
                     try {
                        int year = Integer.parseInt(tokens[c]);
                        int thisYear =
                             Calendar.getInstance().get(Calendar.YEAR);
                        if (year < 1895 || year > thisYear) {
                         throw new Exception("invalid year "
                                             +tokens[c]+" - must be between "
                                             +"1895 and "
                                             +Integer.toString(thisYear)
                                             +" inclusive"); 
                        }
                     } catch (NumberFormatException e) {
                         throw new Exception("invalid number \""
                                             +tokens[c]+"\""); 
                     }
                     sqlCondWhere.append(tokens[c]);
                   }
                   sqlCondWhere.append(")\n");
                   break;
                case "BYE":
                   theOutput = "Goodbye";
                   cmdOK = false;  // Not really, but just to avoid the query
                   break;
                default:
                   // Invalid
                   cmdOK = false;
                   if ((keyWord != null) && (keyWord.trim().length() > 0)) {
                     theOutput = "Invalid command \"" + keyWord + "\".\n"
                               + "Valid values are TITLE, DIRECTOR, ACTOR,"
                               + "ACTRESS, COUNTRY, YEAR ... and BYE to quit";
                   }
                   break;
              }
            } else {
              cmdOK = false; // No input
            }
          }
        } catch (Exception e) {
          cmdOK = false;
          theOutput = e.getMessage();
        }
        if (cmdOK) {
          // Build the query
          StringBuffer query = new StringBuffer();
          //
          query.append("select m.title,c.country_name,m.year_released,\n"
                      +"group_concat(distinct case when pd.first_name"
                      +" is null then ''\n"
                      +"  else pd.first_name||' '\n"
                      +" end||pd.surname) as directors,\n"
                      +"group_concat(distinct case when pa.first_name"
                      +" is null then ''\n"
                      +"  else pa.first_name||' '\n"
                      +" end||pa.surname) as actors,\n"
                      +"group_concat(distinct a.title) as other_titles\n"
                      +"from (");
          query.append(sqlCond);
          query.append(sqlCondWhere);
          query.append("\ngroup by m.movieid) x\n"
                      +" join movies m\n"
                      +"  on m.movieid=x.movieid\n"
                      +" join countries c\n"
                      +"  on c.country_code=m.country\n"
                      +" left outer join credits crd\n"
                      +"  on crd.movieid=m.movieid\n"
                      +" and crd.credited_as='D'\n"
                      +" left outer join people pd\n"
                      +"  on pd.peopleid=crd.peopleid\n"
                      +" left outer join credits cra\n"
                      +"  on cra.movieid=m.movieid\n"
                      +" and cra.credited_as='A'\n"
                      +" left outer join people pa\n"
                      +" on pa.peopleid=cra.peopleid\n"
                      +" left outer join alt_titles a\n"
                      +" on a.movieid=m.movieid\n"
                      +"group by x.rank,\n"
                      +" m.title,\n"
                      +" c.country_name,\n"
                      +" m.year_released\n"
                      +"order by x.rank desc, m.year_released\n");
           theOutput = runQuery(query.toString());
        }
        return theOutput;
    }
}
