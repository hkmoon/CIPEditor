package nvisio.cip.language.jython;

import org.fife.rsta.ac.java.classreader.MethodInfo;
import org.fife.rsta.ac.java.rjc.ast.FormalParameter;
import org.fife.rsta.ac.java.rjc.ast.Method;
import org.fife.rsta.ac.java.rjc.lang.Type;
import org.fife.ui.autocomplete.CompletionProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Copied from the JavaLanguageSupport
 */
public class CIPMethodCompletion extends MethodCompletion
{
	public CIPMethodCompletion( CompletionProvider provider, Method m )
	{
		// NOTE: "void" might not be right - I think this might be constructors
		super( provider, m.getName(), m.getType() == null ? "void" : m.getType().toString() );
		setDefinedIn( m.getParentTypeDeclaration().getName() );
		this.data = new MethodData( m );
		setRelevanceAppropriately();

		int count = m.getParameterCount();
		List< Parameter > params = new ArrayList< Parameter >( count );
		for ( int i = 0; i < count; i++ )
		{
			FormalParameter param = m.getParameter( i );
			Type type = param.getType();
			String name = param.getName();
			params.add( new Parameter( type, name ) );
		}
		setParams( params );
	}

	/**
	 * Creates a completion for a method discovered when parsing a compiled
	 * class file.
	 *
	 * @param provider
	 * @param info Meta data about the method.
	 */
	public CIPMethodCompletion(CompletionProvider provider, MethodInfo info) {

		super(provider, info.getName(), info.getReturnTypeString(false));
		setDefinedIn(info.getClassFile().getClassName(false));
		this.data = new MethodInfoData(info, (SourceCompletionProvider)provider);
		setRelevanceAppropriately();

		String[] paramTypes = info.getParameterTypes();
		List<Parameter> params = new ArrayList<Parameter>(paramTypes.length);
		for (int i=0; i<paramTypes.length; i++) {
			String name = ((MethodInfoData)data).getParameterName(i);
			String type = paramTypes[i].substring(paramTypes[i].lastIndexOf('.')+1);
			params.add(new Parameter(type, name));
		}
		setParams(params);
	}
}
