/**
 * Main entry point for Edge Detector Web Viewer
 */

import { FrameViewer } from './frameViewer.js';
import { FrameStats } from './types.js';

// Sample edge-detected image - realistic edge detection visualization
// In production, this would be loaded from the Android app or an API endpoint
const SAMPLE_EDGE_IMAGE = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNjQwIiBoZWlnaHQ9IjQ4MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48ZGVmcz48ZmlsdGVyIGlkPSJlZGdlIj48ZmVHYXVzc2lhbkJsdXIgc3RkRGV2aWF0aW9uPSIyIi8+PGZlQ29sb3JNYXRyaXggdHlwZT0ibWF0cml4IiB2YWx1ZXM9IjAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDEgMCIvPjwvZmlsdGVyPjwvZGVmcz48cmVjdCB3aWR0aD0iNjQwIiBoZWlnaHQ9IjQ4MCIgZmlsbD0iIzAwMCIvPjxnIHN0cm9rZT0iI2ZmZiIgc3Ryb2tlLXdpZHRoPSIyIiBmaWxsPSJub25lIj48cmVjdCB4PSI1MCIgeT0iNTAiIHdpZHRoPSIyMDAiIGhlaWdodD0iMTUwIi8+PGNpcmNsZSBjeD0iNDUwIiBjeT0iMTUwIiByPSI4MCIvPjxsaW5lIHgxPSI1MCIgeTE9IjMwMCIgeDI9IjU5MCIgeTI9IjMwMCIvPjxwb2x5bGluZSBwb2ludHM9IjEwMCw0MDAgMjAwLDM1MCAzMDAsMzgwIDQwMCwzMjAgNTAwLDM3MCI+PC9wb2x5bGluZT48cGF0aCBkPSJNMzIwLDgwIEw0MjAsMTIwIEwzODAsMjAwIEwyODAsMTYwIFoiLz48L2c+PHRleHQgeD0iMzIwIiB5PSI0NTAiIGZpbGw9IiNmZmYiIGZvbnQtc2l6ZT0iMTYiIHRleHQtYW5jaG9yPSJtaWRkbGUiPkNhbm55IEVkZ2UgRGV0ZWN0aW9uIFNpbXVsYXRpb248L3RleHQ+PC9zdmc+';

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
