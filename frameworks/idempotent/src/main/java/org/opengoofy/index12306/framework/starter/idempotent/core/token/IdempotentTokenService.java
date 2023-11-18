package org.opengoofy.index12306.framework.starter.idempotent.core.token;

import org.opengoofy.index12306.framework.starter.idempotent.core.IdempotentExecuteHandler;

/**
 * Token 实现幂等接口
 */
public interface IdempotentTokenService extends IdempotentExecuteHandler {

    /**
     * 创建幂等验证Token
     */
    String createToken();
}
