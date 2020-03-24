export as namespace AndroidBLEAdvertiserModule;

export function setCompanyId(companyId: number): void;
export function broadcastPacket(uid: String, payload: number[]): Promise<boolean>;
export function cancelPacket(uid: String): void;
export function cancelAllPackets(): void;
export function enableAdapter(): void;
export function disableAdapter(): void;
export function getAdapterState(): Promise<string>;