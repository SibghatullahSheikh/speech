
/**
 * Manages a session with a Sphinx server. Sphinx is an open source
 * speech recognition system from Carnegie-Mellon.
 */

package com.jaivox.recognizer.sphinx;

import com.jaivox.agent.*;

import java.io.*;
import java.net.*;
import java.util.Vector;

public class SpeechSession extends Session implements Runnable {

	public String From;
	public String To;
	
	
/**
 * Creates a session for a sphinx server.
@param s	id of the session, useful for debugging
@param serve	sphinx server that owns this session
@param sock	socket for communications
@param r	responder that handles messages from othe ragnets
 */
	public SpeechSession (String s, Server serve, Socket sock, Responder r) {
		super (s, serve, sock, r);
		From = null;
		To = null;
	}
	
	public SpeechSession () {
		super ();
		From = null;
		To = null;
	}

	void Debug (String message) {
		System.out.println ("[SpeechSession]"+sid+":"+message);
	}

/**
 * Keep the session alive, handling requests
 */
	
	public void run () {
		try {
			in = new BufferedReader (new InputStreamReader (
					socket.getInputStream ()));
			out = new PrintWriter (socket.getOutputStream ());

			while (true) {
				if (outbuffer != null) {
					out.println (outbuffer);
					out.flush ();
					sleep (waitTime);
					Debug ("sent:" + outbuffer);
					outbuffer = null;
				}
				String line = readLineFromSocket ();
				if (line == null) continue;
				Debug ("read "+line);
				
				if (From == null || To == null) getFromTo (line);
				
				MessageData response = responder.respond (line);
				String result = response.getValue ("action");
				
				if (result.equals (terminateMessage)) {
					break;
				}
				else if (result.equals (invalidMessage)) {
					Debug ("Invalid message: "+line);
					continue;
				}
				else if (result.equals (responseMessage)) {
					outbuffer = response.createMessage ();
					Debug ("replying: " + outbuffer);
				}
				else if (result.equals (finishedMessage)) {
					Debug ("Response received, no further action required");
					continue;
				}
				else {
					String to = response.getValue ("to");
					if (To == null) {
						Debug ("Connection has no destination: "+getSid ());
						continue;
					}
					if (!to.equals (To)) {
						Session toSession = appSessionTo (to);
						if (toSession != null) {
							toSession.outbuffer = response.createMessage ();
							continue;
						}
					}
					else {
						outbuffer = response.createMessage ();
						continue;
					}
				}
			}
			Debug ("Closing session "+sid);
			terminate ();
			socket.close ();
			server.removeSession (this);
			interrupt ();
			Debug (sid+" interrupted.");
		}
		catch (Exception e) {
			Debug (sid+":run "+e.toString ());
			e.printStackTrace ();
		}
	}
	
	void getFromTo (String message) {
		MessageData jd = new MessageData (message);
		From = jd.getValue ("to");
		int pos = From.indexOf ("_");
		if (pos != -1) From = From.substring (0, pos);
		To = jd.getValue ("from");
		pos = To.indexOf ("_");
		if (pos != -1) To = To.substring (0, pos);
		// Debug ("Patched From: "+From+" To: "+To);
	}
	
/**
 * For a given agent, see if there is already a SpeechSession with
 * the agent and return it. Return null if there is no such session.
@param to	id of agent to connect to
@return	a session with the agent if it exists, null otherwise
 */
	public SpeechSession appSessionTo (String to) {
		Vector <Session> clients = server.getClients ();
		int n = clients.size ();
		// Debug ("looking for client to "+to);
		for (int i=0; i<n; i++) {
			Session ias = clients.elementAt (i);
			String classname = ias.getClass().getName();
			// Debug ("Checking client "+ias.getSid ()+" classname "+classname);
			if (classname.equals ("SpeechSession")) {
				SpeechSession aps = (SpeechSession) ias;
				// Debug ("client "+i+" id is "+aps.sid + " to "+ aps.To);
				if (aps.To.equals (to)) return aps;
			}
		}
		return null;
	}


}
