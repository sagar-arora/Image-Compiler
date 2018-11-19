 /**
 * JUunit tests for the Parser for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Spring 2018.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Spring 2018 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2018
 */

package cop5556sp18;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.Parser;
import cop5556sp18.Scanner;
import cop5556sp18.Parser.SyntaxException;
import cop5556sp18.Scanner.LexicalException;

public class SimpleParserTest {

	//set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	
	//To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}


	//creates and returns a parser for the given input.
	private Parser makeParser(String input) throws LexicalException {
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		Parser parser = new Parser(scanner);
		return parser;
	}
	
	

	/**
	 * Simple test case with an empty program.  This throws an exception 
	 * because it lacks an identifier and a block. The test case passes because
	 * it expects an exception
	 *  
	 * @throws LexicalException
	 * @throws SyntaxException 
	 */
	@Test
	public void testEmpty() throws LexicalException, SyntaxException {
		String input = "";  //The input is the empty string.  
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		parser.parse();
	}
	
	/**
	 * Smallest legal program.
	 *   
	 * @throws LexicalException
	 * @throws SyntaxException 
	 */
	@Test
	public void testSmallest() throws LexicalException, SyntaxException {
		String input = "b{}";  
		Parser parser = makeParser(input);
		parser.parse();
	}	
	
	
	//This test should pass in your complete parser.  It will fail in the starter code.
	//Of course, you would want a better error message. 
	@Test
	public void testDec0() throws LexicalException, SyntaxException {
		String input = "b{int c;}";
		Parser parser = makeParser(input);
		parser.parse();
	}

	@Test
	public void testDec1() throws LexicalException, SyntaxException {
		String input = "b{int c; int d; float e; boolean f;}";
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testStatement0() throws LexicalException, SyntaxException {
		String input = "b{write abc to abd;}";
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testStatement1() throws LexicalException, SyntaxException {
		String input = "b{write abc to abd;}";
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testStatement2() throws LexicalException, SyntaxException {
		String input = "b{input abc from @ abd==def;}";
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testStatement3() throws LexicalException, SyntaxException {
		String input = "b{input abc from @ abd!=def;}";
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testStatement4() throws LexicalException, SyntaxException {
		String input = "b{c:=d+e;}";
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testStatement5() throws LexicalException, SyntaxException {
		String input = "b{c:=d+e;}";
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testStatement6() throws LexicalException, SyntaxException {
		String input = "b{c[a==b, c==d]:=d+e;}";
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testStatement7() throws LexicalException, SyntaxException {
		String input = "b{show a;}";
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testStatement8() throws LexicalException, SyntaxException {
		String input = "b{show c==a-b;}";
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testWhile() throws LexicalException, SyntaxException {
		String input = "b{while(a>b){int c;};}";
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testIf() throws LexicalException, SyntaxException {
		String input = "b{if(a>b){int c;};}";
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testStatementAssignment() throws LexicalException, SyntaxException {
		String input = "b{blue (abc [cde, fgh]):= cde;}";
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testExpression0() throws LexicalException, SyntaxException {
		String input = "b{input abc from @ a>5?6:7;}";
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testExpression2() throws LexicalException, SyntaxException {
		String input = "b{input abc from @ c==2**3;}";
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testExpression3() throws LexicalException, SyntaxException {
		String input = "b{input abc from @ c==2/3*4;}";
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testExpression4() throws LexicalException, SyntaxException {
		String input = "b{input abc from @ c==2/3*4&abc;}";
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	
			
}
	

