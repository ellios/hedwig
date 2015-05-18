package me.ellios.hedwig.common.config;

import org.apache.commons.lang3.StringUtils;

/**
 * 运行环境
 * Author: ellios
 * Date: 12-10-29 Time: 下午3:51
 */
public enum HedwigEnv {

    PRODUCTION("prod"),
    TEST("test"),
    DEVELOPMENT("dev");

    private String abbreviation;

    private HedwigEnv(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    /**
     * 通过缩写名获取环境，默认是开发环境
     *
     * @param abbreviation
     * @return
     */
    public static HedwigEnv getEnvByAbbreviation(String abbreviation) {
        if (StringUtils.isEmpty(abbreviation)) {
            return DEVELOPMENT;
        }
        for (HedwigEnv env : HedwigEnv.values()) {
            if (StringUtils.equalsIgnoreCase(env.getAbbreviation(), abbreviation)) {
                return env;
            }
        }
        return DEVELOPMENT;
    }
}
