package nvisio.cip.language.jython;

import org.fife.rsta.ac.java.IconFactory;
import org.fife.rsta.ac.java.rjc.ast.Method;
import org.fife.rsta.ac.java.rjc.ast.TypeDeclaration;
import org.fife.rsta.ac.java.rjc.lang.Modifiers;
import org.fife.rsta.ac.java.rjc.lang.Type;

/**
 * Copied from the JavaLanguageSupport
 * Author: HongKee Moon
 */
public class MethodData implements MemberCompletion.Data
{

	private Method method;


	public MethodData(Method method) {
		this.method = method;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEnclosingClassName(boolean fullyQualified) {
		// NOTE: This check isn't really necessary, but is here just in case
		// there's a bug in the parsing code.
		TypeDeclaration td = method.getParentTypeDeclaration();
		if (td==null) {
			new Exception("No parent type declaration for: " + getSignature()).
					printStackTrace();
			return "";
		}
		return td.getName(fullyQualified);
	}


	@Override
	public String getIcon() {

		String key = null;

		Modifiers mod = method.getModifiers();
		if (mod==null) {
			key = IconFactory.METHOD_DEFAULT_ICON;
		}
		else if (mod.isPrivate()) {
			key = IconFactory.METHOD_PRIVATE_ICON;
		}
		else if (mod.isProtected()) {
			key = IconFactory.METHOD_PROTECTED_ICON;
		}
		else if (mod.isPublic()) {
			key = IconFactory.METHOD_PUBLIC_ICON;
		}
		else {
			key = IconFactory.METHOD_DEFAULT_ICON;
		}

		return key;

	}


	@Override
	public String getSignature() {
		return method.getNameAndParameters();
	}


	@Override
	public String getSummary() {
		String docComment = method.getDocComment();
		return docComment!=null ? docComment : method.toString();
	}


	@Override
	public String getType() {
		Type type = method.getType();
		return type==null ? "void" : type.toString();
	}


	@Override
	public boolean isAbstract() {
		return method.getModifiers().isAbstract();
	}


	@Override
	public boolean isConstructor() {
		return method.isConstructor();
	}


	@Override
	public boolean isDeprecated() {
		return method.isDeprecated();
	}


	@Override
	public boolean isFinal() {
		return method.getModifiers().isFinal();
	}


	@Override
	public boolean isStatic() {
		return method.getModifiers().isStatic();
	}


}