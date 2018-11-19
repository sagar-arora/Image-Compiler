package cop5556sp18;

import cop5556sp18.Scanner.Kind;
import cop5556sp18.Scanner.Token;
import cop5556sp18.AST.ASTNode;
import cop5556sp18.AST.ASTVisitor;
import cop5556sp18.AST.Block;
import cop5556sp18.AST.Declaration;
import cop5556sp18.AST.Expression;
import cop5556sp18.AST.ExpressionBinary;
import cop5556sp18.AST.ExpressionBooleanLiteral;
import cop5556sp18.AST.ExpressionConditional;
import cop5556sp18.AST.ExpressionFloatLiteral;
import cop5556sp18.AST.ExpressionFunctionAppWithExpressionArg;
import cop5556sp18.AST.ExpressionFunctionAppWithPixel;
import cop5556sp18.AST.ExpressionIdent;
import cop5556sp18.AST.ExpressionIntegerLiteral;
import cop5556sp18.AST.ExpressionPixel;
import cop5556sp18.AST.ExpressionPixelConstructor;
import cop5556sp18.AST.ExpressionPredefinedName;
import cop5556sp18.AST.ExpressionUnary;
import cop5556sp18.AST.LHS;
import cop5556sp18.AST.LHSIdent;
import cop5556sp18.AST.LHSPixel;
import cop5556sp18.AST.LHSSample;
import cop5556sp18.AST.PixelSelector;
import cop5556sp18.AST.Program;
import cop5556sp18.AST.StatementAssign;
import cop5556sp18.AST.StatementIf;
import cop5556sp18.AST.StatementInput;
import cop5556sp18.AST.StatementShow;
import cop5556sp18.AST.StatementSleep;
import cop5556sp18.AST.StatementWhile;
import cop5556sp18.AST.StatementWrite;
import cop5556sp18.*;
import cop5556sp18.Types;
import cop5556sp18.Types.Type;

public class TypeChecker implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class SemanticException extends Exception {
		Token t;

