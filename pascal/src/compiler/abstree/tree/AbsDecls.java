package compiler.abstree.tree;

import java.util.*;

import compiler.abstree.AbsVisitor;

/**
 * Deklaracije: seznam deklaracij.
 */
public class AbsDecls extends AbsTree {
	
	/** Seznam deklaracij. */
	public LinkedList<AbsDecl> decls;
	
	public AbsDecls() {
		decls = new LinkedList<AbsDecl>();
	}
	public AbsDecls(AbsDecl... decls) {
		this();
        this.decls.addAll(Arrays.asList(decls));
	}
	public void accept(AbsVisitor visitor) {
		visitor.visit(this);
	}

}
