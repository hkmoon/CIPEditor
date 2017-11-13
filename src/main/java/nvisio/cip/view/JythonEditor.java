package nvisio.cip.view;

import net.imagej.ImageJ;
import nvisio.cip.language.java.CompilerUtils;
import nvisio.cip.language.jython.JythonLanguageSupport;
import nvisio.cip.language.jython.buildpath.JarLibraryInfo;
import nvisio.cip.language.jython.JarManager;

import org.fife.rsta.ac.LanguageSupport;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.rsta.ac.java.buildpath.ZipSourceLocation;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.scijava.Context;
import org.scijava.app.AppService;
import org.scijava.command.CommandService;
import org.scijava.io.IOService;
import org.scijava.log.LogService;
import org.scijava.module.ModuleService;
import org.scijava.platform.PlatformService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginService;
import org.scijava.script.ScriptHeaderService;
import org.scijava.script.ScriptInfo;
import org.scijava.script.ScriptLanguage;
import org.scijava.script.ScriptModule;
import org.scijava.script.ScriptService;
import org.scijava.ui.UIService;

import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Author: HongKee Moon
 */
public class JythonEditor extends Editor
{
	protected String[] jarInfos;

	protected ImageJ ij;

	@Parameter
	private Context context;
	@Parameter
	private LogService log;
	@Parameter
	private ModuleService moduleService;
	@Parameter
	private PlatformService platformService;
	@Parameter
	private IOService ioService;
	@Parameter
	private CommandService commandService;
	@Parameter
	private ScriptService scriptService;
	@Parameter
	private PluginService pluginService;
	@Parameter
	private ScriptHeaderService scriptHeaderService;
	@Parameter
	private UIService uiService;
	@Parameter
	private AppService appService;

	public JythonEditor( ImageJ ij )
	{
		this.ij = ij;
		ij.context().inject( this );

		if(jarInfos != null)
			addClassPaths();

		initializeComponents();
	}

	@Override protected String getFilenameCheck( String filename )
	{
		if ( !filename.endsWith( ".py" )  && textArea.getSyntaxEditingStyle().equals( "text/python" ))
		{
			filename += ".py";
		}
		return filename;
	}

	@Override protected String getSyntaxStyle()
	{
		return SyntaxConstants.SYNTAX_STYLE_PYTHON;
	}

