/*
 * #%L
 * de.metas.procurement.base
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

package de.metas.procurement.base.rabbitmq;

import de.metas.common.procurement.sync.Constants;
import de.metas.common.procurement.sync.protocol.RequestToMetasfresh;
import de.metas.common.procurement.sync.protocol.dto.SyncBPartner;
import de.metas.common.procurement.sync.protocol.dto.SyncProduct;
import de.metas.common.procurement.sync.protocol.request_to_metasfresh.GetAllBPartnersRequest;
import de.metas.common.procurement.sync.protocol.request_to_metasfresh.GetAllProductsRequest;
import de.metas.common.procurement.sync.protocol.request_to_metasfresh.GetInfoMessageRequest;
import de.metas.common.procurement.sync.protocol.request_to_metasfresh.PutProductSuppliesRequest;
import de.metas.common.procurement.sync.protocol.request_to_metasfresh.PutWeeklySupplyRequest;
import de.metas.common.procurement.sync.protocol.request_to_procurementweb.PutBPartnersRequest;
import de.metas.common.procurement.sync.protocol.request_to_procurementweb.PutInfoMessageRequest;
import de.metas.common.procurement.sync.protocol.request_to_procurementweb.PutProductsRequest;
import de.metas.common.procurement.sync.protocol.request_to_procurementweb.PutRfQChangeRequest;
import de.metas.procurement.base.IServerSyncBL;
import de.metas.util.Services;
import lombok.NonNull;
import org.adempiere.exceptions.AdempiereException;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import java.io.IOException;
import java.util.List;

@RabbitListener(queues = Constants.QUEUE_NAME_PW_TO_MF)
public class ReceiverFromProcurementWeb
{
	private final SenderToProcurementWeb senderToProcurementWebUI;

	public ReceiverFromProcurementWeb(@NonNull final SenderToProcurementWeb senderToProcurementWebUI)
	{
		this.senderToProcurementWebUI = senderToProcurementWebUI;
	}

	@RabbitHandler
	public void receiveMessage(@NonNull final String message)
	{
		try
		{
			final RequestToMetasfresh procurementEvent = Constants.PROCUREMENT_WEBUI_OBJECT_MAPPER.readValue(message, RequestToMetasfresh.class);
			invokeServerSyncBL(procurementEvent);
		}
		catch (final IOException e)
		{
			throw AdempiereException.wrapIfNeeded(e)
					.appendParametersToMessage()
					.setParameter("message", message);
		}
	}

	private void invokeServerSyncBL(@NonNull final RequestToMetasfresh procurementEvent)
	{
		final IServerSyncBL serverSyncBL = Services.get(IServerSyncBL.class);
		if (procurementEvent instanceof PutWeeklySupplyRequest)
		{
			serverSyncBL.reportWeekSupply((PutWeeklySupplyRequest)procurementEvent);
		}
		else if (procurementEvent instanceof PutProductSuppliesRequest)
		{
			serverSyncBL.reportProductSupplies((PutProductSuppliesRequest)procurementEvent);
		}
		else if (procurementEvent instanceof PutRfQChangeRequest)
		{
			serverSyncBL.reportRfQChanges((PutRfQChangeRequest)procurementEvent);
		}
		else if (procurementEvent instanceof GetAllBPartnersRequest)
		{
			final List<SyncBPartner> allBPartners = serverSyncBL.getAllBPartners();
			senderToProcurementWebUI.send(PutBPartnersRequest.of(allBPartners));
		}
		else if (procurementEvent instanceof GetAllProductsRequest)
		{
			final List<SyncProduct> allProducts = serverSyncBL.getAllProducts();
			senderToProcurementWebUI.send(PutProductsRequest.of(allProducts));
		}
		else if(procurementEvent instanceof GetInfoMessageRequest)
		{
			final String infoMessage = serverSyncBL.getInfoMessage();
			senderToProcurementWebUI.send(PutInfoMessageRequest.of(infoMessage));
		}
		else
		{
			throw new AdempiereException("Unsupported type " + procurementEvent.getClass().getName())
					.appendParametersToMessage()
					.setParameter("procurementEvent", procurementEvent);
		}
	}
}
