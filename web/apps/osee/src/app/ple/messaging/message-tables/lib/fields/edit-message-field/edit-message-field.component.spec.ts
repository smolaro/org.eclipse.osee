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
import { HarnessLoader } from '@angular/cdk/testing';
import { TestbedHarnessEnvironment } from '@angular/cdk/testing/testbed';
import {
	HttpClientTestingModule,
	HttpTestingController,
} from '@angular/common/http/testing';
import {
	ComponentFixture,
	TestBed,
	fakeAsync,
	tick,
} from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { apiURL } from '@osee/environments';
import {
	MessageTypeDropdownComponent,
	RateDropdownComponent,
} from '@osee/messaging/shared/dropdowns';
import {
	MockMessageTypeDropdownComponent,
	MockRateDropdownComponent,
} from '@osee/messaging/shared/dropdowns/testing';
import {
	MessageUiService,
	WarningDialogService,
} from '@osee/messaging/shared/services';
import { ApplicabilityListService } from '@osee/shared/services';
import { applicabilityListServiceMock } from '@osee/shared/testing';
import { EditMessageFieldComponent } from './edit-message-field.component';
import { warningDialogServiceMock } from '@osee/messaging/shared/testing';

describe('EditMessageFieldComponent', () => {
	let component: EditMessageFieldComponent<any>;
	let fixture: ComponentFixture<EditMessageFieldComponent<any>>;
	let httpTestingController: HttpTestingController;
	let uiService: MessageUiService;
	let loader: HarnessLoader;

	beforeEach(async () => {
		await TestBed.overrideComponent(EditMessageFieldComponent, {
			remove: {
				imports: [RateDropdownComponent, MessageTypeDropdownComponent],
			},
			add: {
				imports: [
					MockRateDropdownComponent,
					MockMessageTypeDropdownComponent,
				],
				providers: [
					{
						provide: WarningDialogService,
						useValue: warningDialogServiceMock,
					},
				],
			},
		})
			.configureTestingModule({
				imports: [
					HttpClientTestingModule,
					FormsModule,
					MatFormFieldModule,
					MatInputModule,
					MatSelectModule,
					MatDialogModule,
					NoopAnimationsModule,
					EditMessageFieldComponent,
				],
				providers: [
					{
						provide: ApplicabilityListService,
						useValue: applicabilityListServiceMock,
					},
					{
						provide: WarningDialogService,
						useValue: warningDialogServiceMock,
					},
				],
			})
			.compileComponents();
	});

	beforeEach(() => {
		httpTestingController = TestBed.inject(HttpTestingController);
		uiService = TestBed.inject(MessageUiService);
		fixture = TestBed.createComponent(EditMessageFieldComponent);
		component = fixture.componentInstance;
		loader = TestbedHarnessEnvironment.loader(fixture);
		fixture.detectChanges();
		component.header = 'applicability';
		component.value = { id: '1', name: 'Base' };
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});

	it('should update value', fakeAsync(() => {
		uiService.BranchIdString = '8';
		uiService.connectionIdString = '10';
		component.focusChanged(null);
		component.updateMessage('v2');
		tick(500);
		const req = httpTestingController.expectOne(apiURL + '/orcs/txs');
		expect(req.request.method).toEqual('POST');
	}));
});
