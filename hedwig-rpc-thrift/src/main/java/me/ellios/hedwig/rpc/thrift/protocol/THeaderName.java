package me.ellios.hedwig.rpc.thrift.protocol;

/**
 * Header name.
 *
 * @author George Cao
 * @since 2014-01-13 16
 */
public enum THeaderName {
    API(0),
    SERVICE(1),
    USER(2);
    int index;

    THeaderName(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    private static THeaderName findByIndex0(int index) {
        THeaderName[] names = THeaderName.values();
        for (THeaderName name : names) {
            if (name.getIndex() == index) {
                return name;
            }
        }
        return null;
    }

    public static THeaderName findByIndex(int index) {
        switch (index) {
            case 0:
                return API;
            case 1:
                return SERVICE;
            case 2:
                return USER;
            default:
                return findByIndex0(index);
        }
    }
}
