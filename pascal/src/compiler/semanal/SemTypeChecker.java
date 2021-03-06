package compiler.semanal;

import compiler.abstree.*;
import compiler.abstree.tree.*;
import compiler.semanal.type.*;
import java.util.ArrayList;

public class SemTypeChecker implements AbsVisitor {
    public boolean error = false;
    public int errors = 0;
    private String name = "TypeChecker";
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
    
    //int lvl = 0;    
    int recordlvl = -1;
    ArrayList<SemRecordType> records = new ArrayList<SemRecordType>();

    private boolean inProc = false;
    private boolean inFunc = false;

	public void visit(AbsAlloc acceptor) { acceptor.type.accept(this); }

	public void visit(AbsArrayType acceptor) {
	    acceptor.type.accept(this);
	    acceptor.loBound.accept(this);
	    acceptor.hiBound.accept(this);
	    
        try {
		    Integer 
		        loBound = SemDesc.getActualConst(acceptor.loBound),
		        hiBound = SemDesc.getActualConst(acceptor.hiBound);
		    
		    if(loBound==null || hiBound==null) throw new Exception();

    		SemAtomType
		        loBoundT = (SemAtomType)SemDesc.getActualType(acceptor.loBound),
		        hiBoundT = (SemAtomType)SemDesc.getActualType(acceptor.hiBound);

		    if(loBoundT.type!=SemAtomType.INT || loBoundT.type!=SemAtomType.INT) throw new Exception();

    		SemType type = SemDesc.getActualType(acceptor.type);
		    if(type==null) throw new Exception();
		    
		    SemDesc.setActualType(acceptor, new SemArrayType(type, loBound, hiBound));
	    } catch(Exception e) {
            Error("Invalid array type", acceptor);
	    }
	}
	
	public void visit(AbsAssignStmt acceptor) {
	    acceptor.dstExpr.accept(this);
	    acceptor.srcExpr.accept(this);
		
		SemType
		    dstType = SemDesc.getActualType(acceptor.dstExpr),
		    srcType = SemDesc.getActualType(acceptor.srcExpr);
	        if(dstType instanceof SemSubprogramType) dstType = ((SemSubprogramType)dstType).getResultType();
	        if(srcType instanceof SemSubprogramType) srcType = ((SemSubprogramType)srcType).getResultType();
		
		if(dstType==null || srcType==null) {
		    if(dstType==null && srcType!=null) {
    		    AbsVarDecl decl = (AbsVarDecl)SemDesc.getNameDecl(acceptor.dstExpr);
    		    SemDesc.setActualType(acceptor.dstExpr, srcType);
    		    SemDesc.setActualType(decl, srcType);
    		    
                for(AbsVarDecl todo : todoTypes) {
                    if(todo.name == decl.name) {
                        SemDesc.setActualType(todo, srcType);
                        SemDesc.setActualType(todo.name, srcType);
                        SemDesc.setActualType(todo.type, srcType);
                    }
                }    		    

		        //SemType type = SemDesc.getActualType(acceptor.type);
                //if(type!=null) SemDesc.setActualType(acceptor, type);
		        //if(type==null) Error("Unknown type", acceptor);

		    } else {
		        Error("Unknown types, values or vars", acceptor);
		        return;
		    }
	    } else if(dstType instanceof SemSubprogramType) {
		    SemType returnType = ((SemSubprogramType)dstType).getResultType();
		    if(!returnType.coercesTo(srcType)) {
    		    Error("Incompatible return type", acceptor);
		    } else {
		        //dstType = returnType;
		    }
		} else if(!dstType.coercesTo(srcType)) {
		    Error("Incompatible types", acceptor);
	    } else if(!(dstType instanceof SemAtomType || dstType instanceof SemPointerType)) {
		    Error("Invalid left type", acceptor);
	    }
	}
	
