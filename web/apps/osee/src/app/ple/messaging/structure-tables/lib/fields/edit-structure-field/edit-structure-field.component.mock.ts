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
import { Component, Input } from '@angular/core';

@Component({
	selector: 'osee-messaging-edit-structure-field',
	template: '<p>Dummy</p>',
	standalone: true,
})
// eslint-disable-next-line @angular-eslint/component-class-suffix
export class EditStructureFieldComponentMock {
	@Input() structureId!: string;
	@Input() header!: string;
	@Input() value!: string;
}
