/*
 * #%L
 * metasfresh-webui-api
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

package de.metas.servicerepair.customerreturns.process;

import de.metas.document.engine.DocStatus;
import de.metas.inout.IInOutDAO;
import de.metas.inout.InOutId;
import de.metas.process.IProcessPrecondition;
import de.metas.process.IProcessPreconditionsContext;
import de.metas.process.JavaProcess;
import de.metas.process.ProcessExecutionResult;
import de.metas.process.ProcessPreconditionsResolution;
import de.metas.servicerepair.customerreturns.HUsToReturnViewContext;
import de.metas.servicerepair.customerreturns.HUsToReturnViewFactory;
import de.metas.ui.web.view.CreateViewRequest;
import de.metas.ui.web.view.IView;
import de.metas.ui.web.view.IViewsRepository;
import de.metas.util.Services;
import lombok.NonNull;
import org.compiere.SpringContextHolder;
import org.compiere.model.I_M_InOut;

public class CustomerReturns_OpenHUToReturn extends JavaProcess implements IProcessPrecondition
{
	private final IInOutDAO inOutDAO = Services.get(IInOutDAO.class);
	private final transient IViewsRepository viewsRepo = SpringContextHolder.instance.getBean(IViewsRepository.class);

	@Override
	public ProcessPreconditionsResolution checkPreconditionsApplicable(final @NonNull IProcessPreconditionsContext context)
	{
		if (!context.isSingleSelection())
		{
			return ProcessPreconditionsResolution.rejectBecauseNotSingleSelection();
		}

		final I_M_InOut customerReturns = inOutDAO.getById(InOutId.ofRepoId(context.getSingleSelectedRecordId()));
		final DocStatus docStatus = DocStatus.ofCode(customerReturns.getDocStatus());
		if (!docStatus.isDrafted())
		{
			return ProcessPreconditionsResolution.rejectWithInternalReason("document status should be Drafted");
		}

		return ProcessPreconditionsResolution.accept();
	}

	@Override
	protected String doIt()
	{
		final ProcessExecutionResult.WebuiViewToOpen viewToOpen = openServiceHUEditorView();
		getResult().setWebuiViewToOpen(viewToOpen);
		return MSG_OK;
	}

	private ProcessExecutionResult.WebuiViewToOpen openServiceHUEditorView()
	{
		final InOutId customerReturnsId = InOutId.ofRepoId(getRecord_ID());

		final IView view = viewsRepo.createView(CreateViewRequest.builder(HUsToReturnViewFactory.Window_ID)
				.setParameter(HUsToReturnViewFactory.PARAM_HUsToReturnViewContext, HUsToReturnViewContext.builder()
						.customerReturnsId(customerReturnsId)
						.build())
				.build());

		return ProcessExecutionResult.WebuiViewToOpen.builder()
				.viewId(view.getViewId().getViewId())
				.target(ProcessExecutionResult.ViewOpenTarget.ModalOverlay)
				.build();
	}
}
