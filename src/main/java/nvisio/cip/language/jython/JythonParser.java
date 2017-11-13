package nvisio.cip.language.jython;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;

import org.fife.io.DocumentReader;
import org.fife.rsta.ac.java.rjc.ast.CompilationUnit;
import org.fife.rsta.ac.java.rjc.lexer.Scanner;
import org.fife.rsta.ac.java.rjc.notices.ParserNotice;
import org.fife.rsta.ac.java.rjc.parser.ASTFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;

/**
 * Copied from the JavaLanguageSupport
 */
public class JythonParser extends AbstractParser {

	/**
	 * The property change event that's fired when the document is re-parsed.
	 * Applications can listen for this property change and update themselves
	 * accordingly.
	 */
	public static final String PROPERTY_COMPILATION_UNIT = "CompilationUnit";

	private CompilationUnit cu;
	private PropertyChangeSupport support;
	private DefaultParseResult result;


	/**
	 * Constructor.
	 */
	public JythonParser(RSyntaxTextArea textArea) {
		support = new PropertyChangeSupport(this);
		result = new DefaultParseResult(this);
	}


	/**
	 * Adds all notices from the Java parser to the results object.
	 */
	private void addNotices(RSyntaxDocument doc) {

		result.clearNotices();
		int count = cu==null ? 0 : cu.getParserNoticeCount();

		if (count==0) {
			return;
		}

		for (int i=0; i<count; i++) {
			ParserNotice notice = cu.getParserNotice(i);
			int offs = getOffset(doc, notice);
			if (offs>-1) {
				int len = notice.getLength();
				result.addNotice(new DefaultParserNotice(this,
						notice.getMessage(), notice.getLine(), offs, len));
			}
		}

	}


	public void addPropertyChangeListener(String prop, PropertyChangeListener l) {
		support.addPropertyChangeListener(prop, l);
	}


	/**
	 * Returns the compilation unit from the last time the text area was
	 * parsed.
	 *
	 * @return The compilation unit, or <code>null</code> if it hasn't yet
	 *         been parsed or an unexpected error occurred while parsing.
	 */
	public CompilationUnit getCompilationUnit() {
		return cu;
	}


	public int getOffset(RSyntaxDocument doc, ParserNotice notice) {
		Element root = doc.getDefaultRootElement();
		Element elem = root.getElement(notice.getLine());
		int offs = elem.getStartOffset() + notice.getColumn();
		return offs>=elem.getEndOffset() ? -1 : offs;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public ParseResult parse(RSyntaxDocument doc, String style) {

		cu = null;
		result.clearNotices();
		// Always spell check all lines, for now.
		int lineCount = doc.getDefaultRootElement().getElementCount();
		result.setParsedLines(0, lineCount-1);

		DocumentReader r = new DocumentReader(doc);
		Scanner scanner = new Scanner(r);

		scanner.setDocument(doc);
		ASTFactory fact = new ASTFactory();
		long start = System.currentTimeMillis();
		try {
			cu = fact.getCompilationUnit("SomeFile.java", scanner); // TODO: Real name?
			long time = System.currentTimeMillis() - start;
			result.setParseTime(time);
		} finally {
			r.close();
		}

		//addNotices(doc);
		support.firePropertyChange(PROPERTY_COMPILATION_UNIT, null, cu);
		return result;

	}


	public void removePropertyChangeListener(String prop, PropertyChangeListener l) {
		support.removePropertyChangeListener(prop, l);
	}


}