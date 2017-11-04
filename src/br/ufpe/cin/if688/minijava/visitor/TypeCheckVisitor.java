package br.ufpe.cin.if688.minijava.visitor;

import br.ufpe.cin.if688.minijava.ast.And;
import br.ufpe.cin.if688.minijava.ast.ArrayAssign;
import br.ufpe.cin.if688.minijava.ast.ArrayLength;
import br.ufpe.cin.if688.minijava.ast.ArrayLookup;
import br.ufpe.cin.if688.minijava.ast.Assign;
import br.ufpe.cin.if688.minijava.ast.Block;
import br.ufpe.cin.if688.minijava.ast.BooleanType;
import br.ufpe.cin.if688.minijava.ast.Call;
import br.ufpe.cin.if688.minijava.ast.ClassDeclExtends;
import br.ufpe.cin.if688.minijava.ast.ClassDeclSimple;
import br.ufpe.cin.if688.minijava.ast.False;
import br.ufpe.cin.if688.minijava.ast.Formal;
import br.ufpe.cin.if688.minijava.ast.Identifier;
import br.ufpe.cin.if688.minijava.ast.IdentifierExp;
import br.ufpe.cin.if688.minijava.ast.IdentifierType;
import br.ufpe.cin.if688.minijava.ast.If;
import br.ufpe.cin.if688.minijava.ast.IntArrayType;
import br.ufpe.cin.if688.minijava.ast.IntegerLiteral;
import br.ufpe.cin.if688.minijava.ast.IntegerType;
import br.ufpe.cin.if688.minijava.ast.LessThan;
import br.ufpe.cin.if688.minijava.ast.MainClass;
import br.ufpe.cin.if688.minijava.ast.MethodDecl;
import br.ufpe.cin.if688.minijava.ast.Minus;
import br.ufpe.cin.if688.minijava.ast.NewArray;
import br.ufpe.cin.if688.minijava.ast.NewObject;
import br.ufpe.cin.if688.minijava.ast.Not;
import br.ufpe.cin.if688.minijava.ast.Plus;
import br.ufpe.cin.if688.minijava.ast.Print;
import br.ufpe.cin.if688.minijava.ast.Program;
import br.ufpe.cin.if688.minijava.ast.This;
import br.ufpe.cin.if688.minijava.ast.Times;
import br.ufpe.cin.if688.minijava.ast.True;
import br.ufpe.cin.if688.minijava.ast.Type;
import br.ufpe.cin.if688.minijava.ast.VarDecl;
import br.ufpe.cin.if688.minijava.ast.While;
import br.ufpe.cin.if688.minijava.symboltable.Method;
import br.ufpe.cin.if688.minijava.symboltable.Class;
import br.ufpe.cin.if688.minijava.symboltable.SymbolTable;

public class TypeCheckVisitor implements IVisitor<Type> {

	private boolean isVariable;
    private boolean isMethod;	
	private Method currMethod;
    private Class currClass;
    private Class currFather;
    private SymbolTable symbolTable;

	TypeCheckVisitor(SymbolTable st) {
		symbolTable = st;
		this.currClass = null;
		this.currMethod = null;
		this.isVariable = false;
		this.isMethod = false;
	}

	// MainClass m;
	// ClassDeclList cl;
	public Type visit(Program n) {
		n.m.accept(this);
		
		for (int i = 0; i < n.cl.size(); i++) {
			n.cl.elementAt(i).accept(this);
		}
		return null;
	}

	// Identifier i1,i2;
	// Statement s;
	public Type visit(MainClass n) {
		this.currClass = this.symbolTable.getClass(n.i1.toString());
		this.currMethod = this.symbolTable.getMethod("main", this.currClass.getId());
		
		n.i1.accept(this);
		this.isVariable = true;
		n.i2.accept(this);
		this.isVariable = false;
		n.s.accept(this);
		
		this.currMethod = null;
		this.currClass = null;
		return null;
	}

