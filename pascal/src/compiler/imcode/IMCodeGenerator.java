package compiler.imcode;

import compiler.abstree.*;
import compiler.abstree.tree.*;
import compiler.semanal.*;
import compiler.semanal.type.*;
import compiler.frames.*;
import compiler.imcode.*;
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
	public static LinkedList<ImcChunk> chunks = new LinkedList<ImcChunk>();
	ImcCode code;
	boolean mem = true;

    public void visit(AbsAlloc acceptor) {
        ImcCALL malloc = new ImcCALL(FrmLabel.newLabel("malloc"));
        int argSize = SemDesc.getActualType(acceptor.type).size();
        malloc.args.add(new ImcCONST(SystemStubs.FAKE_FP));
        malloc.size.add(4);
        malloc.args.add(new ImcCONST(argSize));
        malloc.size.add(4);
        code = (ImcCode)malloc;
    }
    
    public void visit(AbsAtomType acceptor) {}
    
    public void visit(AbsAtomConst acceptor) {
        int val = 0;
        switch(acceptor.type) {
            case AbsAtomConst.BOOL: val = acceptor.value.equals("true")? 1 : 0; break;
            case AbsAtomConst.CHAR: val = (int)acceptor.value.charAt(1); break;
            case AbsAtomConst.INT:  val = Integer.parseInt(acceptor.value); break;         
        }
        code = new ImcCONST(val);
    }
    public void visit(AbsUnExpr acceptor) { 
        acceptor.expr.accept(this);
        
        switch(acceptor.oper) {
            case AbsUnExpr.ADD: break;//nop 
            case AbsUnExpr.SUB: code = new ImcBINOP(ImcBINOP.SUB, (ImcExpr)(new ImcCONST(0)), (ImcExpr)code); break;
            case AbsUnExpr.NOT: code = new ImcBINOP(ImcBINOP.NEQ, (ImcExpr)code, (ImcExpr)code); break;
            case AbsUnExpr.MEM: code = ((ImcMEM)code).expr; break;
            case AbsUnExpr.VAL: code = new ImcMEM((ImcExpr)code); break;
        }
    }
    
    public void visit(AbsBinExpr acceptor) {
        switch(acceptor.oper) {
            case AbsBinExpr.RECACCESS:
                mem = false;
                acceptor.fstExpr.accept(this);
                ImcExpr record = (ImcExpr)code;
                SemRecordType recordType = (SemRecordType)SemDesc.getActualType(acceptor.fstExpr);
                String member = ((AbsValName)acceptor.sndExpr).name;
                
                SemType memberType = null;
                int offset = 0;
                for(int i=0; i<recordType.getNumFields(); i++) {
                    if(member.equals(recordType.getFieldName(i).name)) {
                        memberType = recordType.getFieldType(i);
                        break;
                    }
                    offset += recordType.getFieldType(i).size();
                }
                
                if(memberType instanceof SemRecordType) 
                    code = new ImcBINOP(ImcBINOP.ADD, record, new ImcCONST(offset));
                else
                    code = new ImcMEM(new ImcBINOP(ImcBINOP.ADD, record, new ImcCONST(offset)));
                
                mem = true;
            break;
            case AbsBinExpr.ARRACCESS:
                mem = false;
                acceptor.fstExpr.accept(this);
                ImcExpr array = (ImcExpr)code;
                SemArrayType arrayType = (SemArrayType)SemDesc.getActualType(acceptor.fstExpr);
                
                mem = true;
                acceptor.fstExpr.accept(this);
                ImcExpr index = (ImcExpr)code;
                mem = false;

                ImcBINOP arrIndex = new ImcBINOP(ImcBINOP.SUB, index, new ImcCONST(arrayType.loBound));
                ImcBINOP arrOffset = new ImcBINOP(ImcBINOP.MUL, arrIndex, new ImcCONST(arrayType.type.size()));

                code = new ImcMEM(new ImcBINOP(ImcBINOP.ADD, array, arrOffset));
                mem = true;
            break;            
            default:
                acceptor.fstExpr.accept(this);
                ImcExpr fstExpr = (ImcExpr)code;
                acceptor.sndExpr.accept(this);
                ImcExpr sndExpr = (ImcExpr)code;
                
                code = new ImcBINOP(acceptor.oper, fstExpr, sndExpr);
            break;
        }        
    }

    public void visit(AbsConstDecl acceptor) {}
    
    public void visit(AbsProgram acceptor) {
        for (AbsDecl decl : acceptor.decls.decls) if (decl instanceof AbsVarDecl) {//global vars
            AbsVarDecl varDecl = (AbsVarDecl)decl;
            FrmVarAccess varAccess = (FrmVarAccess)FrmDesc.getAccess(varDecl);
            SemType varType = SemDesc.getActualType(varDecl.type);
            chunks.add(new ImcDataChunk(varAccess.label, varType.size()));
        }
        acceptor.decls.accept(this);//funcs

        acceptor.stmt.accept(this);//main
        chunks.add(new ImcCodeChunk(FrmDesc.getFrame(acceptor), (ImcStmt)code));
    }
    
    public void visit(AbsFunDecl acceptor) {
        acceptor.decls.accept(this);
        acceptor.stmt.accept(this);
        chunks.add(new ImcCodeChunk(FrmDesc.getFrame(acceptor), (ImcStmt)code));
    }
    public void visit(AbsProcDecl acceptor) {
        acceptor.decls.accept(this);
        acceptor.stmt.accept(this);
        chunks.add(new ImcCodeChunk(FrmDesc.getFrame(acceptor), (ImcStmt)code));
    }
    
    public void visit(AbsRecordType acceptor) {
        for (AbsDecl decl : acceptor.fields.decls) {
            decl.accept(this);
		}
    }
    
    public void visit(AbsTypeDecl acceptor) { acceptor.type.accept(this); }

    public void visit(AbsVarDecl acceptor) { 
        FrmVarAccess varAccess = (FrmVarAccess)FrmDesc.getAccess(acceptor);
        SemType type = SemDesc.getActualType(acceptor.type);
        ImcDataChunk chunk = new ImcDataChunk(varAccess.label, type.size());
        chunks.add(chunk);
        acceptor.type.accept(this); 
    }

    public void visit(AbsArrayType acceptor) { acceptor.type.accept(this); }

    public void visit(AbsAssignStmt acceptor) {
        acceptor.dstExpr.accept(this);
        ImcExpr dst = (ImcExpr)code;
        acceptor.srcExpr.accept(this);
        ImcExpr src = (ImcExpr)code;
        
        code = new ImcMOVE(dst, src);
    }

    public void visit(AbsBlockStmt acceptor) { acceptor.stmts.accept(this); }

    public void visit(AbsCallExpr acceptor) {
        FrmFrame frm = FrmDesc.getFrame(SemDesc.getNameDecl(acceptor.name));
        ImcCALL call = null;
        if(SystemStubs.isSys(acceptor.name.name)) {
            call = new ImcCALL(FrmLabel.newLabel(acceptor.name.name));
            call.args.add(new ImcCONST(SystemStubs.FAKE_FP));
            call.size.add(4);
        } else {
            call = new ImcCALL(frm.label);
            call.args.add(new ImcTEMP(frm.FP));
            call.size.add(4);
        }
        for(AbsValExpr expr : acceptor.args.exprs) {
            expr.accept(this);
            call.args.add((ImcExpr)code);
            call.size.add(4);
        }
        code = call;
    }

    public void visit(AbsDeclName acceptor) {}

    public void visit(AbsDecls acceptor) { 
        for (AbsDecl decl : acceptor.decls) 
            if(decl instanceof AbsFunDecl || decl instanceof AbsProcDecl)
                decl.accept(this);
    }

    public void visit(AbsExprStmt acceptor) { 
        acceptor.expr.accept(this);
        code = new ImcEXP((ImcExpr)code);
    }

    public void visit(AbsForStmt acceptor) {
        ImcSEQ seq = new ImcSEQ();
        
        //System.out.println(code.getClass().toString()+"1");
        acceptor.name.accept(this);
        //System.out.println(code.getClass().toString()+"2");
        ImcExpr name = (code instanceof ImcEXP)?((ImcEXP)code).expr:(ImcExpr)code;
        acceptor.loBound.accept(this);
        ImcExpr lo = (ImcExpr)code;
        acceptor.hiBound.accept(this);
        ImcExpr hi = (ImcExpr)code;
        
        ImcLABEL tl = new ImcLABEL(FrmLabel.newLabel());
        ImcLABEL fl = new ImcLABEL(FrmLabel.newLabel());
        ImcLABEL sl = new ImcLABEL(FrmLabel.newLabel());
        
        seq.stmts.add(new ImcMOVE(name, lo));
        seq.stmts.add(sl);
            seq.stmts.add(new ImcCJUMP(new ImcBINOP(ImcBINOP.LEQ, name, hi), tl.label, fl.label));
        seq.stmts.add(tl);
            acceptor.stmt.accept(this);
            seq.stmts.add((ImcStmt)code);
            seq.stmts.add(new ImcMOVE(name, new ImcBINOP(ImcBINOP.ADD, name, new ImcCONST(1))));
            seq.stmts.add(new ImcJUMP(sl.label));
        seq.stmts.add(fl);
        
        code = seq;
    }

    public void visit(AbsIfStmt acceptor) {
        ImcSEQ seq = new ImcSEQ();
        
        acceptor.cond.accept(this);
        ImcExpr cond = (ImcExpr)code;
        
        ImcLABEL tl = new ImcLABEL(FrmLabel.newLabel());
        ImcLABEL fl = new ImcLABEL(FrmLabel.newLabel());
        ImcLABEL el = new ImcLABEL(FrmLabel.newLabel());
        
        seq.stmts.add(new ImcCJUMP(cond, tl.label, fl.label));
        seq.stmts.add(tl);
            acceptor.thenStmt.accept(this);
            seq.stmts.add((ImcStmt)code);
            seq.stmts.add(new ImcJUMP(el.label));
        seq.stmts.add(fl);
            acceptor.elseStmt.accept(this);
            seq.stmts.add((ImcStmt)code);
        seq.stmts.add(el);
        
        code = seq;
    }

    public void visit(AbsNilConst acceptor) {
        code = new ImcCONST(0);
    }

    public void visit(AbsPointerType acceptor) { acceptor.type.accept(this); }

    public void visit(AbsStmts acceptor) { 
        ImcSEQ stmts = new ImcSEQ();
        for (AbsStmt stmt : acceptor.stmts) {
            stmt.accept(this);
            stmts.stmts.add((ImcStmt)code);
        }
        code = stmts;
    }

    public void visit(AbsTypeName acceptor) {}

    public void visit(AbsValExprs acceptor) { for (AbsValExpr expr : acceptor.exprs) expr.accept(this); }

    public void visit(AbsValName acceptor) {
        AbsDecl decl = SemDesc.getNameDecl(acceptor);
        FrmAccess access = FrmDesc.getAccess(decl);
        
        if(access instanceof FrmVarAccess || acceptor instanceof AbsValName || decl instanceof AbsVarDecl) {
            FrmVarAccess varAccess = (FrmVarAccess)access;
            code = new ImcNAME((access==null)?FrmLabel.newLabel(acceptor.name):varAccess.label);
            if(mem) code = new ImcMEM((ImcNAME)code);
        }
        if(access instanceof FrmArgAccess) {
            FrmArgAccess argAccess = (FrmArgAccess)access;
            code = new ImcBINOP(ImcBINOP.ADD, new ImcTEMP(argAccess.frame.FP), new ImcCONST(argAccess.offset));
            if(mem) code = new ImcMEM((ImcBINOP)code);
        }
        if(access instanceof FrmLocAccess) {
            FrmLocAccess locAccess = (FrmLocAccess)access;
            code = new ImcBINOP(ImcBINOP.ADD, new ImcTEMP(locAccess.frame.FP), new ImcCONST(locAccess.offset));
            if(mem) code = new ImcMEM((ImcBINOP)code);
        } 
        
        if(decl instanceof AbsFunDecl) {
            FrmFrame frm = FrmDesc.getFrame(decl);
            code = new ImcTEMP(frm.RV);
            if(mem) code = new ImcMEM((ImcTEMP)code);
            
            SemType type = SemDesc.getActualType(decl);
            if(type instanceof SemRecordType || type instanceof SemArrayType) {
                code = new ImcMEM((ImcExpr)code);
            }
        } else
        if(decl instanceof AbsConstDecl) {
            code = new ImcCONST(SemDesc.getActualConst(decl));
        } else {
            //System.out.println(acceptor.getClass().toString());
            //System.out.println(decl.getClass().toString());
        }
    }

    public void visit(AbsWhileStmt acceptor) {
        acceptor.cond.accept(this);
        acceptor.stmt.accept(this);
    }
 

}
