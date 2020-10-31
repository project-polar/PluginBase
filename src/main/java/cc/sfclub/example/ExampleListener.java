package cc.sfclub.example;

import cc.sfclub.events.MessageEvent;
import cc.sfclub.events.message.direct.PrivateMessage;
import cc.sfclub.events.message.group.GroupMessage;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ExampleListener {
    // 订阅注解标识。
    // 可使用ThreadMode指定在哪些线程上运行
    // BACKGROUND == 发布事件的线程
    // MAIN == 主线程
    // ASYNC == 线程池
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onGroupMessage(GroupMessage groupMessage) {
        //所有 Message 都是MessageEvent的子集
        //他们的命名末尾通常都有 Event，例如GroupMessageDeletedEvent
        //但 GroupMessage 和 PrivateMessage 是特例
        //他们的原名分别是：GroupMessageReceivedEvent 与 PrivateMessageReceivedEvent
        //实在是太长了，所以使用了GroupMessage和PrivateMessage作为名字。

        //MessageReceivedEvent都支持回复。如果使用了不支持回复的Event来回复会触发Unsupported异常
        //直接回复这条消息：
        groupMessage.reply("Hello world!");
        //回复消息ID：
        groupMessage.reply(0L, "Hello world!");
        //要注意的是，PolarCore并不会分配消息ID。请注意区分不同平台之间的消息
        groupMessage.getTransform(); //获取来源平台

        groupMessage.getGroupId(); //获取群组ID
        groupMessage.getUserID(); //获取PolarCore UID
        groupMessage.getMessage(); //获取消息
    }

    @Subscribe
    public void onPrivateMessage(PrivateMessage pm) {
        pm.getContact(); //获取联系人对象
        pm.reply(""); //回复
    }

    //Sticky Event:
    //当事件与该类有继承关系的时候也会触发
    @Subscribe(sticky = true)
    public void onAllMessage(MessageEvent me) {

    }
}
