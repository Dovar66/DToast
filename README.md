# DovaToast
自定义Toast，解决系统Toast存在的问题

使用系统Toast的问题

    1.当通知权限被关闭时在华为等手机上Toast不显示；（本项目已解决）

    2.系统Toast的队列机制在不同手机上可能会不相同；（本项目已解决）

    3.系统Toast的BadTokenException问题；（本项目已解决）

    4.Android7.1之后的token null is not valid问题。(捕获处理)

## 问题一：关闭通知权限时Toast不显示

    看下方Toast源码中的show()方法，通过AIDL获取到INotificationManager，并将接下来的显示流程控制权交给NMS。
    NMS中会对Toast进行权限校验、token校验，当通知权限校验不通过时，Toast将不做展示。当然不同ROM中NMS可能会有不同，
    比如MIUI就对这部分内容进行了修改，所以小米手机关闭通知权限不会导致Toast不显示。

      /**
         * Show the view for the specified duration.
         */
        public void show() {
            if (mNextView == null) {
                throw new RuntimeException("setView must have been called");
            }

            INotificationManager service = getService();
            String pkg = mContext.getOpPackageName();
            TN tn = mTN;
            tn.mNextView = mNextView;

            try {
                service.enqueueToast(pkg, tn, mDuration);
            } catch (RemoteException e) {
                // Empty
            }
        }

如何解决这个问题？只要能够绕过NotificationManagerService即可。
    DovaToast通过使用TYPE_TOAST实现Toast功能，内部维护展示队列，并没有使用NMS服务，因此不受通知权限限制。

### 问题二：系统Toast的队列机制在不同手机上可能会不相同

     创建多个系统Toast展示时出现效果不同的对比机型：

            * 小米8-MIUI10（只看到展示第二个,因为新的Toast会将正在展示的Toast取消）、
            * 红米6pro-MIUI9（两个同时展示）、
            * 荣耀5C-android6.0（两个TOAST排队先后显示）、
            * 荣耀5C-android7.0（contentView不同时只看到展示第一个，相同时只看到展示第二个）

     造成这个问题的原因应该是各大ROM中NMS维护Toast队列的逻辑有差异。DovaToast维护一套队列逻辑，保证在所有手机上效果相同。

## 问题三：Toast的BadTokenException问题

Toast有个内部类（TN extends ITransientNotification.Stub），调用Toast.show()会将TN传递给NMS，在NMS中会生成一个windowToken，并将windowToken传给wms，wms会暂时保存该token并用于之后的校验，
然后NMS通过调用TN.show(windowToken)传递token给TN，TN使用该token尝试向WindowManager中添加Toast视图(mParams.token = windowToken)。

            /**
             * schedule handleShow into the right thread
             */
            @Override
            public void show(IBinder windowToken) {
                if (localLOGV) Log.v(TAG, "SHOW: " + this);
                mHandler.obtainMessage(SHOW, windowToken).sendToTarget();
            }

WindowManager收到addView请求后会检查当前窗口的token是否有效，若有效则添加窗口展示Toast，否则抛出BadTokenException异常.

什么情况下windowToken会失效？

    UI线程发生阻塞，导致TN.show()没有及时执行，当NotificationManager的检测超时后便会删除WMS中的该token，即造成token失效。

如何解决？

    Google在api26中修复了这个问题，即增加了try-catch:

                // Since the notification manager service cancels the token right
                // after it notifies us to cancel the toast there is an inherent
                // race and we may attempt to add a window after the token has been
                // invalidated. Let us hedge against that.
                try {
                    mWM.addView(mView, mParams);
                    trySendAccessibilityEvent();
                } catch (WindowManager.BadTokenException e) {
                    /* ignore */
                }

