// Custom webpack configuration for WASM target
// This file is automatically picked up by Kotlin/JS Gradle plugin

config.resolve = config.resolve || {};
config.resolve.alias = config.resolve.alias || {};

// Exclude problematic modules that don't support WASM
config.resolve.alias["kotest-framework-engine"] = false;
config.resolve.alias["kotest-assertions-core"] = false;
config.resolve.alias["kotest-property"] = false;

// Fallback for Node.js modules
config.resolve.fallback = config.resolve.fallback || {};
config.resolve.fallback.fs = false;
config.resolve.fallback.path = false;
config.resolve.fallback.os = false;

// Disable some webpack optimizations that may cause issues
config.optimization = config.optimization || {};
config.optimization.sideEffects = false;

console.log("[WASM Webpack] Custom config loaded");
