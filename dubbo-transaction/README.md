# 基于Dubbo的分布式事务框架(LCN)

  该框架依赖Redis／dubbo／[txManager](https://github.com/1991wangliang/txManager)服务。依赖第三方框架[lorne_core](https://github.com/1991wangliang/lorne_core)
  
## 原理与功能
  基于对spring tx PlatformTransactionManager的本地模块事务控制从而达到全局控制事务的目的。该框架兼容任何依赖PlatformTransactionManager的DB框架。利用"新三阶段事务"的方式来确保事务的一致性，支持本地事务和分布式事务框架共存，当方法进入的是本地事务方法，框架将不做任何分布式事务处理。当需要用到分布式事务的时候只需要在方法上添加分布式事务的注解即可。框架由于基于Spring本地事务做的封装，基本支持依赖spring的所有db框架。并在帖子底部提供了对springjdbc／hibernate／mybatis的演示demo。
  
##### 该框架属于强事务一致性框架。
  
  该框架在设计时就考虑到大型分布式的应用场景，因此框架支持对于dubbo单个模块的集群化。并且TxManager也支持集群化。
  
框架基于"新三阶段事务"：
  
1. 锁定事务单元
2. 确认事务模块状态
3. 通知事务
   

关于LCN框架的详细设计请见[txManager](https://github.com/1991wangliang/txManager)说明

## 演示说明

### dubbo消费者调用方
 

```$xslt
@Service
public class TestServiceImpl implements TestService {


    @Autowired
    private TestDao testDao;//本地db层

    @Autowired
    private Test2Service test2Service;//dubbo服务方业务类


    @Override
    @TxTransaction  //分布式事务注解
    public String hello() {

        String name = "hello_demo1";
        testDao.save(name);//执行本地db插入数据操作

        String res =  test2Service.test();//调用远程db插入数据库操作

        int v = 100/0;//模拟异常操作。该异常会回滚本地和远程的db事务操作
        return res;
    }

}

```

### dubbo服务者提供方

```$xslt
@Service
public class Test2ServiceImpl extends MQTransactionServiceImpl implements Test2Service {


    @Autowired
    private TestDao testDao;//本地db类



    @Override
    public String test() {

        String name = "hello_demo2";

        testDao.save(name);//本地db插入数据操作


        return name;

    }
}

```


   
## 框架使用教程
##### 需要先部署redis服务。  
##### 部署[TxManager](https://github.com/1991wangliang/txManager)全局事务协调管理器。  
##### 本地项目依赖[transaction](https://github.com/1991wangliang/transaction)库.  

maven仓库地址

```$xslt
    <repositories>
        <repository>
            <id>lorne</id>
            <url>https://1991wangliang.github.io/repository</url>
        </repository>
    </repositories>
```
maven transaction 配置

``` 
        <dependency>
               <groupId>com.lorne.tx</groupId>
               <artifactId>transaction</artifactId>
               <version>1.0.0.RELEASE</version>
        </dependency> 
```
##### 配置dubbo服务
```
    <dubbo:application name="tx-transaction-test"   />

    <!--所有参与分布式事务的模块以及TxManager都必须要在同一个服务下-->
    <dubbo:registry protocol="zookeeper" address="127.0.0.1:2181" />

    <!--依赖TxManager服务-->
    <dubbo:reference timeout="3000" interface="com.lorne.tx.mq.service.MQTxManagerService" id="managerService" />

    <dubbo:protocol accesslog="true" name="dubbo" port="20882" />

    <!--所有需要分布式事务的模块也都必须对外提供服务-->

    <!--1. 用户自定义的服务-->
    <dubbo:service interface="com.demo.service.MQTestService" ref="testService"  />
    <bean id="testService" class="com.demo.service.impl.MQTestServiceImpl"   />

    <!--2. 当用户没有需要对外提供的服务时-->
    <!--<dubbo:service interface="com.lorne.tx.mq.service.MQTransactionService" ref="transactionService"  />-->
    <!--<bean id="transactionService" class="com.lorne.tx.mq.service.impl.MQTransactionServiceImpl"   />-->
    
```         
若用户是自定义的服务，则服务必须要实现MQTransactionService接口如下：
```$xslt

public interface MQTestService extends MQTransactionService{

    String test(String name);
}

```
MQTransactionService的实现:第一种方式
```$xslt

@Service
public class MQTestServiceImpl extends MQTransactionServiceImpl implements MQTestService {

    
    @Override
    public String test(String name) {
       //todo 用户业务处理 
       return "";
    }
}

```
MQTransactionService的实现:第二种方式
```$xslt

@Service
public class MQTestServiceImpl implements MQTestService {


    @Autowired
    private MQTransactionService transactionService;
    
    @Override
    public boolean notify(String kid, boolean state) {
        return transactionService.notify(kid, state);
    }

    @Override
    public boolean checkRollback(String kid) {
        return transactionService.checkRollback(kid);
    }

    @Override
    public String test(String name) {
       //todo 用户业务处理 
       return "";
    }
}

```

##### 分布式事务的切面配置
  
```$xslt

    <!--本地事务manager  -->
    <bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
        <property name="dataSource" ref="dataSource"/>
    </bean>

    <!--本地事务切面 -->
    <tx:annotation-driven transaction-manager="transactionManager"/>

    <!--切面 advice 定义-->
    <tx:advice id="txAdvice">
        <tx:attributes>
            <tx:method name="get*" read-only="true"  />
            <tx:method name="find*" read-only="true"/>
            <tx:method name="load*" read-only="true"/>
            <tx:method name="query*" read-only="true"/>
            <tx:method name="select*" read-only="true"/>
            <tx:method name="*" rollback-for="com.le.core.framework.exception.LEException"/>
        </tx:attributes>
    </tx:advice>

    <!--分布式事务拦截器-->
    <bean id="txTransactionInterceptor" class="com.lorne.tx.interceptor.TxManagerInterceptor"/>


    <aop:config>
        <aop:pointcut id="allManagerMethod" expression="execution(* com.**.service.impl.*Impl.*(..))"/>
        <!--本地事务拦截-->
        <aop:advisor  order="100" advice-ref="txAdvice" pointcut-ref="allManagerMethod"/>
        <!--分布式事务拦截-->
        <aop:advisor  order="10" advice-ref="txTransactionInterceptor" pointcut-ref="allManagerMethod"/>
    </aop:config>

```

##### 分布式事务注解(@TxTransaction)
```$xslt
    @Override
    @TxTransaction
    public String test() {

        //todo 业务处理

        return "";

    }
```
关于@TxTransaction的补充说明：
当添加事务注解时方法将开启分布式事务处理方式。当尽当开始方法是分布式事务方法时才进入分布式事务处理逻辑。
若存在业务方法A调用了业务方法B，当分布式事务注解添加在A上，那么整个A方法将被分布式事务所管理，若注解添加在B上，当调用A时将不会被启用分布式事务，尽当业务启动时的方法添加分布式事务注解时方可开启分布式事务注解。


### 演示demo：  
spring-jdbc版本：
[transaction_demo1](https://github.com/1991wangliang/transaction_demo1) [transaction_demo2](https://github.com/1991wangliang/transaction_demo2)   
transaction_demo1是发起方，transaction_demo2是被调用方。

hibernate版本：  
[transaction_hibernate_demo1](https://github.com/1991wangliang/transaction_hibernate_demo1) [transaction_hibernate_demo2](https://github.com/1991wangliang/transaction_hibernate_demo2)   
transaction_hibernate_demo1是发起方，transaction_hibernate_demo2是被调用方。

mybatis版本：  
[transaction_mybatis_demo1](https://github.com/1991wangliang/transaction_mybatis_demo1) [transaction_mybatis_demo2](https://github.com/1991wangliang/transaction_mybatis_demo2)   
transaction_mybatis_demo1是发起方，transaction_mybatis_demo2是被调用方。

QQ交流群：554855843
