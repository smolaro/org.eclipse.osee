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
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { UsermenuComponent } from './components/menus/usermenu/usermenu.component';
import { TypesInterfaceComponent } from './types-interface.component';

const routes: Routes = [
  { path: '', component: TypesInterfaceComponent },
  { path: ':branchType', component: TypesInterfaceComponent },
  { path: ':branchType/:branchId', component: TypesInterfaceComponent },
  { path: ':branchType/:branchId/:type', component: TypesInterfaceComponent },
  { path: '', component: UsermenuComponent, outlet: 'userMenu' }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TypesInterfaceRoutingModule { }
