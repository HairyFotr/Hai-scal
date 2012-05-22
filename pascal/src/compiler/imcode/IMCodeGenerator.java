package compiler.imcode;

import compiler.abstree.*;
import compiler.abstree.tree.*;
import compiler.semanal.*;
import compiler.frames.*;
import java.util.*;

/*Sestavite generator vmesne kode, ki izracuna fragmente vmesne kode:
    ImcCodeChunk vsebuje celotno vmesno kodo funkcije (brez vgnezdenih funkcij) v obliki enega ImcMOVE ukaza, s katerim izraz funkcije priredimo zacasni spremenljivki RV.
    ImcDataChunk vsebuje opis globalne spremenljivke.

V klicni zapis sta dodani dve zacasni spremenljivki:
    RV vsebuje rezultat funkcije.
    FP vsebuje kazalec na klicni zapis.

Ukazi vmesne kode se delijo na dve vrsti: ukazi za izraze in ukazi za stavke.
Ukazi za izraze:
    ImcBINOP(op,left,right): vrne vrednost binarnega izraza z operatorjem op podizrazoma left in right.
    ImcCALL(label,args): vrne vrednost funkcije na naslovu label pri argumentih args (prvi argument je vrednost staticnega linka); vsak argument je predstavljen kot izraz vmesne kode, ki izracuna njegovo vrednost.
    ImcCONST(value): vrne int vrednost value.
    ImcESEQ(stmt,value): najprej izvede stavek stmt, nato vrne vrednost value.
    ImcMEM(expr): dostop do naslova expr (ce je ImcMEM neposredni levi sin ukaza ImcMOVE, pomeni pisanje v pomnilnik, sicer pomeni branje iz pomnilnika).
    ImcNAME(label): vrne naslov label.
    ImcTEMP(temp): vrne vrednost zacasne spremenljivke temp.

Ukazi za stavke:
    ImcCJUMP(cond,true,false): ce je vrednost pogoja cond razlicna od 0, skoci na oznako true, sicer skoci na oznako false.
    ImcEXP(expr): izracuna in zavrze rezultat izraza expr.
    ImcJUMP(label): skoci na oznako label.
    ImcLABEL(label): oznaka label.
    ImcMOVE(dst,src): shrani vrednost src na naslov dst.
    ImcSEQ(stmts): zaporedoma izvede stavke stmts.

P.S. Dodeljevanje pomnilnika z izrazom oblike [<type>] prevedete v klic funkcije malloc(<typesize>), kjer je <typesize> velikost posameznega podatka tipa <type> (v bytih).*/

public class IMCodeGenerator implements AbsVisitor {
	
	/** Zaporedje delov kode.  */
	public static LinkedList<ImcChunk> chunks;

    boolean funcCall = false;
    int sizeArgs = 0;

    //
    public void visit(AbsAtomType acceptor) {}
    
    public void visit(AbsConstDecl acceptor) {}
    
    public void visit(AbsFunDecl acceptor) {
        for (AbsDecl decl : acceptor.decls.decls) {
            decl.accept(this);
        }
        for (AbsDecl decl : acceptor.pars.decls) {
            decl.accept(this);
        }
        acceptor.stmt.accept(this);
    }
    
    public void visit(AbsProgram acceptor) {
        for (AbsDecl decl : acceptor.decls.decls) {
            decl.accept(this);
        }
        acceptor.stmt.accept(this);//main
    }
    
    public void visit(AbsProcDecl acceptor) {
        for (AbsDecl decl : acceptor.decls.decls) {
            decl.accept(this);
        }
        for (AbsDecl decl : acceptor.pars.decls) {
            decl.accept(this);
        }
        acceptor.stmt.accept(this);
    }
    
    public void visit(AbsRecordType acceptor) {
        for (AbsDecl decl : acceptor.fields.decls) {
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
 

}
