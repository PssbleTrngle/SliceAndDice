package com.possible_triangle.sliceanddice.compat

import com.kipti.bnb.content.kinetics.cogwheel_chain.types.CogwheelChainType
import com.kipti.bnb.registry.core.BnbRegistries
import com.kipti.bnb.registry.core.BnbTags.BnbBlockTags
import com.possible_triangle.sliceanddice.MOD_ID
import com.possible_triangle.sliceanddice.modLoc
import net.minecraft.world.level.block.Blocks
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredRegister
import vectorwing.farmersdelight.common.registry.ModItems

object BitsAndBobsCompat {
    fun register(modBus: IEventBus) {
        val registry = DeferredRegister.create(BnbRegistries.COGWHEEL_CHAIN_TYPES, MOD_ID)

        registry.register("pasta") { _ ->
            CogwheelChainType
                .Builder()
                .relatedItem(ModItems.RAW_PASTA)
                .setCogwheelPredicate(BnbBlockTags.FLANGED_COGWHEEL::matches)
                .renderType(CogwheelChainType.ChainRenderInfo.ROPE)
                .renderTexture(modLoc("textures/block/chain_pasta.png"))
                .breakEffectsBlock { Blocks.HAY_BLOCK }
                .build()
        }

        registry.register(modBus)
    }
}
