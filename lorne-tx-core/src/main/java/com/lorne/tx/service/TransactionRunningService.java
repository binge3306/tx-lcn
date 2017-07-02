package com.lorne.tx.service;

import com.lorne.core.framework.utils.task.Task;
import com.lorne.tx.service.model.ServiceThreadModel;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * Created by lorne on 2017/6/9.
 */
public interface TransactionRunningService {

    ServiceThreadModel serviceInThread(boolean signTask, String _groupId, Task task, ProceedingJoinPoint point);


    void serviceWait(boolean signTask, Task task, ServiceThreadModel model);
}
