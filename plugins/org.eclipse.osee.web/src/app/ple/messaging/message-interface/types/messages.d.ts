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
import { applic } from "../../../../types/applicability/applic";
import { subMessage } from "./sub-messages";

export interface message {
    id: string,
    name: string,
    description: string ,
    subMessages: Array<Required<subMessage>>,
    interfaceMessageRate: string ,
    interfaceMessagePeriodicity: string ,
    interfaceMessageWriteAccess: boolean ,
    interfaceMessageType: string ,
    interfaceMessageNumber: string,
    applicability?:applic
}