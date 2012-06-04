package compiler.semanal;

import java.util.*;
import compiler.abstree.tree.*;
import compiler.semanal.type.*;

public class SystemStubs {

	public static final int FAKE_FP = 2011988;

	private static TreeSet<String> sysSubs = new TreeSet<String>();

	public static void fillData() {

		genProcedure("free",SemAtomType.VOID);
		genProcedure("putch",SemAtomType.CHAR);
		genProcedure("putnl");
		genProcedure("putint",SemAtomType.INT);
		genFunction(SemAtomType.CHAR,"getch");
		genFunction(SemAtomType.INT,"getint");
		genFunction(SemAtomType.INT,"ord",SemAtomType.CHAR);
		genFunction(SemAtomType.CHAR,"chr",SemAtomType.INT);

	}

	private static void genProcedure(String name, Integer... pars){
		genFunction(SemAtomType.VOID, name, pars);
	}

	private static void genFunction(int retrurnValue, String name, Integer... pars){
		try {
			sysSubs.add(name);
			AbsDecls pardecls = new AbsDecls();
			SemSubprogramType type = null;
			int c = 0;
			type = new SemSubprogramType(new SemAtomType(retrurnValue));
			for (Integer i: pars){
				pardecls.decls.add(new AbsVarDecl(new AbsDeclName("var"+c++), new AbsAtomType(i)));
				type.addParType(new SemAtomType(i));
			}
			AbsProcDecl acceptor = new AbsProcDecl(new AbsDeclName(name), pardecls, new AbsDecls(), new AbsBlockStmt(new AbsStmts()));
			SemTable.ins(acceptor.name.name, acceptor);
			SemDesc.setActualType(acceptor, type);
		} catch (SemIllegalInsertException e) {
			System.out.println("error making fake function: "+name);
			e.printStackTrace();
		}
	}

	public static boolean isSys(String name) {
		return sysSubs.contains(name);
	}

}