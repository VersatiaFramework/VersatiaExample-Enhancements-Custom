package com.github.marcoral.versatia.example.timing

import com.github.marcoral.versatia.EnhancementContext
import com.github.marcoral.versatia.EnhancementException
import com.github.marcoral.versatia.EnhancementProcessor
import com.github.marcoral.versatia.classLoader
import com.github.marcoral.versatia.di.BeanResolver
import com.github.marcoral.versatia.engine.descriptor.EnhancedElement
import com.github.marcoral.versatia.engine.descriptor.FunctionTarget
import com.github.marcoral.versatia.engine.descriptor.MemberOwner
import com.github.marcoral.versatia.util.scheduler.runTaskTimer

/**
 * Schedules every `@Timed` function once the plugin's beans exist. No fields: whatever a processor
 * needs comes from the element and the context. Bukkit cancels the tasks when the plugin is disabled.
 */
class TimedEnhancementProcessor : EnhancementProcessor {
    override fun process(elements: List<EnhancedElement>, context: EnhancementContext) = context.onPluginEnhanced {
        val beans = context.state[BeanResolver.KEY]
        for (element in elements) {
            val target = element.target as? FunctionTarget ?: throw EnhancementException("${javaClass.name} can only enhance functions, got ${element.target}")
            val ticks = element.annotation.arguments["everyTicks"] as Long
            val description = "${target.className}.${target.functionName}()"
            val instance = when (target.owner) {
                MemberOwner.PLUGIN -> context.plugin
                MemberOwner.OBJECT -> Class.forName(target.binaryClassName, true, context.classLoader).getDeclaredField("INSTANCE").apply { isAccessible = true }.get(null)
                MemberOwner.INSTANCE -> beans?.instanceOf(target.className) ?: throw EnhancementException("Plugin ${context.plugin.name}: no bean ${target.className} exists to call $description on")
            }
            val arguments = if (target.parameters.isEmpty()) emptyList() else beans?.resolve(description, target.parameters)
                ?: throw EnhancementException("Plugin ${context.plugin.name}: $description needs injected parameters, but the plugin has no beans")
            // The context lives for one event; the task keeps only what it needs
            val classLoader = context.classLoader
            context.plugin.runTaskTimer(periodTicks = ticks, delayTicks = ticks) { target.invoker.invoke(classLoader, instance, arguments) }
            context.plugin.logger.info("[${javaClass.simpleName}] scheduled ${target.functionName}() every $ticks ticks")
        }
    }
}
