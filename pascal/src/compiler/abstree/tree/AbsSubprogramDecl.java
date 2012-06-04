package compiler.abstree.tree;

/**
 * Deklaracije: procedura.
 */
public abstract class AbsSubprogramDecl extends AbsDecl {

	/** Ime. */
	public AbsDeclName name;

	/** Parametri. */
	public AbsDecls pars;

	/** Deklaracije. */
	public AbsDecls decls;

	/** Stavek. */
	public AbsBlockStmt stmt;
	
}
