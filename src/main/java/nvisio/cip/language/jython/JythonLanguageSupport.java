package nvisio.cip.language.jython;

import org.fife.rsta.ac.AbstractLanguageSupport;

import org.fife.rsta.ac.java.rjc.ast.CompilationUnit;
import org.fife.rsta.ac.java.rjc.ast.ImportDeclaration;
import org.fife.rsta.ac.java.rjc.ast.Package;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.Timer;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Copied from the JavaLanguageSupport
 * Author: HongKee Moon
 */
public class JythonLanguageSupport extends AbstractLanguageSupport
{
	private JarManager jarManager;
	private Map<JythonParser, Info> parserToInfoMap;
	/**
	 * Client property installed on text areas that points to a listener.
	 */
	private static final String PROPERTY_LISTENER =
			"nvisio.cip.language.jython.JythonLanguageSupport.Listener";

	public JythonLanguageSupport()
	{
		parserToInfoMap = new HashMap<JythonParser, Info>();
		jarManager = new JarManager();
		setAutoActivationEnabled(true);
		setParameterAssistanceEnabled(true);
		setShowDescWindow(true);
	}

	public JarManager getJarManager() {
		return jarManager;
	}

	@Override public void install( RSyntaxTextArea textArea )
	{
		JythonCompletionProvider p = new JythonCompletionProvider(jarManager);
		AutoCompletion ac = new JythonAutoCompletion(p, textArea);
		ac.setListCellRenderer(new JythonCellRenderer());
		ac.setAutoCompleteEnabled(isAutoCompleteEnabled());
		ac.setAutoActivationEnabled(isAutoActivationEnabled());
		ac.setAutoActivationDelay(getAutoActivationDelay());
		ac.setExternalURLHandler(new JavadocUrlHandler());
		ac.setParameterAssistanceEnabled(isParameterAssistanceEnabled());
		ac.setParamChoicesRenderer(new JythonParamListCellRenderer());
		ac.setShowDescWindow(getShowDescWindow());
		ac.install(textArea);
		installImpl(textArea, ac);

		textArea.setToolTipSupplier(p);

		Listener listener = new Listener(textArea);
		textArea.putClientProperty(PROPERTY_LISTENER, listener);

		JythonParser parser = new JythonParser(textArea);
		textArea.putClientProperty(PROPERTY_LANGUAGE_PARSER, parser);
		textArea.addParser(parser);
		textArea.setToolTipSupplier(p);

		Info info = new Info(textArea, p, parser);
		parserToInfoMap.put(parser, info);
	}

	@Override public void uninstall( RSyntaxTextArea textArea )
	{
		uninstallImpl(textArea);

		JythonParser parser = getParser(textArea);
		Info info = parserToInfoMap.remove(parser);
		if (info!=null) { // Should always be true
			parser.removePropertyChangeListener(
					JythonParser.PROPERTY_COMPILATION_UNIT, info);
		}
		textArea.removeParser(parser);
		textArea.putClientProperty(PROPERTY_LANGUAGE_PARSER, null);
		textArea.setToolTipSupplier(null);

		Object listener = textArea.getClientProperty(PROPERTY_LISTENER);
		if (listener instanceof Listener) { // Should always be true
			((Listener)listener).uninstall();
			textArea.putClientProperty(PROPERTY_LISTENER, null);
		}
	}

	public JythonCompletionProvider getCompletionProvider(
			RSyntaxTextArea textArea) {
		AutoCompletion ac = getAutoCompletionFor(textArea);
		return (JythonCompletionProvider)ac.getCompletionProvider();
	}

	public JythonParser getParser(RSyntaxTextArea textArea) {
		// Could be a parser for another language.
		Object parser = textArea.getClientProperty(PROPERTY_LANGUAGE_PARSER);
		if (parser instanceof JythonParser) {
			return (JythonParser)parser;
		}
		return null;
	}

	/**
	 * A hack of <tt>AutoCompletion</tt> that forces the <tt>JavaParser</tt>
	 * to re-parse the document when the user presses ctrl+space.
	 */
	private class JythonAutoCompletion extends AutoCompletion {

		private RSyntaxTextArea textArea;
		private String replacementTextPrefix;

		public JythonAutoCompletion(JythonCompletionProvider provider,
				RSyntaxTextArea textArea) {
			super(provider);
			this.textArea = textArea;
		}

		private String getCurrentLineText() {

			int caretPosition = textArea.getCaretPosition();
			Element root = textArea.getDocument().getDefaultRootElement();
			int line= root.getElementIndex(caretPosition);
			Element elem = root.getElement(line);
			int endOffset = elem.getEndOffset();
			int lineStart = elem.getStartOffset();

			String text = "";
			try {
				text = textArea.getText(lineStart, endOffset-lineStart).trim();
			} catch (BadLocationException e) {
				e.printStackTrace();
			}

			System.out.println(text);

			return text;

		}

