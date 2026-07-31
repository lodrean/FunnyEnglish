// Custom task to build WASM distribution without webpack issues

import java.nio.file.Files
import java.nio.file.StandardCopyOption

tasks.register<Copy>("buildWasmDist") {
    group = "wasm"
    description = "Builds WASM distribution files"
    
    dependsOn("compileKotlinWasmJs")
    
    // Source directories
    from("src/wasmJsMain/resources") {
        include("**/*.html", "**/*.css", "**/*.js")
    }
    
    // Copy compiled WASM files
    from("build/kotlin-webpack/wasmJs/productionExecutable") {
        include("*.js", "*.wasm")
    }
    
    into("build/wasm-dist")
    
    doLast {
        println("✅ WASM distribution built in build/wasm-dist/")
        println("📁 Run: python -m http.server 8081 --directory build/wasm-dist")
    }
}

tasks.register<Exec>("serveWasm") {
    group = "wasm"
    description = "Serve WASM distribution with Python HTTP server"
    dependsOn("buildWasmDist")
    
    workingDir("build/wasm-dist")
    commandLine("python", "-m", "http.server", "8081")
}
