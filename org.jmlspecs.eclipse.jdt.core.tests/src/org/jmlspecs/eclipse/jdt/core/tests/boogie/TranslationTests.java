package org.jmlspecs.eclipse.jdt.core.tests.boogie;

import java.util.Map;

import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.jmlspecs.jml4.boogie.BoogieAdapter;
import org.jmlspecs.jml4.compiler.JmlCompilerOptions;
import org.jmlspecs.jml4.esc.PostProcessor;

public class TranslationTests extends AbstractRegressionTest {
	protected final String testsPath = "";
	
	public TranslationTests(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		BoogieAdapter.DEBUG = true;
		PostProcessor.useOldErrorReporting = true;
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		PostProcessor.useOldErrorReporting = false;
	}
	
	private Map<String, String> options = null;

	// Augment problem detection settings
	@Override
	@SuppressWarnings("unchecked")
	protected Map<String, String> getCompilerOptions() {
		if (options == null) {
			options = super.getCompilerOptions();
			options.put(JmlCompilerOptions.OPTION_EnableJml, CompilerOptions.ENABLED);
			options.put(JmlCompilerOptions.OPTION_EnableJmlDbc, CompilerOptions.ENABLED);
			options.put(JmlCompilerOptions.OPTION_EnableJmlBoogie, CompilerOptions.ENABLED);
			options.put(JmlCompilerOptions.OPTION_JmlBoogieOutputOnly, CompilerOptions.DISABLED);
			options.put(JmlCompilerOptions.OPTION_DefaultNullity, JmlCompilerOptions.NON_NULL);
			options.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
			options.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.ERROR);
			options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.IGNORE);
			options.put(JmlCompilerOptions.OPTION_ReportNonNullTypeSystem, CompilerOptions.ERROR);
			options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
			options.put(CompilerOptions.OPTION_ReportUnnecessaryElse, CompilerOptions.IGNORE);
			options.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.IGNORE);
			options.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.IGNORE);
			// options.put(JmlCompilerOptions.OPTION_SpecPath,
			// NullTypeSystemTestCompiler.getSpecPath());
			options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
			options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
			options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
		}
		return options;
	}
	
	protected void compareJavaToBoogie(String java, String boogie, String adapterOutput) {
		String file = "A.java";
		if (boogie != null) {
			String key = JmlCompilerOptions.OPTION_JmlBoogieOutputOnly;
			getCompilerOptions().put(key, CompilerOptions.ENABLED);
			runNegativeTest(new String[] {file, java},
					"----------\n" +
					"1. ERROR in "+ file + " (at line 1)\n" +
					"	package tests.esc;\n" +
					"	^\n\n" +
					boogie +
					"\n----------\n");
			getCompilerOptions().put(key, CompilerOptions.DISABLED);
		}
	}

	protected void compareJavaExprToBoogie(String java, String boogie) {
		String key = JmlCompilerOptions.OPTION_JmlBoogieOutputOnly;
		getCompilerOptions().put(key, CompilerOptions.ENABLED);
		runNegativeTest(new String[] {"A.java", 
				"package a;\npublic class A { public A() { assert " + java + " == " + java + "; } }" },
				"----------\n" +
				"1. ERROR in A.java (at line 1)\n" +
				"	package a;\n" +
				"	^\n\n" +
				"procedure a.A.A(this: $Ref) requires $dtype(this) == a.A; {\n" +
				"	assert (" + boogie + " == " + boogie + ");\n" +
				"}\n" +
				"\n----------\n");
	}

	// term=FalseLiteral
	public void testFalseLiteral() {
		compareJavaExprToBoogie("false", "false");
	}

	// term=TrueLiteral
	public void testTrueLiteral() {
		compareJavaExprToBoogie("true", "true");
	}

	// term=IntLiteral
	public void testIntLiteral() {
		compareJavaExprToBoogie("2", "2");
	}
	
	// TODO term=DoubleLiteral
	public void testDoubleLiteral() {
		compareJavaExprToBoogie("2.2456", "0"); // FIXME
	}
	
	// term=CharLiteral
	public void testCharLiteral() {
		compareJavaExprToBoogie("'a'", "97");
		compareJavaExprToBoogie("'\\6'", "6");
		compareJavaExprToBoogie("'\\n'", "10");
	}
	
/*******************************************
*			ASSERTS
*******************************************/	
	
	// term=JmlAssertStatement adapter=pass
	public void test_001_assertFalse() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m() {\n" + 
				"      //@ assert false;\n" + 
				"   }\n" + 
				"}\n" 
				,
				// expected boogie
				"procedure tests.esc.A.m(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert false;\n" +
				"}\n",
				// adapter output
				"----------\n" + 
				"1. ERROR in " + testsPath + "A.java (at line 4)\n" + 
				"	//@ assert false;\n" +
				"	           ^^^^^\n" + 
				"This assertion might not hold.\n" + 
				"----------\n"	
		);
	}

	// term=JmlAssertStatement adapter=pass
	public void test_002_assertTrue() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m() {\n" + 
				"      //@ assert true;\n" + 
				"   }\n" + 
				"}\n" 
				,
				// expected boogie
				"procedure tests.esc.A.m(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert true;\n" +
				"}\n",
				// adapter output
				"" 		
		);
	}

	// term=JmlMethodDeclaration,JmlAssertStatement,Argument adapter=pass
	public void test_003_assertParam() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m(boolean b) {\n" + 
				"      //@ assert b;\n" + 
				"   }\n" + 
				"}\n" 
				,
				// expected boogie
				"procedure tests.esc.A.m(this: $Ref, a: bool) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert a;\n" +
				"}\n",
				// adapter output
				"----------\n" + 
				"1. ERROR in " + testsPath + "A.java (at line 4)\n" + 
				"	//@ assert b;\n" +
				"	           ^\n" + 
				"This assertion might not hold.\n" + 
				"----------\n" 			
				);
	}	
	
	// term=JmlAssertStatement,AND_AND_Expression adapter=pass
	public void test_004_assert_sequence_and() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m1() {\n" + 
				"      //@ assert false && false;\n" + 
				"   }\n" +
				"}\n"		
				,
				// expected boogie
				"procedure tests.esc.A.m1(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert (false && false);\n" +
				"}\n",
				// adapter output
				"----------\n" +
				"1. ERROR in " + testsPath + "A.java (at line 4)\n" +
				"	//@ assert false && false;\n" +
				"	           ^^^^^^^^^^^^^^\n" +
				"This assertion might not hold.\n" +
				"----------\n"
				);
	}
	
	// term=JmlAssertStatement,OR_OR_Expression adapter=pass
	public void test_005_assert_sequence_or() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m1() {\n" + 
				"      //@ assert false || false;\n" + 
				"   }\n" + 				
				"}\n"
				,				
				// expected boogie
				"procedure tests.esc.A.m1(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert (false || false);\n" +
				"}\n",
				// adapter output
				"----------\n" +
				"1. ERROR in " + testsPath + "A.java (at line 4)\n" +
				"	//@ assert false || false;\n" +
				"	           ^^^^^^^^^^^^^^\n" +
				"This assertion might not hold.\n" +
				"----------\n"
				);
	}	
			
	// term=JmlAssertStatement,AND_AND_Expression,OR_OR_Expression adapter=pass
	public void test_006_assert_sequence_and_or() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m1() {\n" + 
				"      //@ assert (false && (false || false));\n" + 
				"   }\n" + 		
				"}\n"
				,
				//expected boogie
				"procedure tests.esc.A.m1(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert (false && (false || false));\n" +
				"}\n",
				// adapter output
				"----------\n" + 
				"1. ERROR in " + testsPath + "A.java (at line 4)\n" + 
				"	//@ assert (false && (false || false));\n" +
				"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"This assertion might not hold.\n" + 
				"----------\n"			
				);
	}	
	
	// term=JmlAssertStatement,AND_AND_Expression,OR_OR_Expression adapter=pass
	public void test_007_assert_sequence_or_and() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m1() {\n" + 
				"      //@ assert ((false || false) && false);\n" + 
				"   }\n" + 			
				"}\n"
				,
				//expected boogie
				"procedure tests.esc.A.m1(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert ((false || false) && false);\n" +
				"}\n",
				// adapter output
				"----------\n" + 
				"1. ERROR in " + testsPath + "A.java (at line 4)\n" + 
				"	//@ assert ((false || false) && false);\n" +
				"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"This assertion might not hold.\n" + 
				"----------\n"
				);
	}

	// term=JmlAssertStatement adapter=pass
	public void test_008_assert_sequence_tt() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m() {\n" + 
				"      //@ assert true;\n" + 
				"      //@ assert true;\n" + 
				"   }\n" + 
				"}\n" 
				,
				//expected boogie
				"procedure tests.esc.A.m(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert true;\n" +
				"	assert true;\n" +				
				"}\n",
				// adapter output
				""
				);
	}	
	
	// term=JmlAssertStatement adapter=pass
	public void test_009_assert_sequence_tf() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m() {\n" + 
				"      //@ assert true;\n" + 
				"      //@ assert false;\n" + 
				"   }\n" + 
				"}\n" 
				,
				//expected boogie
				"procedure tests.esc.A.m(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert true;\n" +
				"	assert false;\n" +				
				"}\n",
				// adapter output
				"----------\n" + 
				"1. ERROR in " + testsPath + "A.java (at line 5)\n" + 
				"	//@ assert false;\n" +
				"	           ^^^^^\n" + 
				"This assertion might not hold.\n" + 
				"----------\n"
				);
	}
	
	// term=JmlAssertStatement adapter=pass
	public void test_007_assert_sequence_ft() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m() {\n" + 
				"      //@ assert false;\n" + 
				"      //@ assert true;\n" + 
				"   }\n" + 
				"}\n" 
				,
				//expected boogie
				"procedure tests.esc.A.m(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert false;\n" +
				"	assert true;\n" +				
				"}\n",
				// adapter output
				"----------\n" + 
				"1. ERROR in " + testsPath + "A.java (at line 4)\n" + 
				"	//@ assert false;\n" +
				"	           ^^^^^\n" + 
				"This assertion might not hold.\n" + 
				"----------\n"
				);
	}
	
	// term=JmlAssertStatement adapter=pass
	public void test_008_assert_sequence_ff() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m() {\n" + 
				"      //@ assert false;\n" + 
				"      //@ assert false;\n" + 
				"   }\n" + 
				"}\n" 
				,
				//expected boogie
				"procedure tests.esc.A.m(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert false;\n" +
				"	assert false;\n" +				
				"}\n",
				// adapter output
				"----------\n" + 
				"1. ERROR in " + testsPath + "A.java (at line 4)\n" + 
				"	//@ assert false;\n" +
				"	           ^^^^^\n" + 
				"This assertion might not hold.\n" + 
				"----------\n"
				);
	}	
	
	// term=AssertStatement adapter=pass
	public void test_009_JavaAssertFalse() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m() {\n" + 
				"      assert false;\n" + 
				"      assert false: 1234;\n" + 
				"   }\n" + 
				"}\n" 
				,
				// expected boogie
				"procedure tests.esc.A.m(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert false;\n" +
				"	assert false;\n" +
				"}\n",
				// adapter output
				"----------\n" + 
				"1. ERROR in " + testsPath + "A.java (at line 4)\n" + 
				"	assert false;\n" +
				"	       ^^^^^\n" + 
				"This assertion might not hold.\n" + 
				"----------\n"	
		);
	}

	// term=AssertStatement adapter=pass
	public void test_010_JavaAssertTrue() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m() {\n" + 
				"      assert true;\n" + 
				"   }\n" + 
				"}\n" 
				,
				// expected boogie
				"procedure tests.esc.A.m(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert true;\n" +
				"}\n",
				// adapter output
				"" 		
		);
	}
		
