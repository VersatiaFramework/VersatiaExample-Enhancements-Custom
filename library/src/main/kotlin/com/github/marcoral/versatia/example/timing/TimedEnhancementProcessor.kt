package com.github.marcoral.versatia.example.timing

import com.github.marcoral.versatia.EnhancementContext
import com.github.marcoral.versatia.EnhancementException
import com.github.marcoral.versatia.EnhancementProcessor
import com.github.marcoral.versatia.classLoader
import com.github.marcoral.versatia.di.BeanResolver
import com.github.marcoral.versatia.engine.descriptor.EnhancedElement
import com.github.marcoral.versatia.engine.descriptor.FunctionOwner
import com.github.marcoral.versatia.engine.descriptor.FunctionTarget

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
                FunctionOwner.PLUGIN -> context.plugin
                FunctionOwner.OBJECT -> Class.forName(target.binaryClassName, true, context.classLoader).getDeclaredField("INSTANCE").get(null)
                FunctionOwner.INSTANCE -> beans?.instanceOf(target.className) ?: throw EnhancementException("Plugin ${context.plugin.name}: no bean ${target.className} exists to call $description on")
            }
            val arguments = if (target.parameters.isEmpty()) emptyList() else beans?.resolve(description, target.parameters)
                ?: throw EnhancementException("Plugin ${context.plugin.name}: $description needs injected parameters, but the plugin has no beans")
            context.plugin.server.scheduler.runTaskTimer(context.plugin, Runnable { target.invoker.invoke(context.classLoader, instance, arguments) }, ticks, ticks)
            context.plugin.logger.info("[${javaClass.simpleName}] scheduled ${target.functionName}() every $ticks ticks")
        }
    }
}
