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
export interface healthStatus {
	servers: healthServer[];
}

export interface healthServer {
	serverAlive: Boolean;
	dbAlive: Boolean;
	name: string;
	errorMsg: string;
}

export interface remoteHealthDetails {
	healthDetails: healthDetails;
	errorMsg: string;
}
export interface healthDetails {
	uri: string;
	startTime: string;
	upTime: string;
	authScheme: string;
	authSchemeSupported: string[];
	heapMemAlloc: string;
	heapMemMax: string;
	heapMemUsed: string;
	nonHeapMemAlloc: string;
	nonHeapMemMax: string;
	nonHeapMemUsed: string;
	codeLocation: string;
	systemLoad: string;
	supportedVersions: string[];
	serverId: string;
	binaryDataPath: string;
	threadStats: string[];
	garbageCollectorStats: string[];
	serverWithHealthInfo: string;
}

export const defaultRemoteHealthDetails: remoteHealthDetails = {
	healthDetails: {
		uri: '',
		startTime: '',
		upTime: '',
		authScheme: '',
		authSchemeSupported: [],
		heapMemAlloc: '',
		heapMemMax: '',
		heapMemUsed: '',
		nonHeapMemAlloc: '',
		nonHeapMemMax: '',
		nonHeapMemUsed: '',
		codeLocation: '',
		systemLoad: '',
		supportedVersions: [],
		serverId: '',
		binaryDataPath: '',
		threadStats: [],
		garbageCollectorStats: [],
		serverWithHealthInfo: '',
	},
	errorMsg: '',
};

export interface remoteHealthLog {
	healthLog: healthLog;
}

export interface healthLog {
	log: string;
}

export interface remoteHealthJava {
	healthJava: healthJava;
	errorMsg: string;
}

export interface healthJava {
	vmName: string;
	vmVendor: string;
	vmVersion: string;
	vmSpecVersion: string;
	classPath: string;
	libraryPath: string;
	osName: string;
	osVersion: string;
	osArch: string;
	processArgs: string[];
	processes: string[];
}
export interface remoteHealthTop {
	healthTop: healthTop;
	errorMsg: string;
}

export interface healthTop {
	top: string;
}

export interface healthBalancers {
	balancers: healthBalancer[];
}

export interface healthBalancer {
	name: string;
	alive: boolean;
	errorMsg: string;
}

export interface healthActiveMq {
	activeMqUrl: string;
	active: boolean;
	errorMsg: string;
}

export interface healthUsage {
	allUsers: user[];
	allSessions: session[];
	versionTypeMap: versionTypeMap;
	versionNameMap: versionNameMap;
	errorMsg: string;
}

export interface user {
	name: string;
	email: string;
	userId: string;
	accountId: number;
}

export interface session {
	user: user;
	date: string;
	version: string;
	sessionId: string;
	clientAddress: string;
	clientMachineName: string;
	port: string;
}

export interface versionTypeMap {
	[versionType: string]: user[];
}

export interface versionNameMap {
	[versionName: string]: user[];
}

export interface unknownJson {
	[key: string]: unknown;
}

export interface healthSql {
	errorMsg: string;
	sqls: sql[];
}

export interface sql {
	sqlText: string;
	elapsedTime: string;
	executions: string;
	elapsedTimeAverage: string;
	percent: string;
}

export interface healthSqlSize {
	errorMsg: string;
	size: number;
}

export interface healthTablespace {
	errorMsg: string;
	tablespaces: tablespace[];
}

export interface tablespace {
	tablespaceName: string;
	maxTsPctUsed: string;
	autoExtend: string;
	tsPctUsed: string;
	tsPctFree: string;
	usedTsSize: string;
	freeTsSize: string;
	currTsSize: string;
	maxTxSize: string;
}
