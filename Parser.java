package cop5556sp17;

import cop5556sp17.Scanner.Kind;
import static cop5556sp17.Scanner.Kind.*;

import java.util.ArrayList;
import java.util.List;

import cop5556sp17.Scanner.Token;
import cop5556sp17.Scanner.*;
import cop5556sp17.AST.*;
public class Parser {

	/**
	 * Exception to be thrown if a syntax error is detected in the input.
	 * You will want to provide a useful error message.
	 *
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}

    int count=0;
	Scanner scanner;
	Token t;
    Token t1;
	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * parse the input using tokens from the scanner.
	 * Check for EOF (i.e. no trailing junk) when finished
	 * 
	 * @throws SyntaxException
	 */
	ASTNode parse() throws SyntaxException {
		ASTNode n;
		n=program();
		matchEOF();
		return n;
	}

	Expression expression() throws SyntaxException {
		//TODO
		Expression e1,e2;
		Token token=t,op;
		e1=term();
			while(t.isKind(LT)||t.isKind(LE)||t.isKind(GT)||t.isKind(GE)||t.isKind(EQUAL)||t.isKind(NOTEQUAL)){
				op=relOp();
				e2=term();
				e1=new BinaryExpression(token, e1, op, e2);
			}
			return e1;
	}
Token relOp() throws SyntaxException{
	Token token=t;
	if(t.isKind(LT))
		match(LT);
	else if(t.isKind(LE))
		match(LE);
	else if(t.isKind(GT))
		match(GT);
	else if(t.isKind(GE))
		match(GE);
	else if(t.isKind(EQUAL))
		match(EQUAL);
	else if(t.isKind(NOTEQUAL))
		match(NOTEQUAL);
	else
		throw new SyntaxException("illegal factor");
        return token;
}

	Expression term() throws SyntaxException {
		// TODO
		Expression e1,e2;
		Token token=t,op;
		e1=elem();
		while (t.isKind(PLUS) || t.isKind(MINUS) || t.isKind(OR)) {
			op=weakOp();
			e2=elem();
			e1=new BinaryExpression(token, e1, op, e2);
		}
		return e1;
	}

	Token weakOp() throws SyntaxException{
		Token token=t;
		if(t.isKind(PLUS))
			match(PLUS);
		else if(t.isKind(MINUS))
			match(MINUS);
		else if(t.isKind(OR))
			match(OR);
		else
			throw new SyntaxException("illegal factor");
	return token;
	}
	
	Expression elem() throws SyntaxException {
		// TODO
		Expression e1,e2;
		Token token=t,op;
		e1=factor();

		while (t.isKind(TIMES) || t.isKind(DIV) || t.isKind(AND) || t.isKind(MOD)) {
			op=strongOp();
			e2=factor();
		e1=new BinaryExpression(token, e1, op, e2);
		}
		return e1;
	}

	Token strongOp() throws SyntaxException{
		Token token=t;
		if(t.isKind(TIMES))
			match(TIMES);
		else if(t.isKind(DIV))
			match(DIV);
		else if(t.isKind(AND))
			match(AND);
		else if(t.isKind(MOD))
			match(MOD);
		else
			throw new SyntaxException("illegal factor");
	return token;
	}
	
	
	Expression factor() throws SyntaxException {
		Token token=t;
		Kind kind = t.kind;
		Expression e=null;
		switch (kind) {
		case IDENT: {
			e=new IdentExpression(token);
			consume();
		}
			break;
		case INT_LIT: {
			e=new IntLitExpression(token);		
			consume();
		}
			break;
		case KW_TRUE:
		case KW_FALSE: {
			e=new BooleanLitExpression(token);
			consume();
		}
			break;
		case KW_SCREENWIDTH:
		case KW_SCREENHEIGHT: {
			e=new ConstantExpression(token);
			consume();
		}
			break;
		case LPAREN: {
			consume();
			e=expression();
			match(RPAREN);
		}
			break;
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal factor");
		}
		return e;
	}

