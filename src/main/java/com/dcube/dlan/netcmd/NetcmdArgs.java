/**
 * Created on Dec 11, 2008 2:47:29 PM
 * Copyright (C) 2008 Gary Diao, All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.dcube.dlan.netcmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * NetcmdArgs wrap and parse the parameters
 **/
public class NetcmdArgs {
	
	public static enum Netcmd{
		
		help("-help"),
		startup("-startup"),
		shutdown("-shutdown"),
		start("-start"),
		stop("-stop"),
		list("-list"),
		unknown("-unknown");
		
		public final String abbr;
		private Netcmd(String abbr){
			this.abbr = abbr;
		}
	}
	
	public static final String PARAM_DELIMITER = ";";
	public static final String PARAM_ESCAPE = "&";
	/** command */
	private Netcmd command ;
	/** raw command string, for unkown command, it holds the original command */
	private String rawcommand;
	/** command parameter */
	private String[] args = new String[0];
	
	/**
	 * constructor
	 **/
	public NetcmdArgs(){}
	
	/**
	 * constructor with command and parameters 
	 * @param command the command string
	 * @param args the parameter coupled with command
	 **/
	public NetcmdArgs(Netcmd command, String[] args){
		this.command = command;
		this.args = args;
	}
	
	/**
	 * constructor with parameter
	 **/
	public NetcmdArgs(String[] args){
		if(args != null && args.length > 0){
			this.command = convert(args[0]);
			this.args = Arrays.copyOfRange(args, 1, args.length);
		}
	}
	
	/**
	 * Get command
	 **/
	public Netcmd getCommand(){
		return this.command;
	}
	
	/**
	 * Get raw command string
	 **/
	public String getRawcommand(){
		
		return this.rawcommand;
	}
	
	/**
	 * Get command parameter
	 **/
	public String[] getArgs(){
		
		return this.args;
	}
	
	/**
	 * convert to sendable string to transfer via network
	 **/
	public String toSendable(){
		
		StringBuffer buffer = new StringBuffer();
		buffer.append(NetcmdHandler.ACCESS_STRING + ";args;");
		buffer.append(command).append(PARAM_DELIMITER);
		String escapeStr = PARAM_ESCAPE + PARAM_ESCAPE;
		String delimitStr = PARAM_ESCAPE + PARAM_DELIMITER;
		for(int i = 0 ; i < args.length ; i++) {
			// String arg = args[i].replaceAll("&","&&").replaceAll(PARAM_DELIMITER,"&;");
			String arg = args[i].replaceAll(PARAM_ESCAPE, escapeStr).replaceAll(PARAM_DELIMITER, delimitStr);
			buffer.append(arg);
			buffer.append(PARAM_DELIMITER);
		}
		
		return buffer.toString();
	}
	
	/**
	 * Initial with string transfered via network.
	 **/
	public void readSendable(String senddata){
        StringTokenizer st = new StringTokenizer(senddata, ";");
        if(st.countTokens() > 1) {

            List<String> arglist = new ArrayList<String>();
            String checker = st.nextToken(); // read [access string]
            st.nextToken(); // ignore [args]
            String delimitStr = PARAM_ESCAPE + PARAM_DELIMITER;
            if(checker.equals(NetcmdHandler.ACCESS_STRING)) {
            	String arg = "";
                while (st.hasMoreElements()) {
                	arg += st.nextToken();
                	if(!arg.endsWith(delimitStr)){
                		
                		arg = arg.replaceAll("&;", ";").replaceAll("&&", "&");
                    	arglist.add(arg);
                    	arg = "";
                	}                	
                }
                if(arglist.size()>0){
                	this.rawcommand = arglist.get(0);
                	this.command = convert(arglist.get(0));
                	arglist = (arglist.size() > 1) ? arglist.subList(1, arglist.size()):new ArrayList<String>();
                	this.args = arglist.toArray(new String[0]);
                }
            }
        }
	}
	
	/**
	 * 命令转化函数，如：-start 对应 Netcmd.start
	 **/
	public static Netcmd convert(String arg){
		
		if(Netcmd.help.abbr.equals(arg))
			return Netcmd.help;
		if(Netcmd.start.abbr.equals(arg))
			return Netcmd.start;
		if(Netcmd.stop.abbr.equals(arg))
			return Netcmd.stop;
		if(Netcmd.startup.abbr.equals(arg))
			return Netcmd.startup;
		if(Netcmd.shutdown.abbr.equals(arg))
			return Netcmd.shutdown;
		if(Netcmd.list.abbr.equals(arg))
			return Netcmd.list;
		
		return Netcmd.unknown;
	}
}
