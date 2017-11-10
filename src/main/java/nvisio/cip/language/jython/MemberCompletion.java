package nvisio.cip.language.jython;

import org.fife.rsta.ac.java.IconFactory;
import org.fife.rsta.ac.java.JavaSourceCompletion;

/**
 * Copied from the JavaLanguageSupport
 * Author: HongKee Moon
 */
interface MemberCompletion extends JavaSourceCompletion
{


	/**
	 * Returns the name of the enclosing class.
	 *
	 * @param fullyQualified Whether the name returned should be fully
	 *        qualified.
	 * @return The class name.
	 */
	public String getEnclosingClassName(boolean fullyQualified);


	/**
	 * Returns the signature of this member.
	 *
	 * @return The signature.
	 */
	public String getSignature();


	/**
	 * Returns the type of this member (the return type for methods).
	 *
	 * @return The type of this member.
	 */
	public String getType();


	/**
	 * Returns whether this member is deprecated.
	 *
	 * @return Whether this member is deprecated.
	 */
	public boolean isDeprecated();


	/**
	 * Meta data about the member.  Member completions will be constructed
	 * from a concrete instance of this interface.  This is because there are
	 * two sources that member completions come from - parsing Java source
	 * files and parsing compiled class files (in libraries).
	 */
	public static interface Data extends IconFactory.IconData
	{

		/**
		 * Returns the name of the enclosing class.
		 *
		 * @param fullyQualified Whether the name returned should be fully
		 *        qualified.
		 * @return The class name.
		 */
		public String getEnclosingClassName(boolean fullyQualified);

		/**
		 * Returns the signature of this member.
		 *
		 * @return The signature.
		 * @see MemberCompletion#getSignature()
		 */
		public String getSignature();

		/**
		 * Returns the summary description (should be HTML) for this member.
		 *
		 * @return The summary description, or <code>null</code> if there is
		 *         none.
		 * @see MemberCompletion#getSummary()
		 */
		public String getSummary();

		/**
		 * Returns the type of this member (the return type for methods).
		 *
		 * @return The type of this member.
		 * @see MemberCompletion#getType()
		 */
		public String getType();

		/**
		 * Returns whether this member is a constructor.
		 *
		 * @return Whether this member is a constructor.
		 */
		public boolean isConstructor();

	}


}