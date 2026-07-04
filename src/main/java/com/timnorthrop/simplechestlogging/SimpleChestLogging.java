package com.timnorthrop.simplechestlogging;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleChestLogging extends JavaPlugin {
    private boolean on = true;

    @Override
    public void onEnable() {
        getLogger().info("SimpleChestLogging enabled! Tracking turned on by default. Run \"simplecl off\" to disable.");

        LiteralArgumentBuilder<CommandSourceStack> toggleCommand = Commands.literal("simplecl")
                .requires(sender -> sender.getSender().isOp())
                .then(Commands.literal("on").executes(ctx -> {
                    if (!on) {
                        setOn(true);
                        ctx.getSource().getSender().sendRichMessage("<green>SimpleChestLogging activated.</green>");
                        return Command.SINGLE_SUCCESS;
                    } else {
                        ctx.getSource().getSender().sendMessage("SimpleChestLogging is already activated!");
                        return Command.SINGLE_SUCCESS;
                    }
                }))
                .then(Commands.literal("off").executes(ctx -> {
                    if (on) {
                        setOn(false);
                        ctx.getSource().getSender().sendRichMessage("<red>SimpleChestLogging deactivated.</red>");
                        return Command.SINGLE_SUCCESS;
                    } else {
                        ctx.getSource().getSender().sendMessage("SimpleChestLogging is already deactivated!");
                        return Command.SINGLE_SUCCESS;
                    }
                }));
        LiteralCommandNode<CommandSourceStack> built = toggleCommand.build();
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,
                commands -> commands.registrar().register(built));

        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
    }

    public boolean isOff() {
        return !on;
    }

    private void setOn(boolean on) {
        this.on = on;
    }
}
