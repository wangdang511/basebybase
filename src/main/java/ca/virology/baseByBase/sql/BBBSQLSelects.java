package ca.virology.baseByBase.sql;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;

import ca.virology.lib.DAL.dbExceptions.InvalidOperatorException;
import ca.virology.lib.DAL.dbExceptions.UnknownExpressionUnitException;
import ca.virology.lib.DAL.dbSQLBuilder.AggregateExpression;
import ca.virology.lib.DAL.dbSQLBuilder.ComparisonExpression;
import ca.virology.lib.DAL.dbSQLBuilder.Expression;
import ca.virology.lib.DAL.dbSQLBuilder.GroupBy;
import ca.virology.lib.DAL.dbSQLBuilder.LogicalExpression;
import ca.virology.lib.DAL.dbSQLBuilder.OrderBy;
import ca.virology.lib.DAL.dbSQLBuilder.SQLBuilder;
import ca.virology.lib.DAL.dbSQLBuilder.SQLConstants;
import ca.virology.lib.DAL.dbSQLBuilder.Select;
import ca.virology.lib.DAL.dbSQLBuilder.SingularExpression;
import ca.virology.lib.DAL.dbSQLBuilder.Where;
import ca.virology.lib.DAL.dbStructure.Column;
import ca.virology.lib.DAL.dbStructure.Table;
import ca.virology.lib.server.ServerRequestException;
import ca.virology.lib.util.common.Logger;
import ca.virology.lib.vocsdbAccess.SQLAccess;

/**
 * SQLSelects - Contains all SQL SELECT functions to
 * query the VOCS database
 *
 * @author Tim Teh @ Univerity of Victoria
 * @Version 1.0
 */
public class BBBSQLSelects {

