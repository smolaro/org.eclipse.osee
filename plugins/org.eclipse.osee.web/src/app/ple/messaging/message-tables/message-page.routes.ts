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
import { DiffReportResolver } from '../../../resolvers/diff-report-resolver.resolver';

const routes: Routes = [
	{
		path: '',
		loadChildren: () => import('./toolbar.routes'),
		outlet: 'toolbar',
	},
	{
		path: '',
		loadComponent: () => import('./message-page.component'),
		children: [],
	},
	{
		path: 'diff',
		loadComponent: () => import('./message-page.component'),
		resolve: { diff: DiffReportResolver },
	},
	{
		path: '',
		loadComponent: () =>
			import('../../diff-views/single-diff/single-diff.component'),
		outlet: 'rightSideNav',
	},
	{
		path: '',
		loadComponent: () => import('./lib/menus/usermenu/usermenu.component'),
		outlet: 'userMenu',
	},
	{
		path: '',
		loadComponent: () =>
			import('../shared/headers/mim-header/mim-header.component'),
		outlet: 'navigationHeader',
	},
];

export default routes;
