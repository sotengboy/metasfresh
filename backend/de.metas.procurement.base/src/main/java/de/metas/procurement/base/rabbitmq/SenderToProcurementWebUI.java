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

import com.fasterxml.jackson.core.JsonProcessingException;
import de.metas.common.procurement.sync.Constants;
import de.metas.common.procurement.sync.protocol.ProcurementEvent;
import lombok.NonNull;
import org.adempiere.exceptions.AdempiereException;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class SenderToProcurementWebUI
{
	private final RabbitTemplate rabbitTemplate;
	private final Queue queue;

	public SenderToProcurementWebUI(
			@NonNull final RabbitTemplate rabbitTemplate,
			@NonNull @Qualifier(Constants.QUEUE_NAME_MF_TO_PW) final Queue queue
	)
	{
		this.rabbitTemplate = rabbitTemplate;
		this.queue = queue;
	}

	public void send(@NonNull final ProcurementEvent procurementEvent)
	{
		final String message = convertToString(procurementEvent);
		rabbitTemplate.convertAndSend(queue.getName(), message);
	}

	private String convertToString(@NonNull final ProcurementEvent procurementEvent)
	{
		try
		{
			return Constants.PROCUREMENT_WEBUI_OBJECT_MAPPER.writeValueAsString(procurementEvent);
		}
		catch (final JsonProcessingException e)
		{
			throw AdempiereException.wrapIfNeeded(e)
					.appendParametersToMessage()
					.setParameter("procurementEvent", procurementEvent);
		}
	}
}
