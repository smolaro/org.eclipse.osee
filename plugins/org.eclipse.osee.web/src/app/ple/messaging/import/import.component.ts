/*********************************************************************
 * Copyright (c) 2022 Boeing
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
import { AsyncPipe, NgFor, NgIf } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectChange, MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute } from '@angular/router';
import type {
	connection,
	CrossReference,
	elementImportToken,
	enumeration,
	ImportEnumSet,
	ImportSummary,
	messageToken,
	nodeToken,
	platformTypeImportToken,
	subMessage,
} from '@osee/messaging/shared/types';
import {
	importEnumSetHeaderDetails,
	importMessageHeaderDetails,
	importPlatformTypeHeaderDetails,
	nodeHeaderDetails,
	structureHeaderDetails,
	subMessageHeaderDetails,
	importElementHeaderDetails,
	crossReferenceHeaderDetails,
} from '@osee/messaging/shared/table-headers';
import { BehaviorSubject, from, iif, of, OperatorFunction } from 'rxjs';
import { concatMap, filter, map, reduce, switchMap } from 'rxjs/operators';
import { UiService } from '@osee/shared/services';
import { ImportService, ImportTableComponent } from '@osee/messaging/import';
import { MessagingControlsComponent } from '@osee/messaging/shared/main-content';

@Component({
	selector: 'osee-import',
	templateUrl: './import.component.html',
	styleUrls: ['./import.component.sass'],
	standalone: true,
	imports: [
		NgIf,
		NgFor,
		AsyncPipe,
		FormsModule,
		MatButtonModule,
		MatSelectModule,
		MatTableModule,
		ImportTableComponent,
		MessagingControlsComponent,
	],
})
export class ImportComponent implements OnInit, OnDestroy {
	constructor(
		private route: ActivatedRoute,
		private routerState: UiService,
		private importService: ImportService
	) {}

	ngOnInit(): void {
		this.route.paramMap.subscribe((params) => {
			this.routerState.idValue = params.get('branchId') || '';
			this.routerState.typeValue = params.get('branchType') || '';
		});
	}

	ngOnDestroy(): void {
		this.importService.reset();
	}

	importOptionSelection = this.importService.selectedImportOption;
	branchId = this.importService.branchId;
	branchType = this.importService.branchType;
	importSummary = this.importService.importSummary;
	importOptions = this.importService.importOptions;
	importSuccess = this.importService.importSuccess;
	selectedImportFileName = this.importService.importFile.pipe(
		switchMap((file) =>
			iif(() => file === undefined, of(''), of(file?.name))
		)
	);

	_selectedConnection = new BehaviorSubject<connection | undefined>(
		undefined
	);

	connections = this.importService.connections;

	importOptionSelectionText = this.importOptions.pipe(
		switchMap((options) =>
			iif(
				() => options.length > 0,
				of('Select an import type'),
				of('No import types available')
			)
		)
	);

	connectionSelectionText = this.connections.pipe(
		switchMap((connections) =>
			iif(
				() => connections.length > 0,
				of('Select a Connection'),
				of('No connections available')
			)
		)
	);

	selectImportOption(event: MatSelectChange) {
		this.importService.reset();
		this.SelectedConnection = undefined;
		this.importService.SelectedImportOption = event.value;
	}

	selectFile(event: Event) {
		const target = event.target as HTMLInputElement;
		if (target.files && target.files.length > 0) {
			const file: File = target.files[0];
			this.importService.ImportFile = file;
			this.importService.ImportSuccess = undefined;
			this.importService.ImportInProgress = true;
			target.value = '';
		}
	}

	performImport() {
		this.importService.performImport();
	}

	nodes = this.importSummary.pipe(
		filter((v) => v !== undefined) as OperatorFunction<
			ImportSummary | undefined,
			ImportSummary
		>,
		switchMap((summary) => {
			let nodes: nodeToken[] = [];
			if (summary.createPrimaryNode) {
				nodes = [summary.primaryNode];
			}
			if (summary.createSecondaryNode) {
				nodes = [...nodes, summary.secondaryNode];
			}
			return of(nodes);
		})
	);

	enumSets = this.importSummary.pipe(
		filter((v) => v !== undefined) as OperatorFunction<
			ImportSummary | undefined,
			ImportSummary
		>,
		switchMap((summary) =>
			of(summary?.enumSetEnumRelations).pipe(
				switchMap((relations) =>
					of(summary?.enums).pipe(
						concatMap((enums) =>
							from(summary?.enumSets).pipe(
								concatMap((enumSet) =>
									of(enumSet.id).pipe(
										filter(
											(enumSetId) =>
												enumSetId !== undefined
										) as OperatorFunction<
											string | undefined,
											string
										>,
										concatMap((enumSetId) =>
											of(relations[enumSetId]).pipe(
												filter(
													(v) => v !== undefined
												) as OperatorFunction<
													string[] | undefined,
													string[]
												>,
												concatMap((enumRels) =>
													from(enumRels).pipe(
														map((rel) =>
															enums.find(
																(e) =>
																	e.id === rel
															)
														),
														filter(
															(v) =>
																v !== undefined
														) as OperatorFunction<
															| enumeration
															| undefined,
															enumeration
														>,
														map(
															(enumeration) =>
																enumeration.ordinal +
																' = ' +
																enumeration.name
														)
													)
												),
												reduce(
													(acc, curr) => [
														...acc,
														curr,
													],
													[] as string[]
												),
												concatMap((enumerations) =>
													of({
														name: enumSet.name,
														enums: enumerations,
														applicability:
															enumSet.applicability,
													} as ImportEnumSet)
												)
											)
										)
									)
								)
							)
						),
						reduce(
							(acc, curr) => [...acc, curr],
							[] as ImportEnumSet[]
						)
					)
				)
			)
		)
	);

	nodeHeaderDetails = nodeHeaderDetails;
	nodeHeaders: (keyof nodeToken)[] = ['name', 'description', 'applicability'];

	messageHeaderDetails = importMessageHeaderDetails;
	messageHeaders: (keyof messageToken)[] = [
		'name',
		'description',
		'interfaceMessageRate',
		'interfaceMessagePeriodicity',
		'interfaceMessageWriteAccess',
		'interfaceMessageType',
		'interfaceMessageNumber',
		'applicability',
	];

	submessageHeaderDetails = subMessageHeaderDetails;
	submessageHeaders: (keyof subMessage)[] = [
		'name',
		'description',
		'interfaceSubMessageNumber',
		'applicability',
	];

	structureHeaderDetails = structureHeaderDetails;
	structureHeaders: string[] = [
		'name',
		'description',
		'interfaceMaxSimultaneity',
		'interfaceMinSimultaneity',
		'interfaceTaskFileType',
		'interfaceStructureCategory',
		'applicability',
	];

	elementHeaderDetails = importElementHeaderDetails;
	elementHeaders: (keyof elementImportToken)[] = [
		'name',
		'description',
		'interfaceElementAlterable',
		'interfaceElementIndexStart',
		'interfaceElementIndexEnd',
		'notes',
		'enumLiteral',
		'interfaceDefaultValue',
	];

	platformTypeHeaderDetails = importPlatformTypeHeaderDetails;
	platformTypeHeaders: (keyof platformTypeImportToken)[] = [
		'name',
		'description',
		'interfaceLogicalType',
		'interfacePlatformTypeBitSize',
		'interfacePlatformTypeMinval',
		'interfacePlatformTypeMaxval',
		'interfacePlatformTypeValidRangeDescription',
		'interfacePlatformTypeUnits',
		'interfaceDefaultValue',
	];

	enumSetHeaderDetails = importEnumSetHeaderDetails;
	enumSetHeaders: (keyof ImportEnumSet)[] = [
		'name',
		'enums',
		'applicability',
	];

	crossReferenceHeaderDetails = crossReferenceHeaderDetails;
	crossRefHeaders: (keyof CrossReference)[] = [
		'name',
		'crossReferenceValue',
		'crossReferenceArrayValues',
	];

	setSelectedConnection(connection: connection) {
		this.SelectedConnection = connection;
	}

	get selectedConnection() {
		return this._selectedConnection;
	}

	set SelectedConnection(connection: connection | undefined) {
		this._selectedConnection.next(connection);
		this.SelectedConnectionId = connection?.id || '';
	}

	get selectedConnectionId() {
		return this.importService.selectedConnectionId;
	}

	set SelectedConnectionId(id: string) {
		this.importService.SelectedConnectionId = id;
	}
}

export default ImportComponent;
