package cop5556sp17;



import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;

import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Type;
import cop5556sp17.*;

public class SymbolTable {
	
	
	//TODO  add fields

	/** 
	 * to be called when block entered
	 */
	boolean isDec=false;
	
   Stack<Integer> ss=new Stack<Integer>();
   Hashtable<String, Symboltableentry> store = new Hashtable<String, Symboltableentry>();
   int  cur=0;
   int next=1;
   public void enterScope(){
		//TODO:  IMPLEMENT THIS
		cur = next++; 
		ss.push(cur);
	}
	
	
	/**
	 * leaves scope
	 */
	public void leaveScope(){
		//TODO:  IMPLEMENT THIS
		ss.pop();
		if (ss.isEmpty())
			cur = 0;
		else
			cur = ss.peek();
	}
	
	public boolean insert(String ident, Dec dec){
		//TODO:  IMPLEMENT THIS
		Symboltableentry s = store.get(ident);
		while (s != null) {
			if (s.sc == cur) {
				return false;
			}
			s = s.next;
		}
		store.put(ident, new Symboltableentry(cur, dec, store.get(ident)));
		return true;
	}
	
	public Dec lookup(String ident){
		//TODO:  IMPLEMENT THIS
		Symboltableentry s1;
		Symboltableentry s = store.get(ident);
		if (s == null)
			return null;
		
		for (int i = ss.size() - 1; i >= 0; i--) {
			s1 = s;
			int scope = ss.get(i);
			while (s1 != null && s1.sc != scope) {
				s1 = s1.next;
			}
			if (s1 != null)
				return s1.d;
			if(isDec) break;
		}
		return null;
	
	}
		
	@Override
	public String toString() {
		//TODO:  IMPLEMENT THIS
		return store.toString();
	}
	
	public SymbolTable() {
		enterScope();
	}

}
class Symboltableentry {
	int sc;
	Dec d;
	Symboltableentry next;

	public Symboltableentry(int sc, Dec d, Symboltableentry next) {
		super();
		this.sc = sc;
		this.d = d;
		this.next = next;
	}

}

