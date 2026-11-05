@file:JsModule("./skiko.mjs")
@file:OptIn(ExperimentalWasmJsInterop::class)

package tech.bashga.plata.screens.delivery

// Skiko's own wasm instance, whose memory Skia reads pixels from; `_` holds its exports once Skiko has loaded
internal external val loadedWasm: JsAny
