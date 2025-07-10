package net.scirave.nox.mixin;

import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.block.Block;
import net.scirave.nox.Nox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.List;

@Mixin(ShearsItem.class)
public class ShearsItemMixin {

    @ModifyArg(method = "createToolProperties", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/Tool$Rule;minesAndDrops(Ljava/util/List;F)Lnet/minecraft/world/item/component/Tool$Rule;"))
    private static List<Block> nox$createToolComponent(List<Block> blocks) {
        var list = new ArrayList<>(blocks);
        list.add(Nox.NOX_COBWEB);
        return list;
    }
}
