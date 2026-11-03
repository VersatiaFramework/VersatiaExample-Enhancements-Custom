package dev.versatia.example.timing

import dev.versatia.EnhancementContext
import dev.versatia.EnhancementProcessor
import dev.versatia.classLoader
import dev.versatia.engine.descriptor.EnhancedElement
import dev.versatia.engine.descriptor.FunctionTarget
import dev.versatia.inject
import dev.versatia.receiverOf
import dev.versatia.targetAs
import dev.versatia.util.scheduler.runTaskTimer

/**
 * Schedules every `@Timed` function once the plugin's beans exist. No fields: whatever a processor
 * needs comes from the element and the context. Bukkit cancels the tasks when the plugin is disabled.
 */
class TimedEnhancementProcessor : EnhancementProcessor {
    override fun process(elements: List<EnhancedElement>, context: EnhancementContext) = context.onPluginEnhanced {
        for (element in elements) {
            val target = element.targetAs<FunctionTarget>(this)
            val ticks = element.annotation.arguments["everyTicks"] as Long
            val instance = context.receiverOf(target)
            val arguments = context.inject(target.description, target.parameters)
            // The context lives for one event; the task keeps only what it needs
            val classLoader = context.classLoader
            context.plugin.runTaskTimer(periodTicks = ticks, delayTicks = ticks) { target.invoker.invoke(classLoader, instance, arguments) }
            context.plugin.logger.info("[${javaClass.simpleName}] scheduled ${target.functionName}() every $ticks ticks")
        }
    }
}
