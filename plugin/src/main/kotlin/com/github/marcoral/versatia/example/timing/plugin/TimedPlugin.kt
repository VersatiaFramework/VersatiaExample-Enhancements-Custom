package com.github.marcoral.versatia.example.timing.plugin

import com.github.marcoral.versatia.VersatiaPlugin
import com.github.marcoral.versatia.di.LocalBean
import com.github.marcoral.versatia.example.timing.Timed
import java.time.Instant

class TimedPlugin : VersatiaPlugin() {
    /** Runs every five seconds; `Clock` is injected like any bean dependency. */
    @Timed(everyTicks = 100)
    fun heartbeat(clock: Clock) = logger.info("heartbeat at ${clock.now()}")
}

@LocalBean
class Clock {
    fun now() = Instant.now()
}
