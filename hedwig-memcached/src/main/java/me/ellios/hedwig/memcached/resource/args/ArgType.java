package me.ellios.hedwig.memcached.resource.args;

import me.ellios.hedwig.memcached.resource.args.parser.*;

/**
 * args type.
 *
 * @author gaofeng@qiyi.com
 * @since: 14-3-20
 */
public enum ArgType {
    Byte("byte", new ByteParser()),  //NL
    Short("short", new ShortParser()), //NL
    Integer("int", new IntParser()), //NL
    Long("long", new LongParser()),  //NL
    Float("float", new FloatParser()), //NL
    Double("double", new DoubleParser()), //NL
    String("String", new StringParser()), //NL
    Boolean("boolean", new BooleanParser()), //NL
    Character("char", new CharParser());

    private final String shortName;
    private final ArgParser parse;

    ArgType(java.lang.String shortName, ArgParser parse) {
        this.shortName = shortName;
        this.parse = parse;
    }

    public Object parseValue(String value) {
        return parse.parse(value);
    }

    public static ArgType getArgType(String shortName) {
        if (Integer.name().equals(shortName) || Integer.shortName.equals(shortName)) {
            return Integer;
        }
        if (String.name().equals(shortName) || String.shortName.equals(shortName)) {
            return String;
        }
        if (Boolean.name().equals(shortName) || Boolean.shortName.equals(shortName)) {
            return Boolean;
        }
        if (Long.name().equals(shortName) || Long.shortName.equals(shortName)) {
            return Long;
        }
        if (Float.name().equals(shortName) || Float.shortName.equals(shortName)) {
            return Float;
        }
        if (Double.name().equals(shortName) || Double.shortName.equals(shortName)) {
            return Double;
        }
        if (Byte.name().equals(shortName) || Byte.shortName.equals(shortName)) {
            return Byte;
        }
        if (Short.name().equals(shortName) || Short.shortName.equals(shortName)) {
            return Short;
        }
        if (Character.name().equals(shortName) || Character.shortName.equals(shortName)) {
            return Character;
        }
        return null;
    }
}
