package de.metas.bpartner;

import java.util.Optional;

import org.adempiere.util.Check;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import de.metas.lang.RepoIdAware;
import lombok.Value;

/*
 * #%L
 * de.metas.adempiere.adempiere.base
 * %%
 * Copyright (C) 2018 metas GmbH
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

@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
@Value
public class BPartnerId implements RepoIdAware
{
	int repoId;

	@JsonCreator
	public static BPartnerId ofRepoId(final int repoId)
	{
		return new BPartnerId(repoId);
	}

	public static BPartnerId ofRepoIdOrNull(final int repoId)
	{
		return repoId > 0 ? new BPartnerId(repoId) : null;
	}

	public static Optional<BPartnerId> optionalOfRepoId(final int repoId)
	{
		return Optional.ofNullable(ofRepoIdOrNull(repoId));
	}

	public static int toRepoId(final BPartnerId bpartnerId)
	{
		return toRepoIdOr(bpartnerId, -1);
	}

	public static int toRepoIdOr(final BPartnerId bpartnerId, final int defaultValue)
	{
		return bpartnerId != null ? bpartnerId.getRepoId() : defaultValue;
	}

	private BPartnerId(final int repoId)
	{
		this.repoId = Check.assumeGreaterThanZero(repoId, "bpartnerId");
	}

	@JsonValue
	public int toJson()
	{
		return getRepoId();
	}
}
