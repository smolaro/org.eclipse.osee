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
import { HarnessLoader } from '@angular/cdk/testing';
import { TestbedHarnessEnvironment } from '@angular/cdk/testing/testbed';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatCardHarness } from '@angular/material/card/testing';
import { MatDialogModule } from '@angular/material/dialog';

import { NO_ERRORS_SCHEMA } from '@angular/compiler';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { UserDataAccountService } from '@osee/auth';
import { userDataAccountServiceMock } from '@osee/auth/testing';
import {
	EditEnumSetDialogComponent,
	EditTypeDialogComponent,
} from '@osee/messaging/shared/dialogs';
import { EditEnumSetFieldComponent } from '@osee/messaging/shared/forms';
import {
	ConnectionService,
	CrossReferenceHttpService,
	EnumerationSetService,
	EnumsService,
	MimPreferencesService,
	PreferencesUIService,
	QueryService,
	TypesService,
	UnitsService,
	WarningDialogService,
} from '@osee/messaging/shared/services';
import {
	CrossReferenceHttpServiceMock,
	MimPreferencesServiceMock,
	MockEditEnumSetDialogComponent,
	MockEditEnumSetFieldComponent,
	MockEditTypeDialogComponent,
	QueryServiceMock,
	connectionServiceMock,
	enumerationSetServiceMock,
	enumsServiceMock,
	preferencesUiServiceMock,
	typesServiceMock,
	unitsServiceMock,
	warningDialogServiceMock,
} from '@osee/messaging/shared/testing';
import { ApplicabilityListService } from '@osee/shared/services';
import { applicabilityListServiceMock } from '@osee/shared/testing';
import {
	TransactionBuilderService,
	TransactionService,
} from '@osee/shared/transactions';
import {
	transactionBuilderMock,
	transactionServiceMock,
} from '@osee/shared/transactions/testing';
import { PlatformTypeCardComponent } from './platform-type-card.component';

let loader: HarnessLoader;

