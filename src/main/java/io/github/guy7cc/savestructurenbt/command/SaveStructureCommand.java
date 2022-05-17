package io.github.guy7cc.savestructurenbt.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ResourceLocationException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.event.RegisterCommandsEvent;
import org.w3c.dom.Text;

public class SaveStructureCommand {
    public static void register(RegisterCommandsEvent event){
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("ss")
                .then(Commands.argument("begin", BlockPosArgument.blockPos())
                        .then(Commands.argument("end", BlockPosArgument.blockPos())
                                .then(Commands.argument("namespace", StringArgumentType.string())
                                        .then(Commands.argument("path", StringArgumentType.string())
                                                .executes(ctx ->
                                                        saveStructure(
                                                                ctx.getSource(),
                                                                BlockPosArgument.getSpawnablePos(ctx,"begin"),
                                                                BlockPosArgument.getSpawnablePos(ctx, "end"),
                                                                StringArgumentType.getString(ctx, "namespace"),
                                                                StringArgumentType.getString(ctx, "path"))))))));
    }
    private static int saveStructure(CommandSourceStack source, BlockPos begin, BlockPos end, String namespace, String path) {
        ResourceLocation loc = new ResourceLocation(namespace, path);
        BlockPos min = new BlockPos(Math.min(begin.getX(), end.getX()), Math.min(begin.getY(), end.getY()), Math.min(begin.getZ(), end.getZ()));
        BlockPos max = new BlockPos(Math.max(begin.getX(), end.getX()), Math.max(begin.getY(), end.getY()), Math.max(begin.getZ(), end.getZ()));
        Vec3i size = new Vec3i(max.getX() - min.getX() + 1,max.getY() - min.getY() + 1,max.getZ() - min.getZ() + 1);

        boolean playerExists = false;
        ServerPlayer player = null;
        try{
            player = source.getPlayerOrException();
            playerExists = true;
        } catch(CommandSyntaxException exception){

        }

        ServerLevel serverlevel = source.getLevel();
        StructureManager structuremanager = serverlevel.getStructureManager();

        StructureTemplate structuretemplate;
        try {
            structuretemplate = structuremanager.getOrCreate(loc);
        } catch (ResourceLocationException resourcelocationexception1) {
            source.sendFailure(new TextComponent(resourcelocationexception1.getMessage()));
            return 0;
        }

        structuretemplate.fillFromWorld(serverlevel, min, size, true, Blocks.STRUCTURE_VOID);
        structuretemplate.setAuthor(playerExists ? player.getName().getString() : "?");

        try {
            if(structuremanager.save(loc)){
                source.sendSuccess(new TextComponent("Successfully saved the structure!\n").setStyle(Style.EMPTY.withColor(0x00ffff))
                                .append(new TextComponent("Size: ").setStyle(Style.EMPTY.withColor(0xffff00)))
                                .append(new TextComponent(size.getX() + " * " + size.getY() + " * " + size.getZ() + "\n").setStyle(Style.EMPTY.withColor(0xffffff)))
                                .append(new TextComponent("Author: ").setStyle(Style.EMPTY.withColor(0xffff00)))
                                .append(new TextComponent(structuretemplate.getAuthor() + "\n").setStyle(Style.EMPTY.withColor(0xffffff)))
                                .append(new TextComponent("Location: ").setStyle(Style.EMPTY.withColor(0xffff00)))
                                .append(new TextComponent("saves/" + serverlevel.getServer().getWorldData().getLevelName() + "/generated/" + loc.getNamespace() + "/structures/" + loc.getPath() + ".nbt").setStyle(Style.EMPTY.withColor(0xffffff))), true);
                return 1;
            }
            else{
                source.sendFailure(new TextComponent("Failed to save."));
                return 0;
            }
        } catch (ResourceLocationException resourcelocationexception) {
            source.sendFailure(new TextComponent(resourcelocationexception.getMessage()));
            return 0;
        }


    }
}
