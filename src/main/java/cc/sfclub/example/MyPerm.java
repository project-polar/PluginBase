package cc.sfclub.example;

import cc.sfclub.user.User;
import cc.sfclub.user.perm.Perm;

public class MyPerm extends Perm {
    @Override
    public Result hasPermission(User user, Result regexResult) {
        if (user.getUserName() == "LxnsNB") {
            return Result.SUCCEED;
        }
        return Result.FAILED; //Result.BANNED 表示被禁止使用这个权限
    }

    @Override
    public String toString() {
        return "my.perm.node";
    }
}
