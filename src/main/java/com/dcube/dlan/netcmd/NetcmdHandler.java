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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dcube.dlan.launcher.CoreConfigs;
import com.dcube.dlan.launcher.CoreFacade;

/**
 * 根据请求参数执行命令 
 **/
public class NetcmdHandler{

	Logger logger = LoggerFactory.getLogger(NetcmdHandler.class);
	public static String CMD_PMPT = "-| ";
    /** The Access String as the command prefix.  */
    public static final String ACCESS_STRING = "Dlan Net-Command Send";
    /** The End of conversation */
    public static final String EOCOMM_STRING = "_EOCOMM_";
    
    private Socket rcvsocket ;
    
	public NetcmdHandler() {
	}
    
	public NetcmdHandler(Socket netcmdsocket) {
		this.rcvsocket = netcmdsocket;
	}

	public void setNetcmdSocket(Socket netcmdsocket){
		this.rcvsocket = netcmdsocket;
	}
	
	public void processNetcmd(){

		BufferedReader br = null;

        String address = rcvsocket.getInetAddress().getHostAddress();
        String response = "";
        try {
        	
        	logger.debug("Get net command from:{}", address);
            if (address.equals("localhost") || address.equals("127.0.0.1")) {

                br = new BufferedReader(new InputStreamReader(rcvsocket.getInputStream()));
                String line = br.readLine();
                NetcmdArgs args = new NetcmdArgs();
                if (line != null) {
                	// parse data
                	args.readSendable(line);
                	
                	switch(args.getCommand()){
                	case help:
                		procHelpCmd( args);
                		break;
                	case startup:
                		procStartupCmd( args);
                		break;
                	case shutdown:
                		procShutdownCmd( args);
                		break;
                	case start:
                		procStartCmd( args);
                		break;
                	case stop:
                		procStopCmd( args);
                		break;
                	case list:
                		procListCmd( args);
                		break;
                	default:
                		response = "Illegal command[" + args.getRawcommand() + "] request from [" + address + "]";
                		sendResponseMsg( response,true);
                		logger.warn(response);
                	}
                }
            }else{
                response = "Illegal remote request from [" + address + "]";
                sendResponseMsg( response, true);
                logger.warn(response);
            }
            this.rcvsocket = null;
        }catch (Exception e) {
            
            logger.error("NetcmdHooker exception ",e);
        } finally {

            try {
                if (br != null)
                    br.close();
            } catch (Exception e) { /*ignore */

            }
        }
	}

	/**
	 * 发出响应消息
	 * @param message 消息内容
	 * @param endofcommn 网络通信结束标志,true 结束通信；false 继续通信
	 **/
	public void sendResponseMsg(String message, boolean endofcomm){
		
		// 无可用socket直接控制台输出
		if(this.rcvsocket == null){
			System.out.println(message);
			return;
		}
		try{
	        PrintWriter pw = new PrintWriter(new OutputStreamWriter(rcvsocket.getOutputStream()));
	        String reponse = message + (endofcomm ? EOCOMM_STRING :"");
	        pw.println(reponse.replaceAll("\n", "nnn"));
	        pw.flush();	       
	        // last message then close socket.
	        if(endofcomm){
	        	pw.close();
	        	rcvsocket.close();
	        }	        	
		}catch (IOException e) {
            //if(!(e instanceof SocketException))
            logger.debug("",e);
        }
	}
	
    /**
     * 处理启动命令，由于CoreFacade中已经执行处理，因此此处仅用于提示系统已经启动
     * @param args the arguments that sent by net or console
     */
    public void procStartupCmd(NetcmdArgs args)
    {
    	logger.debug("processing startup command.");
    	sendResponseMsg( "The server already startup, try other command !!! ",false);
    	sendResponseMsg( usage,true);
    }

    /**
     * 执行关闭命令
     * @param args the arguments that sent by net or console
     */
    public void procShutdownCmd(NetcmdArgs args)
    {
    	logger.debug("processing shutdown command.");
		sendResponseMsg( "stoping server...",false);
		CoreFacade.getCoreInstance().shutdown();
		sendResponseMsg( "server stoped...",true);
		NetcmdListener ncl = CoreFacade.getCoreInstance().getNetcmdListener();
		ncl.stopListener();

    }
    
    /**
     * 处理帮助信息命令
     * @param args the arguments that sent by net or console
     */
    public void procHelpCmd(NetcmdArgs args)
    {
    	logger.debug("processing help command.");
    	sendResponseMsg( usage,true);
    }
    
    /**
     * 处理启动消息接收任务命令
     * @param args the arguments that sent by net or console
     */
    public void procStartCmd(NetcmdArgs args)
    {
    	logger.debug("processing start command, parmeter:"+StringUtils.join(args.getArgs(), ",")+"");
    }
    
    /**
     * 处理启动消息接收任务命令
     * @param args the arguments that sent by net or console
     */
    public void procStopCmd(NetcmdArgs args)
    {
    	logger.debug("processing stop command.");
    }
    
    /**
     * 处理查询消息接收任务命令
     * @param args the arguments that sent by net or console
     */
    public void procListCmd(NetcmdArgs args)
    {
    	logger.debug("processing list command.");
    }
    
    //////////////////////////////////////////
    String usage = 
  	      CMD_PMPT + "Message Queue Receive Server(" + CoreConfigs.VERSION_INFO + ") usage:  \n"
  	      + CMD_PMPT + "-help     : display help information \n"
          + CMD_PMPT + "-startup  : startup the Queue Receive Server \n"
          + CMD_PMPT + "-shutdown : shutdown the Queue Receive Server \n"
          + CMD_PMPT + "-start    : start the endpoint message receiving \n"
          + CMD_PMPT + "          : eg. -start xxxendpoint \n"
          + CMD_PMPT + "-stop     : stop the endpoint message receiving \n"
    	  + CMD_PMPT + "          : eg. -stop xxxendpoint \n"
    	  + CMD_PMPT + "-list     : list all endpoints status \n";
}
