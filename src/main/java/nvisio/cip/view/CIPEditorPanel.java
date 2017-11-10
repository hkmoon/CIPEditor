package nvisio.cip.view;

import ij.IJ;
import net.imagej.ImageJ;
import nvisio.cip.language.java.CompilerUtils;
import javafx.embed.swing.SwingNode;
import javafx.scene.layout.BorderPane;

import java.io.File;

/**
 * Author: HongKee Moon
 */
public class CIPEditorPanel extends BorderPane
{
	String[] jarInfos;
	boolean bStandAlone = true;
	Editor eEditor;

	public CIPEditorPanel( ImageJ ij )
	{
		// Add necessary classes for CompilerUtils and JavaLanguageSupport for editor
		try
		{
			//IJ.getClassLoader().loadClass( "nvisio.cip.CIP" );
//			CompilerUtils.addClassPath( "res/lib/CIP-0.1.0.jar" );
			String str = IJ.getClassLoader().loadClass( "nvisio.cip.CIPEditor" ).getResource( "" ).toString();

			//			System.err.println(str);

			if ( str.startsWith( "jar:file" ) )
				bStandAlone = false;

			if ( bStandAlone )
			{
				str = "res/lib/ij-1.51r.jar";

				if ( new File( str ).exists() )
					CompilerUtils.addClassPath( str );

				CompilerUtils.addClassPath( "res/lib/CIP-0.1.0.jar" );
			}
			else
			{
				jarInfos = new String[ 3 ];
				jarInfos[ 0 ] = str.replace( "jar:file:", "" ).replace( "!/nvisio/cip/", "" );
				//				System.out.println( jarInfos[ 0 ] );

				// The below method has a bug
				//				str = IJ.getClassLoader().loadClass( "ij.IJ" ).getResource( "" ).toString();
				//				jarInfos[ 1 ] = str.replace( "jar:file:", "" ).replace( "!/ij/", "" );
				//				System.out.println( jarInfos[ 1 ] );

				str = IJ.class.getResource("IJ.class").getPath();
				jarInfos[ 1 ] = str.replace( "file:", "" ).replace( "!/ij/IJ.class", "" );
				//				System.out.println( jarInfos[ 1 ] );

				str = IJ.getClassLoader().loadClass( "nvisio.cip.CIPService" ).getResource( "" ).toString();
				jarInfos[ 2 ] = str.replace( "jar:file:", "" ).replace( "!/nvisio/cip/", "" );
				//				System.out.println( jarInfos[ 2 ] );
			}

		}
		catch ( ClassNotFoundException e )
		{
			e.printStackTrace();
		}

		// eEditor = new JavaEditor( null );
		eEditor = new JythonEditor( ij );

		final SwingNode node = new SwingNode();

		node.setContent(eEditor);
		setCenter(node);
	}
}