因此对于8.0之前的我们也需要做相同的处理。DovaToast是通过反射完成这个动作，具体看下方实现：

      //捕获8.0之前Toast的BadTokenException，Google在Android 8.0的代码提交中修复了这个问题
         private void hook(Toast toast) {
             try {
                 Field sField_TN = Toast.class.getDeclaredField("mTN");
                 sField_TN.setAccessible(true);
                 Field sField_TN_Handler = sField_TN.getType().getDeclaredField("mHandler");
                 sField_TN_Handler.setAccessible(true);

                 Object tn = sField_TN.get(toast);
                 Handler preHandler = (Handler) sField_TN_Handler.get(tn);
                 sField_TN_Handler.set(tn, new SafelyHandlerWrapper(preHandler));
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }


         public class SafelyHandlerWrapper extends Handler {
             private Handler impl;

             public SafelyHandlerWrapper(Handler impl) {
                 this.impl = impl;
             }

             @Override
             public void dispatchMessage(Message msg) {
                 try {
                     impl.dispatchMessage(msg);
                 } catch (Exception e) {
                 }
             }

             @Override
             public void handleMessage(Message msg) {
                 impl.handleMessage(msg);//需要委托给原Handler执行
             }
         }
## 问题四：Android7.1之后的token null is not valid问题
    Android从7.1开始，Google对WindowManager做了一些限制和修改，特别是TYPE_TOAST类型的窗口，必须要传递一个token用于权限校验才允许添加。
    在API25的源码中，Toast的WindowManager.LayoutParams参数新增了一个token属性，它是在NMS中被初始化的，用于对添加的窗口类型进行校验

                    switch (res) {
                        case WindowManagerGlobal.ADD_BAD_APP_TOKEN:
                        case WindowManagerGlobal.ADD_BAD_SUBWINDOW_TOKEN:
                            throw new WindowManager.BadTokenException(
                                    "Unable to add window -- token " + attrs.token
                                    + " is not valid; is your activity running?");
                        case WindowManagerGlobal.ADD_NOT_APP_TOKEN:
                            throw new WindowManager.BadTokenException(
                                    "Unable to add window -- token " + attrs.token
                                    + " is not for an application");
                        case WindowManagerGlobal.ADD_APP_EXITING:
                            throw new WindowManager.BadTokenException(
                                    "Unable to add window -- app for token " + attrs.token
                                    + " is exiting");
                        case WindowManagerGlobal.ADD_DUPLICATE_ADD:
                            throw new WindowManager.BadTokenException(
                                    "Unable to add window -- window " + mWindow
                                    + " has already been added");
                        case WindowManagerGlobal.ADD_STARTING_NOT_NEEDED:
                            // Silently ignore -- we would have just removed it
                            // right away, anyway.
                            return;
                        case WindowManagerGlobal.ADD_MULTIPLE_SINGLETON:
                            throw new WindowManager.BadTokenException("Unable to add window "
                                    + mWindow + " -- another window of type "
                                    + mWindowAttributes.type + " already exists");
                        case WindowManagerGlobal.ADD_PERMISSION_DENIED:
                            throw new WindowManager.BadTokenException("Unable to add window "
                                    + mWindow + " -- permission denied for window type "
                                    + mWindowAttributes.type);
                        case WindowManagerGlobal.ADD_INVALID_DISPLAY:
                            throw new WindowManager.InvalidDisplayException("Unable to add window "
                                    + mWindow + " -- the specified display can not be found");
                        case WindowManagerGlobal.ADD_INVALID_TYPE:
                            throw new WindowManager.InvalidDisplayException("Unable to add window "
                                    + mWindow + " -- the specified window type "
                                    + mWindowAttributes.type + " is not valid");
                    }

为了解决前面三个问题，DovaToast不得不选择绕过NotificationManagerService的控制，但由于windowToken是NMS生成的，绕过NMS就无法获取到有效的windowToken，
于是就掉进第四个问题里了。除了去获取悬浮窗权限，改用TYPE_PHONE等类型，我暂时还没有找到其他更好的解决方法。但悬浮窗权限又不容易获取，
所以目前DovaToast用了另外一种较为勉强的方案：在捕获到token null is not valid异常时，改用系统Toast去展示。

## TODO LIST:

    *考虑是否对Toast增加优先级属性，优先级高的Toast优先置于待处理队列的头部。