/*******************************************
*			ASSUMES
*******************************************/
		
	// term=JmlAssumeStatement adapter=pass
	public void test_0100_assumeFalse() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m() {\n" + 
				"      //@ assume false;\n" + 
				"   }\n" + 
				"}\n"
				,
				// expected boogie
				"procedure tests.esc.A.m(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assume false;\n" +
				"}\n",
				// adapter output
				"" 				
				);
	}	
	
	// term=JmlAssumeStatement adapter=pass
	public void test_0101_assumeTrue() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m() {\n" + 
				"      //@ assume true;\n" + 
				"   }\n" + 
				"}\n" 
				,
				// expected boogie
				"procedure tests.esc.A.m(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assume true;\n" +
				"}\n",
				// adapter output
				"" 				
				);
	}

	// term=JmlMethodDeclaration adapter=pass
	public void test_0110_JmlMethodDeclaration_EmptyMethod() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m1() {\n" +
				"      \n" + 
				"   }\n" + 
				"}\n" 
				,
				//expected boogie
				"procedure tests.esc.A.m1(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"}\n",
				// adapter output
				""
				);
	}
	
	// term=JmlMethodDeclaration,Argument,JmlResultReference,JmlMethodSpecification,ReturnStatement,JmlAssertStatement,EqualExpression adapter=pass
	public void test_0111_MethodDefinition() {
		this.compareJavaToBoogie(
			// java
			"package tests.esc;\n" + 
			"public class A {\n" +
			"   public void m() {\n" + 
			"   	//@ assert false;\n" + 
			"   }\n" +
			"	" +
			"   //@ ensures \\result == 42;\n" + 
			"	public int n() {\n" +
			"		//@ assert true;\n" +
			"		return 42;\n" +
			"	}\n" + 
			"}\n"
			,
			// expected boogie
			"procedure tests.esc.A.m(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
			"	assert false;\n" +
			"}\n" +
			"procedure tests.esc.A.n(this: $Ref) returns ($r: int) requires $dtype(this) == tests.esc.A; ensures ($r == 42); {\n" +
			"	assert true;\n" +
			"	$r := 42;\n" +
			"	return;\n" +
			"}\n",
			// adapter output
			"----------\n" + 
			"1. ERROR in " + testsPath + "A.java (at line 4)\n" + 
			"	//@ assert false;\n" +
			"	           ^^^^^\n" + 
			"This assertion might not hold.\n" + 
			"----------\n"
		);
	}
	
	// term=JmlMethodDeclaration,JmlAssertStatement adapter=pass
	public void test_0112_DoubleMethodDefinition() {
		this.compareJavaToBoogie(
				// java
				"package tests.esc;\n" +
				"public class A {\n" +
				"   public void m() {\n" + 
				"   	//@ assert false;\n" + 
				"   }\n" +
				"	public void n() {\n" +
				"		//@ assert false;\n" +
				"	}\n" + 
				"}\n",	
				// expected boogie
				"procedure tests.esc.A.m(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert false;\n" +
				"}\n" +
				"procedure tests.esc.A.n(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert false;\n" +
				"}\n",
				// adapter output
				"----------\n" +
				"1. ERROR in " + testsPath + "A.java (at line 4)\n" +
				"	//@ assert false;\n" +
				"	           ^^^^^\n" +
				"This assertion might not hold.\n" + 
				"----------\n" +
				"2. ERROR in " + testsPath + "A.java (at line 7)\n" +
				"	//@ assert false;\n" +
				"	           ^^^^^\n" +
				"This assertion might not hold.\n" + 
				"----------\n");
	}
	
	// term=JmlResultExpression,JmlMethodDeclaration,JmlMethodSpecification,JmlEnsuresClause,JmlRequiresClause adapter=pass
	public void test_0112_JmlMethodDefinition_EnsuresRequires() {
		this.compareJavaToBoogie(
				//java				
				"package tests.esc;\n" +
				"public class A {\n" +
				"   //@ ensures \\result == 42;\n" + 
				"	public int m1() {\n" +
				"		return 42;\n" +
				"	}\n" + 
				"	" +
				"   //@ requires n >= 0;\n" + 
				"	public int m2(int n) {\n" +
				"      if (n == 0)\n" +
		        "         return 1;\n" +
		        "	   return 10;\n"+
				"	}\n" +  
				"   //@ requires n >= 0;\n" + 				
				"   //@ ensures \\result == 42;\n" + 
				"	public int m3(int n) {\n" +
				"      if (n == 0)\n" +
		        "         return 42;\n" +
				"		return 42;\n" +
				"	}\n" + 		
				"   //@ requires n >= 0;\n" + 				
				"   //@ ensures \\result == 42;\n" + 
				"	public int m4(int n) {\n" +
				"      if (n == 0)\n" +
		        "         return 1;\n" +
				"		return 42;\n" +
				"	}\n" +
				"}\n"	
				,
				"procedure tests.esc.A.m1(this: $Ref) returns ($r: int) requires $dtype(this) == tests.esc.A; ensures ($r == 42); {\n" +
				"	$r := 42;\n" +
				"	return;\n" +
				"}\n" +
				"procedure tests.esc.A.m2(this: $Ref, a: int) returns ($r: int) requires $dtype(this) == tests.esc.A; requires (a >= 0); {\n" +
				"	if (a == 0) {\n" +
				"		$r := 1;\n" +
				"		return;\n" +
				"	}\n" +
				"	$r := 10;\n" +
				"	return;\n" +
				"}\n" +
				"procedure tests.esc.A.m3(this: $Ref, a: int) returns ($r: int) requires $dtype(this) == tests.esc.A; requires (a >= 0); ensures ($r == 42); {\n" +
				"	if (a == 0) {\n" +
				"		$r := 42;\n" +
				"		return;\n" +
				"	}\n" +
				"	$r := 42;\n" +
				"	return;\n" +
				"}\n" +
				"procedure tests.esc.A.m4(this: $Ref, a: int) returns ($r: int) requires $dtype(this) == tests.esc.A; requires (a >= 0); ensures ($r == 42); {\n" +
				"	if (a == 0) {\n" +
				"		$r := 1;\n" +
				"		return;\n" +
				"	}\n" +
				"	$r := 42;\n" +
				"	return;\n" +
				"}\n"
				,
				// adapter output
				"----------\n" +
				"1. ERROR in " + testsPath + "A.java (at line 21)\n" +
				"	//@ ensures \\result == 42;\n" +
				"	            ^^^^^^^^^^^^^\n" +
				"This postcondition might not hold.\n" +
				"----------\n" +
				"2. ERROR in " + testsPath + "A.java (at line 24)\n" +
				"	return 1;\n" +
				"	       ^\n" +
				"This postcondition might not hold.\n" +
				"----------\n"
				);
	}
	
	// term=JmlRequiresClause,JmlMethodSpecification adapter=pass
	public void test_0113_Requires() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" +
				"	//@ requires n >= 0;\n" +
				"	public int fib(int n) {\n" +
				"		if (n < 2) return n;\n" +
				"		int n1 = fib(n - 1);\n" +
				"		int n2 = fib(n - 2);\n" +
				"		return n1 + n2;\n" +
				"	}\n" +
				"	\n" +
				"	public void n() {\n" +
				"		int i = fib(-1);\n" +
				"	}\n" +
				"}\n" ,
				// boogie
				"procedure tests.esc.A.fib(this: $Ref, a: int) returns ($r: int) requires $dtype(this) == tests.esc.A; requires (a >= 0); {\n" +
				"	var b: int;\n" +
				"	var c: int;\n" +
				"	if (a < 2) {\n" +
				"		$r := a;\n" +
				"		return;\n" +
				"	}\n" +
				"	call b := tests.esc.A.fib(this, (a - 1));\n" +
				"	call c := tests.esc.A.fib(this, (a - 2));\n" +
				"	$r := (b + c);\n" +
				"	return;\n" +
				"}\n" +
				"procedure tests.esc.A.n(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: int;\n" +
				"	call a := tests.esc.A.fib(this, -1);\n" +
				"}\n",
				// adapter output
				"----------\n" +
				"1. ERROR in A.java (at line 3)\n" +
				"	//@ requires n >= 0;\n" +
				"	             ^^^^^^\n" +
				"This precondition might not hold.\n" +
				"----------\n" +
				"2. ERROR in A.java (at line 12)\n" +
				"	int i = fib(-1);\n" +
				"	        ^^^^^^^\n" +
				"This precondition might not hold.\n" +
				"----------\n"
			);
	}
	
	// term=JmlEnsuresClause,JmlMethodSpecification adapter=pass
	public void test_0114_Ensures() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" +
				"	//@ ensures ((n < 2) ==> (\\result == n)) && ((n >= 2) ==> (\\result == 5));\n" +
				"	public int a(int n) {\n" +
				"		if (n < 2) return n;\n" +
				"		return 5;\n" +
				"	}\n" +
				"	\n" +
				"	public void n() {\n" +
				"		int i = a(0);\n" +
				"		assert i == 1;\n" +
				"	}\n" +
				"	public void m() {\n" +
				"		int i = a(2);\n" +
				"		assert i == 5;\n" +
				"	}\n" +
				"}\n" ,
				// boogie
				"procedure tests.esc.A.a(this: $Ref, a: int) returns ($r: int) requires $dtype(this) == tests.esc.A; ensures (((a < 2) ⇒ ($r == a)) && ((a >= 2) ⇒ ($r == 5))); {\n" +
				"	if (a < 2) {\n" +
				"		$r := a;\n" +
				"		return;\n" +
				"	}\n" +
				"	$r := 5;\n" +
				"	return;\n" +
				"}\n" +
				"procedure tests.esc.A.n(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: int;\n" +
				"	call a := tests.esc.A.a(this, 0);\n" +
				"	assert (a == 1);\n" +
				"}\n" +
				"procedure tests.esc.A.m(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: int;\n" +
				"	call a := tests.esc.A.a(this, 2);\n" +
				"	assert (a == 5);\n" +
				"}\n"
				,
				// adapter output
				"----------\n" +
				"1. ERROR in A.java (at line 11)\n" +
				"	assert i == 1;\n" +
				"	       ^^^^^^\n" +
				"This assertion might not hold.\n" +
				"----------\n"
			);
	}
	
	// term=JmlEnsuresClause,JmlMethodSpecification adapter=pass
	public void test_0115_MultipleEnsures() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" +
				"	//@ ensures true;\n" +
				"	//@ ensures true;\n" +
				"	//@ ensures false;\n" +
				"	public void a() { }\n" +
				"}\n" ,
				// boogie
				"procedure tests.esc.A.a(this: $Ref) requires $dtype(this) == tests.esc.A; ensures true; ensures true; ensures false; {\n" +
				"}\n" 
				,
				// adapter output
				"----------\n" +
				"1. ERROR in A.java (at line 5)\n" +
				"	//@ ensures false;\n" +
				"	            ^^^^^\n" +
				"This postcondition might not hold.\n" +
				"----------\n" +
				"2. ERROR in A.java (at line 6)\n" +
				"	public void a() { }\n" +
				"	            ^^^\n" +
				"This postcondition might not hold.\n" +
				"----------\n"
			);
	}

	// term=JmlAssumeStatement,JmlAssertStatement adapter=pass
	public void test_0200_sequence_assume_assert_tt() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m() {\n" + 
				"      //@ assume true;\n" + 
				"      //@ assert true;\n" + 
				"   }\n" + 
				"}\n" 
				,
				// expected boogie
				"procedure tests.esc.A.m(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assume true;\n" +
				"	assert true;\n" +
				"}\n",
				// adapter output
				""
				);
	}
	
	// term=JmlAssumeStatement,JmlAssertStatement adapter=pass
	public void test_0201_sequence_assume_assert_ff() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m() {\n" + 
				"      //@ assume false;\n" + 
				"      //@ assert false;\n" + 
				"   }\n" + 
				"}\n" 
				,
				// expected boogie
				"procedure tests.esc.A.m(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assume false;\n" +
				"	assert false;\n" +
				"}\n",
				// adapter output
				"" 
				);
	}	
	
	// term=JmlAssumeStatement,JmlAssertStatement adapter=pass
	public void test_0202_sequence_assume_assert_ft() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m() {\n" + 
				"      //@ assume false;\n" + 
				"      //@ assert true;\n" + 
				"   }\n" + 
				"}\n" 
				,
				// expected boogie
				"procedure tests.esc.A.m(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assume false;\n" +
				"	assert true;\n" +
				"}\n",
				// adapter output
				""
				);
	}	
	
	// term=JmlAssumeStatement,JmlAssertStatement adapter=pass
	public void test_0203_sequence_assume_assert_tf() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m() {\n" + 
				"      //@ assume true;\n" + 
				"      //@ assert false;\n" + 
				"   }\n" + 
				"}\n" 
				,
				// expected boogie
				"procedure tests.esc.A.m(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assume true;\n" +
				"	assert false;\n" +
				"}\n",
				// adapter output
				"----------\n" +
				"1. ERROR in " + testsPath + "A.java (at line 5)\n" +
				"	//@ assert false;\n" +
				"	           ^^^^^\n" +
				"This assertion might not hold.\n" +
				"----------\n"
				);
	}	
	
	// term=LocalDeclaration adapter=pass
	public void test_0295_LocalDeclarationDefaultInitialization() {
		compareJavaToBoogie(
			// java source
			"package tests.esc;\n" +
			"public class A {\n" +
			"	public void m() {\n" +
			"		int z;\n" + 
			"		boolean b;\n" +
			"		String q;\n" +
			"		long p;\n" +
//			"		assert z == 0;\n" +
//			"		assert b == false;\n" +
//			"		assert q == null;\n" +
//			"		assert p == 0;\n" + 
			"	}\n" + 
			"}\n"
			, 
			// expected boogie
			"procedure tests.esc.A.m(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
			"	var a: int;\n" +
			"	var b: bool;\n" +
			"	var c: $Ref;\n" +
			"	var d: long;\n" +
			"	assume $dtype(c) == java.lang.String;\n" +
			"	a := 0;\n" +
			"	b := false;\n" +
			"	c := null;\n" +
			"	d := 0;\n" +
//			"	assert (a == 0);\n" +
//			"	assert (b == false);\n" +
//			"	assert (c == null);\n" +
//			"	assert (d == 0);\n" + 
			"}\n",
			// adapter output
			"");
	}
		
	// term=LocalDeclaration,Block adapter=pass
	public void test_0296_LocalDeclaration_Blocks() {
		compareJavaToBoogie(
			// java source
			"package tests.esc;\n" + 				
			"public class A {\n" +
			"   public void m5() {\n" +
			"       { int n=3;\n" +
			"         //@ assert n==3;\n" +
			"       }\n" +
			"       { boolean n=true;\n" +
			"         //@ assert n;\n" +
			"       }\n" +
			"   }\n" +
			"}\n"
			,
			// expected boogie
			"procedure tests.esc.A.m5(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
			"	var a: int;\n" +
			"	var b: bool;\n" +
			"	a := 3;\n" +
			"	assert (a == 3);\n" +
			"	b := true;\n" +
			"	assert b;\n" +
			"}\n",
			// adapter output
			""
			);
	}
	
	// term=LocalDeclaration,UnaryExpression, adapter=pass
	public void test_297_LocalDeclaration() {	
	this.compareJavaToBoogie(
			// java
			"package tests.esc;\n" +
			"public class A {\n" +
			"   public void m1() {\n" + 
			"      boolean b = true;\n" + 
			"      //@ assert b;\n" + 
			"   }\n" + 
			"   public void m2() {\n" +
			"      boolean b = true;\n" + 
			"      //@ assert !b;\n" +
			"   }\n" +
			"   public void m3() {\n" +
			"      int n=3;\n" +
			"      //@ assert n<4;\n" +
			"   }\n" +
			"   public void m4() {\n" +
			"      int n=3;\n" +
			"      //@ assert n<0;\n" +
			"   }\n" +
			"}\n",		
			// expected boogie
			"procedure tests.esc.A.m1(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
			"	var a: bool;\n" +
			"	a := true;\n" +
			"	assert a;\n" +
			"}\n" +
			"procedure tests.esc.A.m2(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
			"	var a: bool;\n" +
			"	a := true;\n" +
			"	assert !a;\n" +
			"}\n" +
			"procedure tests.esc.A.m3(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
			"	var a: int;\n" +
			"	a := 3;\n" +
			"	assert (a < 4);\n" +
			"}\n" +
			"procedure tests.esc.A.m4(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
			"	var a: int;\n" +
			"	a := 3;\n" +
			"	assert (a < 0);\n" +
			"}\n"
			,
			// adapter output
			"----------\n" +
			"1. ERROR in " + testsPath + "A.java (at line 9)\n" +
			"	//@ assert !b;\n" +
			"	           ^^\n" +
			"This assertion might not hold.\n" +
			"----------\n" +
			"2. ERROR in " + testsPath + "A.java (at line 17)\n" +
			"	//@ assert n<0;\n" +
			"	           ^^^\n" +
			"This assertion might not hold.\n" +
			"----------\n");
	}
	
	// term=LocalDeclaration adapter=pass
	public void test_0297_LocalDeclaration() {
		compareJavaToBoogie(
			// java source
			"package tests.esc;\n" + 
			"public class A {\n" +
			"   public void m() {\n" +
			"	   int z;\n" + 
			"      boolean b;\n" + 
			"   }\n" + 
			"}\n"
			, 
			// expected boogie
			"procedure tests.esc.A.m(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
			"	var a: int;\n" +
			"	var b: bool;\n" +
			"	a := 0;\n" +
			"	b := false;\n" +
			"}\n",
			// adapter output
			"");
	}

	// term=LocalDeclaration adapter=pass
	public void test_0298_LocalDeclarationDuplicateSymbol() {
		compareJavaToBoogie(
			// java source
			"package tests.esc;\n" + 
			"public class A {\n" +
			"   public void m() {\n" +
			"	   for (int i = 0; i < 1; i++);\n" + 
			"      for (int i = 0; i < 1; i++);\n" + 
			"   }\n" + 
			"}\n"
			, 
			// expected boogie
			"procedure tests.esc.A.m(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
			"	var a: int;\n" +
			"	var b: int;\n" + // FIXME this is a side effect of RemoveLocal
			"	a := 0;\n" +
			"	while (a < 1) {\n" +
			"		a := (a + 1);\n" +
			"	}\n" +
			"	a := 0;\n" +
			"	while (a < 1) {\n" +
			"		a := (a + 1);\n" +
			"	}\n" +
			"}\n",
			// adapter output
			"");
	}
	
	// term=LocalDeclaration,Assignment adapter=pass
	public void test_0299_LocalDeclarationWithInitialization() {
		compareJavaToBoogie(
			// java source
			"package tests.esc;\n" + 
			"public class A {\n" +
			"   public void m() {\n" +
			"		int z = 3;\n" + 
			"      	boolean b = true;\n" + 			
			"   }\n" + 
			"}\n"
			, 
			// expected boogie
			"procedure tests.esc.A.m(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
			"	var a: int;\n" +
			"	var b: bool;\n" +
			"	a := 3;\n" +
			"	b := true;\n" +
			"}\n",
			// adapter output
			"");
	}

	// term=IfStatement,Argument,ReturnStatement,StringLiteral,Block,EqualExpression,LocalDeclaration adapter=pass
	public void test_0300_IfCondition() {
		compareJavaToBoogie(
			// java source
			"package tests.esc;\n" + 
			"public class A {\n" +
			"   public String m(int x, int y) {\n" +
			"		int z = 3;\n" + 
			"   	if (x == 1) {\n" +
			"			return \"a\";\n" +	
			"		}\n" + 
			"		else {\n" +
			"			return \"b\";\n" +
			"		}\n" +
			"   }\n" + 
			"}\n"
			, 
			// expected boogie
			"const unique $string_lit_0: $Ref;\n" +
			"const unique $string_lit_1: $Ref;\n" +
			"axiom $dtype($string_lit_0) == java.lang.String;\n" +
			"axiom $dtype($string_lit_1) == java.lang.String;\n" +
			"procedure tests.esc.A.m(this: $Ref, a: int, b: int) returns ($r: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
			"	var c: int;\n" +
			"	c := 3;\n" +
			"	if (a == 1) {\n" +
			"		$r := $string_lit_0;\n" +
			"		return;\n" +
			"	}\n" +
			"	else {\n" +
			"		$r := $string_lit_1;\n" +
			"		return;\n" +
			"	}\n" +
			"}\n",
			// adapter output
			"");
	}
	
	// term=IfStatement adapter=pass
	public void test_0301_IfCondition_noBlock() {		 
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m1() {\n" +
				"		if (true) \n" +
				"      		//@ assert (true);\n" +
				"   }\n" +
				"}\n" 
				,
				//expected boogie
				"procedure tests.esc.A.m1(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	if (true) {\n" +
				"		assert true;\n" +
				"	}\n" +
				"}\n",
				// adapter output
				"" );
	}
	
	// term=ConditionalExpression adapter=pass
	public void test_0302_IfCondition_ternary() {
		this.compareJavaToBoogie(
				"package tests.esc;\n" +
				"public class A {\n" + 
				"	public void m() {" +
				"		int a = 0;\n" + 
				"		assert (a < 10 ? a == 0 : false);\n" +
				"	}\n" + 
				"}\n"
				,
				// expected boogie
				"procedure tests.esc.A.m(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: int;\n" +
				"	var b: bool;\n" +
				"	a := 0;\n" +
				"	if (a < 10) {\n" +
				"		b := (a == 0);\n" +
				"	}\n" +
				"	else {\n" +
				"		b := false;\n" +
				"	}\n" +
				"	assert b;\n" +
				"}\n",
				// adapter output
				""
				);
	}

	//term=EmptyStatement,DoStatement,WhileStatement,TrueLiteral,Block adapter=pass
	public void test_0310_EmptyStatement() {
		compareJavaToBoogie(
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m1() {\n" +			
				"		;\n" +
				"   }\n" +	
				"   public void m2() {\n" +
				"		if (true)\n" +
				"			;\n" +
				"   }\n" +
				"   public void m3() {\n" +
				"		if (true) {\n" +
				"			;\n" +
				"		}\n" +
				"   }\n" +
				"   public void m4() {\n" +
				"		while (true) {\n" +
				"			;\n" +
				"		}\n" +
				"   }\n" +
				"   public void m5() {\n" +
				"		do{\n" +
				"			;\n" +
				"		}while(true);\n" +					
				"   }\n" +						
				"}" 
				,
				// expected boogie
				"procedure tests.esc.A.m1(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"}\n" +
				"procedure tests.esc.A.m2(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	if (true) {\n" +
				"	}\n" +
				"}\n" +
				"procedure tests.esc.A.m3(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	if (true) {\n" +
				"	}\n" +
				"}\n" +
				"procedure tests.esc.A.m4(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	while (true) {\n" +
				"	}\n" +
				"}\n" +
				"procedure tests.esc.A.m5(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	while (true) {\n" +
				"	}\n" +
				"}\n",
				// adapter output
				""
				);
	}
	
	//term=UnaryExpression adapter=pass
	public void test_0320_UnaryExpression() {
		compareJavaToBoogie(
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m() {\n" +
				"      boolean b = true;\n" + 
				"      //@ assert !b;\n" +
				"      //@ assert !!b;\n" +
				"      //@ assert !!!b;\n" +				
				"   }\n" +		
				"}" 
				,
				// expected boogie
				"procedure tests.esc.A.m(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: bool;\n" +
				"	a := true;\n" +
				"	assert !a;\n" +
				"	assert !!a;\n" +
				"	assert !!!a;\n" +
				"}\n",
				// adapter output
				"----------\n" +
				"1. ERROR in " + testsPath + "A.java (at line 5)\n" +
				"	//@ assert !b;\n" +
				"	           ^^\n" +
				"This assertion might not hold.\n" +
				"----------\n");
	}
	
	//term=UnaryExpression adapter=pass
	public void test_0321_UnaryExpression() {
		compareJavaToBoogie(
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m() {\n" +
				"	int number = +1;" +
				"	assert number == 1;\n" +				
				"   }\n" +		
				"}" 
				,
				// expected boogie
				"procedure tests.esc.A.m(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: int;\n" +
				"	a := 1;\n" +
				"	assert (a == 1);\n" +
				"}\n",
				// adapter output
				""
		);
	}	
	
	// term=WhileStatement,Block,EqualExpression adapter=pass
	public void test_0350_while() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m1() {\n" +
				"      while (true == true) {" +
				"         //@ assert true;\n" +
				"      }\n" + 
				"   }\n" +	
				"   public void m2() {\n" +
				"      while (true == true) " +
				"         //@ assert true;\n" +				
				"   }\n" + 
				"}\n" 
				,
				//expected boogie
				"procedure tests.esc.A.m1(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	while (true == true) {\n" +
				"		assert true;\n" +
				"	}\n" +
				"}\n" +
				"procedure tests.esc.A.m2(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	while (true == true) {\n" +
				"		assert true;\n" +
				"	}\n" +
				"}\n",
				// adapter output
				"" 				
				);
	}	

	// term=WhileStatement,BreakStatement,LabeledStatement,Block adapter=pass
	public void test_0370_while_break_withlabel() {		 
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m1() {\n" +
				"		blah:\n" +
				"		while(true){\n" +
				"      		//@ assert (true);\n" +
				"			break blah;\n" +	
				"		}\n" +	
				"		if (true) \n" +
				"      		//@ assert (true);\n" +

				"   }\n" +
				"}\n" 
				,
				//expected boogie
				"procedure tests.esc.A.m1(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	blah:\n" +
				"	while (true) {\n" +
				"		assert true;\n" +
				"		break blah;\n" +
				"	}\n" +
				"	if (true) {\n" +
				"		assert true;\n" +
				"	}\n" +
				"}\n",
				// adapter output
				"");
	}

	// term=WhileStatement,BreakStatement,Block adapter=pass
	public void test_0371_while_break() {		 
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m1() {\n" +
				"		while(true){\n" +
				"      		//@ assert (true);\n" +
				"			break;\n" +	
				"		}\n" +
				"	}\n" +	
				"}\n" 
				,
				//expected boogie
				"procedure tests.esc.A.m1(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	while (true) {\n" +
				"		assert true;\n" +
				"		break;\n" +
				"	}\n" +
				"}\n",
				// adapter output
				"");
	}
	
	// term=JmlWhileStatement,JmlLoopInvariant adapter=pass
    public void test_0372_while_invariant_true() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
                "public class A {\n"+
                "   public void m(boolean b) { \n" +
                "   	//@ loop_invariant true;\n" +
                "   	while (b) {\n" +
                "		} \n" +
                "	}\n" +
                "}\n"
                ,
               //expected boogie
                "procedure tests.esc.A.m(this: $Ref, a: bool) requires $dtype(this) == tests.esc.A; {\n" +
        		"	while (a) invariant true; {\n" +
        		"	}\n" +
        		"}\n",
				// adapter output
				""		
                );
		
    }
    
    //term=JmlWhileStatement,JmlLoopInvariant adapter=pass
    public void test_0373_while_invariant_expr() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
                "public class A {\n"+
                "   public void m(boolean b) { \n" +
                "		int n = 5;\n" +
                "   	//@ loop_invariant n == 5;\n" +
                "   	while (b) {\n" +
                "		} \n" +
                "	}\n" +
                "}\n"
                ,
               //expected boogie
                "procedure tests.esc.A.m(this: $Ref, a: bool) requires $dtype(this) == tests.esc.A; {\n" +
				"	var b: int;\n" +
				"	b := 5;\n" +
				"	while (a) invariant (b == 5); {\n" +
				"	}\n" +
				"}\n",
				// adapter output
				""
                );
    }  

    //term=JmlWhileStatement,JmlLoopInvariant adapter=pass
    public void test_0374_while_invariant_break() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
                "public class A {\n"+
                "   public void m(boolean b) { \n" +
                "   	//@ loop_invariant true;\n" +
                "   	here: while (b) { break here;} \n" +
                "	}\n" +
                "}\n"
                ,
               //expected boogie
                "procedure tests.esc.A.m(this: $Ref, a: bool) requires $dtype(this) == tests.esc.A; {\n" +
				"	here:\n" +
				"	while (a) invariant true; {\n" +
				"		break here;\n" +
				"	}\n" +
				"}\n",
				// adapter output
				""
				);
    }     

	// term=DoStatement,Block adapter=pass
	public void test_400_do() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m1() {\n" +
				"		do{\n" +
				"      		//@ assert (true);\n" +
				"		}while(true);\n" +	
				"	}\n" +	
				"   public void m2() {\n" +
				"		do\n" +
				"      		//@ assert (true);\n" +
				"		while(true);\n" +	
				"	}\n" +				
				"}\n" 
				,
				//expected boogie
				"procedure tests.esc.A.m1(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert true;\n" +
				"	while (true) {\n" +
				"		assert true;\n" +
				"	}\n" +
				"}\n" +
				"procedure tests.esc.A.m2(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert true;\n" +
				"	while (true) {\n" +
				"		assert true;\n" +
				"	}\n" +
				"}\n",
				// adapter output
				""

				);
	}
	
	// term=DoStatement,Block adapter=pass
	public void test_401_do_multiline() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m1() {\n" +
				"		do{\n" +
				"      		//@ assert (true);\n" +
				"      		//@ assert (false);\n" +
				"      		//@ assert (true);\n" +
				"		}while(true);\n" +	
				"	}\n" +	
				"}\n" 
				,
				//expected boogie
				"procedure tests.esc.A.m1(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert true;\n" +
				"	assert false;\n" +
				"	assert true;\n" +
				"	while (true) {\n" +
				"		assert true;\n" +
				"		assert false;\n" +
				"		assert true;\n" +				
				"	}\n" +
				"}\n",
				// adapter output
				"----------\n" +
				"1. ERROR in " + testsPath + "A.java (at line 6)\n" +
				"	//@ assert (false);\n" +
				"	           ^^^^^^^\n" +
				"This assertion might not hold.\n" +
				"----------\n"
				);
	}
	
	// term=JmlDoStatement adapter=pass
	public void test_402_do_invariant() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
			    "   public void m1(boolean b) { \n" +
			    "   	//@ loop_invariant true;\n" +
			    "   	do {} while (b); \n" +
			    "	}\n" +
			    "   public void m2(boolean b) { \n" +
			    "   	//@ loop_invariant true;\n" +
				"		do{\n" +
				"      		//@ assert (true);\n" +
				"      		//@ assert (false);\n" +
				"      		//@ assert (true);\n" +
				"		}while(true);\n" +
			    "	}\n" +			    
			    "}\n"
			    ,
			    "procedure tests.esc.A.m1(this: $Ref, a: bool) requires $dtype(this) == tests.esc.A; {\n" +
				"	while (a) invariant true; {\n" +
				"	}\n" +
				"}\n" +
				"procedure tests.esc.A.m2(this: $Ref, a: bool) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert true;\n" +
				"	assert false;\n" +
				"	assert true;\n" +
				"	while (true) invariant true; {\n" +
				"		assert true;\n" +
				"		assert false;\n" +
				"		assert true;\n" +
				"	}\n" +
				"}\n",
				// adapter output
				"----------\n" +
				"1. ERROR in A.java (at line 11)\n" +
				"	//@ assert (false);\n" +
				"	           ^^^^^^^\n" +
				"This assertion might not hold.\n" +
				"----------\n"
			    );
	}
	
	// term=ForStatement,Block,BinaryExpression adapter=pass
	public void test_500_for() {		
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m1() {\n" +
				"		for (int x = 0; x<10 ; x++) {\n" +
				"			//@assert (true);\n" +			
				" 		}\n" +	
				"	}\n" +	
				"   public void m2() {\n" +
				"		for (int x = 10; x>0 ; x--) \n" +
				"			//@assert (true);\n" +			
				" 		\n" +	
				"	}\n" +					
				"}\n" 
				,
				//expected boogie
				"procedure tests.esc.A.m1(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: int;\n" +
				"	a := 0;\n" +
				"	while (a < 10) {\n" +
				"		assert true;\n" +
				"		a := (a + 1);\n" +
				"	}\n" +
				"}\n" +
				"procedure tests.esc.A.m2(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: int;\n" +
				"	a := 10;\n" +
				"	while (a > 0) {\n" +
				"		assert true;\n" +
				"		a := (a - 1);\n" +
				"	}\n" +
				"}\n",
				// adapter output
				""
				);
	}
	
	// term=Assignment,ForStatement,Block,BinaryExpression adapter=pass
	public void test_501_for_multi_initialization() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m1() {\n" +
				"		//@assert (true);\n" +
				"		for (int i=1,j=10; i<j; i++,j++) {\n" +
				"		//@assert (true);\n" +			
				" 		}\n" +	
				"	}\n" +					
				"}\n" 
				,
				//expected boogie
				"procedure tests.esc.A.m1(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: int;\n" +
				"	var b: int;\n" +
				"	assert true;\n" +
				"	a := 1;\n" +
				"	b := 10;\n" +
				"	while (a < b) {\n" +
				"		assert true;\n" +
				"		a := (a + 1);\n" +
				"		b := (b + 1);\n" +
				"	}\n" +
				"}\n",
				// adapter output
				""
				);
	}	
	
	//term=JmlForStatement adapter=pass
	public void test_503_for_invariant() {
		this.compareJavaToBoogie(
				//java	
				"package tests.esc;\n" +
			    "public class A {\n"+
			    "   public void m(boolean b) { \n" +
			    "   	//@ loop_invariant true;\n" +
			    "   	for (int i=0; i<10; i++) {} \n" +
			    "	}\n" +
			    "}\n"
			    ,
			    "procedure tests.esc.A.m(this: $Ref, a: bool) requires $dtype(this) == tests.esc.A; {\n" +
				"	var b: int;\n" +
				"	b := 0;\n" +
				"	while (b < 10) invariant true; {\n" +
				"		b := (b + 1);\n" +
				"	}\n" +
				"}\n",
				// adapter output
				""
			    );
	}
    
	// term=Assignment,PostfixExpression,LocalDeclaration adapter=pass
	public void test_600_postFixExpression() {
		
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m1() {\n" +
				"		int i = 0;\n" +
				"		i++;\n" +
				"		i ++;\n" +
				"		int y = 0;\n" +
				"		y--;\n" +
				"		y --;\n" +
				"	}\n" +					
				"}\n" 
				,
				//expected boogie		
				"procedure tests.esc.A.m1(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: int;\n" +
				"	var b: int;\n" +
				"	a := 0;\n" +
				"	a := (a + 1);\n" +
				"	a := (a + 1);\n" +
				"	b := 0;\n" +
				"	b := (b - 1);\n" +
				"	b := (b - 1);\n" +
				"}\n",
				// adapter output
				""			
				);
	}
	
	// term=Assignment,PrefixExpression,LocalDeclaration adapter=pass
	public void test_601_preFixExpression() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m1() {\n" +
				"		int i = 0;\n" +
				"		++ i;\n" +
				"		++i;\n" +				
				"		int y = 0;\n" +
				"		-- y;\n" +
				"		--y;\n" +				
				"	}\n" +					
				"}\n" 
				,
				//expected boogie			
				"procedure tests.esc.A.m1(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: int;\n" +
				"	var b: int;\n" +
				"	a := 0;\n" +
				"	a := (a + 1);\n" +
				"	a := (a + 1);\n" +
				"	b := 0;\n" +
				"	b := (b - 1);\n" +
				"	b := (b - 1);\n" +
				"}\n",
				// adapter output
				""			
				);
	}	
	
	// term=PrefixExpression,PostFixExpression adapter=pass adapter=pass
	public void test_602_pre_post_FixExpression() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m1() {\n" +
				"		int i = 5;\n" +
				"		int x = 0;" +
				"		x = i ++;\n" +
				"		//@ assert i == 6;\n" +
				"		//@ assert x == 5;\n" +
				"	}\n" +					
				"}\n" 
				,
				// expected boogie			
				"procedure tests.esc.A.m1(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: int;\n" +
				"	var b: int;\n" +
				"	a := 5;\n" +
				"	b := 0;\n" +
				"	b := a;\n" +
				"	a := (a + 1);\n" +
				"	assert (a == 6);\n" +
				"	assert (b == 5);\n" +
				"}\n"
				,
				// adapter output
				""
				);
	}
	
	// term=PrefixExpression,PostFixExpression adapter=pass adapter=pass
	public void test_603_post_pre_FixExpression() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m1() {\n" +
				"		int i = 5;\n" +
				"		int x = 0;" +
				"		x = ++ i;\n" +
				"		//@ assert i == 6;\n" +
				"		//@ assert x == 6;\n" +
				"	}\n" +					
				"}\n" 
				,
				// expected boogie			
				"procedure tests.esc.A.m1(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: int;\n" +
				"	var b: int;\n" +
				"	a := 5;\n" +
				"	b := 0;\n" +
				"	a := (a + 1);\n" +
				"	b := a;\n" +
				"	assert (a == 6);\n" +
				"	assert (b == 6);\n" +
				"}\n"		
				,
				// adapter output
				""
				);
	}

	// term=Assignment adapter=pass adapter=pass
	public void test_604_multiAssignment() {

		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m1() {" +
				"		int a = 1;\n" +
				"		int b = 2;\n" +
				"		int c = b = a = 3;\n" +
				"		//@ assert a == 3;\n" +
				"		//@ assert b == 3;\n" +
				"		//@ assert c == 3;\n" +
				"	}\n" +					
				"}\n" 
				,
				//expected boogie
				"procedure tests.esc.A.m1(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: int;\n" +
				"	var b: int;\n" +
				"	var c: int;\n" +
				"	a := 1;\n" +
				"	b := 2;\n" +
				"	a := 3;\n" +
				"	b := a;\n" +
				"	c := b;\n" +
				"	assert (a == 3);\n" +
				"	assert (b == 3);\n" +
				"	assert (c == 3);\n" +
				"}\n"
				,
				// adapter output
				""
				);
	}	

	// term=Assignment,SingleTypeReference,IntLiteral,JmlLocalDeclaration,LocalDeclaration adapter=pass
	public void test_700_localVarDecl_order() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" +
				"   public void m1() {\n" +
				"		int x = 2;\n" +
				"       int y = 1;\n" +
				"   }\n" + 				
				"}\n"
				,				
				// expected boogie
				"procedure tests.esc.A.m1(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: int;\n" +
				"	var b: int;\n" +
				"	a := 2;\n" +
				"	b := 1;\n" +
				"}\n",
				// adapter output
				""
				);
	}
	
	// term=FieldDeclaration,SingleNameReference,Assignment adapter=pass
	public void test_800_FieldDeclaration() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" +
				" 	int i;\n" +
				" 	boolean b;\n" +
				"	public void m() {\n" +
				"		i = 1;\n" +
				"	}\n" +
				"}\n" 
				,
				// expected boogie
				"var tests.esc.A.i: [$Ref] int;\n" +
				"var tests.esc.A.b: [$Ref] bool;\n" +
				"procedure tests.esc.A.m(this: $Ref) modifies tests.esc.A.i; requires $dtype(this) == tests.esc.A; {\n" +
				"	tests.esc.A.i[this] := 1;\n" +
				"}\n",
				// adapter output
				""
				);
	}

	// term=FieldDeclaration,SingleNameReference,Assignment adapter=pass
	public void test_801_Static_FieldDeclaration() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" +
				" 	int i = 1;\n" +
				"	int x = i;\n" +
				"	int z;\n" +
				"	public A() { }" +
				"}\n" 
				,
				// expected boogie
				"var tests.esc.A.i: [$Ref] int;\n" +				
				"var tests.esc.A.x: [$Ref] int;\n" +
				"var tests.esc.A.z: [$Ref] int;\n" +
				"procedure tests.esc.A.$initFields(this: $Ref) modifies tests.esc.A.i; modifies tests.esc.A.x; requires $dtype(this) == tests.esc.A; ensures (tests.esc.A.i[this] == 1); ensures (tests.esc.A.x[this] == tests.esc.A.i[this]); {\n" +
				"	tests.esc.A.i[this] := 1;\n" +
				"	tests.esc.A.x[this] := tests.esc.A.i[this];\n" +
				"}\n" +
				"procedure tests.esc.A.A(this: $Ref) modifies tests.esc.A.i; modifies tests.esc.A.x; requires $dtype(this) == tests.esc.A; ensures (tests.esc.A.i[this] == 1); ensures (tests.esc.A.x[this] == tests.esc.A.i[this]); {\n" +
				"	call tests.esc.A.$initFields(this);\n" +
				"}\n",
				// adapter output
				""
				);
	}
	
	// term=FieldDeclaration,JmlEnsuresClause,JmlMethodSpecification adapter=pass
	public void test_802_Static_FieldDeclaration() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" +
				" 	int i = 1;\n" +
				"	public A() { }\n" +
				"	public static void x() {\n" +
				"		A a = new A();" +
				"		//@ assert a.i == 1;\n" +
				"	}\n" +
				"}\n" 
				,
				// expected boogie
				"var tests.esc.A.i: [$Ref] int;\n" +				
				"procedure tests.esc.A.$initFields(this: $Ref) modifies tests.esc.A.i; requires $dtype(this) == tests.esc.A; ensures (tests.esc.A.i[this] == 1); {\n" +
				"	tests.esc.A.i[this] := 1;\n" +
				"}\n" +
				"procedure tests.esc.A.A(this: $Ref) modifies tests.esc.A.i; requires $dtype(this) == tests.esc.A; ensures (tests.esc.A.i[this] == 1); {\n" +
				"	call tests.esc.A.$initFields(this);\n" +
				"}\n" +
				"procedure tests.esc.A.x() modifies tests.esc.A.i; {\n" +
				"	var a: $Ref;\n" +
				"	assume $dtype(a) == tests.esc.A;\n" +
				"	call tests.esc.A.A(a);\n" +
				"	assert (tests.esc.A.i[a] == 1);\n" +
				"}\n",
				// adapter output
				""
				);
	}

	// term=JmlMethodSpecification,JmlEnsuresClause,JmlResultExpression adapter=pass adapter=pass
	public void test_900_JmlResultExpression() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" +
				"	//@ ensures \\result == 3;\n" +
				"	int m() { return 3; }\n" +
				"}\n" 
				,
				// expected boogie
				"procedure tests.esc.A.m(this: $Ref) returns ($r: int) requires $dtype(this) == tests.esc.A; ensures ($r == 3); {\n" +
				"	$r := 3;\n" +
				"	return;\n" +
				"}\n"
				,
				// adapter output
				""
				);
	}	
	
	// term=JmlMethodSpecification,JmlEnsuresClause,JmlResultExpression adapter=pass adapter=pass
	public void test_901_JmlResultExpression() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" +
				"	//@ ensures \\result == 4;\n" +
				"	int m() { return 3; }\n" +
				"}\n" 
				,
				// expected boogie
				"procedure tests.esc.A.m(this: $Ref) returns ($r: int) requires $dtype(this) == tests.esc.A; ensures ($r == 4); {\n" +
				"	$r := 3;\n" +
				"	return;\n" +
				"}\n"
				,
				// adapter output
				"----------\n" +
				"1. ERROR in A.java (at line 3)\n" +
				"	//@ ensures \\result == 4;\n" +
				"	            ^^^^^^^^^^^^\n" +
				"This postcondition might not hold.\n" +
				"----------\n" +
				"2. ERROR in A.java (at line 4)\n" +
				"	int m() { return 3; }\n" +
				"	                 ^\n" +
				"This postcondition might not hold.\n" +
				"----------\n"
				);
	}	

	// term=JmlMethodSpecification,JmlEnsuresClause,JmlOldExpression,JmlResultExpression adapter=pass adapter=pass
	public void test_910_JmlOldExpression() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" +
				"	int x;" +
				"	//@ assignable x;\n" +
				"	//@ ensures \\old(x) == x - 1;\n" +
				"	int m() { return x++; }\n" +
				"}\n" 
				,
				// expected boogie
				"var tests.esc.A.x: [$Ref] int;\n" +
				"procedure tests.esc.A.m(this: $Ref) returns ($r: int) modifies tests.esc.A.x; requires $dtype(this) == tests.esc.A; ensures (old(tests.esc.A.x[this]) == (tests.esc.A.x[this] - 1)); {\n" +
				"	$r := tests.esc.A.x[this];\n" +
				"	tests.esc.A.x[this] := (tests.esc.A.x[this] + 1);\n" +
				"	return;\n" +
				"}\n"
				,
				// adapter output
				""
				);
	}		
	
	// term=JmlEnsuresClause,JmlMethodSpecification,JmlOldExpression,JmlResultExpression adapter=pass adapter=pass
	public void test_911_JmlOldExpression() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" +
				"	int x;\n" +
				"	//@ assignable x;\n" +
				"	//@ ensures \\old(x) == x + 10;\n" +
				"	int m() { return x++; }\n" +
				"}\n" 
				,
				// expected boogie
				"var tests.esc.A.x: [$Ref] int;\n" +
				"procedure tests.esc.A.m(this: $Ref) returns ($r: int) modifies tests.esc.A.x; requires $dtype(this) == tests.esc.A; ensures (old(tests.esc.A.x[this]) == (tests.esc.A.x[this] + 10)); {\n" +
				"	$r := tests.esc.A.x[this];\n" +
				"	tests.esc.A.x[this] := (tests.esc.A.x[this] + 1);\n" +
				"	return;\n" +
				"}\n"
				,
				// adapter output
				"----------\n" +
				"1. ERROR in A.java (at line 5)\n" +
				"	//@ ensures \\old(x) == x + 10;\n" +
				"	            ^^^^^^^^^^^^^^^^^\n" +
				"This postcondition might not hold.\n" +
				"----------\n" +
				"2. ERROR in A.java (at line 6)\n" +
				"	int m() { return x++; }\n" +
				"	                 ^^^\n" +
				"This postcondition might not hold.\n" +
				"----------\n"
				);
	}		

	// term=LocalDeclaration,SingleTypeReference,Assignment,IntLiteral adapter=pass
	public void test_1000_int_localdeclaration() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +	
				"public class A {\n" + 
				"	public void m() {\n" +
				"		int i = 0;\n" +
				"		int x;\n" +
				"		int a,s,d;\n" +
				"		int q,w,\ne = 0;\n" +
				"		int y =10, u=20 ,o= 30;\n" +
				"	}\n" +
				"}\n" 
				,
				//expected boogie
				"procedure tests.esc.A.m(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: int;\n" +
				"	var b: int;\n" +
				"	var c: int;\n" +
				"	var d: int;\n" +
				"	var e: int;\n" +
				"	var f: int;\n" +
				"	var g: int;\n" +
				"	var h: int;\n" +
				"	var i: int;\n" +
				"	var j: int;\n" +
				"	var k: int;\n" +
				"	a := 0;\n" +
				"	b := 0;\n" +
				"	c := 0;\n" +
				"	d := 0;\n" +
				"	e := 0;\n" +
				"	f := 0;\n" +
				"	g := 0;\n" +
				"	h := 0;\n" +
				"	i := 10;\n" +
				"	j := 20;\n" +
				"	k := 30;\n" +
				"}\n",
				// adapter output
				""
				);
	}

	
	// term=IntLiteral,EqualExpression adapter=pass
	public void test_1000_int_eq() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m1() {\n" + 
				"      //@ assert 42 == 42;\n" + 
				"   }\n" + 
				"   public void m2() {\n" + 
				"      //@ assert 42 == 43;\n" + 
				"   }\n" + 
				"   public void m3() {\n" + 
				"      //@ assert 42 != 42;\n" + 
				"   }\n" + 
				"   public void m4() {\n" + 
				"      //@ assert 42 != 43;\n" + 
				"   }\n" + 
				"   public void m5() {\n" + 
				"      //@ assert 42 < 42;\n" + 
				"   }\n" + 
				"   public void m6() {\n" + 
				"      //@ assert 42 < 43;\n" + 
				"   }\n" + 
				"   public void m7() {\n" + 
				"      //@ assert 42 > 42;\n" + 
				"   }\n" + 
				"   public void m8() {\n" + 
				"      //@ assert 42 > 43;\n" + 
				"   }\n" + 
				"   public void m9() {\n" + 
				"      //@ assert 43 <= 42;\n" + 
				"   }\n" + 
				"   public void m10() {\n" + 
				"      //@ assert 42 <= 42;\n" + 
				"   }\n" + 
				"   public void m11() {\n" + 
				"      //@ assert 42 <= 43;\n" + 
				"   }\n" + 
				"   public void m12() {\n" + 
				"      //@ assert 42 >= 43;\n" + 
				"   }\n" + 
				"   public void m13() {\n" + 
				"      //@ assert 42 >= 42;\n" + 
				"   }\n" + 
				"   public void m14() {\n" + 
				"      //@ assert 43 >= 42;\n" + 
				"   }\n" + 
				"   public void m15() {\n" + 
				"      //@ assert (42 >= 42) == true;\n" + 
				"   }\n" + 
				"   public void m16() {\n" + 
				"      //@ assert (42 >= 42) == false;\n" + 
				"   }\n" + 
				"   public void m17() {\n" + 
				"      //@ assert (43 >= 42) == true;\n" + 
				"   }\n" + 
				"   public void m18() {\n" + 
				"      //@ assert (43 >= 42) == false;\n" + 
				"   }\n" + 
				"}\n" 
				,
				//expected Boogie
				"procedure tests.esc.A.m1(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert (42 == 42);\n" +
				"}\n" +
				"procedure tests.esc.A.m2(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert (42 == 43);\n" +
				"}\n" +
				"procedure tests.esc.A.m3(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert (42 != 42);\n" +
				"}\n" +
				"procedure tests.esc.A.m4(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert (42 != 43);\n" +
				"}\n" +
				"procedure tests.esc.A.m5(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert (42 < 42);\n" +
				"}\n" +
				"procedure tests.esc.A.m6(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert (42 < 43);\n" +
				"}\n" +
				"procedure tests.esc.A.m7(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert (42 > 42);\n" +
				"}\n" +
				"procedure tests.esc.A.m8(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert (42 > 43);\n" +
				"}\n" +
				"procedure tests.esc.A.m9(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert (43 <= 42);\n" +
				"}\n" +
				"procedure tests.esc.A.m10(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert (42 <= 42);\n" +
				"}\n" +
				"procedure tests.esc.A.m11(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert (42 <= 43);\n" +
				"}\n" +
				"procedure tests.esc.A.m12(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert (42 >= 43);\n" +
				"}\n" +
				"procedure tests.esc.A.m13(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert (42 >= 42);\n" +
				"}\n" +
				"procedure tests.esc.A.m14(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert (43 >= 42);\n" +
				"}\n" +
				"procedure tests.esc.A.m15(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert ((42 >= 42) == true);\n" +
				"}\n" +
				"procedure tests.esc.A.m16(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert ((42 >= 42) == false);\n" +
				"}\n" +
				"procedure tests.esc.A.m17(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert ((43 >= 42) == true);\n" +
				"}\n" +
				"procedure tests.esc.A.m18(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert ((43 >= 42) == false);\n" +
				"}\n",
				// adapter output
				"----------\n" +
				"1. ERROR in A.java (at line 7)\n" +
				"	//@ assert 42 == 43;\n" +
				"	           ^^^^^^^^\n" +
				"This assertion might not hold.\n" +
				"----------\n" +
				"2. ERROR in A.java (at line 10)\n" +
				"	//@ assert 42 != 42;\n" +
				"	           ^^^^^^^^\n" +
				"This assertion might not hold.\n" +
				"----------\n" +
				"3. ERROR in A.java (at line 16)\n" +
				"	//@ assert 42 < 42;\n" +
				"	           ^^^^^^^\n" +
				"This assertion might not hold.\n" +
				"----------\n" +
				"4. ERROR in A.java (at line 22)\n" +
				"	//@ assert 42 > 42;\n" +
				"	           ^^^^^^^\n" +
				"This assertion might not hold.\n" +
				"----------\n" +
				"5. ERROR in A.java (at line 25)\n" +
				"	//@ assert 42 > 43;\n" +
				"	           ^^^^^^^\n" +
				"This assertion might not hold.\n" +
				"----------\n" +
				"6. ERROR in A.java (at line 28)\n" +
				"	//@ assert 43 <= 42;\n" +
				"	           ^^^^^^^^\n" +
				"This assertion might not hold.\n" +
				"----------\n" +
				"7. ERROR in A.java (at line 37)\n" +
				"	//@ assert 42 >= 43;\n" +
				"	           ^^^^^^^^\n" +
				"This assertion might not hold.\n" +
				"----------\n" +
				"8. ERROR in A.java (at line 49)\n" +
				"	//@ assert (42 >= 42) == false;\n" +
				"	           ^^^^^^^^^^^^^^^^^^^\n" +
				"This assertion might not hold.\n" +
				"----------\n" +
				"9. ERROR in A.java (at line 55)\n" +
				"	//@ assert (43 >= 42) == false;\n" +
				"	           ^^^^^^^^^^^^^^^^^^^\n" +
				"This assertion might not hold.\n" +
				"----------\n"
				);
	}
	
	// term=IntLiteral,BinaryExpression,EqualExpression
	public void test_1001_int_arith() {
		this.compareJavaToBoogie(	
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m1() {\n" + 
				"      //@ assert 5 + 2 == 7;\n" + 
				"   }\n" + 
				"   public void m2() {\n" + 
				"      //@ assert 5 - 2 == 3;\n" + 
				"   }\n" + 
				"   public void m3() {\n" + 
				"      //@ assert 5 * 2 == 10;\n" + 
				"   }\n" + 
				"   public void m4() {\n" + 
				"      //@ assert 4 / 2 == 2;\n" + 
				"   }\n" + 
				"   public void m5() {\n" + 
				"      //@ assert 5 % 2 == 1;\n" + 
				"   }\n" + 
				"   public void m6() {\n" + 
				"      //@ assert (5 + 2) * 3 == 21;\n" + 
				"   }\n" + 
				"   public void m1b() {\n" + 
				"      //@ assert 5 + 2 != 7;\n" + 
				"   }\n" + 
				"   public void m2b() {\n" + 
				"      //@ assert 5 - 2 != 3;\n" + 
				"   }\n" + 
				"   public void m3b() {\n" + 
				"      //@ assert 5 * 2 != 10;\n" + 
				"   }\n" + 
				"   public void m4b() {\n" + 
				"      //@ assert 4 / 2 != 2;\n" + 
				"   }\n" + 
				"   public void m5b() {\n" + 
				"      //@ assert 5 % 2 != 1;\n" + 
				"   }\n" +
				"   public void m6b() {\n" + 
				"      //@ assert (5 + 2) * 3 != 22;\n" + 
				"   }\n" + 
				"}\n"
				, 
				//expected boogie
				"procedure tests.esc.A.m1(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert ((5 + 2) == 7);\n" +
				"}\n" +
				"procedure tests.esc.A.m2(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert ((5 - 2) == 3);\n" +
				"}\n" + 
				"procedure tests.esc.A.m3(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert ((5 * 2) == 10);\n" +
				"}\n" +
				"procedure tests.esc.A.m4(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert ((4 / 2) == 2);\n" +
				"}\n" + 
				"procedure tests.esc.A.m5(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert ((5 % 2) == 1);\n" +
				"}\n" +
				"procedure tests.esc.A.m6(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert (((5 + 2) * 3) == 21);\n" +
				"}\n" +
				"procedure tests.esc.A.m1b(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert ((5 + 2) != 7);\n" +
				"}\n" +
				"procedure tests.esc.A.m2b(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert ((5 - 2) != 3);\n" +
				"}\n" +
				"procedure tests.esc.A.m3b(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert ((5 * 2) != 10);\n" +
				"}\n" +
				"procedure tests.esc.A.m4b(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert ((4 / 2) != 2);\n" +
				"}\n" +
				"procedure tests.esc.A.m5b(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert ((5 % 2) != 1);\n" +
				"}\n" +
				"procedure tests.esc.A.m6b(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert (((5 + 2) * 3) != 22);\n" +
				"}\n",
				// adapter output
				"----------\n" +
				"1. ERROR in A.java (at line 22)\n" +
				"	//@ assert 5 + 2 != 7;\n" +
				"	           ^^^^^^^^^^\n" +
				"This assertion might not hold.\n" +
				"----------\n" +
				"2. ERROR in A.java (at line 25)\n" +
				"	//@ assert 5 - 2 != 3;\n" +
				"	           ^^^^^^^^^^\n" +
				"This assertion might not hold.\n" +
				"----------\n" +
				"3. ERROR in A.java (at line 28)\n" +
				"	//@ assert 5 * 2 != 10;\n" +
				"	           ^^^^^^^^^^^\n" +
				"This assertion might not hold.\n" +
				"----------\n" +
				"4. ERROR in A.java (at line 31)\n" +
				"	//@ assert 4 / 2 != 2;\n" +
				"	           ^^^^^^^^^^\n" +
				"This assertion might not hold.\n" +
				"----------\n" +
				"5. ERROR in A.java (at line 34)\n" +
				"	//@ assert 5 % 2 != 1;\n" +
				"	           ^^^^^^^^^^\n" +
				"This assertion might not hold.\n" +
				"----------\n"
				);			
		}

	// term=IntLiteral,ConditionalExpression adapter=none
	public void test_1002_arith_cond() {
		this.compareJavaToBoogie(
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m1() {\n" + 
				"      //@ assert (5 == 3 + 2 ? 42 == 6 * 7 : 1 + 1 == 2);\n" + 
				"   }\n" + 
				"   public void m2() {\n" + 
				"      //@ assert (5 == 3 + 2 ? 42 > 6 * 7 : 1 + 1 == 2);\n" + 
				"   }\n" + 
				"   public void m3() {\n" + 
				"      //@ assert (5 == 3 + 3 ? 42 == 6 * 7 : 1 + 1 != 2);\n" + 
				"   }\n" + 
				"}\n"
				, 
				// expected boogie
				"procedure tests.esc.A.m1(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: bool;\n" +
				"	if (5 == (3 + 2)) {\n" +
				"		a := (42 == (6 * 7));\n" +
				"	}\n" +
				"	else {\n" +
				"		a := ((1 + 1) == 2);\n" +
				"	}\n" +
				"	assert a;\n" +
				"}\n" +
				"procedure tests.esc.A.m2(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: bool;\n" +
				"	if (5 == (3 + 2)) {\n" +
				"		a := (42 > (6 * 7));\n" +
				"	}\n" +
				"	else {\n" +
				"		a := ((1 + 1) == 2);\n" +
				"	}\n" +
				"	assert a;\n" +
				"}\n" +
				"procedure tests.esc.A.m3(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: bool;\n" +
				"	if (5 == (3 + 3)) {\n" +
				"		a := (42 == (6 * 7));\n" +
				"	}\n" +
				"	else {\n" +
				"		a := ((1 + 1) != 2);\n" +
				"	}\n" +
				"	assert a;\n" +
				"}\n",
				// adapter output
				"----------\n" +
				"1. ERROR in A.java (at line 7)\n" +
				"	//@ assert (5 == 3 + 2 ? 42 > 6 * 7 : 1 + 1 == 2);\n" +
				"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"This assertion might not hold.\n" +
				"----------\n" +
				"2. ERROR in A.java (at line 10)\n" +
				"	//@ assert (5 == 3 + 3 ? 42 == 6 * 7 : 1 + 1 != 2);\n" +
				"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"This assertion might not hold.\n" +
				"----------\n"
				);
	}
	
	// term=IntLiteral,ConditionalExpression adapter=pass
	public void test_1003_boolExpr_cond() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m1() {\n" + 
				"      //@ assert (!true ? false : !true);\n" + 
				"   }\n" + 
				"   public void m2() {\n" + 
				"      //@ assert (false && false ? true : false && false);\n" + 
				"   }\n" + 
				"   public void m3() {\n" + 
				"      //@ assert (false || false ? true : false || false);\n" + 
				"   }\n" + 
				"}\n" 
				, 
				// expected boogie
				"procedure tests.esc.A.m1(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: bool;\n" +
				"	if (!true) {\n" +
				"		a := false;\n" +
				"	}\n" +
				"	else {\n" +
				"		a := !true;\n" +
				"	}\n" +
				"	assert a;\n" +
				"}\n" +
				"procedure tests.esc.A.m2(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: bool;\n" +
				"	if (false && false) {\n" +
				"		a := true;\n" +
				"	}\n" +
				"	else {\n" +
				"		a := (false && false);\n" +
				"	}\n" +
				"	assert a;\n" +
				"}\n" +
				"procedure tests.esc.A.m3(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: bool;\n" +
				"	if (false || false) {\n" +
				"		a := true;\n" +
				"	}\n" +
				"	else {\n" +
				"		a := (false || false);\n" +
				"	}\n" +
				"	assert a;\n" +
				"}\n",
				// adapter output
				"----------\n" +
				"1. ERROR in A.java (at line 4)\n" +
				"	//@ assert (!true ? false : !true);\n" +
				"	           ^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"This assertion might not hold.\n" +
				"----------\n" +
				"2. ERROR in A.java (at line 7)\n" +
				"	//@ assert (false && false ? true : false && false);\n" +
				"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"This assertion might not hold.\n" +
				"----------\n" +
				"3. ERROR in A.java (at line 10)\n" +
				"	//@ assert (false || false ? true : false || false);\n" +
				"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"This assertion might not hold.\n" +
				"----------\n"
				);
	}
	
	// term=BinaryExpression adapter=pass
	public void test_1004_implies() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m1() {\n" +
				"      //@ assert (true ==> true);\n" + 
				"   }\n" + 
				"   public void m2() {\n" + 
				"      //@ assert (true ==> false);\n" + 
				"   }\n" + 
				"   public void m3() {\n" + 
				"      //@ assert (false ==> true);\n" + 
				"   }\n" + 
				"   public void m4() {\n" + 
				"      //@ assert (false ==> false);\n" + 
				"   }\n" + 
				"}\n" 
				,
				// expected boogie
				"procedure tests.esc.A.m1(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert (true ⇒ true);\n" +
				"}\n" +
				"procedure tests.esc.A.m2(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert (true ⇒ false);\n" +
				"}\n" +
				"procedure tests.esc.A.m3(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert (false ⇒ true);\n" +
				"}\n" +
				"procedure tests.esc.A.m4(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert (false ⇒ false);\n" +
				"}\n",
				// adapter output
				"----------\n" +
				"1. ERROR in A.java (at line 7)\n" +
				"	//@ assert (true ==> false);\n" +
				"	           ^^^^^^^^^^^^^^^^\n" +
				"This assertion might not hold.\n" +
				"----------\n"
				);
	}
	
	// term=IntLiteral adapter=pass
	public void test_1005_int_boundaries() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" + 
				"   public void m1() {\n" +
				"      int max = 2147483647;\n" +
				"	   //@ assert (max == 2147483647);" +
				"   }\n" +
				"	public void m2() {\n" +
				"		int min = -2147483648;\n" + 
				"	   //@ assert (min == -2147483648);" +
				"	}\n" +
				"}\n"
				,
				// expected boogie
				"procedure tests.esc.A.m1(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: int;\n" +
				"	a := 2147483647;\n" +
				"	assert (a == 2147483647);\n" +
				"}\n" +
				"procedure tests.esc.A.m2(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: int;\n" +
				"	a := -2147483648;\n" +
				"	assert (a == -2147483648);\n" +
				"}\n",
				// adapter output
				""
				);
	}
	
	// term=MessageSend adapter=pass 
	public void test_2000_messageSend() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" +
				"	public void m1() {\n" +
				"		int c = m2();\n" +
				"		//@ assert c == 4;\n" +
				"	}\n" +
				"	public int m2() {\n" +
				"		return 4;\n" +
				"	}\n" +
				"}\n"
				,
				//expected boogie
				"procedure tests.esc.A.m1(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: int;\n" +
				"	call a := tests.esc.A.m2(this);\n" +
				"	assert (a == 4);\n" +
				"}\n" +
				"procedure tests.esc.A.m2(this: $Ref) returns ($r: int) requires $dtype(this) == tests.esc.A; {\n" +
				"	$r := 4;\n" +
				"	return;\n" +
				"}\n"
				,
				// adapter output
				"----------\n" +
				"1. ERROR in A.java (at line 5)\n" +
				"	//@ assert c == 4;\n" +
				"	           ^^^^^^\n" +
				"This assertion might not hold.\n" +
				"----------\n"
				);
	}
	
	// term=MessageSend adapter=pass
	public void test_2001_messageSend() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" +
				"	public void m1() {\n" +
				"		int c = m2(5);\n" +
				"		//@ assert c == 5;\n" +
				"	}\n" +
				"	public int m2(int a) {\n" +
				"		return a;\n" +
				"	}\n" +
				"}\n"
				,
				//expected boogie
				"procedure tests.esc.A.m1(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: int;\n" +
				"	call a := tests.esc.A.m2(this, 5);\n" +
				"	assert (a == 5);\n" +
				"}\n" +
				"procedure tests.esc.A.m2(this: $Ref, a: int) returns ($r: int) requires $dtype(this) == tests.esc.A; {\n" +
				"	$r := a;\n" +
				"	return;\n" +
				"}\n"
				,
				// adapter output
				"----------\n" +
				"1. ERROR in A.java (at line 5)\n" +
				"	//@ assert c == 5;\n" +
				"	           ^^^^^^\n" +
				"This assertion might not hold.\n" +
				"----------\n"
				);
	}
	
	// term=MessageSend adapter=pass
	public void test_2002_messageSend() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" +
				"	public void m1() {\n" +
				"		m2();\n" +
				"	}\n" +
				"	public void m2() {\n" +
				"		//@ assert true;\n" +
				"	}\n" +
				"}\n"
				,
				//expected boogie
				"procedure tests.esc.A.m1(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	call tests.esc.A.m2(this);\n" +
				"}\n" +
				"procedure tests.esc.A.m2(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	assert true;\n" +
				"}\n"
				,
				// adapter output
				""
				);
	}

	// term=MessageSend adapter=pass
	public void test_2003_messageSendStatic() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" +
				"	public static void m() {\n" +
				"		N.n();\n" +
				"	}\n" +
				"}\n" +
				"class N {\n" +
				"	public static void n() {\n" +
				"		//@ assert true;\n" +
				"	}\n" +
				"}\n"
				,
				//expected boogie
				"procedure tests.esc.A.m() {\n" +
				"	call tests.esc.N.n();\n" +
				"}\n" +
				"procedure tests.esc.N.n() {\n" +
				"	assert true;\n" +
				"}\n"
				,
				// adapter output
				""
				);
	}
	
	// term=MessageSend adapter=pass
	public void test_2004_messageSendOnReceiver() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" +
				"	public static void m(N x) {" +
				"		x.o();\n" +
				"		int y = x.n(3);\n" +
				"	}\n" +
				"}\n" +
				"class N {\n" +
				"	public int n(int x) {\n" +
				"		return x;\n" +
				"	}\n" +
				"	public void o() { }\n" +
				"}\n" 
				,
				//expected boogie
				"procedure tests.esc.A.m(a: $Ref) requires $dtype(a) == tests.esc.N; {\n" +
				"	var b: int;\n" +
				"	call tests.esc.N.o(a);\n" +
				"	call b := tests.esc.N.n(a, 3);\n" +
				"}\n" +
				"procedure tests.esc.N.n(this: $Ref, a: int) returns ($r: int) requires $dtype(this) == tests.esc.N; {\n" +
				"	$r := a;\n" +
				"	return;\n" +
				"}\n" +
				"procedure tests.esc.N.o(this: $Ref) requires $dtype(this) == tests.esc.N; {\n" +
				"}\n"
				,
				// adapter output
				""
				);
	}

	// term=MessageSend adapter=pass
	public void test_2005_messageSendOnThis() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" +
				"	public void m() { this.n(); }\n" +
				"	public void n() { }\n" +
				"}\n"
				,
				//expected boogie
				"procedure tests.esc.A.m(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	call tests.esc.A.n(this);\n" +
				"}\n" +
				"procedure tests.esc.A.n(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"}\n",
				// adapter output
				""
				);
	}

	// term=MessageSend adapter=pass
	public void test_2006_messageSendOnField() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" +
				"	public N x = new N();\n" +
				"	public static N y = new N();\n" +
				"	public void m() {\n" +
				"		x.n();\n" +
				"		y.n();\n" +
				"	}\n" +
				"}\n" +
				"class N {\n" +
				"	public N() { }\n" +
				"	public void n() { }\n" +
				"}\n"
				,
				// expected boogie
				"var tests.esc.A.x: [$Ref] $Ref;\n" +
				"var tests.esc.A.y: $Ref;\n" +
				// FIXME "axiom $dtype(tests.esc.A.y) == tests.esc.N;\n" + (boogie says: cannot refer to global variable in this context)
				"procedure tests.esc.A.$initFields(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	call tests.esc.N.N(tests.esc.A.x[this]);\n" +
				"	call tests.esc.N.N(tests.esc.A.y);\n" +
				"}\n" +
				"procedure tests.esc.A.m(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	call tests.esc.N.n(tests.esc.A.x[this]);\n" +
				"	call tests.esc.N.n(tests.esc.A.y);\n" +
				"}\n" +
				"procedure tests.esc.N.N(this: $Ref) requires $dtype(this) == tests.esc.N; {\n" +
				"}\n" +
				"procedure tests.esc.N.n(this: $Ref) requires $dtype(this) == tests.esc.N; {\n" +
				"}\n"
				,
				// adapter output
				// TODO this output will change once global attribute typecheck axioms are implemented
				"----------\n" +
				"1. ERROR in A.java (at line 3)\n" +
				"	public N x = new N();\n" +
				"	             ^^^^^^^\n" +
				"This precondition might not hold.\n" +
				"----------\n" +
				"2. ERROR in A.java (at line 4)\n" +
				"	public static N y = new N();\n" +
				"	                    ^^^^^^^\n" +
				"This precondition might not hold.\n" +
				"----------\n" +
				"3. ERROR in A.java (at line 6)\n" +
				"	x.n();\n" +
				"	^^^^^\n" +
				"This precondition might not hold.\n" +
				"----------\n" +
				"4. ERROR in A.java (at line 7)\n" +
				"	y.n();\n" +
				"	^^^^^\n" +
				"This precondition might not hold.\n" +
				"----------\n" +
				"5. ERROR in A.java (at line 11)\n" +
				"	public N() { }\n" +
				"	       ^^^\n" +
				"This precondition might not hold.\n" +
				"----------\n" +
				"6. ERROR in A.java (at line 11)\n" +
				"	public N() { }\n" +
				"	       ^^^\n" +
				"This precondition might not hold.\n" +
				"----------\n" +
				"7. ERROR in A.java (at line 12)\n" +
				"	public void n() { }\n" +
				"	            ^^^\n" +
				"This precondition might not hold.\n" +
				"----------\n" +
				"8. ERROR in A.java (at line 12)\n" +
				"	public void n() { }\n" +
				"	            ^^^\n" +
				"This precondition might not hold.\n" +
				"----------\n"
				);
	}
	
	// term=MessageSend adapter=pass
	public void test_2007_messageSendOnLocal() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"class A {\n" +
				"	void m() { N x = new N(); x.n(); }\n" +
				"}\n" +
				"class N { public N() { } void n() { } }\n" 
				,
				// expected boogie
				"procedure tests.esc.A.m(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: $Ref;\n" +
				"	assume $dtype(a) == tests.esc.N;\n" +
				"	call tests.esc.N.N(a);\n" +
				"	call tests.esc.N.n(a);\n" +
				"}\n" +
				"procedure tests.esc.N.N(this: $Ref) requires $dtype(this) == tests.esc.N; {\n" +
				"}\n" +
				"procedure tests.esc.N.n(this: $Ref) requires $dtype(this) == tests.esc.N; {\n" +
				"}\n" 
				,
				// adapter output
				""
				);
	}
	
	// term=MessageSend,Assignment adapter=pass
	public void test_2008_MessageSendExpression() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" +
				"	public int fib(int n) {\n" +
				"		if (n < 2) return n;\n" +
				"		return fib(n - 1) + fib(n - 2);\n" +
				"	}\n" +
				"}" 
				,
				// expected boogie
				"procedure tests.esc.A.fib(this: $Ref, a: int) returns ($r: int) requires $dtype(this) == tests.esc.A; {\n" +
				"	var b: int;\n" +
				"	var c: int;\n" +
				"	if (a < 2) {\n" +
				"		$r := a;\n" +
				"		return;\n" +
				"	}\n" +
				"	call b := tests.esc.A.fib(this, (a - 1));\n" +
				"	call c := tests.esc.A.fib(this, (a - 2));\n" +
				"	$r := (b + c);\n" +
				"	return;\n" +
				"}\n"
				,
				// adapter output
				""
				);
	}

	// term=MessageSend adapter=pass
	public void test_2009_MessageSendMissingOutVar() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" +
				"	public boolean a() {\n" +
				"		a();\n" +
				"		return false;\n" +
				"	}\n" +
				"}" 
				,
				// expected boogie
				"procedure tests.esc.A.a(this: $Ref) returns ($r: bool) requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: bool;\n" +
				"	call a := tests.esc.A.a(this);\n" +
				"	$r := false;\n" +
				"	return;\n" +
				"}\n"
				,
				// adapter output
				""
				);
	}

	// term=ArrayTypeReference,ArrayReference adapter=pass
	public void test_2100_arrayField() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"class A {\n" +
				"	int[] x = new int[3];\n" +
				"	//@ modifies x;\n" +
				"	void m() {\n" +
				"		x[0] = 2; x[1] = 4; x[2] = 6;\n" +
				"		assert x[0] + x[1] + x[2] == 12;\n" +
				"	}\n" +
				"}\n" 
				,
				// expected boogie
				"var tests.esc.A.x: [$Ref] [int] int;\n" +
				"procedure tests.esc.A.m(this: $Ref) modifies tests.esc.A.x; requires $dtype(this) == tests.esc.A; {\n" +
				"	tests.esc.A.x[this][0] := 2;\n" +
				"	tests.esc.A.x[this][1] := 4;\n" +
				"	tests.esc.A.x[this][2] := 6;\n" +
				"	assert (((tests.esc.A.x[this][0] + tests.esc.A.x[this][1]) + tests.esc.A.x[this][2]) == 12);\n" +
				"}\n"
				,
				// adapter output
				""
				);
	}
	
	// term=ArrayTypeReference,ArrayReference,ArrayInitializer,QualifiedNameReference adapter=pass
	public void test_2101_arrayInitializer() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"class A {\n" +
				"	void m() {\n" +
				"		int x[] = { 1, 2, 3 };\n" +
				"		assert x[0] + x[1] + x[2] == 6;\n" +
				"		assert x.length == 3;\n" +
				"	}\n" +
				"}\n" 
				,
				// expected boogie
				"procedure tests.esc.A.m(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: [int] int;\n" +
				"	var a.length: int;\n" +
				"	a[0] := 1;\n" +
				"	a[1] := 2;\n" +
				"	a[2] := 3;\n" +
				"	a.length := 3;\n" +
				"	assert (((a[0] + a[1]) + a[2]) == 6);\n" +
				"	assert (a.length == 3);\n" +
				"}\n"
				,
				// adapter output
				""
				);
	}
	
	// term=ArrayTypeReference,ArrayReference,ArrayAllocationExpression adapter=pass
	public void test_2102_arrayAllocation() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"class A {\n" +
				"	void m() {\n" +
				"		int[] x = new int[1];\n" +
				"		if (true) {\n" +
				"			x = new int[2];\n" +
				"		}\n" +
				"	}\n" +
				"}\n" 
				,
				// expected boogie
				"procedure tests.esc.A.m(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: [int] int;\n" +
				"	var a.length: int;\n" +
				"	a[0] := 0;\n" +
				"	a.length := 1;\n" +
				"	if (true) {\n" +
				"		a[0] := 0;\n" +
				"		a[1] := 0;\n" +
				"		a.length := 2;\n" +
				"	}\n" +
				"}\n"
				,
				// adapter output
				""
				);
	}
	
	// term=ArrayTypeReference,QualifiedNameReference adapter=pass
	public void test_2103_arrayLength() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"class A {\n" +
				"	void m() {\n" +
				"		int[] x = new int[3];\n" +
				"		assert x.length == 3;\n" +
				"	}\n" +
				"}\n" 
				,
				// expected boogie
				"procedure tests.esc.A.m(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: [int] int;\n" +
				"	var a.length: int;\n" +
				"	a[0] := 0;\n" +
				"	a[1] := 0;\n" +
				"	a[2] := 0;\n" +
				"	a.length := 3;\n" +
				"	assert (a.length == 3);\n" +
				"}\n"
				,
				// adapter output
				""
				);
	}
	
	// term=ArrayTypeReference,ArrayAllocationExpression adapter=pass
	public void test_2104_arrayDefaultInitialization() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"class A {\n" +
				"	void m() {\n" +
				"		int[] x = new int[2];\n" +
				"		String[] y = new String[2];\n" +
				"		boolean[] z = new boolean[2];\n" +
				"	}\n" +
				"}\n" 
				,
				// expected boogie
				"procedure tests.esc.A.m(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: [int] int;\n" +
				"	var a.length: int;\n" +
				"	var b: [int] $Ref;\n" +
				"	var b.length: int;\n" +
				"	var c: [int] bool;\n" +
				"	var c.length: int;\n" +
				"	a[0] := 0;\n" +
				"	a[1] := 0;\n" +
				"	a.length := 2;\n" +
				"	b[0] := null;\n" +
				"	b[1] := null;\n" +
				"	b.length := 2;\n" +
				"	c[0] := false;\n" +
				"	c[1] := false;\n" +
				"	c.length := 2;\n" +
				"}\n"
				,
				// adapter output
				""
				);
	}
	
	// term=AllocationExpression,JmlConstructorDeclaration adapter=pass
	public void test_2200_JmlConstructorDeclaration() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"class A {" +
				"	public A(int x) {\n" +
				"	}\n" +
				"}\n" 
				,
				// expected boogie
				"procedure tests.esc.A.A(this: $Ref, a: int) requires $dtype(this) == tests.esc.A; {\n" +
				"}\n"
				,
				// adapter output
				""
				);
	}

	// term=MessageSend,AllocationExpression adapter=pass
	public void test_2201_ConstructorCall() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"class A {\n" +
				"	public A(int x) {\n" +
				"	}" +
				"	public static void x() {\n" +
				"		A a = new A(1);" +
				"	}\n" +
				"}\n" 
				,
				// expected boogie
				"procedure tests.esc.A.A(this: $Ref, a: int) requires $dtype(this) == tests.esc.A; {\n" +
				"}\n" +
				"procedure tests.esc.A.x() {\n" +
				"	var a: $Ref;\n" +
				"	assume $dtype(a) == tests.esc.A;\n" +
				"	call tests.esc.A.A(a, 1);\n" +
				"}\n"
				,
				// adapter output
				""
				);
	}

	// term=MessageSend,AllocationExpression,JmlQualifiedName adapter=pass
	public void test_2202_ConstructorCallFieldModification() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"class A {\n" +
				"	String x;\n" +
				"	//@ assignable x;\n" +
				"	//@ ensures x == \"hello world\";\n" +
				"	public A(int q) {\n" +
				"		x = \"hello world\";\n" +
				"	}\n" +
				"	public static void x() {\n" +
				"		A a = new A(1);\n" +
				"		assert a.x != \"abc\";\n" +
				"	}\n" +
				"}\n" 
				,
				// expected boogie
				"var tests.esc.A.x: [$Ref] $Ref;\n" +
