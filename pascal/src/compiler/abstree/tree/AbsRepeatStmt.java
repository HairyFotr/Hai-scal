package compiler.abstree.tree;

import compiler.abstree.AbsVisitor;

/**
 * Stavek 'repeat'.
 */
public class AbsRepeatStmt extends AbsStmt {

	/** Pogoj. */
	public AbsValExpr cond;
	
	/** Stavek. */
	public AbsStmts stmts;
	
	public AbsRepeatStmt(AbsValExpr cond, AbsStmts stmts) {
		this.cond = cond;
		this.stmts = stmts;
	}

	public void accept(AbsVisitor visitor) {
		visitor.visit(this);
	}

}
