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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.SocketException;
import java.io.PrintWriter;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dcube.dlan.launcher.CoreConfigs;

/**
 * 控制台命令发送器
 * @author Gary Diao
 * @version 1.0 DEC 11, 2008
 * @since 	
 */ 
public class NetcmdSender {
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	private final String[] args;
	/** 命令响应消息 */
	private String cmdresponse;
	
	/**
	 * 构造函数
	 * @param _args the command arguments to be sent.
	 */
    public NetcmdSender(String[] _args) {
    	
    	this.args = _args;
    }    
    
    /**
     * 发送命令参数信息
     * @return true if successful, false if connection attempt failed
     */
    public boolean sendNetcmdArgs() {
    	Socket sck = null;
    	PrintWriter pw = null;
		BufferedReader br = null;
    	try {
      	
    		sck = new Socket("127.0.0.1", CoreConfigs.getNetcmdPort());

    		pw = new PrintWriter(new OutputStreamWriter(sck.getOutputStream()));         
    		StringBuffer buffer = new StringBuffer(NetcmdHandler.ACCESS_STRING + ";args;");
         
    		for(int i = 0 ; i < args.length ; i++) {
    			String arg = args[i].replaceAll("&","&&").replaceAll(";","&;");
    			buffer.append(arg);
    			buffer.append(';');
    		}
			
			logger.debug("Send Arguments:"+buffer.toString());
    		pw.println(buffer.toString());
    		pw.flush();
    		
			br = new BufferedReader(new InputStreamReader(sck.getInputStream()));
			boolean isEndofComm = false;
			while(true){
				
				cmdresponse = br.readLine();				
				if(cmdresponse == null)// 未读到数据情况，终止while loop
					break;
				// 检测通信终止字符
				if(isEndofComm = cmdresponse.endsWith(NetcmdHandler.EOCOMM_STRING))
					cmdresponse = cmdresponse.substring(0, cmdresponse.lastIndexOf(NetcmdHandler.EOCOMM_STRING));
				// 换行符转换
				cmdresponse = cmdresponse.replaceAll("nnn","\n");
				System.out.println(cmdresponse);
				if(isEndofComm) // 终止while loop
					break;
			}
			logger.debug("complete command sending");
    		return true;
			
    	}catch(Exception e) {
			if(e instanceof SocketException)
				cmdresponse = "can't create socket to remote server console. ";
			else
				cmdresponse = "can't send args to remote server console. ";
			
			logger.debug(cmdresponse);
    		return false;  //there was a problem connecting to the socket
    	}
    	finally {
    		try {
    			if (pw != null)  pw.close();
				if (br != null)  br.close();
    		}
    		catch (Exception e) {}
    		
    		try {
    			if (sck != null) 	
    				sck.close();
    		}
    		catch (Exception e) {}
    	}
    }

  }