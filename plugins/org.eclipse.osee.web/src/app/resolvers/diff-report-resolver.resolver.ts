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
import {
  Router, Resolve,
  RouterStateSnapshot,
  ActivatedRouteSnapshot
} from '@angular/router';
import { Observable, of, ReplaySubject } from 'rxjs';
import { first, map, switchMap } from 'rxjs/operators';
import { DiffUIService } from '../ple-services/httpui/diff-uiservice.service';
import { changeInstance } from '../types/change-report/change-report';

@Injectable({
  providedIn: 'root'
})
export class DiffReportResolver implements Resolve<changeInstance[] | undefined> {
  replaySubject = new ReplaySubject<changeInstance[] | undefined>(1)
  requested = false;
  constructor (private diffService:DiffUIService) { }
  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<changeInstance[] | undefined> {
    if (this.diffService.id !== route.paramMap.get('branchId')) {
      this.requested = false;
      this.diffService.branchId = route.paramMap.get('branchId') || '';
    }
    if (this.diffService.type !== route.paramMap.get('branchType')) {
      this.requested = false;
      this.diffService.branchType = route.paramMap.get('branchType') || '';
    }
    this.diffService.DiffMode = route.url.some(e => e.path === 'diff');

    if (!this.requested) {
      this.requested = true;
      this.diffService.diff.subscribe((value) => {
        this.replaySubject.next(value);
      })
    }
    return this.replaySubject.pipe(first());
  }
}