	// Identifier i;
	// VarDeclList vl;
	// MethodDeclList ml;
	public Type visit(ClassDeclSimple n) {
		this.currClass = symbolTable.getClass(n.i.toString());
		
		n.i.accept(this);
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.elementAt(i).accept(this);
		}
		for (int i = 0; i < n.ml.size(); i++) {
			n.ml.elementAt(i).accept(this);
		}
		return null;
	}

	// Identifier i;
	// Identifier j;
	// VarDeclList vl;
	// MethodDeclList ml;
	public Type visit(ClassDeclExtends n) {
		this.currClass = symbolTable.getClass(n.i.toString());
		this.currFather = symbolTable.getClass(n.j.toString());
		
		n.i.accept(this);
		n.j.accept(this);
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.elementAt(i).accept(this);
		}
		for (int i = 0; i < n.ml.size(); i++) {
			n.ml.elementAt(i).accept(this);
		}
		
		this.currFather = null;
		this.currClass = null;
		return null;
	}

	// Type t;
	// Identifier i;
	public Type visit(VarDecl n) {
		Type t = n.t.accept(this);
		this.isVariable = true;
		n.i.accept(this);
		this.isVariable = false;
		return t;
	}

	// Type t;
	// Identifier i;
	// FormalList fl;
	// VarDeclList vl;
	// StatementList sl;
	// Exp e;
	public Type visit(MethodDecl n) {
		this.currMethod = this.symbolTable.getMethod(n.i.toString(), this.currClass.getId());
		Type tMethod = n.t.accept(this);
		this.isMethod = true;
		
		n.i.accept(this);
		this.isMethod = false;
		for (int i = 0; i < n.fl.size(); i++) {
			n.fl.elementAt(i).accept(this);
		}
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.elementAt(i).accept(this);
		}
		for (int i = 0; i < n.sl.size(); i++) {
			n.sl.elementAt(i).accept(this);
		}
		
		Type tExp = n.e.accept(this);
		if(!this.symbolTable.compareTypes(tMethod, tExp)) {
			System.out.printf("Incompatible types: %s cannot be converted to %s.", this.getTypeName(tExp), this.getTypeName(tMethod));
		}
		
		this.currMethod = null;
		return tMethod;
	}

	// Type t;
	// Identifier i;
	public Type visit(Formal n) {
		Type t = n.t.accept(this);
		this.isVariable = true;
		n.i.accept(this);
		this.isVariable = false;
		return t;
	}

	public Type visit(IntArrayType n) {
		return n;
	}

	public Type visit(BooleanType n) {
		return n;
	}

	public Type visit(IntegerType n) {
		return n;
	}

	// String s;
	public Type visit(IdentifierType n) {
		if(!this.symbolTable.containsClass(n.s)) {
			System.out.printf("Cannot find symbol %s.", n.s);
		}
		return n;
	}

	// StatementList sl;
	public Type visit(Block n) {
		for (int i = 0; i < n.sl.size(); i++) {
			n.sl.elementAt(i).accept(this);
		}
		return null;
	}

	// Exp e;
	// Statement s1,s2;
	public Type visit(If n) {
		Type tExp = n.e.accept(this);
		
		if(!this.symbolTable.compareTypes(tExp, new BooleanType())) {
			System.out.printf("Incompatible types: %s cannot be converted to BooleanType.", this.getTypeName(tExp));
		}		
		n.s1.accept(this);
		n.s2.accept(this);
		return null;
	}

	// Exp e;
	// Statement s;
	public Type visit(While n) {
		Type tExp = n.e.accept(this);
		
		if(!this.symbolTable.compareTypes(tExp, new BooleanType())) {
			System.out.printf("Incompatible types: %s cannot be converted to BooleanType.", this.getTypeName(tExp));
		}
		n.s.accept(this);
		return null;
	}

	// Exp e;
	public Type visit(Print n) {
		n.e.accept(this);
		return null;
	}

	// Identifier i;
	// Exp e;
	public Type visit(Assign n) {
		this.isVariable = true;
		Type tID = n.i.accept(this);
		this.isVariable = false;
		Type tExp = n.e.accept(this);
		
		if(!this.symbolTable.compareTypes(tID,tExp)) {
			System.out.printf("Incompatible types: %s cannot be converted to %s.", this.getTypeName(tExp), this.getTypeName(tID));
		}
		return null;
	}

	// Identifier i;
	// Exp e1,e2;
	public Type visit(ArrayAssign n) {
		this.isVariable = true;
		Type tID = n.i.accept(this);
		this.isVariable = false;
		
		Type tExp1 = n.e1.accept(this);
		Type tExp2 = n.e2.accept(this);
		
		if(!this.symbolTable.compareTypes(tID, new IntArrayType())) {
			System.out.printf("IntArrayType required, but %s found.", this.getTypeName(tID));
		}
		if(!this.symbolTable.compareTypes(tExp1, new IntArrayType())) {
			System.out.printf("Incompatible types: %s cannot be converted to IntegerType.", this.getTypeName(tExp1));
		}
		if(!this.symbolTable.compareTypes(tExp2, new IntArrayType())) {
			System.out.printf("Incompatible types: %s cannot be converted to IntegerType.", this.getTypeName(tExp2));
		}
		
		return null;
	}

	// Exp e1,e2;
	public Type visit(And n) {
		Type tExp1 = n.e1.accept(this);
		Type tExp2 = n.e2.accept(this);
		Type tBoolean = new BooleanType();
		
		if((!this.symbolTable.compareTypes(tExp1, tBoolean)) || (!this.symbolTable.compareTypes(tExp2, tBoolean))) {
			System.out.printf("Incompatible types: %s and %s to operation 'AND'.", 
					this.getTypeName(tExp1), this.getTypeName(tExp2));
		}
		
		return tBoolean;
	}

	// Exp e1,e2;
	public Type visit(LessThan n) {
		Type tExp1 = n.e1.accept(this);
		Type tExp2 = n.e2.accept(this);
		Type tInt = new IntegerType();
		
		if((!this.symbolTable.compareTypes(tExp1, tInt)) || (!this.symbolTable.compareTypes(tExp2, tInt))) {
			System.out.printf("Incompatible types: %s and %s to operation 'LESS THAN'.", 
					this.getTypeName(tExp1), this.getTypeName(tExp2));
		}
		
		return tInt;
	}

	// Exp e1,e2;
	public Type visit(Plus n) {
		Type tExp1 = n.e1.accept(this);
		Type tExp2 = n.e2.accept(this);
		Type tInt = new IntegerType();
		
		if((!this.symbolTable.compareTypes(tExp1, tInt)) || (!this.symbolTable.compareTypes(tExp2, tInt))) {
			System.out.printf("Incompatible types: %s and %s to operation 'PLUS'.", 
					this.getTypeName(tExp1), this.getTypeName(tExp2));
		}
		
		return tInt;
	}

	// Exp e1,e2;
	public Type visit(Minus n) {
		Type tExp1 = n.e1.accept(this);
		Type tExp2 = n.e2.accept(this);
		Type tInt = new IntegerType();
		
		if((!this.symbolTable.compareTypes(tExp1, tInt)) || (!this.symbolTable.compareTypes(tExp2, tInt))) {
			System.out.printf("Incompatible types: %s and %s to operation 'MINUS'.", 
					this.getTypeName(tExp1), this.getTypeName(tExp2));
		}
		
		return tInt;
	}

	// Exp e1,e2;
	public Type visit(Times n) {
		Type tExp1 = n.e1.accept(this);
		Type tExp2 = n.e2.accept(this);
		Type tInt = new IntegerType();
		
		if((!this.symbolTable.compareTypes(tExp1, tInt)) || (!this.symbolTable.compareTypes(tExp2, tInt))) {
			System.out.printf("Incompatible types: %s and %s to operation 'TIMES'.", 
					this.getTypeName(tExp1), this.getTypeName(tExp2));
		}
		
		return tInt;
	}

	// Exp e1,e2;
	public Type visit(ArrayLookup n) {
		Type tExp1 = n.e1.accept(this);
		Type tExp2 = n.e2.accept(this);
		Type tInt = new IntegerType();
		
		if(!this.symbolTable.compareTypes(tExp1, new IntArrayType())) {
			System.out.printf("IntArrayType required, but %s found.", this.getTypeName(tExp1));
		}
		if(!this.symbolTable.compareTypes(tExp2, new IntegerType())) {
		System.out.printf("Incompatible types: %s cannot be converted to IntegerType.", this.getTypeName(tExp2));
		}
		
		return tInt;
	}

	// Exp e;
	public Type visit(ArrayLength n) {
		Type tExp = n.e.accept(this);		
		Type tInt = new IntegerType();
		
		if(!this.symbolTable.compareTypes(tExp, new IntArrayType())) {
			System.out.printf("IntArrayType required, but %s found.", this.getTypeName(tExp));
		}
		return tInt;
	}

	// Exp e;
	// Identifier i;
	// ExpList el;
	public Type visit(Call n) {
		n.e.accept(this);
		n.i.accept(this);
		for (int i = 0; i < n.el.size(); i++) {
			n.el.elementAt(i).accept(this);
		}
		return null;
	}

	// int i;
	public Type visit(IntegerLiteral n) {
		return new IntegerType();
	}

	public Type visit(True n) {
		return new BooleanType();
	}

	public Type visit(False n) {
		return new BooleanType();
	}

	// String s;
	public Type visit(IdentifierExp n) {
		Type t = this.symbolTable.getVarType(this.currMethod, this.currClass, n.s);
		return t;
	}

	public Type visit(This n) {
		return this.currClass.type();
	}

	// Exp e;
	public Type visit(NewArray n) {
		Type tExp = n.e.accept(this);
		Type tArray = new IntArrayType();
		
		if(!this.symbolTable.compareTypes(tExp, new IntegerType())) {
			System.out.printf("Incompatible types: %s cannot be converted to IntegerType.", this.getTypeName(tExp));
		}
		return tArray;
	}

	// Identifier i;
	public Type visit(NewObject n) {
		n.i.accept(this);
		return this.symbolTable.getClass(n.i.toString()).type();
	}

	// Exp e;
	public Type visit(Not n) {
		Type tExp = n.e.accept(this);
		Type tBoolean = new BooleanType();
		
		if(!this.symbolTable.compareTypes(tExp, tBoolean)) {
			System.out.printf("Incompatible types: %s to operation 'NOT'.", 
					this.getTypeName(tExp));
		}
		return tBoolean;
	}

	// String s;
	public Type visit(Identifier n) {
		Type identifier;
		
		if(this.isVariable) {
			identifier = this.symbolTable.getVarType(this.currMethod, this.currClass, n.toString());
		} else if(this.isMethod) {
			identifier = this.symbolTable.getMethodType(n.toString(), this.currClass.getId());
		} else {			
			identifier = this.symbolTable.getClass(n.toString()).type();
		}
		
		if(identifier == null) {
			System.out.printf("Cannot find symbol %s.", n.toString());
		}
		
		return identifier;
	}
	
	private String getTypeName(Type t) {
		if(t instanceof IdentifierType) {
			return ((IdentifierType) t).s;
		} else if(t != null){
			return t.getClass().getSimpleName();
		} else {
			return "null";
		}
	}
	
}
