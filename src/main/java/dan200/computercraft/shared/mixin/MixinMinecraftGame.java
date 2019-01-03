/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.mixin;

import dan200.computercraft.client.FrameInfo;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin( MinecraftClient.class )
public abstract class MixinMinecraftGame
{
    /**
     * @see MinecraftClient#render(boolean)
     */
    @Inject( method = "render", at = @At( "HEAD" ) )
    private void onRender( CallbackInfo info )
    {
        FrameInfo.instance().onRenderFrame();
    }
}
