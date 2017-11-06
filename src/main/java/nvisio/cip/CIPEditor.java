package nvisio.cip;

import nvisio.cip.view.CIPEditorPanel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.imagej.ImageJ;

/**
 * Author: HongKee Moon
 */
public class CIPEditor extends Application
{
	final static String mTitle = "CIP Editor";
	static Stage mStage;

	public void run( Stage primaryStage, ImageJ ij )
	{
		Platform.setImplicitExit( false );

		if( null != mStage )
		{
			Platform.runLater( new Runnable()
			{
				@Override public void run()
				{
					mStage.show();
				}
			} );
		}
		else
		{
			com.sun.javafx.application.PlatformImpl.startup( new Runnable()
			{
				@Override public void run()
				{
					if ( null == primaryStage && mStage == null )
						mStage = new Stage();
					else
						mStage = primaryStage;

					Scene scene = new Scene( new CIPEditorPanel(), 500, 600 );

					mStage.setOnCloseRequest( new EventHandler< WindowEvent >()
					{
						@Override public void handle( WindowEvent event )
						{
							mStage.hide();
							event.consume();
						}
					} );
					mStage.setTitle( mTitle );
					mStage.setScene( scene );
					mStage.show();
				}
			} );
		}
	}

	@Override
	public void start( Stage primaryStage ) throws Exception
	{
		run( primaryStage, new ImageJ() );
	}
}