describe('PlatformTypeCardComponent', () => {
	let component: PlatformTypeCardComponent;
	let fixture: ComponentFixture<PlatformTypeCardComponent>;

	beforeEach(async () => {
		await TestBed.overrideComponent(EditEnumSetDialogComponent, {
			remove: {
				imports: [EditEnumSetFieldComponent],
			},
			add: {
				imports: [MockEditEnumSetFieldComponent],
			},
		})
			.overrideComponent(PlatformTypeCardComponent, {
				remove: {
					imports: [
						EditTypeDialogComponent,
						EditEnumSetDialogComponent,
					],
				},
				add: {
					imports: [
						MockEditTypeDialogComponent,
						MockEditEnumSetDialogComponent,
					],
					providers: [
						{
							provide: WarningDialogService,
							useValue: warningDialogServiceMock,
						},
						{
							provide: UnitsService,
							useValue: unitsServiceMock,
						},
						{
							provide: TransactionService,
							useValue: transactionServiceMock,
						},
						{
							provide: CrossReferenceHttpService,
							useValue: CrossReferenceHttpServiceMock,
						},
						{
							provide: ConnectionService,
							useValue: connectionServiceMock,
						},
						{
							provide: PreferencesUIService,
							useValue: preferencesUiServiceMock,
						},
					],
				},
			})
			.configureTestingModule({
				imports: [
					NoopAnimationsModule,
					MatDialogModule,
					MockEditTypeDialogComponent,
					MockEditEnumSetDialogComponent,
					PlatformTypeCardComponent,
				],
				declarations: [],
				providers: [
					{ provide: QueryService, useValue: QueryServiceMock },
					{ provide: TypesService, useValue: typesServiceMock },
					{
						provide: MimPreferencesService,
						useValue: MimPreferencesServiceMock,
					},
					{
						provide: EnumerationSetService,
						useValue: enumerationSetServiceMock,
					},
					{
						provide: TransactionBuilderService,
						useValue: transactionBuilderMock,
					},
					{
						provide: UserDataAccountService,
						useValue: userDataAccountServiceMock,
					},
					{ provide: EnumsService, useValue: enumsServiceMock },
					{
						provide: ApplicabilityListService,
						useValue: applicabilityListServiceMock,
					},
					{
						provide: WarningDialogService,
						useValue: warningDialogServiceMock,
					},
					{
						provide: UnitsService,
						useValue: unitsServiceMock,
					},
					{
						provide: TransactionService,
						useValue: transactionServiceMock,
					},
					{
						provide: CrossReferenceHttpService,
						useValue: CrossReferenceHttpServiceMock,
					},
					{
						provide: ConnectionService,
						useValue: connectionServiceMock,
					},
					{
						provide: PreferencesUIService,
						useValue: preferencesUiServiceMock,
					},
				],
				schemas: [NO_ERRORS_SCHEMA], //weirdness with standalone component + dialog
			})
			.compileComponents();
	});

	beforeEach(() => {
		fixture = TestBed.createComponent(PlatformTypeCardComponent);
		loader = TestbedHarnessEnvironment.loader(fixture);
		component = fixture.componentInstance;
		const expectedData = {
			id: '0',
			name: 'Random enumeration',
			description: '',
			interfaceLogicalType: 'enumeration',
			interfacePlatformTypeMinval: '0',
			interfacePlatformTypeMaxval: '1',
			interfacePlatformTypeBitSize: '8',
			interfaceDefaultValue: '0',
			interfacePlatformTypeMsbValue: '0',
			interfacePlatformTypeBitsResolution: '0',
			interfacePlatformTypeCompRate: '0',
			interfacePlatformTypeAnalogAccuracy: '0',
			interfacePlatformType2sComplement: false,
			interfacePlatformTypeEnumLiteral: 'A string',
			interfacePlatformTypeUnits: 'N/A',
			interfacePlatformTypeValidRangeDescription: 'N/A',
			applicability: {
				id: '1',
				name: 'Base',
			},
			enumSet: {
				id: '-1',
				name: '',
				description: '',
				applicability: { id: '1', name: 'Base' },
			},
		};
		component.typeData = expectedData;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});

	it('should create a header of class mat-card-header-text with text of Random Boolean', async () => {
		fixture.detectChanges();
		const card = await loader.getHarness(MatCardHarness);
		expect(await card.getTitleText()).toEqual('Random enumeration');
	});
	it('should create a subtitle with text of Boolean', async () => {
		fixture.detectChanges();
		const card = await loader.getHarness(MatCardHarness);
		expect(await card.getSubtitleText()).toEqual('enumeration');
	});

	xit('should contain text that has minimum value, maximum value, byte size, default value, msb value, resolution, comp rate, analog accuracy, edit and Create New Type From Base', async () => {
		fixture.detectChanges();
		const card = await loader.getHarness(MatCardHarness);
		expect(await card.getText()).toContain('Random enumeration');
		expect(await card.getText()).toContain('enumeration');
		expect(await card.getText()).toContain('Minimum Value: 0');
		expect(await card.getText()).toContain('Maximum Value: 1');
		expect(await card.getText()).toContain('Bit Size: 8');
		expect(await card.getText()).toContain('Comp Rate: 0');
		expect(await card.getText()).toContain('Default Value: 0');
		expect(await card.getText()).toContain('MSB Value: 0');
		expect(await card.getText()).toContain('Resolution: 0');
		expect(await card.getText()).toContain('Analog Accuracy: 0');
		//expect(await card.getText()).toContain('Edit')
		expect(await card.getText()).toContain('Create New Type From Base');
		expect(await card.getText()).toContain(
			'View Related Enumeration Set Attributes'
		);
	});

	//re-enable if we re-activate platform type editing
	// it('should open dialog and create an edit of an existing type', async() => {
	//   const openDialog = spyOn(component, 'openDialog').and.callThrough();
	//   let dialogRefSpy = jasmine.createSpyObj({ afterClosed: of<editPlatformTypeDialogData>({mode:editPlatformTypeDialogDataMode.edit,type:{name:'',description:'',interfaceLogicalType:'',interfacePlatform2sComplement:false,interfacePlatformTypeAnalogAccuracy:'',interfacePlatformTypeBitSize:'0',interfacePlatformTypeBitsResolution:'',interfacePlatformTypeCompRate:'',interfaceDefaultValue:'0',interfacePlatformTypeEnumLiteral:'',interfacePlatformTypeMaxval:'',interfacePlatformTypeMinval:'',interfacePlatformTypeMsbValue:'',interfacePlatformTypeUnits:'',interfacePlatformTypeValidRangeDescription:''}}), close: null });
	//   let dialogSpy = spyOn(TestBed.inject(MatDialog), 'open').and.returnValue(dialogRefSpy)
	//   const button = await (await loader.getHarness(MatCardHarness)).getHarness(MatButtonHarness.with({ text: new RegExp("Edit") }));
	//   await button.click();
	//   expect(openDialog).toHaveBeenCalledWith(editPlatformTypeDialogDataMode.edit);
	// })
});
