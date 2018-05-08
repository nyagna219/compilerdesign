package cop5556sp17;

import java.util.ArrayList;
import java.util.List;

import cop5556sp17.Scanner.Kind;

public class Scanner {

	
	/**
	 * Kind enum
	 */
	public static enum Kind {
		IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"), 
		KW_IMAGE("image"), KW_URL("url"), KW_FILE("file"), KW_FRAME("frame"), 
		KW_WHILE("while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"), 
		SEMI(";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"), 
		RBRACE("}"), ARROW("->"), BARARROW("|->"), OR("|"), AND("&"), 
		EQUAL("=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(">="), 
		PLUS("+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"), 
		ASSIGN("<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE("convolve"), 
		KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH("screenwidth"), 
		OP_WIDTH("width"), OP_HEIGHT("height"), KW_XLOC("xloc"), KW_YLOC("yloc"), 
		KW_HIDE("hide"), KW_SHOW("show"), KW_MOVE("move"), OP_SLEEP("sleep"), 
		KW_SCALE("scale"), EOF("eof");

		Kind(String text) {
			this.text = text;
		}

		final String text;
		String getText() {
			return text;
		}
		
		
	}
/**
 * Thrown by Scanner when an illegal character is encountered
 */
	public static enum State {
		START("START"), IN_DIGIT("IN_DIGIT"), IN_IDENT("IN_IDENT"), AFTER_EQ("AFTER_EQ"),AFTER_MINUS("AFTER_MINUS"), AFTER_GT("AFTER_GT"),AFTER_LT("AFTER_LT"),AFTER_NOT("AFTER_NOT"), AFTER_OR("AFTER_OR"), AND("AND"), AFTER_DIV("AFTER_DIV"), AFTER_BARMINUS("AFTER_BARMINUS"), COMMENT("COMMENT"), COM_CHECK("COM_CHECK");

		final String text;
		
		State(String text) {
			this.text = text;
		}

		String getText() {
			return text;
		}
	}
	
	
	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception {
		public IllegalCharException(String message) {
			super(message);
		}
	}
	
	/**
	 * Thrown by Scanner when an int literal is not a value that can be represented by an int.
	 */
	@SuppressWarnings("serial")
	public static class IllegalNumberException extends Exception {
	public IllegalNumberException(String message){
		super(message);
		}
	}
	

	/**
	 * Holds the line and position in the line of a token.
	 */
	static class LinePos {
		public final int line;
		public final int posInLine;
		
		public LinePos(int line, int posInLine) {
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString() {
			return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
		}
	}
		

	

	public class Token {
		public final Kind kind;
		public final int pos;  //position in input array
		public final int length;  
		//returns the text of this Token
		
		//these three methods are from assignment3
		@Override
		  public int hashCode() {
		   final int prime = 31;
		   int result = 1;
		   result = prime * result + getOuterType().hashCode();
		   result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		   result = prime * result + length;
		   result = prime * result + pos;
		   return result;
		  }

		  @Override
		  public boolean equals(Object obj) {
		   if (this == obj) {
		    return true;
		   }
		   if (obj == null) {
		    return false;
		   }
		   if (!(obj instanceof Token)) {
		    return false;
		   }
		   Token other = (Token) obj;
		   if (!getOuterType().equals(other.getOuterType())) {
		    return false;
		   }
		   if (kind != other.kind) {
		    return false;
		   }
		   if (length != other.length) {
		    return false;
		   }
		   if (pos != other.pos) {
		    return false;
		   }
		   return true;
		  }

		 

		  private Scanner getOuterType() {
		   return Scanner.this;
		  }
		
		
		public boolean isKind(Kind arg)
		{
			if (this.kind == arg)
				return true;
			return false;

		}
		
		
		
		public String getText() {
			//TODO IMPLEMENT THIS
			return chars.substring(pos, pos + length);
		    }
		
		//returns a LinePos object representing the line and column of this Token
		LinePos getLinePos(){
			//TODO IMPLEMENT THIS
			int p=0;
			for( p=startIndexOfLine.size()-1;p>=0;p--){
				if(pos>=startIndexOfLine.get(p)){
					return new LinePos(p, pos-startIndexOfLine.get(p));
				}
			}
			return new LinePos(startIndexOfLine.size(), 0);
		}

		Token(Kind kind, int pos, int length) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}

		/** 
		 * Precondition:  kind = Kind.INT_LIT,  the text can be represented with a Java int.
		 * Note that the validity of the input should have been checked when the Token was created.
		 * So the exception should never be thrown.
		 * 
		 * @return  int value of this token, which should represent an INT_LIT
		 * @throws NumberFormatException
		 */
		public int intVal() throws NumberFormatException{
			//TODO IMPLEMENT THIS
			if(this.kind==Kind.INT_LIT){
				return Integer.parseInt(chars.substring(this.pos, this.pos+length));
			}
			throw new NumberFormatException();
		
	}

		public Kind getTKind() {
			// TODO Auto-generated method stub
			return this.kind;
		}

		

	
		

	}

	private List<Integer> startIndexOfLine=new ArrayList<Integer>();
	Scanner(String chars) {
		this.chars = chars;
		tokens = new ArrayList<Token>();
	}


	
	/**
	 * Initializes Scanner object by traversing chars and adding tokens to tokens list.
	 * 
	 * @return this scanner
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	
	public Scanner scan() throws IllegalCharException, IllegalNumberException {
		int pos = 0; 
		//TODO IMPLEMENT THIS!!!!
		int length=chars.length();
		int startPos = 0;
		int ch;
		
		State state = State.START;
		while (pos <= length)
		{
			ch = pos < length ? chars.charAt(pos) : -1;
			switch (state)
			{
			case START:
			{
				pos = skipWhiteSpace(pos,length);
				ch = pos < length ? chars.charAt(pos) : -1;
				startPos = pos;
				switch (ch)
				{
			case -1:
			{
				tokens.add(new Token(Kind.EOF, pos, 0));
				pos++;
			}  break;
				
			case '+':
			{
				tokens.add(new Token(Kind.PLUS, startPos, 1));
				pos++;
			} break;
			case '-':
			{
				state = State.AFTER_MINUS;
				pos++;
			} break;
			case '|':
			{
				state = State.AFTER_OR;
				pos++;
				} break;
			case '&':
			{
				tokens.add(new Token(Kind.AND, startPos, 1));
				pos++;
			} break;
			case '/':
			{
				state = State.AFTER_DIV;
				pos++;
			} break;
			case '%':
			{
				tokens.add(new Token(Kind.MOD, startPos, 1));
				pos++;
			} break;
			
			case '*':
			{
				tokens.add(new Token(Kind.TIMES, startPos, 1));
				pos++;
			} break;
			case '=':
			{
				state = State.AFTER_EQ;
				pos++;
			} break;
			case '!':
			{
				state = State.AFTER_NOT;
				pos++;
			} break;
			case '>':
			{
				state = State.AFTER_GT;
				pos++;
			
			} break;
			case '<':
			{
				state = State.AFTER_LT;
				pos++;
			} break;
			case ';':
			{
				tokens.add(new Token(Kind.SEMI,startPos, 1));
				pos++;
			} break;
			case '{':
			{
				tokens.add(new Token(Kind.LBRACE,startPos, 1));
				pos++;
			}break;
			case '}':
			{
				tokens.add(new Token(Kind.RBRACE,startPos, 1));
				pos++;
			}break;
			case '(':
			{
				tokens.add(new Token(Kind.LPAREN,startPos, 1));
				pos++;
			}break;
			case ')':
			{
				tokens.add(new Token(Kind.RPAREN,startPos, 1));
				pos++;
			}break;
			case ',':
			{
				tokens.add(new Token(Kind.COMMA,startPos, 1));
				pos++;
			}break;
			case '0':
			{
				tokens.add(new Token(Kind.INT_LIT,startPos, 1));
				pos++;
			}break;
			default:
			{
				if (Character.isDigit(ch))
				{
					state = State.IN_DIGIT;
					pos++;
				} else if (Character.isJavaIdentifierStart(ch))
				{
				state = State.IN_IDENT;
				pos++;
				} else 
				{
					throw new IllegalCharException( "illegal char " +ch+" at pos "+pos); } }
				} // switch (ch)
			}  break;
			case IN_DIGIT:
			{
				if (Character.isDigit(ch)) {
					pos++;
				} else {
					try {
						Integer.parseInt(chars.substring(startPos, pos));
					} catch (NumberFormatException e) {
						throw new IllegalNumberException(
								chars.substring(startPos, pos) + " is not an integer");
					}
					tokens.add(new Token(Kind.INT_LIT, startPos, pos - startPos));
					state = State.START;
				}
			}  break;
			case IN_IDENT:
			{
				if(Character.isJavaIdentifierPart(ch))
				pos++;
			  
				else
				 if(chars.substring(startPos, pos).equals("integer"))
			    {
			    	tokens.add(new Token(Kind.KW_INTEGER, startPos, pos - startPos));	
			    	state = State.START;
			    }	
			    else if(chars.substring(startPos, pos).equals("boolean"))
			    {
			    	tokens.add(new Token(Kind.KW_BOOLEAN, startPos, pos - startPos));	
			    	state = State.START;
			    }
			    else if(chars.substring(startPos, pos).equals("image"))
			    {
			    	tokens.add(new Token(Kind.KW_IMAGE, startPos, pos - startPos));	
			    	state = State.START;
			    }
			    else if(chars.substring(startPos, pos).equals("url"))
			    {
			    	tokens.add(new Token(Kind.KW_URL, startPos, pos - startPos));	
			    	state = State.START;
			    }
			    else if(chars.substring(startPos, pos).equals("file"))
			    {
			    	tokens.add(new Token(Kind.KW_FILE, startPos, pos - startPos));	
			    	state = State.START;
			    }	
			    else if(chars.substring(startPos, pos).equals("frame"))
			    {
			    	tokens.add(new Token(Kind.KW_FRAME, startPos, pos - startPos));	
			    	state = State.START;
			    }	
			    else if(chars.substring(startPos, pos).equals("while"))
			    {
			    	tokens.add(new Token(Kind.KW_WHILE, startPos, pos - startPos));	
			    	state = State.START;
			    }	
			    else if(chars.substring(startPos, pos).equals("if"))
			    {
			    	tokens.add(new Token(Kind.KW_IF, startPos, pos - startPos));	
			    	state = State.START;
			    }	
			    else if(chars.substring(startPos, pos).equals("true"))
			    {
			    	tokens.add(new Token(Kind.KW_TRUE, startPos, pos - startPos));	
			    	state = State.START;
			    }	
			    else if(chars.substring(startPos, pos).equals("false"))
			    {
			    	tokens.add(new Token(Kind.KW_FALSE, startPos, pos - startPos));	
			    	state = State.START;
			    }	
			    else if(chars.substring(startPos, pos).equals("screenheight"))
			    {
			    	tokens.add(new Token(Kind.KW_SCREENHEIGHT, startPos, pos - startPos));	
			    	state = State.START;
			    }
			    else if(chars.substring(startPos, pos).equals("screenwidth"))
			    {
			    	tokens.add(new Token(Kind.KW_SCREENWIDTH, startPos, pos - startPos));	
			    	state = State.START;
			    }
			    else if(chars.substring(startPos, pos).equals("blur"))
			    {
			    	tokens.add(new Token(Kind.OP_BLUR, startPos, pos - startPos));	
			    	state = State.START;
			    }
			    else if(chars.substring(startPos, pos).equals("gray"))
			    {
			    	tokens.add(new Token(Kind.OP_GRAY, startPos, pos - startPos));	
			    	state = State.START;
			    }
			    else if(chars.substring(startPos, pos).equals("convolve"))
			    {
			    	tokens.add(new Token(Kind.OP_CONVOLVE, startPos, pos - startPos));	
			    	state = State.START;
			    }
			    else if(chars.substring(startPos, pos).equals("width"))
			    {
			    	tokens.add(new Token(Kind.OP_WIDTH, startPos, pos - startPos));	
			    	state = State.START;
			    }
			    else if(chars.substring(startPos, pos).equals("height"))
			    {
			    	tokens.add(new Token(Kind.OP_HEIGHT, startPos, pos - startPos));	
			    	state = State.START;
			    }
			    else if(chars.substring(startPos, pos).equals("xloc"))
			    {
			    	tokens.add(new Token(Kind.KW_XLOC, startPos, pos - startPos));	
			    	state = State.START;
			    }
			    else if(chars.substring(startPos, pos).equals("yloc"))
			    {
			    	tokens.add(new Token(Kind.KW_YLOC, startPos, pos - startPos));	
			    	state = State.START;
			    }
			    else if(chars.substring(startPos, pos).equals("hide"))
			    {
			    	tokens.add(new Token(Kind.KW_HIDE, startPos, pos - startPos));	
			    	state = State.START;
			    }
			    else if(chars.substring(startPos, pos).equals("show"))
			    {
			    	tokens.add(new Token(Kind.KW_SHOW, startPos, pos - startPos));	
			    	state = State.START;
			    }
			    else if(chars.substring(startPos, pos).equals("move"))
			    {
			    	tokens.add(new Token(Kind.KW_MOVE, startPos, pos - startPos));	
			    	state = State.START;
			    }
			    else if(chars.substring(startPos, pos).equals("sleep"))
			    {
			    	tokens.add(new Token(Kind.OP_SLEEP, startPos, pos - startPos));	
			    	state = State.START;
			    }
			    else if(chars.substring(startPos, pos).equals("scale"))
			    {
			    	tokens.add(new Token(Kind.KW_SCALE, startPos, pos - startPos));	
			    	state = State.START;
			    }
			    else /*if(chars.substring(startPos, pos).equals("eof")
			    {
			    	tokens.add(new Token(Kind.EOF, startPos, pos - startPos));	
			    	state = State.START;
			    }
			    else	*/											
			{
			tokens.add(new Token(Kind.IDENT, startPos, pos - startPos));
			state = State.START;
			}
			
				}  break;
			case AFTER_EQ:
			{
				if (ch == '=') {
					tokens.add(new Token(Kind.EQUAL, startPos, 2));
					state = State.START;
					pos++;
				}else{
					throw new IllegalCharException("Invalid Equal Token");
				}
			}  break;
			case AFTER_MINUS: {
				if (ch == '>') {
					tokens.add(new Token(Kind.ARROW, startPos, 2));
					pos++;
				} else {
					tokens.add(new Token(Kind.MINUS, startPos, 1));
				}
				state = State.START;
			}
				break;
			case AFTER_GT: {
				if (ch == '=') {
					tokens.add(new Token(Kind.GE, startPos, 2));
					pos++;
				} else {
					tokens.add(new Token(Kind.GT, startPos, 1));
				}
				state = State.START;
			}
				break;
			case AFTER_LT: {
				if (ch == '=') {
					tokens.add(new Token(Kind.LE, startPos, 2));
					pos++;
				} else if (ch == '-') {
					tokens.add(new Token(Kind.ASSIGN, startPos, 2));
					pos++;
				} else {
					tokens.add(new Token(Kind.LT, startPos, 1));
				}
				state = State.START;
			}
				break;
			case AFTER_NOT: {
				if (ch == '=') {
					tokens.add(new Token(Kind.NOTEQUAL, startPos, 2));
					pos++;
				} else {
					tokens.add(new Token(Kind.NOT, startPos, 1));
				}
				state = State.START;
			}
				break;
			case AFTER_OR: {
				if (ch == '-') {
					state=State.AFTER_BARMINUS;
					pos++;
				} else {
					tokens.add(new Token(Kind.OR, startPos, 1));
					state = State.START;
				}
			}
				break;
			case AFTER_BARMINUS: {
				if (ch == '>') {
					tokens.add(new Token(Kind.BARARROW, startPos, 3));
					pos++;
				} else {
					tokens.add(new Token(Kind.OR, startPos, 1));
					tokens.add(new Token(Kind.MINUS, startPos+1, 1));
				}
				state = State.START;
			}
				break;
			case AFTER_DIV: {
				if (ch == '*') {
					state=State.COMMENT;
					pos++;
				} else {
					tokens.add(new Token(Kind.DIV, startPos, 1));
					state = State.START;
				}
			}
				break;
			case COMMENT: {
				if (pos<length && 10 == Character.codePointAt(chars, pos)) {
					startIndexOfLine.add( pos);
				}
				else if (ch == '*') {
					state=State.COM_CHECK;
				}
				pos++;
			}
				break;
			case COM_CHECK: {
				if (ch == '/') {
					state = State.START;
				} else if (ch == '*') {
					state = State.COM_CHECK;
				} else {
					state = State.COMMENT;
				}
				pos++;
			}
				break;
			default:
			assert false;
			}// switch(state)
			} // while return this;
		//tokens.add(new Token(Kind.EOF,pos,0));
		return this;  
	}



	/*public int skipWhiteSpace(int pos) {
		// TODO Auto-generated method stub
		while(chars.charAt(pos)==' ')
	 		   pos++;
		return 0;
	}*/

	private int skipWhiteSpace(int pos, int length) {
		while (pos < length && Character.isWhitespace(chars.charAt(pos))) {
			if (Character.codePointAt(chars, pos) == 10) {
				startIndexOfLine.add(pos);
			}
			pos++;
		}
		return pos;
	}

	final ArrayList<Token> tokens;
	final String chars;
	int tokenNum;

	/*
	 * Return the next token in the token list and update the state so that
	 * the next call will return the Token..  
	 */
	public Token nextToken() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum++);
	}
	
	/*
	 * Return the next token in the token list without updating the state.
	 * (So the following call to next will return the same token.)
	 */
	public Token peek(){
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum);		
	}

	

	/**
	 * Returns a LinePos object containing the line and position in line of the 
	 * given token.  
	 * 
	 * Line numbers start counting at 0
	 * 
	 * @param t
	 * @return
	 */
	public LinePos getLinePos(Token t) {
		//TODO IMPLEMENT THIS
		return t.getLinePos();
		
		
		
		
	}


	
}