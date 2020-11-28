package cc.sfclub.example;

import cc.sfclub.catcode.entities.At;
import cc.sfclub.catcode.entities.Image;
import cc.sfclub.catcode.entities.Plain;
import cc.sfclub.command.Source;
import cc.sfclub.core.Core;
import cc.sfclub.events.Event;
import cc.sfclub.events.server.ServerStartedEvent;
import cc.sfclub.plugin.Plugin;
import cc.sfclub.plugin.SimpleConfig;
import cc.sfclub.user.User;
import cc.sfclub.user.UserManager;
import cc.sfclub.user.perm.Perm;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import org.greenrobot.eventbus.Subscribe;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;

/*
     Plugin main class,it should extend Plugin.class.About specify Main class,see build.gradle
 */
public class Main extends Plugin {
    /*
        服务器完全加载完毕后触发
        在plugin.json中配置autoRegister后才有用。
     */
    @Subscribe
    public void onServerStart(ServerStartedEvent event) {
        //以下是插件常用操作介绍。
        // 1.持久化
        // 若在plugin.json内指定了`dataClass`项，polar将会自动初始化这个数据类。
        // 通过以下方法获取(此情况下需要强转):
        getConfig().get();
        // 自己加载一个 SimpleConfig:
        SimpleConfig<OurData> ourSimpleCfg = new SimpleConfig<>(this, OurData.class); //注意:SimpleConfig默认序列化为json
        ourSimpleCfg.saveDefault(); //自动创建默认配置，如果配置文件不存在
        ourSimpleCfg.saveConfig(); //直接**覆盖保存配置**
        ourSimpleCfg.setConfigFileName("data.json"); //设置保存文件名
        ourSimpleCfg.set(new OurData()); //替换掉内部的OurData对象
        getDataFolder(); //获取数据文件夹
        // 2.监听事件
        // 通过 Event 操作事件相关
        //Event.registerListeners(Object... object);
        Event.registerListeners(new ExampleListener());
        // 发布事件: Event.postEvent(XX);
        // 注意:广播消息请使用 Event.broadcastMessage();
        // 区别在于后者会经过PolarSec的安全审计，前者不会。
        // 事件订阅以及类型详见ExampleListener.java

        //3. 说话
        getLogger().info("Hello!");
        getLogger().warn("{}", "Hello!");

        //4. 注册命令
        // 基本树形结构和过滤器
        registerCommand(
                LiteralArgumentBuilder.<Source>literal("command_name")
                        .executes(s -> { //Command<S>，可以写单独的类处理命令也可以Lambda
                            Source source = s.getSource();
                            source.getSender(); //发送者
                            source.getMessageEvent(); //信息事件，可直接回复，用instanceof判定事件来源
                            source.getTime(); //发送时间
                            return 0;//是否成功
                        }).then(
                        LiteralArgumentBuilder.<Source>literal("sub_cmd") //command_name sub_cmd
                                .requires(e -> e.getSender().hasPermission(Perm.of("do.sub_cmd"))) //requires: 检查条件是否通过
                                //关于fork redirect请自行查阅brigadier文档
                                .executes(s -> { //然后执行..
                                    return 0;
                                }).then(LiteralArgumentBuilder.<Source>literal("a")) //command_name sub_cmd_a
                )
                        .then(LiteralArgumentBuilder.<Source>literal("sub_cmd_2")) //command_name sub_cmd_2
        );
        //接下来是接收参数的命令
        registerCommand(
                LiteralArgumentBuilder.<Source>literal("killpid")
                        .then(
                                RequiredArgumentBuilder.<Source, Integer>argument("pid", integer())
                                        .executes(c -> {
                                            c.getArgument("pid", Integer.class); //获取 pid
                                            return 0;
                                        })
                        )
        );

        //5. 用户，权限
        // 获取UserManager:
        UserManager um = Core.get().userManager();
        um.byUUID("...."); //通过UUID查询用户
        um.byName("username"); //用户名
        um.byPlatformID("qq", "2114623054"); //通过平台信息查询
        // 注册:
        um.register("xxgroup", Perm.of("...")); //注册一个用户，让他进入xxgroup并赋予初始权限
        um.register("xxgroup", "qq", "2114623054"); //注册一个用户，进入xxgroup并且和QQ:2114623054建立绑定
        um.registerGroup("group_name"); //创建一个叫group_name的权限组，不赋予初始权限。
        // 更新
        // 更新用户有两种方法
        User u = new User();
        //update / save
        um.update(u); //注意：User的无参构造器标注了 @Internal 注解，带有该注解的所有方法和字段产生的问题自负。
        // 权限判断
        u.hasPermission(Perm.of("xxx")); //判断是否带有XXX权限..
        u.getUniqueID();// 获取UUID
        u.addPermission(Perm.of("aaa")); //添加权限，如果原本没有
        u.asFormattedName(); //格式化为人类可读名称
        // 若有用户名: 用户名(UUID)
        // 若无: UUID
        // 更多请查阅Javadoc。

        // 权限
        // 权限使用Perm.of("XX")得到一个权限节点为XX的纯字符串权限。
        // 纯字符串权限指的是，只匹配权限节点是否通过而不进行而次匹配
        // 用户可以通过继承 Perm 并重写 hasPermission(User,Result) 做到自定义权限判断器，第二个参数是权限节点判断结果。
        // 接着这样注册一个权限:
        Perm.register(new MyPerm());
        //注意: Perm.of内部是用的是 WeakHashMap ，这意味着你注册的PermObj若无强引用有可能会被GC

        //6. 在外部获取到插件
        Main.get(Main.class);
        // 等价
        Plugin.get(Main.class); // 所以不要给你的插件主类名取Main.

        //7. 获取数据库对象
        Core.get().ORM();

        //8. Bot
        Core.get().bot("transformer name"); //获取一个Bot
        // 更多Bot相关，请参考MiraiAdapter( https://github.com/project-polar/MiraiAdapter ).

        //9. 猫码
        //猫码就是特殊消息，每一种猫码都具有对应的class和Builder.
        Image.builder();
        At.builder();
        Plain.builder();

    }

    // 在此处存放插件被加载时的初始化逻辑，onEnable优先于onServerStart。
    @Override
    public void onEnable() {

    }

    // 请在这里处理插件卸载相关
    @Override
    public void onDisable() {

    }
}
