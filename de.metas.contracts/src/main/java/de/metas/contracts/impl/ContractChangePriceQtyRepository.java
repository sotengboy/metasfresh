/**
 *
 */
package de.metas.contracts.impl;

import java.math.BigDecimal;
import java.util.List;

import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.util.Services;
import org.springframework.stereotype.Component;

import de.metas.contracts.model.I_C_Flatrate_Term;
import de.metas.contracts.model.I_C_SubscriptionProgress;
import de.metas.contracts.model.X_C_SubscriptionProgress;
import de.metas.contracts.subscription.ISubscriptionDAO;
import de.metas.contracts.subscription.ISubscriptionDAO.SubscriptionProgressQuery;
import de.metas.invoicecandidate.api.IInvoiceCandidateHandlerBL;
import lombok.NonNull;

/*
 * #%L
 * de.metas.contracts
 * %%
 * Copyright (C) 2017 metas GmbH
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

/**
 * @author metas-dev <dev@metasfresh.com>
 *
 */
@Component
public class ContractChangePriceQtyRepository
{
	public void changeFlatrateTermPrice(@NonNull final I_C_Flatrate_Term term, @NonNull final BigDecimal price)
	{
		term.setPriceActual(price);
		InterfaceWrapperHelper.save(term);

		invalidateInvoiceCandidatesOfFlatrateTerm(term);
	}

	public void changeFlatrateTermQty(@NonNull final I_C_Flatrate_Term term, @NonNull final BigDecimal qty)
	{
		term.setPlannedQtyPerUnit(qty);
		InterfaceWrapperHelper.save(term);

		invalidateInvoiceCandidatesOfFlatrateTerm(term);
	}

	private void invalidateInvoiceCandidatesOfFlatrateTerm(@NonNull final I_C_Flatrate_Term term)
	{
		Services.get(IInvoiceCandidateHandlerBL.class).invalidateCandidatesFor(term);
	}

	public void changeQtyInSubscriptionProgressOfFlatrateTerm(@NonNull final I_C_Flatrate_Term term, @NonNull final BigDecimal qty)
	{
		final ISubscriptionDAO subscriptionPA = Services.get(ISubscriptionDAO.class);

		final List<I_C_SubscriptionProgress> deliveries = subscriptionPA.retrieveSubscriptionProgresses(SubscriptionProgressQuery.builder()
				.term(term)
				.excludedStatus(X_C_SubscriptionProgress.STATUS_Done)
				.excludedStatus(X_C_SubscriptionProgress.STATUS_Delivered)
				.excludedStatus(X_C_SubscriptionProgress.STATUS_InPicking)
				.build());

		deliveries.forEach(delivery -> {
					delivery.setQty(qty);
					InterfaceWrapperHelper.save(delivery);
				});

	}
}
