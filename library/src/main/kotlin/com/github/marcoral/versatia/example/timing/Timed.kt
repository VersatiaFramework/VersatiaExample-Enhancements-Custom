package com.github.marcoral.versatia.example.timing

import com.github.marcoral.versatia.Phase
import com.github.marcoral.versatia.VersatiaEnhancement

/** Runs the annotated function every [everyTicks] ticks, with its parameters injected like bean dependencies. */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@VersatiaEnhancement(TimingProcessors.TIMED, enhancementPhase = Phase.PLUGIN_START_ONLY)
annotation class Timed(val everyTicks: Long)

object TimingProcessors {
    const val TIMED = "com.github.marcoral.versatia.example.timing.TimedEnhancementProcessor"
}
