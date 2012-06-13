package compiler.semanal;

import compiler.abstree.*;
import compiler.abstree.tree.*;
import compiler.semanal.type.*;

public class SemNameResolver implements AbsVisitor {

    int recordlvl = -1;
    boolean record() { return recordlvl!=-1; }
    
    public boolean error = false;
    public int errors = 0;
    private String name = "Nameresolver";
    
    public void Error(String s, AbsTree abs) {
        System.out.println((++errors)+": " + name  + ": "+s+" at: "+abs.begLine+","+abs.begColumn);
        error = true;
    }
    
	@Override public void visit(AbsAlloc acceptor) { acceptor.type.accept(this); }

	@Override
	public void visit(AbsArrayType acceptor) {
	    acceptor.type.accept(this);
	    acceptor.loBound.accept(this);
	    acceptor.hiBound.accept(this);
	}

	@Override
	public void visit(AbsAssignStmt acceptor) {
	    acceptor.dstExpr.accept(this);
	    acceptor.srcExpr.accept(this);
	}

	@Override
	public void visit(AbsAtomConst acceptor) {
        String val = acceptor.value;
        switch(acceptor.type) {
    	    case AbsAtomConst.BOOL: 
    	        //SemDesc.setActualConst(acceptor, Boolean.parseBoolean(val));
    	        break;
    	    case AbsAtomConst.CHAR: 
    	        //if(val.charAt(0)=='\'' && val.charAt(2)=='\'') SemDesc.setActualConst(acceptor, val.charAt(1)); else throw new Exception(); 
    	        break;
	        case AbsAtomConst.INT: 
	            SemDesc.setActualConst(acceptor, Integer.parseInt(val)); 
	            break;
	    }
	}

	@Override public void visit(AbsAtomType acceptor) {}

	@Override
	public void visit(AbsBinExpr acceptor) {
		acceptor.fstExpr.accept(this);
        if(acceptor.oper==AbsBinExpr.RECACCESS) return; //record = true;
	    acceptor.sndExpr.accept(this);
	    Integer 
	        fstVal = SemDesc.getActualConst(acceptor.fstExpr),
	        sndVal = SemDesc.getActualConst(acceptor.sndExpr);
	    if(fstVal!=null && sndVal!=null) switch(acceptor.oper) {
		    case AbsBinExpr.ADD: SemDesc.setActualConst(acceptor, fstVal + sndVal);	break;
		    case AbsBinExpr.SUB: SemDesc.setActualConst(acceptor, fstVal - sndVal);	break;
		    case AbsBinExpr.MUL: SemDesc.setActualConst(acceptor, fstVal * sndVal);	break;
		    case AbsBinExpr.DIV: if(sndVal!=0) SemDesc.setActualConst(acceptor, fstVal / sndVal);	break;
		    case AbsBinExpr.MOD: if(sndVal!=0) SemDesc.setActualConst(acceptor, fstVal % sndVal);	break;
	    }
	    //record = false;
	}

	@Override public void visit(AbsBlockStmt acceptor) { acceptor.stmts.accept(this); }

	@Override
	public void visit(AbsCallExpr acceptor) {
	    AbsDecl funcproc = SemTable.fnd(acceptor.name.name);
	    if(funcproc==null) {
	        Error("unknown method", acceptor);
	    } else {
	        SemDesc.setNameDecl(acceptor, funcproc);
	    }
	    
        acceptor.name.accept(this);
        acceptor.args.accept(this);
	}

	@Override
	public void visit(AbsConstDecl acceptor) {
	    if(!record()) try {
		    SemTable.ins(acceptor.name.name, acceptor);
	    } catch(SemIllegalInsertException e) {
	        Error("redeclaration", acceptor.name);
	    }

		acceptor.value.accept(this);
		
		Integer value = SemDesc.getActualConst(acceptor.value);
		if(value!=null) SemDesc.setActualConst(acceptor, value);
	}

	@Override public void visit(AbsDeclName acceptor) {}

	@Override public void visit(AbsDecls acceptor) { for(AbsDecl decl : acceptor.decls) decl.accept(this); }

	@Override public void visit(AbsExprStmt acceptor) { acceptor.expr.accept(this); }