	public void visit(AbsAtomConst acceptor) {
        switch(acceptor.type) {
    	    case AbsAtomConst.BOOL: SemDesc.setActualType(acceptor, new SemAtomType(SemAtomType.BOOL)); break;
    	    case AbsAtomConst.CHAR: SemDesc.setActualType(acceptor, new SemAtomType(SemAtomType.CHAR)); break;
    	    case AbsAtomConst.INT: SemDesc.setActualType(acceptor, new SemAtomType(SemAtomType.INT)); break;
    	    default: Error("Unknown atomic type", acceptor);
	    }
	}
	
	public void visit(AbsAtomType acceptor) {
        switch(acceptor.type) {
    	    case AbsAtomType.BOOL: SemDesc.setActualType(acceptor, new SemAtomType(SemAtomType.BOOL)); break;
    	    case AbsAtomType.CHAR: SemDesc.setActualType(acceptor, new SemAtomType(SemAtomType.CHAR)); break;
    	    case AbsAtomType.INT: SemDesc.setActualType(acceptor, new SemAtomType(SemAtomType.INT)); break;
	    }
	}
	
	public void visit(AbsBinExpr acceptor) {
		acceptor.fstExpr.accept(this);
		acceptor.sndExpr.accept(this);
		try {
		    SemType
		        fstType = SemDesc.getActualType(acceptor.fstExpr),
		        sndType = SemDesc.getActualType(acceptor.sndExpr);
		    if(fstType instanceof SemSubprogramType) fstType = ((SemSubprogramType)fstType).getResultType();
		    if(sndType instanceof SemSubprogramType) sndType = ((SemSubprogramType)sndType).getResultType();
		    		    
	        switch(acceptor.oper) {
	            case AbsBinExpr.ADD:case AbsBinExpr.SUB:case AbsBinExpr.MUL:case AbsBinExpr.DIV:case AbsBinExpr.MOD:
	                if(((SemAtomType)fstType).type!=SemAtomType.INT 
	                || ((SemAtomType)sndType).type!=SemAtomType.INT) throw new Exception();
	                
	                SemDesc.setActualType(acceptor, new SemAtomType(SemAtomType.INT));
	                break;
                case AbsBinExpr.EQU:case AbsBinExpr.NEQ:case AbsBinExpr.LTH:case AbsBinExpr.GTH:case AbsBinExpr.LEQ:case AbsBinExpr.GEQ:
                    if(fstType.coercesTo(sndType) && (fstType instanceof SemAtomType || fstType instanceof SemPointerType))
    	                SemDesc.setActualType(acceptor, new SemAtomType(SemAtomType.BOOL));
                    else 
                        throw new Exception();
                    break;
                case AbsBinExpr.AND:case AbsBinExpr.OR:case AbsBinExpr.XOR:
                    if(fstType.coercesTo(sndType) && ((SemAtomType)fstType).type==SemAtomType.BOOL)
    	                SemDesc.setActualType(acceptor, new SemAtomType(SemAtomType.BOOL));
                    else 
                        Error("Weird logic expression", acceptor);
                        //throw new Exception();
                    break;
                case AbsBinExpr.ARRACCESS:
                    if(fstType instanceof SemArrayType && ((SemAtomType)sndType).type==SemAtomType.INT)
    	                SemDesc.setActualType(acceptor, ((SemArrayType)fstType).type);
                    else 
                        Error("Weird array access", acceptor);
                        //throw new Exception();
                    break;
                case AbsBinExpr.RECACCESS:
                    if(fstType instanceof SemRecordType && acceptor.sndExpr instanceof AbsValName) {
                        SemRecordType r = (SemRecordType)fstType;
                        AbsValName sex = (AbsValName)acceptor.sndExpr;
                        AbsDeclName dn = null;
                        for(int i=0; i<r.getNumFields(); i++) {
                            if((sex.name).equals(r.getFieldName(i).name)) {
                                dn = r.getFieldName(i);
                                SemDesc.setActualType(acceptor, r.getFieldType(i));
                                break;
                            }
                        }
                        if(dn==null) throw new Exception();
                    } else {
                        Error("Weird record access", acceptor);
                    }
                    break;
	        }
	    } catch(Exception e) {
	        Error("Incompatible types", acceptor);
	    }
	}

