package compiler.frames;

import compiler.abstree.*;
import compiler.abstree.tree.*;
import compiler.semanal.*;

public class FrmEvaluator implements AbsVisitor {

    boolean funcCall = false;
    int sizeArgs = 0;
    
    public void visit(AbsAtomType acceptor) {}
    
    public void visit(AbsConstDecl acceptor) {}
    
    public void visit(AbsFunDecl acceptor) {
        FrmFrame frame = new FrmFrame(acceptor, SemDesc.getScope(acceptor));
        for (AbsDecl decl : acceptor.pars.decls) {
            if (decl instanceof AbsVarDecl) {        
                AbsVarDecl varDecl = (AbsVarDecl)decl;
                FrmArgAccess access = new FrmArgAccess(varDecl, frame);
                FrmDesc.setAccess(varDecl, access);
            }
        }
        for (AbsDecl decl : acceptor.decls.decls) {
            if (decl instanceof AbsVarDecl) {        
                AbsVarDecl varDecl = (AbsVarDecl)decl;
                FrmLocAccess access = new FrmLocAccess(varDecl, frame);
                frame.locVars.add(access);
                FrmDesc.setAccess(varDecl, access);
            }
            decl.accept(this);
        }
        sizeArgs = 0;
        funcCall = false;
        acceptor.stmt.accept(this);
        frame.sizeArgs = sizeArgs;
        if(funcCall) frame.sizeArgs += 4;
        
        FrmDesc.setFrame(acceptor, frame);
    }
    
    public void visit(AbsProgram acceptor) {
        FrmFrame frame = new FrmFrame(acceptor, 1);
        for (AbsDecl decl : acceptor.decls.decls) {
            if (decl instanceof AbsVarDecl) {
                AbsVarDecl varDecl = (AbsVarDecl)decl;
                FrmVarAccess access = new FrmVarAccess(varDecl);
                FrmDesc.setAccess(varDecl, access);
            }
            decl.accept(this);
        }
        sizeArgs = 0;
        funcCall = false;
        acceptor.stmt.accept(this);//main
        frame.sizeArgs = sizeArgs;
        if(funcCall) frame.sizeArgs += 4;
        
        FrmDesc.setFrame(acceptor, frame);
    }
    
    public void visit(AbsProcDecl acceptor) {
        FrmFrame frame = new FrmFrame(acceptor, SemDesc.getScope(acceptor));
        for (AbsDecl pars : acceptor.pars.decls) {
            if (pars instanceof AbsVarDecl) {        
                AbsVarDecl varDecl = (AbsVarDecl)pars;
                FrmArgAccess access = new FrmArgAccess(varDecl, frame);
                FrmDesc.setAccess(varDecl, access);
            }
        }
        for (AbsDecl decl : acceptor.decls.decls) {
            if (decl instanceof AbsVarDecl) {        
                AbsVarDecl varDecl = (AbsVarDecl)decl;
                FrmLocAccess access = new FrmLocAccess(varDecl, frame);
                frame.locVars.add(access);
                FrmDesc.setAccess(varDecl, access);
            }
            decl.accept(this);
        }
        sizeArgs = 0;
        funcCall = false;
        acceptor.stmt.accept(this);
        frame.sizeArgs = sizeArgs;
        if(funcCall) frame.sizeArgs += 4;
        
        FrmDesc.setFrame(acceptor, frame);
    }
    
    public void visit(AbsRecordType acceptor) {
        int offset = 0;
        for (AbsDecl decl : acceptor.fields.decls) {
            if (decl instanceof AbsVarDecl) {
                AbsVarDecl varDecl = (AbsVarDecl)decl;
                FrmCmpAccess access = new FrmCmpAccess(varDecl, offset);
                FrmDesc.setAccess(varDecl, access);
                offset = offset + SemDesc.getActualType(varDecl.type).size();
            }
            decl.accept(this);
		}
    }
    
    public void visit(AbsTypeDecl acceptor) { acceptor.type.accept(this); }

    public void visit(AbsVarDecl acceptor) { acceptor.type.accept(this); }

    public void visit(AbsAlloc acceptor) { acceptor.type.accept(this); }

    public void visit(AbsArrayType acceptor) { acceptor.type.accept(this); }

    public void visit(AbsAssignStmt acceptor) {
        acceptor.dstExpr.accept(this);
        acceptor.srcExpr.accept(this);
    }

    public void visit(AbsAtomConst acceptor) {}

    public void visit(AbsBinExpr acceptor) {
        acceptor.fstExpr.accept(this);
        acceptor.sndExpr.accept(this);
    }

    public void visit(AbsBlockStmt acceptor) { acceptor.stmts.accept(this); }

    public void visit(AbsCallExpr acceptor) {
        funcCall = true;
        int callSize = 0;
        for(AbsValExpr expr : acceptor.args.exprs) callSize += SemDesc.getActualType(expr).size();
        if(callSize > sizeArgs) sizeArgs = callSize;        
    }

    public void visit(AbsDeclName acceptor) {}

    public void visit(AbsDecls acceptor) { for (AbsDecl decl : acceptor.decls) decl.accept(this); }

    public void visit(AbsExprStmt acceptor) { acceptor.expr.accept(this); }

    public void visit(AbsForStmt acceptor) {
        acceptor.name.accept(this);
        acceptor.loBound.accept(this);
        acceptor.hiBound.accept(this);
    }

    public void visit(AbsIfStmt acceptor) {
        acceptor.cond.accept(this);
        acceptor.thenStmt.accept(this);
        acceptor.elseStmt.accept(this);
    }

    public void visit(AbsNilConst acceptor) {}

    public void visit(AbsPointerType acceptor) { acceptor.type.accept(this); }

    public void visit(AbsStmts acceptor) { for (AbsStmt s : acceptor.stmts) s.accept(this); }

    public void visit(AbsTypeName acceptor) {}

    public void visit(AbsUnExpr acceptor) { acceptor.expr.accept(this); }

    public void visit(AbsValExprs acceptor) { for (AbsValExpr expr : acceptor.exprs) expr.accept(this); }

    public void visit(AbsValName acceptor) {}

    public void visit(AbsWhileStmt acceptor) {
        acceptor.cond.accept(this);
        acceptor.stmt.accept(this);
    }
    
    public void visit(AbsRepeatStmt acceptor) {
        acceptor.cond.accept(this);
        acceptor.stmts.accept(this);
    }
}
