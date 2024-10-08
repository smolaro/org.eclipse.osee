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
import {
	animate,
	state,
	style,
	transition,
	trigger,
} from '@angular/animations';
import { DataSource } from '@angular/cdk/collections';
import { CdkVirtualForOf } from '@angular/cdk/scrolling';
import { AsyncPipe, NgClass, NgStyle } from '@angular/common';
import {
	ChangeDetectionStrategy,
	Component,
	Inject,
	Input,
	OnDestroy,
	viewChild,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatIconButton } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import {
	MatFormField,
	MatHint,
	MatLabel,
	MatPrefix,
} from '@angular/material/form-field';
import { MatIcon } from '@angular/material/icon';
import { MatInput } from '@angular/material/input';
import {
	MatMenu,
	MatMenuContent,
	MatMenuItem,
	MatMenuTrigger,
} from '@angular/material/menu';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import {
	MatCell,
	MatCellDef,
	MatColumnDef,
	MatHeaderCell,
	MatHeaderCellDef,
	MatHeaderRow,
	MatHeaderRowDef,
	MatRow,
	MatRowDef,
	MatTable,
} from '@angular/material/table';
import { MatTooltip } from '@angular/material/tooltip';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { LayoutNotifierService } from '@osee/layout/notification';
import {
	defaultEditElementProfile,
	defaultEditStructureProfile,
	defaultViewElementProfile,
	defaultViewStructureProfile,
} from '@osee/messaging/shared/constants';
import { StructureDataSource } from '@osee/messaging/shared/datasources';
import { EditViewFreeTextFieldDialogComponent } from '@osee/messaging/shared/dialogs/free-text';
import { MessagingControlsComponent } from '@osee/messaging/shared/main-content';
import {
	CurrentStructureService,
	HeaderService,
} from '@osee/messaging/shared/services';
import { STRUCTURE_SERVICE_TOKEN } from '@osee/messaging/shared/tokens';
import type {
	EditViewFreeTextDialog,
	ElementDialog,
	element,
	structure,
	structureWithChanges,
} from '@osee/messaging/shared/types';
import {
	TwoLayerAddButtonComponent,
	CurrentViewSelectorComponent,
} from '@osee/shared/components';
import { applic } from '@osee/shared/types/applicability';
import { difference } from '@osee/shared/types/change-report';
import { HighlightFilteredTextDirective } from '@osee/shared/utils';
import { combineLatest, from, iif, of } from 'rxjs';
import {
	debounceTime,
	distinct,
	filter,
	first,
	map,
	mergeMap,
	pairwise,
	reduce,
	scan,
	share,
	shareReplay,
	startWith,
	switchMap,
	take,
	takeUntil,
	tap,
} from 'rxjs/operators';
import { AddElementDialogComponent } from '../../dialogs/add-element-dialog/add-element-dialog.component';
import { DefaultAddElementDialog } from '../../dialogs/add-element-dialog/add-element-dialog.default';
import { AddStructureDialog } from '../../dialogs/add-structure-dialog/add-structure-dialog';
import { AddStructureDialogComponent } from '../../dialogs/add-structure-dialog/add-structure-dialog.component';
import { DeleteStructureDialogComponent } from '../../dialogs/delete-structure-dialog/delete-structure-dialog.component';
import { RemoveStructureDialogComponent } from '../../dialogs/remove-structure-dialog/remove-structure-dialog.component';
import { EditStructureFieldComponent } from '../../fields/edit-structure-field/edit-structure-field.component';
import { StructureTableLongTextFieldComponent } from '../../fields/structure-table-long-text-field/structure-table-long-text-field.component';
import { SubElementTableComponent } from '../sub-element-table/sub-element-table.component';

