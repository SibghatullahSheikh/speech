
import java.util.Properties;
import java.util.Vector;

import com.jaivox.interpreter.Command;
import com.jaivox.interpreter.HistNode;

/**
 * roadCommand is a user-defined function handler. The jaivox.interpreter
 * package contains a default Command class, this can be replaced by a
 * class similar to the one here.
 */

public class roadCommand extends Command {

	cmdProc proc;

	String base = "./";
	String spec = "road_de.spec";
	String quest = "road.quest";
	String datafile = "road.txt";
	String answer = "answer_de.txt";

/**
 * Create a roadCommand class, specifying properties used for naswering questions
 * as well as the cmdProc class which makes answers.
 */
	public roadCommand () {
		Properties pp = new Properties ();
		pp.setProperty ("base_dir", base);
		pp.setProperty ("specs_file", spec);
		pp.setProperty ("data_file", datafile);
		pp.setProperty ("questions_file", quest);
		pp.setProperty ("answer_file", answer);
		proc = new cmdProc (this, pp);
	}

/**
 * Overrides Command.handle. This is generated by the Jvgen generator from
 * user-defined functions in the grammar.
 * @param f
 * @param question
 * @param spec
 * @param instate
 * @param history
 * @return
 */
	public String [] handle (String f,
		String question, String spec, String instate,
		Vector <HistNode> history) {
		if (f.equals ("ask"))
			return ask (question, spec, instate, history);
		else if (f.equals ("find"))
			return find (question, spec, instate, history);
		else return null;
	}

/**
 * This function, without the connection to cmdProc, is created by Jvgen.
 * This function is expected to provide a true or false answer.
 * @param question
 * @param spec
 * @param instate
 * @param history
 * @return
 */
	String [] ask (String question, String spec, String instate, Vector <HistNode> history) {
		// String result = "implement ask command!";
		String [] result = proc.makeAnswer (question, history);
		return result;
	}

/**
 * This function is also generated by the generator, tools.Jvgen. This one
 * finds specific answers, while ask returns true or false.
 * @param question
 * @param spec
 * @param instate
 * @param history
 * @return
 */

	String [] find (String question, String spec, String instate, Vector <HistNode> history) {
		// String result = "implement find command!";
		String [] result = proc.makeAnswer (question, history);
		return result;
	}

}

