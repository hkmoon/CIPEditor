package nvisio.cip.language.jython;

import java.awt.Graphics;
import javax.swing.Icon;

import org.fife.rsta.ac.java.AbstractJavaSourceCompletion;
import org.fife.rsta.ac.java.IconFactory;
import org.fife.rsta.ac.java.classreader.FieldInfo;
import org.fife.rsta.ac.java.rjc.ast.Field;
import org.fife.rsta.ac.java.rjc.lang.Type;
import org.fife.ui.autocomplete.CompletionProvider;

/**
 * Copied from the JavaLanguageSupport
 * Author: HongKee Moon
 */
public class FieldCompletion extends AbstractJavaSourceCompletion
		implements MemberCompletion {

	private Data data;

	/**
	 * The relevance of fields.  This allows fields to be "higher" in
	 * the completion list than other types.
	 */
	private static final int RELEVANCE		= 3;


	public FieldCompletion(CompletionProvider provider, Field field) {
		super(provider, field.getName());
		this.data = new FieldData(field);
		setRelevance(RELEVANCE);
	}


	public FieldCompletion(CompletionProvider provider, FieldInfo info) {
		super(provider, info.getName());
		this.data = new FieldInfoData(info, (SourceCompletionProvider)provider);
		setRelevance(RELEVANCE);
	}


	private FieldCompletion(CompletionProvider provider, String text) {
		super(provider, text);
		setRelevance(RELEVANCE);
	}


	@Override
	public boolean equals(Object obj) {
		return (obj instanceof FieldCompletion) &&
				((FieldCompletion)obj).getSignature().equals(getSignature());
	}


	public static FieldCompletion createLengthCompletion(
			CompletionProvider provider, final Type type) {
		FieldCompletion fc = new FieldCompletion(provider, type.toString());
		fc.data = new Data() {

			@Override
			public String getEnclosingClassName(boolean fullyQualified) {
				return type.getName(fullyQualified);
			}

//			@Override
//			public String getIcon() {
//				return IconFactory.FIELD_PUBLIC_ICON;
//			}
			@Override
			public String getIcon() {
				return null;
			}

			@Override
			public String getSignature() {
				return "length";
			}

			@Override
			public String getSummary() {
				return null;
			}

			@Override
			public String getType() {
				return "int";
			}

			@Override
			public boolean isConstructor() {
				return false;
			}

			@Override
			public boolean isDeprecated() {
				return false;
			}

			@Override
			public boolean isAbstract() {
				return false;
			}

			@Override
			public boolean isFinal() {
				return false;
			}

			@Override
			public boolean isStatic() {
				return false;
			}

		};
		return fc;
	}


	@Override
	public String getEnclosingClassName(boolean fullyQualified) {
		return data.getEnclosingClassName(fullyQualified);
	}


	@Override
	public Icon getIcon() {
		return IconFactory.get().getIcon(data);
	}

	@Override
	public String getSignature() {
		return data.getSignature();
	}


	@Override
	public String getSummary() {

		String summary = data.getSummary(); // Could be just the method name

		// If it's the Javadoc for the method...
		if (summary!=null && summary.startsWith("/**")) {
			summary = org.fife.rsta.ac.java.Util.docCommentToHtml(summary);
		}

		return summary;

	}


	@Override
	public String getType() {
		return data.getType();
	}


	@Override
	public int hashCode() {
		return getSignature().hashCode();
	}


	@Override
	public boolean isDeprecated() {
		return data.isDeprecated();
	}


	@Override
	public void rendererText(Graphics g, int x, int y, boolean selected) {
		MethodCompletion.rendererText(this, g, x, y, selected);
	}


}