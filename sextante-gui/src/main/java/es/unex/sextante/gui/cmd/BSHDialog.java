package es.unex.sextante.gui.cmd;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.io.FileReader;

import javax.swing.JDialog;

import bsh.Interpreter;
import bsh.util.JConsole;
import es.unex.sextante.core.Sextante;

/**
 * A dialog with a BeanShell interface with extended commands to execute SEXTANTE algorithms
 * 
 * @author volaya
 * 
 */
public class BSHDialog
         extends
            JDialog {

   public BSHDialog(final Frame parent) {

      super(parent, Sextante.getText("Command_line"), true);
      setSize(new Dimension(400, 300));
      setPreferredSize(new Dimension(400, 300));
      getContentPane().setLayout(new BorderLayout());
      final JConsole console = new JConsole();
      getContentPane().add("Center", console);
      final Interpreter interpreter = new Interpreter(console);
      interpreter.getNameSpace().importCommands("es.unex.sextante.gui.cmd.bshcommands");
      final String[] files = ScriptsIO.getScriptFiles();
      if (files != null) {
         for (int i = 0; i < files.length; i++) {
            try {
               final FileReader reader = new FileReader(files[i]);
               interpreter.eval(reader);
            }
            catch (final Exception e) {}
         }
      }
      new Thread(interpreter).start();

   }
}
