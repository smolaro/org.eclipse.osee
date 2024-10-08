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
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { response } from '@osee/shared/types';
import { of } from 'rxjs';
import { PlConfigBranchService } from '../../services/pl-config-branch-service.service';
import { PlConfigCurrentBranchService } from '../../services/pl-config-current-branch.service';
import { testBranchApplicability } from '../../testing/mockBranchService';

import { CurrentBranchInfoService } from '@osee/shared/services';
import { testBranchInfo } from '@osee/shared/testing';
import { DialogService } from 'src/app/ple/plconfig/lib/services/dialog.service';
import { DialogServiceMock } from 'src/app/ple/plconfig/lib/testing/mockDialogService.mock';
import { ConfigurationDropdownComponent } from './configuration-dropdown.component';

describe('ConfigurationDropdownComponent', () => {
	let component: ConfigurationDropdownComponent;
	let fixture: ComponentFixture<ConfigurationDropdownComponent>;

	beforeEach(async () => {
		const testResponse: response = {
			empty: false,
			errorCount: 0,
			errors: false,
			failed: false,
			ids: [],
			infoCount: 0,
			numErrors: 0,
			numErrorsViaSearch: 0,
			numWarnings: 0,
			numWarningsViaSearch: 0,
			results: [],
			success: true,
			tables: [],
			title: '',
			txId: '2',
			warningCount: 0,
		};
		const branchService = jasmine.createSpyObj('PlConfigBranchService', [
			'deleteConfiguration',
			'copyConfiguration',
			'addConfiguration',
		]);
		var addConfigurationSpy =
			branchService.addConfiguration.and.returnValue(of(testResponse));
		var copyConfigurationSpy =
			branchService.copyConfiguration.and.returnValue(of(testResponse));
		var delteConfigurationSpy =
			branchService.deleteConfiguration.and.returnValue(of(testResponse));
		await TestBed.configureTestingModule({
			imports: [
				MatMenuModule,
				ConfigurationDropdownComponent,
				MatIconModule,
				MatButtonModule,
				MatFormFieldModule,
			],
			providers: [
				provideNoopAnimations(),
				{ provide: DialogService, useValue: DialogServiceMock },
				{
					provide: CurrentBranchInfoService,
					useValue: { currentBranch: of(testBranchInfo) },
				},
				{ provide: MatDialog, useValue: {} },
				{
					provide: PlConfigCurrentBranchService,
					useValue: {
						branchApplicability: of(testBranchApplicability),
					},
				},
				{ provide: PlConfigBranchService, useValue: branchService },
			],
		}).compileComponents();
	});

	beforeEach(() => {
		fixture = TestBed.createComponent(ConfigurationDropdownComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});
});