	Block block() throws SyntaxException {
		//TODO
		Token token=t;
		Token op=token;
		ArrayList<Dec> decs=new ArrayList<Dec>();
		ArrayList<Statement> statements=new ArrayList<Statement>();
		if(t.isKind(LBRACE))
		{
			match(LBRACE);
			boolean desc = t.isKind(KW_INTEGER)||t.isKind(KW_BOOLEAN)||t.isKind(KW_IMAGE)||t.isKind(KW_FRAME);
			boolean statement = t.isKind(OP_SLEEP)||t.isKind(KW_WHILE)||t.isKind(KW_IF)||t.isKind(IDENT)||t.isKind(OP_BLUR)||t.isKind(OP_GRAY)||t.isKind(OP_CONVOLVE)||t.isKind(KW_SHOW)||t.isKind(KW_HIDE)||t.isKind(KW_MOVE)||t.isKind(KW_XLOC)||t.isKind(KW_YLOC)||t.isKind(OP_HEIGHT)||t.isKind(OP_WIDTH)||t.isKind(KW_SCALE);
			while(desc||statement)
			{
				if(desc)
				{
				decs.add(dec());
				}
				else if(statement)
				{
					statements.add(statement());
				}
				 desc = t.isKind(KW_INTEGER)||t.isKind(KW_BOOLEAN)||t.isKind(KW_IMAGE)||t.isKind(KW_FRAME);
				 statement = t.isKind(OP_SLEEP)||t.isKind(KW_WHILE)||t.isKind(KW_IF)||t.isKind(IDENT)||t.isKind(OP_BLUR)||t.isKind(OP_GRAY)||t.isKind(OP_CONVOLVE)||t.isKind(KW_SHOW)||t.isKind(KW_HIDE)||t.isKind(KW_MOVE)||t.isKind(KW_XLOC)||t.isKind(KW_YLOC)||t.isKind(OP_HEIGHT)||t.isKind(OP_WIDTH)||t.isKind(KW_SCALE);
			}
			match(RBRACE);
		}else{
			throw new SyntaxException("Incorrect Block");
		}
		return new Block(op, decs, statements);
	}

	Program program() throws SyntaxException {
		//TODO
		Token token=t;
		ArrayList<ParamDec> list=new ArrayList<ParamDec>();
		Block b = null;
		if(t.isKind(IDENT))
			{
			t1=scanner.peek();
			if(t1.isKind(LBRACE)){
				match(IDENT);
				b=block();
			
			}
			else if(t1.isKind(KW_URL)||t1.isKind(KW_FILE)||t1.isKind(KW_INTEGER)||t1.isKind(KW_BOOLEAN))
			{
			match(IDENT);
			list.add(param_dec());
			while (t.isKind(COMMA)) {
				match(COMMA);
				list.add(param_dec());
			}
			b=block();
			}
			}else{
				throw new SyntaxException("Incorrect Program");
			}
	return new Program(token, list, b);
	}

	/*void programextension() throws SyntaxException {
		//TODO
if(t.isKind(KW_URL)||t.isKind(KW_FILE)||t.isKind(KW_INTEGER)||t.isKind(KW_BOOLEAN))
		{
			param_dec();
			while (t.isKind(COMMA)) {
				match(COMMA);
				param_dec();
			}
		}
	}*/
	
	ParamDec param_dec() throws SyntaxException {
		//TODO
		Token token=t;
		Token ident;
		ParamDec d=null;
		if(t.isKind(KW_URL)||t.isKind(KW_FILE)||t.isKind(KW_INTEGER)||t.isKind(KW_BOOLEAN))
		{
		consume();
		ident=t;
		match(IDENT);
		d=new ParamDec(token,ident);
		}
		return d;
	}