	@Override protected void setLanguage( RSyntaxTextArea ta )
	{
		LanguageSupportFactory lsf = LanguageSupportFactory.get();
		lsf.addLanguageSupport( SyntaxConstants.SYNTAX_STYLE_PYTHON, "nvisio.cip.language.jython.JythonLanguageSupport" );
		LanguageSupport support = (JythonLanguageSupport) lsf.getSupportFor( SyntaxConstants.SYNTAX_STYLE_PYTHON );

		JythonLanguageSupport jls = (JythonLanguageSupport)support;
		jls.setAutoActivationDelay(300);
		jls.setAutoActivationEnabled(true);

		try {
			jls.getJarManager().addCurrentJreClassFileSource();
			if(jarInfos != null)
			{
				// include jar files in order to provide code suggestions
				addClassPathForJLS(jls.getJarManager());
			}
			else
			{
				String jar = "res/lib/ij-1.51r.jar";

				if(new File(jar).exists())
				{
					JarLibraryInfo ijJarInfo = new JarLibraryInfo( jar );
					jls.getJarManager().addClassFileSource(ijJarInfo);
				}


				jls.getJarManager().addClassFileSource( new JarLibraryInfo( new File("res/lib/CIP-0.1.0.jar"), new ZipSourceLocation("res/lib/CIP-0.1.0-sources.jar") ) );
			}


		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		lsf.register( ta );
	}

	@Override protected JButton[] getExtraButtons()
	{
		JButton[] buttons = new JButton[1];

		JButton btn = new JButton("Run");
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				compile();
			}
		});

		buttons[0] = btn;

		return buttons;
	}

	@Override protected JFileChooser getFileChooser()
	{
		JFileChooser c = new JFileChooser();

		c.setFileFilter(new ExtensionFileFilter("Jython","py"));

		return c;
	}

	@Override protected String getEditorType()
	{
		return "Jython Macro Editor";
	}

	@Override protected void closingWindow()
	{

	}

	private void addClassPaths()
	{
		for(int i = 0; i < jarInfos.length; i++)
		{
			if(new File(jarInfos[i]).exists())
				CompilerUtils.addClassPath( jarInfos[ i ] );
		}
	}

	private void addClassPathForJLS(JarManager jarManager ) throws IOException
	{
		for(int i = 0; i < jarInfos.length; i++)
		{
			if(new File(jarInfos[i]).exists())
				jarManager.addClassFileSource( new JarLibraryInfo( new File(jarInfos[i]) ) );
		}
	}

	private void compile()
	{
		StringWriter writer = new StringWriter();

		try {
			textArea.write(writer);
		} catch (IOException e) {
			e.printStackTrace();
		}

		String code = writer.toString();

		if(code.trim().isEmpty())
		{
			System.out.println("No code is provided.");
			return;
		}

		final ScriptLanguage language = scriptService.getLanguageByExtension( "py" );
		Reader reader = new StringReader(code);
		Writer errors = new StringWriter();
		Writer output = new StringWriter();

		new Executer() {
			@Override
			public void execute() {
				try
				{
					final ScriptInfo info = new ScriptInfo(context, fileName, reader);

					final ScriptModule module = info.createModule();
					context.inject(module);

					// use the currently selected language to execute the script
					module.setLanguage(language);

					// map stdout and stderr to the UI
					module.setOutputWriter(output);
					module.setErrorWriter(errors);

					moduleService.run(module, true).get();
				}
				catch (final Throwable e) {
					System.out.println(e);
				}

				System.out.println(output);
				System.err.println(errors);
			}
		};
	}

	private final ArrayList<Executer> executingTasks = new ArrayList<>();

	public abstract class Executer extends ThreadGroup {
		Executer() {
			super("Script Editor Run :: " + new Date().toString());
			// Store itself for later
			executingTasks.add(this);
			// Fork a task, as a part of this ThreadGroup
			new Thread(this, getName()) {
				{
					setPriority(Thread.NORM_PRIORITY);
					start();
				}

				@Override
				public void run() {
					try {
						execute();
						// Wait until any children threads die:
						int activeCount = getThreadGroup().activeCount();
						while (activeCount > 1) {
							if (isInterrupted()) break;
							try {
								Thread.sleep(500);
								final List<Thread> ts = getAllThreads();
								activeCount = ts.size();
								if (activeCount <= 1) break;
								log.debug("Waiting for " + ts.size() + " threads to die");
								int count_zSelector = 0;
								for (final Thread t : ts) {
									if (t.getName().equals("zSelector")) {
										count_zSelector++;
									}
									log.debug("THREAD: " + t.getName());
								}
								if (activeCount == count_zSelector + 1) {
									// Do not wait on the stack slice selector thread.
									break;
								}
							}
							catch (final InterruptedException ie) {
								/* ignore */
							}
						}
					}
					catch (final Throwable t) {
						System.out.println(t);
					}
					finally {
						executingTasks.remove(Executer.this);
					}
				}
			};
		}

		/** The method to extend, that will do the actual work. */
		abstract void execute();

		/** Fetch a list of all threads from all thread subgroups, recursively. */
		List<Thread> getAllThreads() {
			final ArrayList<Thread> threads = new ArrayList<>();
			// From all subgroups:
			final ThreadGroup[] tgs = new ThreadGroup[activeGroupCount() * 2 + 100];
			this.enumerate(tgs, true);
			for (final ThreadGroup tg : tgs) {
				if (null == tg) continue;
				final Thread[] ts = new Thread[tg.activeCount() * 2 + 100];
				tg.enumerate(ts);
				for (final Thread t : ts) {
					if (null == t) continue;
					threads.add(t);
				}
			}
			// And from this group:
			final Thread[] ts = new Thread[activeCount() * 2 + 100];
			this.enumerate(ts);
			for (final Thread t : ts) {
				if (null == t) continue;
				threads.add(t);
			}
			return threads;
		}

		@Override
		public String toString() {
			return getName();
		}
	}
}