@Component({
	selector: 'osee-structure-table',
	templateUrl: './structure-table.component.html',
	styles: [],
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [
		NgClass,
		NgStyle,
		AsyncPipe,
		RouterLink,
		TwoLayerAddButtonComponent,
		FormsModule,
		MatFormField,
		MatLabel,
		MatInput,
		MatIcon,
		MatPrefix,
		MatHint,
		MatTable,
		MatColumnDef,
		MatHeaderCell,
		MatHeaderCellDef,
		MatTooltip,
		MatCell,
		MatCellDef,
		MatIconButton,
		MatHeaderRow,
		MatHeaderRowDef,
		MatRow,
		MatRowDef,
		MatPaginator,
		MatMenu,
		MatMenuContent,
		MatMenuItem,
		MatMenuTrigger,
		CdkVirtualForOf,
		EditStructureFieldComponent,
		AddStructureDialogComponent,
		MessagingControlsComponent,
		CurrentViewSelectorComponent,
		SubElementTableComponent,
		HighlightFilteredTextDirective,
		StructureTableLongTextFieldComponent,
	],
	animations: [
		trigger('detailExpand', [
			state('collapsed', style({ maxHeight: '0vh' })),
			state('expanded', style({ maxHeight: '60vh' })),
			transition(
				'expanded <=> collapsed',
				animate('225ms cubic-bezier(0.42, 0.0, 0.58, 1)')
			),
		]),
		trigger('expandButton', [
			state('closed', style({ transform: 'rotate(0)' })),
			state('open', style({ transform: 'rotate(-180deg)' })),
			transition(
				'open <=> closed',
				animate('225ms cubic-bezier(0.42, 0.0, 0.58, 1)')
			),
		]),
	],
})
export class StructureTableComponent implements OnDestroy {
	expandedElement = this.structureService.expandedRows;
	@Input() previousLink = '../../../../';
	@Input() structureId = '';
	messageData: DataSource<structure | structureWithChanges> =
		new StructureDataSource(this.structureService);
	@Input() hasFilter: boolean = false;
	truncatedSections: string[] = [];
	editableStructureHeaders: (keyof structure)[] = [
		'name',
		'nameAbbrev',
		'description',
		'interfaceMaxSimultaneity',
		'interfaceMinSimultaneity',
		'interfaceTaskFileType',
		'interfaceStructureCategory',
		'applicability',
	];

	filter: string = '';
	searchTerms: string = '';
	@Input() breadCrumb: string = '';
	preferences = this.structureService.preferences;
	isEditing = this.preferences.pipe(
		map((x) => x.inEditMode),
		share(),
		shareReplay(1)
	);
	structures = this.structureService.structures.pipe(
		tap((structs) => {
			if (this.filter !== '') {
				structs.forEach((s) => {
					if (s.elements && s.elements.length > 0) {
						this.rowChange(s, true);
					}
				});
			}
		})
	);
	structuresCount = this.structureService.structuresCount;
	currentPage = this.structureService.currentPage;
	currentPageSize = this.structureService.currentPageSize;

	currentOffset = combineLatest([
		this.structureService.currentPage.pipe(startWith(0), pairwise()),
		this.structureService.currentPageSize,
	]).pipe(
		debounceTime(100),
		scan((acc, [[previousPageNum, currentPageNum], currentSize]) => {
			if (previousPageNum < currentPageNum) {
				return (acc += currentSize);
			} else {
				return acc;
			}
		}, 10)
	);

	minPageSize = combineLatest([
		this.currentOffset,
		this.structuresCount,
	]).pipe(
		debounceTime(100),
		switchMap(([offset, messages]) => of([offset, messages])),
		map(([offset, length]) => Math.max(offset + 1, length + 1))
	);

	private _currentElementHeaders = combineLatest([
		this.headerService.AllElementHeaders,
		this.preferences,
	]).pipe(
		switchMap(([allHeaders, response]) =>
			of(response.columnPreferences).pipe(
				mergeMap((r) =>
					from(r).pipe(
						filter(
							(column) =>
								allHeaders.includes(
									column.name as keyof element
								) && column.enabled
						),
						distinct((r) => r.name),
						map((header) => header.name as keyof element),
						reduce(
							(acc, curr) => [...acc, curr],
							[] as (keyof element)[]
						)
					)
				)
			)
		),
		mergeMap((headers) =>
			iif(
				() => headers.length !== 0,
				of(headers).pipe(
					map((array) => {
						array.push(
							array.splice(array.indexOf('applicability'), 1)[0]
						);
						return array;
					})
				),
				this.isEditing.pipe(
					switchMap((editing) =>
						iif(
							() => editing,
							of(defaultEditElementProfile),
							of(defaultViewElementProfile)
						)
					)
				)
			)
		),
		switchMap((finalHeaders) => of(['rowControls', ...finalHeaders])),
		share(),
		shareReplay(1)
	);

	currentElementHeaders = combineLatest([
		this._currentElementHeaders,
		this.headerService.AllElementHeaders,
	]).pipe(
		map(([headers, allheaders]) =>
			headers.sort(
				(a, b) =>
					allheaders.indexOf(a as keyof element) -
					allheaders.indexOf(b as keyof element)
			)
		)
	);

	private _currentStructureHeaders = combineLatest([
		this.headerService.AllStructureHeaders,
		this.preferences,
	]).pipe(
		switchMap(([allHeaders, response]) =>
			of(response.columnPreferences).pipe(
				mergeMap((r) =>
					from(r).pipe(
						filter(
							(column) =>
								allHeaders.includes(
									column.name as Extract<
										keyof structure,
										string
									>
								) && column.enabled
						),
						distinct((r) => r.name),
						map(
							(header) =>
								header.name as Extract<keyof structure, string>
						),
						reduce(
							(acc, curr) => [...acc, curr],
							[] as Extract<keyof structure, string>[]
						)
					)
				)
			)
		),
		mergeMap((headers) =>
			iif(
				() => headers.length !== 0,
				of(headers),
				this.isEditing.pipe(
					switchMap((editing) =>
						iif(
							() => editing,
							of(defaultEditStructureProfile),
							of(defaultViewStructureProfile)
						)
					)
				)
			)
		),
		switchMap((finalHeaders) => of([' ', ...finalHeaders])),
		share(),
		shareReplay(1)
	);

