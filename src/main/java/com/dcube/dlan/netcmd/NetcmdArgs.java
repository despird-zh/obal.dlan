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
 * 调用参数类，封装启动及其他操作参数 
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
	/** 命令 */
	private Netcmd command ;
	/** 原始命令参数 */
	private String rawcommand;
	/** 命令关联参数 */
	private String[] args = new String[0];
	
	/**
	 * 构造函数 
	 **/
	public NetcmdArgs(){}
	
	/**
	 * 构造函数，接收命令和附加参数 
	 **/
	public NetcmdArgs(Netcmd command, String[] args){
		this.command = command;
		this.args = args;
	}
	
	/**
	 * 构造函数，通过数组进行参数设置 
	 **/
	public NetcmdArgs(String[] args){
		if(args != null && args.length > 0){
			this.command = convert(args[0]);
			this.args = Arrays.copyOfRange(args, 1, args.length);
		}
	}
	
	/**
	 * 获取命令 
	 **/
	public Netcmd getCommand(){
		return this.command;
	}
	
	/**
	 * 获取原始的命令字符串，由于非合法的命令都是 Netcmd.unknown，因此
	 * 原始命令保存在rawcommand中
	 **/
	public String getRawcommand(){
		
		return this.rawcommand;
	}
	/**
	 * 获取命令参数 
	 **/
	public String[] getArgs(){
		
		return this.args;
	}
	
	/**
	 * 转化成可传输字符串 
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
	 * 通过字符串进行参数解析和设置 
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
