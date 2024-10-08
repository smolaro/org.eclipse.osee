/*********************************************************************
 * Copyright (c) 2021 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { user } from '@osee/shared/types/auth';
import { apiURL } from '@osee/environments';
import type { element } from '../../types/element';
import type { structure } from '../../types/structure';
import type { message } from '../../types/messages';
import type { subMessage } from '../../types/sub-messages';
import type {
	MimPreferences,
	MimUserGlobalPreferences,
} from '../../types/mim.preferences';
import { ARTIFACTTYPEIDENUM } from '@osee/shared/types/constants';

import { TransactionService } from '@osee/shared/transactions';
import { attributeType, transaction } from '@osee/shared/types';
@Injectable({
	providedIn: 'root',
})
export class MimPreferencesService {
	constructor(
		private http: HttpClient,
		private txService: TransactionService
	) {}

	getUserPrefs(branchId: string, user: user) {
		return this.http.get<
			MimPreferences<structure & message & subMessage & element>
		>(apiURL + '/mim/user/' + branchId);
	}

	getBranchPrefs(user: user) {
		return this.http.get<string[]>(apiURL + '/mim/user/branches');
	}

	createGlobalUserPrefs(
		user: user,
		prefs: Partial<MimUserGlobalPreferences>
	) {
		const tx = {
			branch: '570',
			txComment: 'Create MIM User Global Preferences',
			createArtifacts: [
				{
					typeId: ARTIFACTTYPEIDENUM.GLOBALUSERPREFERENCES,
					name: 'MIM Global User Preferences',
					key: 'globalPrefs',
					attributes: [
						{
							typeName: 'MIM Word Wrap',
							value: prefs.wordWrap,
						},
					],
				},
			],
			addRelations: [
				{
					typeName: 'User to MIM User Global Preferences',
					aArtId: user.id,
					bArtId: 'globalPrefs',
				},
			],
		} as transaction;

		return this.txService.performMutation(tx);
	}

	updateGlobalUserPrefs(
		current: MimUserGlobalPreferences,
		updated: MimUserGlobalPreferences
	) {
		let setAttributes: attributeType[] = [];
		if (current.wordWrap !== updated.wordWrap) {
			setAttributes.push({
				typeName: 'MIM Word Wrap',
				value: updated.wordWrap,
			});
		}

		const tx = {
			branch: '570',
			txComment: 'Updating MIM User Global Preferences',
			modifyArtifacts: [{ id: updated.id, setAttributes: setAttributes }],
		} as transaction;

		return this.txService.performMutation(tx);
	}

	performMutation(transaction: transaction) {
		return this.txService.performMutation(transaction);
	}
}
