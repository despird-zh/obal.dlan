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

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dcube.dlan.launcher.CoreConfigs;

/**
 * Network command listener
 * @author Gary Diao
 * @version 1.0 DEC 11, 2008
 * @since
 */
public class NetcmdListener
{
    Logger logger = LoggerFactory.getLogger(this.getClass());
    /** The ServerSocket to listen the connect request. */
    private ServerSocket svr_sck;

    /** The Listener's Running State. */
    private int state;

    /** The Running Flag */
    private boolean blocked = false;

    /** Faulty State */
    public static final int STATE_FAULTY = 0;

    /** Listening State */
    public static final int STATE_LISTENING = 1;

    /** the listener thread */
    private Thread lsnrTrd = null;

    private Thread hdlrTrd = null;
    
    /**
     * Net Command listener constructor, here a Serversock will be created, and set the state<br>
     * to STATE_LISTENING, if the socket port is used by others the state will be STATE_FAULTY.<br>
     * After setting up the listening server socket, add a lifecycleAdapter object to KagileCore<br>
     * object, and customize the started and stoped event response.
     */
    public NetcmdListener(){

        try {

            svr_sck = new ServerSocket(CoreConfigs.getNetcmdPort(), 50, InetAddress.getByName("127.0.0.1")); //NOLAR: only bind to localhost
            state = STATE_LISTENING;

        }catch (Throwable t) {
            state = STATE_FAULTY;
        }

        if(state == STATE_LISTENING){
        	
           lsnrTrd = new Thread(new Runnable(){

				@Override
				public void run() {
					startListener();
				}
				
			}, "netcmd listener thread");
           
           lsnrTrd.start();
        }
    }

    /**
     * start the network command listener daemon thread
     */
    private void startListener() {

        logger.debug("NetcmdListener enter listening state.");
        NetcmdHandler cmdhandler = new NetcmdHandler();

        while (state == STATE_LISTENING) {
            
            try {

                final Socket rcvsocket = svr_sck.accept();
                // -----2015/5/13-----------------------
                // We just use only one thread to handle the request, if another net command comes
                // Just send feedback response ASAP.
                if(!blocked )
                {
                	blocked = true;// set blocked flag 
                	cmdhandler.setNetcmdSocket(rcvsocket);
                	startProcesssNetcmd(cmdhandler);
                }else{
                	NetcmdHandler drophandler = new NetcmdHandler(rcvsocket);
                	drophandler.sendResponseMsg("Server is busy, pls. send command later.",true);
                }
            }catch (Exception e) {
                if(!(e instanceof SocketException))
                    logger.debug("Non SocketException occurs",e);
            }
        }
    }

    /**
     * Start handler process 
     **/
    private void startProcesssNetcmd(final NetcmdHandler cmdhandler){
    	
        // handler process
        hdlrTrd = new Thread(new Runnable(){

			@Override
			public void run() {
				cmdhandler.processNetcmd();
				blocked = false; // reset blocked flag
			}                	
        }, "netcmd handler thread");
        hdlrTrd.setDaemon(true);
        hdlrTrd.start();
    }
    
    /**
     * stop the demoon thread and close listening server socket.
     */
    public void stopListener() {
    	state = STATE_FAULTY;
        try {
            logger.trace("Stop net command listener");
            lsnrTrd.interrupt();
            svr_sck.close();
        }catch (Throwable e) {/*ignore */}
    }

    /**
     * get the state of netcommand listener
     * @return int the state of listener STATE_FAULTY - error, STATE_LISTENING - normal listening
     */
    public int getState() {

        return state;

    }

}
