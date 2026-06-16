package com.possible_triangle.sliceanddice.block.sprinkler.actions

import com.possible_triangle.sliceanddice.api.sprinkler.SimpleSprinkleActionType
import com.possible_triangle.sliceanddice.api.sprinkler.SprinkleAction
import com.possible_triangle.sliceanddice.api.sprinkler.SprinkleContext
import com.possible_triangle.sliceanddice.index.SDTags
import net.minecraft.world.level.block.BonemealableBlock

object FertilizerAction : SprinkleAction {
    override fun type() = Type

    object Type : SimpleSprinkleActionType<FertilizerAction>(FertilizerAction) {
        override fun consume(
            context: SprinkleContext,
            config: FertilizerAction,
        ) {
            context.forEachBlock { pos ->
                val state = context.level.getBlockState(pos)
                if (state.`is`(SDTags.FERTILIZER_BLACKLIST)) return@forEachBlock
                val block = state.block

                if (block !is BonemealableBlock) return@forEachBlock
                if (!block.isValidBonemealTarget(context.level, pos, state)) return@forEachBlock
                if (context.random.nextInt(30) < 26) return@forEachBlock
                if (!block.isBonemealSuccess(context.level, context.random, pos, state)) return@forEachBlock

                block.performBonemeal(context.level, context.random, pos, state)
            }
        }
    }
}
