# 基于SpringCloud的分布式事务框架(LCN)

  该框架依赖Redis／SpringCloud／[TxManager](https://github.com/1991wangliang/springcloud-tx-manager)服务。依赖第三方框架[lorne_core](https://github.com/1991wangliang/lorne_core)
  
## 原理与功能
  基于对spring tx PlatformTransactionManager的本地模块事务控制从而达到全局控制事务的目的。该框架兼容任何依赖PlatformTransactionManager的DB框架。利用"新三阶段事务"的方式来确保事务的一致性，支持本地事务和分布式事务框架共存，当方法进入的是本地事务方法，框架将不做任何分布式事务处理。当需要用到分布式事务的时候只需要在方法上添加分布式事务的注解即可。框架由于基于Spring本地事务做的封装，基本支持依赖spring的所有db框架。并在帖子底部提供了对spring-jdbc／spring-jpa／mybatis的演示demo。
  
##### 该框架属于强事务一致性框架。
  
  该框架在设计时就考虑到大型分布式的应用场景，因此框架支持对于SpringCloud单个模块的集群化。并且TxManager也支持集群化。
  
框架基于"新三阶段事务"：
  
1. 锁定事务单元
2. 确认事务模块状态
3. 通知事务
   

关于LCN框架的详细设计请见[TxManager](https://github.com/1991wangliang/springcloud-tx-manager)说明

## 演示说明

### SpringCloud消费者调用方
 

```$xslt
    @Override
    @TxTransaction
    public int save() {

        int rs2 = demo2Client.save();//远程调用方
        
        int rs1 = testDao.save();


        return rs1+rs2;
    }

```

### SpringCloud服务者提供方

```$xslt
    @Override
    @Transactional
    public int save() {

        int rs = testDao.save();

        return rs;
    }

```


   
## 框架使用教程
##### 需要先部署redis服务。  
##### 部署[TxManager](https://github.com/1991wangliang/springcloud-tx-manager)全局事务协调管理器。  
##### 本地项目依赖[springcloud-transaction](https://github.com/1991wangliang/springcloud-transaction)库.  

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
             	<artifactId>springcloud-transaction</artifactId>
             	<version>1.0.0.RELEASE</version>
        </dependency> 
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
[springcloud-jdbc-demo1](https://github.com/1991wangliang/springcloud-jdbc-demo1) [springcloud-jdbc-demo2](https://github.com/1991wangliang/springcloud-jdbc-demo2)   
springcloud-jdbc-demo1是发起方，springcloud-jdbc-demo2是被调用方。

spring-jpa版本：
[springcloud-jpa-demo1](https://github.com/1991wangliang/springcloud-jpa-demo1) [springcloud-jpa-demo2](https://github.com/1991wangliang/springcloud-jpa-demo2)   
springcloud-jpa-demo1是发起方，springcloud-jpa-demo2是被调用方。

spring-mybatis版本：
[springcloud-mybatis-demo1](https://github.com/1991wangliang/springcloud-mybatis-demo1) [springcloud-mybatis-demo2](https://github.com/1991wangliang/springcloud-mybatis-demo2)   
springcloud-mybatis-demo1是发起方，springcloud-mybatis-demo2是被调用方。

QQ交流群：554855843