	public void visit(AbsBlockStmt acceptor) { acceptor.stmts.accept(this); }

	
	public void visit(AbsCallExpr acceptor) {
        acceptor.name.accept(this);
        acceptor.args.accept(this);
        SemDesc.setActualType(acceptor, SemDesc.getActualType(acceptor.name));
	}
	
	public void visit(AbsConstDecl acceptor) {
		acceptor.value.accept(this);
		
		SemType type = SemDesc.getActualType(acceptor.value);
		AbsDecl decl = SemDesc.getNameDecl(acceptor.name);
		SemDesc.setActualType(decl, type);
		SemDesc.setActualType(acceptor, type);
		SemDesc.setActualType(acceptor.name, type);
	}

	public void visit(AbsDeclName acceptor) {}

	public void visit(AbsDecls acceptor) { for(AbsDecl decl : acceptor.decls) decl.accept(this); }

	public void visit(AbsExprStmt acceptor) { acceptor.expr.accept(this); }

	public void visit(AbsForStmt acceptor) {
	    acceptor.name.accept(this);

		try {
		    SemType type = SemDesc.getActualType(acceptor.name);
    		if(((SemAtomType)type).type!=SemAtomType.INT) throw new Exception();
        } catch(Exception e) {
		    Error("Invalid iterator type", acceptor);
        }
        
	    acceptor.loBound.accept(this);
	    acceptor.hiBound.accept(this);
	    
		try {
		    SemType 
		        lo = SemDesc.getActualType(acceptor.loBound),
		        hi = SemDesc.getActualType(acceptor.hiBound);
	        if(lo instanceof SemSubprogramType) lo = ((SemSubprogramType)lo).getResultType();
	        if(hi instanceof SemSubprogramType) hi = ((SemSubprogramType)hi).getResultType();
		
            if(((SemAtomType)lo).type!=SemAtomType.INT 
            || ((SemAtomType)hi).type!=SemAtomType.INT) throw new Exception();		    
		} catch(Exception e) {
		    Error("Invalid loop range", acceptor);
		}
		
		acceptor.stmt.accept(this);
	}
	
	public void visit(AbsFunDecl acceptor) {
	    boolean block = inFunc;
	    inFunc = true;
        acceptor.pars.accept(this);
        acceptor.type.accept(this);
        acceptor.decls.accept(this);
        
        SemSubprogramType funType = new SemSubprogramType(SemDesc.getActualType(acceptor.type));//return type
        for(AbsDecl decl : acceptor.pars.decls) funType.addParType(SemDesc.getActualType(decl));//parameter types
		SemDesc.setActualType(acceptor, funType);
        acceptor.stmt.accept(this);
	    if(!block) inFunc = false;
    }
	
	public void visit(AbsIfStmt acceptor) {
	    acceptor.cond.accept(this);
	    acceptor.thenStmt.accept(this);
	    acceptor.elseStmt.accept(this);
	}
	
	public void visit(AbsNilConst acceptor) {
		SemDesc.setActualType(acceptor, new SemPointerType(new SemAtomType(SemAtomType.VOID)));
	}
	
	public void visit(AbsPointerType acceptor) { 
		acceptor.type.accept(this);
		SemDesc.setActualType(acceptor, new SemPointerType(SemDesc.getActualType(acceptor.type)));
	}
	
	public void visit(AbsProcDecl acceptor) {
	    boolean block = inProc;
	    inProc = true;
        acceptor.pars.accept(this);
        acceptor.decls.accept(this);

        SemSubprogramType procType = new SemSubprogramType(new SemAtomType(SemAtomType.VOID));//return type
        for(AbsDecl decl : acceptor.pars.decls) procType.addParType(SemDesc.getActualType(decl));//parameter types
		SemDesc.setActualType(acceptor, procType);
        acceptor.stmt.accept(this);
	    if(!block) inProc = false;
	}
	