	Dec dec() throws SyntaxException {
		//TODO
		Token token=t;
		Token ident;
		Dec d=null;
		if(t.isKind(KW_INTEGER)||t.isKind(KW_BOOLEAN)||t.isKind(KW_IMAGE)||t.isKind(KW_FRAME))
		{
		consume();
		ident=t;
		match(IDENT);
		d=new Dec(token,ident);
		}
		return d;
	}

	Statement statement() throws SyntaxException {
		//TODO
		Token token=t;
		Expression e=null;
		Statement s;
		if(t.isKind(OP_SLEEP))
			{
			match(OP_SLEEP);
			e=expression();
			s=new SleepStatement(token, e);
			match(SEMI);
			}
		else if(t.isKind(KW_WHILE))
		   {
			s=whileStatement();
		   }
		else if(t.isKind(KW_IF))
		   {
			s=ifStatement();
	    	}
		else if(t.isKind(IDENT) && scanner.peek().isKind(ASSIGN))
		{
			
			s=assign();
			match(SEMI);
		}
		else if(t.isKind(IDENT)||t.isKind(OP_BLUR )||t.isKind(OP_GRAY) ||t.isKind(OP_CONVOLVE)||t.isKind(KW_SHOW) || t.isKind(KW_HIDE)|| t.isKind(KW_MOVE)||t.isKind(KW_XLOC)||t.isKind(KW_YLOC)||t.isKind(OP_WIDTH)||t.isKind(OP_HEIGHT)||t.isKind(KW_SCALE))
		{
			s=chain();
			match(SEMI);
		}else throw new SyntaxException("illegal factor");
return s;
	}

	WhileStatement whileStatement() throws SyntaxException {
		Token token=t;
		Expression e=null;
		Block b=null;
		WhileStatement w=null;
		if(t.isKind(KW_WHILE))
		{
			match(KW_WHILE);
			match(LPAREN);
			e=expression();
			match(RPAREN);
			b=block();
		w=new WhileStatement(token, e, b);
		}return w;
	} 
		
	IfStatement ifStatement() throws SyntaxException {
		Token token=t;
		Expression e=null;
		Block b=null;
		if(t.isKind(KW_IF))
		{
			match(KW_IF);
			match(LPAREN);
			e=expression();
			match(RPAREN);
			b=block();
	
		}
		return new IfStatement(token, e, b);
	} 
	
	AssignmentStatement assign() throws SyntaxException {
		Token token=t;
		IdentLValue lvalue=new IdentLValue(token);
		AssignmentStatement a=null;
		Expression e=null;
		if(t.isKind(IDENT))
		{
			t1=scanner.peek();
			if(t1.isKind(ASSIGN))
			{		
			match(IDENT);
			match(ASSIGN);
			e=expression();
			a=new AssignmentStatement(token, lvalue, e);
			}
		}else
			throw new SyntaxException("illegal factor");
		return a;
		
	} 
	
	Chain chain() throws SyntaxException {
		//TODO
		Token token=t;
		Token op;
		ChainElem c2;
		Chain c1;
		if(t.isKind(IDENT)||t.isKind(OP_BLUR )||t.isKind(OP_GRAY) ||t.isKind(OP_CONVOLVE)||t.isKind(KW_SHOW) || t.isKind(KW_HIDE)|| t.isKind(KW_MOVE)||t.isKind(KW_XLOC)||t.isKind(KW_YLOC)||t.isKind(OP_WIDTH)||t.isKind(OP_HEIGHT)||t.isKind(KW_SCALE))
		{
			c1=chainElem();
			op=arrowOp();
			c2=chainElem();
			c1=new BinaryChain(token, c1, op, c2);
			while(t.isKind(ARROW)||t.isKind(BARARROW))
			{
				op=arrowOp();
				c2=chainElem();
				c1=new BinaryChain(token, c1, op, c2);
			}
		}else
			throw new SyntaxException("illegal factor");
	return c1;
	}

	Token arrowOp() throws SyntaxException {
		Token token=t;
		if(t.isKind(ARROW))
		match(ARROW);
			else	
			if(t.isKind(BARARROW))	
			match(BARARROW);
			else
				throw new SyntaxException("illegal factor");
	return token;
	} 
	
