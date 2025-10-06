/**
 * Type definitions for Edge Detector Web Viewer
 */

export interface FrameStats {
    fps: number;
    width: number;
    height: number;
    processingTime?: number;
}

export interface ProcessedFrame {
    imageData: string; // Base64 encoded image
    stats: FrameStats;
    timestamp: number;
}

export interface ViewerConfig {
    containerId: string;
    showStats: boolean;
    autoUpdate: boolean;
}
