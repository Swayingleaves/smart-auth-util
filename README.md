* 配置本地maven 以适配私服，或者修改为自己的私服配置

* pom引入依赖

```xml
<dependency>
    <groupId>com.swayingleaves</groupId>
    <artifactId>smart-auth-util</artifactId>
    <version>${latest-version}</version>
</dependency>
```

* 要使用auth-util,使用的项目需要的Redis配置相同
* 实现强依赖Redis，请配置Redis相关
```yaml
spring:
  redis:
    host: 127.0.0.1
    port: 6379
    database: xx
    password: xxxx
```

* 启动类添加扫描包注解
```java
@ComponentScan(basePackages = {"com.swayingleaves"})
```
* 不一定能适用于大家的项目，但是看下实现逻辑就好