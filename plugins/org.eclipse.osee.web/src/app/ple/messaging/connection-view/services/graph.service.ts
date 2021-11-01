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
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { apiURL } from 'src/environments/environment';
import { connection, connectionWithChanges, OseeEdge } from '../../shared/types/connection';
import { node, nodeData, nodeDataWithChanges, OseeNode } from '../../shared/types/node';

@Injectable({
  providedIn: 'root'
})
export class GraphService {

  constructor (private http: HttpClient) { }
  
  getNodes(id: string) {
    return this.http.get<{nodes:OseeNode<nodeData>[],edges:OseeEdge<connection>[]}>(apiURL+'/mim/branch/'+id+"/graph")
  }
}