	currentStructureHeaders = combineLatest([
		this._currentStructureHeaders,
		this.headerService.AllStructureHeaders,
	]).pipe(
		map(([headers, allheaders]) =>
			headers.sort(
				(a, b) => allheaders.indexOf(a) - allheaders.indexOf(b)
			)
		)
	);

	_connectionsRoute = this.structureService.connectionsRoute;
	_messageData = this.structureService.message.pipe(
		takeUntil(this.structureService.done)
	);
	structureDialog = this.structureService.SubMessageId.pipe(
		take(1),
		switchMap((submessage) =>
			this.dialog
				.open(AddStructureDialogComponent, {
					minWidth: '80%',
					data: {
						id: submessage,
						name: this.breadCrumb,
						structure: {
							id: '-1',
							name: '',
							elements: [],
							description: '',
							interfaceMaxSimultaneity: '',
							interfaceMinSimultaneity: '',
							interfaceStructureCategory: '',
							interfaceTaskFileType: 0,
							applicability: {
								id: '1',
								name: 'Base',
							},
						},
					},
				})
				.afterClosed()
				.pipe(
					take(1),
					filter((val) => val !== undefined),
					switchMap((value: AddStructureDialog) =>
						iif(
							() =>
								value.structure.id !== '-1' &&
								value.structure.id.length > 0,
							this.structureService.relateStructure(
								value.structure.id
							),
							this.structureService.createStructure(
								value.structure
							)
						)
					)
				)
		),
		first()
	);
	layout = this.layoutNotifier.layout;
	menuPosition = {
		x: '0',
		y: '0',
	};
	matMenuTrigger = viewChild.required(MatMenuTrigger);
	sideNav = this.structureService.sideNavContent;
	sideNavOpened = this.sideNav.pipe(map((value) => value.opened));
	inDiffMode = this.structureService.isInDiff.pipe(
		switchMap((val) => iif(() => val, of('true'), of('false')))
	);

	textExpanded: boolean = false;
	toggleExpanded() {
		this.textExpanded = !this.textExpanded;
	}

	constructor(
		public dialog: MatDialog,
		private route: ActivatedRoute,
		@Inject(STRUCTURE_SERVICE_TOKEN)
		private structureService: CurrentStructureService,
		private layoutNotifier: LayoutNotifierService,
		private headerService: HeaderService
	) {}

	ngOnDestroy(): void {
		this.structureService.filter = '';
	}

	valueTracker(index: any, item: any) {
		return index;
	}

	structureTracker(index: number, item: structure | structureWithChanges) {
		return item.id !== '-1' ? item.id : index.toString();
	}
	openAddElementDialog(structure: structure | structureWithChanges) {
		const dialogData = new DefaultAddElementDialog(
			structure?.id || '',
			structure?.name || ''
		);
		let dialogRef = this.dialog.open(AddElementDialogComponent, {
			data: dialogData,
			minWidth: '80%',
		});
		let createElement = dialogRef.afterClosed().pipe(
			filter(
				(val) =>
					(val !== undefined || val !== null) &&
					val?.element !== undefined
			),
			switchMap((value: ElementDialog) =>
				iif(
					() =>
						value.element.id !== undefined &&
						value.element.id !== '-1' &&
						value.element.id.length > 0,
					this.structureService.relateElement(
						structure.id,
						value.element.id !== undefined ? value.element.id : '-1'
					),
					this.structureService.createNewElement(
						value.element,
						structure.id,
						value.type.id as string
					)
				)
			),
			take(1)
		);
		createElement.subscribe();
	}

	rowIsExpanded(value: string) {
		return this.structureService.expandedRows.pipe(
			map((rows) => rows.map((s) => s.id).includes(value))
		);
	}

	expandRow(value: structure | structureWithChanges) {
		this.structureService.addExpandedRow = value;
	}
	hideRow(value: structure | structureWithChanges) {
		this.structureService.removeExpandedRow = value;
	}

	rowChange(value: structure | structureWithChanges, type: boolean) {
		if (type) {
			this.expandRow(value);
		} else {
			this.hideRow(value);
		}
	}

	applyFilter(event: Event) {
		const filterValue = (event.target as HTMLInputElement).value;
		this.searchTerms = filterValue;
		this.filter = filterValue.trim().toLowerCase();
		this.structureService.filter = (event.target as HTMLInputElement).value;
	}
	isTruncated(value: string) {
		if (this.truncatedSections.find((x) => x === value)) {
			return true;
		}
		return false;
	}

