# Edge Detector Web Viewer

TypeScript-based web viewer for displaying processed frames from the Edge Detector Android app.

## Features

- 📊 Real-time frame statistics display (FPS, resolution, processing time)
- 🖼️ Base64 image rendering support
- 📘 Modular TypeScript architecture with type safety
- 🎨 Modern responsive UI
- 🔄 Frame refresh capability

## Setup

### Prerequisites

- Node.js 14+ and npm
- TypeScript 5.3+

### Installation

```bash
cd web
npm install
```

### Build

```bash
npm run build
```

This will compile TypeScript files to JavaScript in the `dist/` directory.

### Development

```bash
npm run watch
```

Watches for changes and recompiles automatically.

### Running

Open `web/public/index.html` in a browser, or serve it with a local server:

```bash
# Using Python
python -m http.server 8080

# Using Node.js http-server (install with: npm install -g http-server)
http-server public -p 8080
```

Then navigate to `http://localhost:8080`

## Project Structure

```
web/
├── src/
│   ├── types.ts         # TypeScript type definitions
│   ├── frameViewer.ts   # Main viewer class
│   └── index.ts         # Entry point
├── public/
│   └── index.html       # HTML page
├── dist/                # Compiled JavaScript (generated)
├── package.json         # npm configuration
├── tsconfig.json        # TypeScript configuration
└── README.md           # This file
```

## Architecture

### Types (`types.ts`)
Defines interfaces for:
- `FrameStats`: Frame statistics (FPS, resolution, processing time)
- `ProcessedFrame`: Processed frame data structure
- `ViewerConfig`: Viewer configuration options

### Frame Viewer (`frameViewer.ts`)
Main viewer class that handles:
- Image display
- Statistics rendering
- DOM manipulation
- Frame updates

### Main Entry (`index.ts`)
Application initialization:
- Sets up viewer instance
- Loads sample frames
- Handles user interactions
- Provides API for external use

## Integration with Android App

In production, this viewer would receive frames from the Android app through:

1. **WebSocket Connection**: Real-time streaming of processed frames
2. **HTTP REST API**: Polling for frame updates
3. **Local File System**: Reading saved frames

Currently, it uses a static sample frame for demonstration.

## API Usage

```typescript
import { FrameViewer } from './frameViewer';

// Create viewer
const viewer = new FrameViewer({
    containerId: 'viewer-container',
    showStats: true,
    autoUpdate: false
});

// Load a frame
viewer.loadSampleFrame(base64ImageData, {
    fps: 24.5,
    width: 640,
    height: 480,
    processingTime: 42.3
});

// Get current frame
const frame = viewer.getCurrentFrame();
```

## Future Enhancements

- [ ] WebSocket integration for real-time streaming
- [ ] Multiple frame format support (JPEG, PNG, WebP)
- [ ] Frame recording and playback
- [ ] Performance graphs and charts
- [ ] Advanced filtering options
- [ ] Mobile-responsive controls

## License

MIT License - Created for Flam RnD Intern Assessment 2025