		/**
		 * Overridden to allow for prepending to the replacement text.  This
		 * allows us to insert fully qualified class names. instead of
		 * unqualified ones, if necessary (i.e. if the user tries to
		 * auto-complete <code>javax.swing.text.Document</code>, but they've
		 * explicitly imported <code>org.w3c.dom.Document</code> - we need to
		 * insert the fully qualified name in that case).
		 */
		@Override
		protected String getReplacementText(Completion c, Document doc,
				int start, int len) {
			String text = super.getReplacementText(c, doc, start, len);
			if (replacementTextPrefix!=null) {
				text = replacementTextPrefix + text;
				replacementTextPrefix = null;
			}
			return text;
		}

		/**
		 * Determines whether the class name being completed has been imported,
		 * and if it hasn't, returns the import statement that should be added
		 * for it.  Alternatively, if the class hasn't been imported, but a
		 * class with the same (unqualified) name HAS been imported, this
		 * method sets things up so the fully-qualified version of this class's
		 * name is inserted.<p>
		 *
		 * Thanks to Guilherme Joao Frantz and Jonatas Schuler for helping
		 * with the patch!
		 *
		 * @param cc The completion being inserted.
		 * @return Whether an import was added.
		 */
		private ImportToAddInfo getShouldAddImport(ClassCompletion cc) {

			String text = getCurrentLineText();

			System.out.println(text);

			// Make sure we're not currently typing an import statement.
			if (!text.startsWith("import ")) {

				JythonCompletionProvider provider = (JythonCompletionProvider)
						getCompletionProvider();
				CompilationUnit cu = provider.getCompilationUnit();
				int offset = 0;
				boolean alreadyImported = false;

				// Try to bail early, if possible.
				if (cu==null) { // Can never happen, right?
					return null;
				}
				if ("java.lang".equals(cc.getPackageName())) {
					// Package java.lang is "imported" by default.
					return null;
				}
				if ("nvisio.cip".equals(cc.getPackageName())) {
					// Package java.lang is "imported" by default.
					return null;
				}

				String className = cc.getClassName(false);
				String fqClassName = cc.getClassName(true);

				// If the completion is in the same package as the source we're
				// editing (or both are in the default package), bail.
				int lastClassNameDot = fqClassName.lastIndexOf('.');
				boolean ccInPackage = lastClassNameDot>-1;
				Package pkg = cu.getPackage();
				if (ccInPackage && pkg!=null) {
					String ccPkg = fqClassName.substring(0, lastClassNameDot);
					String pkgName = pkg.getName();
					if (ccPkg.equals(pkgName)) {
						return null;
					}
				}
				else if (!ccInPackage && pkg==null) {
					return null;
				}

				// Loop through all import statements.
				Iterator<ImportDeclaration > i = cu.getImportIterator();
				for (; i.hasNext(); ) {

					ImportDeclaration id = i.next();
					offset = id.getNameEndOffset() + 1;

					// Pulling in static methods, etc. from a class - skip
					if (id.isStatic()) {
						continue;
					}

					// Importing all classes in the package...
					else if (id.isWildcard()) {
						// NOTE: Class may be in default package...
						if (lastClassNameDot>-1) {
							String imported = id.getName();
							int dot = imported.lastIndexOf('.');
							String importedPkg = imported.substring(0, dot);
							String classPkg = fqClassName.substring(0, lastClassNameDot);
							if (importedPkg.equals(classPkg)) {
								alreadyImported = true;
								break;
							}
						}
					}

					// Importing a single class from a package...
					else {

						String fullyImportedClassName = id.getName();
						int dot = fullyImportedClassName.lastIndexOf('.');
						String importedClassName = fullyImportedClassName.
								substring(dot + 1);

						// If they explicitly imported a class with the
						// same name, but it's in a different package, then
						// the user is required to fully-qualify the class
						// in their code (if unqualified, it would be
						// assumed to be of the type of the qualified
						// class).
						if (className.equals(importedClassName)) {
							offset = -1; // Means "must fully qualify"
							if (fqClassName.equals(fullyImportedClassName)) {
								alreadyImported = true;
							}
							break;
						}

					}

				}

				// If the class wasn't imported, we'll need to add an
				// import statement!
				if (!alreadyImported) {

					StringBuilder importToAdd = new StringBuilder();

					// If there are no previous imports, add the import
					// statement after the package line (if any).
					if (offset == 0) {
						if (pkg!=null) {
							offset = pkg.getNameEndOffset() + 1;
							// Keep an empty line between package and imports.
							importToAdd.append('\n');
						}
					}

					// We read through all imports, but didn't find our class.
					// Add a new import statement after the last one.
					if (offset > -1) {
						//System.out.println(classCompletion.getAlreadyEntered(textArea));
						if (offset>0) {
							importToAdd.append("\nimport ").append(fqClassName).append(';');
						}
						else {
							importToAdd.append("import ").append(fqClassName).append(";\n");
						}
						// TODO: Determine whether the imports are alphabetical,
						// and if so, add the new one alphabetically.
						return new ImportToAddInfo(offset, importToAdd.toString());
					}

					// Otherwise, either the class was imported, or a class
					// with the same name was explicitly imported.
					else {
						// Another class with the same name was imported.
						// We must insert the fully-qualified class name
						// so the compiler resolves the correct class.
						int dot = fqClassName.lastIndexOf('.');
						if (dot>-1) {
							String pkgName = fqClassName.substring(0, dot+1);
							replacementTextPrefix = pkgName;
						}
					}

				}

			}

			return null;

		}

