// Custom task to build WASM distribution without webpack issues

import java.nio.file.Files
import java.nio.file.StandardCopyOption

tasks.register<Copy>("buildWasmDist") {
    group = "wasm"
    description = "Builds WASM distribution files"
    
    dependsOn("wasmJsBrowserProductionWebpack")
    
    // Source directories
    from("src/wasmJsMain/resources") {
        include("**/*.html", "**/*.css", "**/*.js")
    }
    
    // Copy compiled WASM files
    from("build/kotlin-webpack/wasmJs/productionExecutable") {
        include("*.js", "*.wasm")
    }
    // Compose resources (drawable/strings) — БЕЗ них белый экран после сплэша (404, 2026-08-08)
    from("build/dist/wasmJs/productionExecutable") {
        include("composeResources/**")
    }
    
    into("build/wasm-dist")
    
    doLast {
        println("✅ WASM distribution built in build/wasm-dist/")
        println("📁 Run: python -m http.server 8085 --directory build/wasm-dist")
    }
}

tasks.register<Exec>("serveWasm") {
    group = "wasm"
    description = "Serve WASM distribution with Python HTTP server"
    dependsOn("buildWasmDist")
    
    workingDir("build/wasm-dist")
    commandLine("python", "-m", "http.server", "8085")
}
