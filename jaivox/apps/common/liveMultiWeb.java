/*
   Jaivox version 0.5 August 2013
   Copyright 2010-2013 by Bits and Pixels, Inc.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

/**
 * PATliveMultiWeb crates a WebAsrServer.
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.jaivox.recognizer.web.WebAsrServer;
import com.jaivox.recognizer.web.SpeechInput;
import com.jaivox.recognizer.web.Mike;
import com.jaivox.util.Log;

public class PATliveMultiWeb extends Thread {

	static int port = PATport_recognizer;
	static int waitTime = 1000; // one second

	static String work1 =
	"send PATnamerecognizer_0 {action: interpret, from: PATnamerecognizer, to: PATnameinterpreter, message: ";
	static String connect1 = "connect localhost PATport_interpreter";
	static String who1 = "send PATnamerecognizer_0 {action: JviaWho, from: PATnamerecognizer, to: PATnameinterpreter, message: Jviawho}";
	static int wait = 20; // maximum length of input in seconds
	static String type = ".wav";

	static void processSpeech (WebAsrServer server) {
		SpeechInput R = new SpeechInput ();
		Mike mike = new Mike ("PATliveMultiWeb", type);
		while (true) {
			String flac = mike.nextsample (type, wait);
			mike.showtime ("result is "+flac);
			if (flac != null) {
				String result = R.recognize (flac, "PATlang");
				mike.showtime ("recognized "+result);
				String name = server.getServerId ();
				String message = work1 + "\"" + result + "\"}";
				server.execute (message);
			}
			// need to give up control before trying the
			// next one
			try {
				Thread.sleep (5000);
			}
			catch (Exception e) {
				e.printStackTrace ();
			}
		}
	}

	public static void main (String args []) {
		WebAsrServer Recognizer;
		Log log = new Log ();
		log.setLevelByName ("PATlog_level");
		try {
			Recognizer = new WebAsrServer ("PATnamerecognizer", PATport_recognizer);
			BufferedReader in = new BufferedReader (
				new InputStreamReader (System.in));

			Log.info ("Waiting to connect to PATnameinterpreter");
			boolean connected = false;
			int waiting = 0;
			while (!connected && waiting < 1000) {
				sleep (waitTime);
				String result = Recognizer.executeReply (connect1);
				if (result.startsWith ("OK:")) {
					connected = true;
					break;
				}
				else {
					waiting++;
					System.out.println ("Waiting ... "+waiting);
					continue;
				}
			}
			if (!connected) {
				Log.severe ("Waiting for too long, terminating ...");
				return;
			}
			else {
				Log.info ("Connected to PATnameinterpreter");
				Log.info ("Waiting to establish who credentials");
			}
			sleep (waitTime * 4);
			boolean established = false;
			waiting = 0;
			while (!established && waiting < 100) {
				sleep (waitTime);
				String result = Recognizer.executeReply (who1);
				if (result.startsWith ("OK:")) {
					established = true;
					break;
				}
				else {
					waiting++;
					Log.info ("Establishing credentials ... "+waiting);
					continue;
				}
			}
			if (!established) {
				Log.severe ("Establishing did not work, terminating ...");
				return;
			}
			else {
				Log.info ("Established credentials with PATnameinterpreter");
				Log.info ("Processing recognition results");
			}

			sleep (waitTime * 4);

			// loop in SpeechInput
			processSpeech (Recognizer);
			System.out.println ("Terminating ...");
			System.exit (1);
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

}