		public SemanticException(Token t, String message) {
			super(message);
			this.t = t;
		}
	}
	
	SymbolTable symbols=new SymbolTable();

	
	
	// Name is only used for naming the output file. 
	// Visit the child block to type check program.
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		program.block.visit(this, arg);
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		symbols.enterScope();
		for (ASTNode node: block.decsOrStatements) {
			node.visit(this, arg);
		}
		symbols.leaveScope();
		return null;
	}

	@Override
	public Object visitDeclaration(Declaration declaration, Object arg) throws Exception {
		// TODO Auto-generated method stub
		String name = declaration.name;
		Types.Type type = Types.getType(declaration.type);
		if(symbols.lookup(name)!=null && symbols.lookupScope(name)==symbols.getCurrentScope() ){
			throw new SemanticException(declaration.firstToken,"Varibale "+name+
					"has been already declared ! at "+ declaration.firstToken.posInLine());
		}

		Expression e0=declaration.width;
		Expression e1=declaration.height;
		if(e0!=null ) {
			e0.visit(this, null);
			if(e0.type!=Type.INTEGER || type!=Type.IMAGE) {
				throw new SemanticException(declaration.firstToken,"Type Mismatch!");
		}
		}
		if(e1!=null ) {
			e1.visit(this, null);
			if(e1.type!=Type.INTEGER || type!=Type.IMAGE){
				throw new SemanticException(declaration.firstToken,"Type Mismatch!");
		}
		}
		symbols.insert(name, declaration);
		return null;
	}

	@Override
	public Object visitStatementWrite(StatementWrite statementWrite, Object arg) throws Exception {
		
		String sourceName=statementWrite.sourceName;
		Declaration sourceDec= symbols.lookup(sourceName);
		if(sourceDec==null)
			throw new SemanticException(statementWrite.firstToken, "Statement Write is wrong, source not declared");
		
		String destName=statementWrite.destName;
		Declaration destDec= symbols.lookup(destName);
	
		if(destDec==null)
			throw new SemanticException(statementWrite.firstToken, "Statement Write is wrong, Destination not declared");
		
		Types.Type sourceType=Types.getType(sourceDec.type);
		if(sourceType!=Types.Type.IMAGE) {
			throw new SemanticException(statementWrite.firstToken, "Statement Write is wrong, source type should be image type instead found to be"+ sourceType);	
		}
		Types.Type destType=Types.getType(destDec.type);
		if(destType!=Types.Type.FILE) {
			throw new SemanticException(statementWrite.firstToken, "Statement Write is wrong, destiation type should be File type instead found to be"+ sourceType);	
		}
		
		return null;
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg) throws Exception {
	
		String destName=statementInput.destName;
		Expression e=statementInput.e;
		e.visit(this, null);
		Declaration dec=symbols.lookup(destName);
		if(dec==null) throw new SemanticException(statementInput.firstToken, "Variable not declared");
		if(e.type!=Types.Type.INTEGER) 
			throw new SemanticException(statementInput.firstToken, "Decleration should be of type Integer but found "
														+dec.type+" at "+ statementInput.firstToken.posInLine());

		return null;
	}

	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
			
		Expression ex=pixelSelector.ex;
		ex.visit(this,null);
		Expression ey=pixelSelector.ey;
		ey.visit(this, null);
		if(ex.type!=ey.type) {
			throw new SemanticException(pixelSelector.firstToken,"There is type mismatch");
		}
		if(ex.type!=Type.INTEGER && ex.type!=Type.FLOAT)
		{
			throw new SemanticException(pixelSelector.firstToken,"Type of expression 0 is expected to be Integer OR Float but found "+ ex.type+ 
					" at "+ pixelSelector.firstToken.posInLine());
		}
		
		return null;
	}

	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		
		Expression e0=expressionConditional.guard;
		e0.visit(this, null);
		Expression e1=expressionConditional.trueExpression;
		e1.visit(this, null);
		Expression e2=expressionConditional.falseExpression;
		e2.visit(this, null);
		if(e0.type!=Type.BOOLEAN) {
			throw new SemanticException(expressionConditional.firstToken, "Conditional Statement expression 1 should be of type boolean but found to be" + e0.type +
											" at "+ expressionConditional.firstToken.posInLine());
		}
		
		if(e1.type!=e2.type)
		{
			throw new SemanticException(expressionConditional.firstToken, "expression 1 and expression 2 have different types, first is "+
																			e1.type+ " and second is "+ e2.type);
		}

		expressionConditional.type=e1.type;
		return null;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
		
		Expression e0 = expressionBinary.leftExpression;
		e0.visit(this, null);
		Expression e1 = expressionBinary.rightExpression;
		e1.visit(this, null);
		Kind op = expressionBinary.op;
		
		if((op==Kind.OP_EQ||op==Kind.OP_NEQ||op==Kind.OP_GE||op==Kind.OP_GT||op==Kind.OP_LE||op==Kind.OP_LT) && ((e0.type==Types.Type.INTEGER && 
				e1.type==Types.Type.INTEGER )||(e0.type==Types.Type.FLOAT && e1.type==Types.Type.FLOAT) || (e0.type==Types.Type.BOOLEAN &&
				e1.type==Types.Type.BOOLEAN))){
			expressionBinary.type = Types.Type.BOOLEAN;
		}
		else if((op==Kind.OP_AND || op==Kind.OP_OR)&&((e0.type==Types.Type.INTEGER && e1.type==Types.Type.INTEGER )
				|| 	(e0.type==Types.Type.BOOLEAN && e1.type==Types.Type.BOOLEAN ))){
			expressionBinary.type = e0.type;
		}
		else if((op==Kind.OP_POWER ||op==Kind.OP_PLUS ||op==Kind.OP_MINUS|| op == Kind.OP_TIMES || op == Kind.OP_DIV || op == Kind.OP_MOD) 
				&&(e0.type==Types.Type.INTEGER && e1.type==Types.Type.INTEGER )){
			expressionBinary.type = Types.Type.INTEGER;
		}
		else if((op==Kind.OP_POWER ||op==Kind.OP_PLUS ||op==Kind.OP_MINUS|| op == Kind.OP_TIMES || op == Kind.OP_DIV ) 
				&&((e0.type==Types.Type.INTEGER && e1.type==Types.Type.FLOAT)||(e0.type==Types.Type.FLOAT && e1.type==Types.Type.FLOAT)||
						(e0.type==Types.Type.FLOAT && e1.type==Types.Type.INTEGER)  )) {
			expressionBinary.type = Types.Type.FLOAT;
		}
		else
			expressionBinary.type=null;
		
		if(expressionBinary.type==null)
			throw new SemanticException(expressionBinary.firstToken,"There is type mismatch");
				
		return null;
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnarnull, Object arg) throws Exception {
		Expression e=expressionUnarnull.expression;
		e.visit(this, null);
		expressionUnarnull.type=e.type;
		return null;
	}

	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		expressionIntegerLiteral.type = Types.Type.INTEGER;
		return null;
	}

	@Override
	public Object visitBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg) throws Exception {
		
		expressionBooleanLiteral.type = Types.Type.BOOLEAN;
		return null;
	}

	@Override
	public Object visitExpressionPredefinedName(ExpressionPredefinedName expressionPredefinedName, Object arg)
			throws Exception {
		
		expressionPredefinedName.type=Type.INTEGER;
		return null;
	}

	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		expressionFloatLiteral.type=Types.Type.FLOAT;
		return null;
	}
	
	@Override
	public Object visitExpressionFunctionAppWithExpressionArg(
			ExpressionFunctionAppWithExpressionArg expressionFunctionAppWithExpressionArg, Object arg)
			throws Exception {
		Kind function=expressionFunctionAppWithExpressionArg.function;
		Expression e=expressionFunctionAppWithExpressionArg.e;
		e.visit(this, null);
		if((function==Kind.KW_abs || function==Kind.KW_alpha || function==Kind.KW_green || 
				function==Kind.KW_blue || function==Kind.KW_red ) && (e.type==Type.INTEGER ) ) {
			expressionFunctionAppWithExpressionArg.type=Type.INTEGER;
		}
		else if((function==Kind.KW_abs || function==Kind.KW_sin || function==Kind.KW_cos || 
				function==Kind.KW_atan || function==Kind.KW_log ) && (e.type==Type.FLOAT) ) {
			expressionFunctionAppWithExpressionArg.type=Type.FLOAT;
		}
		else if(function==Kind.KW_float && (e.type==Type.FLOAT || e.type==Type.INTEGER  )) {
			expressionFunctionAppWithExpressionArg.type=Type.FLOAT;
		}
		else if(function==Kind.KW_int && (e.type==Type.FLOAT || e.type==Type.INTEGER  )) {
			expressionFunctionAppWithExpressionArg.type=Type.INTEGER;
		}
		else if((function==Kind.KW_height || function==Kind.KW_width ) && (e.type==Type.IMAGE )) {
			expressionFunctionAppWithExpressionArg.type=Type.INTEGER;
		}
		else {
			throw new SemanticException(expressionFunctionAppWithExpressionArg.firstToken, "Incompatible function and expression");
		}
		
		return null;
	}

	@Override
	public Object visitExpressionFunctionAppWithPixel(ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
			Object arg) throws Exception {
		Kind functionName=expressionFunctionAppWithPixel.name;
		Expression e0=expressionFunctionAppWithPixel.e0;
		e0.visit(this, null);
		Expression e1=expressionFunctionAppWithPixel.e1;
		e1.visit(this, null);
		if(functionName==Kind.KW_cart_x || functionName==Kind.KW_cart_y ) {
			if(e0.type!=Types.Type.FLOAT ) {
				throw new SemanticException(expressionFunctionAppWithPixel.firstToken,"types of expression 0 should be float but found out to be "+ e0.type);
			}
			if(e1.type!=Types.Type.FLOAT ) {
				throw new SemanticException(expressionFunctionAppWithPixel.firstToken,"types of expression 1 should be float but found out to be "+ e1.type);
			}
			expressionFunctionAppWithPixel.type=Type.INTEGER;
		}
		if(functionName==Kind.KW_polar_a|| functionName==Kind.KW_polar_r) {
			if(e0.type!=Types.Type.INTEGER ) {
				throw new SemanticException(expressionFunctionAppWithPixel.firstToken,"types of expression 0 should be Integer but found out to be "+ 
																						e0.type+" at"+ expressionFunctionAppWithPixel.firstToken.posInLine());
			}
			if(e1.type!=Types.Type.INTEGER ) {
				throw new SemanticException(expressionFunctionAppWithPixel.firstToken,"types of expression 1 should be Integer but found out to be "+ e1.type);
			}
			expressionFunctionAppWithPixel.type=Type.FLOAT;
		}
		
		return null;
	}

	@Override
	public Object visitExpressionPixelConstructor(ExpressionPixelConstructor expressionPixelConstructor, Object arg)
			throws Exception {
		
		Expression alpha=expressionPixelConstructor.alpha;
		alpha.visit(this, null);
		Expression blue=expressionPixelConstructor.blue;
		blue.visit(this, null);
		Expression red=expressionPixelConstructor.red;
		red.visit(this, null);
		Expression green=expressionPixelConstructor.green;
		green.visit(this, null);
		if(alpha.type!=Types.Type.INTEGER) {
			throw new SemanticException(expressionPixelConstructor.firstToken,"types of alpha should be Integer but found out to be "+ alpha.type);
		}
			
		if(blue.type!=Types.Type.INTEGER)  {
			throw new SemanticException(expressionPixelConstructor.firstToken,"types of blue should be Integer but found out to be "+ blue.type);
		}
			
		if(green.type!=Types.Type.INTEGER)  {
			throw new SemanticException(expressionPixelConstructor.firstToken,"types of green should be Integer but found out to be "+ green.type);
		}
				
		if(red.type!=Types.Type.INTEGER )
		{
			throw new SemanticException(expressionPixelConstructor.firstToken,"types of red should be Integer but found out to be "+ red.type);
		}
		
		expressionPixelConstructor.type=Type.INTEGER;
		return null;
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws Exception {
		
		LHS l = statementAssign.lhs;
		l.visit(this,null);
		Expression e = statementAssign.e;
		e.visit(this, null);
		if(l.type!=e.type)
			throw new SemanticException(statementAssign.firstToken,"types don't match in assigsment");
		return null;
	}

	@Override
	public Object visitStatementShow(StatementShow statementShow, Object arg) throws Exception {
		Expression e=statementShow.e;
		e.visit(this, null);
		if(e.type!=Type.INTEGER && e.type!=Type.BOOLEAN && e.type!=Type.FLOAT && e.type!=Type.IMAGE)
		{
			throw new SemanticException(e.firstToken,"Show condition is not of type Integer, Float, Image, boolean instead found of type"
					+ e.firstToken.kind +" at " + statementShow.firstToken.posInLine());
		}
			
		return null;
	}

	@Override
	public Object visitExpressionPixel(ExpressionPixel expressionPixel, Object arg) throws Exception {
		
		String name=expressionPixel.name;
		Declaration dec=symbols.lookup(name);
		if(dec==null) {
			throw new SemanticException(expressionPixel.firstToken, "Variable not declared "+ expressionPixel.firstToken.getText()+ 
																	" at "+expressionPixel.firstToken.posInLine());
		}
		if(Types.getType(dec.type)!=Type.IMAGE) {
			throw new SemanticException(expressionPixel.firstToken,"lhsPixel is not of type IMAGE instead found of type"
					+ expressionPixel.firstToken.kind +" at " + expressionPixel.firstToken.posInLine());
		}
		PixelSelector ps=expressionPixel.pixelSelector;
		if(ps!=null) {
			ps.visit(this, null);
		}
		
		expressionPixel.type=Types.Type.INTEGER;
		return null;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws Exception {
		Declaration dec= symbols.lookup(expressionIdent.name);
		if(dec==null) 
			throw new SemanticException(expressionIdent.firstToken, "Variable not declared "+ 
						expressionIdent.firstToken.getText()+ " at "+expressionIdent.firstToken.posInLine());
		
		expressionIdent.type=Types.getType(dec.type);
		
		return null;
	}

	@Override
	public Object visitLHSSample(LHSSample lhsSample, Object arg) throws Exception {
		
		String name=lhsSample.name;
		Declaration dec=symbols.lookup(name);
		if(dec==null) {
			throw new SemanticException(lhsSample.firstToken, "Variable not declared "+ lhsSample.firstToken.getText()+ 
																	" at "+lhsSample.firstToken.posInLine());
			}

		if(Types.getType(dec.type)!=Type.IMAGE) 
		{
			throw new SemanticException(lhsSample.firstToken,"lhsPixel is not of type IMAGE instead found of type"
					+ lhsSample.firstToken.kind +" at " + lhsSample.firstToken.posInLine());
		}
		PixelSelector ps=lhsSample.pixelSelector;
		if(ps!=null) {
			ps.visit(this,null);	
		}
		
		lhsSample.type=Types.Type.INTEGER;
		return null;
		
	}

	@Override
	public Object visitLHSPixel(LHSPixel lhsPixel, Object arg) throws Exception {
		String name=lhsPixel.name;
		Declaration dec=symbols.lookup(name);
		if(dec==null) {
			throw new SemanticException(lhsPixel.firstToken, "Variable not declared "+ lhsPixel.firstToken.getText()+ 
																	" at "+lhsPixel.firstToken.posInLine());
			}

		if(Types.getType(dec.type)!=Type.IMAGE) 
		{
			throw new SemanticException(lhsPixel.firstToken,"lhsPixel is not of type IMAGE instead found of type"
					+ lhsPixel.firstToken.kind +" at " + lhsPixel.firstToken.posInLine());
		}
		PixelSelector ps=lhsPixel.pixelSelector;
		if(ps!=null) {
			ps.visit(this,null);	
		}
		lhsPixel.type=Types.Type.INTEGER;
		return null;
	}

	@Override
	public Object visitLHSIdent(LHSIdent lhsIdent, Object arg) throws Exception {
		String name=lhsIdent.name;
		Declaration dec=symbols.lookup(name);
		if(dec==null) 
			throw new SemanticException(lhsIdent.firstToken, "Variable not declared "+ lhsIdent.firstToken.getText()+ " at "+lhsIdent.firstToken.posInLine());
		lhsIdent.type=Types.getType(dec.type);
		return null;
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg) throws Exception {
		Expression e = statementIf.guard;
		e.visit(this,null);

		if(e.type!= Types.Type.BOOLEAN){
			throw new SemanticException(e.firstToken,"While condition is not of type boolean instead found of type"
					+ e.firstToken.kind +" at " + statementIf.firstToken.posInLine());
		}

		Block block = statementIf.b;
		block.visit(this,null);

		return null;
		
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws Exception {

		Expression e = statementWhile.guard;
		e.visit(this,null);

		if(e.type!= Types.Type.BOOLEAN){
			throw new SemanticException(e.firstToken,"While condition is not of type boolean instead found of type"
					+ e.firstToken.kind +" at " + statementWhile.firstToken.posInLine());
		}

		Block block = statementWhile.b;
		block.visit(this,null);

		return null;
	}

	@Override
	public Object visitStatementSleep(StatementSleep statementSleep, Object arg) throws Exception {
		Expression e = statementSleep.duration;
		e.visit(this,null);
		if(e.type!=Types.Type.INTEGER){
			throw new SemanticException(e.firstToken,"sleep can only be assigned with Integer value but found type "
					+ e.firstToken.kind +" at " + statementSleep.firstToken.posInLine());
		}
		return null;
	}


}
