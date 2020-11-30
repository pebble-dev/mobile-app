package com.getpebble.android.kit.util;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A key-value pair stored in a {@link PebbleDictionary}.
 *
 * @author zulak@getpebble.com
 */
public class PebbleTuple {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    static final Map<String, TupleType> TYPE_NAMES = new HashMap<String, TupleType>();

    static {
        for (TupleType t : TupleType.values()) {
            TYPE_NAMES.put(t.getName(), t);
        }
    }

    static final Map<Integer, Width> WIDTH_MAP = new HashMap<Integer, Width>();

    static {
        for (Width w : Width.values()) {
            WIDTH_MAP.put(w.value, w);
        }
    }

    /**
     * The integer key identifying the tuple.
     */
    public final int key;
    /**
     * The type of value contained in the tuple.
     */
    public final TupleType type;
    /**
     * The 'width' of the tuple's value; This value will always be 'NONE' for non-integer types.
     */
    public final Width width;
    /**
     * The length of the tuple's value in bytes.
     */
    public final int length;
    /**
     * The value being associated with the tuple's key.
     */
    public final Object value;

    private PebbleTuple(final int key, final TupleType type, final Width width, final int length, final Object value) {
        this.key = key;
        this.type = type;
        this.width = width;
        this.length = length;
        this.value = value;
    }

    public static PebbleTuple create(
            final int key, final TupleType type, final Width width, final int value) {
        return new PebbleTuple(key, type, width, width.value, Long.valueOf(value));
    }

    public static PebbleTuple create(
            final int key, final TupleType type, final Width width, final Object value) {

        int length = Integer.MAX_VALUE;
        if (width != Width.NONE) {
            length = width.value;
        } else if (type == TupleType.BYTES) {
            length = ((byte[]) value).length;
        } else if (type == TupleType.STRING) {
            length = ((String) value).getBytes(UTF8).length;
        }

        if (length > 0xffff) {
            throw new ValueOverflowException();
        }

        return new PebbleTuple(key, type, width, length, value);
    }

    public static class ValueOverflowException extends RuntimeException {
        public ValueOverflowException() {
            super("Value exceeds tuple capacity");
        }
    }

    public static enum Width {
        NONE(0),
        BYTE(1),
        SHORT(2),
        WORD(4);

        public final int value;

        private Width(final int width) {
            value = width;
        }

        public static Width fromValue(int widthValue) {
            for (Width width : values()) {
                if (widthValue == width.value) {
                    return width;
                }
            }

            throw new IllegalArgumentException("Unknown width value: " + widthValue);
        }
    }

    public static enum TupleType {
        BYTES(0),
        STRING(1),
        UINT(2),
        INT(3);

        public final byte ord;

        private TupleType(int ord) {
            this.ord = (byte) ord;
        }

        public String getName() {
            return name().toLowerCase(Locale.US);
        }
    }
}

