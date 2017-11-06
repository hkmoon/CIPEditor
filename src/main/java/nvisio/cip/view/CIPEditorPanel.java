package nvisio.cip.view;

import ij.IJ;
import nvisio.cip.compiler.CompilerUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingNode;
import javafx.scene.Scene;
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

	public CIPEditorPanel()
	{
		// Add necessary classes for CompilerUtils and JavaLanguageSupport for editor
		try
		{
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
				jarInfos[ 0 ] = str.replace( "jar:file:", "" ).replace( "!/invizio/cip/", "" );
				//				System.out.println( jarInfos[ 0 ] );

				// The below method has a bug
				//				str = IJ.getClassLoader().loadClass( "ij.IJ" ).getResource( "" ).toString();
				//				jarInfos[ 1 ] = str.replace( "jar:file:", "" ).replace( "!/ij/", "" );
				//				System.out.println( jarInfos[ 1 ] );

				str = IJ.class.getResource("IJ.class").getPath();
				jarInfos[ 1 ] = str.replace( "file:", "" ).replace( "!/ij/IJ.class", "" );
				//				System.out.println( jarInfos[ 1 ] );

				str = IJ.getClassLoader().loadClass( "nvisio.cip.CIPService" ).getResource( "" ).toString();
				jarInfos[ 2 ] = str.replace( "jar:file:", "" ).replace( "!/invizio/cip/", "" );
				//				System.out.println( jarInfos[ 2 ] );
			}

		}
		catch ( ClassNotFoundException e )
		{
			e.printStackTrace();
		}

		eEditor = new JavaMacroEditor( null );

		final SwingNode node = new SwingNode();

		node.setContent(eEditor);
		setCenter(node);

		sceneProperty().addListener(new ChangeListener<Scene>()
		{
			@Override
			public void changed(ObservableValue<? extends Scene> observable,
					Scene oldValue,
					Scene newValue)
			{
				if (newValue != null)
				{
//					newValue.widthProperty().addListener(new ChangeListener<Number>() {
//						@Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
//							System.out.println("Width: " + newSceneWidth);
////							eEditor.setSize( (int)getHeight(), (int)getWidth() );
////							node.resize( getHeight(), getWidth() );
//							eEditor.setSize( (int)getWidth(), (int)getHeight() );
//							node.resize( getWidth(), getHeight() );
//						}
//					});
//					newValue.heightProperty().addListener(new ChangeListener<Number>() {
//						@Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
//							System.out.println("Height: " + newSceneHeight);
////							eEditor.setSize( (int)getHeight(), (int)getWidth() );
////							node.resize( getHeight(), getWidth() );
//							eEditor.setSize( (int)getWidth(), (int)getHeight() );
//							node.resize( getWidth(), getHeight() );
//						}
//					});
				}
			}
		});
//
//		focusedProperty().addListener((observable,
//				oldValue,
//				newValue) -> {
//			if (newValue)
//				node.setContent(eEditor);
//		});
//
//		setOnMouseClicked(event -> {
//			this.requestFocus();
//		});
	}
}
