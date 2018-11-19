package cop5556sp18;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.Parser;
import cop5556sp18.Scanner;
import cop5556sp18.AST.ASTVisitor;
import cop5556sp18.AST.Program;
import cop5556sp18.TypeChecker.SemanticException;

public class TypeCheckerTest {

	/*
	 * set Junit to be able to catch exceptions
	 */
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * Prints objects in a way that is easy to turn on and off
	 */
	static final boolean doPrint = true;

	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 * Scans, parses, and type checks the input string
	 * 
	 * @param input
	 * @throws Exception
	 */
	void typeCheck(String input) throws Exception {
		show(input);
		// instantiate a Scanner and scan input
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		// instantiate a Parser and parse input to obtain and AST
		Program ast = new Parser(scanner).parse();
		show(ast);
		// instantiate a TypeChecker and visit the ast to perform type checking and
		// decorate the AST.
		ASTVisitor v = new TypeChecker();
		ast.visit(v, null);
	}



	/**
	 * Simple test case with an almost empty program.
	 * 
	 * @throws Exception
	 */
	@Test
	public void emptyProg() throws Exception {
		String input = "emptyProg{}";
		typeCheck(input);
	}

	@Test
	public void expression1() throws Exception {
		String input = "prog {show 3+4;}";
		typeCheck(input);
	}
	@Test
	public void expression6() throws Exception {
		String input = "prog {show 5.4+4;}";
		typeCheck(input);
	}
	
	@Test
	public void expression7() throws Exception {
		String input = "prog {show 5.4**4;}";
		typeCheck(input);
	}
	
	@Test
	public void expression8() throws Exception {
		String input = "prog {show 5%4;}";
		typeCheck(input);
	}
	
	@Test
	public void expression9() throws Exception {
		String input = "prog {sleep(5%4);}";
		typeCheck(input);
	}
	
	
	
	@Test
	public void expression2() throws Exception {
		String input = "prog {show true & false;}";
		typeCheck(input);
	}
	
	@Test
	public void expression3() throws Exception {
		String input = "prog {show 3+4;}";
		typeCheck(input);
	}
	
	@Test
	public void testIf() throws Exception {
		String input = "b{int c; image j;}";
		typeCheck(input);
	}
	
	@Test
	public void testSomething1() throws Exception {
		String input = "prog{image 	abc[2,4];}";
		typeCheck(input);
	}
	

}

