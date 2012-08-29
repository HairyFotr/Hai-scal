package compiler.imcode;

import java.io.*;
import java.util.*;

public class ImcSEQ extends ImcStmt {

	/* Stavki.  */
	public LinkedList<ImcStmt> stmts;

	public ImcSEQ() {
		stmts = new LinkedList<ImcStmt>();
	}

	@Override
	public void toXML(PrintStream xml) {
		xml.print("<imcnode kind=\"SEQ\">\n");
		Iterator<ImcStmt> stmts = this.stmts.iterator();
		while (stmts.hasNext()) {
			ImcStmt stmt = stmts.next();
			stmt.toXML(xml);
		}
		xml.print("</imcnode>\n");
	}

	@Override
	public ImcSEQ linear() {
		ImcSEQ lin = new ImcSEQ();
		Iterator<ImcStmt> stmts = this.stmts.iterator();
		int limiter = 5000;
		while (stmts.hasNext() && limiter>0) {
			ImcStmt stmt = stmts.next();
			if(stmt==null) {
			    limiter--;
			    continue;
		    }
			ImcSEQ linStmt = stmt.linear();
			lin.stmts.addAll(linStmt.stmts);
		}
		return lin;
	}

}