    /**
     * Queries the database with:
     * SELECT virus_abbrev_name, virus_size, virus_dna_seq FROM viruses WHERE virusID = virusID
     *
     * @param virusID    for the WHERE clause of the select
     * @param vocsAccess - the Vocs Access Object
     * @return the result set as a vector
     */
    public static Vector virusDNASequenceSelect(int virusID, SQLAccess vocsAccess) {
        Vector rs = new Vector();
        //create the select: SELECT virus_abbrev_name, virus_size, virus_dna_seq FROM viruses
        Select select = new Select();
        select.addColumnToSelect((vocsAccess.getVocsDB()).getTable("genome").getColumn("genome_abbr"));
        select.addVocsFunctionToSelect(SQLConstants.VIRUS_BP_SIZE);
        select.addColumnToSelect((vocsAccess.getVocsDB()).getTable("genome").getColumn("genome_seq"));
        //create the where: WHERE virus_id=virusId
        Expression expression = null;
        try {
            expression = new ComparisonExpression(new SingularExpression((vocsAccess.getVocsDB()).getTable("genome").getColumn("genome_id")),
                    new SingularExpression("" + virusID, SQLConstants.NUMBEROPERAND),
                    SQLConstants.EQ);
        } catch (InvalidOperatorException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database Query Error\n " +
                            "Please contact administrator.", "VOCS Server Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (UnknownExpressionUnitException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database Query Error\n " +
                            "Please contact administrator.", "VOCS Server Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        Where where = new Where();
        where.addWhereClause(expression);
        //build the sql for the specified database
        SQLBuilder selectBuilder = new SQLBuilder(vocsAccess.getVocsDB(), vocsAccess.getDBType(), select, where);
        //run the query
        Logger.println("The select statement sent from virusDNASequenceSelect: " + selectBuilder.getSQL());
        try {
            rs = (vocsAccess.getQueryHandler()).doSelectQuery(selectBuilder);
        } catch (ServerRequestException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database Query Error\n " +
                            "Please contact administrator.", "VOCS Server Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        return rs;

    }

    /**
     * Queries the database with:
     * SELECT gene_id, gene_name, orf_start, orf_stop,gene_strand FROM genes, gene_orfs WHERE virus_id = viruse_id AND gene_fragment = 0 AND join condition ORDER BY gene_orf_start ASC
     *
     * @param virusID    for the WHERE clause
     * @param vocsAccess - the Vocs Access Object
     * @return the result set as a vector
     */
    public static Vector geneOrfInfoSelect(int virusID, SQLAccess vocsAccess) {
        Vector rs = new Vector();
        //create the select: SELECT gene_id, gene_name, orf_start, orf_stop,gene_strand FROM genes, gene_orfs
        Select select = new Select();

        select.addColumnToSelect((vocsAccess.getVocsDB()).getTable("gene").getColumn("gene_id"));
        select.addColumnToSelect((vocsAccess.getVocsDB()).getTable("gene").getColumn("gene_abbr"));
        select.addColumnToSelect((vocsAccess.getVocsDB()).getTable("gene_orfs").getColumn("orf_start"));
        select.addColumnToSelect((vocsAccess.getVocsDB()).getTable("gene_orfs").getColumn("orf_stop"));
        select.addColumnToSelect((vocsAccess.getVocsDB()).getTable("gene").getColumn("strand"));

        //create the where:  WHERE virus_id = viruse_id AND gene_fragment = 0 AND genes.gene_id=gene_orfs.orf_gene_id
        Expression expression = null;
        List expressions = new LinkedList();
        try {
            Expression exp1 = new ComparisonExpression(new SingularExpression((vocsAccess.getVocsDB()).getTable("gene").getColumn("genome_id")),
                    new SingularExpression("" + virusID, SQLConstants.NUMBEROPERAND),
                    SQLConstants.EQ);
            expressions.add(exp1);

            Expression exp2_1 = new ComparisonExpression(new SingularExpression((vocsAccess.getVocsDB()).getTable("gene").getColumn("molecule_type")),
                    new SingularExpression("protein", SQLConstants.STRINGOPERAND),
                    SQLConstants.EQ);
            Expression exp2_2 = new ComparisonExpression(new SingularExpression((vocsAccess.getVocsDB()).getTable("gene").getColumn("molecule_type")),
                    new SingularExpression("polyprotein", SQLConstants.STRINGOPERAND),
                    SQLConstants.EQ);
            Expression exp2 = new LogicalExpression(exp2_1, exp2_2, SQLConstants.OR);
            expressions.add(exp2);

            Expression exp3 = new ComparisonExpression(new SingularExpression((vocsAccess.getVocsDB()).getTable("gene_orfs").getColumn("position")),
                    new SingularExpression("1", SQLConstants.NUMBEROPERAND),
                    SQLConstants.EQ);
            expressions.add(exp3);
            expression = new LogicalExpression(expressions, SQLConstants.AND);
        } catch (InvalidOperatorException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database Query Error\n " +
                            "Please contact administrator.", "VOCS Server Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (UnknownExpressionUnitException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database Query Error\n " +
                            "Please contact administrator.", "VOCS Server Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        Where where = new Where();
        where.addWhereClause(expression);

        //add order by
        OrderBy orderby = new OrderBy();
        orderby.addOrderByASC((vocsAccess.getVocsDB()).getTable("gene_orfs").getColumn("orf_start"));

        //build the sql
        SQLBuilder selectBuilder = new SQLBuilder(vocsAccess.getVocsDB(), vocsAccess.getDBType(), select, where, orderby);
        Logger.println("The select statement sent from geneOrfInfoSelect: " + selectBuilder.getSQL());
        //run the query
        try {
            rs = (vocsAccess.getQueryHandler()).doSelectQuery(selectBuilder);
        } catch (ServerRequestException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database Query Error\n " +
                            "Please contact administrator.", "VOCS Server Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        return rs;
    }


    // have to execute geneOrthologNameSelect and geneOrthologIDSelect as two separate SQL statements because the WhereHandler built into virology-lib
    //  doesn't allow for more than one table to be in accessed in the same where expression

    /**
     * Queries the database with:
     * SELECT name FROM ortholog_group WHERE ortholog_group.ortholog_group_id=orthologID
     *
     * @param geneName   for the call to geneOrthologIDSelect
     * @param vocsAccess = the Vocs Access Object
     * @return the result as a String
     */
    public static String geneOrthologNameSelect(String geneName, SQLAccess vocsAccess) {
        Vector orthologName = new Vector();
        Vector orthologID = geneOrthologIDSelect(geneName, vocsAccess);

        Integer orthoID = Integer.parseInt(((Object[]) (orthologID.elementAt(0)))[0].toString());

        if (orthoID <= 0) return "";
        else {
            // run the query
            try {
                orthologName = (vocsAccess.getQueryHandler()).doRawSelectQuery("SELECT name FROM ortholog_group WHERE ortholog_group_id='" + orthoID + "'");
            } catch (ServerRequestException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Database Query Error\n" + "Please contact administrator.", "VOCS Server Error", JOptionPane.ERROR_MESSAGE);
            }

            return ((Object[]) (orthologName.elementAt(0)))[0].toString();
        }
    }

    /**
     * Queries the database with:
     * SELECT ortholog_group_id FROM genes WHERE gene_abbr=geneName
     *
     * @param geneName   for the WHERE clause
     * @param vocsAccess = the Vocs Access Object
     * @return the result as an int
     */
    public static Vector geneOrthologIDSelect(String geneName, SQLAccess vocsAccess) {
        Vector orthologID = new Vector();
        //run the query
        try {
            orthologID = (vocsAccess.getQueryHandler()).doRawSelectQuery("SELECT ortholog_group_id FROM gene WHERE gene_abbr='" + geneName + "'");
        } catch (ServerRequestException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database Query Error\n " +
                            "Please contact administrator.", "VOCS Server Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        return orthologID;
    }

    /**
     * Queries the database with:
     * SELECT virus_name, virus_size FROM viruses WHERE virusID = virusID
     *
     * @param vocsAccess - the Vocs Access Object
     * @return the result set as a vector
     */
    public static Vector virusNameandSizeSelect(int virusID, SQLAccess vocsAccess) {
        Vector rs = new Vector();
        //create the select: SELECT virus_name, virus_size FROM viruses
        Select select = new Select();
        select.addColumnToSelect((vocsAccess.getVocsDB()).getTable("genome").getColumn("genome_abbr"));
        select.addVocsFunctionToSelect(SQLConstants.VIRUS_BP_SIZE);
        //create the where: WHERE virus_id=virusId
        ComparisonExpression expression = null;
        try {
            expression = new ComparisonExpression(new SingularExpression((vocsAccess.getVocsDB()).getTable("genome").getColumn("genome_id")),
                    new SingularExpression("" + virusID, SQLConstants.NUMBEROPERAND),
                    SQLConstants.EQ);
        } catch (InvalidOperatorException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database Query Error\n " +
                            "Please contact administrator.", "VOCS Server Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (UnknownExpressionUnitException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database Query Error\n " +
                            "Please contact administrator.", "VOCS Server Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        Where where = new Where();
        where.addWhereClause(expression);
        //build the sql for the specified database
        SQLBuilder selectBuilder = new SQLBuilder(vocsAccess.getVocsDB(), vocsAccess.getDBType(), select, where);
        //run the query
        Logger.println("The select statement sent from virusNameandSizeSelect: " + selectBuilder.getSQL());

        try {
            rs = (vocsAccess.getQueryHandler()).doSelectQuery(selectBuilder);
        } catch (ServerRequestException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database Query Error\n " +
                            "Please contact administrator.", "VOCS Server Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        return rs;
    }

    /**
     * Queries the database with:
     * SELECT virus_id,virus_name FROM viruses ORDER BY name ASC
     *
     * @param vocsAccess - the Vocs Access Object
     * @return the result set as a vector
     */
    public static Vector virusIDandNameSelect(SQLAccess vocsAccess) {
        Vector rs = new Vector();
        //create the select: SELECT virus_id, virus_name FROM viruses
        Select select = new Select();

        select.addColumnToSelect((vocsAccess.getVocsDB()).getTable("genome").getColumn("genome_id"));
        select.addColumnToSelect((vocsAccess.getVocsDB()).getTable("genome").getColumn("genome_name"));

        //add order by
        OrderBy orderby = new OrderBy();
        orderby.addOrderByASC((vocsAccess.getVocsDB()).getTable("genome").getColumn("genome_name"));

        //build the sql for the specified database
        SQLBuilder selectBuilder = new SQLBuilder(vocsAccess.getVocsDB(), vocsAccess.getDBType(), select, orderby);
        Logger.println("The select statement sent from virusIDandNameSelect: " + selectBuilder.getSQL());
        //run the query
        try {
            rs = (vocsAccess.getQueryHandler()).doSelectQuery(selectBuilder);
        } catch (ServerRequestException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database Query Error\n " +
                            "Please contact administrator.", "VOCS Server Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        return rs;
    }


}
