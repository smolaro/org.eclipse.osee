/*********************************************************************
 * Copyright (c) 2023 Boeing
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
import { AsyncPipe } from '@angular/common';
import {
	ChangeDetectionStrategy,
	Component,
	inject,
	Input,
	OnChanges,
	OnInit,
	Output,
	SimpleChanges,
} from '@angular/core';
import { ControlContainer, FormsModule, NgForm } from '@angular/forms';
import {
	MatAutocomplete,
	MatAutocompleteTrigger,
} from '@angular/material/autocomplete';
import { MatIconButton } from '@angular/material/button';
import {
	ErrorStateMatcher,
	MatOption,
	ShowOnDirtyErrorStateMatcher,
} from '@angular/material/core';
import { MatFormField, MatHint, MatSuffix } from '@angular/material/form-field';
import { MatIcon } from '@angular/material/icon';
import { MatInput } from '@angular/material/input';
import { CurrentMessageTypesService } from '@osee/messaging/shared/services';
import { MatOptionLoadingComponent } from '@osee/shared/components';
import { NamedId } from '@osee/shared/types';
import {
	BehaviorSubject,
	debounceTime,
	distinct,
	distinctUntilChanged,
	of,
	ReplaySubject,
	Subject,
	switchMap,
} from 'rxjs';
const _comparator = (previous: string, current: string) => {
	if (typeof previous === 'string' && typeof current === 'string') {
		return previous === current;
	}
	return false;
};
@Component({
	selector: 'osee-message-type-dropdown',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [
		AsyncPipe,
		FormsModule,
		MatOptionLoadingComponent,
		MatFormField,
		MatInput,
		MatAutocomplete,
		MatAutocompleteTrigger,
		MatHint,
		MatSuffix,
		MatIcon,
		MatIconButton,
		MatOption,
	],
	templateUrl: './message-type-dropdown.component.html',
	styles: [],
	animations: [
		trigger('dropdownOpen', [
			state(
				'open',
				style({
					opacity: 0,
				})
			),
			state(
				'closed',
				style({
					opacity: 1,
				})
			),
			transition('open=>closed', [animate('0.5s')]),
			transition('closed=>open', [animate('0.5s 0.25s')]),
		]),
	],
	viewProviders: [{ provide: ControlContainer, useExisting: NgForm }],
})
export class MessageTypeDropdownComponent implements OnChanges, OnInit {
	private _currentMessageTypesService = inject(CurrentMessageTypesService);

	private _typeAhead = new BehaviorSubject<string>('');
	private _openAutoComplete = new ReplaySubject<void>();

	private _isOpen = new BehaviorSubject<boolean>(false);

	@Input() required: boolean = false;
	@Input() disabled: boolean = false;

	@Input() hintHidden: boolean = false;
	@Input() messageType: string = '';

	private _messageTypeChange = new Subject<string>();
	@Output() messageTypeChange = this._messageTypeChange.pipe(
		//skip(1),
		distinctUntilChanged(),
		debounceTime(50)
		//tap((v) => console.log(v))
	);

	@Input() errorMatcher: ErrorStateMatcher =
		new ShowOnDirtyErrorStateMatcher();

	protected _size = this._currentMessageTypesService.currentPageSize;

	get filter() {
		return this._typeAhead.pipe(
			//debounceTime(500)
			//tap((v) => console.log(v))
			distinct(),
			distinctUntilChanged(_comparator)
			//map((v) => v.name)
		);
	}
	protected _messageTypes = this._openAutoComplete.pipe(
		debounceTime(500),
		distinctUntilChanged(),
		switchMap((_) =>
			this.filter.pipe(
				distinctUntilChanged(),
				debounceTime(500),
				switchMap((filter) =>
					of((pageNum: string | number) =>
						this._currentMessageTypesService.getFilteredPaginatedMessageTypes(
							pageNum,
							filter
							//typeof filter === 'string' ? filter : filter.name
						)
					)
				)
			)
		)
	);

	_count = this._openAutoComplete.pipe(
		debounceTime(500),
		distinctUntilChanged(),
		switchMap((_) =>
			this.filter.pipe(
				distinctUntilChanged(),
				debounceTime(500),
				switchMap((filter) =>
					this._currentMessageTypesService.getFilteredCount(
						filter
						//typeof filter === 'string' ? filter : filter.name
					)
				)
			)
		)
	);

	updateTypeAhead(value: string | NamedId) {
		if (typeof value === 'string') {
			this._typeAhead.next(value);
		} else {
			this._typeAhead.next(value.name);
		}
	}
	autoCompleteOpened() {
		this._openAutoComplete.next();
		this._isOpen.next(true);
	}
	close() {
		this._isOpen.next(false);
	}
	updateValue(value: NamedId | string) {
		if (typeof value === 'string' && value !== '') {
			this._messageTypeChange.next(value);
			this.updateTypeAhead(value);
		} else if (typeof value === 'object') {
			this._messageTypeChange.next(value.name);
			this.updateTypeAhead(value.name);
		}
	}

	ngOnChanges(changes: SimpleChanges): void {
		if (
			changes.messageType !== undefined &&
			changes.messageType.previousValue !==
				changes.messageType.currentValue &&
			changes.messageType.currentValue !== undefined &&
			changes.messageType.currentValue !== null
		) {
			this.updateTypeAhead(changes.messageType.currentValue);
		}
	}
	ngOnInit(): void {
		this.updateTypeAhead(this.messageType);
	}
	get isOpen() {
		return this._isOpen;
	}
	clear() {
		this.updateValue({ id: '-1', name: '' });
		this.updateTypeAhead('');
	}

	displayFn(value: string) {
		//console.log(value);
		return value ? value : '';
		//return messageTypeDisplayFn(value);
	}
}
