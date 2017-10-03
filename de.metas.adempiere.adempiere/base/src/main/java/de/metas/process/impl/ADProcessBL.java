package de.metas.process.impl;

import org.adempiere.ad.table.api.IADTableDAO;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.util.Services;
import org.compiere.model.I_AD_Column;
import org.compiere.model.I_AD_Process;
import org.compiere.model.I_AD_Table;
import org.compiere.model.I_AD_Workflow;
import org.compiere.model.X_AD_Process;
import org.compiere.wf.api.IADWorkflowBL;

import de.metas.document.engine.DocumentTableFields;
import de.metas.process.IADProcessBL;
import lombok.NonNull;

/*
 * #%L
 * de.metas.adempiere.adempiere.base
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

public class ADProcessBL implements IADProcessBL
{

	@Override
	public I_AD_Process createAndLinkDocumentSpecificProcess(@NonNull final I_AD_Table document)
	{
		// Services
		final IADWorkflowBL workflowBL = Services.get(IADWorkflowBL.class);
		
		final I_AD_Process documentSpecificProcess = InterfaceWrapperHelper.newInstance(I_AD_Process.class);

		// Workflow
		final I_AD_Workflow documentSpecificWorkflow = workflowBL.createWorkflowForDocument(document);
		documentSpecificProcess.setAD_Workflow(documentSpecificWorkflow);

		documentSpecificProcess.setValue(documentSpecificWorkflow.getValue());

		documentSpecificProcess.setName(documentSpecificWorkflow.getName());

		documentSpecificProcess.setEntityType(documentSpecificWorkflow.getEntityType());
		documentSpecificProcess.setAccessLevel(documentSpecificWorkflow.getAccessLevel());
		documentSpecificProcess.setType(X_AD_Process.TYPE_Java);
		documentSpecificProcess.setIsReport(false);
		documentSpecificProcess.setIsUseBPartnerLanguage(true);

		InterfaceWrapperHelper.save(documentSpecificProcess);

		linkProccessWithDocument(document, documentSpecificProcess);

		return documentSpecificProcess;
	}

	private void linkProccessWithDocument(@NonNull final I_AD_Table document, @NonNull final I_AD_Process documentSpecificProcess)
	{
		final I_AD_Column processingColumn = getColumnForDocument(document, DocumentTableFields.COLUMNNAME_Processing);
		linkProcessingColumnWithProcess(documentSpecificProcess, processingColumn);

		final I_AD_Column docActionColumn = getColumnForDocument(document, DocumentTableFields.COLUMNNAME_DocAction);
		linkProcessingColumnWithProcess(documentSpecificProcess, docActionColumn);

	}

	private void linkProcessingColumnWithProcess(final I_AD_Process documentSpecificProcess, final I_AD_Column processingColumn)
	{
		if (processingColumn == null)
		{
			// nothing to do if the column does not exist for the document table
			return;
		}
		processingColumn.setAD_Process(documentSpecificProcess);

		InterfaceWrapperHelper.save(processingColumn);

	}

	private I_AD_Column getColumnForDocument(@NonNull final I_AD_Table document, @NonNull final String columnName)
	{
		final IADTableDAO adTableDAO = Services.get(IADTableDAO.class);

		final String tableName = adTableDAO.retrieveTableName(document.getAD_Table_ID());

		return adTableDAO.retrieveColumn(tableName, columnName);
	}

}
