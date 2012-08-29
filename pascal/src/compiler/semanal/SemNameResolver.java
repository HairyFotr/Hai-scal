package compiler.semanal;

import compiler.abstree.*;
import compiler.abstree.tree.*;
import compiler.semanal.type.*;

public class SemNameResolver implements AbsVisitor {
    public boolean error = false;
    public int errors = 0;
    private String name = "NameResolver";
    private String leading0(int i) {
        String out = "" + i;
        if(out.length()==1) out = "0"+out;
        return out;
    }
    public void Error(String s, AbsTree abs) {
        System.out.println(
            leading0(++errors)+": "+
            name+
            " at ("+leading0(abs.begLine)+","+leading0(abs.begColumn)+"): "+
            "("+abs.getClass().getName().substring(abs.getClass().getName().lastIndexOf(".")+1)+") \t"+
            s
        );
        error = true;
    }
    

    int recordlvl = -1;
    boolean record() { return recordlvl!=-1; }
    
	public void visit(AbsAlloc acceptor) { acceptor.type.accept(this); }

	public void visit(AbsArrayType acceptor) {
	    acceptor.type.accept(this);
	    acceptor.loBound.accept(this);
	    acceptor.hiBound.accept(this);
	}

	public void visit(AbsAssignStmt acceptor) {
	    acceptor.dstExpr.accept(this);
	    acceptor.srcExpr.accept(this);
	}

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

	public void visit(AbsAtomType acceptor) {}
	
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

	public void visit(AbsBlockStmt acceptor) { acceptor.stmts.accept(this); }

	public void visit(AbsCallExpr acceptor) {
	    AbsDecl funcproc = SemTable.fnd(acceptor.name.name);
	    if(funcproc==null) {
	        Error("Undefined method '"+acceptor.name.name+"'", acceptor);
	    } else {
	        SemDesc.setNameDecl(acceptor, funcproc);
	    }
	    
        acceptor.name.accept(this);
        acceptor.args.accept(this);
	}

	public void visit(AbsConstDecl acceptor) {
	    if(!record()) try {
		    SemTable.ins(acceptor.name.name, acceptor);
	    } catch(SemIllegalInsertException e) {
	        Error("Const '"+acceptor.name.name+"' already defined", acceptor.name);
	    }

		acceptor.value.accept(this);
		
		Integer value = SemDesc.getActualConst(acceptor.value);
		if(value!=null) SemDesc.setActualConst(acceptor, value);
	}

	public void visit(AbsDeclName acceptor) {}

	public void visit(AbsDecls acceptor) { for(AbsDecl decl : acceptor.decls) decl.accept(this); }

	public void visit(AbsExprStmt acceptor) { acceptor.expr.accept(this); }

	public void visit(AbsForStmt acceptor) {
	    acceptor.name.accept(this);
	    AbsDecl var = SemTable.fnd(acceptor.name.name);
	    if(var==null) {
	        Error("Undeclared variable '"+acceptor.name.name+"'", acceptor);
        } else {
	        SemDesc.setNameDecl(acceptor, var);
	    }

	    acceptor.loBound.accept(this);
	    acceptor.hiBound.accept(this);
		
		acceptor.stmt.accept(this);
	}

	public void visit(AbsIfStmt acceptor) {
	    acceptor.cond.accept(this);
	    acceptor.thenStmt.accept(this);
	    acceptor.elseStmt.accept(this);
	}

	public void visit(AbsNilConst acceptor) {}

	public void visit(AbsPointerType acceptor) { acceptor.type.accept(this); }

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

	public void visit(AbsProgram acceptor) {
	    SystemStubs.fillData();
	
        acceptor.decls.accept(this);
        acceptor.stmt.accept(this);
	}

	public void visit(AbsRecordType acceptor) {
	    recordlvl++;
        acceptor.fields.accept(this);
        recordlvl--;
	}

	public void visit(AbsStmts acceptor) { for(AbsStmt stmt : acceptor.stmts) stmt.accept(this); }

	public void visit(AbsTypeDecl acceptor) {
        if(!record()) try {
	        SemTable.ins(acceptor.name.name, acceptor);
	    } catch(SemIllegalInsertException e) {
		    Error("Type '"+acceptor.name.name+"' already defined", acceptor.name);
	    }
	
	    acceptor.type.accept(this);
	    SemDesc.setNameDecl(acceptor.name, acceptor);
	}

	public void visit(AbsTypeName acceptor) {
	    AbsDecl typedecl = SemTable.fnd(acceptor.name);
	    if(typedecl==null) {
	        Error("Undefined type '"+acceptor.name+"'", acceptor);
	    } else {
	        SemDesc.setNameDecl(acceptor, typedecl);
	    }
	}

	public void visit(AbsUnExpr acceptor) {
		acceptor.expr.accept(this);
		Integer val = SemDesc.getActualConst(acceptor.expr);
		if(val!=null) switch(acceptor.oper) {
		    case AbsUnExpr.ADD: SemDesc.setActualConst(acceptor, val); break;
		    case AbsUnExpr.SUB: SemDesc.setActualConst(acceptor, -val); break;
		}
	}

	public void visit(AbsValExprs acceptor) { for(AbsValExpr expr : acceptor.exprs) expr.accept(this); }
	
	public void visit(AbsValName acceptor) {
	    //if(!record) {
	        AbsDecl decl = SemTable.fnd(acceptor.name);
	        if(decl==null) {
		        Error("Undefined variable '"+acceptor.name+"'", acceptor);
	        } else {
		        SemDesc.setNameDecl(acceptor, decl);
		        Integer val = SemDesc.getActualConst(decl);
		        if(val!=null) SemDesc.setActualConst(acceptor, val);
	        }
        //}
	}

	public void visit(AbsVarDecl acceptor) {
		if(!record()) try {
			SemTable.ins(acceptor.name.name, acceptor);
		} catch(SemIllegalInsertException e) {
			Error("Variable "+acceptor.name.name+" already defined", acceptor.name);
		}
		
		acceptor.type.accept(this);
	}

	public void visit(AbsWhileStmt acceptor) {
	    acceptor.cond.accept(this);
	    acceptor.stmt.accept(this);
	}

	public void visit(AbsRepeatStmt acceptor) {
	    acceptor.cond.accept(this);
	    acceptor.stmts.accept(this);
	}

	public void visit(AbsReturn acceptor) {
	    if(acceptor.expr != null)
    	    acceptor.expr.accept(this);
	}
}






