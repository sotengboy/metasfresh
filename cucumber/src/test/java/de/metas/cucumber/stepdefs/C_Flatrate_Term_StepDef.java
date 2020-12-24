/*
 * #%L
 * de.metas.cucumber
 * %%
 * Copyright (C) 2020 metas GmbH
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

package de.metas.cucumber.stepdefs;

import de.metas.bpartner.BPartnerId;
import de.metas.bpartner.service.IBPartnerDAO;
import de.metas.contracts.FlatrateTermId;
import de.metas.contracts.FlatrateTransitionId;
import de.metas.contracts.IFlatrateDAO;
import de.metas.contracts.model.I_C_Flatrate_Conditions;
import de.metas.contracts.model.I_C_Flatrate_Data;
import de.metas.contracts.model.I_C_Flatrate_Term;
import de.metas.contracts.model.X_C_Flatrate_Conditions;
import de.metas.contracts.model.X_C_Flatrate_Term;
import de.metas.order.InvoiceRule;
import de.metas.util.Services;
import io.cucumber.java.en.Given;
import lombok.NonNull;
import org.adempiere.model.InterfaceWrapperHelper;
import org.compiere.model.I_C_BPartner;
import org.compiere.model.I_C_BPartner_Location;
import org.compiere.model.I_M_Product;
import org.compiere.util.Env;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class C_Flatrate_Term_StepDef
{
	private final StepDefData<I_C_BPartner> bpartnerTable;
	private final StepDefData<I_M_Product> productTable;
	private final StepDefData<I_C_Flatrate_Term> contractTable;
	private final IFlatrateDAO flatrateDAO = Services.get(IFlatrateDAO.class);
	private final IBPartnerDAO bPartnerDAO = Services.get(IBPartnerDAO.class);

	public C_Flatrate_Term_StepDef(
			@NonNull final StepDefData<I_C_BPartner> bpartnerTable,
			@NonNull final StepDefData<I_M_Product> productTable,
			@NonNull final StepDefData<I_C_Flatrate_Term> contractTable)
	{
		this.bpartnerTable = bpartnerTable;
		this.productTable = productTable;
		this.contractTable = contractTable;
	}

	@Given("metasfresh contains procurement C_Flatrate_Terms:")
	public void metasfresh_contains_c_flatrate_terms(@NonNull final io.cucumber.datatable.DataTable dataTable)
	{
		final I_C_Flatrate_Conditions flatrateConditions = flatrateDAO
				.retrieveConditions(Env.getCtx())
				.stream()
				.filter(c -> c.getName().equals("Procurement-Test") && c.getAD_Org_ID() == StepDefConstants.ORG_ID.getRepoId())
				.findAny()
				.orElseGet(() -> InterfaceWrapperHelper.newInstance(I_C_Flatrate_Conditions.class));

		flatrateConditions.setName("Procurement-Test");
		flatrateConditions.setAD_Org_ID(StepDefConstants.ORG_ID.getRepoId());
		flatrateConditions.setType_Conditions(X_C_Flatrate_Conditions.TYPE_CONDITIONS_Procurement);
		flatrateConditions.setC_Flatrate_Transition_ID(StepDefConstants.FLATRATE_TRANSITION_ID.getRepoId());
		flatrateConditions.setInvoiceRule(InvoiceRule.AfterDelivery.getCode());
		flatrateConditions.setDocStatus(X_C_Flatrate_Conditions.DOCSTATUS_Completed);
		flatrateConditions.setProcessed(true);
		InterfaceWrapperHelper.saveRecord(flatrateConditions);

		final List<Map<String, String>> tableRows = dataTable.asMaps(String.class, String.class);
		for (final Map<String, String> tableRow : tableRows)
		{
			final String bPartnerIdentifier = tableRow.get("C_BPartner.RecordIdentifier");
			assertThat(bPartnerIdentifier).as("C_BPartner.RecordIdentifier is mandatory").isNotBlank();
			final I_C_BPartner bPartner = bpartnerTable.get(bPartnerIdentifier);

			final I_C_BPartner_Location bPartnerLocation = bPartnerDAO.retrieveBPartnerLocation(IBPartnerDAO.BPartnerLocationQuery.builder().bpartnerId(BPartnerId.ofRepoId(bPartner.getC_BPartner_ID()))
					.type(IBPartnerDAO.BPartnerLocationQuery.Type.BILL_TO)
					.build());
			assertThat(bPartnerLocation).isNotNull(); // guard

			final I_C_Flatrate_Data flatrateData = flatrateDAO.retriveOrCreateFlatrateData(bPartner);

			final I_C_Flatrate_Term contractRecord = InterfaceWrapperHelper.newInstance(I_C_Flatrate_Term.class);
			contractRecord.setC_Flatrate_Conditions_ID(flatrateConditions.getC_Flatrate_Conditions_ID());
			contractRecord.setC_Flatrate_Data_ID(flatrateData.getC_Flatrate_Data_ID());
			contractRecord.setDocStatus(X_C_Flatrate_Term.DOCSTATUS_Completed);
			contractRecord.setProcessed(true);
			contractRecord.setBill_BPartner_ID(bPartner.getC_BPartner_ID());
			contractRecord.setBill_Location_ID(bPartnerLocation.getC_BPartner_Location_ID());
			contractRecord.setAD_Org_ID(StepDefConstants.ORG_ID.getRepoId());

			final String productIdentifier = tableRow.get("M_Product.RecordIdentifier");
			assertThat(productIdentifier).as("M_Product.RecordIdentifier is mandatory").isNotBlank();
			final I_M_Product product = productTable.get(productIdentifier);
			contractRecord.setM_Product_ID(product.getM_Product_ID());

			contractRecord.setStartDate(DataTableUtil.extractDateTimestampForColumnName(tableRow, "StartDate"));
			contractRecord.setEndDate(DataTableUtil.extractDateTimestampForColumnName(tableRow, "EndDate"));

			InterfaceWrapperHelper.saveRecord(contractRecord);

			contractTable.put(DataTableUtil.extractRecordIdentifier(tableRow, "C_FlatrateTerm"), contractRecord);
		}
	}
}
