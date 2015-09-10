/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package xal.app.ioc2db;

/**
 *
 * @author Mariano J. Padilla
 */
public class AppResources {
    private int type=0;
    private String [] daDbfile=null;
    private String daDbfilename="";
    private String host = "";
    private String [] pvs=null;
    private String [][] oraclepvs=null;
    private String [] xlsfile=null;
    private String [] dbfile=null;
    private String [][] records=null;
    private String [][] dbfields=null;
    private String xlsfilename="";
    private String dbfilename="";
    private boolean set = false;

    public AppResources(){
        pvs = null;
        daDbfile=null;
        daDbfilename="";
        xlsfile=null;
        dbfile = null;
        xlsfilename = "";
        dbfilename = "";
        daDbfile=null;
        daDbfilename="";
        oraclepvs=null;
        set = false;
        type=0;
    }
    public boolean setall(String [] ipvs, String[] ixlscont, String [] idbfile, String ixls, String idb,String ihost){
        if((ipvs.length>0)&&(idbfile.length>0)&&(ixls!=null)&&(idb!=null)){
            host = ihost;
            pvs = ipvs;
            xlsfile = ixlscont;
            dbfile = idbfile;
            xlsfilename = ixls;
            dbfilename = idb;
            set = true;
            type = 2;
            setRecords();
            parseOraclePvs();
        }
        return set;
    }
    public boolean setall(String [] ipvs, String [] idaDb, String ihost,String idaDbfilename){
        if((ipvs.length>0) && (idaDb.length>0)){
            host = ihost;
            daDbfilename=idaDbfilename;
            pvs = ipvs;
            daDbfile = idaDb;
            type = 1;
            set = true;
            setRecords();
            parseOraclePvs();
        }
        return set;
    }
    // <editor-fold defaultstate="collapsed" desc="db File Parser">
    private void setFields(String fields){
        String [] items = fields.split("}");
        int maxfields = 0;
        for(int x=0;x<items.length;x++){
            String [] items2 = items[x].split("\n");
            if((x==0) || (maxfields<items2.length)){
                maxfields = items2.length;
            }
            items2 = null;
        }
        dbfields = new String[items.length][maxfields+1];
        String item="";
        for(int x=0;x<items.length;x++){
            String [] items2 = items[x].split("\n");
            for(int y = 0;y<items2.length;y++){
                item = items2[y].replaceAll("	field", "");
                item = item.replaceAll("A\\)", "");
                item = item.replaceAll("a\\)", "");
                item = item.replaceAll("\\)", "");
                item = item.replaceAll("\\(", "");
                item = item.replaceAll("\\@", "");
                item = item.replaceAll("\\$", "");
                item = item.replaceAll("\"", "");
                dbfields[x][y+1] = item;
                dbfields[x][0] = Integer.toString(items2.length);
            }
        }
        item = null;
    }
    private void setRecords(){
        int y=0;
        //System.out.println(dbfile.length);
        String myRecords="";
        String myFields="";
        if(type==2){
            for(int x =0; x<dbfile.length;x++){
                if (dbfile[x].indexOf("$(SS)")>0){
                    y=x+1;
                    //System.out.println(y);
                    int fld=0;
                    myRecords+=dbfile[x]+"\n";
                    while(dbfile[y].indexOf("}")==-1){
                        //System.out.println(dbfile[y]+dbfile[y].indexOf("}"));
                        if(dbfile[y].indexOf("field")>0){
                            //System.out.println(dbfile[y].indexOf("field"));
                            myFields+=dbfile[y]+"\n";
                        }
                        y++;
                        fld++;
                    }
                    myFields+=dbfile[y];
                    myRecords = myRecords.replaceAll("record","");
                    myRecords = myRecords.replaceAll("\\)","");
                    myRecords = myRecords.replaceAll("\\(","");
                    myRecords = myRecords.replaceAll("\"","");
                    x=y;
                    y=0;
                }
            }
            String [] recs = myRecords.split("\n");
            myRecords=null;
            //fields = myFields.split("}");
            records = new String[recs.length][2];
            for(int x=0;x<recs.length;x++){
                String [] items = recs[x].split(",");
                String [] items2 = items[1].split(":");
                records[x][0]=items[0];
                records[x][1]=items2[items2.length-1];
                items = null;
                items2 = null;
            }
            setFields(myFields);
        }else if(type==1){
            String [] items=null;
            records = new String[pvs.length][2];
            y=0;
            for(int x = 1;x<daDbfile.length;x++){
                if(daDbfile[x].startsWith("RECORD")){
                     items = daDbfile[x].split("\t");
                    if(items[0].length()>6){
                        daDbfile[x] = daDbfile[x].replaceAll(" ","");
                        daDbfile[x] = daDbfile[x].replaceAll("RECORD", "RECORD"+"\t");
                        items = daDbfile[x].split("\t");
                    }
                    records[y][0]=items[3];
                    records[y][1]=items[2];
                    //System.out.println(records[y][0]);
                    y++;
                }
            }
            System.out.println("records "+records.length);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Oracle pv parser">
    private boolean parseOraclePvs(){
        boolean res=false;
        if(type==2){
            String [] items=null;
            oraclepvs = new String[pvs.length][7];
            //System.out.println("pvs "+Integer.toString(pvs.length));
            for(int x = 0;x<pvs.length;x++){
                //System.out.println(x);
                if(properpv(pvs[x])){
                    items = pvs[x].split(":");
                    oraclepvs[x][0]=pvs[x];
                    oraclepvs[x][1]=items[0]+":"+items[1];
                    oraclepvs[x][2]=records[x][0];
                    oraclepvs[x][3]=items[2];
                    for(int y=0;y<Integer.parseInt(dbfields[x][0]);y++){
                        if(dbfields[x][y].startsWith("SCAN")){
                            String [] items1 = dbfields[x][y].split(",");
                            String [] items2 = items1[1].split(" ");
                            if(items2.length>1){
                                if(isNumeric(items2[0])){
                                    oraclepvs[x][4] = items2[0];
                                    oraclepvs[x][5] = items2[1];
                                }else{
                                    oraclepvs[x][4]="1";
                                    oraclepvs[x][5]=items2[0]+" "+items2[1];
                                }
                            }else{
                                oraclepvs[x][4] = "1";
                                oraclepvs[x][5] = items2[0];
                            }
                            items1=null;
                            items2=null;
                        }
                        if(dbfields[x][y].startsWith("DESC")){
                            String [] items1= dbfields[x][y].split(",");
                            //System.out.println(dbfields[x][y]);
                            if(items1.length>1){
                                oraclepvs[x][6] = items1[1];
                            }else{
                                oraclepvs[x][6]="";
                            }
                            items1=null;
                        }
                    }
                }else{
                    oraclepvs[x][0]=pvs[x];
                    oraclepvs[x][1]="null";
                    oraclepvs[x][2]="null";
                    oraclepvs[x][3]="null";
                    oraclepvs[x][4]="null";
                    oraclepvs[x][5]="null";
                }
                if(oraclepvs[x][4]==null){
                    oraclepvs[x][4]="1";
                    oraclepvs[x][5]="Passive";
                }
            }
        }else if(type==1){
            String [] items=null;
            oraclepvs = new String[pvs.length][7];
            for(int x=0;x<pvs.length;x++){
                items = pvs[x].split(":");
                oraclepvs[x][0]=pvs[x];
                oraclepvs[x][1]=items[0]+":"+items[1];
                oraclepvs[x][2]=records[x][0];
                oraclepvs[x][3]=items[2];
                oraclepvs[x][4]="1";
                oraclepvs[x][5]="passive";
                oraclepvs[x][6]=records[x][1];
            }
        }
        return res;
    }
   
    // </editor-fold>

    private boolean isNumeric(String str){
    try{
        //System.out.println(str);
        double d = Double.parseDouble(str);
        //System.out.println(d);
    }catch(NumberFormatException NFE){
        //System.out.println(NFE);
        return false;
    }
    return true;
}
    // <editor-fold defaultstate="collapsed" desc="member access functions">
    public String [][] getOraclePvs(){
        return oraclepvs;
    }
    public String [] getxlsfile(){
        return xlsfile;
    }
    public String [] getdbfile(){
        return dbfile;
    }
    public String getxlsfilename(){
        return xlsfilename;
    }
    public String getdbfilename(){
        return dbfilename;
    }
    public boolean IsSet(){
        return set;
    }
    public void clearall(){
        pvs = null;
        xlsfile=null;
        dbfile = null;
        xlsfilename = "";
        dbfilename = "";
        daDbfile=null;
        daDbfilename="";
        oraclepvs=null;
        set = false;
        type=0;
    }
        public String gethost(){
        return host;
    }
    public String [] getpvs(){
        return pvs;
    }
    private boolean properpv(String pv){
        boolean res = false;
            if(pv != null){
                if (pv.length()<5){
                    return res;
                }else{
                    res=true;
                }
            }
        return res;
    }

    // </editor-fold>
}