	ChainElem chainElem() throws SyntaxException {
		//TODO
		Token token=t,op;
		ChainElem c=null;
		Tuple t1;
		if(t.isKind(IDENT))
			{
			c=new IdentChain(token);
			match(IDENT);
			}
		else if(t.isKind(OP_BLUR )||t.isKind(OP_GRAY) ||t.isKind(OP_CONVOLVE))
				{
			op=filterOp();
			t1=arg();
				c= new FilterOpChain(op, t1);
				}
		else if(t.isKind(KW_SHOW) || t.isKind(KW_HIDE)|| t.isKind(KW_MOVE)||t.isKind(KW_XLOC)||t.isKind(KW_YLOC))
				{
			op=frameOp();
			t1=arg();
				c=new FrameOpChain(op, t1);
				}
		else if(t.isKind(OP_WIDTH)||t.isKind(OP_HEIGHT)||t.isKind(KW_SCALE))
				{
			op=imageOp();
			t1=arg();
				c=new ImageOpChain(op, t1);
				}
		else
			throw new SyntaxException("illegal factor");
	       return c;
	}
	

	Token filterOp() throws SyntaxException {
		Token token=t;
		if(t.isKind(OP_BLUR))
		match(OP_BLUR);
		else	
		if(t.isKind(OP_GRAY))	
		match(OP_GRAY);
		else	
		if(t.isKind(OP_CONVOLVE))	
		match(OP_CONVOLVE);
		else
			throw new SyntaxException("illegal factor");
	return token;
	} 
	
	Token frameOp() throws SyntaxException {
		Token token=t;
		if(t.isKind(KW_SHOW))
		match(KW_SHOW);
		else	
		if(t.isKind(KW_HIDE))	
		match(KW_HIDE);
		else	
		if(t.isKind(KW_MOVE))	
		match(KW_MOVE);
		else
		if(t.isKind(KW_XLOC))
		match(KW_XLOC);
		else
		if(t.isKind(KW_YLOC))
		match(KW_YLOC);
		else
			throw new SyntaxException("illegal factor");
	return token;
	} 
	
	Token imageOp() throws SyntaxException {
		Token token=t;
		if(t.isKind(OP_WIDTH ))
		match(OP_WIDTH );
		else	
		if(t.isKind(OP_HEIGHT ))	
		match(OP_HEIGHT );
		else	
		if(t.isKind(KW_SCALE))	
		match(KW_SCALE);
		else
			throw new SyntaxException("illegal factor");
	  return token;
	} 
	
	
	Tuple arg() throws SyntaxException {
		//TODO
		Tuple t1;
		Token token=t;
		List<Expression> list=new ArrayList<Expression>();
		if(t.isKind(LPAREN))
		{
			match(LPAREN);
			list.add(expression());
			while(t.isKind(COMMA))
			{
				match(COMMA);
				list.add(expression());
			}
			match(RPAREN);
		}
		return new Tuple(token, list);
	}

	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.isKind(EOF)) {
			return t;
		}
		throw new SyntaxException("expected EOF");
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		if (t.isKind(kind)) {
			return consume();
		}
		throw new SyntaxException("saw " + t.kind + "expected " + kind);
	}

	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * current token is consumed and returned. If not, a SyntaxException is
	 * thrown.
	 * 
	 * * Precondition: for all given kinds, kind != EOF
	 * 
	 * @param kinds
	 *            list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind... kinds) throws SyntaxException {
		// TODO. Optional but handy
		
		for(Kind k: kinds)
		{
			if(t.kind==k)
			{
				
				return consume();  			
			}
		}
		
		throw new SyntaxException("saw " + t.kind );	
		//return null; //replace this statement
	}

	/**
	 * Gets the next token and returns the consumed token.
	 * 
	 * Precondition: t.kind != EOF
	 * 
	 * @return
	 * 
	 */
	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}

}
