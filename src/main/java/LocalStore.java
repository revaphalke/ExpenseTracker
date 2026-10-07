import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

/** Single shared local store for the whole app. No SQL/JDBC/MySQL. */
public final class LocalStore {
    private static final Path DIR=Paths.get(System.getProperty("user.home"), ".expense-tracker");
    private static final Path FILE=DIR.resolve("data.ser");
    private final StoreData data;

    private static final class StoreData implements Serializable {
        private static final long serialVersionUID=1L;
        Map<String,UserAccount> users=new LinkedHashMap<>();
        Map<Integer,List<Transaction>> transactions=new LinkedHashMap<>();
        int nextUserId=1, nextTransactionId=1;
    }

    public LocalStore(){ data=load(); }
    private StoreData load(){
        try{
            Files.createDirectories(DIR);
            if(Files.exists(FILE)){
                try(ObjectInputStream in=new ObjectInputStream(Files.newInputStream(FILE))){
                    Object o=in.readObject(); if(o instanceof StoreData) return (StoreData)o;
                }
            }
        }catch(Exception ignored){}
        return new StoreData();
    }
    private synchronized void save(){
        try{
            Files.createDirectories(DIR); Path tmp=DIR.resolve("data.tmp");
            try(ObjectOutputStream out=new ObjectOutputStream(Files.newOutputStream(tmp))){ out.writeObject(data); }
            try{ Files.move(tmp,FILE,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE); }
            catch(AtomicMoveNotSupportedException ex){ Files.move(tmp,FILE,StandardCopyOption.REPLACE_EXISTING); }
        }catch(IOException ex){ throw new RuntimeException("Could not save local data",ex); }
    }
    public synchronized UserAccount register(String username,String password){
        String u=username==null?"":username.trim(); if(u.isEmpty()||password==null||password.isEmpty()) return null;
        String key=u.toLowerCase(Locale.ROOT); if(data.users.containsKey(key)) return null;
        UserAccount account=new UserAccount(data.nextUserId++,u,hash(password));
        data.users.put(key,account); data.transactions.put(account.id,new ArrayList<>()); save(); return account;
    }
    public synchronized UserAccount login(String username,String password){
        String u=username==null?"":username.trim(); UserAccount a=data.users.get(u.toLowerCase(Locale.ROOT));
        return a!=null && password!=null && a.passwordHash.equals(hash(password)) ? a : null;
    }
    public synchronized UserAccount getUser(int id){ for(UserAccount a:data.users.values()) if(a.id==id) return a; return null; }
    public synchronized double getMonthlyTarget(int userId){ UserAccount a=getUser(userId); return a==null?0.0:a.monthlyTarget; }
    public synchronized void setMonthlyTarget(int userId,double target){
        UserAccount a=getUser(userId);
        if(a==null) throw new IllegalArgumentException("User account not found");
        if(target<0) throw new IllegalArgumentException("Monthly target cannot be negative");
        a.monthlyTarget=target; save();
    }
    public synchronized List<Transaction> allTransactions(int userId){ return new ArrayList<>(data.transactions.getOrDefault(userId,Collections.emptyList())); }
    public synchronized Transaction add(int userId,LocalDate date,String type,String category,String description,String paymentMethod,double amount){
        Transaction t=new Transaction(data.nextTransactionId++,date,type,category,description,paymentMethod,amount);
        data.transactions.computeIfAbsent(userId,k->new ArrayList<>()).add(t); save(); return t;
    }
    public synchronized void update(int userId,int id,LocalDate date,String type,String category,String description,String paymentMethod,double amount){
        List<Transaction> list=data.transactions.get(userId); if(list==null)return;
        for(Transaction t:list) if(t.id==id){ t.date=date;t.type=type;t.category=category;t.description=description;t.paymentMethod=paymentMethod;t.amount=amount;save();return; }
    }
    public synchronized void delete(int userId,int id){ List<Transaction> list=data.transactions.get(userId); if(list!=null){list.removeIf(t->t.id==id);save();} }
    public synchronized void clearTransactions(int userId){ data.transactions.put(userId,new ArrayList<>()); save(); }
    public static String hash(String value){
        try{ byte[] b=MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)); StringBuilder s=new StringBuilder(); for(byte x:b)s.append(String.format("%02x",x)); return s.toString(); }
        catch(Exception ex){throw new IllegalStateException(ex);}
    }
}
