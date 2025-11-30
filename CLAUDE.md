# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

合記 (heji) is a multi-user expense tracking application with real-time synchronization. The project consists of:

- **Android Client** (`client/`): Kotlin/Java Android app using MVI architecture
- **Go Server** (`server/`): REST API and WebSocket server with clean architecture
- **Protocol Buffers** (`proto/`): Communication protocol definitions

## Architecture

### Client (Android)
- **Pattern**: Single Activity + Multiple Fragment MVI architecture
- **Database**: Room (SQLite) for local-first data storage
- **Sync**: WebSocket-based real-time synchronization with offline capability
- **Key Libraries**: Navigation, Retrofit, Room, MPAndroidChart, DataStore

### Server (Go)
- **Pattern**: Clean architecture with dependency injection
- **Framework**: Gin for HTTP/WebSocket endpoints  
- **Database**: MongoDB for primary storage, Redis for caching
- **Storage**: MinIO for file/image storage
- **Auth**: JWT-based authentication

### Communication
- **Protocol**: Protocol Buffers (proto3) for client-server communication
- **Sync**: WebSocket for real-time data synchronization
- **Offline**: Client-first with background sync when online

## Common Development Commands

### Server Development
```bash
# Navigate to server directory
cd server/

# Run development server
go run main.go

# Build server binary
go build -o heji-server main.go

# Run with specific config
go run main.go -config config.yml

# Install dependencies
go mod download
go mod tidy
```

### Android Client Development
```bash
# Navigate to client directory
cd client/

# Build debug APK
./gradlew assembleDebug

# Build release APK (both local and cloud flavors)
./gradlew assembleRelease

# Install debug build to device
./gradlew installDebug

# Run tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Connect ADB to MuMu emulator (custom task)
./gradlew aconnectMuMUu
```

### Protocol Buffer Generation
```bash
# Generate all protobuf files (from server/wsmsg directory)
./gen.bat

# Or manually generate specific files:
# Generate Go protobuf files
protoc --go_out=. message.proto

# Generate Java protobuf files
protoc --java_out=. message.proto

# Generate Kotlin protobuf files
protoc --kotlin_out=. message.proto
```

### Testing
```bash
# Server tests
cd server/
go test ./...

# Android unit tests
cd client/
./gradlew test

# Android instrumented tests
./gradlew connectedAndroidTest
```

## Key Configuration Files

- `server/config.yml`: Server configuration (MongoDB, Redis, MinIO, JWT settings)
- `client/app/build.gradle`: Android app build configuration with product flavors (local/cloud)
- `client/gradle.properties`: Build properties and network endpoints for different flavors
- `server/go.mod`: Go module dependencies
- `server/Dockerfile`: Multi-stage Docker build configuration
- `.drone.yml`: CI/CD pipeline configuration
- `server/wsmsg/gen.bat`: Protocol buffer generation script

## Data Flow & Sync Architecture

1. **Local First**: Client stores data in Room database immediately
2. **Background Sync**: Changes are synced to server via WebSocket in background
3. **Conflict Resolution**: Server timestamp-based conflict resolution
4. **Offline Support**: Full offline functionality with sync when reconnected

## Build Flavors & Deployment

### Android Product Flavors
- **local**: Development build pointing to localhost (`10.0.2.2:8080`)
- **cloud**: Production build pointing to cloud server

### CI/CD Pipeline
- Automated builds triggered on push via Drone CI
- Server: Docker image build and deployment
- Client: APK building and automatic deployment with custom naming

## Important Notes

- Uses ObjectId for consistent primary keys across client/server
- All file uploads go through MinIO storage service
- JWT tokens required for authenticated endpoints
- WebSocket connection handles real-time bill/book synchronization
- Client supports importing from Alipay, WeChat, ETC, Excel, CSV formats
- Export functionality supports Excel, CSV, and QianJi formats
- Sentry integration for error monitoring and performance tracking
- Multi-stage Docker builds with UPX compression for optimized server deployment