	public void visit(AbsProgram acceptor) {
        acceptor.decls.accept(this);
        
        acceptor.stmt.accept(this);
	}
	
	public void visit(AbsRecordType acceptor) {
	    recordlvl++;
	    records.add(new SemRecordType());
        acceptor.fields.accept(this);
		SemDesc.setActualType(acceptor, records.get(records.size()-1));
	    records.remove(recordlvl);
        recordlvl--;
	}

	public void visit(AbsStmts acceptor) { for(AbsStmt stmt : acceptor.stmts) stmt.accept(this); }

	public void visit(AbsTypeDecl acceptor) {
		acceptor.type.accept(this);
		acceptor.name.accept(this);
		SemType type = SemDesc.getActualType(acceptor.type);
		SemDesc.setActualType(acceptor, type);
		SemDesc.setActualType(acceptor.name, type);
		if(type==null) Error("Invalid type", acceptor);
	}
	
	public void visit(AbsTypeName acceptor) {
        AbsDecl decl = SemDesc.getNameDecl(acceptor);
		SemType type = SemDesc.getActualType(decl);
        if(type!=null) SemDesc.setActualType(acceptor, type);
		if(type==null) Error("Unknown type", acceptor);
	}
	
	public void visit(AbsUnExpr acceptor) {
		acceptor.expr.accept(this);
		SemType type = SemDesc.getActualType(acceptor.expr);
		SemDesc.setActualType(acceptor, type);
		
		try {
		    if(type!=null) switch(acceptor.oper) {
		        case AbsUnExpr.ADD:case AbsUnExpr.SUB: 
		            if(((SemAtomType)type).type==SemAtomType.INT) 
		                SemDesc.setActualType(acceptor, type); 
	                else
	                    throw new Exception();
	                break;
		        case AbsUnExpr.NOT:
		            if(((SemAtomType)type).type==SemAtomType.BOOL) 
		                SemDesc.setActualType(acceptor, type); 
	                else
	                    throw new Exception();
	                break;
		        case AbsUnExpr.MEM:
			        SemDesc.setActualType(acceptor, new SemPointerType(type));
			        break;
		        case AbsUnExpr.VAL:
			        SemDesc.setActualType(acceptor, ((SemPointerType)type).type);
			        break;
		    }
	    } catch (Exception e) {
	        Error("Invalid unexpr", acceptor);
	    }
	}

	public void visit(AbsValExprs acceptor) { for(AbsValExpr expr : acceptor.exprs) expr.accept(this); }

	public void visit(AbsValName acceptor) {
		AbsDecl decl = SemDesc.getNameDecl(acceptor);
		SemType type = SemDesc.getActualType(decl);		
		if(decl!=null && type==null) {
		    decl.accept(this);
		    type = SemDesc.getActualType(decl);		
		}
		if(type!=null) SemDesc.setActualType(acceptor, type);
	}

    ArrayList<AbsVarDecl> todoTypes = new ArrayList<AbsVarDecl>();
	
	public void visit(AbsVarDecl acceptor) {
		acceptor.type.accept(this);
		if(acceptor.type instanceof AbsAtomType && ((AbsAtomType)acceptor.type).type == AbsAtomType.AUTO) {
            todoTypes.add(acceptor);
		    return;
	    }
		
		SemType type = SemDesc.getActualType(acceptor.type);
        if(type!=null) SemDesc.setActualType(acceptor, type);
		if(type==null) Error("Unknown type", acceptor);
        
        if(recordlvl>=0) {
            records.get(records.size()-1).addField(acceptor.name, type);
        }
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
	    if(inProc && acceptor.expr == null)
    	    return;
	    else if(inFunc && acceptor.expr != null)
    	    acceptor.expr.accept(this);
	    else
	        Error("Illegal return", acceptor);
	}
}
