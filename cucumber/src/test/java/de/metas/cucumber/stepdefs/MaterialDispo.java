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

import de.metas.cucumber.stepdefs.MaterialDispoTable.MaterialDispoTableRow;
import de.metas.material.dispo.commons.candidate.CandidateBusinessCase;
import de.metas.material.dispo.commons.candidate.CandidateType;
import de.metas.material.dispo.commons.candidate.MaterialDispoDataItem;
import de.metas.material.dispo.commons.candidate.MaterialDispoRecordRepository;
import de.metas.material.dispo.model.I_MD_Candidate;
import de.metas.material.event.PostMaterialEventService;
import de.metas.material.event.commons.AttributesKey;
import de.metas.material.event.commons.EventDescriptor;
import de.metas.material.event.commons.MaterialDescriptor;
import de.metas.material.event.commons.OrderLineDescriptor;
import de.metas.material.event.commons.ProductDescriptor;
import de.metas.material.event.shipmentschedule.ShipmentScheduleCreatedEvent;
import de.metas.organization.OrgId;
import de.metas.util.Services;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.NonNull;
import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.ad.trx.api.ITrxManager;
import org.adempiere.mm.attributes.AttributeSetInstanceId;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.service.ClientId;
import org.adempiere.warehouse.WarehouseId;
import org.compiere.SpringContextHolder;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.TimeUtil;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class MaterialDispo
{
	private static final WarehouseId WAREHOUSE_ID = WarehouseId.ofRepoId(540008);
	public static final OrgId ORG_ID = OrgId.ofRepoId(1000000);

	private PostMaterialEventService postMaterialEventService;
	private MaterialDispoRecordRepository materialDispoRecordRepository;

	@Before
	public void beforeEach()
	{
		postMaterialEventService = SpringContextHolder.instance.getBean(PostMaterialEventService.class);
		materialDispoRecordRepository = SpringContextHolder.instance.getBean(MaterialDispoRecordRepository.class);
		Env.setClientId(Env.getCtx(), ClientId.METASFRESH);
	}

	@Given("metasfresh initially has no MD_Candidate data")
	public void setupMD_Candidate_Data()
	{
		truncateMDCandidateData();
	}

	private void truncateMDCandidateData()
	{
		DB.executeUpdateEx("TRUNCATE TABLE MD_Candidate cascade", ITrx.TRXNAME_None);
	}

	@When("metasfresh receives a shipmentScheduleCreatedEvent")
	public void shipmentScheduleCreatedEvent(@NonNull final DataTable dataTable)
	{
		final Map<String, String> map = dataTable.asMaps().get(0);

		final int productId = Integer.parseInt(map.get("M_Product_ID"));
		final Instant preparationDate = Instant.parse(map.get("PreparationDate"));
		final BigDecimal qty = new BigDecimal(map.get("Qty"));

		final ShipmentScheduleCreatedEvent shipmentScheduleCreatedEvent = ShipmentScheduleCreatedEvent.builder()
				.eventDescriptor(EventDescriptor.ofClientAndOrg(ClientId.METASFRESH.getRepoId(), ORG_ID.getRepoId()))
				.materialDescriptor(MaterialDescriptor.builder()
						.productDescriptor(ProductDescriptor.completeForProductIdAndEmptyAttribute(productId))
						.date(preparationDate)
						.quantity(qty)
						.warehouseId(WAREHOUSE_ID)
						.build())
				.shipmentScheduleId(11)
				.reservedQuantity(qty)
				.documentLineDescriptor(OrderLineDescriptor.builder().orderId(10).orderLineId(20).docTypeId(30).orderBPartnerId(40).build())
				.build();

		postMaterialEventService.postEventNow(shipmentScheduleCreatedEvent);
	}

	@When("metasfresh initially has this MD_Candidate data")
	public void metasfresh_has_this_md_candidate_data1(@NonNull final MaterialDispoTable table)
	{
		truncateMDCandidateData();
		//		Services.get(ITrxManager.class)
		//				.runOutOfTransaction(trxName -> {
		for (final MaterialDispoTableRow tableRow : table.getRows())
		{
			final I_MD_Candidate mdCandidateRecord = InterfaceWrapperHelper.newInstance(I_MD_Candidate.class);
			mdCandidateRecord.setAD_Org_ID(ORG_ID.getRepoId());
			mdCandidateRecord.setM_Product_ID(tableRow.getProductId().getRepoId());
			mdCandidateRecord.setM_AttributeSetInstance_ID(AttributeSetInstanceId.NONE.getRepoId());
			mdCandidateRecord.setStorageAttributesKey(AttributesKey.NONE.getAsString());
			mdCandidateRecord.setM_Warehouse_ID(WAREHOUSE_ID.getRepoId());
			mdCandidateRecord.setMD_Candidate_Type(tableRow.getType().getCode());
			mdCandidateRecord.setMD_Candidate_BusinessCase(CandidateBusinessCase.toCode(tableRow.getBusinessCase()));
			mdCandidateRecord.setQty(tableRow.getQty());
			mdCandidateRecord.setDateProjected(TimeUtil.asTimestamp(tableRow.getTime()));
			InterfaceWrapperHelper.saveRecord(mdCandidateRecord);

			final I_MD_Candidate mdStockCandidateRecord = InterfaceWrapperHelper.newInstance(I_MD_Candidate.class);
			mdStockCandidateRecord.setAD_Org_ID(ORG_ID.getRepoId());
			mdStockCandidateRecord.setM_Product_ID(tableRow.getProductId().getRepoId());
			mdStockCandidateRecord.setM_AttributeSetInstance_ID(AttributeSetInstanceId.NONE.getRepoId());
			mdStockCandidateRecord.setStorageAttributesKey(AttributesKey.NONE.getAsString());
			mdStockCandidateRecord.setM_Warehouse_ID(WAREHOUSE_ID.getRepoId());
			mdStockCandidateRecord.setMD_Candidate_Type(CandidateType.STOCK.getCode());
			final boolean isDemand = CandidateType.DEMAND.equals(tableRow.getType()) || CandidateType.INVENTORY_DOWN.equals(tableRow.getType());
			if (isDemand)
			{
				mdStockCandidateRecord.setMD_Candidate_Parent_ID(mdCandidateRecord.getMD_Candidate_ID());
			}
			mdStockCandidateRecord.setQty(tableRow.getAtp());
			mdStockCandidateRecord.setDateProjected(TimeUtil.asTimestamp(tableRow.getTime()));
			InterfaceWrapperHelper.saveRecord(mdStockCandidateRecord);

			final boolean isSupply = CandidateType.SUPPLY.equals(tableRow.getType()) || CandidateType.INVENTORY_UP.equals(tableRow.getType());
			if (isSupply)
			{
				mdCandidateRecord.setMD_Candidate_Parent_ID(mdStockCandidateRecord.getMD_Candidate_ID());
				InterfaceWrapperHelper.saveRecord(mdCandidateRecord);
			}

			if (!isDemand && !isSupply)
			{
				fail("Unsupported type " + tableRow.getType());
			}
		}
		//				});
	}

	@Then("metasfresh has this MD_Candidate data")
	public void metasfresh_has_this_md_candidate_data(@NonNull final MaterialDispoTable table)
	{
		for (final MaterialDispoTableRow tableRow : table.getRows())
		{
			final MaterialDispoDataItem materialDispoRecord = materialDispoRecordRepository.getBy(tableRow.createQuery());
			assertThat(materialDispoRecord).isNotNull(); // add message

			assertThat(materialDispoRecord.getType()).isEqualTo(tableRow.getType());
			assertThat(materialDispoRecord.getBusinessCase()).isEqualTo(tableRow.getBusinessCase());
			assertThat(materialDispoRecord.getMaterialDescriptor().getProductId()).isEqualTo(tableRow.getProductId().getRepoId());
			assertThat(materialDispoRecord.getMaterialDescriptor().getDate()).isEqualTo(tableRow.getTime());
			assertThat(materialDispoRecord.getMaterialDescriptor().getQuantity()).isEqualByComparingTo(tableRow.getQty());
			assertThat(materialDispoRecord.getAtp()).isEqualByComparingTo(tableRow.getAtp());
		}
	}

	@Then("metasfresh has this MD_Candidate_Demand_Detail data")
	public void metasfresh_has_this_md_candidate_demand_detail_data(@NonNull final DataTable dataTable)
	{
		// Write code here that turns the phrase above into concrete actions
		// For automatic transformation, change DataTable to one of
		// E, List<E>, List<List<E>>, List<Map<K,V>>, Map<K,V> or
		// Map<K, List<V>>. E,K,V must be a String, Integer, Float,
		// Double, Byte, Short, Long, BigInteger or BigDecimal.
		//
		// For other transformations you can register a DataTableType.
		//	throw new io.cucumber.java.PendingException();
	}
}
