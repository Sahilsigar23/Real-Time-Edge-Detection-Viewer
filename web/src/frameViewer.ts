/**
 * Frame Viewer - Displays processed frames from Edge Detector app
 */

import { FrameStats, ProcessedFrame, ViewerConfig } from './types.js';

export class FrameViewer {
    private container: HTMLElement;
    private imageElement: HTMLImageElement;
    private statsElement: HTMLDivElement;
    private config: ViewerConfig;
    private currentFrame: ProcessedFrame | null = null;

    constructor(config: ViewerConfig) {
        this.config = config;
        
        const containerElement = document.getElementById(config.containerId);
        if (!containerElement) {
            throw new Error(`Container with id "${config.containerId}" not found`);
        }
        this.container = containerElement;
        
        this.imageElement = this.createImageElement();
        this.statsElement = this.createStatsElement();
        
        this.setupDOM();
    }

    private createImageElement(): HTMLImageElement {
        const img = document.createElement('img');
        img.className = 'frame-image';
        img.alt = 'Processed frame';
        img.style.maxWidth = '100%';
        img.style.height = 'auto';
        img.style.border = '2px solid #333';
        img.style.borderRadius = '8px';
        return img;
    }

    private createStatsElement(): HTMLDivElement {
        const div = document.createElement('div');
        div.className = 'frame-stats';
        div.style.marginTop = '20px';
        div.style.padding = '15px';
        div.style.backgroundColor = '#f5f5f5';
        div.style.borderRadius = '8px';
        div.style.fontFamily = 'monospace';
        return div;
    }

    private setupDOM(): void {
        this.container.appendChild(this.imageElement);
        if (this.config.showStats) {
            this.container.appendChild(this.statsElement);
        }
    }

    /**
     * Update the viewer with a new processed frame
     */
    public updateFrame(frame: ProcessedFrame): void {
        this.currentFrame = frame;
        this.imageElement.src = frame.imageData;
        
        if (this.config.showStats) {
            this.updateStats(frame.stats);
        }
    }

    /**
     * Update the stats display
     */
    private updateStats(stats: FrameStats): void {
        const statsHTML = `
            <h3 style="margin-top: 0; color: #333;">Frame Statistics</h3>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
                <div>
                    <strong>FPS:</strong> ${stats.fps.toFixed(2)}
                </div>
                <div>
                    <strong>Resolution:</strong> ${stats.width}x${stats.height}
                </div>
                ${stats.processingTime ? `
                <div>
                    <strong>Processing Time:</strong> ${stats.processingTime.toFixed(2)}ms
                </div>
                ` : ''}
            </div>
        `;
        this.statsElement.innerHTML = statsHTML;
    }

    /**
     * Load a sample frame from base64 string
     */
    public loadSampleFrame(base64Data: string, stats: FrameStats): void {
        const frame: ProcessedFrame = {
            imageData: base64Data,
            stats: stats,
            timestamp: Date.now()
        };
        this.updateFrame(frame);
    }

    /**
     * Clear the viewer
     */
    public clear(): void {
        this.imageElement.src = '';
        this.statsElement.innerHTML = '';
        this.currentFrame = null;
    }

    /**
     * Get the current frame
     */
    public getCurrentFrame(): ProcessedFrame | null {
        return this.currentFrame;
    }
}
