package eu.codlab.airplane;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import eu.codlab.airplane.ShellCommand.CommandResult;

import android.content.Context;

public class CopyProgram {
	private Context _context;
	public final static int APK_DOESNOTEXIST=1<<1;
	public final static int APK_SYS_DOESNOTEXIST=1<<2;
	public final static int APK_SYS_COULDNOTCREATE=1<<3;
	public final static int APK_SYS_EXIST=1<<4;
	public final static int APK_SYS_COULDNOTDELETE = 1<<5;
	public final static int APK_COULDNOTCREATE=1<<6;
	public final static int APK_SYS_SUCCESS=0;
	public final static int CANTSU=1<<7;
	private ShellCommand cmd;
	final private static String APKNAME="AirplaneOnWake"; 
	final private static String APKNAMEET="AirplaneOnWak*";
	final private static String APKDEFAULT="eu.codlab.airplane-1.apk";//TODO get package name;
	private CopyProgram(){

	}

	public CopyProgram(Context context){
		this();
		cmd = new ShellCommand();
	}

	private String getSu(){
		return "su";
	}

	private String getRemountWrite(String device){
		return "mount -o remount,rw -t yaffs2 "+device+" /system";
	}

	private String getRemountRead(String device){
		return "mount -o remount,ro -t yaffs2 "+device+" /system";
	}

	private String getMount(){
		return "mount";
	}

	public boolean isMask(int val, int mask){
		return (val & mask) == mask;
	}
	
	public String executeSuMount(){

		CommandResult r = cmd.su.runWaitFor("mount");
		String res = r.stdout;
		if(r == null || res == null)
			return null;
		//String res =  executeCommand(getSu()+" -c "+getMount()+" root",null);
		String [] split = res.split("\n");
		String device = null;
		int esp=0;
		for(int i =0;i<split.length && device == null;i++){
			if(split[i].indexOf(" /system") >= 0){
				esp = split[i].indexOf(" ");
				if( esp > 0)
					device = split[i].substring(0,esp);
			}
		}
		return device;
	}
	
	public String getApplicationPathName(){
		CommandResult sapk = cmd.su.runWaitFor("ls /data/app/eu.codlab.airplane*");
		return sapk.exit_value == 0 && sapk.stdout != null && sapk.stdout.indexOf("eu.codlab") >=0 ?
				 sapk.stdout.replace(" ", "") : null;
	}
	
	public String getApplicationPathNameOrDefault(){
		String ret =  getApplicationPathName();
		return ret != null ? ret : CopyProgram.APKDEFAULT;
	}

	public int copyProgram(){
		if(cmd.canSU()){
			if(existProgramSys()){
				return this.APK_SYS_EXIST;
			}
			String apk = getApplicationPathName();
			if(apk != null){
				String device = executeSuMount();
				if(device != null){
					if(cmd.su.runWaitFor(getRemountWrite(device)).success() != true)
						return this.APK_SYS_COULDNOTCREATE;
					//CommandResult  r = cmd.su.runWaitFor("ls /data/app/eu.codlab.airplane*");
					cmd.su.runWaitFor("cat "+apk+" > /system/app/"+APKNAME+".apk");
					//cmd.su.runWaitFor("rm "+apk);
					cmd.su.runWaitFor(getRemountRead(device));
					return this.APK_SYS_SUCCESS;
				}else{
					return this.APK_SYS_COULDNOTCREATE;
				}

			}
			return 	APK_DOESNOTEXIST;
		}
		return CANTSU;
	}

	public boolean existProgramSys(){
		CommandResult sapk = cmd.su.runWaitFor("ls /system/app/"+APKNAMEET+".apk");
		if(sapk.stdout == null || (sapk.stdout.indexOf(APKNAME+".apk") < 0)){
			return false;
		}
		return true;
	}
	public int copyProgramFromSys(){
		if(cmd.canSU()){
			if(existProgramSys()){
				String device = executeSuMount();
				if(device != null){
					if(cmd.su.runWaitFor(getRemountWrite(device)).success() != true)
						return this.APK_SYS_COULDNOTDELETE;
					cmd.su.runWaitFor("cat /system/app/"+APKNAME+".apk > "+this.getApplicationPathNameOrDefault());
					cmd.su.runWaitFor("rm /system/app/"+APKNAME+".apk");
					cmd.su.runWaitFor(getRemountRead(device));
					return this.APK_SYS_SUCCESS;
				}else{
					return this.APK_SYS_COULDNOTDELETE;
				}

			}
			return 	APK_SYS_DOESNOTEXIST;
		}
		return CANTSU;
	}

	private String executeCommand(String command, String arg){	
		String full = null;
		try {
			Process process = Runtime.getRuntime().exec(command);
			process.waitFor();
			if(arg != null){
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
				out.write(arg);
				out.close();
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = "";
			while ((line = in.readLine()) != null) {  
				full = full + "\n" + line;
			}
			in.close();
			return full;
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return full;
	}
}