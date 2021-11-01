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
import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { BranchUIService } from 'src/app/ple-services/ui/branch/branch-ui.service';
import { UiService } from 'src/app/ple-services/ui/ui.service';

@Injectable({
  providedIn: 'root'
})
export class RouteStateService {
  constructor (private uiService: UiService) { }
  
  get type() {
    return this.uiService.type;
  }

  get id() {
    return this.uiService.id;
  }

  set branchType(value: string) {
    this.uiService.typeValue = value;
  }

  set branchId(value: string) {
    this.uiService.idValue = value;
  }

  get isInDiff() {
    return this.uiService.isInDiff;
  }

  set DiffMode(value: boolean) {
    this.uiService.diffMode = value;
  }
}
