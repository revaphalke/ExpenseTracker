import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;

public final class DBUtil {
    public static final String[] MONTHS={"January","February","March","April","May","June","July","August","September","October","November","December"};
    public static final String DATE_PATTERN="dd/MM/yyyy";
    private DBUtil(){}
    public static boolean isValidMonth(String month){ if(month==null)return false; for(String m:MONTHS)if(m.equalsIgnoreCase(month))return true; return false; }
    public static int monthNumber(String month){ if(month==null)return -1; for(int i=0;i<MONTHS.length;i++)if(MONTHS[i].equalsIgnoreCase(month))return i+1; return -1; }
    public static String monthNameOf(java.util.Date date){ if(date==null)return null; Calendar c=Calendar.getInstance();c.setTime(date);return MONTHS[c.get(Calendar.MONTH)]; }
    public static String defaultMonth(){ return MONTHS[LocalDate.now().getMonthValue()-1]; }
    public static java.util.Date parseDisplayDate(String text)throws ParseException{ SimpleDateFormat sdf=new SimpleDateFormat(DATE_PATTERN);sdf.setLenient(false);return sdf.parse(text==null?"":text.trim()); }
    public static java.util.Date tryParseDisplayDate(String text){ if(text==null||text.trim().isEmpty())return null;try{return parseDisplayDate(text);}catch(ParseException ex){return null;} }
    public static String formatDisplayDate(java.util.Date date){return date==null?"":new SimpleDateFormat(DATE_PATTERN).format(date);}
    public static String formatShortDate(java.util.Date date){return date==null?"":new SimpleDateFormat("dd/MM").format(date);}
    public static String today(){return new SimpleDateFormat(DATE_PATTERN).format(new java.util.Date());}
}