	openAddStructureDialog() {
		this.structureDialog.subscribe();
	}

	openMenu(
		event: MouseEvent,
		id: string,
		name: string,
		description: string,
		structure: structure,
		header: keyof structure,
		diff: string
	) {
		event.preventDefault();
		this.menuPosition.x = event.clientX + 'px';
		this.menuPosition.y = event.clientY + 'px';
		this.matMenuTrigger().menuData = {
			id: id,
			name: name,
			description: description,
			structure: structure,
			header: header,
			diffMode: diff === 'true',
			url:
				this.route.snapshot.pathFromRoot
					.map((r) => r.url)
					.join()
					.replace(/(diff)/g, '')
					.replace(/,/g, '/')
					.replace(/\/\//g, '') +
				'/' +
				id +
				(diff === 'true' ? '/diff' : ''),
		};
		this.matMenuTrigger().openMenu();
	}

	removeStructureDialog(id: string, name: string) {
		this.structureService.SubMessageId.pipe(
			take(1),
			switchMap((subMessageId) =>
				this.dialog
					.open(RemoveStructureDialogComponent, {
						data: {
							subMessageId: subMessageId,
							structureId: id,
							structureName: name,
						},
					})
					.afterClosed()
					.pipe(
						take(1),
						switchMap((dialogResult: string) =>
							iif(
								() => dialogResult === 'ok',
								this.structureService.removeStructureFromSubmessage(
									id,
									subMessageId
								),
								of()
							)
						)
					)
			)
		).subscribe();
	}

	deleteStructureDialog(id: string, name: string) {
		this.dialog
			.open(DeleteStructureDialogComponent, {
				data: {
					structureId: id,
					structureName: name,
				},
			})
			.afterClosed()
			.pipe(
				take(1),
				switchMap((dialogResult: string) =>
					iif(
						() => dialogResult === 'ok',
						this.structureService.deleteStructure(id),
						of()
					)
				)
			)
			.subscribe();
	}

	insertStructure(afterStructure?: string) {
		this.dialog
			.open(AddStructureDialogComponent, {
				data: {
					id: this.structureService.subMessageId,
					name: this.breadCrumb,
					structure: {
						id: '-1',
						name: '',
						elements: [],
						description: '',
						interfaceMaxSimultaneity: '',
						interfaceMinSimultaneity: '',
						interfaceStructureCategory: '',
						interfaceTaskFileType: 0,
					},
				},
			})
			.afterClosed()
			.pipe(
				take(1),
				filter((val) => val !== undefined),
				switchMap((value: AddStructureDialog) =>
					iif(
						() =>
							value.structure.id !== '-1' &&
							value.structure.id.length > 0,
						this.structureService.relateStructure(
							value.structure.id,
							afterStructure
						),
						this.structureService.createStructure(
							value.structure,
							afterStructure
						)
					)
				)
			)
			.subscribe();
	}

	copyStructure(
		structure: structure | structureWithChanges,
		afterStructure?: string
	) {
		this.dialog
			.open(AddStructureDialogComponent, {
				data: {
					id: this.structureService.subMessageId,
					name: this.breadCrumb,
					structure: structuredClone(structure),
				},
			})
			.afterClosed()
			.pipe(
				take(1),
				filter((val) => val !== undefined),
				switchMap((result) =>
					this.structureService.copyStructure(
						result.structure,
						afterStructure
					)
				)
			)
			.subscribe();
	}
	openDescriptionDialog(description: string, structureId: string) {
		this.dialog
			.open(EditViewFreeTextFieldDialogComponent, {
				data: {
					original: JSON.parse(JSON.stringify(description)) as string,
					type: 'Description',
					return: description,
				},
				minHeight: '60%',
				minWidth: '60%',
			})
			.afterClosed()
			.pipe(
				take(1),
				switchMap((response: EditViewFreeTextDialog | string) =>
					iif(
						() =>
							response === 'ok' ||
							response === 'cancel' ||
							response === undefined,
						//do nothing
						of(),
						//change description
						this.structureService.partialUpdateStructure({
							id: structureId,
							description: (response as EditViewFreeTextDialog)
								.return,
						})
					)
				)
			)
			.subscribe();
	}

	getHeaderByName(value: keyof structure) {
		return this.headerService.getHeaderByName(value, 'structure');
	}

	viewDiff(open: boolean, value: difference, header: string) {
		this.structureService.sideNav = {
			opened: open,
			field: header,
			currentValue: value.currentValue as string | number | applic,
			previousValue: value.previousValue as
				| string
				| number
				| applic
				| undefined,
			transaction: value.transactionToken,
		};
	}
	setPage(event: PageEvent) {
		this.structureService.pageSize = event.pageSize;
		this.structureService.page = event.pageIndex;
	}
}
