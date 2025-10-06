/**
 * Main entry point for Edge Detector Web Viewer
 */

import { FrameViewer } from './frameViewer';
import { FrameStats } from './types';

// Sample edge-detected image placeholder
// In production, this would be loaded from the Android app or an API endpoint
const SAMPLE_EDGE_IMAGE = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNjQwIiBoZWlnaHQ9IjQ4MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iNjQwIiBoZWlnaHQ9IjQ4MCIgZmlsbD0iIzAwMCIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmaWxsPSIjZmZmIiBmb250LXNpemU9IjI0IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkb21pbmFudC1iYXNlbGluZT0ibWlkZGxlIj5FZGdlIERldGVjdGlvbiBTYW1wbGU8L3RleHQ+PHRleHQgeD0iNTAlIiB5PSI2MCUiIGZpbGw9IiNmZmYiIGZvbnQtc2l6ZT0iMTYiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGRvbWluYW50LWJhc2VsaW5lPSJtaWRkbGUiPjY0MHg0ODA8L3RleHQ+PC9zdmc+';

// Sample frame statistics
const sampleStats: FrameStats = {
    fps: 24.5,
    width: 640,
    height: 480,
    processingTime: 42.3
};

// Initialize the viewer when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    console.log('Edge Detector Web Viewer initializing...');

    try {
        // Create the frame viewer instance
        const viewer = new FrameViewer({
            containerId: 'viewer-container',
            showStats: true,
            autoUpdate: false
        });

        // Load sample frame
        viewer.loadSampleFrame(SAMPLE_EDGE_IMAGE, sampleStats);

        console.log('Frame viewer initialized successfully');

        // Add a refresh button handler if it exists
        const refreshButton = document.getElementById('refresh-btn');
        if (refreshButton) {
            refreshButton.addEventListener('click', () => {
                console.log('Refreshing frame...');
                viewer.loadSampleFrame(SAMPLE_EDGE_IMAGE, {
                    ...sampleStats,
                    fps: 20 + Math.random() * 10,
                    processingTime: 30 + Math.random() * 30
                });
            });
        }

        // Demonstrate the API by logging current frame info
        setTimeout(() => {
            const currentFrame = viewer.getCurrentFrame();
            if (currentFrame) {
                console.log('Current frame loaded:', {
                    timestamp: new Date(currentFrame.timestamp).toISOString(),
                    stats: currentFrame.stats
                });
            }
        }, 1000);

    } catch (error) {
        console.error('Failed to initialize viewer:', error);
        const container = document.getElementById('viewer-container');
        if (container) {
            container.innerHTML = '<p style="color: red; padding: 20px;">Failed to initialize viewer. Check console for details.</p>';
        }
    }
});

// Export for external use if needed
export { FrameViewer };
