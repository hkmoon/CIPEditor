package nvisio.cip.language.jython;

import org.fife.rsta.ac.java.JarManager;
import org.fife.rsta.ac.java.classreader.ClassFile;
import org.fife.rsta.ac.java.rjc.ast.ImportDeclaration;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: HongKee Moon
 * Support CIP package implicitly
 */
public class JythonJarManager extends JarManager
{
	public JythonJarManager()
	{
		super();
	}

	@Override
	public List<ClassFile> getClassesWithUnqualifiedName(String name,
			List<ImportDeclaration > importDeclarations) {

		List<ClassFile> result = super.getClassesWithUnqualifiedName( name, importDeclarations );

		// Also check java.lang
		String qualified = "nvisio.cip." + name;
		ClassFile entry = getClassEntry(qualified);
		if (entry!=null) {
			if (result==null) {
				result = new ArrayList<ClassFile>(1); // Usually small
			}
			result.add(entry);
		}

		return result;
	}
}
