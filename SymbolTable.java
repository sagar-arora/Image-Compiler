package cop5556sp18;


import java.util.HashMap;

import cop5556sp18.Types.Type;
import cop5556sp18.AST.Declaration;

import java.util.*;


public class SymbolTable {

	private int currentScope;
	private int nextScope;
	private Stack<Integer> scopeStack ;
	Hashtable<String,List<DecScopeTuple>> symbolTable;

	public SymbolTable() {
		this.symbolTable =  new Hashtable<String, List<DecScopeTuple>>();
		this.currentScope = 0;
		this.nextScope = 0;
		this.scopeStack = new Stack<Integer>();
		getScopeStack().push(0);
	}
	


	public Stack<Integer> getScopeStack() {
		return scopeStack;
	}

	public void setScopeStack(Stack<Integer> scopeStack) {
		this.scopeStack = scopeStack;
	}

	public int getCurrentScope() {
		return currentScope;
	}

	public void setCurrentScope(int currentScope) {
		this.currentScope = currentScope;
	}

	public int getNextScope() {
		return nextScope;
	}

	public void setNextScope(int nextScope) {
		this.nextScope = nextScope;
	}

	public Hashtable<String, List<DecScopeTuple>> getSymbolTable() {
		return symbolTable;
	}

	public void setSymbolTable(Hashtable<String, List<DecScopeTuple>> symbolTable) {
		this.symbolTable = symbolTable;
	}

	class DecScopeTuple {
		private int scope;
		private Declaration dec;

		DecScopeTuple(int scope,Declaration dec){
			this.scope=scope;
			this.dec = dec;
		}
		
		public int getScope() {
			return scope;
		}

		public void setScope(int scope) {
			this.scope = scope;
		}

		public Declaration getDec() {
			return dec;
		}

		public void setDec(Declaration dec) {
			this.dec = dec;
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append(" Scope " + scope).append(" " + dec);
			return sb.toString();
		}
	}
	

	
	public void enterScope(){
		setCurrentScope(++nextScope);
		getScopeStack().push(getCurrentScope());
	}

	public void leaveScope(){
		getScopeStack().pop();
		setCurrentScope(getScopeStack().peek());
	}
	
	public boolean insert(String val, Declaration dec) throws TypeChecker.SemanticException {
		List<DecScopeTuple> list;
		DecScopeTuple decScope = new DecScopeTuple(getCurrentScope(),dec);
		if(!symbolTable.containsKey(val)) {
			list = new LinkedList<DecScopeTuple>();
			list.add(decScope);
		    symbolTable.put(val,list);
		}
		else{
			list = symbolTable.get(val);
			Iterator<DecScopeTuple> iterator = list.listIterator();
			while (iterator.hasNext()){
				DecScopeTuple temp = iterator.next();
				if(temp.scope == currentScope)
				{
				throw new TypeChecker.SemanticException(dec.firstToken,"identifier redecleration not allowed" + decScope.dec.firstToken.getText()+ " in this block " + decScope.dec.firstToken.posInLine());
				}
			}
			list.add(decScope);
		}
		return true;
	}
	
	public Declaration lookup(String val){
		List<DecScopeTuple> list;
		int validScope = getCurrentScope();
		int maxScope = -1;
		Declaration validDecleration=null;
		if(symbolTable.containsKey(val)) {
			list = symbolTable.get(val);
			Iterator<DecScopeTuple> iterator = list.listIterator();
			while (iterator.hasNext()){
				DecScopeTuple decTuple = iterator.next();
				int temp = decTuple.getScope();
				if(temp<=validScope && scopeStack.contains(temp)){
					if(decTuple.getScope()>maxScope){
						maxScope = temp;
						validDecleration = decTuple.getDec();
					}
				}
			}
			return validDecleration;
		}
		return null;
	}
		

	public int lookupScope(String val){
		List<DecScopeTuple> list;
		int validScope = getCurrentScope();
		int maxScope = -1;
		if(symbolTable.containsKey(val)) {
			list = symbolTable.get(val);
			Iterator<DecScopeTuple> iterator = list.listIterator();
			while (iterator.hasNext()){
				DecScopeTuple decTuple = iterator.next();
				int temp = decTuple.getScope();
				if(temp<=validScope && scopeStack.contains(temp)){
					if(decTuple.getScope()>maxScope){
						maxScope = temp;
					}
				}
			}
			return maxScope;
		}
		return -1;
	}
	

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Symbol Table\n");

		for (String key: symbolTable.keySet()) {
			sb.append(key + " " + symbolTable.get(key) + "\n");
		}
		sb.append("The scope of Stack\n").append(scopeStack);

		return sb.toString();
	}
}