//				"axiom ∀n: int • $dtype(tests.esc.A.x[n]) == tests.esc.A;\n" +
				"const unique $string_lit_0: $Ref;\n" +
				"const unique $string_lit_1: $Ref;\n" +
				"axiom $dtype($string_lit_0) == java.lang.String;\n" +
				"axiom $dtype($string_lit_1) == java.lang.String;\n" +
				"procedure tests.esc.A.A(this: $Ref, a: int) modifies tests.esc.A.x; requires $dtype(this) == tests.esc.A; ensures (tests.esc.A.x[this] == $string_lit_0); {\n" +
				"	tests.esc.A.x[this] := $string_lit_0;\n" +
				"}\n" +
				"procedure tests.esc.A.x() modifies tests.esc.A.x; {\n" +
				"	var a: $Ref;\n" +
				"	assume $dtype(a) == tests.esc.A;\n" +
				"	call tests.esc.A.A(a, 1);\n" +
				"	assert (tests.esc.A.x[a] != $string_lit_1);\n" +
				"}\n"
				,
				// adapter output
				""
				);
	}
	
	// term=CompilationUnitDeclaration adapter=pass
	public void test_2203_MultipleClasses() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"class A {" +
				"	public A() { }\n" +
				"	//@ ensures \\result == 2;\n" +
				"	public int n() { return 2; }\n" +
				"}\n" +
				"class B {\n" +
				"	public B() {\n" +
				"		A a = new A();\n" +
				"		int n = a.n();\n" +
				"		n++;\n" +
				"		//@ assert n == 3;\n" +
				"	}\n" +
				"}\n" 
				,
				// expected boogie
				"procedure tests.esc.A.A(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"}\n" +
				"procedure tests.esc.A.n(this: $Ref) returns ($r: int) requires $dtype(this) == tests.esc.A; ensures ($r == 2); {\n" +
				"	$r := 2;\n" +
				"	return;\n" +
				"}\n" +
				"procedure tests.esc.B.B(this: $Ref) requires $dtype(this) == tests.esc.B; {\n" +
				"	var a: $Ref;\n" +
				"	var b: int;\n" +
				"	assume $dtype(a) == tests.esc.A;\n" +
				"	call tests.esc.A.A(a);\n" +
				"	call b := tests.esc.A.n(a);\n" +
				"	b := (b + 1);\n" +
				"	assert (b == 3);\n" +
				"}\n" 
				,
				// adapter output
				""
				);
	}

	// term=ArrayTypeReference,ArrayReference,ConstructorDeclaration,MessageSend adapter=pass
	public void test_3000_StandardJavaClass() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"class A {\n" +
				"	public A() { }\n" +
				"	public int fib(int n) { return 2; }\n" +
				"	public static void main(String[] args) {\n" +
				"		A a = new A();\n" +
				"		int n = a.fib(1);\n" +
				"		assert n == 1;\n" +
				"	}\n" +
				"}\n"
				,
				// expected boogie
				"procedure tests.esc.A.A(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"}\n" +
				"procedure tests.esc.A.fib(this: $Ref, a: int) returns ($r: int) requires $dtype(this) == tests.esc.A; {\n" +
				"	$r := 2;\n" +
				"	return;\n" +
				"}\n" +
				"procedure tests.esc.A.main(a: [int] $Ref) {\n" +
				"	var b: $Ref;\n" +
				"	var c: int;\n" +
				"	assume $dtype(b) == tests.esc.A;\n" +
				"	call tests.esc.A.A(b);\n" +
				"	call c := tests.esc.A.fib(b, 1);\n" +
				"	assert (c == 1);\n" +
				"}\n"
				,
				// adpater output
				"----------\n" +
				"1. ERROR in A.java (at line 8)\n" +
				"	assert n == 1;\n" +
				"	       ^^^^^^\n" +
				"This assertion might not hold.\n" +
				"----------\n"
				);
	}
	
	// term=FieldReference,JmlOldExpression,JmlEnsuresClause adapter=pass
	public void test_3001_TestCounterClass() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" +
				"	public int counter;\n" +
				"	//@ assignable counter;\n" +
				"	//@ ensures counter == \\old(counter) + 1;\n" +
				"	public void increaseCounter() {\n" +
				"		counter = counter + 2;\n" +
				"	}\n" +
				"}\n" 
				,
				// expected boogie
				"var tests.esc.A.counter: [$Ref] int;\n" +
				"procedure tests.esc.A.increaseCounter(this: $Ref) modifies tests.esc.A.counter; requires $dtype(this) == tests.esc.A; ensures (tests.esc.A.counter[this] == (old(tests.esc.A.counter[this]) + 1)); {\n" +
				"	tests.esc.A.counter[this] := (tests.esc.A.counter[this] + 2);\n" +
				"}\n"
				,
				// adapter output
				"----------\n" +
				"1. ERROR in A.java (at line 5)\n" +
				"	//@ ensures counter == \\old(counter) + 1;\n" +
				"	            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"This postcondition might not hold.\n" +
				"----------\n" +
				"2. ERROR in A.java (at line 6)\n" +
				"	public void increaseCounter() {\n" +
				"	            ^^^^^^^^^^^^^^^^^\n" +
				"This postcondition might not hold.\n" +
				"----------\n"
				);
	}
	
	// term=FieldReference,JmlOldExpression,JmlEnsuresClause adapter=pass
	public void test_3002_TestCounterClass() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" +
				"	public int counter;\n" +
				"	public A() { }\n" +
				"	//@ assignable counter;\n" +
				"	//@ ensures counter == \\old(counter) + 1;\n" +
				"	public void increaseCounter() {\n" +
				"		counter = counter + 2;\n" +
				"	}\n" +
				"	public void x() { A a = new A(); a.increaseCounter(); }\n" +
				"}\n" 
				,
				// expected boogie
				"var tests.esc.A.counter: [$Ref] int;\n" +
				"procedure tests.esc.A.A(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"}\n" +
				"procedure tests.esc.A.increaseCounter(this: $Ref) modifies tests.esc.A.counter; requires $dtype(this) == tests.esc.A; ensures (tests.esc.A.counter[this] == (old(tests.esc.A.counter[this]) + 1)); {\n" +
				"	tests.esc.A.counter[this] := (tests.esc.A.counter[this] + 2);\n" +
				"}\n" +
				"procedure tests.esc.A.x(this: $Ref) modifies tests.esc.A.counter; requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: $Ref;\n" +
				"	assume $dtype(a) == tests.esc.A;\n" +
				"	call tests.esc.A.A(a);\n" +
				"	call tests.esc.A.increaseCounter(a);\n" +
				"}\n"
				,
				// adapter output
				"----------\n" +
				"1. ERROR in A.java (at line 6)\n" +
				"	//@ ensures counter == \\old(counter) + 1;\n" +
				"	            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"This postcondition might not hold.\n" +
				"----------\n" +
				"2. ERROR in A.java (at line 7)\n" +
				"	public void increaseCounter() {\n" +
				"	            ^^^^^^^^^^^^^^^^^\n" +
				"This postcondition might not hold.\n" +
				"----------\n"
				);
	}

	// term=FieldReference,Assignment adapter=pass
	public void test_3003_TestAttributeMutation() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" +
				"	public int counter;\n" +
				"	//@ modifies counter;\n" +
				"	public A() { counter = 2; }\n" +
				"	//@ modifies counter;\n" +
				"	public void x() {\n" +
				"		A a = new A();\n" +
				"		a.counter = 10;\n" +
				"		//@ assert a.counter == 11;\n" +
				"	}\n" +
				"}\n" 
				,
				// expected boogie
				"var tests.esc.A.counter: [$Ref] int;\n" +
				"procedure tests.esc.A.A(this: $Ref) modifies tests.esc.A.counter; requires $dtype(this) == tests.esc.A; {\n" +
				"	tests.esc.A.counter[this] := 2;\n" +
				"}\n" +
				"procedure tests.esc.A.x(this: $Ref) modifies tests.esc.A.counter; requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: $Ref;\n" +
				"	assume $dtype(a) == tests.esc.A;\n" +
				"	call tests.esc.A.A(a);\n" +
				"	tests.esc.A.counter[a] := 10;\n" +
				"	assert (tests.esc.A.counter[a] == 11);\n" +
				"}\n"
				,
				// adapter output
				"----------\n" +
				"1. ERROR in A.java (at line 10)\n" +
				"	//@ assert a.counter == 11;\n" +
				"	           ^^^^^^^^^^^^^^^\n" +
				"This assertion might not hold.\n" +
				"----------\n"
				);
	}
	
	// term=StringLiteral adapter=pass
	public void test_3004_TestStringArgument() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" +
				"	public void a(String arg) {\n" +
				"	}\n" +
				"	public void b() {\n" +
				"		a(\"hello world\");" +
				"	}\n" +
				"}\n"
				,
				// expected boogie
				"const unique $string_lit_0: $Ref;\n" +
				"axiom $dtype($string_lit_0) == java.lang.String;\n" +
				"procedure tests.esc.A.a(this: $Ref, a: $Ref) requires $dtype(this) == tests.esc.A; requires $dtype(a) == java.lang.String; {\n" +
				"}\n" +
				"procedure tests.esc.A.b(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	call tests.esc.A.a(this, $string_lit_0);\n" +
				"}\n"
				,
				// adapter output
				""
				);
	}

	// term=InstanceOfExpression adapter=pass
	public void test_3005_Subclass() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A extends Object {" +
				"	public A() { }\n" +
				"}\n"
				,
				// expected Boogie
				"procedure tests.esc.A.A(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"}\n"
				,
				""
				);

	}
	
	// term=InstanceOfExpression adapter=pass
	public void test_3006_InstanceOfExpression() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" +
				"	//@ requires arg instanceof C;\n" +
				"	public void a(B arg) {\n" +
				"		((C)arg).x();\n" +
				"	}\n" +
				"	public void b() {\n" +
				"		C c = new C();\n" +
				"		B b = new B();\n" +
				"		a(c);\n" +
				"		a(b);\n" +
				"	}\n" +
				"}" +
				"class B { public B() { } }\n" +
				"class C extends B { public C() { } public void x() { } }\n"
				,
				// expected boogie
				"procedure tests.esc.A.a(this: $Ref, a: $Ref) requires $dtype(this) == tests.esc.A; requires $dtype(a) == tests.esc.B; requires $dtype(a) <: tests.esc.C; {\n" +
				"	call tests.esc.C.x(a);\n" +
				"}\n" +
				"procedure tests.esc.A.b(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: $Ref;\n" +
				"	var b: $Ref;\n" +
				"	assume $dtype(a) == tests.esc.C;\n" +
				"	assume $dtype(b) == tests.esc.B;\n" +
				"	call tests.esc.C.C(a);\n" +
				"	call tests.esc.B.B(b);\n" +
				"	call tests.esc.A.a(this, a);\n" +
				"	call tests.esc.A.a(this, b);\n" +
				"}\n" +
				"procedure tests.esc.B.B(this: $Ref) requires $dtype(this) == tests.esc.B; {\n" +
				"}\n" +
				"procedure tests.esc.C.C(this: $Ref) requires $dtype(this) == tests.esc.C; {\n" +
				"}\n" +
				"procedure tests.esc.C.x(this: $Ref) requires $dtype(this) == tests.esc.C; {\n" +
				"}\n"
				,
				// adapter output
				"----------\n" +
				"1. ERROR in A.java (at line 4)\n" +
				"	public void a(B arg) {\n" +
				"	                ^^^\n" +
				"This precondition might not hold.\n" +
				"----------\n" +
				"2. ERROR in A.java (at line 10)\n" +
				"	a(c);\n" +
				"	^^^^\n" +
				"This precondition might not hold.\n" +
				"----------\n"
				);
		
	}
	
	// term=ThisReference adapter=pass
	public void test_3007_ThisReference() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" +
				"	public void a() {\n" +
				"		this.a();\n" +
				"	}\n" +
				"}" 
				,
				// expected boogie
				"procedure tests.esc.A.a(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	call tests.esc.A.a(this);\n" +
				"}\n"
				,
				// adapter output
				""
				);
	}
	
	// term=MessageSend adapter=fail
	public void test_3008_FullFibonacci() {
		this.compareJavaToBoogie(
				//java
				"package tests.esc;\n" +
				"public class A {\n" +
				"	//@ requires n >= 0;\n" +
				"	//@ ensures n < 2 ==> \\result == n;\n" +
				"	//@ ensures n >= 2 ==> \\result == fib(n-1) + fib(n-2);\n" +
				"	public int fib(int n) {\n" +
				"		if (n < 2) return n;\n" +
				"		return fib(n-1) + fib(n-2);\n" +
				"	}\n" +
				"	public void useFib() {\n" +
				"		//@ assert fib(0) == 0;\n" +
				"		//@ assert fib(1) == 1;\n" +
				"		//@ assert fib(23) == 28657;\n" +
				"		//@ assert fib(2) == 100;\n" +
				"	}\n" +
				"}" 
				,
				// expected boogie
				"function $fn.tests.esc.A.fib(this: $Ref, a: int) returns (int);\n" +
				"procedure tests.esc.A.fib(this: $Ref, a: int) returns ($r: int) requires $dtype(this) == tests.esc.A; requires (a >= 0); ensures ((a < 2) ⇒ ($r == a)); ensures ((a >= 2) ⇒ ($r == ($fn.tests.esc.A.fib(this, (a - 1)) + $fn.tests.esc.A.fib(this, (a - 2))))); {\n" +
				"	var b: int;\n" +
				"	var c: int;\n" +
				"	if (a < 2) {\n" +
				"		$r := a;\n" +
				"		return;\n" +
				"	}\n" +
				"	call b := tests.esc.A.fib(this, (a - 1));\n" +
				"	call c := tests.esc.A.fib(this, (a - 2));\n" +
				"	$r := (b + c);\n" +
				"	return;\n" +
				"}\n" +
				"procedure tests.esc.A.useFib(this: $Ref) requires $dtype(this) == tests.esc.A; {\n" +
				"	var a: int;\n" +
				"	var b: int;\n" +
				"	var c: int;\n" +
				"	var d: int;\n" +
				"	call a := tests.esc.A.fib(this, 0);\n" +
				"	assert (a == 0);\n" +
				"	call b := tests.esc.A.fib(this, 1);\n" +
				"	assert (b == 1);\n" +
				"	call c := tests.esc.A.fib(this, 23);\n" +
				"	assert (c == 28657);\n" +
				"	call d := tests.esc.A.fib(this, 2);\n" +
				"	assert (d == 100);\n" +
				"}\n"
				,
				// adapter output
				"----------\n" +
				"1. ERROR in A.java (at line 14)\n" +
				"	//@ assert fib(2) == 100;\n" +
				"	           ^^^^^^^^^^^^^\n" +
				"This assertion might not hold.\n" +
				"----------\n"
				);
	}
}