		/**
		 * Overridden to handle special cases, because sometimes Java code
		 * completions will edit more in the source file than just the text
		 * at the current caret position.
		 */
		@Override
		protected void insertCompletion(Completion c,
				boolean typedParamListStartChar) {

			ImportToAddInfo importInfo = null;

			// We special-case class completions because they may add import
			// statements to the top of our source file.  We don't add the
			// (possible) new import statement until after the completion is
			// inserted; that way, when we treat it as an atomic undo/redo,
			// when the user undoes the completion, the caret stays in the
			// code instead of jumping to the import.
			if (c instanceof ClassCompletion) {
				importInfo = getShouldAddImport((ClassCompletion)c);
				if (importInfo!=null) {
					textArea.beginAtomicEdit();
				}
			}

			try {
				super.insertCompletion(c, typedParamListStartChar);
				if (importInfo!=null) {
					textArea.insert(importInfo.text, importInfo.offs);
				}
			} finally {
				// Be safe and always pair beginAtomicEdit() and endAtomicEdit()
				textArea.endAtomicEdit();
			}

		}

		@Override
		protected int refreshPopupWindow() {
			// Force the parser to re-parse
			JythonParser parser = getParser(textArea);
			RSyntaxDocument doc = (RSyntaxDocument )textArea.getDocument();
			String style = textArea.getSyntaxEditingStyle();
			parser.parse(doc, style);
			return super.refreshPopupWindow();
		}

	}

	private static class Info implements PropertyChangeListener
	{

		public JythonCompletionProvider provider;
		//public JavaParser parser;

		public Info(RSyntaxTextArea textArea, JythonCompletionProvider provider,
				JythonParser parser) {
			this.provider = provider;
			//this.parser = parser;
			parser.addPropertyChangeListener(
					JythonParser.PROPERTY_COMPILATION_UNIT, this);
		}

		/**
		 * Called when a text area is re-parsed.
		 *
		 * @param e The event.
		 */
		@Override
		public void propertyChange(PropertyChangeEvent e) {

			String name = e.getPropertyName();

			if (JythonParser.PROPERTY_COMPILATION_UNIT.equals(name)) {
				CompilationUnit cu = (CompilationUnit)e.getNewValue();
				//				structureTree.update(file, cu);
				//				updateTable();
				provider.setCompilationUnit(cu);
			}

		}

	}

	private static class ImportToAddInfo {

		public int offs;
		public String text;

		public ImportToAddInfo(int offset, String text) {
			this.offs = offset;
			this.text = text;
		}

	}

	/**
	 * Listens for various events in a text area editing Java (in particular,
	 * caret events, so we can track the "active" code block).
	 */
	private class Listener implements CaretListener, ActionListener
	{

		private RSyntaxTextArea textArea;
		private Timer t;

		public Listener(RSyntaxTextArea textArea) {
			this.textArea = textArea;
			textArea.addCaretListener(this);
			t = new Timer(650, this);
			t.setRepeats(false);
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			JythonParser parser = getParser(textArea);
			if (parser==null) {
				return; // Shouldn't happen
			}
			CompilationUnit cu = parser.getCompilationUnit();

			// Highlight the line range of the Java method being edited in the
			// gutter.
			if (cu != null) { // Should always be true
				int dot = textArea.getCaretPosition();
				Point p = cu.getEnclosingMethodRange(dot);
				if (p != null) {
					try {
						int startLine = textArea.getLineOfOffset(p.x);
						// Unterminated blocks can end in Integer.MAX_VALUE
						int endOffs = Math.min(p.y,
								textArea.getDocument().getLength());
						int endLine = textArea.getLineOfOffset(endOffs);
						textArea.setActiveLineRange(startLine, endLine);
					} catch (BadLocationException ble) {
						ble.printStackTrace();
					}
				}
				else {
					textArea.setActiveLineRange(-1, -1);
				}
			}

		}

		@Override
		public void caretUpdate(CaretEvent e) {
			t.restart();
		}

		/**
		 * Should be called whenever Java language support is removed from a
		 * text area.
		 */
		public void uninstall() {
			textArea.removeCaretListener(this);
		}

	}
}
