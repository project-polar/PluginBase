package cc.sfclub.example;

import cc.sfclub.command.Source;
import cc.sfclub.core.Core;
import cc.sfclub.events.MessageEvent;
import cc.sfclub.events.message.direct.PrivateMessageReceivedEvent;
import cc.sfclub.events.message.group.GroupMessageReceivedEvent;
import cc.sfclub.events.server.ServerStartedEvent;
import cc.sfclub.events.server.ServerStoppingEvent;
import cc.sfclub.plugin.Plugin;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import org.greenrobot.eventbus.Subscribe;

/*
     Plugin main class,it should extend Plugin.class.About specify Main class,see build.gradle
 */
public class Main extends Plugin {
    /*
        Fired when server started.(Core loaded)
     */
    @Subscribe
    public void onServerStart(ServerStartedEvent event) {
        //Logging (SLF4J)
        Core.getLogger().info("Here is a example! {}", "I'm a example!");
        //Get a core
        Core.get();
        //About getting things which is core inside
        Core.get().ORM(); //Core.get().XXX (Meaning Core.getXXX())
        //Register a command (Also see https://fabricmc.net/wiki/tutorial:commands#arguments https://github.com/mojang/brigadier)
        Core.get().dispatcher().register(
                LiteralArgumentBuilder.<Source>literal("ROOT_COMMAND")
                        .executes(content -> {  //Fired when user input have no another arguments.
                            Source source = content.getSource(); //Get message source
                            MessageEvent me = source.getMessageEvent();//It's a message event.You have to cast it's type
                            if (me instanceof PrivateMessageReceivedEvent) {
                                PrivateMessageReceivedEvent pm = (PrivateMessageReceivedEvent) me;
                                pm.getContact().sendMessage("Hello! This is a direct message without any things!!");
                                pm.getContact().reply(me.getMessageID(), "This is a quote message!");
                            }
                            if (me instanceof GroupMessageReceivedEvent) {
                                GroupMessageReceivedEvent gm = (GroupMessageReceivedEvent) me;
                                gm.getGroup().sendMessage("This is a direct message!");
                                gm.getGroup().reply(me.getMessageID(), "This is a quote message!");
                            }
                            return 0;
                        })
                        .then(LiteralArgumentBuilder.<Source>literal("SUB_COMMAND") //A sub command (!p ROOT_COMMAND SUB_COMMAND)
                                .executes(content -> 0) //do nothing
                        )
                        .then(LiteralArgumentBuilder.<Source>literal("INPUT_NUMBER") // "ROOT_COMMAND INPUT_NUMBER"
                                .then(RequiredArgumentBuilder.<Source, Integer>argument("number", IntegerArgumentType.integer())  // "ROOT_COMMAND INPUT_NUMBER <number>"
                                        .executes(ctx -> {
                                            Core.getLogger().info("The number is {}", IntegerArgumentType.getInteger(ctx, "number")); // "The number is <number>"
                                            return 0;
                                        })
                                )
                        )
        );
        //Get a bot and then you can do many things.
        Core.get().bot("NAME").orElseThrow(NullPointerException::new);

    }

    /*
        Fired when server stopping.
     */
    @Subscribe
    public void onServerStop(ServerStoppingEvent event) {

    }
}
