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
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ArtifactHierarchyPanelComponent } from './artifact-hierarchy-panel.component';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import {
	BranchPickerStub,
	CurrentActionDropdownMockComponent,
} from '@osee/shared/components/testing';
import { ViewSelectorMockComponent } from '@osee/messaging/shared/testing';
import { ArtifactHierarchyPathService } from '../../../services/artifact-hierarchy-path.service';
import { artifactHierarchyPathServiceMock } from '../../../testing/artifact-hierarchy-path.service.mock';
import { CommonModule } from '@angular/common';
import { MatExpansionModule } from '@angular/material/expansion';
import { ArtifactHierarchyComponent } from '../artifact-hierarchy/artifact-hierarchy.component';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { MatIconModule } from '@angular/material/icon';
import { ArtifactSearchComponent } from '../artifact-search-panel/artifact-search/artifact-search.component';
import { ArtifactHierarchyOptionsMockComponent } from '../artifact-hierarchy-options/artifact-hierarchy-oprions.component.mock';
import { ArtifactSearchMockComponent } from '../artifact-search-panel/artifact-search-panel.component.mock';
import {
	ActionService,
	CurrentActionService,
	CurrentBranchInfoService,
} from '@osee/shared/services';
import { of } from 'rxjs';
import {
	actionServiceMock,
	currentActionServiceMock,
	testBranchInfo,
} from '@osee/shared/testing';
import { ArtifactExplorerExpansionPanelComponent } from '../../shared/artifact-explorer-expansion-panel/artifact-explorer-expansion-panel.component';
import { createActionServiceMock } from '@osee/configuration-management/create-action/testing';
import { CreateActionService } from '@osee/configuration-management/create-action/services';

describe('ArtifactHierarchyPanelComponent', () => {
	let component: ArtifactHierarchyPanelComponent;
	let fixture: ComponentFixture<ArtifactHierarchyPanelComponent>;

	beforeEach(() => {
		TestBed.overrideComponent(ArtifactHierarchyPanelComponent, {
			set: {
				imports: [
					CommonModule,
					MatExpansionModule,
					ArtifactHierarchyComponent,
					DragDropModule,
					ArtifactHierarchyOptionsMockComponent,
					MatIconModule,
					ArtifactSearchComponent,
					CurrentActionDropdownMockComponent,
					BranchPickerStub,
					ViewSelectorMockComponent,
					ArtifactSearchMockComponent,
					ArtifactExplorerExpansionPanelComponent,
				],
			},
		}).configureTestingModule({
			imports: [ArtifactHierarchyPanelComponent, NoopAnimationsModule],
			providers: [
				{
					provide: ArtifactHierarchyPathService,
					useValue: artifactHierarchyPathServiceMock,
				},
				{
					provide: CurrentBranchInfoService,
					useValue: {
						get currentBranch() {
							return of(testBranchInfo);
						},
					} as Partial<CurrentBranchInfoService>,
				},
				{
					provide: CurrentActionService,
					useValue: currentActionServiceMock,
				},
				{
					provide: CreateActionService,
					useValue: createActionServiceMock,
				},
				{ provide: ActionService, useValue: actionServiceMock },
			],
		});
		fixture = TestBed.createComponent(ArtifactHierarchyPanelComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});
});
