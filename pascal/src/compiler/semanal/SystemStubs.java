package compiler.semanal;

import java.util.*;
import compiler.abstree.tree.*;
import compiler.semanal.type.*;

public class SystemStubs {

	public static final int FAKE_FP = 2011988;

	private static TreeSet<String> sysStubs = new TreeSet<String>();

	public static void fillData() {
		addVar("__line__", SemAtomType.INT);
		addVar("__col__", SemAtomType.INT);
		addVar("__random__", SemAtomType.INT);
		addProcedure("free",SemAtomType.VOID);
		addProcedure("putch",SemAtomType.CHAR);
		addProcedure("putnl");
		addProcedure("putint",SemAtomType.INT);
		addProcedure("putbool",SemAtomType.BOOL);
		addFunction(SemAtomType.CHAR,"getch");
		addFunction(SemAtomType.INT,"getint");
		addFunction(SemAtomType.INT,"getbool");
		addFunction(SemAtomType.INT,"ord",SemAtomType.CHAR);
		addFunction(SemAtomType.CHAR,"chr",SemAtomType.INT);

	}

	private static void addVar(String name, Integer type) {
		try {
			sysStubs.add(name);
			AbsDeclName declName = new AbsDeclName(name);
			AbsAtomType declType = new AbsAtomType(type);
			AbsVarDecl acceptor = new AbsVarDecl(declName, declType);
			SemType declSemType = new SemAtomType(type);
			SemTable.ins(acceptor.name.name, acceptor);
			SemDesc.setActualType(acceptor, declSemType);
		} catch(SemIllegalInsertException e) {
			System.out.println("Error while adding var: "+name);
			e.printStackTrace();
		}
	}

	private static void addProcedure(String name, Integer... pars){
		addFunction(SemAtomType.VOID, name, pars);
	}

	private static void addFunction(int returnType, String name, Integer... pars){
		try {
			sysStubs.add(name);
			AbsDecls pardecls = new AbsDecls();
			SemSubprogramType type = null;
			int c = 0;
			type = new SemSubprogramType(new SemAtomType(returnType));
			for(Integer i: pars) {
				pardecls.decls.add(new AbsVarDecl(new AbsDeclName("var"+c++), new AbsAtomType(i)));
				type.addParType(new SemAtomType(i));
			}
			AbsProcDecl acceptor = new AbsProcDecl(new AbsDeclName(name), pardecls, new AbsDecls(), new AbsBlockStmt(new AbsStmts()));
			SemTable.ins(acceptor.name.name, acceptor);
			SemDesc.setActualType(acceptor, type);
		} catch(SemIllegalInsertException e) {
			System.out.println("Error while adding stub function: "+name);
			e.printStackTrace();
		}
	}

	public static boolean isStub(String name) { return sysStubs.contains(name); }

}
