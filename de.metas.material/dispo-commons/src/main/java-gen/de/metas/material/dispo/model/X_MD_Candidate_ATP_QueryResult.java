/** Generated Model - DO NOT CHANGE */
package de.metas.material.dispo.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Properties;

/** Generated Model for MD_Candidate_ATP_QueryResult
 *  @author Adempiere (generated) 
 */
@SuppressWarnings("javadoc")
public class X_MD_Candidate_ATP_QueryResult extends org.compiere.model.PO implements I_MD_Candidate_ATP_QueryResult, org.compiere.model.I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = -24794103L;

    /** Standard Constructor */
    public X_MD_Candidate_ATP_QueryResult (Properties ctx, int MD_Candidate_ATP_QueryResult_ID, String trxName)
    {
      super (ctx, MD_Candidate_ATP_QueryResult_ID, trxName);
      /** if (MD_Candidate_ATP_QueryResult_ID == 0)
        {
        } */
    }

    /** Load Constructor */
    public X_MD_Candidate_ATP_QueryResult (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }


    /** Load Meta Data */
    @Override
    protected org.compiere.model.POInfo initPO (Properties ctx)
    {
      org.compiere.model.POInfo poi = org.compiere.model.POInfo.getPOInfo (ctx, Table_Name, get_TrxName());
      return poi;
    }

	/** Set Kunde.
		@param C_BPartner_Customer_ID Kunde	  */
	@Override
	public void setC_BPartner_Customer_ID (int C_BPartner_Customer_ID)
	{
		if (C_BPartner_Customer_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_C_BPartner_Customer_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_C_BPartner_Customer_ID, Integer.valueOf(C_BPartner_Customer_ID));
	}

	/** Get Kunde.
		@return Kunde	  */
	@Override
	public int getC_BPartner_Customer_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_BPartner_Customer_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Plandatum.
		@param DateProjected Plandatum	  */
	@Override
	public void setDateProjected (java.sql.Timestamp DateProjected)
	{
		set_ValueNoCheck (COLUMNNAME_DateProjected, DateProjected);
	}

	/** Get Plandatum.
		@return Plandatum	  */
	@Override
	public java.sql.Timestamp getDateProjected () 
	{
		return (java.sql.Timestamp)get_Value(COLUMNNAME_DateProjected);
	}

	/** Set Produkt.
		@param M_Product_ID 
		Produkt, Leistung, Artikel
	  */
	@Override
	public void setM_Product_ID (int M_Product_ID)
	{
		if (M_Product_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_M_Product_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_M_Product_ID, Integer.valueOf(M_Product_ID));
	}

	/** Get Produkt.
		@return Produkt, Leistung, Artikel
	  */
	@Override
	public int getM_Product_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_Product_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Lager.
		@param M_Warehouse_ID 
		Lager oder Ort für Dienstleistung
	  */
	@Override
	public void setM_Warehouse_ID (int M_Warehouse_ID)
	{
		if (M_Warehouse_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_M_Warehouse_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_M_Warehouse_ID, Integer.valueOf(M_Warehouse_ID));
	}

	/** Get Lager.
		@return Lager oder Ort für Dienstleistung
	  */
	@Override
	public int getM_Warehouse_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_Warehouse_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Menge.
		@param Qty 
		Menge
	  */
	@Override
	public void setQty (java.math.BigDecimal Qty)
	{
		set_ValueNoCheck (COLUMNNAME_Qty, Qty);
	}

	/** Get Menge.
		@return Menge
	  */
	@Override
	public java.math.BigDecimal getQty () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_Qty);
		if (bd == null)
			 return BigDecimal.ZERO;
		return bd;
	}

	/** Set Reihenfolge.
		@param SeqNo 
		Zur Bestimmung der Reihenfolge der Einträge; die kleinste Zahl kommt zuerst
	  */
	@Override
	public void setSeqNo (int SeqNo)
	{
		set_ValueNoCheck (COLUMNNAME_SeqNo, Integer.valueOf(SeqNo));
	}

	/** Get Reihenfolge.
		@return Zur Bestimmung der Reihenfolge der Einträge; die kleinste Zahl kommt zuerst
	  */
	@Override
	public int getSeqNo () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_SeqNo);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set StorageAttributesKey (technical).
		@param StorageAttributesKey StorageAttributesKey (technical)	  */
	@Override
	public void setStorageAttributesKey (java.lang.String StorageAttributesKey)
	{
		set_ValueNoCheck (COLUMNNAME_StorageAttributesKey, StorageAttributesKey);
	}

	/** Get StorageAttributesKey (technical).
		@return StorageAttributesKey (technical)	  */
	@Override
	public java.lang.String getStorageAttributesKey () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_StorageAttributesKey);
	}
}