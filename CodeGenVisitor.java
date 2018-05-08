package cop5556sp17;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.ASTVisitor;
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
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import static cop5556sp17.AST.Type.TypeName.FRAME;
import static cop5556sp17.AST.Type.TypeName.IMAGE;
import static cop5556sp17.AST.Type.TypeName.URL;
import static cop5556sp17.Scanner.Kind.*;

class DecLocal{
	String id;
	String t;
	Label b1;
	Label b2;
	int sno;
	public DecLocal(Label b1, Label b2, String id, String t, int sno) {
		super();
		this.b1 = b1;
		this.b2 = b2;
		this.id = id;
		this.t = t;
		this.sno = sno;
	}
}
public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	int count=0;
	int locCount=1;
	List<DecLocal> loc=new ArrayList<DecLocal>();
	
	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.getName();
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
				new String[] { "java/lang/Runnable" });
		cw.visitSource(sourceFileName, null);

		// generate constructor code
		// get a MethodVisitor
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		// Create label at start of code
		Label constructorStart = new Label();
		mv.visitLabel(constructorStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		// generate code to call superclass constructor
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		// visit parameter decs to add each as field to the class
		// pass in mv so decs can add their initialization code to the
		// constructor.
		ArrayList<ParamDec> params = program.getParams();
		for (ParamDec dec : params)
			dec.visit(this, mv);
		mv.visitInsn(RETURN);
		// create label at end of code
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		// finish up by visiting local vars of constructor
		// the fourth and fifth arguments are the region of code where the local
		// variable is defined as represented by the labels we inserted.
		mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
		// indicates the max stack size for the method.
		// because we used the COMPUTE_FRAMES parameter in the classwriter
		// constructor, asm
		// will do this for us. The parameters to visitMaxs don't matter, but
		// the method must
		// be called.
		mv.visitMaxs(1, 1);
		// finish up code generation for this method.
		mv.visitEnd();
		// end of constructor

		// create main method which does the following
		// 1. instantiate an instance of the class being generated, passing the
		// String[] with command line arguments
		// 2. invoke the run method.
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		// create run method
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		mv.visitCode();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		program.getB().visit(this, null);
		mv.visitInsn(RETURN);
		Label endRun = new Label();
		mv.visitLabel(endRun);
		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
//TODO  visit the local variables
		for(DecLocal decl:loc){
            mv.visitLocalVariable(decl.id, decl.t, null, decl.b1, decl.b2, decl.sno);
        }
		mv.visitMaxs(1, 1);
		mv.visitEnd(); // end of run method
		cw.visitEnd();//end of class
	//generate classfile and return it
		return cw.toByteArray();
	}



	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		assignStatement.getE().visit(this, arg);
		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getType());
		assignStatement.getVar().visit(this, arg);
		return null;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		binaryChain.getE0().visit(this, true);
		 if(binaryChain.getE0().getType().equals(TypeName.URL))
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "readFromURL", "(Ljava/net/URL;)Ljava/awt/image/BufferedImage;", false);	
		 if(binaryChain.getE0().getType().equals(TypeName.FILE))
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "readFromFile", "(Ljava/io/File;)Ljava/awt/image/BufferedImage;", false);
		  if(binaryChain.getArrow().isKind(BARARROW)){
			  binaryChain.getE1().setBarArrow(true);
		  }
		binaryChain.getE1().visit(this, false);
		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
      //TODO  Implement this
		if(binaryExpression.getType()==TypeName.INTEGER)
		{
			binaryExpression.getE0().visit(this, arg); 
			binaryExpression.getE1().visit(this, arg); 
			Label l1 = new Label();
			if(binaryExpression.getOp().isKind(Kind.PLUS))
					mv.visitInsn(IADD);
			if(binaryExpression.getOp().isKind(Kind.MINUS))
					mv.visitInsn(ISUB);			
			if(binaryExpression.getOp().isKind(Kind.TIMES))
					mv.visitInsn(IMUL);
			if(binaryExpression.getOp().isKind(Kind.DIV))
					mv.visitInsn(IDIV);
			if(binaryExpression.getOp().isKind(Kind.MOD))
					mv.visitInsn(IREM);
			if(binaryExpression.getOp().isKind(Kind.AND))
					mv.visitInsn(IAND);
			if(binaryExpression.getOp().isKind(Kind.OR))
					mv.visitInsn(IOR);
			if(binaryExpression.getOp().isKind(Kind.LT)){
				mv.visitJumpInsn(IF_ICMPLT, l1);
			mv.visitInsn(ICONST_0);
			Label l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(l2);
			}
			if(binaryExpression.getOp().isKind(Kind.GT))
			{	
				mv.visitJumpInsn(IF_ICMPGT, l1);
			mv.visitInsn(ICONST_0);
			Label l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(l2);
			}		
			if(binaryExpression.getOp().isKind(Kind.LE))
			{
				mv.visitJumpInsn(IF_ICMPLE, l1);
			mv.visitInsn(ICONST_0);
			Label l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(l2);
			}
			if(binaryExpression.getOp().isKind(Kind.GE))
			{	
					mv.visitJumpInsn(IF_ICMPGE, l1);
				mv.visitInsn(ICONST_0);
				Label l2 = new Label();
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(l2);
			}
		}
		else if(binaryExpression.getType()==TypeName.IMAGE)
		{
				if(binaryExpression.getOp().isKind(PLUS)){
					binaryExpression.getE0().visit(this, arg);
					binaryExpression.getE1().visit(this, arg);
					mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "add", "(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
				}
				if(binaryExpression.getOp().isKind(MINUS)){
					binaryExpression.getE0().visit(this, arg);
					binaryExpression.getE1().visit(this, arg);
					mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "sub", "(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
				}
				if(binaryExpression.getOp().isKind(TIMES)){
					if(binaryExpression.getE0().getType().equals(INTEGER)){
						binaryExpression.getE1().visit(this, arg);
						binaryExpression.getE0().visit(this, arg); 
					}else{
						binaryExpression.getE0().visit(this, arg);
						binaryExpression.getE1().visit(this, arg); 
					}
					mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "mul", "(Ljava/awt/image/BufferedImage;I)Ljava/awt/image/BufferedImage;", false);
				}
				if(binaryExpression.getOp().isKind(DIV)){
					if(binaryExpression.getE0().getType().equals(INTEGER)){
						binaryExpression.getE1().visit(this, arg);
						binaryExpression.getE0().visit(this, arg); 
					}else{
						binaryExpression.getE0().visit(this, arg);
						binaryExpression.getE1().visit(this, arg); 
					}
					mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "div", "(Ljava/awt/image/BufferedImage;I)Ljava/awt/image/BufferedImage;", false);
				}
				if(binaryExpression.getOp().isKind(MOD)){
					if(binaryExpression.getE0().getType().equals(MOD)){
						binaryExpression.getE1().visit(this, arg);
						binaryExpression.getE0().visit(this, arg); 
					}else{
						binaryExpression.getE0().visit(this, arg);
						binaryExpression.getE1().visit(this, arg); 
					}
					mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "mod", "(Ljava/awt/image/BufferedImage;I)Ljava/awt/image/BufferedImage;", false);
				}
			}
			
		else if(binaryExpression.getType()==TypeName.BOOLEAN)
		{
			Label l1 = new Label();
			if(binaryExpression.getOp().isKind(AND)) {
				binaryExpression.getE0().visit(this, arg);
				mv.visitJumpInsn(IFEQ, l1);
				binaryExpression.getE1().visit(this, arg);
				mv.visitJumpInsn(IFEQ, l1);
				mv.visitInsn(ICONST_1);
				Label l2 = new Label();
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(l2);
			}
			
			if(binaryExpression.getOp().isKind(OR)) {
				binaryExpression.getE0().visit(this, arg);
				mv.visitJumpInsn(IFNE, l1);
				binaryExpression.getE1().visit(this, arg);
				mv.visitJumpInsn(IFNE, l1);
				mv.visitInsn(ICONST_0);
				Label l2 = new Label();
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(l2);
			}
				
			if(binaryExpression.getOp().isKind(EQUAL)) {
				if (binaryExpression.getE0().getType().equals(TypeName.INTEGER)
						|| binaryExpression.getE0().getType().equals(TypeName.BOOLEAN))
				{
					binaryExpression.getE0().visit(this, arg);
					binaryExpression.getE1().visit(this, arg);
					mv.visitJumpInsn(IF_ICMPEQ, l1);
				mv.visitInsn(ICONST_0);
				Label l2 = new Label();
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(l2);
			}
		}
				if(binaryExpression.getOp().isKind(NOTEQUAL)) {
				if (binaryExpression.getE0().getType().equals(TypeName.INTEGER)||binaryExpression.getE0().getType().equals(TypeName.BOOLEAN))
						{
					binaryExpression.getE0().visit(this, arg);
					binaryExpression.getE1().visit(this, arg);
				mv.visitJumpInsn(IF_ICMPNE, l1);
				mv.visitInsn(ICONST_0);
				Label l2 = new Label();
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(l2);
				}
				}
				if(binaryExpression.getOp().isKind(LT)) {
					binaryExpression.getE0().visit(this, arg);
					binaryExpression.getE1().visit(this, arg);
				mv.visitJumpInsn(IF_ICMPLT, l1);
				mv.visitInsn(ICONST_0);
				Label l2 = new Label();
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(l2);
				}
				if(binaryExpression.getOp().isKind(LE)) {
				binaryExpression.getE0().visit(this, arg);
				binaryExpression.getE1().visit(this, arg);
				mv.visitJumpInsn(IF_ICMPLE, l1);
				mv.visitInsn(ICONST_0);
				Label l2 = new Label();
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(l2);
				}
				if(binaryExpression.getOp().isKind(GT)) {
					binaryExpression.getE0().visit(this, arg);
					binaryExpression.getE1().visit(this, arg);
				mv.visitJumpInsn(IF_ICMPGT, l1);
				mv.visitInsn(ICONST_0);
				Label l2 = new Label();
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(l2);
				}
				if(binaryExpression.getOp().isKind(GE)) {
					binaryExpression.getE0().visit(this, arg);
					binaryExpression.getE1().visit(this, arg);
				mv.visitJumpInsn(IF_ICMPGE, l1);
				mv.visitInsn(ICONST_0);
				Label l2 = new Label();
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(l2);
			}	
		}
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		//TODO  Implement this
		int c=locCount;
		Label l0=new Label();
		mv.visitLabel(l0);
		Label l1=new Label();
		for (Dec d : block.getDecs()) {
			d.visit(this, arg);
			loc.add(new DecLocal(l0, l1, d.getIdent().getText(), Type.getTypeName(d.getFirstToken()).getJVMTypeDesc(), locCount-1));
			}
		for (Statement s : block.getStatements()) 
			s.visit(this, arg);
		mv.visitLabel(l1);
		locCount=c;
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		//TODO Implement this
		mv.visitLdcInsn(booleanLitExpression.getValue());
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		if(constantExpression.getFirstToken().getTKind()==(KW_SCREENWIDTH))
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFrame", "getScreenWidth", "()I", false);
		if(constantExpression.getFirstToken().getTKind()==(KW_SCREENHEIGHT))
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFrame", "getScreenHeight", "()I", false);
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		//TODO Implement this
		declaration.setSlot(locCount++);
		if(Type.getTypeName(declaration.getFirstToken())==(IMAGE)||Type.getTypeName(declaration.getFirstToken())==(FRAME))
		{
			mv.visitInsn(ACONST_NULL);
			mv.visitVarInsn(ASTORE, locCount-1);
		}
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		filterOpChain.getArg().visit(this, arg);
		if(filterOpChain.isBarArrow()){

			if(filterOpChain.getFirstToken().isKind(OP_GRAY))
			{
				mv.visitInsn(DUP);
				mv.visitInsn(SWAP);
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFilterOps", "grayOp", "(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
			}
		}else{
		if(filterOpChain.getFirstToken().isKind(OP_BLUR))
		{
			mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFilterOps", "blurOp", "(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
		}
		if(filterOpChain.getFirstToken().isKind(OP_GRAY))
		{
			mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFilterOps", "grayOp", "(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
		}
		if(filterOpChain.getFirstToken().isKind(OP_CONVOLVE))
		{
			mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFilterOps", "convolveOp", "(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
		}
		}
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		
		frameOpChain.getArg().visit(this, arg);
		if(frameOpChain.getFirstToken().isKind(KW_SHOW))
				{
			mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "showImage", "()Lcop5556sp17/PLPRuntimeFrame;", false);
				}
		if(frameOpChain.getFirstToken().isKind(KW_XLOC)){
			mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "getXVal", "()I", false);
		}
		if(frameOpChain.getFirstToken().isKind(KW_YLOC)){
			mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "getYVal", "()I", false);
		}
		if(frameOpChain.getFirstToken().isKind(KW_HIDE)){
			mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "hideImage", "()Lcop5556sp17/PLPRuntimeFrame;", false);
		}
		if(frameOpChain.getFirstToken().isKind(KW_MOVE)){
			mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "moveFrame", "(II)Lcop5556sp17/PLPRuntimeFrame;", false);
		}
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		int i=0;
		int ind=-1;
		for(i=loc.size()-1;i>=0;i--){
			if(identChain.getFirstToken().getText().equals(loc.get(i).id)){
				ind=i;
				break;
			}
		}
		if((boolean)arg==true){
			if(identChain.getType()==(TypeName.INTEGER)||identChain.getType()==(TypeName.BOOLEAN))
			{
				if(ind>=0)
					 mv.visitVarInsn(ILOAD,loc.get(ind).sno);
				else
					 mv.visitFieldInsn(Opcodes.GETSTATIC, className, identChain.getFirstToken().getText(), Type.getTypeName(identChain.getFirstToken()).getJVMTypeDesc());
			}
			if(identChain.getType()==(TypeName.FILE))
				mv.visitFieldInsn(GETSTATIC, className, identChain.getFirstToken().getText(), "Ljava/io/File;");
			if(identChain.getType()==(TypeName.URL))
				mv.visitFieldInsn(GETSTATIC, className, identChain.getFirstToken().getText(), "Ljava/net/URL;");
				
			if(identChain.getType()==(TypeName.IMAGE)||identChain.getType()==(TypeName.FRAME))
				mv.visitVarInsn(ALOAD,loc.get(ind).sno);
				
		}else{
			
			if(identChain.getType()==(TypeName.INTEGER)||identChain.getType()==(TypeName.BOOLEAN)){
				if(ind>=0){
					mv.visitInsn(DUP);
				mv.visitVarInsn(ISTORE,loc.get(ind).sno);
				}
				else{
					mv.visitInsn(DUP);
					 mv.visitFieldInsn(PUTSTATIC, className, identChain.getFirstToken().getText(), Type.getTypeName(identChain.getFirstToken()).getJVMTypeDesc());
					}
			}
			if(identChain.getType()==(TypeName.FILE)){
				mv.visitFieldInsn(GETSTATIC, className, identChain.getFirstToken().getText(), "Ljava/io/File;");
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "write", "(Ljava/awt/image/BufferedImage;Ljava/io/File;)Ljava/awt/image/BufferedImage;", false);
			}
			if(identChain.getType()==(TypeName.URL))
				mv.visitFieldInsn(PUTSTATIC, className, identChain.getFirstToken().getText(), "Ljava/net/URL;");
			if(identChain.getType().equals(TypeName.IMAGE)){
				mv.visitInsn(DUP);
				mv.visitVarInsn(ASTORE,loc.get(ind).sno);
			}
			if(identChain.getType()==(TypeName.FRAME)){
				mv.visitVarInsn(ALOAD,loc.get(ind).sno);
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFrame", "createOrSetFrame", "(Ljava/awt/image/BufferedImage;Lcop5556sp17/PLPRuntimeFrame;)Lcop5556sp17/PLPRuntimeFrame;", false);
				mv.visitInsn(DUP);
				mv.visitVarInsn(ASTORE,loc.get(ind).sno);
			}
		}
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		if (identExpression.getDec() instanceof ParamDec) {
			mv.visitFieldInsn(GETSTATIC, className, identExpression.getFirstToken().getText(),
					Type.getTypeName(identExpression.getDec().getFirstToken()).getJVMTypeDesc());
		} else if (Type.getTypeName(identExpression.getDec().getFirstToken()) == TypeName.INTEGER || Type.getTypeName(identExpression.getDec().getFirstToken()) == TypeName.BOOLEAN) {
	            mv.visitVarInsn(ILOAD,identExpression.getDec().getSlot());
	        } 
		else {
            mv.visitVarInsn(ALOAD, identExpression.getDec().getSlot());
        }
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		Dec d = identX.getDec();
		if( Type.getTypeName(d.getFirstToken()).isType(IMAGE)){
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "copyImage", "(Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
			mv.visitVarInsn(ASTORE, d.getSlot());
		}
		else if( Type.getTypeName(d.getFirstToken()).isType(FRAME)){
			mv.visitVarInsn(ASTORE, d.getSlot());
		}
		else
		if (d instanceof ParamDec) {
			mv.visitFieldInsn(PUTSTATIC, className, identX.getText(),
			Type.getTypeName(d.getFirstToken()).getJVMTypeDesc());
		} 
		else {
			mv.visitVarInsn(ISTORE, d.getSlot());
		}
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		ifStatement.getE().visit(this, arg); 
		Label l0 = new Label();
		mv.visitJumpInsn(IFEQ, l0); 
		ifStatement.getB().visit(this, arg); 
		mv.visitLabel(l0);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		if(imageOpChain.getFirstToken().isKind(KW_SCALE))  
		{
			imageOpChain.getArg().visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "scale", "(Ljava/awt/image/BufferedImage;I)Ljava/awt/image/BufferedImage;", false);
        }
		if(imageOpChain.getFirstToken().isKind(OP_WIDTH))  
				{
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getWidth", "()I", false);
				}
		if(imageOpChain.getFirstToken().isKind(OP_HEIGHT))  
		{
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getHeight", "()I", false);
		}
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		//TODO Implement this
		mv.visitLdcInsn(intLitExpression.value); 
		return null;
	}


	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		//TODO Implement this
		//For assignment 5, only needs to handle integers and booleans
		FieldVisitor fv = cw.visitField(ACC_STATIC, paramDec.getIdent().getText(), Type.getTypeName(paramDec.getFirstToken()).getJVMTypeDesc(), null,null);
		fv.visitEnd();
		Label param = new Label();
		mv.visitLabel(param);
		if(Type.getTypeName(paramDec.getFirstToken()).isType(TypeName.INTEGER))
		{
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(count++);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			mv.visitFieldInsn(PUTSTATIC, className, paramDec.getIdent().getText(), "I");
		}
		if(Type.getTypeName(paramDec.getFirstToken()).isType(TypeName.FILE))
		{
	    mv.visitTypeInsn(NEW, "java/io/File");
	    mv.visitInsn(DUP);
	    mv.visitVarInsn(ALOAD, 1);
	    mv.visitLdcInsn(count++);
	    mv.visitInsn(AALOAD);
	    mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
	    mv.visitFieldInsn(PUTSTATIC, className, paramDec.getIdent().getText(), "Ljava/io/File;");
	}
		if(Type.getTypeName(paramDec.getFirstToken()).isType(TypeName.BOOLEAN))
		{
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(count++);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "getBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitFieldInsn(PUTSTATIC, className, paramDec.getIdent().getText(), "Z");
		}
	    if(Type.getTypeName(paramDec.getFirstToken()).isType(TypeName.URL))
		{
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(count++);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "getURL",
					"([Ljava/lang/String;I)Ljava/net/URL;", false);
			mv.visitFieldInsn(PUTSTATIC, className, paramDec.getIdent().getText(), "Ljava/net/URL;");
		}
		return null;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		sleepStatement.getE().visit(this, arg);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		List<Expression> e = tuple.getExprList();
		for (Expression expr : e) {
			expr.visit(this, arg);
		}
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		//TODO Implement this
		Label l0 = new Label();
		Label l1 = new Label();
		mv.visitLabel(l0);
		whileStatement.getE().visit(this, arg); 
		mv.visitJumpInsn(IFEQ, l1); 
		whileStatement.getB().visit(this, arg);
		mv.visitJumpInsn(GOTO, l0); 
		mv.visitLabel(l1);
    	return null;
	}
}


