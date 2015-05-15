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
package com.dcube.dlan.launcher;

import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dcube.dlan.netcmd.NetcmdArgs;
import com.dcube.dlan.netcmd.NetcmdHandler;
import com.dcube.dlan.netcmd.NetcmdListener;
import com.dcube.dlan.netcmd.NetcmdSender;
import com.dcube.dlan.netcmd.NetcmdArgs.Netcmd;
import com.dcube.disruptor.EventDispatcher;
import com.dcube.launcher.Lifecycle;
import com.dcube.launcher.LifecycleHooker;

/**
 * 核心启动类 
 **/
public class CoreFacade extends Lifecycle{
	
	/** the network command information listener */
	private NetcmdListener netcmdlsnr;
	
	Logger logger = LoggerFactory.getLogger(CoreFacade.class);
	
	private static CoreFacade instance;
	
	public static CoreFacade getCoreInstance(){
		
		return instance;
	}
	
	/**
	 * 隐藏的构造函数
	 */
	private CoreFacade(String args[])
	{

		// Create the net command listener.
		netcmdlsnr = new NetcmdListener();
		// Check the listener of same thread existence.
		boolean another_exist = netcmdlsnr.getState() != NetcmdListener.STATE_LISTENING;			

		if( another_exist ) {
			// If the new listener can be created, then send arguments to listener through net.
			// Create the net command sender.
			logger.debug("The NetCommand listener already exists.");
			NetcmdSender cs = new NetcmdSender(args);
						
			cs.sendNetcmdArgs() ;

	   	}else{
	   		
			logger.debug("no remote server cosole.");			
	   		// read the command arguments
	   		NetcmdArgs ncmdargs = new NetcmdArgs(args);
	   		if(Netcmd.startup == ncmdargs.getCommand()){
	   			// initial the event dispatcher
		   		EventDispatcher eventengine = EventDispatcher.getInstance();
		   		
		   		LifecycleHooker enginehooker = eventengine.getLifecycleHooker();
		   		// register engine to life cycle hooker
		   		super.regLifecycleHooker(enginehooker);
		   		// initialize the event engine
		   		initial();
		   		// Trigger start life cycle event
		   		startup();
		   		logger.debug("Server startup");
	   		}else if(Netcmd.help == ncmdargs.getCommand()){
	   			NetcmdHandler handler = new NetcmdHandler();
	   			handler.procHelpCmd(ncmdargs);
	   			netcmdlsnr.stopListener();
	   		}
	   		else{
	   			logger.debug("Please start server first");
	   			netcmdlsnr.stopListener();
	   		}
			
	   	}

	}

	@Override
	public void initial(){
		fireEvent(LifeState.INITIAL);
	}

	@Override
	public void startup(){
		state = LifeState.STARTUP;

		fireEvent(LifeState.STARTUP);
	
		state = LifeState.RUNNING;
	}

	@Override
	public void shutdown(){
		fireEvent(LifeState.SHUTDOWN);
		this.state = LifeState.SHUTDOWN;
	}

	public NetcmdListener getNetcmdListener(){
		
		return this.netcmdlsnr;
	}
	
	/**
	 * 启动入口
	 */
	public static void main(String args[])
	{
		Properties prop = new Properties();

		prop.setProperty("log4j.rootCategory", "ERROR, CONSOLE");
		prop.setProperty("log4j.appender.CONSOLE", "org.apache.log4j.ConsoleAppender");
		prop.setProperty("log4j.appender.CONSOLE.layout", "org.apache.log4j.PatternLayout");
		prop.setProperty("log4j.appender.CONSOLE.layout.ConversionPattern", "%d{HH:mm:ss,SSS} [%t] %-5p %C{1} : %m%n");
		prop.setProperty("log4j.logger.com.hele","DEBUG");
		PropertyConfigurator.configure(prop);
		instance = new CoreFacade(args);
	}
}
