# DovaToast
自定义Toast，解决系统Toast存在的问题

使用系统Toast的问题

    1.当通知权限被关闭时在华为等手机上Toast不显示；

    2.系统Toast的队列机制在不同手机上可能会不相同。

创建多个系统Toast展示时出现效果不同的对比机型：
 * 小米8-MIUI10（只看到展示第二个,因为新的Toast会将正在展示的Toast取消）、
 * 红米6pro-MIUI9（两个同时展示）、
 * 荣耀5C-android6.0（两个TOAST排队先后显示）、
 * 荣耀5C-android7.0（contentView不同时只看到展示第一个，相同时只看到展示第二个）

## TODO LIST:
    *考虑是否对Toast增加优先级属性，优先级高的Toast优先置于待处理队列的头部。