package cop5556sp18;
/* *
 * Initial code for SimpleParser for the class project in COP5556 Programming Language Principles 
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


import cop5556sp18.Scanner.Token;
import cop5556sp18.AST.*;
import cop5556sp18.Scanner.Kind;
import static cop5556sp18.Scanner.Kind.*;

import java.util.ArrayList;


public class Parser {
	
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}



	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}


	public Program parse() throws SyntaxException {
		Program p=program();
		matchEOF();
		return p;
	}

	/*
	 * Program ::= Identifier Block
	 */
	public Program program() throws SyntaxException {
		Token first=t;
		Block b=null;
		match(IDENTIFIER);
		b=block();
		return new Program(first,first,b);
	}
	
	/*
	 * Block ::=  { (  (Declaration | Statement) ; )* }
	 */
	
	Kind[] firstDec = { KW_int, KW_boolean, KW_image, KW_float, KW_filename };
	Kind[] firstType = { KW_int, KW_boolean, KW_float, KW_filename };
	Kind[] firstStatement = { KW_input, KW_write, IDENTIFIER, KW_red, KW_green, KW_blue, KW_alpha, KW_while, KW_if, KW_show, KW_sleep /* TODO  correct this */  };

	public Block block() throws SyntaxException {
		
		Token firstToken=t;
		ArrayList<ASTNode> decs = new ArrayList<>();
		match(LBRACE);
		while (isKind(firstDec)|| isKind(firstStatement)) {
	     if (isKind(firstDec)) {
			decs.add(declaration());
		} else if (isKind(firstStatement)) {
			decs.add(statement());
		}
			match(SEMI);
		}
		match(RBRACE);
		
		return new Block(firstToken,decs);
	}
	
	public Declaration declaration() throws SyntaxException {
		//TODO
		Declaration d=null;
		Token first=t;
		Expression e1=null;
		Expression e2=null;
		if(isKind(firstDec)){
			type();
			Token name=t;
			match(IDENTIFIER);
			if(isKind(Kind.LSQUARE)){
				consume();
				e1=expression();
				match(Kind.COMMA);
				e2=expression();
				match(RSQUARE);
			}
			d=new Declaration(first,first,name,e1,e2);
		}
		else
		throw new UnsupportedOperationException();
		
		return d;
	}
	
	public void type() throws SyntaxException{
		if(isKind(KW_int) || isKind(KW_boolean) || isKind(KW_filename) || isKind(KW_image) || isKind(KW_float)) consume();
	}
	
	public Expression expression() throws SyntaxException {
		Token first = t;
		Expression one = null;
		one = orExpression();
		if(one==null) throw new SyntaxException(t,"exp Expected");
		Expression two = null;
		Expression three = null;
		if (isKind(OP_QUESTION)) {
			match(OP_QUESTION);
			two = expression();
			match(OP_COLON);
			three = expression();
		} else
			return one;
		return new ExpressionConditional(first, one, two, three);
	}
	
	public Expression orExpression() throws SyntaxException{
		Token op=null;
		Expression one=null;
		Expression two=null;
		Token first=t;
		one=AndExpression();
		if(one!=null){
		while(isKind(Kind.OP_OR)){
			op=t;
			consume();
			two=AndExpression();
			if (two != null) {
				one = new ExpressionBinary(first, one, op, two);
			} 
			else
				return null;		
			} 
		}
		return one;
		}
		

	
	public Expression AndExpression() throws SyntaxException{
		Expression one = null;
		 Expression two = null;
		 Token first = t;
		 Token op = null;
		one=EqExpression();
		if(one!=null){
		while(isKind(Kind.OP_AND)){
			op=t;
			consume();
			two=EqExpression();
			if(two!=null){
				one = new ExpressionBinary(first,one,op,two);
			}
			else return null;
			}
		}
		return one;
	}
	
	public Expression EqExpression() throws SyntaxException{
		Expression one = null;
		 Expression two = null;
		 Token first = t;
		Token op = null;
		one=RelExpression();
		if(one!=null){
		while(isKind(Kind.OP_EQ) || isKind(Kind.OP_NEQ)){
			op=t;
			consume();
			two=RelExpression();
			if(two!=null){
				one = new ExpressionBinary(first,one,op,two);
			}
			else return null;
			}
		}
		return one;
	}
	
	public Expression RelExpression() throws SyntaxException{
		Expression one = null;
		Expression two = null;
		 Token first = t;
		Token op = null;
		one=AddExpression();
		if(one!=null){
		while(isKind(Kind.OP_LT) || isKind(Kind.OP_GT) || isKind(Kind.OP_LE) || isKind(Kind.OP_GE)){
			op = t;
			consume();
			two=AddExpression();
			if(two!=null){
				one = new ExpressionBinary(first,one,op,two);
			}
			else return null;
		
		}
		}
		return one;
	}
		
	public Expression AddExpression() throws SyntaxException{
		Expression one = null;
		 Expression two = null;
		 Token first = t;
		Token op = null;
		one=MultiExpression();
		while(isKind(Kind.OP_PLUS) || isKind(Kind.OP_MINUS) ){
			op=t;
			consume();
			two=MultiExpression();
			if(two!=null){
				one = new ExpressionBinary(first,one,op,two);
			}
			else return null;	
		}
		return one;
	}
	
	public Expression MultiExpression() throws SyntaxException{
		Expression one = null;
		 Expression two = null;
		 Token first = t;
		Token op = null;
		one=PowerExpression();
		while(isKind(Kind.OP_TIMES) || isKind(Kind.OP_DIV) || isKind(Kind.OP_MOD)){
			op=t;
			consume();
			two=PowerExpression();
			if(two!=null){
				one = new ExpressionBinary(first,one,op,two);
			}
			else return null;
		}
		
		return one;
	}
	
	public Expression PowerExpression() throws SyntaxException{
		Expression one = null;
		 Expression two = null;
		 Token first = t;
		Token op = null;
		one=UnaryExpression();
		if (one!=null) {
		if(isKind(Kind.OP_POWER))
		{	op=t;
			consume();
			two=PowerExpression();
			if (two != null) {
				one = new ExpressionBinary(first, one, op, two);
			} else
				return null;
			
		}
		}
		return one;
	}
	
	public Expression UnaryExpression() throws SyntaxException{
		Token first = t;
		Token op = null;
		
		if(isKind(OP_PLUS) || isKind(OP_MINUS)){
			op = t;
			consume();
			Expression e=UnaryExpression();
			if(e==null) return null;
			return new ExpressionUnary(first,op,e);
		}
		else{
			return UnaryExpressionNotPlusOrMinus();
		}
	}
	
	Kind[] firstFunctionApplication={KW_sin, KW_cos, KW_atan, KW_abs,KW_log, KW_cart_x, KW_cart_y, KW_polar_a, KW_polar_r, KW_int, KW_float, KW_width, KW_height} ;
	public Expression UnaryExpressionNotPlusOrMinus() throws SyntaxException{
		Token first = t;
		if(isKind(Kind.OP_EXCLAMATION)){
			Token op = t;
			Expression e =null;
			consume();
			Expression u=UnaryExpression();
			if(u!=null)
			{
				e = new ExpressionUnary(first,op,u);
			}
			return e;
		}
		else if(isKind(INTEGER_LITERAL) || isKind(FLOAT_LITERAL) || isKind(Kind.BOOLEAN_LITERAL)){
			if(isKind(INTEGER_LITERAL)){
				ExpressionIntegerLiteral l = new ExpressionIntegerLiteral(first,t); 
				consume();
				return l;
			}
			else if(isKind(FLOAT_LITERAL)){
				ExpressionFloatLiteral l = new ExpressionFloatLiteral(first,t); 
				consume();
				return l;
			}else if(isKind(BOOLEAN_LITERAL)){
				ExpressionBooleanLiteral l = new ExpressionBooleanLiteral(first,t); 
				consume();
				return l;
			}
		}else if(isKind(Kind.LPAREN)){
				consume();
				Expression e=expression();
				match(Kind.RPAREN);
				return e;
			}
			else if(isKind(firstFunctionApplication)|| isKind(firstColor)){
				consume();
				if(isKind(LPAREN)){
					consume();
					Expression e=expression();
					match(RPAREN);
					return new ExpressionFunctionAppWithExpressionArg(first, first, e);
			}
				else{
						match(LSQUARE);
						Expression e1=expression();
						match(Kind.COMMA);
						Expression e2=expression();
						match(RSQUARE);
						return new ExpressionFunctionAppWithPixel(first, first, e1,e2);
					}
			}
			else if(isKind(IDENTIFIER)){
				consume();
				PixelSelector p=null;
				ExpressionPixel ep=null;
				if(isKind(Kind.LSQUARE))
				{	
					p=pixelselector();	
					return new ExpressionPixel(first, first, p);
				}
				return new ExpressionIdent(first, first);
			}
			else if(isKind(Kind.LPIXEL)){
				return pixelConstructor(); 
			}else if (isKind(KW_Z) || isKind(KW_default_height) || isKind(Kind.KW_default_width)){
				preDefinedName();
				return new ExpressionPredefinedName(first, first);
			}
			else{
				throw new SyntaxException(t,"Syntax Error at position");	
			}
		return null;
		}
	
	public Expression pixelConstructor() throws SyntaxException{
		Token first=t;
		match(LPIXEL);
		Expression one=expression();
		match(COMMA);
		Expression two=expression();
		match(COMMA);
		Expression three=expression();
		match(COMMA);
		Expression four=expression();
		match(RPIXEL);
		return new ExpressionPixelConstructor(first, one, two, three, four);
	}
	public void preDefinedName() throws SyntaxException{
		if(isKind(KW_Z) || isKind(KW_default_height) || isKind(Kind.KW_default_width)){
			consume();
		}
	}
	
		
	
	
	Kind[] firstLHS={IDENTIFIER,KW_red, KW_green, KW_blue, KW_alpha};
	
	public Statement statement() throws SyntaxException {
		//TODO
		Token first = t;
		Statement s=null;
		if(isKind(KW_input)){
			s=statementInput();
			return s;
		}
		else if(isKind(KW_write)){
			s=statementWrite();
			return s;
		}
		else if(isKind(firstLHS)){
			s=StatementAssignment();
			return s;
		}
		else if(isKind(KW_while)){
			consume();
			match(Kind.LPAREN);
			Expression e=expression();
			match(Kind.RPAREN);
			Block b=block();
			return new StatementWhile(first, e, b);
		}
		else if(isKind(KW_if)){
			consume();
			match(Kind.LPAREN);
			Expression e=expression();
			match(Kind.RPAREN);
			Block b=block();
			return new StatementIf(first, e, b);
		}
		else if(isKind(KW_show)){
			consume();
			Expression e=expression();
			return new StatementShow(first,e);
		}
		else if(isKind(KW_sleep)){
			consume();
			Expression e=expression();
			return new StatementSleep(first,e);
		}else {
			throw new SyntaxException(t,"couldn't find statement type");
		}
		//throw new UnsupportedOperationException();
	}
	
	public Statement StatementAssignment() throws SyntaxException{
		Token first=t;
		Expression e=null;
		LHS lhs=LHS();
		match(Kind.OP_ASSIGN);
		e=expression();
		return new StatementAssign(first,lhs,e);
	}
	
	Kind[] firstColor={KW_red, KW_green, KW_blue, KW_alpha};
	public LHS LHS() throws SyntaxException {
		Token first=t;
		PixelSelector p=null;
			if(isKind(IDENTIFIER)){
				Token name=t;
				match(IDENTIFIER);
				if(isKind(Kind.LSQUARE)){
					p=pixelselector();
					return new LHSPixel(first,name,p);
				}
				return new LHSIdent(first,name);
				}
			else{
				Token col=t;
				color();
				match(Kind.LPAREN);
				Token name=t;
				match(IDENTIFIER);
				p=pixelselector();
				match(Kind.RPAREN);
				return new LHSSample(first,name,p,col);
			}
	}
	
	public void color() throws SyntaxException{
		if(isKind(firstColor))
			consume();
	}


	public Statement statementInput() throws SyntaxException{
		Token first=t;
		match(KW_input);
		Token name=t;
		match(IDENTIFIER);
		match(KW_from);
		match(Kind.OP_AT);
		Expression e=expression();
		return new StatementInput(first, name, e);
	}
	
	public Statement statementWrite() throws SyntaxException{
		Token first=t;
		match(KW_write);
		Token sourceName=t;
		match(IDENTIFIER);
		match(KW_to);
		Token destName=t;
		match(IDENTIFIER);
		return new StatementWrite(first, sourceName, destName);
	}
	
	public PixelSelector pixelselector() throws SyntaxException{
		Token first  = t;
		Expression e1=null;
		Expression e2=null;
		match(LSQUARE);
		e1=expression();
		match(COMMA);
		e2=expression();
		match(RSQUARE);
		return new PixelSelector(first,e1,e2);
	}
	
	protected boolean isKind(Kind kind) {
		return t.kind == kind;
	}

	protected boolean isKind(Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind)
				return true;
		}
		return false;
	}


	/**
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		Token tmp = t;
		if (isKind(kind)) {
			consume();
			return tmp;
		}
		throw new SyntaxException(t,"Syntax Error at position"+tmp.posInLine()+"at token: "+tmp.getText()+"at line"+tmp.line()+" Expected Token:"+ kind); //TODO  give a better error message!
	}


	private Token consume() throws SyntaxException {
		Token tmp = t;
		if (isKind( EOF)) {
			throw new SyntaxException(t,"Syntax Error"); //TODO  give a better error message!  
			//Note that EOF should be matched by the matchEOF method which is called only in parse().  
			//Anywhere else is an error. */
		}
		System.out.println(t.getText());
		t = scanner.nextToken();
		return tmp;
	}


	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (isKind(EOF)) {
			return t;
		}
		throw new SyntaxException(t,"Syntax Error at position"+t.posInLine()+"at token: "+t.getText()+"at line"+t.line()+" Expected Token:"+ Kind.EOF); //TODO  give a better error message!
	}
	

}
