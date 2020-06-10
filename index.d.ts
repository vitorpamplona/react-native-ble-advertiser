export as namespace BLEAdvertiser;

export interface ScanOptions {
    numberOfMatches?: number;
    matchMode?: number;
    scanMode?: number;
    reportDelay?: number;
}

export interface BroadcastOptions {
    txPowerLevel?: number;
    advertiseMode?: number;
    includeDeviceName?: boolean;
    includeTxPowerLevel?: boolean;
    connectable?: boolean;
}

export function setCompanyId(companyId: number): void;
export function broadcast(uid: String, manufData: number[], options?: BroadcastOptions): Promise<string>;
export function stopBroadcast(): Promise<string>;
export function scan(manufDataFilter: number[], options?: ScanOptions): Promise<string>;
export function scanByService(uidFilter: String, options?: ScanOptions): Promise<string>;
export function stopScan(): Promise<string>;
export function enableAdapter(): void;
export function disableAdapter(): void;
export function getAdapterState(): Promise<string>;
export function isActive(): Promise<boolean>;