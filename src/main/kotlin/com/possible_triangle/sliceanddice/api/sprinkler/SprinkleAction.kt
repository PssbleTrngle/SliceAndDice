package com.possible_triangle.sliceanddice.api.sprinkler

import com.mojang.serialization.Codec
import com.possible_triangle.sliceanddice.api.SDRegistries
import com.possible_triangle.sliceanddice.block.sprinkler.SprinklerBlock
import dev.ryanhcode.sable.companion.SableCompanion
import net.createmod.catnip.outliner.Outliner
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.core.Position
import net.minecraft.core.Vec3i
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.entity.EntityTypeTest
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import kotlin.math.ceil
import kotlin.math.floor

interface SprinkleAction {
    companion object {
        @JvmField
        val CODEC: Codec<Holder<SprinkleAction>> =
            Codec.lazyInitialized {
                SDRegistries.SPRINKLER_ACTIONS_REGISTRY.holderByNameCodec()
            }
    }

    fun tick(context: SprinkeContext) {
    }

    fun start(context: SprinkeContext) {
    }

    fun stop(context: SprinkeContext) {
    }
}
