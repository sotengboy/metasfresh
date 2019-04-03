package de.metas.material.cockpit.availableforsales;

import static de.metas.util.Check.assume;

import java.util.List;

import org.adempiere.ad.dao.ConstantQueryFilter;
import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.ad.dao.impl.TypedSqlQuery;
import org.compiere.Adempiere;
import org.compiere.db.Database;
import org.compiere.model.IQuery;
import org.compiere.util.TimeUtil;

import de.metas.material.cockpit.model.I_MD_Available_For_Sales_QueryResult;
import de.metas.util.Services;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/*
 * #%L
 * metasfresh-available-for-sales
 * %%
 * Copyright (C) 2019 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

@UtilityClass
public class AvailableForSalesSqlHelper
{
	public IQuery<I_MD_Available_For_Sales_QueryResult> createDBQueryForAvailableForSalesMultiQuery(@NonNull final AvailableForSalesMultiQuery multiQuery)
	{
		final List<AvailableForSalesQuery> availableForSalesQueries = multiQuery.getAvailableForSalesQueries();
		final IQueryBL queryBL = Services.get(IQueryBL.class);

		if (availableForSalesQueries.isEmpty())
		{
			return queryBL
					.createQueryBuilder(I_MD_Available_For_Sales_QueryResult.class)
					.filter(ConstantQueryFilter.of(false))
					.create();
		}

		final IQuery<I_MD_Available_For_Sales_QueryResult> dbQuery = createDBQueryForAvailableForSalesQuery(0, availableForSalesQueries.get(0));
		for (int i = 1; i < availableForSalesQueries.size(); i++)
		{
			dbQuery.addUnion(createDBQueryForAvailableForSalesQuery(i, availableForSalesQueries.get(i)), false/* distinct */);
		}

		return dbQuery;
	}

	private IQuery<I_MD_Available_For_Sales_QueryResult> createDBQueryForAvailableForSalesQuery(
			int queryNo,
			@NonNull final AvailableForSalesQuery availableForSalesQuery)
	{
		final IQuery<I_MD_Available_For_Sales_QueryResult> dbQuery = Services
				.get(IQueryBL.class)
				.createQueryBuilder(I_MD_Available_For_Sales_QueryResult.class)
				.create();

		assume(isRealSqlQuery(), "Unit test mode is currently not supported");
		assume(dbQuery instanceof TypedSqlQuery, "If we are not in unit test mode, then our query has to be a sql query; query={}", dbQuery);

		final TypedSqlQuery<I_MD_Available_For_Sales_QueryResult> sqlDbQuery = (TypedSqlQuery<I_MD_Available_For_Sales_QueryResult>)dbQuery;

		final String dateString = Database.TO_DATE(TimeUtil.asTimestamp(availableForSalesQuery.getDateOfInterest()), false/* dayOnly */);

		sqlDbQuery.setSqlFrom("de_metas_material.retrieve_available_for_sales("
				+ "p_QueryNo => " + queryNo
				+ ", p_M_Product_ID => " + availableForSalesQuery.getProductId()
				+ ", p_StorageAttributesKey => '" + availableForSalesQuery.getStorageAttributesKey() + "'"
				+ ", p_PreparationDate => " + dateString
				+ ", p_shipmentDateLookAheadHours => " + availableForSalesQuery.getShipmentDateLookAheadHours()
				+ ", p_salesOrderLookBehindHours => " + availableForSalesQuery.getSalesOrderLookBehindHours()
				+ ")");

		return dbQuery;
	}

	private boolean isRealSqlQuery()
	{
		final boolean isRealSqlQuery = !Adempiere.isUnitTestMode();
		return isRealSqlQuery;
	}
}
