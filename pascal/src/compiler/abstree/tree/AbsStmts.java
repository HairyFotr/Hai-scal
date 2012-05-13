package compiler.abstree.tree;

import java.util.*;

import compiler.abstree.AbsVisitor;

/**
 * Stavki: seznam stavkov.
 */
public class AbsStmts extends AbsTree {
	
	/** Seznam stavkov. */
	public LinkedList<AbsStmt> stmts;
	
	public AbsStmts() {
		stmts = new LinkedList<AbsStmt>();
	}
	public AbsStmts(AbsStmt... stmts) {
		this();
        this.stmts.addAll(Arrays.asList(stmts));
	}

	public void accept(AbsVisitor visitor) {
		visitor.visit(this);
	}

}
