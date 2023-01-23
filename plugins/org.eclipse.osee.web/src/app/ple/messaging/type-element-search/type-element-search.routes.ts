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
import { Routes } from '@angular/router';

const routes: Routes = [
	{
		path: '',
		loadChildren: () =>
			import('../../../layout/lib/toolbar/toolbar.routes'),
		outlet: 'toolbar',
	},
	{
		path: '',
		loadComponent: () => import('./type-element-search.component'),
	},
	{
		path: ':branchType',
		loadComponent: () => import('./type-element-search.component'),
	},
	{
		path: ':branchType/:branchId',
		loadComponent: () => import('./type-element-search.component'),
	},
];

export default routes;
