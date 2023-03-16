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
import { of } from 'rxjs';
import { apiURL } from '@osee/environments';
import type { subMessage } from '../../types/sub-messages';
import {
	ATTRIBUTETYPEIDENUM,
	ARTIFACTTYPEIDENUM,
} from '@osee/shared/types/constants';
import {
	TransactionBuilderService,
	TransactionService,
} from '@osee/shared/transactions';
import { relation, transaction } from '@osee/shared/types';

@Injectable({
	providedIn: 'root',
})
export class SubMessagesService {
	constructor(
		private http: HttpClient,
		private builder: TransactionBuilderService,
		private transactionService: TransactionService
	) {}

	getSubMessage(
		branchId: string,
		connectionId: string,
		messageId: string,
		subMessageId: string
	) {
		return this.http.get<subMessage>(
			apiURL +
				'/mim/branch/' +
				branchId +
				'/connections/' +
				connectionId +
				'/messages/' +
				messageId +
				'/submessages/' +
				subMessageId
		);
	}

	getPaginatedFilteredSubMessages(
		branchId: string | number,
		filter: string,
		pageNum: string | number
	) {
		return this.http.get<Required<subMessage>[]>(
			apiURL +
				'/mim/branch/' +
				branchId +
				'/submessages/filter/' +
				filter,
			{
				params: {
					pageNum: pageNum,
					count: 3,
					orderByAttribute: ATTRIBUTETYPEIDENUM.NAME,
				},
			}
		);
	}
	createMessageRelation(
		messageId: string,
		subMessageId?: string,
		afterArtifact?: string
	) {
		let relation: relation = {
			typeName: 'Interface Message SubMessage Content',
			sideA: messageId,
			sideB: subMessageId,
			afterArtifact: afterArtifact || 'end',
		};
		return of(relation);
	}
	changeSubMessage(branchId: string, submessage: Partial<subMessage>) {
		return of(
			this.builder.modifyArtifact(
				submessage,
				undefined,
				branchId,
				'Update SubMessage'
			)
		);
	}

	addRelation(
		branchId: string,
		relation: relation,
		transaction?: transaction
	) {
		return of(
			this.builder.addRelation(
				relation.typeName,
				undefined,
				relation.sideA as string,
				relation.sideB as string,
				relation.afterArtifact,
				undefined,
				transaction,
				branchId,
				'Relating SubMessage'
			)
		);
	}
	deleteRelation(branchId: string, relation: relation) {
		return of(
			this.builder.deleteRelation(
				relation.typeName,
				undefined,
				relation.sideA as string,
				relation.sideB as string,
				undefined,
				undefined,
				branchId,
				'Relating SubMessage'
			)
		);
	}
	createSubMessage(
		branchId: string,
		submessage: Partial<subMessage>,
		relations: relation[],
		transaction?: transaction,
		key?: string
	) {
		return of(
			this.builder.createArtifact(
				submessage,
				ARTIFACTTYPEIDENUM.SUBMESSAGE,
				relations,
				transaction,
				branchId,
				'Create SubMessage',
				key
			)
		);
	}
	deleteSubMessage(branchId: string, submessageId: string) {
		return of(
			this.builder.deleteArtifact(
				submessageId,
				undefined,
				branchId,
				'Deleting Submessage'
			)
		);
	}
	performMutation(
		branchId: string,
		connectionId: string,
		messageId: string,
		body: transaction
	) {
		return this.transactionService.performMutation(body);
	}
}
