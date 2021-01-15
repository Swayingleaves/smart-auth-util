/*
 * @Copyright(2018 - 2019):
 *   All software developed at bxtdata should be copyright protected and
 *   thereby bear a clear, standardized copyright notice along with a
 *   disclaimer notice. This copyright and disclaimer notice protects the
 *   software author(s) and the bxtdata from any liability that might result,
 *   however remote, from the use of the software. Accordingly, the use of
 *   this notice is especially not applicable to software made available for use
 *   beyond the author(s), and includes software distributed as "freeware"
 *   or open source via computer networks.
 */

package com.swayingleaves.smartauthutil.code;

/**
 * @author : zhenglin
 * @since : 2019/5/27 12:00
 * @apiNote : 静态变量
 */
public class Const {
    /**
     * 单点登录cookie中的sso值名
     */
    public static final String SSO_TOKEN = "SSO_TOKEN";
    /**
     * 登录过期时间(分钟)
     */
    public static final int LOGIN_TIME_OFF = 40;

    public static final String SMART_AUTH = "SMART_AUTH:";
    public static final String LOGIN_USER = SMART_AUTH+"LOGIN_USER:";
    public static final String SU_LOGIN_USER_ID = LOGIN_USER+"ID:";
    public static final String SU_LOGIN_USER_IP = LOGIN_USER+"IP:";
    public static final String SU_LOGIN_USER_TOKEN = LOGIN_USER+"TOKEN:";


    public static final String IP_LIMIT_DIR_NAME = "SMART_AUTH:IP_LIMIT:";

    public static final String AND = "AND";
    public static final String OR = "OR";

}
