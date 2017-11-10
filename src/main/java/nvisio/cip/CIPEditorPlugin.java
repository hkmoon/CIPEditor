package nvisio.cip;

import net.imagej.ImageJ;
import nvisio.cip.view.JythonEditor;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.swing.JFrame;

/**
 * Author: HongKee Moon
 */
@Plugin(type = Command.class, menuPath = "SCF>CIPEditor")
public class CIPEditorPlugin implements Command
{
	@Parameter
	private LogService log;

	@Parameter
	private ImageJ ij;

	@Override
	public void run()
	{
		new CIPEditor().run( null, ij );

//		JFrame jFrame = new JFrame( "CIP Editor" );
//		jFrame.setContentPane( new JythonEditor( ij ) );
//		jFrame.setVisible( true );
	}

	public static void main(final String... args) throws Exception {
		// Launch ImageJ as usual.
		final ImageJ ij = net.imagej.Main.launch(args);

		// Launch the "CIPEditor" command
		ij.command().run( CIPEditorPlugin.class, true );
	}
}