	@Override
	public void visit(AbsForStmt acceptor) {
	    acceptor.name.accept(this);
	    AbsDecl var = SemTable.fnd(acceptor.name.name);
	    if(var==null) {
	        Error("undeclared variable", acceptor);
        } else {
	        SemDesc.setNameDecl(acceptor, var);
	    }

	    acceptor.loBound.accept(this);
	    acceptor.hiBound.accept(this);
		
		acceptor.stmt.accept(this);
	}

	@Override
	public void visit(AbsIfStmt acceptor) {
	    acceptor.cond.accept(this);
	    acceptor.thenStmt.accept(this);
	    acceptor.elseStmt.accept(this);
	}

	@Override public void visit(AbsNilConst acceptor) {}

	@Override public void visit(AbsPointerType acceptor) { acceptor.type.accept(this); }

	@Override
	public void visit(AbsFunDecl acceptor) {
        try {
			SemTable.ins(acceptor.name.name, acceptor);
		} catch (SemIllegalInsertException e) {
			Error("I don't do polymorphism", acceptor.name);
		}

		SemTable.newScope();
	        acceptor.pars.accept(this);
	        acceptor.type.accept(this);
	        acceptor.decls.accept(this);
	        acceptor.stmt.accept(this);
		SemTable.oldScope();	    
    }

	@Override
	public void visit(AbsProcDecl acceptor) {
        try {
			SemTable.ins(acceptor.name.name, acceptor);
		} catch (SemIllegalInsertException e) {
			Error("I don't do polymorphism", acceptor.name);
		}
		SemTable.newScope();
	        acceptor.pars.accept(this);
	        acceptor.decls.accept(this);
	        acceptor.stmt.accept(this);
		SemTable.oldScope();	    
	}

	@Override
	public void visit(AbsProgram acceptor) {
	    SystemStubs.fillData();
	
        acceptor.decls.accept(this);
        acceptor.stmt.accept(this);
	}

	@Override
	public void visit(AbsRecordType acceptor) {
	    recordlvl++;
        acceptor.fields.accept(this);
        recordlvl--;
	}

	@Override public void visit(AbsStmts acceptor) { for(AbsStmt stmt : acceptor.stmts) stmt.accept(this); }

	@Override
	public void visit(AbsTypeDecl acceptor) {
        if(!record()) try {
			SemTable.ins(acceptor.name.name, acceptor);
		} catch(SemIllegalInsertException e) {
			Error("redeclaration", acceptor.name);
		}
		
		acceptor.type.accept(this);
		SemDesc.setNameDecl(acceptor.name, acceptor);
	}

	@Override
	public void visit(AbsTypeName acceptor) {
	    AbsDecl typedecl = SemTable.fnd(acceptor.name);
	    if(typedecl==null) {
	        Error("unknown type", acceptor);
	    } else {
	        SemDesc.setNameDecl(acceptor, typedecl);
	    }
	}

	@Override
	public void visit(AbsUnExpr acceptor) {
		acceptor.expr.accept(this);
		Integer val = SemDesc.getActualConst(acceptor.expr);
		if(val!=null) switch(acceptor.oper) {
		    case AbsUnExpr.ADD: SemDesc.setActualConst(acceptor, val); break;
		    case AbsUnExpr.SUB: SemDesc.setActualConst(acceptor, -val); break;
		}
	}

	@Override public void visit(AbsValExprs acceptor) { for(AbsValExpr expr : acceptor.exprs) expr.accept(this); }

	@Override
	public void visit(AbsValName acceptor) {
	    //if(!record) {
	        AbsDecl decl = SemTable.fnd(acceptor.name);
	        if(decl==null) {
		        Error("undefined variable", acceptor);
	        } else {
		        SemDesc.setNameDecl(acceptor, decl);
		        Integer val = SemDesc.getActualConst(decl);
		        if(val!=null) SemDesc.setActualConst(acceptor, val);
	        }
        //}
	}

	@Override
	public void visit(AbsVarDecl acceptor) {
		if(!record()) try {
			SemTable.ins(acceptor.name.name, acceptor);
		} catch(SemIllegalInsertException e) {
			Error("redeclaration", acceptor.name);
		}
		
		acceptor.type.accept(this);
	}

	@Override
	public void visit(AbsWhileStmt acceptor) {
	    acceptor.cond.accept(this);
	    acceptor.stmt.accept(this);
	}

	@Override
	public void visit(AbsRepeatStmt acceptor) {
	    acceptor.cond.accept(this);
	    acceptor.stmts.accept(this);
	}
}
