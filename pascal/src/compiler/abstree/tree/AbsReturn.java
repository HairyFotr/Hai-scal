package compiler.abstree.tree;

import compiler.abstree.AbsVisitor;

public class AbsReturn extends AbsStmt {

	public AbsValExpr expr;
	
	public AbsReturn(AbsValExpr expr) {
		this.expr = expr;
	}

	public void accept(AbsVisitor visitor) {
		visitor.visit(this);
	}

}
