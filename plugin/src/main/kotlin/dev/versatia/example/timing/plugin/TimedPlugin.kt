package dev.versatia.example.timing.plugin

import dev.versatia.VersatiaPlugin
import dev.versatia.di.LocalBean
import dev.versatia.example.timing.Timed
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
