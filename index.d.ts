export as namespace AndroidBLEAdvertiserModule;

export interface ScanOptions {
    numberOfMatches?: number;
    matchMode?: number;
    scanMode?: number;
    reportDelay?: number;
}

export function setCompanyId(companyId: number): void;
export function broadcast(uid: String, payload: number[]): Promise<string>;
export function stopBroadcast(): Promise<string>;
export function scan(payload: number[], options?: ScanOptions): Promise<string>;
export function stopScan(): Promise<string>;
export function enableAdapter(): void;
export function disableAdapter(): void;
export function getAdapterState(): Promise<string>;