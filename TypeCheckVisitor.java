package cop5556sp17;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;
import cop5556sp17.AST.*;
import java.util.ArrayList;
import java.util.*;
import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.LinePos;
import cop5556sp17.Scanner.Token;
import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.ARROW;
import static cop5556sp17.Scanner.Kind.KW_HIDE;
import static cop5556sp17.Scanner.Kind.KW_MOVE;
import static cop5556sp17.Scanner.Kind.KW_SHOW;
import static cop5556sp17.Scanner.Kind.KW_XLOC;
import static cop5556sp17.Scanner.Kind.KW_YLOC;
import static cop5556sp17.Scanner.Kind.OP_BLUR;
import static cop5556sp17.Scanner.Kind.OP_CONVOLVE;
import static cop5556sp17.Scanner.Kind.OP_GRAY;
import static cop5556sp17.Scanner.Kind.OP_HEIGHT;
import static cop5556sp17.Scanner.Kind.OP_WIDTH;
import static cop5556sp17.Scanner.Kind.*;

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		TypeCheckException(String message) {
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		binaryChain.getE0().visit(this, null);
		 binaryChain.getE1().visit(this, null);
		 TypeName t0=binaryChain.getE0().getType();
			TypeName t1=binaryChain.getE1().getType();
			Kind o1=binaryChain.getArrow().kind;
			if(t0==URL && t1==IMAGE && o1==ARROW){
				binaryChain.setType(IMAGE);
			}else if(t0==FILE && t1==IMAGE && o1==ARROW){
				binaryChain.setType(IMAGE);
			}
			else if(t0==FRAME && binaryChain.getE1() instanceof FrameOpChain  && o1==ARROW && (binaryChain.getE1().getFirstToken().kind==Kind.KW_XLOC||binaryChain.getE1().getFirstToken().kind==Kind.KW_YLOC)){ 
				binaryChain.setType(INTEGER);
			}
			else if(t0==FRAME && binaryChain.getE1() instanceof FrameOpChain  && o1==ARROW && (binaryChain.getE1().getFirstToken().kind==Kind.KW_SHOW||binaryChain.getE1().getFirstToken().kind==Kind.KW_HIDE||binaryChain.getE1().getFirstToken().kind==Kind.KW_MOVE)){
				binaryChain.setType(FRAME);
			}
			else if(t0==IMAGE && binaryChain.getE1() instanceof ImageOpChain  && o1==ARROW && (binaryChain.getE1().getFirstToken().kind==Kind.OP_WIDTH||binaryChain.getE1().getFirstToken().kind==Kind.OP_HEIGHT)){ 
				binaryChain.setType(INTEGER);
			}
			else if(t0==IMAGE && t1==FRAME && o1==ARROW){
				binaryChain.setType(FRAME);
			}
			else if(t0==IMAGE && t1==FILE && o1==ARROW){
				binaryChain.setType(NONE);
			}
			else if(t0==IMAGE && binaryChain.getE1() instanceof FilterOpChain  && o1==ARROW && (binaryChain.getE1().getFirstToken().kind==Kind.OP_BLUR||binaryChain.getE1().getFirstToken().kind==Kind.OP_GRAY||binaryChain.getE1().getFirstToken().kind==Kind.OP_CONVOLVE)){
				binaryChain.setType(IMAGE);
			}
			else if(t0==IMAGE && binaryChain.getE1() instanceof ImageOpChain  && o1==ARROW && binaryChain.getE1().getFirstToken().kind==Kind.KW_SCALE){
				binaryChain.setType(IMAGE);
			}
			else if(t0==IMAGE &&  o1==ARROW && binaryChain.getE1() instanceof IdentChain && t1==IMAGE){
				binaryChain.setType(IMAGE);
			}
			else if(t0==INTEGER &&  o1==ARROW && binaryChain.getE1() instanceof IdentChain && t1==INTEGER){
				binaryChain.setType(INTEGER);
			}
			else if(t0==IMAGE && binaryChain.getE1() instanceof FilterOpChain  && (o1==Kind.BARARROW) && (binaryChain.getE1().getFirstToken().kind==Kind.OP_GRAY/*||binaryChain.getFirstToken().kind==Kind.OP_CONVOLVE)*/)){
				binaryChain.setType(IMAGE);
			}
			else{
				throw new TypeCheckException( "error");
			}
		return binaryChain.getType();
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		binaryExpression.getE0().visit(this, null);
		binaryExpression.getE1().visit(this, null);
		TypeName t0=binaryExpression.getE0().getType();
		TypeName t1=binaryExpression.getE1().getType();
		Kind o1=binaryExpression.getOp().kind;
		if(t0==INTEGER && t1==INTEGER && (o1==PLUS || o1==MINUS||o1==MOD)){
			binaryExpression.setType(INTEGER);
		}else if(t0==IMAGE && t1==IMAGE && (o1==PLUS || o1==MINUS)){
			binaryExpression.setType(IMAGE);
		}else if(t0==INTEGER && t1==INTEGER && (o1==Kind.TIMES || o1==DIV)){
			binaryExpression.setType(INTEGER);
		}
		else if((( t0==IMAGE && t1==INTEGER )||(t0==INTEGER && t1==IMAGE  )) &&(  o1==Kind.TIMES|| o1==Kind.DIV|| o1==Kind.MOD)){
			binaryExpression.setType(IMAGE);
		}
		else if(t0==INTEGER && t1==INTEGER && (o1==Kind.LT || o1==LE || o1==Kind.GT || o1==GE)){
			binaryExpression.setType(BOOLEAN);
		}
		else if(t0==BOOLEAN && t1==BOOLEAN && (o1==Kind.LT || o1==LE || o1==Kind.GT || o1==GE|| o1==Kind.AND || o1==Kind.OR)){
			binaryExpression.setType(BOOLEAN);
		}
		else if(t0== t1 && (o1==Kind.EQUAL || o1==Kind.NOTEQUAL)){
			binaryExpression.setType(BOOLEAN);
		}else{
			throw new TypeCheckException("Incompatible types");
		}
		
		return binaryExpression.getType();
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO Auto-generated method stub
		symtab.enterScope();
		ArrayList<Dec> alist=  block.getDecs();
		ArrayList<Statement> slist=  block.getStatements();
		for(Dec d:alist)
			d.visit(this, null);
		for(Statement s:slist)
			s.visit(this, null);
		
		symtab.leaveScope();
		
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		booleanLitExpression.setType(BOOLEAN);
		return BOOLEAN;
		
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		filterOpChain.getArg().visit(this, null);
		if (filterOpChain.getArg().getExprList().size() == 0) {
			filterOpChain.setType(TypeName.IMAGE);
			return null;
		}
		throw new TypeCheckException( "error");
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		frameOpChain.getArg().visit(this, null);
		frameOpChain.setKind(frameOpChain.getFirstToken().kind);
		if (frameOpChain.getFirstToken().isKind(KW_SHOW) || frameOpChain.getFirstToken().isKind(KW_HIDE)) {
			if (frameOpChain.getArg().getExprList().size() == 0) {
				frameOpChain.setType(NONE);
				return frameOpChain;
			}
		}
		else if (frameOpChain.getFirstToken().isKind(KW_XLOC) || frameOpChain.getFirstToken().isKind(KW_YLOC)) {
			if (frameOpChain.getArg().getExprList().size() == 0) {
				frameOpChain.setType(INTEGER);
				return frameOpChain;
			}
		} else if (frameOpChain.getFirstToken().isKind(KW_MOVE)) {
			if (frameOpChain.getArg().getExprList().size() == 2) {
				frameOpChain.setType(NONE);
				return frameOpChain;
			}
		}
			throw new TypeCheckException( "error");
		

	
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Dec d = symtab.lookup(identChain.getFirstToken().getText());
		if(d==null) throw new TypeCheckException(identChain.getFirstToken().getText() +" not defined in scope");
		identChain.setType(Type.getTypeName(d.getFirstToken()));
		return d.getType();
	
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Dec d = symtab.lookup(identExpression.getFirstToken().getText());
		if(d==null)
			throw new TypeCheckException(identExpression.getFirstToken().getText() +" not defined in scope");
		identExpression.setDec(d);
		identExpression.setType(Type.getTypeName(d.getFirstToken()));
		return d.getType();
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		ifStatement.getE().visit(this, null);
		ifStatement.getB().visit(this, null);
		if (ifStatement.getE().getType() == BOOLEAN) {
			return BOOLEAN;
		}
		throw new TypeCheckException( "error");
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		intLitExpression.setType(INTEGER);
		return INTEGER;
	
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		sleepStatement.getE().visit(this, null);
		if(sleepStatement.getE().getType()==INTEGER){
			return INTEGER;
		}
		throw new TypeCheckException( "error");
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		whileStatement.getE().visit(this, null);
		whileStatement.getB().visit(this, null);
		if (whileStatement.getE().getType() == BOOLEAN) {
			return BOOLEAN;
		}
		throw new TypeCheckException( "error");
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		// TODO Auto-generated method stub
		symtab.isDec=true;
		if (symtab.lookup(declaration.getIdent().getText()) == null) {
			symtab.insert(declaration.getIdent().getText(), declaration);
			symtab.isDec=false;
			return null;
		}
		symtab.isDec=false;
		throw new TypeCheckException( "error");
}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO Auto-generated method stub
		ArrayList<ParamDec> alist=program.getParams();
		for(ParamDec p:alist)
			p.visit(this, null);
	
		program.getB().visit(this, null);	
			
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		assignStatement.getVar().visit(this, null);
		assignStatement.getE().visit(this, null);
		if(assignStatement.getVar().getType()==assignStatement.getE().getType()){
			return assignStatement.getVar().getType();
		}
		throw new TypeCheckException( "error");
		
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Dec d = symtab.lookup(identX.getFirstToken().getText());
		if(d==null) throw new TypeCheckException( "error");
		identX.setDec(d);
		identX.setType(Type.getTypeName(d.getFirstToken()));
		return d;
		
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		// TODO Auto-generated method stub
		symtab.isDec=true;
		if(symtab.lookup(paramDec.getIdent().getText())==null){
			symtab.insert(paramDec.getIdent().getText(), paramDec);
			symtab.isDec=false;
			return null;
		}
		symtab.isDec=false;
			throw new TypeCheckException( "error");
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		// TODO Auto-generated method stub
		constantExpression.setType(INTEGER);
		return INTEGER;
	
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		imageOpChain.getArg().visit(this, null);
		imageOpChain.setKind(imageOpChain.getFirstToken().kind);
		if (imageOpChain.getFirstToken().isKind(OP_WIDTH) || imageOpChain.getFirstToken().isKind(OP_HEIGHT)) {
			if (imageOpChain.getArg().getExprList().size() == 0) {
				imageOpChain.setType(INTEGER);
				return imageOpChain;
			}
		} else if (imageOpChain.getFirstToken().isKind(KW_SCALE)) {
			if (imageOpChain.getArg().getExprList().size() == 1) {
				imageOpChain.setType(TypeName.IMAGE);
				return imageOpChain;
			}
		}
		throw new TypeCheckException(" error");
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		// TODO Auto-generated method stub
		List<Expression> tl = tuple.getExprList();
		for (Expression e : tl) {
			e.visit(this, null);
			if (e.getType() != INTEGER) {
				throw new TypeCheckException( "error");
			}
		}
		return INTEGER;
	}


}
