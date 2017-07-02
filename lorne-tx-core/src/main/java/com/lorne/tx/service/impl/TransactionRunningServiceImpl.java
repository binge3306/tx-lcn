package com.lorne.tx.service.impl;

import com.lorne.core.framework.Constant;
import com.lorne.core.framework.exception.ServiceException;
import com.lorne.core.framework.utils.KidUtils;
import com.lorne.core.framework.utils.task.ConditionUtils;
import com.lorne.core.framework.utils.task.IBack;
import com.lorne.core.framework.utils.task.Task;
import com.lorne.tx.mq.model.TxGroup;
import com.lorne.tx.mq.service.MQTxManagerService;
import com.lorne.tx.mq.service.NettyService;
import com.lorne.tx.service.TransactionRunningService;
import com.lorne.tx.service.model.ServiceThreadModel;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.concurrent.TimeUnit;

/**
 * Created by lorne on 2017/6/9.
 */
@Service
public class TransactionRunningServiceImpl implements TransactionRunningService {

    @Autowired
    private PlatformTransactionManager txManager;

    @Autowired
    private MQTxManagerService txManagerService;

    @Autowired
    private NettyService nettyService;


    private Logger logger = LoggerFactory.getLogger(TransactionRunningServiceImpl.class);

    @Override
    public ServiceThreadModel serviceInThread(boolean signTask, String _groupId, Task task, ProceedingJoinPoint point) {

        String kid = KidUtils.generateShortUuid();
        TxGroup txGroup = txManagerService.addTransactionGroup(_groupId, kid);

        //获取不到模块信息重新连接，本次事务异常返回数据.
        if (txGroup == null) {
            task.setBack(new IBack() {
                @Override
                public Object doing(Object... objects) throws Throwable {
                    throw new ServiceException("添加事务组异常.");
                }
            });
            task.signalTask();
            nettyService.restart();
            return null;
        }

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);
        TransactionStatus status = txManager.getTransaction(def);

        Task waitTask = ConditionUtils.getInstance().createTask(kid);

        try {
            final Object res = point.proceed();
            task.setBack(new IBack() {
                @Override
                public Object doing(Object... objects) throws Throwable {
                    return res;
                }
            });
            //通知TxManager调用成功
            txManagerService.notifyTransactionInfo(_groupId, kid, true);
        } catch (final Throwable throwable) {
            task.setBack(new IBack() {
                @Override
                public Object doing(Object... objects) throws Throwable {
                    throw throwable;
                }
            });
            //通知TxManager调用失败
            txManagerService.notifyTransactionInfo(_groupId, kid, false);
        }

        if (signTask)
            task.signalTask();


        ServiceThreadModel model = new ServiceThreadModel();
        model.setStatus(status);
        model.setWaitTask(waitTask);
        model.setTxGroup(txGroup);

        return model;

    }


    @Override
    public void serviceWait(boolean signTask, Task task, final ServiceThreadModel model) {
        Task waitTask = model.getWaitTask();
        final String taskId = waitTask.getKey();
        TransactionStatus status = model.getStatus();

        Constant.scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                Task task = ConditionUtils.getInstance().getTask(taskId);
                if (task.getState() == 0) {
                    task.setBack(new IBack() {
                        @Override
                        public Object doing(Object... objects) throws Throwable {
                            return 0;
                        }
                    });
                    logger.info("自定回滚执行");
                    task.signalTask();
                }
            }
        }, model.getTxGroup().getWaitTime(), TimeUnit.SECONDS);

        if (!signTask) {
            txManagerService.closeTransactionGroup(model.getTxGroup().getGroupId());
        }
        logger.info("进入回滚等待.");
        waitTask.awaitTask();

        try {
            int state = (Integer) waitTask.getBack().doing();
            logger.info("waitTask:" + state);
            if (state == 1) {
                txManager.commit(status);
            } else {
                txManager.rollback(status);
                if (state == -1) {
                    task.setBack(new IBack() {
                        @Override
                        public Object doing(Object... objs) throws Throwable {
                            throw new Throwable("事务模块网络异常.");
                        }
                    });
                }
            }
            if (!signTask) {
                task.signalTask();
            }
        } catch (Throwable throwable) {
            txManager.rollback(status);
        } finally {
            waitTask.remove();
        }
    }


}
