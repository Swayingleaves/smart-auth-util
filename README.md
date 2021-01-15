<div align='center'><h1>权限认证工具</h1></div>

# 注意事项
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

# 使用说明

* 基于cookie的SSO验证，请求接口时cookie需带上key为SSO_TOKEN的token，例如：
![token说明](https://raw.githubusercontent.com/Swayingleaves/smart-auth-util/master/token说明.png)  (SSO_TOKEN的值为登录成功时接口返回的用户token；)

> @CheckLogin 检查登录

|  类别   | 说明  |
|  ----  | ----  |
| 适用于  | 类、方法 |
| 作用  | 验证请求用户是否登录 |
| 验证级别  | 1 |
```java
@RequestMapping("/test")
@CheckLogin
@RestController
public class TestController {
	......//
}
```

> @CheckGroup 检查组

|  类别   | 说明  |
|  ----  | ----  |
| 适用于  | 类、方法 |
| 作用  | 验证请求用户是否具有组权限 |
| 验证级别  | 1 |
```java
@RequestMapping("/test")
@CheckGroup(group = "auth",opt = AuthOpt.AND)
@RestController
public class TestController {
	......//
}
```

> @CheckGroup 检查组

|  类别   | 说明  |
|  ----  | ----  |
| 适用于  | 类、方法 |
| 作用  | 验证请求用户是否具有组权限 |
| 验证级别  | 1 |
| 注意  | 需controller层使用@CheckLogin注解 |

|  注解参数   | 说明  |
|  ----  | ----  |
| String[] group() default {}  | 指定组 |
| AuthOpt opt() default AuthOpt.AND  | 指定组间关系  默认OR  可选 AuthOpt.AND |

```java
@RequestMapping("/test")
@CheckGroup(group = "auth",opt = AuthOpt.AND)
@RestController
public class TestController {
	......//
}
```

> @CheckRole 检查角色

|  类别   | 说明  |
|  ----  | ----  |
| 适用于  | 方法 |
| 作用  | 验证请求用户是否拥有访问该方法的角色 |
| 验证级别  | 2 |
| 注意  | 需controller层使用@CheckLogin注解 |

|  注解参数   | 说明  |
|  ----  | ----  |
| String[] roles() default {}  | 指定角色 |
| AuthOpt opt() default AuthOpt.AND  | 指定关系  默认OR  可选 AuthOpt.AND |

```java
@RequestMapping("/test")
@CheckLogin
@RestController
public class TestController {

    @GetMapping("/queryMsg")
    @CheckRole(roles = {"admin","emp"},opt = AuthOpt.AND)
    public String queryMsg(){
        return "msg";
    }
    
    @GetMapping("/delMsg")
    @CheckRole(roles = {"admin","master"},opt = AuthOpt.OR)
    public String delMsg(){
        return "del";
    }
}
```

> @CheckPermission 检查权限

|  类别   | 说明  |
|  ----  | ----  |
| 适用于  | 方法 |
| 作用  | 验证请求用户是否拥有访问该方法的权限 |
| 验证级别  | 3 |
| 注意  | 需controller层使用@CheckLogin注解 |

|  注解参数   | 说明  |
|  ----  | ----  |
| String[] permissions() default {}  | 指定权限 |
| AuthOpt opt() default AuthOpt.AND  | 指定关系  默认OR  可选 AuthOpt.AND |

```java
@RequestMapping("/test")
@CheckLogin
@RestController
public class TestController {
 
    @GetMapping("/queryMsg")
    @CheckPermission(permissions = {"admin:query","master:add"},opt=AuthOpt.OR)
    public String queryMsg(){
        return "msg";
    }
 
    @GetMapping("/delMsg")
    @CheckPermission(permissions = {"admin:del","master:del"},opt = AuthOpt.AND)
    public String delMsg(){
        return "del";
    }
}
```

> @Limit 限流

|  类别   | 说明  |
|  ----  | ----  |
| 适用于  | 方法 |
| 作用  | 限制访问频率 |

|  注解参数   | 说明  |
|  ----  | ----  |
| int rate() default 1000;  | 速率 如限制1000次 |
| String methodType() default "M";  | 方法类型 |
| TimeUnit timeUnit() default TimeUnit.MINUTES;  | 时间 如TimeUnit.MINUTES 为分 则限制为 每分钟 1000次访问可选 |

```java
@RequestMapping("/test")
@CheckLogin
@RestController
public class TestController {

    @GetMapping("/queryMsg")
    @Limit(rate = 10,methodType = "QUERY_MSG",timeUnit = TimeUnit.MINUTES)
    public String queryMsg(){
        return "msg";
    }

    @GetMapping("/delMsg")
    @Limit(rate = 5,methodType = "DEL_MSG",timeUnit = TimeUnit.HOURS)
    public String delMsg(){
        return "del";
    }
}
```

> @Record 记录请求日志

|  类别   | 说明  |
|  ----  | ----  |
| 适用于  | 方法 |
| 作用  | 日志打印 访问方法的 ip,url,method,class,method name,params |

```java
@RequestMapping("/test")
@CheckLogin
@RestController
public class TestController {

    @GetMapping("/queryMsg")
    @Record
    public String queryMsg(){
        return "msg";
    }
}
```
```java
ip:[0:0:0:0:0:0:0:1],url:[http://localhost:8080/test/queryMsg],method:[GET],class:[com.bxtdata.authtest.controller.TestController],method name:[queryMsg],params:[[]]
```

# 异常说明

* 请自行在controller层捕捉异常并处理
```java
/**
 * @author zhenglin
 * @since  2019/6/18 11:11
 * @apiNote 统一异常处理
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 登录异常
     * @param e
     * @return
     */
    @ExceptionHandler(value = LoginException.class)
    public JSONObject exception(LoginException e){
        return Result.build(CodeEnum.NOT_LOGGED_IN.code, CodeEnum.NOT_LOGGED_IN.message);
    }

    /**
     * 非法请求
     * @param e
     * @return
     */
    @ExceptionHandler(value = IllegalRequestException.class)
    public JSONObject exception(IllegalRequestException e){
        return Result.build(CodeEnum.ILLEGAL_REQUESTS.code, CodeEnum.ILLEGAL_REQUESTS.message);
    }

    /**
     * 无权访问
     * @param e
     * @return
     */
    @ExceptionHandler(value = NoAuthorityException.class)
    public JSONObject exception(NoAuthorityException e){
        return Result.build(CodeEnum.NO_AUTHORITY.code, CodeEnum.NO_AUTHORITY.message,e.getMessage());
    }

    /**
     * 频率限制
     * @param e
     * @return
     */
    @ExceptionHandler(value = LimitException.class)
    public JSONObject limitException(LimitException e){
        return Result.build(CodeEnum.RATE_LIMIT.code, CodeEnum.RATE_LIMIT.message);
    }
}
```
